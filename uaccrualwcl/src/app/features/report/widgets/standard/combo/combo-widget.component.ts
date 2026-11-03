/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, forwardRef, ElementRef, ViewChild, AfterViewInit, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormControl } from '@angular/forms';
import { ParameterOption } from '../../../models/report.models';
import { Subscription } from 'rxjs';

/**
 * Widget standard per parametri di tipo COMBO
 * Implementa un select dropdown
 */
@Component({
  selector: 'app-combo-widget',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './combo-widget.component.html',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => ComboWidgetComponent),
    multi: true
  }]
})
export class ComboWidgetComponent implements ControlValueAccessor, AfterViewInit, OnChanges, OnDestroy {

  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() options: ParameterOption[] = [];
  @Input() placeholder?: string;
  @Input() set control(ctrl: FormControl | undefined) {
    this._ctrlSub?.unsubscribe();
    this._control = ctrl;
    if (ctrl) {
      this.value = ctrl.value;
      this._ctrlSub = ctrl.valueChanges.subscribe(v => {
        this.value = v;
        this.applyValueToSelect();
      });
    }
  }

  private _control?: FormControl;
  private _ctrlSub?: Subscription;

  @ViewChild('selectElement', { static: true }) selectRef!: ElementRef<HTMLSelectElement>;

  value: any = null;
  onChange: any = () => {};
  onTouched: any = () => {};

  ngOnDestroy(): void {
    this._ctrlSub?.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.applyValueToSelect();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['options'] && this.value != null) {
      setTimeout(() => this.applyValueToSelect());
    }
  }

  writeValue(value: any): void {
    this.value = value;
    this.applyValueToSelect();
  }

  private applyValueToSelect(): void {
    if (this.selectRef?.nativeElement) {
      this.selectRef.nativeElement.value = this.value != null ? String(this.value) : '';
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

  onSelectionChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (this._control) {
      this._control.setValue(value);
    } else {
      this.onChange(value);
    }
    this.onTouched();
  }
}
