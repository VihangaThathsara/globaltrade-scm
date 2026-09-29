package com.globaltrade.scm.web.servlet;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getUserPrincipal() != null) {
            response.sendRedirect(request.getContextPath() + "/app/dashboard.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=1");
            return;
        }

        UsernamePasswordCredential credential = new UsernamePasswordCredential(
                username.trim(), new Password(password));

        AuthenticationStatus status = securityContext.authenticate(
                request,
                response,
                AuthenticationParameters.withParams().credential(credential)
        );

        if (status == AuthenticationStatus.SUCCESS) {
            if (!response.isCommitted()) {
                response.sendRedirect(request.getContextPath() + "/app/dashboard.jsp");
            }
            return;
        }

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return;
        }

        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=1");
        }
    }
}
