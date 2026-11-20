package com.collegetracker;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.collegetracker.service.DashboardFacade;
import com.collegetracker.service.DashboardFacade.DashboardData;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final DashboardFacade dashboardFacade = new DashboardFacade();

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

            DashboardData data = dashboardFacade.loadDashboard(
                studentId, request.getParameter("sort"));
            request.setAttribute("profile", data.profile());
            request.setAttribute("courses", data.courses());
            request.setAttribute("assignments", data.assignments());
            request.setAttribute("sortKey", data.sortKey());

            request.getRequestDispatcher("/WEB-INF/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Unable to load dashboard.", e);
        }
    }
}
