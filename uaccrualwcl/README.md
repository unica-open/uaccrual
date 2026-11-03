# Configurations
All configuration MUST be set in the `src/environments/environment.<env>.ts` file used for compilation.
The file is selected by the build configuration of `angular.json`, which replaces the default `src/environments/environment.ts`.

- `production` = specifies whether the application is built for production
- `environmentName` = the name of the environment

CONFIGURATION FOR THE BACKEND SERVICES
- `services.accrualLegacyApiUrl` = Endpoint for the accrualsbsrv backend service
- `services.accrualNewApiUrl` = Endpoint for the accrualsbapi backend service
- `services.accrualReportsApiUrl` = Endpoint for the accrualreportsbapi backend service
- `services.accrualAuthApiUrl` = Endpoint for the accrualauthsbapi backend service

AUTHENTICATION CONFIGURATION
- `authentication.devFileUrl` = file with the development user, used only for the local development.
  May be left blank in every other environment

FEATURES CONFIGURATION
- `features.enableDebugTools` = enables the debug tools
- `features.enableMockData` = enables the mock data instead of the backend services
- `features.enableAdvancedFeatures` = enables the advanced functionalities

NETWORK CONFIGURATION
- `network.timeout` = timeout of the calls to the backend services, in milliseconds
- `network.retryAttempts` = number of retries of a failed call
- `network.retryDelay` = delay between two retries, in milliseconds

VERSION CONFIGURATION
- The version shown by the application is read at build time by `scripts/generate-version.js`
  from the `accrualwcl` entry of the `versioni.properties` file of the main project,
  falling back to the `version` of `package.json` when that file cannot be found

CONFIGURATION FOR INTEGRATION TO CSI Shibboleth SSO
- No property is required: the web server MUST protect the `/accrualwcl` path with the Shibboleth
  Service Provider and forward `/accrualwcl/api/sso-shibboleth` to the accrualauthsbapi
  `/api/v1/auth/sso-shibboleth` endpoint

DEVELOPMENT SERVER CONFIGURATION
- `proxy.conf.json` = proxy of the Angular development server towards the backend services
  (see `proxy.conf.local.json.sample`)
