/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { Store } from '@ngxs/store';
import { CausaleAccrualService } from '../../../../core/services/causale-accrual.service';
import { Causale, CausaleTipo, Evento, CausaleStato, ContoCollegato, CausaleRequest } from '../../model/causale.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { PdcTreeSearchModalComponent } from '../../../primanota/components/pdc-tree-search-modal/pdc-tree-search-modal.component';
import { PdcService } from '../../../../core/services/pdc.service';
import { ContextState } from '../../../../core/store/context/context.state';

@Component({
  selector: 'app-causale-accrual-insert',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterModule, PdcTreeSearchModalComponent],
  providers: [DatePipe],
  templateUrl: './causale-accrual-insert.component.html',
  styleUrls: ['./causale-accrual-insert.component.scss']
})
export class CausaleAccrualInsertComponent implements OnInit {

  causale: Partial<Causale> = {};
  tipiCausale: CausaleTipo[] = [];
  eventiCausale: Evento[] = [];
  statiCausale: CausaleStato[] = []; 
  
  nuovoConto: Partial<ContoCollegato> = {
    pdcId: 0,
    classe: '',
    codice: '',
    descrizione: '',
    segno: 'DARE',
    tipoImporto: 'LORDO',
    inizio_validita: '',
    fine_validita: undefined,
  };
  conti: ContoCollegato[] = []; 

  // Proprietà per controllare la visibilità della modale di ricerca conti
  showPdcTreeSearchModal: boolean = false;
  searchCodiceForModal: string = '';
  searchDescrizioneForModal: string = '';

  private router = inject(Router);
  private store = inject(Store);
  private causaleAccrualService = inject(CausaleAccrualService);
  private notificationService = inject(NotificationService);
  private datePipe = inject(DatePipe);
  private pdcService = inject(PdcService);

  private getAnnoEsercizio(): number {
    return this.store.selectSnapshot(ContextState.getAnnoBilancio) ?? new Date().getFullYear();
  }

  ngOnInit(): void {
    const defaultDate = `${this.getAnnoEsercizio()}-01-01`;
    this.causale.inizio_validita = defaultDate;
    this.nuovoConto.inizio_validita = defaultDate;
    this.loadDropdowns();
  }

  // Metodo per confrontare gli oggetti per ID per le select
  compareById(obj1: any, obj2: any): boolean {
    return obj1 && obj2 && obj1.id === obj2.id;
  }

  loadDropdowns(): void {
    // Ordina i tipi causale per descrizione
    this.causaleAccrualService.getTipiCausale().subscribe(data => {
      this.tipiCausale = data.sort((a, b) => 
        (a.descrizione || '').localeCompare(b.descrizione || '')
      );
    });
    // Ordina gli eventi per descrizione
    this.causaleAccrualService.getEventiCausale().subscribe(data => {
      this.eventiCausale = data.sort((a, b) => 
        (a.descrizione || '').localeCompare(b.descrizione || '')
      );
    });
    this.causaleAccrualService.getStati().subscribe(data => {
      this.statiCausale = data;
      this.causale.stato = this.statiCausale.find(s => s.codice === 'P');
    });
  }

  private _saveCausale(exitAfterSave: boolean): void {
    if (!this.causale.codice || !this.causale.descrizione || !this.causale.tipo || !this.causale.evento || !this.causale.inizio_validita) {
      this.notificationService.error('Errore: Compila tutti i campi obbligatori (Codice, Descrizione, Tipo, Evento, Inizio Validità).');
      return;
    }

    const causaleToSave: CausaleRequest = {
      codice: this.causale.codice,
      descrizione: this.causale.descrizione,
      tipo: this.causale.tipo ? { id: this.causale.tipo.id } : undefined,
      evento: this.causale.evento ? { id: this.causale.evento.id } : undefined,
      stato: this.causale.stato ? { id: this.causale.stato.id } : undefined,
      ente: { id: 1 }, 
      inizio_validita: this.causale.inizio_validita ? this.datePipe.transform(new Date(this.causale.inizio_validita), 'yyyy-MM-dd')! : undefined,
      fine_validita: this.causale.fine_validita ? this.datePipe.transform(new Date(this.causale.fine_validita), 'yyyy-MM-dd')! : undefined,
      contiCollegati: this.conti.map(conto => ({
        pdcId: conto.pdcId,
        classe: conto.classe,
        codice: conto.codice,
        descrizione: conto.descrizione,
        segno: conto.segno,
        tipoImporto: conto.tipoImporto,
        inizioValidita: conto.inizio_validita!,
        fineValidita: conto.fine_validita
      }))
    };
    this.causaleAccrualService.createCausale(causaleToSave).subscribe({
      next: (response) => {
        this.notificationService.success('Causale salvata con successo!');
        if (exitAfterSave) {
          this.router.navigate(['/causale/results']);
        } else {
          this.router.navigate(['/causale/modifica', response.id]);
        }
      },
      error: (error) => {
        console.error('Errore durante lo salvataggio della causale:', error);
        this.notificationService.error('Errore durante lo salvataggio della causale.');
      }
    });
  }

  onSave(): void {
    this._saveCausale(false);
  }

  onSaveAndExit(): void {
    this._saveCausale(true);
  }

  onCancel(): void {
    this.causale = { inizio_validita: `${this.getAnnoEsercizio()}-01-01` };
    this.conti = [];
    this.resetNuovoConto();
  }

  aggiungiConto() {
    if (!this.nuovoConto.pdcId) {
      this.notificationService.error('Errore: Seleziona un Conto dalla ricerca ad albero.');
      return;
    }
    if (!this.nuovoConto.codice || !this.nuovoConto.descrizione || !this.nuovoConto.classe || !this.nuovoConto.segno || !this.nuovoConto.tipoImporto || !this.nuovoConto.inizio_validita) {
      this.notificationService.error('Errore: Compila Codice Conto, Descrizione, Classe, Segno, Tipo Importo e Inizio Validità per il conto collegato.');
      return;
    }

    const contoDaAggiungere: ContoCollegato = {
      pdcId: Number(this.nuovoConto.pdcId!), 
      classe: this.nuovoConto.classe!,
      codice: this.nuovoConto.codice!,
      descrizione: this.nuovoConto.descrizione!,
      segno: this.nuovoConto.segno!,
      tipoImporto: this.nuovoConto.tipoImporto!,
      inizio_validita: this.nuovoConto.inizio_validita!, 
      fine_validita: this.nuovoConto.fine_validita, 
    };

    this.conti.push(contoDaAggiungere); 
    this.resetNuovoConto();
  }

  resetNuovoConto() {
    this.nuovoConto = {
      pdcId: 0,
      classe: '',
      codice: '',
      descrizione: '',
      segno: 'DARE',
      tipoImporto: 'LORDO',
      inizio_validita: `${this.getAnnoEsercizio()}-01-01`,
      fine_validita: undefined,
    };
    this.showPdcTreeSearchModal = false;
  }

  rimuoviConto(index: number) {
    this.conti.splice(index, 1);
  }

  onBack() {
    this.router.navigate(['/causale/results']);
  }

  cercaConto() {
    // Imposta i termini di ricerca separatamente per codice e descrizione
    const searchCodice = this.nuovoConto.codice || '';
    const searchDescrizione = this.nuovoConto.descrizione || '';

    // Costruisci i filtri per la ricerca
    const filters: any = {};
    if (searchCodice.trim()) {
      filters.codice = searchCodice.trim();
    }
    if (searchDescrizione.trim()) {
      filters.descrizione = searchDescrizione.trim();
    }

    // Se non ci sono filtri, apri la modale per mostrare tutto l'albero
    if (!searchCodice.trim() && !searchDescrizione.trim()) {
      this.searchCodiceForModal = '';
      this.searchDescrizioneForModal = '';
      this.showPdcTreeSearchModal = true;
      return;
    }

    // Esegui la ricerca per determinare quanti risultati ci sono
    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data) => {
        if (data.length === 1 && (!data[0].children || data[0].children.length === 0)) {
          // Un solo risultato senza figli: selezione automatica
          this.onPdcSelected(data[0]);
        } else {
          // Più risultati o un solo risultato non-foglia: apri la modale
          this.searchCodiceForModal = searchCodice;
          this.searchDescrizioneForModal = searchDescrizione;
          this.showPdcTreeSearchModal = true;
        }
      },
      error: (error) => {
        console.error('Errore nella ricerca dei conti:', error);
        this.searchCodiceForModal = searchCodice;
        this.searchDescrizioneForModal = searchDescrizione;
        this.showPdcTreeSearchModal = true;
      }
    });
  }

  onPdcSelected(selectedAccount: any): void {
    if (selectedAccount) {
      this.nuovoConto.pdcId = Number(selectedAccount.id);
      this.nuovoConto.codice = selectedAccount.codice_ufficiale || selectedAccount.codice;
      this.nuovoConto.descrizione = selectedAccount.descrizione;
      if (this.nuovoConto.codice) {
          this.nuovoConto.classe = this.nuovoConto.codice.substring(0, 1);
      }
    }
    this.showPdcTreeSearchModal = false;
  }

  onModalClosed(): void {
    this.showPdcTreeSearchModal = false;
  }
}
