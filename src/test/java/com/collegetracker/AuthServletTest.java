package com.collegetracker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;

class AuthServletTest {
    @Test
    void logoutInvalidatesExistingSessionAndReturnsToLoginPage() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getSession(false)).thenReturn(session);

        new AuthServlet().doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("index.jsp");
    }
}
