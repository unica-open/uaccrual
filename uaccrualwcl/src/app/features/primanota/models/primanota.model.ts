/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Page } from "../../../shared/models/page.model";

// Conto Collegato (per la tabella paginata)
export interface PrimaNotaContoCollegatoPage {
  codice: string;
  descrizione: string;
  dare: number;
  avere: number;
}

// Totali Conti (per l'endpoint dedicato)
export interface ContiTotali {
  totaleDare: number;
  totaleAvere: number;
}

// Attributo Collegato (per la tabella paginata)
export interface PrimaNotaAttributoPage {
  tipoAttributo: string;
  tipoAttributoDescrizione?: string; // Descrizione del tipo attributo, valorizzata dal backend
  valore: string;
  conto?: string;
}

// Prima Nota Collegata (per la tabella paginata)
export interface PrimaNotaCollegataPage {
  idPrimaNotaCollegata: number;
  tipo: string;
  fonte: string;
  evento: string;
  causale: string;
  descrizioneCausale: string;
  numeroProvvisorio: number;
  dataRegProvvisoria: Date;
  numeroDefinitivo: number;
  dataRegDefinitiva: Date;
  stato: string;
  descrizione: string;
  relazione: string;
  dataOperazione: Date;
  importo: number;
  valuta: string;
}

// Corrisponde a PrimaNotaSearchFilterDTO
export interface PrimaNotaSearchFilter {
  codiceTipo?: string;
  codiceFonte?: string;
  codiceEvento?: string;
  codiceCausale?: string;
  numeroProvvisorio?: number;
  numeroDefinitivo?: number;
  codiceStato?: string;
  descrizione?: string;
  codiceConto?: string;
  descrizioneConto?: string;
  dataRegProvvisoriaDa?: Date;
  dataRegProvvisoriaA?: Date;
  dataRegDefinitivaDa?: Date;
  dataRegDefinitivaA?: Date;
  importoDa?: number;
  importoA?: number;
  soloNonQuadrate?: boolean;
}

// Corrisponde a PrimaNotaSearchResultDTO
export interface PrimaNotaSearchResult {
  id: number;
  anno: number; // Aggiunto per l'identificazione univoca della prima nota
  tipo: string;
  fonte: string;
  evento: string;
  causale: string;
  numeroProvvisorio: number;
  dataRegProvvisoria: Date;
  stato: string;
  numeroDefinitivo: number;
  dataRegDefinitiva: Date;
  descrizione: string;
  totaleDare: number;
  totaleAvere: number;
  isAggiornabile: boolean;
  isAnnullabile: boolean;
  isRiapribile: boolean;
  isValidabile: boolean;
}

// Corrisponde a PrimaNotaDetailDTO
export interface PrimaNotaDetail {
    id: number;
    anno: number; // Aggiunto per l'identificazione univoca della prima nota
    tipo: string; // Codice del tipo
    tipoDescrizione: string; // Descrizione del tipo
    fonte: string; // Codice della fonte
    fonteDescrizione: string; // Descrizione della fonte
    evento: string; // Codice dell'evento
    eventoDescrizione: string; // Descrizione dell'evento
    causale: string; // Codice della causale
    causaleDescrizione: string; // Descrizione della causale
    numeroProvvisorio: number;
    dataRegProvvisoria: Date;
    stato: string; // Codice dello stato
    statoDescrizione: string; // Descrizione dello stato
    numeroDefinitivo: number;
    dataRegDefinitiva: Date;
    descrizione: string;
    conti: PrimaNotaContoCollegato[];
    attributi: PrimaNotaAttributo[];
    primeNoteCollegate: PrimaNotaCollegata[];
}

export interface PrimaNotaContoCollegato {
    id?: number; // Aggiunto per permettere l'identificazione univoca nel salvataggio
    codice: string;
    descrizione: string;
    dare: number;
    avere: number;
}

export interface PrimaNotaAttributo {
    tipoAttributo: string; // Codice del tipo attributo
    tipoAttributoDescrizione?: string; // Descrizione del tipo attributo (opzionale per inserimento)
    valore: string;
    conto?: string;
}

export interface PrimaNotaCollegata {
    relazione: string;
    tipo: string;
    fonte: string;
    evento: string;
    descrizioneCausale: string;
    numeroProvvisorio: number;
    dataRegProvvisoria: Date;
    numeroDefinitivo: number;
    dataRegDefinitiva: Date;
    stato: string;
    descrizione: string;
}
