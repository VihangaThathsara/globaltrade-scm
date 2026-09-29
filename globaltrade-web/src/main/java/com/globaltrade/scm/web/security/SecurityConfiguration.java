package com.globaltrade.scm.web.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:app/jdbc/GlobalTradeDS",
        callerQuery = "SELECT password_hash FROM users WHERE username = ? AND active = 1",
        groupsQuery = "SELECT group_name FROM user_groups WHERE username = ?",
        hashAlgorithm = Pbkdf2PasswordHash.class,
        priority = 30
)
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.jsp",
                errorPage = "/login-error.jsp",
                useForwardToLogin = false
        )
)
@ApplicationScoped
public class SecurityConfiguration {
}
