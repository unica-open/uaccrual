/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

export const REPORT_NAME_MAP: Record<string, string> = {
  'ACCRUAL-001': 'Mastrino',
  'ACCRUAL-002': 'Libro Giornale',
  'ACCRUAL-003': 'Bilancio di Verifica'
};

export function getReportCode(rptdesignFileName: string): string {
  if (!rptdesignFileName) return '';
  return rptdesignFileName.split('_')[0];
}

export function getReportNameFromRptdesign(rptdesignFileName: string): string {
  if (!rptdesignFileName) return '';
  const code = getReportCode(rptdesignFileName);
  return REPORT_NAME_MAP[code] || rptdesignFileName;
}
