<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (request.getUserPrincipal() != null) {
        response.sendRedirect(request.getContextPath() + "/app/dashboard.jsp");
        return;
    }
    boolean loginError = "1".equals(request.getParameter("error"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Secure Access</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="login-body">
<div class="login-grid-bg"></div>
<div class="login-orbit orbit-one"></div>
<div class="login-orbit orbit-two"></div>
<main class="login-shell">
    <section class="login-visual">
        <img class="login-hero-logo" src="<%=request.getContextPath()%>/assets/images/globaltrade-logo.png" alt="GlobalTrade logistics network">
        <div class="login-brand-copy">
            <p class="eyebrow">GLOBALTRADE LOGISTICS CORPORATION</p>
            <h1>Move with clarity.<br><span>Operate globally.</span></h1>
            <p>A unified workspace for shipments, inventory, partners and international trade operations.</p>
        </div>
        <div class="network-strip">
            <div><strong>50+</strong><span>Markets</span></div>
            <div><strong>24/7</strong><span>Visibility</span></div>
            <div><strong>Global</strong><span>Network</span></div>
        </div>
    </section>

    <section class="login-panel-wrap">
        <div class="login-panel">
            <div class="mobile-brand">
                <img src="<%=request.getContextPath()%>/assets/images/globaltrade-logo.png" alt="GlobalTrade">
            </div>
            <div class="login-title">
                <p class="eyebrow">OPERATIONS PORTAL</p>
                <h2>Welcome back</h2>
                <p>Sign in to continue to GlobalTrade operations.</p>
            </div>

            <% if (loginError) { %>
            <div class="login-error-message" role="alert">
                <span>!</span>
                <div><strong>Sign-in failed</strong><p>Check your username and password and try again.</p></div>
            </div>
            <% } %>

            <form class="login-form" method="post" action="<%=request.getContextPath()%>/login" autocomplete="off">
                <label class="field-label" for="username">Username</label>
                <div class="field-control">
                    <input id="username" name="username" type="text" required autofocus autocomplete="off">
                </div>

                <label class="field-label" for="password">Password</label>
                <div class="field-control password-control">
                    <input id="password" name="password" type="password" required autocomplete="off">
                    <button type="button" class="password-toggle" id="passwordToggle" aria-label="Show password" aria-pressed="false">
                        <svg class="eye-icon eye-open" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6S2.5 12 2.5 12Z"></path>
                            <circle cx="12" cy="12" r="2.7"></circle>
                        </svg>
                        <svg class="eye-icon eye-closed" viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M3 3l18 18"></path>
                            <path d="M10.6 6.2A9.3 9.3 0 0 1 12 6c6 0 9.5 6 9.5 6a15.8 15.8 0 0 1-3 3.6"></path>
                            <path d="M6.2 6.2C3.8 8 2.5 12 2.5 12s3.5 6 9.5 6a9.6 9.6 0 0 0 4.1-.9"></path>
                            <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"></path>
                        </svg>
                    </button>
                </div>

                <button class="primary-action login-action" type="submit">Sign in <span>→</span></button>
            </form>
            <div class="login-foot">Authorized access only</div>
        </div>
    </section>
</main>
<script>
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const clearLoginFields = () => {
        if (usernameInput) usernameInput.value = '';
        if (passwordInput) passwordInput.value = '';
    };
    clearLoginFields();
    try { history.replaceState(null, '', '<%=request.getContextPath()%>/login.jsp'); } catch (_) {}
    window.addEventListener('pagehide', clearLoginFields);
    window.addEventListener('pageshow', async () => {
        clearLoginFields();
        try {
            const response = await fetch('<%=request.getContextPath()%>/session-status', {cache:'no-store', credentials:'same-origin'});
            const data = await response.json();
            if (data?.authenticated) window.location.replace('<%=request.getContextPath()%>/app/dashboard.jsp');
        } catch (_) {}
    });

    const passwordToggle = document.getElementById('passwordToggle');

    passwordToggle.addEventListener('click', () => {
        const isHidden = passwordInput.type === 'password';
        passwordInput.type = isHidden ? 'text' : 'password';
        passwordToggle.classList.toggle('showing', isHidden);
        passwordToggle.setAttribute('aria-label', isHidden ? 'Hide password' : 'Show password');
        passwordToggle.setAttribute('aria-pressed', String(isHidden));
    });
</script>
</body>
</html>
