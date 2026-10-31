package com.collegetracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    private String hashPassword(String password) {
        return hash(password);
    }

    private String hash(String value) {
        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(bytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect("index.jsp");
            return;
        }

        try {
            DatabaseSetup.initialize();

            switch (action == null ? "" : action) {
                case "register":
                    register(request, response);
                    break;

                case "login":
                    login(request, response);
                    break;

                case "verify":
                    verify(request, response);
                    break;

                case "forgot":
                    forgot(request, response);
                    break;

                case "reset":
                    reset(request, response);
                    break;

                default:
                    response.sendError(400, "Invalid action.");
            }

        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void register(HttpServletRequest request,
                          HttpServletResponse response)
            throws SQLException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        if (blank(username) || blank(password) || blank(email)) {
            response.sendError(400, "All fields are required.");
            return;
        }

        String token = generateToken();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO students " +
                "(username, password_hash, email, " +
                "email_verified, verification_token_hash) " +
                "VALUES (?, ?, ?, FALSE, ?)")) {

            stmt.setString(1, username.trim());
            stmt.setString(2, hashPassword(password));
            stmt.setString(3, email.trim().toLowerCase());
            stmt.setString(4, hash(token));

            stmt.executeUpdate();
        }

        // Development testing only.
        // Real email delivery is not configured yet.
        System.out.println(
            "DEVELOPMENT VERIFICATION CODE for " +
            email + ": " + token
        );

        response.sendRedirect("index.jsp?registered=true");
    }

    private void login(HttpServletRequest request,
                       HttpServletResponse response)
            throws SQLException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (blank(username) || blank(password)) {
            response.sendError(
                400, "Username and password required."
            );
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, password_hash, email, " +
                "email_verified FROM students WHERE username = ?")) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next() &&
                    rs.getString("password_hash")
                      .equals(hashPassword(password))) {

                    boolean verified =
                        rs.getBoolean("email_verified");

                    // Older accounts had no email field.
                    boolean legacyAccount =
                        rs.getString("email") == null;

                    // Require verification only for new accounts.
                    if (!verified && !legacyAccount) {
                        response.sendRedirect(
                            "index.jsp?error=unverified"
                        );
                        return;
                    }

                    HttpSession oldSession =
                        request.getSession(false);

                    if (oldSession != null) {
                        oldSession.invalidate();
                    }

                    HttpSession session =
                        request.getSession(true);

                    session.setAttribute(
                        "studentId", rs.getInt("id")
                    );

                    session.setAttribute(
                        "username", username
                    );

                    response.sendRedirect("dashboard");

                } else {
                    response.sendRedirect(
                        "index.jsp?error=login"
                    );
                }
            }
        }
    }

    private void verify(HttpServletRequest request,
                        HttpServletResponse response)
            throws SQLException, IOException {

        String email = request.getParameter("email");
        String token = request.getParameter("token");

        if (blank(email) || blank(token)) {
            response.sendError(
                400, "Email and code required."
            );
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE students SET email_verified = TRUE, " +
                "verification_token_hash = NULL " +
                "WHERE LOWER(email) = ? " +
                "AND verification_token_hash = ? " +
                "AND email_verified = FALSE")) {

            stmt.setString(
                1, email.trim().toLowerCase()
            );

            stmt.setString(2, hash(token.trim()));

            int updated = stmt.executeUpdate();

            response.sendRedirect(
                updated == 1
                ? "index.jsp?status=verified"
                : "index.jsp?error=verification"
            );
        }
    }

    private void forgot(HttpServletRequest request,
                        HttpServletResponse response)
            throws SQLException, IOException {

        String email = request.getParameter("email");

        if (blank(email)) {
            response.sendError(400, "Email required.");
            return;
        }

        String token = generateToken();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE students SET reset_token_hash = ?, " +
                "reset_token_expires = ? " +
                "WHERE LOWER(email) = ?")) {

            stmt.setString(1, hash(token));

            stmt.setTimestamp(
                2,
                Timestamp.from(
                    Instant.now().plusSeconds(900)
                )
            );

            stmt.setString(
                3, email.trim().toLowerCase()
            );

            int updated = stmt.executeUpdate();

            if (updated == 1) {
                // Development testing only.
                System.out.println(
                    "DEVELOPMENT PASSWORD RESET CODE for " +
                    email + ": " + token
                );
            }
        }

        response.sendRedirect(
            "index.jsp?status=resetrequested"
        );
    }

    private void reset(HttpServletRequest request,
                       HttpServletResponse response)
            throws SQLException, IOException {

        String email = request.getParameter("email");
        String token = request.getParameter("token");

        String newPassword =
            request.getParameter("newPassword");

        if (blank(email) || blank(token) ||
                blank(newPassword)) {

            response.sendError(
                400, "All fields required."
            );
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE students SET password_hash = ?, " +
                "reset_token_hash = NULL, " +
                "reset_token_expires = NULL " +
                "WHERE LOWER(email) = ? " +
                "AND reset_token_hash = ? " +
                "AND reset_token_expires > CURRENT_TIMESTAMP")) {

            stmt.setString(
                1, hashPassword(newPassword)
            );

            stmt.setString(
                2, email.trim().toLowerCase()
            );

            stmt.setString(
                3, hash(token.trim())
            );

            int updated = stmt.executeUpdate();

            response.sendRedirect(
                updated == 1
                ? "index.jsp?status=reset"
                : "index.jsp?error=reset"
            );
        }
    }
}
