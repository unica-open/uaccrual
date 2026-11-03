/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, Input, SimpleChanges, OnChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngxs/store';
import { FlussoService } from '../../../../core/services/flusso.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { NotificheService } from '../../../../core/services/notifiche.service';
import { FlussoSearchFilter, FlussoSearchResult, PagedFlussoSearchResult } from '../../model/flusso.model';
import { take } from 'rxjs/operators';
import { ContextState } from '../../../../core/store/context/context.state';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule } from '@angular/material/dialog';
import { ElaboraConfirmDialogComponent } from '../elabora-confirm-dialog/elabora-confirm-dialog.component';
import { ElaboraProgressDialogComponent } from '../elabora-progress-dialog/elabora-progress-dialog.component';
import { ElaboraAnnullamentoConfermaDialogComponent } from '../elabora-annullamento-conferma-dialog/elabora-annullamento-conferma-dialog.component';
import { ElaboraFlussoResponseDTO } from '../../model/flusso.model';
@Component({
  selector: 'app-flusso-search-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
  ],
  templateUrl: './flusso-search-list.component.html',
  styleUrls: ['./flusso-search-list.component.scss']
})
export class FlussoSearchListComponent implements OnInit, OnChanges {

  @Input() inputFilters: FlussoSearchFilter | null = null;

  dataSource: FlussoSearchResult[] = [];
  flussoSearchFilters: FlussoSearchFilter = {};
  loading: boolean = false;

  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  pageSizeOptions: number[] = [5, 10, 20, 50];
  public Math = Math;
  canElabFlusso$!: Observable<boolean>;

  constructor(
    private datePipe: DatePipe,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private flussoService: FlussoService,
    private notificationService: NotificationService,
    private notificheService: NotificheService,
    private dialog: MatDialog,
    private store: Store
  ) {
    this.canElabFlusso$ = this.store.select(ContextState.canElabFlusso);
    const perms = this.store.selectSnapshot(ContextState.getUserPermissions);
    console.log('[FlussoList] constructor - permessi nello store:', perms.length, perms);
    console.log('[FlussoList] constructor - canElabFlusso (snapshot):', perms.includes('OP-SPE-elaboraFile'));
  }

  ngOnInit(): void {
    if (this.inputFilters !== null) {
      this.flussoSearchFilters = this.inputFilters;
      this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputFilters'] && !changes['inputFilters'].firstChange && this.inputFilters !== null) {
      this.flussoSearchFilters = this.inputFilters;
      this.currentPage = 0;
      this.searchFlussi(this.flussoSearchFilters, 0, this.pageSize);
    }
  }

  searchFlussi(filters: FlussoSearchFilter, page: number, pageSize: number): void {
    this.loading = true;
    this.flussoService.searchFlussi(filters, page, pageSize).subscribe({
      next: (response: PagedFlussoSearchResult) => {
        this.dataSource = response.data;
        this.totalElements = response.total_elements;
        this.currentPage = response.page;
        this.pageSize = response.page_size;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);
        this.loading = false;
        const statiPresenti = response.data.map(f => JSON.stringify(f.stato));
        console.log('[FlussoList] risultati:', response.data.length, '- stati:', statiPresenti);
        const perms = this.store.selectSnapshot(ContextState.getUserPermissions);
        console.log('[FlussoList] permessi al momento dei risultati:', perms.includes('OP-SPE-elaboraFile'), '- totale:', perms.length);
      },
      error: (error) => {
        console.error('Error fetching flussi:', error);
        this.loading = false;
        this.notificationService.showError('Errore durante il recupero dei flussi.', 'Errore di sistema');
      }
    });
  }

  onPageSizeChange(event: any): void {
    this.pageSize = parseInt(event.target.value, 10);
    this.currentPage = 0;
    this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
  }

  onCustomPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
    }
  }

  getPages(): (number | -1)[] {
    const pages: (number | -1)[] = [];
    if (this.totalPages <= 7) {
      for (let i = 0; i < this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(0);
      if (this.currentPage > 3) {
        pages.push(-1);
      }
      for (let i = Math.max(1, this.currentPage - 1); i <= Math.min(this.totalPages - 2, this.currentPage + 1); i++) {
        pages.push(i);
      }
      if (this.currentPage < this.totalPages - 4) {
        pages.push(-1);
      }
      pages.push(this.totalPages - 1);
    }
    return pages;
  }

  onConsulta(element: FlussoSearchResult): void {
    const retParams: any = { retPage: this.currentPage, retPageSize: this.pageSize };
    if (this.flussoSearchFilters.codiceFile) retParams.retCodiceFile = this.flussoSearchFilters.codiceFile;
    if (this.flussoSearchFilters.fonte) retParams.retFonte = this.flussoSearchFilters.fonte;
    if (this.flussoSearchFilters.tipoFile) retParams.retTipoFile = this.flussoSearchFilters.tipoFile;
    if (this.flussoSearchFilters.dataCaricamento) retParams.retDataCaricamento =
      this.flussoSearchFilters.dataCaricamento;
    this.router.navigate(['../detail', element.id], { relativeTo: this.activatedRoute, queryParams: retParams });
  }

  onExportOne(element: FlussoSearchResult): void {
    this.flussoService.downloadFlusso(element.id).subscribe({
      next: (response: Blob) => {
        const filename = `file_originale_${element.codice}.csv`;
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(response);
        a.href = objectUrl;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(objectUrl);
      },
      error: (err) => {
        console.error(`Errore durante il download del file originale ${element.codice}:`, err);
        this.notificationService.showError(`Errore durante il download del file originale ${element.codice}.`, 'Errore');
      }
    });
  }

  onBack(): void {
    const queryParams: any = {};
    if (this.flussoSearchFilters.codiceFile) queryParams.codiceFile = this.flussoSearchFilters.codiceFile;
    if (this.flussoSearchFilters.fonte) queryParams.fonte = this.flussoSearchFilters.fonte;
    if (this.flussoSearchFilters.tipoFile) queryParams.tipoFile = this.flussoSearchFilters.tipoFile;
    if (this.flussoSearchFilters.dataCaricamento) queryParams.dataCaricamento =
      this.flussoSearchFilters.dataCaricamento;
    this.router.navigate(['/flusso/search'], { queryParams });
  }

  /**
   * Determines if a file can be processed based on its type and state
   */
  canBeProcessed(element: FlussoSearchResult): boolean {
    // Usiamo il codice dello stato restituito dal backend
    return element.stato === 'Da elaborare';
  }

  /**
   * Handles the elaboration of a selected file
   */
  onElabora(element: FlussoSearchResult): void {
    const confirmRef = this.dialog.open(ElaboraConfirmDialogComponent, {
      width: '480px',
      data: { codiceFlusso: element.codice }
    });

    confirmRef.afterClosed().pipe(take(1)).subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      // Apri subito la progress dialog con simulazione (prima della chiamata HTTP)
      this.flussoService.forceProgressStart(element.id);
      const progressRef = this.dialog.open(ElaboraProgressDialogComponent, {
        width: '500px',
        disableClose: true,
        data: { flussoId: element.id, codiceFlusso: element.codice }
      });

      this.flussoService.elaboraFlusso(element.id).pipe(take(1)).subscribe({
        next: (response: ElaboraFlussoResponseDTO) => {
          if (response?.needsConfirmation) {
            // Conflitto: azzera progress (nessuna elaborazione avviata) e apri il dialog di annullamento
            this.flussoService.cancelProgress();
            this.flussoService.clearPendingConfirmation();
            progressRef.close();
            this.apriDialogAnnullamento(element, response);
          } else {
            // Elaborazione sincrona completata: aggiorna la barra e lascia chiudere il dialog
            const success = response?.flussoElaboraResponse?.success;
            const errMsg = !success ? (response?.flussoElaboraResponse?.message || undefined) : undefined;
            this.flussoService.forceProgressEnd(element.id, success ? 'COMPLETED' : 'FAILED', errMsg);
            progressRef.afterClosed().pipe(take(1)).subscribe(() => {
              if (success) {
                element.stato = 'Elaborato';
                this.notificationService.showSuccess(`Flusso ${element.codice} elaborato con successo.`, 'Elaborazione');
              } else {
                this.notificationService.showError(`Errore durante l'elaborazione del flusso ${element.codice}.`, 'Errore');
              }
              this.notificheService.load().subscribe();
              this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
            });
          }
        },
        error: (err) => {
          console.error(`Errore avvio elaborazione flusso ${element.codice}:`, err);
          const httpErrMsg = err?.error?.message || err?.error?.detail || err?.error?.error || err?.message;
          this.flussoService.forceProgressEnd(element.id, 'FAILED', httpErrMsg);
          progressRef.afterClosed().pipe(take(1)).subscribe(() => {
            this.notificationService.showError(`Impossibile avviare l'elaborazione del flusso ${element.codice}.`, 'Errore');
          });
        }
      });
    });
  }

  private apriDialogAnnullamento(element: FlussoSearchResult, elaboraResponse: ElaboraFlussoResponseDTO): void {
    const annullaRef = this.dialog.open(ElaboraAnnullamentoConfermaDialogComponent, {
      width: '680px',
      disableClose: true,
      data: {
        codiceFlusso: element.codice,
        confirmationMessage: elaboraResponse.confirmationMessage || 'Esistono già prime note associate a questo flusso. Vuoi annullarle e ricrearle?',
        primaNote: elaboraResponse.primaNoteDaAnnullare || [],
        resumeToken: elaboraResponse.resumeToken
      }
    });

    annullaRef.afterClosed().pipe(take(1)).subscribe((userConfirmed: boolean | undefined) => {
      if (userConfirmed === true) {
        // Utente ha confermato: riprendi in modo asincrono, mostra progress
        this.flussoService.resumeElaborazioneFlusso(elaboraResponse.resumeToken!, true).pipe(take(1)).subscribe({
          next: () => {
            this.flussoService.startStatusPolling(element.id);
            const progressRef = this.dialog.open(ElaboraProgressDialogComponent, {
              width: '500px',
              disableClose: true,
              data: { flussoId: element.id, codiceFlusso: element.codice }
            });
            progressRef.afterClosed().pipe(take(1)).subscribe((result: { success: boolean } | undefined) => {
              if (result?.success) {
                element.stato = 'Elaborato';
                this.notificationService.showSuccess(`Flusso ${element.codice} elaborato con successo.`, 'Elaborazione');
              } else {
                this.notificationService.showError(`Errore durante l'elaborazione del flusso ${element.codice}.`, 'Errore');
              }
              // L'esito dell'elaborazione in background e' anche una notifica persistente.
              this.notificheService.load().subscribe();
              this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
            });
          },
          error: () => {
            this.notificationService.showError('Errore durante la ripresa dell\'elaborazione.', 'Errore');
          }
        });
      } else if (userConfirmed === false) {
        // Utente ha rifiutato: annulla sincrono
        this.flussoService.resumeElaborazioneFlusso(elaboraResponse.resumeToken!, false).pipe(take(1)).subscribe({
          next: () => {
            this.notificationService.showInfo('Elaborazione annullata.', 'Annullato');
            // L'esito resta nel centro avvisi: va riletto dal backend, non tenuto in memoria.
            this.notificheService.load().subscribe();
            this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize);
          },
          error: () => this.searchFlussi(this.flussoSearchFilters, this.currentPage, this.pageSize)
        });
      }
      // undefined = dialog chiusa senza azione (ESC) → non fare nulla
    });
  }
}