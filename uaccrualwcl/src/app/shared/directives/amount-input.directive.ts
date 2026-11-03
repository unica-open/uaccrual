/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import {
  Directive,
  ElementRef,
  HostListener,
  Input,
  OnInit,
  Renderer2
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Direttiva per la formattazione automatica degli importi negli input.
 * Formatta con il separatore delle migliaia (punto) e decimali (virgola) in formato italiano.
 *
 * Caratteristiche:
 * - Nessuna formattazione mentre si digita, ma il valore nel model è sempre arrotondato ai
 *   decimali dichiarati, così da coincidere con quello mostrato
 * - Formattazione solo alla perdita del focus
 * - Accetta la virgola come separatore decimale durante la digitazione
 * - Il backspace cancella normalmente senza riportare a 0
 * - Quando si rientra nel campo, mostra la virgola per i decimali e rimuove il punto delle migliaia
 * - I valori numerici (es. dalla URL) vengono automaticamente formattati in italiano
 *
 * Utilizzo:
 *   <input [amountInput]="2" [(ngModel)]="importo" />
 *   <input amountInput [amountDecimalPlaces]="4" [formControl]="ctrl" />
 */
@Directive({
  selector: '[amountInput]',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: AmountInputDirective,
      multi: true
    }
  ]
})
export class AmountInputDirective implements ControlValueAccessor, OnInit {

  /** Numero di decimali (default: 2) */
  @Input() amountDecimalPlaces: number = 2;

  /** Consente valori negativi (default: false) */
  @Input() amountAllowNegative: boolean = false;

  private _value: number | null = null;
  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};
  private isFocused: boolean = false;

  constructor(
    private el: ElementRef<HTMLInputElement>,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    // Formatta il valore iniziale se presente
    const initialValue = this.el.nativeElement.value;
    if (initialValue) {
      const num = this.parseValue(initialValue);
      if (!isNaN(num)) {
        this._value = num;
        this.el.nativeElement.value = this.formatNumber(num);
      }
    }
  }

  // Implementazione ControlValueAccessor
  writeValue(value: number | string | null): void {
    // Converte stringa a numero se necessario
    if (typeof value === 'string') {
      this._value = this.parseValue(value);
    } else if (typeof value === 'number') {
      // Il valore è già un numero (probabilmente dalla URL)
      this._value = value;
    } else {
      this._value = null;
    }

    // Mostra il valore formattato solo se non focalizzato
    if (!this.isFocused && this._value !== null && this._value !== undefined && typeof this._value === 'number') {
      this.el.nativeElement.value = this.formatNumber(this._value);
    } else if (this.isFocused && this._value !== null && this._value !== undefined) {
      // Quando focalizzato, mostra il valore con virgola decimale ma senza punto delle migliaia
      this.el.nativeElement.value = this.getRawValue(this._value);
    } else {
      this.el.nativeElement.value = '';
    }
  }

  registerOnChange(fn: (value: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.renderer.setProperty(this.el.nativeElement, 'disabled', isDisabled);
  }

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target.value;

    // Rimuovi tutti i separatori delle migliaia (punti) - l'utente potrebbe averli incollati
    const rawValue = value.replace(/\./g, '');

    // Sostituisci la virgola con il punto per il parsing
    const normalizedValue = rawValue.replace(',', '.');

    // Se il valore è vuoto, imposta null
    if (normalizedValue === '' || normalizedValue === '-' || normalizedValue === '.') {
      this._value = null;
      this.onChange(null);
      return;
    }

    // Parsing del numero
    const num = parseFloat(normalizedValue);

    if (isNaN(num)) {
      // Mantieni il valore nel campo ma non aggiornare _value
      return;
    }

    // Validazione valori negativi
    if (num < 0 && !this.amountAllowNegative) {
      // Rimuovi il segno meno
      target.value = target.value.replace('-', '');
      this._value = null;
      this.onChange(null);
      return;
    }

    // Il model riceve il valore già arrotondato ai decimali dichiarati: senza questo un importo
    // digitato con tre decimali resta intero nel model mentre a video se ne vedono due, e chi
    // somma gli importi lavora su numeri diversi da quelli mostrati.
    const arrotondato = this.arrotonda(num);

    this._value = arrotondato;
    this.onChange(arrotondato);
  }

  @HostListener('focus')
  onFocus(): void {
    this.isFocused = true;
    // Quando si rientra nel campo, mostra il valore con virgola decimale ma senza punto delle migliaia
    if (this._value !== null && this._value !== undefined) {
      this.el.nativeElement.value = this.getRawValue(this._value);
    }
  }

  @HostListener('blur')
  onBlur(): void {
    this.isFocused = false;
    this.onTouched();
    // Applica la formattazione completa quando si perde il focus
    if (this._value !== null && this._value !== undefined && typeof this._value === 'number') {
      this.el.nativeElement.value = this.formatNumber(this._value);
    } else {
      this.el.nativeElement.value = '';
    }
  }

  @HostListener('keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    // Consenti: backspace, delete, tab, escape, enter, arrow keys
    const allowedKeys = ['Backspace', 'Delete', 'Tab', 'Escape', 'Enter', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

    // Consenti Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
    if ((event.ctrlKey || event.metaKey) && ['a', 'c', 'v', 'x'].includes(event.key.toLowerCase())) {
      return;
    }

    if (allowedKeys.includes(event.key)) {
      return;
    }

    // Blocca tasti non numerici (eccetto virgola, punto e meno)
    const isNumber = /^[0-9]$/.test(event.key);
    const isDecimalSeparator = event.key === ',' || event.key === '.';
    const isNegative = event.key === '-' && this.amountAllowNegative;

    if (!isNumber && !isDecimalSeparator && !isNegative) {
      event.preventDefault();
      return;
    }

    // Previene inserimento multiplo di separatori decimali
    if (isDecimalSeparator) {
      const target = this.el.nativeElement as HTMLInputElement;
      const currentValue = target.value;
      // Conta le virgole già presenti
      const commaCount = (currentValue.match(/,/g) || []).length;
      if (commaCount >= 1) {
        event.preventDefault();
      }
    }

    // Previene inserimento del meno se non all'inizio
    if (isNegative && event.key === '-') {
      const target = this.el.nativeElement as HTMLInputElement;
      const selectionStart = target.selectionStart;
      if (selectionStart !== 0) {
        event.preventDefault();
      }
    }
  }

  // AI generated 2026-09-18
  // reviewed by tarantinod
  /**
   * Arrotonda il valore ai decimali dichiarati, con criterio HALF_UP.
   */
  private arrotonda(value: number): number {
    const decimali = this.amountDecimalPlaces;
    if (!isFinite(value) || decimali < 0) {
      return value;
    }
    // Lo spostamento della virgola in notazione esponenziale evita l'errore binario di
    // `value * 10^n` e arrotonda sulla rappresentazione decimale, come il
    // BigDecimal.setScale(2, HALF_UP) con cui il backend persiste l'importo: 1,005 dà 1,01.
    const segno = value < 0 ? -1 : 1;
    const spostato = Number(`${Math.abs(value)}e${decimali}`);
    if (!isFinite(spostato)) {
      // Valori che JavaScript rappresenta già in notazione esponenziale
      return Number(value.toFixed(decimali));
    }
    return segno * Number(`${Math.round(spostato)}e-${decimali}`);
  }

  /**
   * Restituisce il valore "raw" per l'editing: senza separatore delle migliaia, con virgola decimale
   * Esempio: 1234567.89 -> 1234567,89
   */
  private getRawValue(value: number): string {
    if (typeof value !== 'number' || isNaN(value)) {
      return '';
    }
    const fixedNum = value.toFixed(this.amountDecimalPlaces);
    const [integerPart, decimalPart] = fixedNum.split('.');
    // Rimuovi gli zeri non significativi dalla parte decimale
    let trimmedDecimal = decimalPart;
    // Mantieni sempre almeno un decimale se il numero originale aveva decimali
    // Altrimenti rimuovi tutti i decimali se sono tutti zeri
    if (this.amountDecimalPlaces > 0) {
      // Rimuovi zeri finali ma mantieni almeno uno zero se necessario per coerenza
      trimmedDecimal = decimalPart.replace(/0+$/, '');
      if (trimmedDecimal === '' && decimalPart.length > 0) {
        // Se tutti zeri, mantieni solo uno zero per indicare che ci sono decimali
        trimmedDecimal = '0';
      }
    }
    return trimmedDecimal ? `${integerPart},${trimmedDecimal}` : integerPart;
  }

  /**
   * Formatta un numero in formato italiano manualmente
   * Esempio: 1234567.89 -> 1.234.567,89
   */
  private formatNumber(value: number): string {
    // Controllo che il valore sia un numero valido
    if (typeof value !== 'number' || isNaN(value)) {
      return '';
    }
    const fixedNum = value.toFixed(this.amountDecimalPlaces);
    const [integerPart, decimalPart] = fixedNum.split('.');
    const integerWithSeparators = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${integerWithSeparators},${decimalPart}`;
  }

  /**
   * Parse un valore stringa in numero
   * Formato italiano: virgola = decimale, punto = migliaia
   * Esempio: "1.234,56" -> 1234.56
   */
  private parseValue(value: string): number {
    if (!value) {
      return NaN;
    }
    // Rimuovi i separatori delle migliaia (punti)
    const rawValue = value.replace(/\./g, '');
    // Converti la virgola in punto per il parsing
    const normalizedValue = rawValue.replace(',', '.');
    return parseFloat(normalizedValue);
  }
}
