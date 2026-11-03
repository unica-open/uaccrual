# Configurations
All configuration MUST be set in the `src/main/resources/application-<profile>.yml` file of the Spring profile used at runtime.
The profile is selected by the Maven profile used for compilation (`local`, `consip`, `coll`, `forn1`, `forn2`,
`prod-rp-01`, `prod-cmto-01`, `prod-int-01`) or by the `SPRING_PROFILES_ACTIVE` environment variable.
The environment files are not versioned: use `application-local.yml.sample` as template.
The properties set in `application.yml` are shared by all the environments and should not be overridden.

BUILD CONFIGURATION (`buildfiles/local.properties`, see `buildfiles/sample.properties`)
- `java.home` = the Java 17 JDK home folder used by the `exec-*.sh` build scripts

SERVER CONFIGURATION
- `server.port` = the HTTP port of the service
- `server.servlet.context-path` = the context path of the service. Set to `/accrualauthsbapi` in `application.yml`

DATABASE CONFIGURATION (`ACCRUALAUTH` database, `accrualauth` schema, see accrualauthdbimpl)
- `spring.datasource.url` = JDBC URL of the database, with `currentSchema=accrualauth`
- `spring.datasource.username` = database user
- `spring.datasource.password` = database password
- `spring.datasource.driver-class-name` = JDBC driver class (`org.postgresql.Driver`)
- `spring.datasource.hikari.minimum-idle` = minimum number of idle connections in the pool
- `spring.datasource.hikari.maximum-pool-size` = maximum number of connections in the pool
- `spring.datasource.hikari.connection-timeout` = maximum wait for a connection from the pool, in milliseconds
- `spring.datasource.hikari.idle-timeout` = maximum idle time of a connection, in milliseconds
- `spring.datasource.hikari.max-lifetime` = maximum lifetime of a connection, in milliseconds
- `spring.datasource.hikari.auto-commit` = auto-commit behaviour of the connections
- `spring.datasource.hikari.pool-name` = name of the connection pool
- `spring.flyway.enabled` = no more used. MUST be left to `false`: the schema is managed by accrualauthdbimpl

SECURITY CONFIGURATION
- `jwt.secret` = secret used to sign the issued JWT. MUST be the same in all the backend services
- `jwt.expiration` = validity of the issued JWT, in milliseconds
- `security.public-endpoints` = list of the paths accessible without authentication (login, SSO, password reset)
- `cors.allowed-origins` = origins allowed by the CORS policy (default `*`)
- `cors.allowed-methods` = HTTP methods allowed by the CORS policy (default `GET,POST,OPTIONS`)
- `cors.allowed-headers` = headers allowed by the CORS policy (default `*`)
- `cors.allow-credentials` = specifies whether the CORS policy allows credentials (default `false`)
- `cors.max-age` = validity of the CORS preflight response, in seconds (default 3600)

CONFIGURATION FOR INTEGRATION TO CSI Shibboleth SSO
- No property is required: the `Shib-Iride-IdentitaDigitale` header is injected by the web server
  protecting the `/api/v1/auth/sso-shibboleth` path with the Shibboleth Service Provider

CONFIGURATION FOR THE PASSWORD RESET MAIL
- `spring.mail.host` = hostname of the SMTP server
- `spring.mail.port` = port of the SMTP server
- `spring.mail.username` = user of the SMTP server
- `spring.mail.password` = password of the SMTP server
- `spring.mail.properties.mail.smtp.auth` = specifies whether the SMTP server requires authentication
- `spring.mail.properties.mail.smtp.starttls.enable` = specifies whether STARTTLS should be used
- `app.mail.username` = sender address of the mails
- `app.mail.token-reset-pwd-expiration` = validity of the password reset token, in milliseconds
- `app.mail.reset-link` = URL of the password reset page of accrualwcl. The token is appended to it

LOGGING CONFIGURATION
- `logging.level.<package>` = log level for the given package
- `logging.pattern.console` = log pattern for the console
- `logging.pattern.file` = log pattern for the log file
- `logging.file.name` = the log file. May refer to the `LOG_PATH` environment variable
- `logging.file.max-size` = maximum size of a log file before rolling
- `logging.file.max-history` = number of rolled log files to be kept
