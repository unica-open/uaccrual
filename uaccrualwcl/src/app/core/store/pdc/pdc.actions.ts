/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// Definisce gli eventi/comandi che possono essere lanciati dall'applicazione
// per modificare lo stato del Piano dei Conti (Pdc).

/**
 * Azione per avviare una ricerca dei conti PdC.
 * Viene "dispacciata" dal PdcSearchComponent.
 */
export class SearchPdcConti {
  static readonly type = '[Pdc] Search Conti';
  constructor(public filter: any) { } // TODO: Sostituire 'any' con un modello forte (es. PdcSearchFilter)
}

/**
 * Azione per recuperare il dettaglio di un singolo conto.
 * Viene "dispacciata" dal PdcDetailComponent.
 */
export class GetPdcDetail {
  static readonly type = '[Pdc] Get Detail';
  constructor(public id: number) { }
}

/**
 * Azione per navigare a una pagina specifica dei risultati.
 * Viene "dispacciata" dal componente di paginazione nel PdcListComponent.
 */
export class ChangePdcPage {
  static readonly type = '[Pdc] Change Page';
  constructor(public page: number, public size: number) { }
}

/**
 * Azione per eliminare un conto PdC.
 * Viene "dispacciata" dal PdcListComponent.
 *
 * Il conto va identificato con la tripletta completa, che è la chiave dell'endpoint
 * DELETE /pdc/{codiceUfficiale}/{codiceClasse}/{codiceFamiglia}. Il solo codice non basta:
 * ricostruire la tripletta con una ricerca non è possibile, perché /pdc/search pretende
 * anche pdc_classe_codice e risponde 400 senza.
 */
export class DeletePdc {
  static readonly type = '[Pdc] Delete Conto';
  constructor(
    public codiceUfficiale: string,
    public codiceClasse: string,
    public codiceFamiglia: string
  ) { }
}

/**
 * Azione per pulire i risultati della ricerca e i filtri.
 * Può essere usata quando si lascia la feature.
 */
export class ClearPdcState {
  static readonly type = '[Pdc] Clear State';
}

/**
 * Azione per impostare un messaggio nello stato.
 * Viene usata dal NotificationService per mostrare messaggi in pagina.
 */
export class SetMessage {
  static readonly type = '[Pdc] Set Message';
  constructor(
    public message: {
      type: 'error' | 'warning' | 'info' | 'success';
      title: string;
      text: string;
    }
  ) { }
}

/**
 * Azione per cancellare il messaggio dallo stato.
 * Viene usata per rimuovere il messaggio dopo che è stato visualizzato.
 */
export class ClearMessage {
  static readonly type = '[Pdc] Clear Message';
}
