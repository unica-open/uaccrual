/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { PdcService } from '../../../../../core/services/pdc.service';
import { PdcTreeNode } from '../../../../pdc/model/pdc.model';

/**
 * Widget globale per parametri di tipo PDC (Piano dei Conti)
 * Implementa una ricerca ad albero con autocomplete
 */
@Component({
  selector: 'app-pdc-widget',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, MatIconModule],
  templateUrl: './pdc-widget.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => PdcWidgetComponent),
    multi: true
  }]
})
export class PdcWidgetComponent implements ControlValueAccessor {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() placeholder: string = 'Cerca conto...';

  value: any = null;
  onChange: any = () => {};
  onTouched: any = () => {};

  searchResults: PdcTreeNode[] = [];
  loading = false;
  showResults = false;
  searchTerm = '';
  selectedNode: PdcTreeNode | null = null;

  constructor(
    private pdcService: PdcService
  ) {
  }

  writeValue(value: any): void {
    this.value = value;
    // Se value è un oggetto PdcTreeNode, lo uso per popolare selectedNode
    if (value && typeof value === 'object' && value.codice) {
      this.selectedNode = value;
      this.searchTerm = value.descrizione || value.codice;
    } else if (value && typeof value === 'string') {
      // Se è solo una stringa (codice), provo a caricare il nodo
      this.loadNodeByCodice(value);
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  /**
   * Carica un nodo PDC dato il codice
   */
  private loadNodeByCodice(codice: string): void {
    if (!codice || codice.trim() === '') {
      return;
    }

    this.loading = true;
    this.pdcService.searchPdcTree({ codice }).subscribe({
      next: (nodes: PdcTreeNode[]) => {
        if (nodes && nodes.length > 0) {
          this.selectedNode = nodes[0];
          this.searchTerm = nodes[0].descrizione || nodes[0].codice;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Gestisce l'input di ricerca
   */
  onSearch(): void {
    if (this.searchTerm.length < 2) {
      this.searchResults = [];
      this.showResults = false;
      return;
    }

    this.loading = true;

    this.pdcService.searchPdcTree({ codice: this.searchTerm }).subscribe({
      next: (results: PdcTreeNode[]) => {
        this.searchResults = results || [];
        this.showResults = true;
        this.loading = false;
      },
      error: () => {
        this.searchResults = [];
        this.showResults = false;
        this.loading = false;
      }
    });
  }

  /**
   * Seleziona un nodo dalla lista dei risultati
   */
  selectItem(node: PdcTreeNode): void {
    this.selectedNode = node;
    this.value = node.codice;  // Salvo solo il codice come valore
    this.onChange(this.value);
    this.onTouched();
    this.showResults = false;
    this.searchTerm = node.descrizione || node.codice;
  }

  /**
   * Apre il dropdown dei risultati
   */
  onFocus(): void {
    if (this.searchTerm.length >= 2) {
      this.showResults = true;
    }
  }

  /**
   * Chiude il dropdown dopo un breve delay
   */
  onBlur(): void {
    setTimeout(() => {
      this.showResults = false;
      this.onTouched();
    }, 200);
  }

  /**
   * Pulisce la selezione
   */
  clearSelection(): void {
    this.selectedNode = null;
    this.searchTerm = '';
    this.value = null;
    this.onChange(null);
    this.onTouched();
    this.searchResults = [];
    this.showResults = false;
  }
}
