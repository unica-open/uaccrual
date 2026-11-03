/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../../core/services/notification.service';
import { PrimaNotaAttributo } from '../../models/primanota.model';
import { PrimaNotaContoCollegato } from '../../models/primanota.model';
import { AttributoService, AttributoInfo } from '../../../../core/services/attributo.service';

/**
 * Componente per il Tab "Attributi" del form Gestisci Prima Nota
 *
 * Gestisce:
 * - Accordion "AGGIUNGI" per inserimento nuovi attributi
 * - Tabella attributi con paginazione
 * - Dropdown per Tipo Attributo e Conto Collegato
 * - Modifica attributi esistenti (solo in modalità modifica)
 *
 * NON usa PrimeNG - solo HTML + Tailwind
 */
@Component({
  selector: 'app-pn-tab-attributi',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './pn-tab-attributi.component.html',
  styleUrl: './pn-tab-attributi.component.scss'
})
export class PnTabAttributiComponent implements OnInit {

  /** Lista degli attributi (passata dal padre) */
  @Input() attributi: PrimaNotaAttributo[] = [];

  /** Lista dei conti collegati (per il dropdown conto collegato) */
  @Input() conti: PrimaNotaContoCollegato[] = [];

  /** Flag per modalità modifica */
  @Input() isModifica = false;

  /** Evento emesso quando la lista attributi cambia */
  @Output() attributiChange = new EventEmitter<PrimaNotaAttributo[]>();

  /** Evento emesso quando l'utente preme "Salva" */
  @Output() salva = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Salva ed Esci" */
  @Output() salvaEdEsci = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Indietro" */
  @Output() indietro = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Annulla" */
  @Output() annulla = new EventEmitter<void>();

  /** Tipi di attributo disponibili (dal backend) */
  attributiTipi: AttributoInfo[] = [];

  /** Campi per l'inserimento nuovo attributo */
  nuovoTipoAttributo = '';
  nuovoValoreAttributo = '';
  nuovoContoCollegato = '';

  /** Indice della riga in modifica (-1 se nessuna) */
  indiceRigaInModifica = -1;

  /** Flag per espandere/collassare l'accordion */
  accordionEspanso = false;

  // Proprietà per la paginazione
  currentPage = 0;
  pageSize = 10;
  pageSizeOptions: number[] = [10, 20, 50, 100];

  constructor(
    private attributoService: AttributoService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.caricaAttributi();
  }

  /**
   * Carica la lista degli attributi dal backend
   */
  caricaAttributi(): void {
    this.attributoService.getAttributiByEnte().subscribe({
      next: (data: AttributoInfo[]) => {
        this.attributiTipi = data;
      },
      error: () => {
        this.mostraErrore('Errore nel caricamento attributi');
      }
    });
  }

  /**
   * Annulla l'inserimento dell'attributo
   */
  onAnnullaAttributo(): void {
    this.nuovoTipoAttributo = '';
    this.nuovoValoreAttributo = '';
    this.nuovoContoCollegato = '';
    this.accordionEspanso = false;
    this.indiceRigaInModifica = -1;
  }

  /**
   * Conferma l'inserimento dell'attributo
   */
  onConfermaAttributo(): void {
    // Validazione campi obbligatori
    if (!this.nuovoTipoAttributo) {
      this.mostraErrore('Selezionare un tipo attributo');
      return;
    }

    if (!this.nuovoValoreAttributo) {
      this.mostraErrore('Inserire un valore');
      return;
    }

    // Crea l'attributo
    const tipoSelezionato = this.attributiTipi.find(t => t.codice === this.nuovoTipoAttributo);
    const attributo: PrimaNotaAttributo = {
      tipoAttributo: this.nuovoTipoAttributo,
      tipoAttributoDescrizione: tipoSelezionato?.descrizione,
      valore: this.nuovoValoreAttributo,
      conto: this.nuovoContoCollegato || undefined
    };

    if (this.indiceRigaInModifica >= 0) {
      // Modalità modifica: aggiorna la riga esistente
      this.attributi[this.indiceRigaInModifica] = attributo;
      this.mostraSuccesso('Attributo aggiornato');
    } else {
      // Modalità inserimento: aggiungi nuova riga
      this.attributi.push(attributo);
      this.mostraSuccesso('Attributo aggiunto');
    }

    this.attributiChange.emit(this.attributi);

    // Pulisci i campi dell'accordion
    this.onAnnullaAttributo();
  }

  /**
   * Prepara la modifica di un attributo esistente
   */
  modificaAttributo(index: number): void {
    const attributo = this.attributi[index];
    
    // Popola i campi dell'accordion con i dati dell'attributo
    this.nuovoTipoAttributo = attributo.tipoAttributo;
    this.nuovoValoreAttributo = attributo.valore;
    this.nuovoContoCollegato = attributo.conto || '';
    
    // Imposta l'indice della riga in modifica
    this.indiceRigaInModifica = index;
    
    // Espandi l'accordion
    this.accordionEspanso = true;
  }

  /**
   * Rimuove un attributo dalla lista
   */
  rimuoviAttributo(index: number): void {
    // Se siamo in modalità modifica, l'index è assoluto, altrimenti è relativo alla pagina
    const indexToRemove = this.isModifica ? index : (this.currentPage * this.pageSize) + index;
    
    this.attributi.splice(indexToRemove, 1);
    this.attributiChange.emit(this.attributi);
    this.mostraSuccesso('Attributo rimosso');
  }

  /**
   * Ottiene gli attributi per la pagina corrente
   */
  getAttributiPagina(): PrimaNotaAttributo[] {
    if (!this.isModifica) {
      // In inserimento, mostro tutti gli attributi (non c'è paginazione)
      return this.attributi;
    }
    
    // In modifica, applico la paginazione
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.attributi.slice(start, end);
  }

  /**
   * Verifica se la pagina corrente è l'ultima
   */
  isLastPage(): boolean {
    if (!this.isModifica) {
      return true;
    }
    const end = (this.currentPage + 1) * this.pageSize;
    return end >= this.attributi.length;
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
    if (!this.isModifica || this.attributi.length === 0) {
      return 0;
    }
    return Math.ceil(this.attributi.length / this.pageSize);
  }

  /**
   * Ottiene l'intervallo di elementi visualizzati
   */
  getDisplayRange(): string {
    if (!this.isModifica || this.attributi.length === 0) {
      return '';
    }
    const start = (this.currentPage * this.pageSize) + 1;
    const end = Math.min((this.currentPage + 1) * this.pageSize, this.attributi.length);
    return `${start}-${end} di ${this.attributi.length}`;
  }

  /**
   * Azione "Salva" - salva la prima nota
   */
  onSalva(): void {
    this.salva.emit();
  }

  /**
   * Azione "Salva ed Esci" - salva la prima nota ed esce
   */
  onSalvaEdEsci(): void {
    this.salvaEdEsci.emit();
  }

  /**
   * Azione "Indietro" - torna al tab Conti
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
