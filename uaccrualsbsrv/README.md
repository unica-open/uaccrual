# Configurations
All configuration MUST be set in the `src/main/resources/application-<profile>.yml` file of the Spring profile used at runtime.
The profile is selected by the Maven profile used for compilation or by the `SPRING_PROFILES_ACTIVE` environment variable.
The environment files are not versioned: use `application-local.yml.sample` as template.
The properties set in `application.yml` are shared by all the environments and should not be overridden.

BUILD CONFIGURATION (`buildfiles/local.properties`, see `buildfiles/sample.properties`)
- `java.home` = the Java 17 JDK home folder used by the `exec-*.sh` build scripts

SERVER CONFIGURATION
- `server.port` = the HTTP port of the service
- `server.servlet.context-path` = the context path of the service. Set to `/accrualsbsrv` in `application.yml`
- `management.endpoints.web.exposure.include` = the Spring Boot Actuator endpoints to be exposed

DATABASE CONFIGURATION (`ACCRUAL` database, see accrualdbimpl)
- `spring.datasource.url` = JDBC URL of the database
- `spring.datasource.username` = database user
- `spring.datasource.password` = database password
- `spring.datasource.driver-class-name` = JDBC driver class (`org.postgresql.Driver`)
- `spring.jpa.show-sql` = specifies whether the JPA-generated SQL should be logged
- `spring.jpa.properties.hibernate.format_sql` = specifies whether the JPA-generated SQL should be formatted

SECURITY CONFIGURATION (read by accrualcommonsrv)
- `jwt.secret` = secret used to validate the JWT issued by accrualauthsbapi. MUST be the same in all the backend services
- `jwt.expiration` = validity of the JWT, in milliseconds
- `security.public-endpoints` = list of the paths accessible without authentication
- `dev.auth.enabled` = enables the development authentication, based on base64-encoded tokens instead of JWT.
  MUST be `false` (or not set) in every environment other than the local development one

LOGGING CONFIGURATION
- `logging.level.<package>` = log level for the given package
- `logging.pattern.console` = log pattern for the console
- `logging.pattern.file` = log pattern for the log file
- `logging.file.name` = the log file. May refer to the `LOG_PATH` environment variable
- `logging.logback.rollingpolicy.max-size` = maximum size of a log file before rolling
- `logging.logback.rollingpolicy.max-history` = number of rolled log files to be kept
