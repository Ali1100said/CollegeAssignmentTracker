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

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("studentId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");
        String action = request.getParameter("action");

        // Keep the original Add Course form working.
        if (action == null) {
            action = "add";
        }

        try {
            DatabaseSetup.initialize();

            if ("add".equals(action)) {

                String name = request.getParameter("courseName");
                if (name == null || name.isBlank()
                        || name.length() > 150) {
                    response.sendError(400, "Invalid course name.");
                    return;
                }

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO courses (student_id, course_name) " +
                         "VALUES (?, ?)")) {

                    stmt.setInt(1, studentId);
                    stmt.setString(2, name.trim());
                    stmt.executeUpdate();
                }

            } else if ("edit".equals(action)) {

                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));
                String name = request.getParameter("courseName");

                if (name == null || name.isBlank()
                        || name.length() > 150) {
                    response.sendError(400, "Invalid course name.");
                    return;
                }

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE courses SET course_name = ? " +
                         "WHERE id = ? AND student_id = ?")) {

                    stmt.setString(1, name.trim());
                    stmt.setInt(2, courseId);
                    stmt.setInt(3, studentId);
                    stmt.executeUpdate();
                }

            } else if ("delete".equals(action)) {

                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));

                try (Connection conn = Database.getConnection()) {
                    conn.setAutoCommit(false);

                    try {
                        try (PreparedStatement stmt =
                                conn.prepareStatement(
                                "DELETE FROM assignments " +
                                "WHERE course_id = ? AND course_id IN " +
                                "(SELECT id FROM courses " +
                                "WHERE student_id = ?)")) {

                            stmt.setInt(1, courseId);
                            stmt.setInt(2, studentId);
                            stmt.executeUpdate();
                        }

                        try (PreparedStatement stmt =
                                conn.prepareStatement(
                                "DELETE FROM courses " +
                                "WHERE id = ? AND student_id = ?")) {

                            stmt.setInt(1, courseId);
                            stmt.setInt(2, studentId);
                            stmt.executeUpdate();
                        }

                        conn.commit();

                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    }
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("dashboard.jsp");

        } catch (IllegalArgumentException e) {
            response.sendError(400, "Invalid course information.");
        } catch (SQLException e) {
            throw new ServletException("Database error.", e);
        }
    }
}
