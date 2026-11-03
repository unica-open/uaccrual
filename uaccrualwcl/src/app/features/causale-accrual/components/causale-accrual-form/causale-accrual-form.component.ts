/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { Store } from '@ngxs/store';
import { ContextState } from '../../../../core/store/context/context.state';

import { Causale, CausaleTipo, Evento, CausaleStato, ContoCollegato, CausaleRequest } from '../../model/causale.model';
import { CausaleAccrualService } from '../../../../core/services/causale-accrual.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PdcService } from '../../../../core/services/pdc.service';
import { PdcTreeSearchModalComponent } from '../../../primanota/components/pdc-tree-search-modal/pdc-tree-search-modal.component';

@Component({
  selector: 'app-causale-accrual-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, RouterModule, PdcTreeSearchModalComponent],
  providers: [DatePipe],
  templateUrl: './causale-accrual-form.component.html',
  styleUrl: './causale-accrual-form.component.scss',
})
export class CausaleAccrualFormComponent implements OnInit {
  causaleId: number | null = null;
  causale: Partial<Causale> | null = null;

  mode: 'view' | 'edit' | 'insert' = 'view';
  get isViewMode() { return this.mode === 'view'; }
  get isEditMode() { return this.mode === 'edit'; }
  get isInsertMode() { return this.mode === 'insert'; }

  tipiCausale: CausaleTipo[] = [];
  eventiCausale: Evento[] = [];
  statiCausale: CausaleStato[] = [];

  nuovoConto: Partial<ContoCollegato> = {
    pdcId: 0, classe: '', codice: '', descrizione: '',
    segno: 'DARE', tipoImporto: 'LORDO',
    inizio_validita: '',
    fine_validita: undefined,
  };

  showPdcTreeSearchModal = false;
  searchCodiceForModal = '';
  searchDescrizioneForModal = '';
  editingContoIndex: number | null = null;

  private originalCausale: Partial<Causale> | null = null;

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private causaleAccrualService = inject(CausaleAccrualService);
  private pdcService = inject(PdcService);
  private notificationService = inject(NotificationService);
  private datePipe = inject(DatePipe);
  private store = inject(Store);

  canUpdate$ = this.store.select(ContextState.canUpdateCausale);

  private getAnnoEsercizio(): number {
    return this.store.selectSnapshot(ContextState.getAnnoBilancio) ?? new Date().getFullYear();
  }

  ngOnInit() {
    this.mode = (this.route.snapshot.data['mode'] as 'view' | 'edit' | 'insert') || 'view';

    const defaultDate = `${this.getAnnoEsercizio()}-01-01`;
    this.nuovoConto.inizio_validita = defaultDate;

    if (this.isInsertMode) {
      this.causale = { inizio_validita: defaultDate, contiCollegati: [] };
      this.loadDropdowns();
    } else {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.causaleId = +id;
        this.loadCausale(this.causaleId);
        if (this.isEditMode) {
          this.loadDropdowns();
        }
      }
    }
  }

  loadCausale(id: number) {
    this.causaleAccrualService.getCausaleById(id).subscribe({
      next: (data: any) => {
        this.causale = data;

        if (this.causale!.inizio_validita) {
          this.causale!.inizio_validita = this.datePipe.transform(new Date(this.causale!.inizio_validita), 'yyyy-MM-dd') || undefined;
        }
        if (this.causale!.fine_validita) {
          this.causale!.fine_validita = this.datePipe.transform(new Date(this.causale!.fine_validita), 'yyyy-MM-dd') || undefined;
        }

        if (this.causale!.contiCollegati) {
          const dataOggi = new Date().toISOString().split('T')[0];
          const dataFallback = this.causale!.inizio_validita || dataOggi;
          this.causale!.contiCollegati = this.causale!.contiCollegati.map((conto: any) => {
            const inizio = conto.inizio_validita || conto.inizioValidita || dataFallback;
            const fine = conto.fine_validita || conto.fineValidita;
            return {
              ...conto,
              inizio_validita: this.datePipe.transform(new Date(inizio), 'yyyy-MM-dd') || dataFallback,
              fine_validita: fine ? this.datePipe.transform(new Date(fine), 'yyyy-MM-dd') : undefined
            };
          });
        } else {
          this.causale!.contiCollegati = [];
        }

        // Lo snapshot per l'ANNULLA va preso DOPO la normalizzazione delle date:
        // gli input type="date" accettano solo 'yyyy-MM-dd', quindi ripristinare il
        // formato grezzo del BE lascerebbe i campi vuoti.
        this.originalCausale = this.deepClone(this.causale);
      },
      error: () => {
        this.notificationService.error('Errore nel caricamento della causale.');
        this.router.navigate(['/causale/results']);
      }
    });
  }

  compareById(obj1: any, obj2: any): boolean {
    return obj1 && obj2 && obj1.id === obj2.id;
  }

  loadDropdowns(): void {
    this.causaleAccrualService.getTipiCausale().subscribe(data => {
      this.tipiCausale = data.sort((a, b) => (a.descrizione || '').localeCompare(b.descrizione || ''));
    });
    this.causaleAccrualService.getEventiCausale().subscribe(data => {
      this.eventiCausale = data.sort((a, b) => (a.descrizione || '').localeCompare(b.descrizione || ''));
    });
    this.causaleAccrualService.getStati().subscribe(data => {
      this.statiCausale = data;
      if (this.isInsertMode && this.causale) {
        this.causale.stato = data.find(s => s.codice === 'P');
      }
    });
  }

  onBack() {
    this.router.navigate(['/causale/results']);
  }

  onModifica() {
    if (this.causaleId) {
      this.router.navigate(['/causale/modifica', this.causaleId]);
    }
  }

  onCancel(): void {
    if (this.isInsertMode) {
      this.causale = {
        inizio_validita: `${this.getAnnoEsercizio()}-01-01`,
        contiCollegati: [],
        stato: this.statiCausale.find(s => s.codice === 'P')
      };
      this.resetNuovoConto();
    } else if (this.originalCausale) {
      this.causale = this.deepClone(this.originalCausale);
    } else if (this.causaleId) {
      this.loadCausale(this.causaleId);
    }
  }

  onSave(): void { this._saveCausale(false); }
  onSaveAndExit(): void { this._saveCausale(true); }

  private _saveCausale(exitAfterSave: boolean): void {
    if (this.isInsertMode) {
      this._insertCausale(exitAfterSave);
    } else {
      this._updateCausale(exitAfterSave);
    }
  }

  private _insertCausale(exitAfterSave: boolean): void {
    if (!this.causale?.codice || !this.causale.descrizione || !this.causale.tipo || !this.causale.evento || !this.causale.inizio_validita) {
      this.notificationService.error('Compila tutti i campi obbligatori (Codice, Descrizione, Tipo, Evento, Inizio Validità).');
      return;
    }
    const req: CausaleRequest = {
      codice: this.causale.codice,
      descrizione: this.causale.descrizione,
      tipo: { id: this.causale.tipo.id },
      evento: { id: this.causale.evento.id },
      stato: this.causale.stato ? { id: this.causale.stato.id } : undefined,
      ente: { id: 1 },
      inizio_validita: this.datePipe.transform(new Date(this.causale.inizio_validita), 'yyyy-MM-dd') || undefined,
      fine_validita: this.causale.fine_validita
        ? this.datePipe.transform(new Date(this.causale.fine_validita), 'yyyy-MM-dd') || undefined
        : undefined,
      contiCollegati: (this.causale.contiCollegati || []).map(c => ({
        pdcId: Number(c.pdcId), classe: c.classe, codice: c.codice,
        descrizione: c.descrizione, segno: c.segno, tipoImporto: c.tipoImporto,
        inizioValidita: c.inizio_validita!, fineValidita: c.fine_validita
      }))
    };
    this.causaleAccrualService.createCausale(req).subscribe({
      next: (response) => {
        this.notificationService.success('Causale salvata con successo!');
        if (exitAfterSave) {
          this.router.navigate(['/causale/results']);
        } else {
          this.router.navigate(['/causale/modifica', response.id]);
        }
      },
      error: () => this.notificationService.error('Errore durante lo salvataggio della causale.')
    });
  }

  private _updateCausale(exitAfterSave: boolean): void {
    const causale = this.causale as Causale;
    if (!causale?.id) {
      this.notificationService.error('Dati della causale non caricati o ID causale mancante.');
      return;
    }
    const req: CausaleRequest = {
      codice: causale.codice,
      descrizione: causale.descrizione,
      tipo: causale.tipo ? { id: causale.tipo.id } : undefined,
      evento: causale.evento ? { id: causale.evento.id } : undefined,
      stato: causale.stato ? { id: causale.stato.id } : undefined,
      ente: causale.ente ? { id: causale.ente.id } : undefined,
      inizio_validita: causale.inizio_validita,
      fine_validita: causale.fine_validita,
      contiCollegati: causale.contiCollegati?.map(c => ({
        pdcId: Number(c.pdcId!), classe: c.classe, codice: c.codice,
        descrizione: c.descrizione, segno: c.segno, tipoImporto: c.tipoImporto,
        inizioValidita: c.inizio_validita!, fineValidita: c.fine_validita
      })) || [],
      version: causale.version
    };
    this.causaleAccrualService.updateCausale(causale.id, req).subscribe({
      next: () => {
        this.notificationService.success('Causale aggiornata con successo!');
        if (exitAfterSave) {
          this.router.navigate(['/causale/results']);
        } else {
          this.loadCausale(this.causaleId!);
        }
      },
      error: () => this.notificationService.error('Errore durante l\'aggiornamento della causale.')
    });
  }

  aggiungiConto() {
    if (!this.causale) return;
    if (!this.nuovoConto.pdcId) {
      this.notificationService.error('Seleziona un Conto dalla ricerca ad albero.');
      return;
    }
    const segno = this.nuovoConto.segno;
    const tipoImporto = this.nuovoConto.tipoImporto;
    if (!segno || !tipoImporto) {
      this.notificationService.error('Compila Segno e Tipo Importo per il conto collegato.');
      return;
    }
    const inizioValiditaVal = this.nuovoConto.inizio_validita
      || this.causale!.inizio_validita
      || `${this.getAnnoEsercizio()}-01-01`;
    if (!this.causale.contiCollegati) {
      this.causale.contiCollegati = [];
    }
    const conto: ContoCollegato = {
      pdcId: Number(this.nuovoConto.pdcId!),
      classe: this.nuovoConto.classe!,
      codice: this.nuovoConto.codice!,
      descrizione: this.nuovoConto.descrizione!,
      segno, tipoImporto,
      inizio_validita: inizioValiditaVal!,
      fine_validita: this.nuovoConto.fine_validita,
    };
    if (this.isEditMode && this.editingContoIndex !== null) {
      this.causale.contiCollegati[this.editingContoIndex] = conto;
      this.notificationService.success('Conto collegato aggiornato.');
    } else {
      this.causale.contiCollegati.push(conto);
      this.notificationService.success('Conto collegato aggiunto.');
    }
    this.resetNuovoConto();
  }

  resetNuovoConto() {
    this.nuovoConto = {
      pdcId: 0, classe: '', codice: '', descrizione: '',
      segno: 'DARE', tipoImporto: 'LORDO',
      inizio_validita: `${this.getAnnoEsercizio()}-01-01`,
      fine_validita: undefined,
    };
    this.showPdcTreeSearchModal = false;
    this.editingContoIndex = null;
  }

  rimuoviConto(index: number) {
    this.causale?.contiCollegati?.splice(index, 1);
  }

  modificaConto(index: number) {
    if (this.causale?.contiCollegati?.[index]) {
      this.editingContoIndex = index;
      const contoOriginale = this.causale.contiCollegati[index] as any;
      const dataOggi = new Date().toISOString().split('T')[0];
      const dataFallback = this.causale.inizio_validita || dataOggi;
      this.nuovoConto = {
        ...contoOriginale,
        inizio_validita: contoOriginale.inizio_validita || contoOriginale.inizioValidita || dataFallback
      };
      this.notificationService.info('Modifica il conto nel form e premi Aggiorna.');
    }
  }

  cercaConto() {
    const searchCodice = this.nuovoConto.codice || '';
    const searchDescrizione = this.nuovoConto.descrizione || '';
    const filters: any = {};
    if (searchCodice.trim()) filters.codice = searchCodice.trim();
    if (searchDescrizione.trim()) filters.descrizione = searchDescrizione.trim();
    if (!searchCodice.trim() && !searchDescrizione.trim()) {
      this.searchCodiceForModal = '';
      this.searchDescrizioneForModal = '';
      this.showPdcTreeSearchModal = true;
      return;
    }
    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data) => {
        if (data.length === 1 && (!data[0].children || data[0].children.length === 0)) {
          this.onPdcSelected(data[0]);
        } else {
          this.searchCodiceForModal = searchCodice;
          this.searchDescrizioneForModal = searchDescrizione;
          this.showPdcTreeSearchModal = true;
        }
      },
      error: () => {
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

  private deepClone<T>(obj: T): T {
    return JSON.parse(JSON.stringify(obj));
  }
}
