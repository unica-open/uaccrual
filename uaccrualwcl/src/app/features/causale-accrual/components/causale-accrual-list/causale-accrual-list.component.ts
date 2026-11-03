/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule, NgFor, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subscription, Observable } from 'rxjs'; // Aggiungi Subscription e Observable
import { take } from 'rxjs/operators';
import { CausaleAccrualService } from '../../../../core/services/causale-accrual.service';
import { ExportService } from '../../../../core/services/export.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Causale, Paged, CausaleSearchCriteria } from '../../model/causale.model'; // Importa i DTO reali
import { MatDialog } from '@angular/material/dialog'; // Import per MatDialog
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../../shared/components/confirm-dialog/confirm-dialog.component'; // Import per ConfirmDialogComponent e ConfirmDialogData

// Import per NGXS
import { Store, Select } from '@ngxs/store';
import { CausaleState } from '../../../../core/store/causale/causale.state';
import { ChangeCausalePage, SearchCausali, ValidaCausale, DeleteCausale } from '../../../../core/store/causale/causale.actions';
import { ContextState } from '../../../../core/store/context/context.state';
@Component({
  selector: 'app-causale-accrual-list',
  standalone: true,
  imports: [
    CommonModule,
    NgFor,
    NgClass,
    FormsModule,
    RouterModule,
  ],
  templateUrl: './causale-accrual-list.component.html',
  styleUrls: ['./causale-accrual-list.component.scss']
})
export class CausaleAccrualListComponent implements OnInit, OnDestroy {

  dataSource: Causale[] = [];
  totalElements: number = 0;
  pageSize: number = 10;
  currentPage: number = 0;
  pageSizeOptions: number[] = [10, 20, 50, 100];
  loading: boolean = false;
  totalPages: number = 0;
  searchFilters: CausaleSearchCriteria | null = null;

  causaleAccrualService = inject(CausaleAccrualService);
  private exportService = inject(ExportService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private store = inject(Store);
  private dialog = inject(MatDialog);
  public Math = Math;

  canInsert$ = this.store.select(ContextState.canAddCausale);

  private subscriptions = new Subscription();

  ngOnInit() {
    console.log('ngOnInit executed.');
    this.subscriptions.add(  
      this.store.select(CausaleState.getSearchResults).subscribe((results: Paged<Causale> | null) => {
        console.log('NGXS searchResults subscription triggered:', results);
        console.log('Raw results object from subscription:', results); // ADDED LOG
        if (results) {
          this.dataSource = results.data;
          console.log('dataSource after update:', this.dataSource); // ADDED LOG
          this.totalElements = results.total_elements;
          this.currentPage = results.page;
          this.pageSize = results.page_size;
          this.totalPages = Math.ceil(this.totalElements / this.pageSize);
        }
      })
    );

    this.subscriptions.add(
      this.store.select(CausaleState.isLoading).subscribe((loading: boolean) => {
        console.log('NGXS loading$ subscription triggered:', loading);
        this.loading = loading;
      })
    );

    this.subscriptions.add(
      this.store.select(CausaleState.getSearchFilter).subscribe({
        next: (filters: CausaleSearchCriteria | null) => {
          console.log('NGXS searchFilter$ subscription triggered:', filters);
          if (filters) {
            this.searchFilters = filters;
          }
        },
        error: (err: any) => {
          console.error('Error in searchFilter$ subscription:', err);
        },
        complete: () => {
          console.log('searchFilter$ subscription completed.');
        }
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  loadResults() {
    const filterToDispatch = this.searchFilters || {}; // Provide an empty object if searchFilters is null
    this.store.dispatch(new SearchCausali(filterToDispatch as CausaleSearchCriteria)); // Cast to ensure type compatibility
  }

  onPageSizeChange(event: any) {
    this.pageSize = Number(event.target.value);
    this.currentPage = 0;
    this.store.dispatch(new ChangeCausalePage(this.currentPage, this.pageSize));
  }

  onCustomPageChange(pageIndex: number) {
    if (pageIndex >= 0 && pageIndex < this.totalPages) {
      this.currentPage = pageIndex;
      this.store.dispatch(new ChangeCausalePage(this.currentPage, this.pageSize));
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
    this.router.navigate(['/causale/search']);
  }

  onExport() {
    const headers = [
      { key: 'tipo.descrizione', label: 'Tipo Causale' }, // Accedi alla descrizione
      { key: 'codice', label: 'Codice' },
      { key: 'descrizione', label: 'Descrizione Causale' },
      { key: 'evento.descrizione', label: 'Evento Causale' }, // Accedi alla descrizione
      { key: 'stato.descrizione', label: 'Stato' } // Accedi alla descrizione
    ];
    // L'export deve contenere tutte le causali trovate, non solo quelle della
    // pagina visualizzata: rileggiamo l'elenco completo con gli stessi filtri.
    this.loading = true;
    this.causaleAccrualService.getAllCausaliResults(this.searchFilters || {})
      .pipe(take(1))
      .subscribe({
        next: (data: Causale[]) => {
          this.loading = false;
          this.exportService.exportAsCsv(data, headers, 'risultati-ricerca-causale-accrual');
        },
        error: () => {
          this.loading = false;
          this.notificationService.error("Errore durante l'esportazione delle causali.");
        }
      });
  }

  onConsulta(causale: Causale) { // Usa il DTO Causale
    this.router.navigate([`/causale/dettaglio/${causale.id}`]);
  }

  onAggiorna(causale: Causale) { // Usa il DTO Causale
    if (causale.modificabile) { // Utilizza la proprietà dal DTO
      this.router.navigate([`/causale/modifica/${causale.id}`]);
    } else {
      alert(`La causale ${causale.codice} non è modificabile`);
    }
  }

  onValida(causale: Causale) { // Usa il DTO Causale
    if (causale.validabile) {
      this.store.dispatch(new ValidaCausale(causale.id));
    } else {
      alert(`La causale ${causale.codice} non è validabile`);
    }
  }

  onElimina(causale: Causale) { // Usa il DTO Causale
    if (causale.eliminabile) { // Utilizza la proprietà dal DTO
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          message: `Sei sicuro di voler eliminare la causale ${causale.codice}? Questa operazione è irreversibile.`,
          title: 'Conferma Eliminazione'
        } as ConfirmDialogData
      });

      dialogRef.afterClosed().subscribe(result => {
        console.log('Result from dialog:', result); // Aggiunto per debug
        if (result) {
          this.store.dispatch(new DeleteCausale(causale.id));
        }
      });
    } else {
      alert(`La causale ${causale.codice} non è eliminabile.`);
    }
  }

  onInserisci() {
    // La logica di verifica inseribilità non è ancora inclusa nel DTO Causale,
    // quindi per ora reindirizziamo direttamente. In futuro, potremmo volerla nel DTO.
    this.router.navigate(['/causale/inserisci']);
  }

  /**
   * Verifica se il pulsante di modifica deve essere visibile
   * Una causale è modificabile solo se ha stato 'P' (Provvisorio)
   */
  isModificabile(element: Causale): boolean {
    return !!element.modificabile;
  }

  /**
   * Verifica se il pulsante di validazione deve essere visibile
   * Una causale è validabile solo se ha stato 'P' (Provvisorio)
   */
  isValidabile(element: Causale): boolean {
    return !!element.validabile;
  }

  /**
   * Verifica se il pulsante di eliminazione deve essere visibile
   * Una causale è eliminabile solo se ha stato 'P' (Provvisorio)
   */
  isEliminabile(element: Causale): boolean {
    return !!element.eliminabile;
  }

  /**
   * Verifica se la causale ha stato 'A' (Annullato)
   * In questo caso, solo la consultazione è permessa
   */
  isAnnullata(element: Causale): boolean {
    return element.stato?.codice === 'A';
  }
}