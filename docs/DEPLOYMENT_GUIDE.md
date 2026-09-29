# GlobalTrade Deployment Guide

## 1. Requirements

Install:

- JDK 17
- Maven 3.9+
- Payara Server 6
- MySQL 8

## 2. Database setup

1. Start MySQL.
2. Run `database/globaltrade_mysql.sql` using HeidiSQL or the MySQL client.
3. Open:

   `globaltrade-ear/src/main/application/META-INF/glassfish-resources.xml`

4. Update the datasource values with the MySQL details used on the computer:

```xml
<property name="url" value="jdbc:mysql://localhost:3306/globaltrade_scm?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=Asia/Colombo"/>
<property name="user" value="root"/>
<property name="password" value=""/>
```

- Change the port if MySQL is not using `3306`.
- Change the MySQL username if required.
- Add the MySQL password inside `value="..."`. Keep `value=""` when the MySQL user has no password.

## 3. Build the project

From the project root run:

```bash
mvn clean package
```

The EAR file will be created at:

`globaltrade-ear/target/globaltrade-scm.ear`

## 4. Deploy to Payara

Start Payara 6 and deploy the EAR through IntelliJ or run:

```bash
asadmin deploy globaltrade-ear/target/globaltrade-scm.ear
```

Open:

`http://localhost:8080/globaltrade/`
