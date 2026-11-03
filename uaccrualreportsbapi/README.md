# Configurations
All configuration MUST be set in the `src/main/resources/application-<profile>.yml` file of the Spring profile used at runtime.
The profile is selected by the Maven profile used for compilation or by the `SPRING_PROFILES_ACTIVE` environment variable.
The environment files are not versioned: use `application-local.yml.sample` as template.
The properties set in `application.yml` are shared by all the environments and should not be overridden.

BUILD CONFIGURATION (`buildfiles/local.properties`, see `buildfiles/sample.properties`)
- `java.home` = the Java 17 JDK home folder used by the `exec-*.sh` build scripts

SERVER CONFIGURATION
- `server.port` = the HTTP port of the service
- `server.servlet.context-path` = the context path of the service. Set to `/accrualreportsbapi` in `application.yml`
- `management.endpoints.web.exposure.include` = the Spring Boot Actuator endpoints to be exposed
- `management.endpoint.health.show-details` = when the details of the health endpoint are shown
- `springdoc.swagger-ui.enabled` = specifies whether the Swagger UI is exposed
- `springdoc.api-docs.enabled` = specifies whether the OpenAPI documentation is exposed

DATABASE CONFIGURATION (`ACCRUAL` database, see accrualdbimpl)
- `spring.datasource.url` = JDBC URL of the database. It is also passed to the BIRT report templates
- `spring.datasource.username` = database user. It is also passed to the BIRT report templates
- `spring.datasource.password` = database password. It is also passed to the BIRT report templates
- `spring.datasource.driver-class-name` = JDBC driver class (`org.postgresql.Driver`)
- `spring.datasource.hikari.minimum-idle` = minimum number of idle connections in the pool
- `spring.datasource.hikari.maximum-pool-size` = maximum number of connections in the pool
- `spring.datasource.hikari.connection-timeout` = maximum wait for a connection from the pool, in milliseconds
- `spring.datasource.hikari.idle-timeout` = maximum idle time of a connection, in milliseconds
- `spring.datasource.hikari.max-lifetime` = maximum lifetime of a connection, in milliseconds
- `spring.datasource.hikari.auto-commit` = auto-commit behaviour of the connections
- `spring.datasource.hikari.pool-name` = name of the connection pool

SECURITY CONFIGURATION (read by accrualcommonsrv)
- `jwt.secret` = secret used to validate the JWT issued by accrualauthsbapi. MUST be the same in all the backend services
- `jwt.expiration` = validity of the JWT, in milliseconds
- `security.public-endpoints` = list of the paths accessible without authentication
- `dev.auth.enabled` = enables the development authentication, based on base64-encoded tokens instead of JWT.
  MUST be `false` (or not set) in every environment other than the local development one
- `cors.allowed-origins` = origins allowed by the CORS policy
- `cors.allowed-methods` = HTTP methods allowed by the CORS policy
- `cors.allowed-headers` = headers allowed by the CORS policy
- `cors.allow-credentials` = specifies whether the CORS policy allows credentials
- `cors.max-age` = validity of the CORS preflight response, in seconds

CONFIGURATION FOR THE BIRT REPORT ENGINE
- `birt.reports.directory` = folder containing the `.rptdesign` report templates of accrualreport
- `birt.locale` = locale used to render the reports (default `it_IT`)
- `birt.render.temp.dir` = working folder for the report rendering (default the Java temporary folder)
- `birt.render.timeout-seconds` = maximum duration of a report rendering, in seconds
- `birt.file.max-size` = maximum size of a generated report file, in bytes (default 52428800)

LOGGING CONFIGURATION
- `logging.level.<package>` = log level for the given package
- `logging.pattern.console` = log pattern for the console
- `logging.pattern.file` = log pattern for the log file
- `logging.file.name` = the log file. May refer to the `LOG_PATH` environment variable
- `logging.file.max-size` = maximum size of a log file before rolling
- `logging.file.max-history` = number of rolled log files to be kept
