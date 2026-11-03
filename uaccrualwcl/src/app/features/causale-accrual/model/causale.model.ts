/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// accrualwcl/src/app/features/causale-accrual/model/causale.model.ts

export interface CausaleTipo {
  id: number; // Modificato a number
  codice: string;
  descrizione: string;
}

export interface Evento {
  id: number; // Modificato a number
  codice: string;
  descrizione: string;
}

export interface CausaleStato {
  id: number; // Modificato a number
  codice: string;
  descrizione: string;
}

export interface Ente {
  id: number; // Modificato a number
  codiceFiscale?: string;
  denominazione?: string;
}

export interface ContoCollegato {
  pdcId: number;
  classe: string; // Descrizione Classe conto
  codice: string;   // Codice ufficiale conto
  descrizione: string;
  segno: string;
  tipoImporto: string;
  inizio_validita: string; // Già in snake_case coerentemente
  fine_validita?: string; // Già in snake_case coerentemente
}

export interface ContoCollegatoRequest {
  pdcId: number;
  classe: string;
  codice: string;
  descrizione: string;
  segno: string;
  tipoImporto: string;
  inizioValidita: string;
  fineValidita?: string;
}

export interface Causale {
  id: number; // Modificato a number
  codice: string;
  descrizione: string;
  tipo?: CausaleTipo; // Oggetto completo per il tipo
  evento?: Evento; // Oggetto completo per l'evento
  stato?: CausaleStato; // Oggetto completo per lo stato
  ente?: Ente; // Oggetto completo per l'ente
  inizio_validita?: string; // Cambiato a snake_case per allineamento
  fine_validita?: string; // Cambiato a snake_case per allineamento
  modificabile?: boolean;
  eliminabile?: boolean;
  validabile?: boolean;
  contiCollegati?: ContoCollegato[]; // Aggiunto per allinearsi al backend
  version?: number; // Aggiunto per optimistic locking
}

// DTO per le richieste di creazione/aggiornamento della causale
export interface CausaleRequest {
  codice: string;
  descrizione: string;
  tipo?: { id: number }; // Solo l'ID per la request
  evento?: { id: number }; // Solo l'ID per la request
  stato?: { id: number }; // Solo l'ID per la request
  ente?: { id: number }; // Solo l'ID per la request
  inizio_validita?: string; // Già in snake_case coerentemente
  fine_validita?: string; // Già in snake_case coerentemente
  contiCollegati?: ContoCollegatoRequest[];
  version?: number; // Solo per l'aggiornamento (optimistic locking)
}

export interface CausaleSearchCriteria {
  tipoCausale?: string;
  eventoCausale?: string;
  codice?: string;
  descrizione?: string;
  stato?: string;
  // Non includiamo limit e offset qui, saranno gestiti direttamente nel service
}

export interface Paged<T> {
  data: T[];
  page: number;
  page_size: number;
  total_elements: number;
}
