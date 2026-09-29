package com.globaltrade.scm.web.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/app/*", "/api/*", "/login.jsp", "/index.jsp", "/login-error.jsp", "/access-denied.jsp", "/login", "/logout", "/session-status"})
public class NoCacheFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse http = (HttpServletResponse) response;
        http.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0");
        http.setHeader("Pragma", "no-cache");
        http.setDateHeader("Expires", 0);
        http.setHeader("Surrogate-Control", "no-store");
        chain.doFilter(request, response);
    }
}
