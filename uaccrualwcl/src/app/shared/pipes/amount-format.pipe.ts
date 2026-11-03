/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe per la formattazione degli importi in formato italiano.
 * Formato: 1.234.567,89 (punto come separatore delle migliaia, virgola per i decimali)
 *
 * Utilizzo:
 *   {{ valore | amountFormat }}           // 1234567.89 -> 1.234.567,89
 *   {{ valore | amountFormat:0 }}         // 1234567.89 -> 1.234.568 (arrotondato)
 *   {{ valore | amountFormat:4 }}         // 1234567.89 -> 1.234.567,8900
 */
@Pipe({
  name: 'amountFormat',
  standalone: true
})
export class AmountFormatPipe implements PipeTransform {

  transform(value: number | string | null | undefined, decimalPlaces: number = 2): string {
    // Gestione esplicita di null e undefined
    if (value === null || value === undefined) {
      return '';
    }
    
    // Gestione stringa vuota
    if (value === '') {
      return '';
    }

    // Converte il valore in numero
    const num = typeof value === 'string' ? parseFloat(value) : value;

    if (isNaN(num)) {
      return String(value);
    }

    // Formattazione manuale per garantire il formato italiano
    return this.formatNumberIt(num, decimalPlaces);
  }

  /**
   * Formatta un numero in formato italiano manualmente
   * Esempio: 1234567.89 -> 1.234.567,89
   */
  private formatNumberIt(num: number, decimalPlaces: number): string {
    // Separa parte intera e decimale
    const fixedNum = num.toFixed(decimalPlaces);
    const [integerPart, decimalPart] = fixedNum.split('.');

    // Aggiungi separatori delle migliaia alla parte intera
    const integerWithSeparators = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');

    // Unisci con la virgola come separatore decimale
    return `${integerWithSeparators},${decimalPart}`;
  }
}
