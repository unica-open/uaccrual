/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';

@Component({
  selector: 'app-causale-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './causale.widget.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => CausaleWidgetComponent),
    multi: true
  }]
})
export class CausaleWidgetComponent implements ControlValueAccessor {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() options: any[] = [];
  @Input() control?: FormControl;

  value: any = null;
  onChange: any = () => {};
  onTouched: any = () => {};

  writeValue(value: any): void {
    if (!this.control) {
      this.value = value;
    }
  }

  registerOnChange(fn: any): void {
    if (!this.control) {
      this.onChange = fn;
    }
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onSelectionChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (this.control) {
      this.control.setValue(value);
    } else {
      this.onChange(value);
    }
    this.onTouched();
  }
}
