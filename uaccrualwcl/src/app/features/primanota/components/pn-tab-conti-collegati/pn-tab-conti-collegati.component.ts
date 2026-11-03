/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../../core/services/notification.service';
import { AmountInputDirective } from '../../../../shared/directives/amount-input.directive';
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format.pipe';
import { PdcTreeSearchModalComponent } from '../pdc-tree-search-modal/pdc-tree-search-modal.component';
import { PdcTreeNode } from '../../../pdc/model/pdc.model';
import { PdcService } from '../../../../core/services/pdc.service';
import { MessageDisplayComponent } from '../../../../shared/components/message-display/message-display.component';
import { PrimaNotaContoCollegato } from '../../models/primanota.model';
import { Page } from '../../../../shared/models/page.model';

/**
 * Componente per il Tab "Conti Collegati" del form Gestisci Prima Nota
 *
 * Gestisce:
 * - Accordion "AGGIUNGI" per inserimento nuovi conti
 * - Tabella conti collegati con paginazione
 * - Ricerca conto con modale ad albero
 * - Totali Dare/Avere
 * - Modifica conti esistenti (solo in modalità modifica)
 *
 * NON usa PrimeNG - solo HTML + Tailwind
 */
@Component({
  selector: 'app-pn-tab-conti-collegati',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    AmountInputDirective,
    AmountFormatPipe,
    PdcTreeSearchModalComponent,
    MessageDisplayComponent,
  ],
  templateUrl: './pn-tab-conti-collegati.component.html',
  styleUrl: './pn-tab-conti-collegati.component.scss'
})
export class PnTabContiCollegatiComponent implements OnInit, OnChanges {

  /** Lista dei conti collegati (passata dal padre) */
  @Input() conti: PrimaNotaContoCollegato[] = [];

  /** Flag per modalità modifica */
  @Input() isModifica = false;

  /**
   * Rileva cambiamenti nei dati in input (quando il padre ripristina i conti dopo ANNULLA)
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['conti'] && !changes['conti'].firstChange) {
      // Ricalcola i totali quando i conti cambiano dall'esterno
      this.calcolaTotali();
      this.cdr.detectChanges();
    }
  }

  /** Evento emesso quando la lista conti cambia */
  @Output() contiChange = new EventEmitter<PrimaNotaContoCollegato[]>();

  /** Evento emesso quando l'utente preme "Salva" */
  @Output() salva = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Salva ed Esci" */
  @Output() salvaEdEsci = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Prosegui" */
  @Output() prosegui = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Indietro" */
  @Output() indietro = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Annulla" */
  @Output() annulla = new EventEmitter<void>();

  /** Conto selezionato dalla modale */
  selectedConto: PdcTreeNode | null = null;

  /** Flag per mostrare/nascondere la modale di ricerca */
  showPdcTreeSearchModal = false;

  /** Termini di ricerca per la modale */
  searchCodiceForModal = '';
  searchDescrizioneForModal = '';

  /** Campi per l'inserimento nuovo conto (usati come filtri di ricerca) */
  nuovoContoCodice = '';
  nuovoContoDescrizione = '';
  nuovoContoDareAvere: 'D' | 'A' = 'D';
  nuovoContoImporto: number | string = '';

  /** Indice della riga in modifica (-1 se nessuna) */
  indiceRigaInModifica = -1;

  /** Flag per mostrare il messaggio di nessun risultato */
  showNoContiResult = false;

  /** Flag per espandere/collassare l'accordion */
  accordionEspanso = false;

  /** Totali */
  totaleDare = 0;
  totaleAvere = 0;

  // Proprietà per la paginazione
  currentPage = 0;
  pageSize = 10;
  pageSizeOptions: number[] = [10, 20, 50, 100];

  constructor(
    private pdcService: PdcService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.calcolaTotali();
  }

  /**
   * Verifica ricorsiva se un nodo contiene almeno una foglia tra i suoi discendenti
   */
  private hasFogliaInDiscendenza(nodo: PdcTreeNode): boolean {
    if (this.isNodoFoglia(nodo)) {
      return true;
    }
    if (nodo.children && nodo.children.length > 0) {
      return nodo.children.some(child => this.hasFogliaInDiscendenza(child));
    }
    return false;
  }

  /**
   * Verifica se un nodo è foglia
   */
  private isNodoFoglia(nodo: PdcTreeNode): boolean {
    return !!(nodo.foglia || (nodo as any).foglia_non_foglia);
  }

  /**
   * Verifica se la scrittura è valida (per abilitare i pulsanti Salva)
   */
  isScritturaValida(): boolean {
    if (this.conti.length < 2) {
      return false;
    }

    const hasDare = this.conti.some(c => c.dare > 0);
    const hasAvere = this.conti.some(c => c.avere > 0);

    if (!hasDare || !hasAvere) {
      return false;
    }

    const totaleDare = Math.round(this.conti.reduce((sum, c) => sum + (c.dare || 0), 0) * 100) / 100;
    const totaleAvere = Math.round(this.conti.reduce((sum, c) => sum + (c.avere || 0), 0) * 100) / 100;

    return Math.abs(totaleDare - totaleAvere) < 0.0001; // Confronto "stretto" normalizzato
  }

  /**
   * Apre la modale di ricerca conti
   */
  onSearchConto(): void {
    const searchCodice = this.nuovoContoCodice || '';
    const searchDescrizione = this.nuovoContoDescrizione || '';

    // Resetta il messaggio di nessun risultato
    this.showNoContiResult = false;

    // Se non ci sono filtri, apri la modale per mostrare tutto l'albero
    if (!searchCodice.trim() && !searchDescrizione.trim()) {
      this.searchCodiceForModal = '';
      this.searchDescrizioneForModal = '';
      this.showPdcTreeSearchModal = true;
      return;
    }

    // Costruisci i filtri per la ricerca
    const filters: any = {};
    if (searchCodice.trim()) {
      filters.codice = searchCodice.trim();
    }
    if (searchDescrizione.trim()) {
      filters.descrizione = searchDescrizione.trim();
    }

    // Esegui la ricerca per determinare quanti risultati ci sono
    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data: PdcTreeNode[]) => {
        // Se c'è un solo risultato ma NON è foglia e non ha foglie nella discendenza, mostra errore WEB_ERR_0028
        const hasValidLeaf = data.length > 0 && this.hasFogliaInDiscendenza(data[0]);
        if (data.length === 1 && !this.isNodoFoglia(data[0]) && !hasValidLeaf) {
          this.mostraErrore('WEB_ERR_028 - Il conto indicato non è foglia');
          this.showNoContiResult = false;
          return;
        }

        // Filtra solo i conti foglia (foglia o foglia_non_foglia)
        const contiFoglia = data.filter((conto: any) => this.hasFogliaInDiscendenza(conto));

        if (contiFoglia.length === 0) {
          // Nessun risultato: mostra il messaggio in alto
          this.showNoContiResult = true;
        } else if (contiFoglia.length === 1) {
          // Un solo risultato foglia: selezione automatica
          const account = contiFoglia[0];
          // Usa codice_ufficiale se disponibile, altrimenti usa codice
          this.nuovoContoCodice = account.codice_ufficiale || account.codice;
          this.nuovoContoDescrizione = account.descrizione;
          this.showNoContiResult = false;
        } else {
          // Più risultati: apri la modale
          this.searchCodiceForModal = searchCodice;
          this.searchDescrizioneForModal = searchDescrizione;
          this.showPdcTreeSearchModal = true;
          this.showNoContiResult = false;
        }
      },
      error: () => {
        this.mostraErrore('Errore nella ricerca dei conti');
        // In caso di errore, apri comunque la modale
        this.searchCodiceForModal = searchCodice;
        this.searchDescrizioneForModal = searchDescrizione;
        this.showPdcTreeSearchModal = true;
        this.showNoContiResult = false;
      }
    });
  }

  /**
   * Gestisce la selezione di un conto dalla modale
   */
  onContoSelezionato(nodo: PdcTreeNode | null): void {
    if (nodo) {
      // Verifica che sia un conto foglia
      if (!this.isNodoFoglia(nodo)) {
        this.mostraErrore('WEB_ERR_028 - Il conto indicato non è foglia');
        this.showPdcTreeSearchModal = false;
        return;
      }

      // Usa codice_ufficiale se disponibile, altrimenti usa codice
      this.nuovoContoCodice = (nodo as any).codice_ufficiale || nodo.codice;
      this.nuovoContoDescrizione = nodo.descrizione;
    }
    this.showPdcTreeSearchModal = false;
  }

  /**
   * Gestisce la chiusura della modale
   */
  onModalClosed(): void {
    this.showPdcTreeSearchModal = false;
  }

  /**
   * Annulla l'inserimento del conto
   */
  onAnnullaConto(): void {
    this.nuovoContoCodice = '';
    this.nuovoContoDescrizione = '';
    this.nuovoContoDareAvere = 'D';
    this.nuovoContoImporto = '';
    this.accordionEspanso = false;
    this.indiceRigaInModifica = -1;
  }

  /**
   * Conferma l'inserimento del conto
   */
  onConfermaConto(): void {
    // Validazione campi obbligatori
    if (!this.nuovoContoCodice || !this.nuovoContoDescrizione) {
      this.mostraErrore('Selezionare un conto');
      return;
    }

    if (!this.nuovoContoImporto) {
      this.mostraErrore('Inserire l\'importo');
      return;
    }

    // La direttiva amountInput gestisce già la conversione in numero
    const importo = typeof this.nuovoContoImporto === 'number'
      ? this.nuovoContoImporto
      : parseFloat(this.nuovoContoImporto);

    if (isNaN(importo) || importo <= 0) {
      this.mostraErrore('Inserire un importo valido');
      return;
    }

    // ALG-003: Verifica Conto da inserire nella scrittura
    // Chiama il backend per validare esistenza, validità temporale e attributo foglia
    this.pdcService.validaContoDaInserire(this.nuovoContoCodice).subscribe({
      next: () => {
        this.aggiungiContoAllaLista(importo);
      },
      error: (err) => {
        // Gestisce errore dalla validazione backend
        if (err?.error?.code && err.error.code.startsWith('WEB_ERR_')) {
          // Estrae solo il valore specifico (es. codice conto) dopo l'ultima occorrenza di ": "
          const fullDetail = err.error.title || err.error.detail || '';
          const detailValue = fullDetail.includes(': ') ? fullDetail.split(': ').pop() : undefined;
          this.notificationService.showMessageByCode(err.error.code, detailValue);
        } else {
          this.mostraErrore('Errore nella validazione del conto');
        }
      }
    });
  }

  /**
   * Aggiunge o aggiorna il conto nella lista dopo validazione positiva
   */
  private aggiungiContoAllaLista(importo: number): void {
    // Crea il conto collegato con la struttura corretta per il backend
    // Struttura: { codice, descrizione, dare, avere }
    const conto: PrimaNotaContoCollegato = {
      codice: this.nuovoContoCodice,
      descrizione: this.nuovoContoDescrizione,
      dare: this.nuovoContoDareAvere === 'D' ? importo : 0,
      avere: this.nuovoContoDareAvere === 'A' ? importo : 0
    };

    if (this.indiceRigaInModifica >= 0) {
      // Modalità modifica: aggiorna la riga esistente
      this.conti[this.indiceRigaInModifica] = conto;
      this.mostraSuccesso('Conto aggiornato');
    } else {
      // Modalità inserimento: aggiungi nuova riga
      this.conti.push(conto);
      this.mostraSuccesso('Conto aggiunto');
    }

    this.calcolaTotali();
    this.contiChange.emit(this.conti);

    // Pulisci i campi dell'accordion
    this.onAnnullaConto();
  }

  /**
   * Prepara la modifica di un conto esistente
   */
  modificaConto(index: number): void {
    const conto = this.conti[index];

    // Popola i campi dell'accordion con i dati del conto
    this.nuovoContoCodice = conto.codice;
    this.nuovoContoDescrizione = conto.descrizione;
    this.nuovoContoDareAvere = conto.dare > 0 ? 'D' : 'A';
    // Passa il numero direttamente, NON come stringa (la direttiva amountInput gestirà la formattazione)
    this.nuovoContoImporto = conto.dare > 0 ? conto.dare : conto.avere;

    // Imposta l'indice della riga in modifica
    this.indiceRigaInModifica = index;

    // Espandi l'accordion
    this.accordionEspanso = true;
  }

  /**
   * Rimuove un conto dalla lista
   */
  rimuoviConto(index: number): void {
    // Se siamo in modalità modifica, l'index è assoluto, altrimenti è relativo alla pagina
    const indexToRemove = this.isModifica ? index : (this.currentPage * this.pageSize) + index;

    this.conti.splice(indexToRemove, 1);
    this.calcolaTotali();
    this.contiChange.emit(this.conti);
    this.mostraSuccesso('Conto rimosso');
  }

  /**
   * Calcola i totali Dare e Avere (con arrotondamento a 2 decimali)
   */
  calcolaTotali(): void {
    const dare = this.conti.reduce((sum, c) => sum + (c.dare || 0), 0);
    const avere = this.conti.reduce((sum, c) => sum + (c.avere || 0), 0);

    // Arrotonda per evitare problemi di precisione floating point (es. 1.5456322 -> 1.55)
    this.totaleDare = Math.round(dare * 100) / 100;
    this.totaleAvere = Math.round(avere * 100) / 100;
  }

  /**
   * Ottiene i conti per la pagina corrente
   */
  getContiPagina(): PrimaNotaContoCollegato[] {
    if (!this.isModifica) {
      // In inserimento, mostro tutti i conti (non c'è paginazione)
      return this.conti;
    }

    // In modifica, applico la paginazione
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.conti.slice(start, end);
  }

  /**
   * Verifica se la pagina corrente è l'ultima
   */
  isLastPage(): boolean {
    if (!this.isModifica) {
      return true;
    }
    const end = (this.currentPage + 1) * this.pageSize;
    return end >= this.conti.length;
  }

  /**
   * Va alla pagina precedente
   */
  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
    }
  }

  /**
   * Va alla pagina successiva
   */
  nextPage(): void {
    if (!this.isLastPage()) {
      this.currentPage++;
    }
  }

  /**
   * Cambia la dimensione della pagina
   */
  onPageSizeChange(event: Event): void {
    const newSize = +(event.target as HTMLSelectElement).value;
    this.pageSize = newSize;
    this.currentPage = 0;
  }

  /**
   * Ottiene il numero totale di pagine
   */
  getTotalPages(): number {
    if (!this.isModifica || this.conti.length === 0) {
      return 0;
    }
    return Math.ceil(this.conti.length / this.pageSize);
  }

  /**
   * Ottiene l'intervallo di elementi visualizzati
   */
  getDisplayRange(): string {
    if (!this.isModifica || this.conti.length === 0) {
      return '';
    }
    const start = (this.currentPage * this.pageSize) + 1;
    const end = Math.min((this.currentPage + 1) * this.pageSize, this.conti.length);
    return `${start}-${end} di ${this.conti.length}`;
  }

  /**
   * Azione "Prosegui" - valida la scrittura e abilita il tab Attributi
   */
  onProsegui(): void {
    // Controllo lunghezza minima conti
    if (this.conti.length < 2) {
      this.mostraErrore('WEB_ERR_018 - Almeno 2 conti con segni diversi');
      return;
    }

    // Validazione scrittura utilizzando la logica unificata
    if (!this.isScritturaValida()) {
      this.mostraErrore('WEB_ERR_019 - Totale Dare e Avere devono coincidere');
      return;
    }

    this.prosegui.emit();
  }

  /**
   * Azione "Salva" - salva la prima nota e rimane nel tab Conti
   */
  onSalva(): void {
    // Validazione scrittura
    if (!this.isScritturaValida()) {
      this.mostraErrore('WEB_ERR_018 - Almeno 2 conti con segni diversi');
      return;
    }

    this.salva.emit();
  }

  /**
   * Azione "Salva ed Esci" - salva la prima nota ed esce
   */
  onSalvaEdEsci(): void {
    // Validazione scrittura
    if (!this.isScritturaValida()) {
      this.mostraErrore('WEB_ERR_018 - Almeno 2 conti con segni diversi');
      return;
    }

    this.salvaEdEsci.emit();
  }

  /**
   * Azione "Indietro" - torna al tab Testata
   */
  onIndietro(): void {
    this.indietro.emit();
  }

  /**
   * Azione "Annulla" - annulla l'operazione
   */
  onAnnulla(): void {
    this.annulla.emit();
  }

  /**
   * Toggle accordion
   */
  toggleAccordion(): void {
    this.accordionEspanso = !this.accordionEspanso;
  }

  /**
   * Mostra messaggio di errore
   */
  private mostraErrore(messaggio: string): void {
    // Verifica se il messaggio contiene un codice WEB_
    const webCodeMatch = messaggio.match(/(WEB_(OK|ERR|WRN|INF)_\d+)/);
    if (webCodeMatch && webCodeMatch[1]) {
      this.notificationService.showMessageByCode(webCodeMatch[1], messaggio.split(' - ')[1]);
    } else {
      this.notificationService.showError(messaggio);
    }
  }

  /**
   * Mostra messaggio di informazione
   */
  private mostraInfo(messaggio: string): void {
    // Verifica se il messaggio contiene un codice WEB_
    const webCodeMatch = messaggio.match(/(WEB_(OK|ERR|WRN|INF)_\d+)/);
    if (webCodeMatch && webCodeMatch[1]) {
      this.notificationService.showMessageByCode(webCodeMatch[1], messaggio.split(' - ')[1]);
    } else {
      this.notificationService.showInfo(messaggio);
    }
  }

  /**
   * Mostra messaggio di successo
   */
  private mostraSuccesso(messaggio: string): void {
    // Verifica se il messaggio contiene un codice WEB_
    const webCodeMatch = messaggio.match(/(WEB_(OK|ERR|WRN|INF)_\d+)/);
    if (webCodeMatch && webCodeMatch[1]) {
      this.notificationService.showMessageByCode(webCodeMatch[1]);
    } else {
      this.notificationService.showSuccess(messaggio);
    }
  }
}
