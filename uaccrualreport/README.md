# Configurations
No configuration is required by this module.

The BIRT report templates in `src/` MUST be copied to the folder set in the `birt.reports.directory`
property of accrualreportsbapi:
- `ACCRUAL-001_stampa_mastrino.rptdesign` = general ledger (mastrino)
- `ACCRUAL-002_stampa_libro_giornale.rptdesign` = journal (libro giornale)
- `ACCRUAL-003_stampa_bilancio_verifica.rptdesign` = trial balance (bilancio di verifica)

The database connection of the templates is provided at runtime by accrualreportsbapi,
from its `spring.datasource.*` properties.
