/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Widget globale per parametri di tipo EVENTO
 * Implementa un select dropdown (semplificato per ora)
 */
@Component({
  selector: 'app-evento-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './evento-widget.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => EventoWidgetComponent),
    multi: true
  }]
})
export class EventoWidgetComponent implements ControlValueAccessor {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() options: any[] = [];

  value: any = null;
  onChange: any = () => {};
  onTouched: any = () => {};

  writeValue(value: any): void {
    this.value = value;
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

  onSelectionChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.onChange(value);
    this.onTouched();
  }
}
