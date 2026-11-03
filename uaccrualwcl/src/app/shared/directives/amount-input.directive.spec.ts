/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// AI generated 2026-09-18
// reviewed by tarantinod

import { ElementRef, Renderer2 } from '@angular/core';
import { AmountInputDirective } from './amount-input.directive';

describe('AmountInputDirective', () => {

  let input: HTMLInputElement;
  let direttiva: AmountInputDirective;
  let valoreEmesso: number | null;

  beforeEach(() => {
    input = document.createElement('input');
    direttiva = new AmountInputDirective(new ElementRef(input), {} as Renderer2);
    valoreEmesso = null;
    direttiva.registerOnChange(valore => (valoreEmesso = valore));
  });

  /** Simula la digitazione: il browser aggiorna il campo, la direttiva reagisce all'evento input */
  function digita(testo: string): number | null {
    input.value = testo;
    direttiva.onInput({ target: input } as unknown as Event);
    return valoreEmesso;
  }

  it('arrotonda ai decimali dichiarati l\'importo digitato con più cifre', () => {
    expect(digita('456080,782')).toBe(456080.78);
    expect(digita('137123,764')).toBe(137123.76);
  });

  it('mette nel model lo stesso valore che mostra alla perdita del focus', () => {
    digita('456080,782');
    direttiva.onBlur();

    expect(input.value).toBe('456.080,78');
    expect(valoreEmesso).toBe(456080.78);
  });

  // Task 1127: con questi importi il totale calcolato sui valori del model dava 4.973.324,65
  // mentre le righe mostrate a video sommavano a 4.973.324,64, e la quadratura risultava a posto
  it('somma gli importi della segnalazione 1127 al totale mostrato a video', () => {
    const totale = ['456080,782', '137123,764', '4292031,42', '88088,68']
      .reduce((somma, testo) => somma + (digita(testo) ?? NaN), 0);

    expect(totale).toBe(4973324.64);
  });

  it('arrotonda per eccesso la mezza unità, come il BigDecimal del backend', () => {
    expect(digita('1,005')).toBe(1.01);
  });

  it('non altera gli importi che hanno al più i decimali dichiarati', () => {
    expect(digita('4292031,42')).toBe(4292031.42);
    expect(digita('1234,5')).toBe(1234.5);
    expect(digita('1000')).toBe(1000);
    expect(digita('1.234.567,89')).toBe(1234567.89);
  });

  it('emette null quando il campo viene svuotato', () => {
    expect(digita('')).toBeNull();
  });

  it('rispetta il numero di decimali configurato', () => {
    direttiva.amountDecimalPlaces = 4;

    expect(digita('1,234567')).toBe(1.2346);
  });
});
