/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

export interface TipoFile {
  id?: number;
  codice: string;
  descrizione: string;
}

export interface FlussoUploadRequest {
  codice: string;
  tipoFile: string; // Sarà il codice del TipoFile
  note?: string;
  file: File; // Il file da caricare
}

export interface FlussoUploadResponse {
  message: string;
}

export interface Paged<T> {
  data: T[];
  page: number;
  page_size: number;
  total_elements: number;
}

export interface FlussoSearchFilter {
  codiceFile?: string;
  fonte?: string;
  tipoFile?: string;
  dataCaricamento?: string;
}

export interface FlussoSearchResult {
  id: string; // Aggiunto il campo id
  fonte: string;
  tipo: string;
  codice: string;
  nome: string;
  dataCaricamento: string;
  stato: string;
}

export interface Fonte {
  id?: number;
  codice: string;
  descrizione: string;
}

export interface TipoFlusso {
  id?: number;
  codice: string;
  descrizione: string;
}

export interface PagedFlussoSearchResult extends Paged<FlussoSearchResult> {} // Added this line

// Nuova interfaccia per rispecchiare l'API TipoFile
export interface TipoFileApi {
  codice: string;
  descrizione: string;
}

export interface EsitoElaborazione {
  dataElaborazione: string; // ISO string
  esito: string;
  messaggio: string;
}

export interface PrimaNotaConfermaInfo {
  id: number;
  anno: number;
  numeroProvvisorio: string;
  descrizione: string;
  causale: string;
  stato: string;
}

export interface FlussoElaboraResponse {
  success: boolean;
  message: string;
}

export interface ElaboraFlussoResponseDTO {
  flussoElaboraResponse?: FlussoElaboraResponse;
  needsConfirmation?: boolean;
  confirmationCode?: string;
  confirmationMessage?: string;
  resumeToken?: string;
  tipoMessaggio?: string;
  primaNoteDaAnnullare?: PrimaNotaConfermaInfo[];
}

export interface ProcessStatusResponse {
  jobId: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  progressPercentage: number;
  currentStep?: string;
  message?: string;
}

export interface FlussoDetail {
  id: string;
  codice: string;
  nome: string;
  fonte: string;
  tipo: string;
  dataCaricamento: string; // ISO string
  stato: string;
  note?: string; // Added from CDU
  esitiElaborazione?: EsitoElaborazione[]; // Added from CDU
}