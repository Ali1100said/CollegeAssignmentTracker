package com.collegetracker;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final StudentDao studentDao = new StudentDao();
    private final CourseDao courseDao = new CourseDao();
    private final AssignmentDao assignmentDao = new AssignmentDao();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        try {
            DatabaseSetup.initialize();

            request.setAttribute(
                "profile", studentDao.findProfile(studentId)
            );
            request.setAttribute(
                "courses", courseDao.findByStudent(studentId)
            );
            request.setAttribute(
                "assignments", assignmentDao.findByStudent(studentId)
            );

            request.getRequestDispatcher("/WEB-INF/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Unable to load dashboard.", e);
        }
    }
}
