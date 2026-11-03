# Configurations
This module is a library: it has no runtime configuration of its own.
The properties below are read from the configuration of the backend service including it
(accrualsbsrv, accrualsbapi, accrualauthsbapi, accrualreportsbapi), and MUST be set there.

The library must be built and installed in the local Maven repository (`install-local.sh`)
before building the backend services.

BUILD CONFIGURATION (`buildfiles/local.properties`, see `buildfiles/sample.properties`)
- `java.home` = the Java 17 JDK home folder used by the build scripts

SECURITY CONFIGURATION
- `jwt.secret` = secret used to validate the JWT issued by accrualauthsbapi. MUST be the same in all the backend services
- `jwt.expiration` = validity of the JWT, in milliseconds
- `security.public-endpoints` = list of the paths accessible without authentication
- `dev.auth.enabled` = enables the development authentication, based on base64-encoded tokens instead of JWT.
  MUST be `false` (or not set) in every environment other than the local development one

MULTI-ENTITY CONFIGURATION
- No property is required: the entity and the fiscal year are read from the `codice_ente`,
  `anno_bilancio` and `fase_bilancio` claims of the JWT
