
package com.collegetracker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()) {
            response.sendError(400, "Name and email are required.");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        try {
            DatabaseSetup.initialize();

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE students SET full_name = ?, email = ? WHERE id = ?")) {

                stmt.setString(1, fullName.trim());
                stmt.setString(2, email.trim());
                stmt.setInt(3, studentId);
                stmt.executeUpdate();
            }

            response.sendRedirect("dashboard.jsp?profile=updated");

        } catch (SQLException e) {
            throw new ServletException("Unable to update profile.", e);
        }
    }
}
