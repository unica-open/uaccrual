# Configurations
No configuration is required by this module.

The scripts implement the `accrualauth` schema of the `ACCRUALAUTH` database (tested on PostgreSQL 15),
used by accrualauthsbapi. They are organized by version in `dist/<version>/`:
- `accrualauth_script_iniziale.sql` = creation of the schema, for a new installation
- `delta-<version>.sql` = upgrade from the previous version. The deltas MUST be applied in version order
- `dml_alimentazione_iniziale_per_ente/<entity>/` = initial data (users, profiles) for the given entity (ente)
