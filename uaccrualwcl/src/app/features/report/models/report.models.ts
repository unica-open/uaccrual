/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Modelli per il modulo Reportistica (CDU-014)
 */

/**
 * Tipo di parametro report
 */
export type ReportParameterType = 'TEXT' | 'NUMBER' | 'DATE' | 'COMBO' | 'RADIO' | 'CHECKBOX';

/**
 * Opzione per parametri di tipo COMBO o RADIO
 */
export interface ParameterOption {
  valore: any;
  etichetta: string;
  dipendeDa?: any;
}

/**
 * Definizione di un parametro report
 */
export interface ReportParameter {
  nome: string;
  etichetta: string;
  tipo: ReportParameterType;
  obbligatorio: boolean;
  valoreDefault?: any;
  valoriPossibili?: ParameterOption[];
  hidden?: boolean; // Se true, il parametro non viene mostrato ma inviato come hidden field
  gruppo?: string | null; // Nome del gruppo di appartenenza (null se non raggruppato)
}

/**
 * Sezione per il rendering dei parametri nel form
 */
export interface RenderSection {
  type: 'parameter' | 'group' | 'spacer' | 'ungrouped';
  parameter?: ReportParameter; // per type='parameter'
  groupName?: string; // per type='group'
  parameters?: ReportParameter[]; // per type='group' o 'ungrouped'
}

/**
 * Metadata di un report estratti dal template BIRT
 */
export interface ReportInfo {
  template: string;
  nome?: string;
  parametri: ReportParameter[];
}

/**
 * Request per ottenere metadata report
 */
export interface ReportInfoRequest {
  rptdesign: string;
}

/**
 * Request per generazione report
 */
export interface GenerateRequest {
  rptdesign: string;
  rptconfig?: string;
  format: string;
  parameters?: { [key: string]: any };
}

/**
 * Response per inizializzazione report asincrono
 */
export interface AsyncReportInitResponse {
  reportId: string;
  status: 'PENDING' | 'PROCESSING';
  createdAt: string;
}

/**
 * Stato di un'operazione asincrona (CDU-014)
 */
export interface AsyncReportOperationStatus {
  reportId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';
  progress?: number;
  downloadUrl?: string;
  error?: string;
}

/**
 * Configurazione di un report nella lista
 */
export interface ReportConfig {
  codice: string;
  nome: string;
  descrizione: string;
  template: string;
}

// ============================================================================
// MODELLI PER REPORT ASINCRONI (CDU-015)
// ============================================================================

/**
 * Stati possibili di un report asincrono
 */
export type AsyncReportStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';

/**
 * Tipologie di report supportate
 */
export type AsyncReportType = 'MASTRINO' | 'LIBRO_GIORNALE' | 'BILANCIO' | 'ALTRO';

/**
 * Filtro di ricerca per report asincroni
 */
export interface AsyncReportSearchFilter {
  /** Data inizio elaborazione (inclusiva) */
  data_elaborazione_da?: string; // Formato: YYYY-MM-DD
  /** Data fine elaborazione (inclusiva) */
  data_elaborazione_a?: string; // Formato: YYYY-MM-DD
  /** Stato del report (filtro singolo) */
  stato?: AsyncReportStatus;
  /** Numero pagina (0-based) */
  pagina?: number;
  /** Dimensione pagina */
  dimensione_pagina?: number;
  /** Ordinamento: campo (es. 'completedAt') */
  ordinamento?: string;
  /** Direzione ordinamento */
  direzione_ordinamento?: 'ASC' | 'DESC';
}

/**
 * Request per API di ricerca report asincroni
 */
export interface AsyncReportSearchRequest {
  /** Data inizio elaborazione (inclusiva) */
  data_elaborazione_da?: string; // Formato: YYYY-MM-DD
  /** Data fine elaborazione (inclusiva) */
  data_elaborazione_a?: string; // Formato: YYYY-MM-DD
  /** Stato del report (filtro singolo) */
  stato?: AsyncReportStatus;
  /** Tipo di report (filtro opzionale) */
  tipo_report?: 'ACCRUAL-001' | 'ACCRUAL-002' | 'ACCRUAL-003';
  /** Formato del report (filtro opzionale) */
  formato?: 'pdf' | 'xlsx';
  /** Numero pagina (0-based) */
  pagina?: number;
  /** Dimensione pagina */
  dimensione_pagina?: number;
  /** Ordinamento: campo (es. 'completedAt') */
  ordinamento?: string;
  /** Direzione ordinamento */
  direzione_ordinamento?: 'ASC' | 'DESC';
}

/**
 * Riepilogo report asincrono (voce di elenco)
 */
export interface AsyncReportSummary {
  /** ID univoco del report */
  report_id: string;
  /** Nome del file rptdesign utilizzato */
  rptdesign_file_name?: string;
  /** Formato del report (pdf, xlsx, docx, html) */
  formato: string;
  /** Nome del file generato */
  file_nome?: string;
  /** Stato del report */
  stato: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';
  /** Data creazione del report */
  data_creazione: string; // ISO 8601
  /** Data completamento del report (opzionale) */
  data_completamento?: string; // ISO 8601
  /** Dimensione del file in byte (opzionale) */
  dimensione_file?: number;
  /** Percentuale di avanzamento (0-100, opzionale) */
  progress_percentage?: number;
}

/**
 * Esito di elaborazione report asincrono
 */
export interface AsyncReportEsito {
  /** ID univoco esito */
  esitoId: string;
  /** ID report di appartenenza */
  reportId: string;
  /** Codice soggetto (se applicabile) */
  soggettoCodice?: string;
  /** Descrizione soggetto (se applicabile) */
  soggettoDescrizione?: string;
  /** Esito elaborazione */
  esito: 'SUCCESS' | 'WARNING' | 'ERROR';
  /** Messaggio descrittivo */
  messaggio?: string;
  /** Data elaborazione */
  elaboratoAt: string; // ISO 8601
}

/**
 * Dettaglio completo report asincrono
 * Nota: i campi sono piatti per allineamento con il DTO backend AsyncReportDetail
 * che estende AsyncReportSummary (pattern allOf in OpenAPI)
 */
export interface AsyncReportDetail {
  /** ID univoco del report */
  report_id: string;
  /** Formato del report (pdf, xlsx, docx, html) */
  formato: string;
  /** Nome del file generato */
  file_nome?: string;
  /** Stato del report */
  stato: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';
  /** Data creazione del report */
  data_creazione: string; // ISO 8601
  /** Data completamento del report (opzionale) */
  data_completamento?: string; // ISO 8601
  /** Dimensione del file in byte (opzionale) */
  dimensione_file?: number;
  /** Percentuale di avanzamento (0-100, opzionale) */
  progress_percentage?: number;
  /** Nome del file rptdesign utilizzato */
  rptdesign_file_name?: string;
  /** Nome del file rptconfig utilizzato (opzionale) */
  rptconfig_file_name?: string;
  /** Parametri del report in formato JSON */
  parametri?: string;
  /** Parametri utilizzati per la generazione (parsed da parametri) */
  parametriUtilizzati?: { [key: string]: any };
  /** Mappa nome_parametro → etichetta leggibile (solo parametri visibili) */
  etichette_parametri?: { [key: string]: string };
  /** Lista esiti elaborazione */
  esiti?: AsyncReportEsito[];
  /** Eventuale messaggio di errore */
  messaggio_errore?: string;
  /** Eventuale messaggio di errore (alias per backward compatibility) */
  errorMessage?: string;
  /** Data e ora di avvio elaborazione (opzionale) */
  data_avvio?: string; // ISO 8601
  /** Versione del record (optimistic locking) */
  versione?: number;
  /** ID dell'ente proprietario del report */
  ente_id?: number;
  /** URL per il download (se completato) */
  downloadUrl?: string;
}

/**
 * Response paginata per ricerca report asincroni
 */
export interface AsyncReportSearchResponse {
  /** Lista report (pagina corrente) */
  report: AsyncReportSummary[];
  /** Totale elementi */
  totale_elementi: number;
  /** Pagina corrente (0-based) */
  pagina_corrente: number;
  /** Dimensione pagina */
  dimensione_pagina: number;
  /** Totale pagine */
  totale_pagine: number;
}

/**
 * Enum per stati report (utility per UI)
 */
export enum AsyncReportStatusEnum {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  ERROR = 'ERROR'
}

/**
 * Enum per tipi report (utility per UI)
 */
export enum AsyncReportTypeEnum {
  MASTRINO = 'MASTRINO',
  LIBRO_GIORNALE = 'LIBRO_GIORNALE',
  BILANCIO = 'BILANCIO',
  ALTRO = 'ALTRO'
}

/**
 * Mappatura stati per visualizzazione UI
 */
export const AsyncReportStatusLabels: Record<AsyncReportStatus, string> = {
  'PENDING': 'In coda',
  'PROCESSING': 'In elaborazione',
  'COMPLETED': 'Completato',
  'ERROR': 'Errore'
};

/**
 * Mappatura stati per colori PrimeNG
 */
export const AsyncReportStatusSeverity: Record<AsyncReportStatus, 'success' | 'info' | 'warn' | 'danger'> = {
  'PENDING': 'info',
  'PROCESSING': 'info',
  'COMPLETED': 'success',
  'ERROR': 'danger'
};
