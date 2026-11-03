/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges, ChangeDetectorRef, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PrimaNotaSearchResult } from '../../models/primanota.model';
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format.pipe';
@Component({
  selector: 'app-pn-search-results',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AmountFormatPipe],
  templateUrl: './pn-search-results.component.html',
  styleUrls: ['./pn-search-results.component.scss']
})
export class PnSearchResultsComponent implements OnChanges, AfterViewChecked {
  @Input() results: PrimaNotaSearchResult[] | null = [];
  @Input() totalElements: number | null = 0;
  @Input() currentPage: number | null = 0;
  @Input() pageSize: number | null = 20;
  @Input() loading: boolean | null = false;
  @Input() filter: any = null;
  @Input() errorMessage: string | null = null;
  @Input() canInsert: boolean = false;
  @Input() canValidate: boolean = false;

  @Output() viewDetail = new EventEmitter<PrimaNotaSearchResult>();
  @Output() pageChange = new EventEmitter<{ page: number; size: number }>();
  @Output() massValidate = new EventEmitter<{ anno: number, numero: number }[]>();
  @Output() selectedItemsChange = new EventEmitter<number[]>();
  @Output() insert = new EventEmitter<void>();
  @Output() export = new EventEmitter<void>();
  @Output() annulla = new EventEmitter<PrimaNotaSearchResult>();
  @Output() riapri = new EventEmitter<PrimaNotaSearchResult>();
  @Output() valida = new EventEmitter<{ anno: number, numero: number }>();

  @ViewChild('selectAllCheckbox') selectAllCheckbox!: ElementRef<HTMLInputElement>;

  selectedItems: Set<number> = new Set();
  private previousIndeterminateState: boolean = false;
  
  // Page size options
  pageSizeOptions: number[] = [10, 20, 50, 100];

  constructor(private cdr: ChangeDetectorRef) { }

  onViewDetail(item: PrimaNotaSearchResult): void {
    this.viewDetail.emit(item);
  }

  onPageChange(page: number): void {
    if (this.pageSize) {
      this.pageChange.emit({ page, size: this.pageSize });
    }
  }

  onCustomPageChange(page: number): void {
    this.onPageChange(page);
  }

  onPageSizeChange(event: Event): void {
    const newSize = +(event.target as HTMLSelectElement).value;
    if (this.currentPage !== null) {
      this.pageChange.emit({ page: this.currentPage, size: newSize });
    }
  }

  onBack(): void {
    // Navigate back or emit event
  }

  onInsert(): void {
    this.insert.emit();
  }

  onExport(): void {
    this.export.emit();
  }

  onAnnulla(item: PrimaNotaSearchResult): void {
    this.annulla.emit(item);
  }

  onRiapri(item: PrimaNotaSearchResult): void {
    this.riapri.emit(item);
  }

  /**
   * Toggle selection of a single item
   */
  onToggleItem(id: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedItems.add(id);
    } else {
      this.selectedItems.delete(id);
    }
    this.emitSelectedItems();
    this.cdr.detectChanges();
  }

  /**
   * Toggle selection of all items in the current page
   */
  onToggleAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      // Select all items in the current results
      (this.results || []).forEach(item => this.selectedItems.add(item.id));
    } else {
      // Deselect all items in the current results
      (this.results || []).forEach(item => this.selectedItems.delete(item.id));
    }
    this.emitSelectedItems();
    this.cdr.detectChanges();
  }

  /**
   * Check if all items in the current page are selected
   */
  isAllSelected(): boolean {
    if (!this.results || this.results.length === 0) {
      return false;
    }
    return (this.results || []).every(item => this.selectedItems.has(item.id));
  }

  /**
   * Check if the selection is in indeterminate state (some but not all selected)
   */
  isIndeterminate(): boolean {
    if (!this.results || this.results.length === 0) {
      return false;
    }
    const selectedCount = (this.results || []).filter(item => this.selectedItems.has(item.id)).length;
    return selectedCount > 0 && selectedCount < this.results.length;
  }

  /**
   * Emit the selected items to parent components
   */
  private emitSelectedItems(): void {
    this.selectedItemsChange.emit(Array.from(this.selectedItems));
  }

  onCheckboxChange(id: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.selectedItems.add(id);
    } else {
      this.selectedItems.delete(id);
    }
  }

  onValida(item: PrimaNotaSearchResult): void {
    this.valida.emit({ anno: item.anno, numero: item.numeroProvvisorio });
  }

  onMassValidate(): void {
    if (this.selectedItems.size === 0) {
      return;
    }

    const itemsToValidate = (this.results || []).filter(item => this.selectedItems.has(item.id));
    const nonProvvisorie = itemsToValidate.filter(item => item.stato.toUpperCase() !== 'PROVVISORIA');

    if (nonProvvisorie.length > 0) {
      alert("L'operazione è possibile solo su prime note in stato PROVVISORIA");
      return;
    }

    // Emetti anno e numero per ogni elemento selezionato
    const idsToValidate = itemsToValidate.map(item => ({
      anno: item.anno,
      numero: item.numeroProvvisorio
    }));
    
    this.massValidate.emit(idsToValidate);
    this.selectedItems.clear();
  }

  // Helper for pagination
  get totalPages(): number {
    if (!this.totalElements || !this.pageSize) {
      return 0;
    }
    return Math.ceil(this.totalElements / this.pageSize);
  }

  getPages(): number[] {
    if (this.currentPage === null || !this.totalPages) {
      return [];
    }

    const pages: number[] = [];
    const current = this.currentPage;
    const total = this.totalPages;

    // Always show first page
    pages.push(0);

    // Show ellipsis before current if needed
    if (current > 2) {
      pages.push(-1);
    }

    // Show previous page if not already shown
    if (current > 1 && current - 1 !== 0) {
      pages.push(current - 1);
    }

    // Show current page if not first page
    if (current !== 0) {
      pages.push(current);
    }

    // Show next page
    if (current < total - 1) {
      pages.push(current + 1);
    }

    // Show ellipsis after current if needed
    if (current < total - 3 && total - 1 > current + 1) {
      pages.push(-1);
    }

    // Show last page if not already shown
    if (current < total - 2 && total - 1 !== 0) {
      pages.push(total - 1);
    }

    return pages;
  }

  // Called when input properties change
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['results']) {
      // Clear selection when results change (e.g., page change)
      this.selectedItems.clear();
      this.cdr.detectChanges();
    }
  }

  // Update indeterminate state after view check
  ngAfterViewChecked(): void {
    if (this.selectAllCheckbox) {
      const indeterminate = this.isIndeterminate();
      if (indeterminate !== this.previousIndeterminateState) {
        this.selectAllCheckbox.nativeElement.indeterminate = indeterminate;
        this.previousIndeterminateState = indeterminate;
      }
    }
  }

  // Expose Math for template
  Math = Math;
}
