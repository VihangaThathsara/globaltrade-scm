# Testing and Validation

## Testing

Run the normal JUnit 5 test suite from the project root:

```bash
mvn clean test
```

The tests cover the main business rules, exception behavior, timer configuration, transaction setup, interceptor attachment, carrier handling, route-risk/optimization decision logic, replenishment recommendations and password hashing.

If Payara 6 is running, the optional Arquillian integration tests can also be run with:

```bash
mvn -Parquillian -Darquillian.admin.password=<your-admin-password> -pl globaltrade-ejb -am verify
```

## Validation

After deployment, check these main flows manually:

- Login with the available user roles and confirm access restrictions.
- Add, review and deactivate partners.
- Add and update inventory and confirm low-stock alerts include a replenishment recommendation when the preferred minimum is reached.
- Create shipments, update shipment status and test cancellation rules.
- Check customs submission, approval and rejection flows.
- Confirm timer-based alerts and monitoring records are created correctly, including route optimization recommendations for risky routes.
- Confirm invalid vendor data and unavailable carrier scenarios are handled without saving incorrect data.
- Perform an audited update and confirm Activity Trail records the actual caller username.
- Logout and verify protected pages cannot be reopened from the browser cache.
