/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule, NgFor, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { PdcService } from '../../../../core/services/pdc.service';
import { Pdc } from '../../model/pdc.model';
import { ExportService } from '../../../../core/services/export.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { NotificationService } from '../../../../core/services/notification.service';
import { InfoDialogComponent } from '../../../../shared/components/info-dialog/info-dialog.component';
// Import per NGXS
import { Store, Select } from '@ngxs/store';
import { PdcState } from '../../../../core/store/pdc/pdc.state';
import { ChangePdcPage, ClearMessage, DeletePdc, SearchPdcConti } from '../../../../core/store/pdc/pdc.actions';

@Component({
  selector: 'app-pdc-list',
  standalone: true,
  imports: [
    CommonModule,
    NgFor,
    NgClass,
    FormsModule,
  ],
  templateUrl: './pdc-list.component.html',
  styleUrls: ['./pdc-list.component.scss']
})
export class PdcListComponent implements OnInit, OnDestroy {

  // Non usiamo più lo stato NGXS per questi dati, ma direttamente i BehaviorSubject del servizio
  results$!: Observable<any>;
  loading$!: Observable<boolean>;

  dataSource: Pdc[] = [];
  totalElements: number = 0;
  pageSize: number = 10;
  currentPage: number = 0;
  pageSizeOptions: number[] = [10, 20, 50, 100];
  totalPages: number = 0;

  private subscriptions = new Subscription();

  private store = inject(Store);
  public pdcService = inject(PdcService);
  private exportService = inject(ExportService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);
  public Math = Math;

  ngOnInit() {
    // Sottoscriviamoci direttamente ai BehaviorSubject del servizio
    const resultsSub = this.pdcService.pdcList$.subscribe(data => {
      this.dataSource = data;
    });

    const totalElementsSub = this.pdcService.totalElements$.subscribe(total => {
      this.totalElements = total;
      this.totalPages = Math.ceil(total / this.pageSize);
    });

    const loadingSub = this.pdcService.loading$.subscribe(loading => {
      // Non facciamo nulla di particolare qui, ma possiamo usarlo se necessario
    });

    const messageSub = this.store.select(PdcState.getMessage).subscribe(message => {
      if (!message?.text) return;
      if (message.type === 'success') this.notificationService.success(message.text, message.title);
      else if (message.type === 'error') this.notificationService.error(message.text, message.title);
      else if (message.type === 'warning') this.notificationService.warning(message.text, message.title);
      else this.notificationService.info(message.text, message.title);
    });

    this.subscriptions.add(resultsSub);
    this.subscriptions.add(totalElementsSub);
    this.subscriptions.add(loadingSub);
    this.subscriptions.add(messageSub);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
    this.store.dispatch(new ClearMessage());
  }

  onPageSizeChange(event: any) {
    const newSize = Number(event.target.value);
    this.pageSize = newSize;
    this.currentPage = 0;
    this.store.dispatch(new ChangePdcPage(this.currentPage, this.pageSize)).subscribe({
      error: (error) => {
        console.error('Errore durante il cambio dimensione pagina:', error);
      }
    });
  }

  onCustomPageChange(pageIndex: number) {
    if (pageIndex >= 0 && pageIndex < this.totalPages) {
      this.currentPage = pageIndex;
      this.store.dispatch(new ChangePdcPage(this.currentPage, this.pageSize)).subscribe({
        error: (error) => {
          console.error('Errore durante il cambio pagina:', error);
        }
      });
    }
  }

  getPages(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    let startPage = Math.max(0, this.currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
      startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    if (startPage > 0) {
      pages.push(0);
      if (startPage > 1) pages.push(-1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    if (endPage < this.totalPages - 1) {
      if (endPage < this.totalPages - 2) pages.push(-1);
      pages.push(this.totalPages - 1);
    }

    return pages;
  }
  
  onBack() {
    this.router.navigate(['/pdc/search']);
  }

  onExport() {
    this.pdcService.loadingSubject$.next(true);

    const currentFilters = this.pdcService.filtersValue;
    const today = new Date().toISOString().split('T')[0];

    this.pdcService.getAllPdcResults(currentFilters).subscribe({
      next: (data: Pdc[]) => {
        // Pre-mappa foglia (boolean) e codice (usa codice_ufficiale) prima di passare all'ExportService
        const exportData = data.map(item => ({
          ...item,
          codice_export: item.codice_ufficiale || item.codice,
          foglia_label: item.foglia ? 'Si' : 'No'
        }));

        const headers = [
          { key: 'pdc_classe.descrizione', label: 'Classe' },
          { key: 'pdc_tipo.descrizione',   label: 'Tipo' },
          { key: 'evento.descrizione',      label: 'Evento' },
          { key: 'livello',                 label: 'Livello' },
          { key: 'segmento',                label: 'Segmento' },
          { key: 'codice_export',           label: 'Codice' },
          { key: 'descrizione',             label: 'Descrizione' },
          { key: 'foglia_label',            label: 'Foglia' },
          { key: 'pdc_stato.descrizione',   label: 'Stato' },
        ];

        this.exportService.exportAsCsv(exportData, headers, `pdc_export_${today}`);

        this.pdcService.loadingSubject$.next(false);
        this.notificationService.showMessageByCode('WEB_INF_0006');
      },
      error: (error) => {
        console.error('Errore durante l\'esportazione:', error);
        this.pdcService.loadingSubject$.next(false);
        this.notificationService.showMessageByCode('WEB_ERR_0001');
      }
    });
  }

  onConsulta(pdc: Pdc) {
    // Utilizza la tripletta per la navigazione
    this.router.navigate([`/pdc/dettaglio/${pdc.codice_ufficiale || pdc.codice}/${pdc.pdc_classe?.codice || 'N/A'}/${pdc.pdc_famiglia?.codice || 'N/A'}`]);
  }

  onAggiorna(pdc: Pdc) {
    if (this.pdcService.isPdcModificabile(pdc)) {
      // Utilizza la tripletta per la navigazione
      this.router.navigate([`/pdc/edit/${pdc.codice_ufficiale || pdc.codice}/${pdc.pdc_classe?.codice || 'N/A'}/${pdc.pdc_famiglia?.codice || 'N/A'}`]);
    } else {
      this.notificationService.info(`Il conto ${pdc.codice_ufficiale || pdc.codice} non è modificabile`, 'Operazione non consentita');
    }
  }

  onElimina(pdc: Pdc) {
    if (this.pdcService.isPdcEliminabile(pdc)) {
      // AI generated 2026-09-14
      // reviewed by simonep
      const codiceUfficiale = pdc.codice_ufficiale || pdc.codice;
      const codiceClasse = pdc.pdc_classe?.codice;
      const codiceFamiglia = pdc.pdc_famiglia?.codice;

      if (!codiceClasse || !codiceFamiglia) {
        this.notificationService.warning(`Il Pdc ${codiceUfficiale} non riporta classe e famiglia: impossibile eliminarlo`, 'Operazione non consentita');
        return;
      }

      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: { message: 'WEB_WRN_0006 - Si vuole eliminare il dato selezionato?', title: 'Conferma Eliminazione' } as ConfirmDialogData
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          // La tripletta è già qui: passarla evita la ricerca di ricostruzione che il servizio faceva
          this.store.dispatch(new DeletePdc(codiceUfficiale, codiceClasse, codiceFamiglia));
        }
      });
    } else {
      this.notificationService.warning(`Il Pdc ${pdc.codice_ufficiale || pdc.codice} non è eliminabile`, 'Operazione non consentita');
    }
  }

  onInserisciFiglio(pdc: Pdc) {
    if (this.pdcService.canInsertPdcFiglio(pdc)) {
      // Usa la tripletta per la navigazione
      const codiceUfficiale = pdc.codice_ufficiale || pdc.codice;
      const codiceClasse = pdc.pdc_classe?.codice || 'N/A';
      const codiceFamiglia = pdc.pdc_famiglia?.codice || 'N/A';
      this.router.navigate([`/pdc/new/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}`]);
    } else {
      this.notificationService.info(`Non è possibile inserire un figlio per il conto ${pdc.codice_ufficiale || pdc.codice}`, 'Operazione non consentita');
    }
  }
}