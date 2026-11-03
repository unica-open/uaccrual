/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { PdcService } from '../../../../../core/services/pdc.service';
import { PdcTreeNode } from '../../../../pdc/model/pdc.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-pdc-widget',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, MatIconModule],
  templateUrl: './pdc.widget.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => PdcWidgetComponent),
    multi: true
  }]
})
export class PdcWidgetComponent implements ControlValueAccessor, OnDestroy {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() placeholder: string = 'Cerca conto...';
  @Input() set control(ctrl: FormControl | undefined) {
    this._ctrlSub?.unsubscribe();
    this._control = ctrl;
    if (ctrl) {
      this.value = ctrl.value;
      this.syncFromValue(ctrl.value);
      this._ctrlSub = ctrl.valueChanges.subscribe(v => {
        this.value = v;
        this.syncFromValue(v);
      });
    }
  }

  private _control?: FormControl;
  private _ctrlSub?: Subscription;

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

  ngOnDestroy(): void {
    this._ctrlSub?.unsubscribe();
  }

  writeValue(value: any): void {
    this.value = value;
    this.syncFromValue(value);
  }

  private syncFromValue(value: any): void {
    if (value && typeof value === 'object' && value.codice) {
      this.selectedNode = value;
      this.searchTerm = value.descrizione || value.codice;
    } else if (value && typeof value === 'string') {
      this.loadNodeByCodice(value);
    } else {
      this.selectedNode = null;
      this.searchTerm = '';
    }
  }

  registerOnChange(fn: any): void {
    if (!this._control) {
      this.onChange = fn;
    }
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

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

  onSearch(event?: Event): void {
    if (event) {
      this.searchTerm = (event.target as HTMLInputElement).value;
    }
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

  selectItem(node: PdcTreeNode): void {
    this.selectedNode = node;
    this.value = node.codice;
    if (this._control) {
      this._control.setValue(node.codice);
    } else {
      this.onChange(node.codice);
    }
    this.onTouched();
    this.showResults = false;
    this.searchTerm = node.descrizione || node.codice;
  }

  onFocus(): void {
    if (this.searchTerm.length >= 2) {
      this.showResults = true;
    }
  }

  onBlur(): void {
    setTimeout(() => {
      this.showResults = false;
      this.onTouched();
    }, 200);
  }

  clearSelection(): void {
    this.selectedNode = null;
    this.searchTerm = '';
    this.value = null;
    if (this._control) {
      this._control.setValue(null);
    } else {
      this.onChange(null);
    }
    this.onTouched();
    this.searchResults = [];
    this.showResults = false;
  }
}
