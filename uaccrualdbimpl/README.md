# Configurations
No configuration is required by this module.

The scripts implement the application schema of the `ACCRUAL` database (tested on PostgreSQL 15),
used by accrualsbsrv, accrualsbapi and accrualreportsbapi. They are organized by version in `dist/<version>/`:
- `accrual_script_iniziale.sql` = creation of the schema, for a new installation
- `delta-<version>.sql` = upgrade from the previous version. The deltas MUST be applied in version order
- `dml_alimentazione_iniziale_per_ente/<entity>/` = initial data for the given entity (ente)
- `old/` = no more used. Kept for history only
