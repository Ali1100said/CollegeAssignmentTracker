package com.collegetracker;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/assignments")
public class AssignmentServlet extends HttpServlet {

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

        try {
            DatabaseSetup.initialize();

            if ("add".equals(action)) {

                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));
                String title = request.getParameter("title");
                String dueDate = request.getParameter("dueDate");

                if (!validTitle(title)) {
                    response.sendError(400, "Invalid assignment title.");
                    return;
                }

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO assignments " +
                         "(course_id, title, due_date) " +
                         "SELECT id, ?, ? FROM courses " +
                         "WHERE id = ? AND student_id = ?")) {

                    stmt.setString(1, title.trim());
                    setDueDate(stmt, 2, dueDate);
                    stmt.setInt(3, courseId);
                    stmt.setInt(4, studentId);

                    if (stmt.executeUpdate() == 0) {
                        response.sendError(403, "Course not found.");
                        return;
                    }
                }

            } else if ("edit".equals(action)) {

                int assignmentId = Integer.parseInt(
                    request.getParameter("assignmentId"));
                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));
                String title = request.getParameter("title");
                String dueDate = request.getParameter("dueDate");

                if (!validTitle(title)) {
                    response.sendError(400, "Invalid assignment title.");
                    return;
                }

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE assignments SET course_id = ?, " +
                         "title = ?, due_date = ? " +
                         "WHERE id = ? " +
                         "AND course_id IN " +
                         "(SELECT id FROM courses WHERE student_id = ?) " +
                         "AND EXISTS (SELECT 1 FROM courses " +
                         "WHERE id = ? AND student_id = ?)")) {

                    stmt.setInt(1, courseId);
                    stmt.setString(2, title.trim());
                    setDueDate(stmt, 3, dueDate);
                    stmt.setInt(4, assignmentId);
                    stmt.setInt(5, studentId);
                    stmt.setInt(6, courseId);
                    stmt.setInt(7, studentId);
                    stmt.executeUpdate();
                }

            } else if ("complete".equals(action)
                    || "pending".equals(action)) {

                int assignmentId = Integer.parseInt(
                    request.getParameter("assignmentId"));

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE assignments SET completed = ? " +
                         "WHERE id = ? AND course_id IN " +
                         "(SELECT id FROM courses WHERE student_id = ?)")) {

                    stmt.setBoolean(1, "complete".equals(action));
                    stmt.setInt(2, assignmentId);
                    stmt.setInt(3, studentId);
                    stmt.executeUpdate();
                }

            } else if ("delete".equals(action)) {

                int assignmentId = Integer.parseInt(
                    request.getParameter("assignmentId"));

                try (Connection conn = Database.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM assignments " +
                         "WHERE id = ? AND course_id IN " +
                         "(SELECT id FROM courses WHERE student_id = ?)")) {

                    stmt.setInt(1, assignmentId);
                    stmt.setInt(2, studentId);
                    stmt.executeUpdate();
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("dashboard.jsp");

        } catch (IllegalArgumentException e) {
            response.sendError(400, "Invalid assignment information.");
        } catch (SQLException e) {
            throw new ServletException("Database error.", e);
        }
    }

    private boolean validTitle(String title) {
        return title != null
                && !title.isBlank()
                && title.length() <= 200;
    }

    private void setDueDate(PreparedStatement stmt,
                            int position, String dueDate)
            throws SQLException {

        if (dueDate == null || dueDate.isBlank()) {
            stmt.setNull(position, java.sql.Types.DATE);
        } else {
            stmt.setDate(position, Date.valueOf(dueDate));
        }
    }
}
