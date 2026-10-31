package com.collegetracker;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {

    private final CourseDao courseDao = new CourseDao();

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

                courseDao.add(studentId, name.trim());

            } else if ("edit".equals(action)) {

                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));
                String name = request.getParameter("courseName");

                if (name == null || name.isBlank()
                        || name.length() > 150) {
                    response.sendError(400, "Invalid course name.");
                    return;
                }

                courseDao.update(studentId, courseId, name.trim());

            } else if ("delete".equals(action)) {

                int courseId = Integer.parseInt(
                    request.getParameter("courseId"));

                courseDao.delete(studentId, courseId);

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("dashboard");

        } catch (IllegalArgumentException e) {
            response.sendError(400, "Invalid course information.");
        } catch (SQLException e) {
            throw new ServletException("Database error.", e);
        }
    }
}
