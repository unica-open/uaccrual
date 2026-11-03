/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

export interface PdcContoRelazione {
  tipoRelazione: string;
  classe: string;
  tipo: string;
  livello: number;
  segmento: string;
  codice: string;
  descrizione: string;
}

export interface Pdc {
  id: string;
  id_pdc_padre?: number;
  codice: string;
  codice_ufficiale?: string;
  codice_completo?: string;
  descrizione: string;
  partita?: boolean;
  ordine?: string;
  livello: number;
  collegamento_bil_itas?: string;
  fondo_dotazione?: boolean;
  foglia: boolean;
  foglia_non_foglia?: boolean;
  pdc_padre?: Pdc;
  pdc_famiglia?: PdcFamiglia;
  pdc_classe?: PdcClasse;
  pdc_tipo?: PdcTipo;
  pdc_stato?: PdcStato;
  ente?: Ente;
  inizio_validita?: string;
  fine_validita?: string | null;
  // Campi aggiunti per la funzionalità di ricerca e UI
  classe?: string;
  tipo?: string;
  evento?: Evento;
  segmento?: string;
  stato?: string;
  // Campi calcolati dal backend per UI
  modificabile: boolean;
  eliminabile: boolean;
  inseribile_figlio: boolean;
  // Campi per la gestione del form
  id_classe_conto?: number;
  id_tipo_conto?: number;
  id_stato_conto?: number;
  flag_foglia?: boolean;
  flag_fondo_dotazione?: boolean;
  flag_a_partita?: boolean;
  codice_bilancio?: string;
  data_inizio_validita?: string;
  data_fine_validita?: string;
  version?: number;
}

// Nuova interfaccia per la gestione del conto PdC
export interface ContoPdc {
  id_pdc?: number;
  id_pdc_padre?: number;
  codice: string;
  codice_completo?: string;
  descrizione: string;
  id_classe_conto: number;
  id_tipo_conto: number;
  id_stato_conto?: number;
  livello?: number;
  flag_foglia?: boolean;
  flag_fondo_dotazione?: boolean;
  flag_a_partita?: boolean;
  codice_bilancio?: string;
  data_inizio_validita: string; // Formato YYYY-MM-DD
  data_fine_validita?: string; // Formato YYYY-MM-DD
  version?: number;
}

export interface PdcFamiglia {
  id: string;
  codice: string;
  descrizione: string;
  modificabile: boolean;
  ricercabile: boolean;
}

export interface PdcClasse {
  id: string;
  codice: string;
  descrizione: string;
  segno?: string; // DARE o AVERE
  tipoImporto?: string; // L, I, O
}

export interface PdcTipo {
  id: string;
  codice: string;
  descrizione: string;
}

export interface PdcStato {
  id: string;
  codice: string;
  descrizione: string;
}

export interface Evento {
  id: string;
  codice: string;
  descrizione: string;
}

export interface Ente {
  id: string;
  codiceFiscale: string;
  denominazione: string;
}

export interface PdcSearchCriteria {
  classe?: string;
  famiglia?: string;
  tipo?: string;
  codiceEvento?: string;
  codice?: string;
  descrizione?: string;
  stato?: string;
  collegamento_bil_itas?: string;
}

export interface PagedPdc<T> {
  data: T[];
  page: number;
  page_size: number;
  total_elements: number;
}

// Interfaccia per rappresentare un nodo dell'albero dei conti PDC
export interface PdcTreeNode extends Pdc {
  children?: PdcTreeNode[];
}
