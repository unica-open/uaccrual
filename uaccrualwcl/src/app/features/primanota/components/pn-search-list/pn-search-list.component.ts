/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngxs/store';
import { Observable, Subscription } from 'rxjs';
import { ContextState } from '../../../../core/store/context/context.state';
import { PnSearchResultsComponent } from '../pn-search-results/pn-search-results.component';
import { PrimaNotaSearchFilter, PrimaNotaSearchResult } from '../../models/primanota.model';
import { ChangePage, AnnullaPrimaNota, RiapriPrimaNota, ValidaPrimaNota, ValidaPrimeNoteMassiva, SearchPrimeNote } from '../../state/primanota-search.actions';
import { PrimanotaService } from '../../../../core/services/primanota.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-pn-search-list',
  standalone: true,
  imports: [CommonModule, RouterModule, PnSearchResultsComponent],
  templateUrl: './pn-search-list.component.html',
  styleUrl: './pn-search-list.component.scss'
})
export class PnSearchListComponent implements OnInit, OnDestroy {
  // Proprietà per i dati dal servizio come fa PDC
  dataSource: PrimaNotaSearchResult[] = [];
  filter: Partial<PrimaNotaSearchFilter> = {};
  totalElements: number = 0;
  pageSize: number = 20;
  currentPage: number = 0;
  loading: boolean = false;
  canInsert: boolean = false;
  canValidate: boolean = false;

  private subscriptions = new Subscription();

  constructor(
    private store: Store,
    private primanotaService: PrimanotaService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    // Sottoscrizione ai dati dal servizio come fa PDC
    const resultsSub = this.primanotaService.primaNotaList$.subscribe(data => {
      this.dataSource = data;
    });

    const totalElementsSub = this.primanotaService.totalElements$.subscribe(total => {
      this.totalElements = total;
    });

    const loadingSub = this.primanotaService.loading$.subscribe(loading => {
      this.loading = loading;
    });

    // Sottoscrizione ai filtri dal servizio
    const filterSub = this.primanotaService.filters$.subscribe(filters => {
      this.filter = filters;
    });

    // Sottoscrizione alla pagina corrente
    const currentPageSub = this.primanotaService.currentPage$.subscribe(page => {
      this.currentPage = page;
    });

    const canInsertSub = this.store.select(ContextState.canAddPrimaNota).subscribe(can => {
      this.canInsert = can;
    });

    const canValidateSub = this.store.select(ContextState.canValidaPrimaNota).subscribe(can => {
      this.canValidate = can;
    });

    this.subscriptions.add(resultsSub);
    this.subscriptions.add(totalElementsSub);
    this.subscriptions.add(loadingSub);
    this.subscriptions.add(filterSub);
    this.subscriptions.add(currentPageSub);
    this.subscriptions.add(canInsertSub);
    this.subscriptions.add(canValidateSub);

  }

  /**
   * Esegue un refresh dei dati della ricerca
   */
  refreshData(): void {
    const currentFilter = this.primanotaService.filtersValue;
    const currentPage = this.primanotaService.currentPageValue;
    const pageSize = this.primanotaService.pageSizeValue;

    if (Object.keys(currentFilter).length > 0 || currentPage > 0) {
      // Dispatcha l'azione di search con gli stessi filtri per aggiornare i dati
      this.store.dispatch(new SearchPrimeNote(currentFilter));
    }
  }

  onViewDetail(item: PrimaNotaSearchResult): void {
    if (item && item.anno) {
      // Naviga direttamente verso la pagina di dettaglio con i parametri e la provenienza
      this.router.navigate(['/primanota/dettaglio', item.anno, item.numeroProvvisorio], {
        queryParams: { from: 'results' }
      });
    } else {
      // Se non abbiamo l'anno nei risultati, non possiamo chiamare l'endpoint
      console.warn('Impossibile ottenere l\'anno per la prima nota con ID:', item?.id);
    }
  }

  onPageChange(event: { page: number; size: number }): void {
    // Usa il servizio per la paginazione come fa PDC
    const currentFilters = this.primanotaService.filtersValue;
    this.store.dispatch(new ChangePage(event.page, event.size));
  }

  onValida(item: { anno: number, numero: number }): void {
    if (!item || !item.anno || !item.numero) return;

    // Chiamo direttamente il backend
    // Il backend restituirà 409 con:
    // - WEB_CON_0016 se la verifica NON va a buon fine (NO num. definitivo OPPURE parametro = NO)
    // - WEB_CON_033 se la verifica VA a buon fine (HA num. definitivo E parametro = SI)
    this.store.dispatch(new ValidaPrimaNota(item.anno, item.numero));
  }

  onMassValidate(items: { anno: number, numero: number }[]): void {
    if (!items || items.length === 0) {
      this.notificationService.showError('WEB_ERR_0002 - Nessuna prima nota selezionata');
      return;
    }

    // Chiamo direttamente il backend
    // Il backend restituirà 409 con:
    // - WEB_CON_0017 se la verifica NON va a buon fine (NO num. definitivo OPPURE parametro = NO)
    // - WEB_CON_034 se la verifica VA a buon fine (HA num. definitivo E parametro = SI)
    this.store.dispatch(new ValidaPrimeNoteMassiva(items));
  }

  onInsert(): void {
    // Naviga alla pagina di inserimento nuova prima nota passando la provenienza dai risultati
    this.router.navigate(['/primanota/nuovo'], {
      queryParams: {
        from: 'results',
        ...this.filter  // Preserva i filtri applicati
      }
    });
  }

  onExport(): void {
    // Mostra un indicatore di caricamento
    this.primanotaService.loadingSubject$.next(true);

    // Ottieni i filtri correnti
    const currentFilters = this.primanotaService.filtersValue;

    // Chiama il servizio per l'export
    this.primanotaService.exportPrimaNotaList(currentFilters).subscribe({
      next: (blob: Blob) => {

        // Crea URL temporaneo per il download
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const today = new Date().toISOString().split('T')[0];
        link.download = `primanota_export_${today}.csv`;
        link.click();

        // Pulizia
        window.URL.revokeObjectURL(url);

        // Nascondi l'indicatore di caricamento IMMEDIATAMENTE
        this.primanotaService.loadingSubject$.next(false);

        // Mostra messaggio di successo
        this.notificationService.showMessageByCode('WEB_INF_0006'); // Messaggio di esportazione completata

      },
      error: (error) => {
        console.error('Errore durante l\'esportazione:', error);
        // Nascondi l'indicatore di caricamento in caso di errore
        this.primanotaService.loadingSubject$.next(false);
        this.notificationService.showError('WEB_ERR_0001 - Errore durante l\'esportazione');
      }
    });
  }

  onAnnulla(item: PrimaNotaSearchResult): void {
    if (!item || !item.anno) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        message: 'WEB_CON_0015 - Si desidera procedere con l\'annullamento prima nota?',
        title: 'Annullamento prima nota'
      } as ConfirmDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.store.dispatch(new AnnullaPrimaNota(item.anno, item.numeroProvvisorio));
      }
    });
  }

  onRiapri(item: PrimaNotaSearchResult): void {
    if (!item || !item.anno) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        message: 'WEB_CON_0023 - Si desidera procedere con la riapertura della prima nota?',
        title: 'Riapertura prima nota'
      } as ConfirmDialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.store.dispatch(new RiapriPrimaNota(item.anno, item.numeroProvvisorio));
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
