package com.collegetracker;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

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
            if (session != null) session.invalidate();
            response.sendRedirect("index.jsp");
            return;
        }

        try {
            DatabaseSetup.initialize();
            switch (action == null ? "" : action) {
                case "register" -> register(request, response);
                case "login" -> login(request, response);
                case "verify" -> verify(request, response);
                case "forgot" -> forgot(request, response);
                case "reset" -> reset(request, response);
                default -> response.sendError(400, "Invalid action.");
            }
        } catch (MessagingException e) {
            response.sendError(503,
                "Email delivery is unavailable. Check SMTP configuration and try again.");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, MessagingException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        if (blank(username) || blank(password) || blank(email)) {
            response.sendError(400, "All fields are required.");
            return;
        }
        authService.register(username.trim(), password,
            email.trim().toLowerCase());
        response.sendRedirect("index.jsp?registered=true");
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (blank(username) || blank(password)) {
            response.sendError(400, "Username and password required.");
            return;
        }
        StudentDao.LoginRecord record =
            authService.authenticate(username, password);
        if (record == null) {
            response.sendRedirect("index.jsp?error=login");
            return;
        }
        boolean legacyAccount = record.email() == null;
        if (!record.emailVerified() && !legacyAccount) {
            response.sendRedirect("index.jsp?error=unverified");
            return;
        }
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("studentId", record.id());
        session.setAttribute("username", username);
        response.sendRedirect("dashboard");
    }

    private void verify(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String email = request.getParameter("email");
        String token = request.getParameter("token");
        if (blank(email) || blank(token)) {
            response.sendError(400, "Email and code required.");
            return;
        }
        boolean verified = authService.verify(
            email.trim().toLowerCase(), token.trim());
        response.sendRedirect(verified
            ? "index.jsp?status=verified"
            : "index.jsp?error=verification");
    }

    private void forgot(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, MessagingException {
        String email = request.getParameter("email");
        if (blank(email)) {
            response.sendError(400, "Email required.");
            return;
        }
        authService.requestPasswordReset(email.trim().toLowerCase());
        response.sendRedirect("index.jsp?status=resetrequested");
    }

    private void reset(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String email = request.getParameter("email");
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        if (blank(email) || blank(token) || blank(newPassword)) {
            response.sendError(400, "All fields required.");
            return;
        }
        boolean reset = authService.resetPassword(
            email.trim().toLowerCase(), token.trim(), newPassword);
        response.sendRedirect(reset
            ? "index.jsp?status=reset"
            : "index.jsp?error=reset");
    }
}
