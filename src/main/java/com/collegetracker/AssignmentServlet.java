package com.collegetracker;

import java.io.IOException;
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

    private final AssignmentDao assignmentDao = new AssignmentDao();

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

                if (!assignmentDao.add(studentId, courseId, title.trim(),
                        parseDueDate(dueDate))) {
                    response.sendError(403, "Course not found.");
                    return;
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

                assignmentDao.update(studentId, assignmentId, courseId,
                    title.trim(), parseDueDate(dueDate));

            } else if ("complete".equals(action)
                    || "pending".equals(action)) {

                int assignmentId = Integer.parseInt(
                    request.getParameter("assignmentId"));

                assignmentDao.setCompleted(studentId, assignmentId,
                    "complete".equals(action));

            } else if ("delete".equals(action)) {

                int assignmentId = Integer.parseInt(
                    request.getParameter("assignmentId"));

                assignmentDao.delete(studentId, assignmentId);

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("dashboard");

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

    private Date parseDueDate(String dueDate) {
        return dueDate == null || dueDate.isBlank()
            ? null : Date.valueOf(dueDate);
    }
}
