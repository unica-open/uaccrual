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
  selector: 'app-data-da-widget',
  standalone: true,
  imports: [CommonModule],
  styles: [`
    :host input[type="date"] {
      background-color: #ffffff !important;
      color: #111827 !important;
      color-scheme: light;
    }
  `],
  template: `
    <div class="flex flex-col space-y-1">
      <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
        {{ label }}
        <span *ngIf="required" class="text-red-500">*</span>
      </label>
      <input
        type="date"
        [value]="value"
        (input)="onInputChange($event)"
        (blur)="onTouched()"
        [required]="required"
        [disabled]="disabled"
        class="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:cursor-not-allowed bg-white text-gray-900"
      />
    </div>
  `,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => DataDaWidget),
    multi: true
  }]
})
export class DataDaWidget implements ControlValueAccessor, OnDestroy {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() set control(ctrl: FormControl | undefined) {
    this._ctrlSub?.unsubscribe();
    this._control = ctrl;
    if (ctrl) {
      const v = ctrl.value;
      if (!v || v === new Date().toISOString().split('T')[0]) {
        ctrl.setValue(this.defaultDate, { emitEvent: false });
      }
      this.value = ctrl.value;
      this._ctrlSub = ctrl.valueChanges.subscribe(val => this.value = val);
    }
  }

  private _control?: FormControl;
  private _ctrlSub?: Subscription;

  value: any = '1998-01-01';

  private get defaultDate(): string {
    const anno = this.store.selectSnapshot(ContextState.getAnnoBilancio) ?? new Date().getFullYear();
    return `${anno}-01-01`;
  }

  constructor(private store: Store) {}
  onChange: any = () => {};
  onTouched: any = () => {};

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
