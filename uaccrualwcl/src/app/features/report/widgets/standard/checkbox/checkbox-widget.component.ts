/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';
import { Subscription } from 'rxjs';

/**
 * Widget standard per parametri di tipo CHECKBOX
 * Implementa un checkbox booleano
 */
@Component({
  selector: 'app-checkbox-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkbox-widget.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => CheckboxWidgetComponent),
    multi: true
  }]
})
export class CheckboxWidgetComponent implements ControlValueAccessor, OnDestroy {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() set control(ctrl: FormControl | undefined) {
    this._ctrlSub?.unsubscribe();
    this._control = ctrl;
    if (ctrl) {
      this.value = ctrl.value === true || ctrl.value === 'true';
      this._ctrlSub = ctrl.valueChanges.subscribe(v => this.value = v === true || v === 'true');
    }
  }

  private _control?: FormControl;
  private _ctrlSub?: Subscription;

  value: boolean = false;
  onChange: any = () => {};
  onTouched: any = () => {};

  ngOnDestroy(): void {
    this._ctrlSub?.unsubscribe();
  }

  writeValue(value: any): void {
    this.value = value === true || value === 'true';
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

  onCheckboxChange(event: Event): void {
    const value = (event.target as HTMLInputElement).checked;
    this.value = value;
    if (this._control) {
      this._control.setValue(value);
    } else {
      this.onChange(value);
    }
    this.onTouched();
  }
}
