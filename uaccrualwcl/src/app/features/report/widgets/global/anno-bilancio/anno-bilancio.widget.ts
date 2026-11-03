/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';
import { Store } from '@ngxs/store';
import { ContextState } from '../../../../../core/store/context/context.state';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-anno-bilancio-widget',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex flex-col space-y-1">
      <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
        {{ label }}
        <span *ngIf="required" class="text-red-500">*</span>
      </label>
      <input
        type="number"
        [value]="value"
        (input)="onInputChange($event)"
        (blur)="onTouched()"
        [required]="required"
        [disabled]="disabled"
        class="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
      />
    </div>
  `,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => AnnoBilancioWidget),
    multi: true
  }]
})
export class AnnoBilancioWidget implements ControlValueAccessor, OnDestroy {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() set control(ctrl: FormControl | undefined) {
    this._ctrlSub?.unsubscribe();
    this._control = ctrl;
    if (ctrl) {
      const v = ctrl.value;
      if (v === null || v === undefined || v === '') {
        ctrl.setValue(this.defaultAnno, { emitEvent: false });
      }
      this.value = ctrl.value;
      this._ctrlSub = ctrl.valueChanges.subscribe(val => this.value = val);
    }
  }

  private _control?: FormControl;
  private _ctrlSub?: Subscription;

  value: any = null;
  onChange: any = () => {};
  onTouched: any = () => {};

  private get defaultAnno(): number {
    return this.store.selectSnapshot(ContextState.getAnnoBilancio) ?? new Date().getFullYear();
  }

  constructor(private store: Store) {}

  ngOnDestroy(): void {
    this._ctrlSub?.unsubscribe();
  }

  writeValue(value: any): void {
    this.value = value;
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

  onInputChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this._control) {
      this._control.setValue(value);
    } else {
      this.onChange(value);
    }
    this.onTouched();
  }
}
