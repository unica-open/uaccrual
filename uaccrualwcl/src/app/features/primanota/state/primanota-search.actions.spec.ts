/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import {
  SearchPrimeNote,
  ChangePage,
  GetPrimaNotaDetail,
  CheckIncomplete,
  CheckIncompleteSuccess,
  ClearSearch,
  SearchFailed,
  AnnullaPrimaNota,
  AnnullaPrimaNotaSuccess,
  AnnullaPrimaNotaFailed,
  RiapriPrimaNota,
  RiapriPrimaNotaSuccess,
  RiapriPrimaNotaFailed
} from './primanota-search.actions';

describe('Primanota Search Actions', () => {
  describe('SearchPrimeNote', () => {
    it('should create the action with filter', () => {
      const filter = {
        codiceStato: 'A',
        descrizione: 'Test'
      };

      const action = new SearchPrimeNote(filter);

      expect(action).toBeDefined();
      expect(action.filter).toEqual(filter);
    });

    it('should create the action with empty filter', () => {
      const action = new SearchPrimeNote({});

      expect(action).toBeDefined();
      expect(action.filter).toEqual({});
    });
  });

  describe('ChangePage', () => {
    it('should create the action with page and size', () => {
      const action = new ChangePage(2, 50);

      expect(action.page).toBe(2);
      expect(action.size).toBe(50);
    });
  });

  describe('GetPrimaNotaDetail', () => {
    it('should create the action with anno and numero', () => {
      const action = new GetPrimaNotaDetail(2025, 123);

      expect(action.anno).toBe(2025);
      expect(action.numero).toBe(123);
    });
  });

  describe('CheckIncomplete', () => {
    it('should create the action', () => {
      const action = new CheckIncomplete();
      expect(action).toBeDefined();
    });
  });

  describe('CheckIncompleteSuccess', () => {
    it('should create the action with hasIncomplete', () => {
      const action = new CheckIncompleteSuccess(true);

      expect(action.hasIncomplete).toBe(true);
    });
  });

  describe('ClearSearch', () => {
    it('should create the action', () => {
      const action = new ClearSearch();
      expect(action).toBeDefined();
    });
  });

  describe('SearchFailed', () => {
    it('should create the action with error', () => {
      const error = { message: 'Search failed' };
      const action = new SearchFailed(error);

      expect(action.error).toEqual(error);
    });
  });

  describe('AnnullaPrimaNota', () => {
    it('should create the action with anno and numero', () => {
      const action = new AnnullaPrimaNota(2025, 123);

      expect(action.anno).toBe(2025);
      expect(action.numero).toBe(123);
    });
  });

  describe('AnnullaPrimaNotaSuccess', () => {
    it('should create the action with messaggio', () => {
      const messaggio = 'Prima nota annullata con successo';
      const action = new AnnullaPrimaNotaSuccess(messaggio);

      expect(action.messaggio).toBe(messaggio);
    });
  });

  describe('AnnullaPrimaNotaFailed', () => {
    it('should create the action with error', () => {
      const error = { message: 'Annullamento fallito' };
      const action = new AnnullaPrimaNotaFailed(error);

      expect(action.error).toEqual(error);
    });
  });

  describe('RiapriPrimaNota', () => {
    it('should create the action with anno and numero', () => {
      const action = new RiapriPrimaNota(2025, 123);

      expect(action.anno).toBe(2025);
      expect(action.numero).toBe(123);
    });
  });

  describe('RiapriPrimaNotaSuccess', () => {
    it('should create the action with messaggio', () => {
      const messaggio = 'Prima nota riaperta con successo';
      const action = new RiapriPrimaNotaSuccess(messaggio);

      expect(action.messaggio).toBe(messaggio);
    });
  });

  describe('RiapriPrimaNotaFailed', () => {
    it('should create the action with error', () => {
      const error = { message: 'Riapertura fallita' };
      const action = new RiapriPrimaNotaFailed(error);

      expect(action.error).toEqual(error);
    });
  });
});
