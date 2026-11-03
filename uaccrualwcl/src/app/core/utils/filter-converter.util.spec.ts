/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { buildFilterFromCriteria } from '../utils/filter-converter.util';

describe('FilterConverterUtil - RSQL Generation', () => {
  describe('buildFilterFromCriteria', () => {
    
    it('dovrebbe creare un filtro RSQL per collegamento_bil_itas', () => {
      const filters = { collegamento_bil_itas: 'BIL001' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("collegamentoBilItas=si='BIL001'");
    });

    it('dovrebbe creare un filtro RSQL per codice', () => {
      const filters = { codice: '1.1.1' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("codiceUfficiale=si='1.1.1'");
    });

    it('dovrebbe creare un filtro RSQL per descrizione', () => {
      const filters = { descrizione: 'test' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("descrizione=si='test'");
    });

    it('dovrebbe creare un filtro RSQL per classe', () => {
      const filters = { classe: 'AP' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("pdcClasse.codice=eq='AP'");
    });

    it('dovrebbe creare un filtro RSQL per tipo', () => {
      const filters = { tipo: 'AMM' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("pdcTipo.codice=eq='AMM'");
    });

    it('dovrebbe creare un filtro RSQL per stato', () => {
      const filters = { stato: 'A' };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("pdcStato.codice=eq='A'");
    });

    it('dovrebbe combinare filtri multipli con ;', () => {
      const filters = { 
        codice: '1.1',
        descrizione: 'test',
        stato: 'A'
      };
      const result = buildFilterFromCriteria(filters);
      // L'ordine non è garantito, quindi verifichiamo la presenza di ogni parte
      expect(result).toContain("codiceUfficiale=si='1.1'");
      expect(result).toContain("descrizione=si='test'");
      expect(result).toContain("pdcStato.codice=eq='A'");
      expect(result.split(';').length).toBe(3);
    });

    it('dovrebbe ignorare valori null, undefined o stringhe vuote', () => {
      const filters = { 
        codice: '1.1.1',
        descrizione: null,
        stato: undefined,
        tipo: ''
      };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("codiceUfficiale=si='1.1.1'");
    });

    it('dovrebbe restituire una stringa vuota per filtri vuoti', () => {
      const filters = {};
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe('');
    });

    it('dovrebbe ignorare i campi "famiglia" e "codiceEvento"', () => {
      const filters = { 
        codice: '1.2.3',
        famiglia: 'FAM1', 
        codiceEvento: 'EV01' 
      };
      const result = buildFilterFromCriteria(filters);
      expect(result).toBe("codiceUfficiale=si='1.2.3'");
    });

  });
});
