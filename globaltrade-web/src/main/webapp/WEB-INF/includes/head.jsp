<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#061225">
<meta http-equiv="Cache-Control" content="no-store, no-cache, must-revalidate, max-age=0">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="0">
<% if (request.getRequestURI().contains("/app/")) { %>
<script>document.documentElement.classList.add('gt-session-checking');</script>
<% } %>
<link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/images/globaltrade-logo.png">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/globaltrade.css">
