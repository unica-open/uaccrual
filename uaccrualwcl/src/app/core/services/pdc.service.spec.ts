/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PdcService } from './pdc.service';
import { PdcSearchCriteria } from '../../features/pdc/model/pdc.model';

describe('PdcService - CDU Ricerca conto PdC Accrual', () => {
  let service: PdcService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ]
    });
    service = TestBed.inject(PdcService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verifica che non ci siano richieste pendenti
  });

  describe('ricercaPdc - Algoritmo Ricerca conto Accrual', () => {
    it('dovrebbe costruire correttamente i filtri per la ricerca come richiesto dal CDU', () => {
      const searchCriteria: PdcSearchCriteria = {
        codice: '1.1.1.01.17',
        descrizione: 'test'
      };

      service.ricercaPdc(searchCriteria, 0, 20, 'codice,asc');
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Verifica che il parametro filter contenga i filtri corretti secondo il CDU in formato RSQL
      const filter = req.request.params.get('filter');
      expect(filter).toContain("codiceUfficiale=si='1.1.1.01.17'");
      expect(filter).toContain("descrizione=si='test'");
      expect(req.request.params.get('offset')).toBe('0');
      expect(req.request.params.get('limit')).toBe('20');
      expect(req.request.params.get('sort')).toBe('codice,asc');
      
      req.flush({ data: [], total_elements: 0 }); // Risposta fittizia
    });

    it('dovrebbe gestire correttamente la ricerca con filtro per famiglia', () => {
      const searchCriteria: PdcSearchCriteria = {
        famiglia: 'FAM001'
      };

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // La famiglia è mappata direttamente come parametro, non come filter
      expect(req.request.params.get('famiglia')).toBe('FAM001');
      expect(req.request.params.has('filter')).toBe(false);
      
      req.flush({ data: [], total_elements: 0 });
    });

    it('dovrebbe gestire correttamente la ricerca con filtro per tipo', () => {
      const searchCriteria: PdcSearchCriteria = {
        tipo: 'AMM'
      };

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Verifica che il filtro per tipo sia costruito correttamente
      expect(req.request.params.get('filter')).toBe("pdcTipo.codice=eq='AMM'");
      
      req.flush({ data: [], total_elements: 0 });
    });

    it('dovrebbe gestire correttamente la ricerca con filtro per stato', () => {
      const searchCriteria: PdcSearchCriteria = {
        stato: 'A'
      };

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Verifica che il filtro per stato sia costruito correttamente
      expect(req.request.params.get('filter')).toBe("pdcStato.codice=eq='A'");
      
      req.flush({ data: [], total_elements: 0 });
    });

    it('dovrebbe gestire correttamente la ricerca con filtro per codice di bilancio', () => {
      const searchCriteria: PdcSearchCriteria = {
        collegamento_bil_itas: 'BIL001'
      };

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Secondo il CDU: "Se parametro 'Codice di bilancio' è valorizzato selezionare i conti con il codice collegamento con schemi di bilancio ITAS iniziante per il valore indicato"
      expect(req.request.params.get('filter')).toBe("collegamentoBilItas=si='BIL001'");
      
      req.flush({ data: [], total_elements: 0 });
    });

    it('dovrebbe gestire correttamente la ricerca con filtro per evento', () => {
      const searchCriteria: PdcSearchCriteria = {
        codiceEvento: 'EV001'
      };

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Verifica che il filtro per evento sia costruito correttamente
      expect(req.request.params.get('codiceEvento')).toBe('EV001');
      expect(req.request.params.has('filter')).toBe(false);
      
      req.flush({ data: [], total_elements: 0 });
    });

    it('dovrebbe gestire correttamente la ricerca senza filtri', () => {
      const searchCriteria: PdcSearchCriteria = {};

      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      // Nessun parametro filter quando non ci sono criteri
      expect(req.request.params.has('filter')).toBe(false);
      expect(req.request.params.get('offset')).toBe('0');
      expect(req.request.params.get('limit')).toBe('10');
      
      req.flush({ data: [], total_elements: 0 });
    });

    describe('Requisiti specifici dell\'algoritmo Ricerca conto Accrual', () => {
      it('dovrebbe selezionare i conti con codice ufficiale iniziante per il valore indicato', () => {
        const searchCriteria: PdcSearchCriteria = {
          codice: '1.1.1.01'
        };

        service.ricercaPdc(searchCriteria, 0, 10);
        
        const req = httpMock.expectOne(request => 
          request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
          request.method === 'GET'
        );

        // Verifica che il filtro per codice usi "si" (starts with) su codiceUfficiale
        expect(req.request.params.get('filter')).toBe("codiceUfficiale=si='1.1.1.01'");
        
        req.flush({ data: [], total_elements: 0 });
      });

      it('dovrebbe selezionare i conti con la descrizione iniziante per il valore indicato', () => {
        const searchCriteria: PdcSearchCriteria = {
          descrizione: 'test'
        };

        service.ricercaPdc(searchCriteria, 0, 10);
        
        const req = httpMock.expectOne(request => 
          request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
          request.method === 'GET'
        );

        // Verifica che il filtro per descrizione usi "si" (starts with)
        expect(req.request.params.get('filter')).toBe("descrizione=si='test'");
        
        req.flush({ data: [], total_elements: 0 });
      });

      it('dovrebbe selezionare i conti con codice collegamento ITAS iniziante per il valore indicato', () => {
        const searchCriteria: PdcSearchCriteria = {
          collegamento_bil_itas: 'BIL'
        };

        service.ricercaPdc(searchCriteria, 0, 10);
        
        const req = httpMock.expectOne(request => 
          request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
          request.method === 'GET'
        );

        // Verifica che il filtro per collegamento_bil_itas usi "si" (starts with)
        expect(req.request.params.get('filter')).toBe("collegamentoBilItas=si='BIL'");
        
        req.flush({ data: [], total_elements: 0 });
      });
    });
  });

  describe('Gestione errori - Variante 3.a', () => {
    it('dovrebbe gestire correttamente gli errori di rete', () => {
      const searchCriteria: PdcSearchCriteria = {
        codice: '1.1.1.01.17'
      };

      const consoleErrorSpy = spyOn(console, 'error'); // Per evitare output indesiderato negli errori
      
      service.ricercaPdc(searchCriteria, 0, 10);
      
      const req = httpMock.expectOne(request => 
        request.url.startsWith('/accrualsbsrv/api/v1/pdc') && 
        request.method === 'GET'
      );

      const mockError = new ErrorEvent('network error', { message: 'Errore di rete simulato' });
      req.error(mockError);
      
      // Verifica che l\'errore sia gestito correttamente
      // Il servizio logga due volte, verifichiamo la seconda chiamata che contiene il messaggio per l\'utente
      expect(consoleErrorSpy.calls.mostRecent().args).toEqual(['WEB_ERR_0001 - Si è verificato un errore imprevisto:', jasmine.stringMatching(/Errore di rete simulato|Http failure response/)]);
    });
  });
});
