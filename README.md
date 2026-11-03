<!--
TODO before publishing:
- replace <REPOSITORY_HOST>/<ORGANIZATION> with the public location of the repositories
- create the referenced files that do not exist yet: INSTALLATION.md, CONTRIBUTING.md, AUTHORS.txt,
  Copyrights.txt, "EUPL v1_2 IT-LICENSE.txt", "EUPL v1_2 EN-LICENSE.txt"
-->

# Project Title
UACCRUAL

Contabilit&agrave; economico-patrimoniale per competenza per gli enti pubblici

# Project Description
This is a multi-module project handling the accrual-based accounting for Italy's Public Administration:
Chart of Accounts (Piano dei Conti), accounting reasons (Causali), journal entries (Prime Note),
data flows coming from external accounting systems (for example CONTABILIA/SIAC of this community) and the MEF reporting,
with a multi-entity (multi-ente) user profiling.

The modules are as follow:
- libraries:
  - [uaccrualcommonsrv]: common security library for the backend services (JWT validation, multi-entity and fiscal year context)
- backend services (please note that all of them depend on accrualcommonsrv):
  - [uaccrualauthsbapi]: service implementation for the AUTHENTICATION functionalities (login, Shibboleth SSO, JWT issuing, password reset)
  - [uaccrualsbsrv]: service implementation for the CORE accounting functionalities (Chart of Accounts, Causali, Prime Note)
  - [uaccrualsbapi]: service implementation for the additional accounting functionalities, including the data flows from external systems
  - [uaccrualreportsbapi]: service implementation for the REPORT functionalities, synchronous and asynchronous, based on BIRT
- frontend services:
  - [uaccrualwcl]: Angular web client for all the ACCRUAL functionalities
- transversal:
  - [uaccrualreport]: reporting templates (BIRT) for the report service
  - [uaccrualdbimpl]: database implementation of the application schema, with all the required scripts
  - [uaccrualauthdbimpl]: database implementation of the authentication schema (`accrualauth`), with all the required scripts


# Configurations
For the configuration of each single module, please refer to the
README.md file which is present in each module.

# Getting Started
Please refer to the Prerequisites section to gather the requested configuration prior to configure the project.

Please refer to the Installing section for specifications about the installation process.

# Prerequisites
- The Java projects are written in UTF-8 and are compatible with Java 17
- (Compilation only) The `JAVA_HOME` environment variable is required to point to a Java 17 JDK home folder
  (alternatively, the `java.home` property of the `buildfiles/local.properties` file of each module)
- The Java runtime used is Java 17
- Apache Maven for the building and dependencies management process (the Maven Wrapper `mvnw` is included in each Java module)
- `uaccrualcommonsrv` must be built and installed in the local Maven repository before the backend services
- Node.js and npm, in a version supported by Angular 19, for the building process of the web client
- All the libraries listed in the `docs/BOM.csv` file of each module must be accessible to compile the project.
  The libraries are published on the public Maven and npm registries, with the exception of the following CSI libraries,
  which are published only at (https://repart.csi.it) and are not included in the project:
  * `it.csi.csi:csi-stadapter:1.4.6` (all the backend services)
  * `it.csi.apitools:csi-java-swagger3-codegen:1.1.1.001` (all the backend services, used by the code generation from `openapi.yaml`)
  * `it.csi.iridev2:iridev2-pep-intf:2.0.0` (accrualsbsrv only)
  If the CSI repository is not reachable, these libraries can be requested from CSI Piemonte; for convenience, they are also included in the `lib.tar.gz` archive, from which they can be installed in the local Maven repository.

- The backend services are Spring Boot 3 applications packaged as executable JARs: no Application Server is required
- A web server for the static web client and the reverse proxy towards the backend services
  (tested on Apache HTTP Server, with the Shibboleth Service Provider module for the SSO from CONTABILIA)
- A compatible PostgreSQL DBMS (tested on PostgreSQL 15), hosting the `ACCRUAL` and `ACCRUALAUTH` databases

# Installing
Please refer to the [INSTALLATION.md](./INSTALLATION.md) file for the steps
required for the installation.

# Contributing
Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our code of conduct,
and the process for submitting pull requests to us.

# Versioning
We use Semantic Versioning for versioning. (http://semver.org) \
Not every module is released with every product version: the version of each module
composing a product version is declared in the [versioni.properties](./versioni.properties) file.

# Authors
See the list of contributors who participated in this project in file [AUTHORS.txt](./AUTHORS.txt).

# Copyrights
See the list of copyrighters in this project in file Copyrights.txt
“&copy; Copyright CSI Piemonte – 2026”.

# License
SPDX-License-Identifier: EUPL-1.2\
See the [**"EUPL v1_2 IT-LICENSE.txt"**](./EUPL%20v1_2%20IT-LICENSE.txt)
and [**"EUPL v1_2 EN-LICENSE.txt"**](./EUPL%20v1_2%20EN-LICENSE.txt) files for details
