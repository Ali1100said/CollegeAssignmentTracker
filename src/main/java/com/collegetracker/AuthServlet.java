package com.collegetracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            response.sendError(400, "Username and password required.");
            return;
        }

        try {
            DatabaseSetup.initialize();

            if ("register".equals(action)) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO students (username, password_hash) VALUES (?, ?)")) {

                    stmt.setString(1, username);
                    stmt.setString(2, hashPassword(password));
                    stmt.executeUpdate();
                }

                response.sendRedirect("index.jsp?registered=true");

            } else if ("login".equals(action)) {
                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "SELECT id, password_hash FROM students WHERE username = ?")) {

                    stmt.setString(1, username);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()
                                && rs.getString("password_hash")
                                     .equals(hashPassword(password))) {

                            HttpSession session = request.getSession();
                            request.changeSessionId();
                            session.setAttribute("studentId", rs.getInt("id"));
                            session.setAttribute("username", username);

                            response.sendRedirect("dashboard.jsp");
                        } else {
                            response.sendRedirect("index.jsp?error=login");
                        }
                    }
                }
            } else {
                response.sendError(400, "Invalid action.");
            }

        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
