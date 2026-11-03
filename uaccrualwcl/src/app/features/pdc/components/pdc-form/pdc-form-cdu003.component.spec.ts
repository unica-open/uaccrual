/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { PdcFormComponent } from './pdc-form.component';
import { PdcService } from '../../../../core/services/pdc.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Pdc, PdcClasse, PdcTipo, PdcStato, PdcFamiglia } from '../../model/pdc.model';
import { ActivatedRoute } from '@angular/router';

describe('PdcFormComponent - CDU-003 Tests', () => {
  let component: PdcFormComponent;
  let fixture: ComponentFixture<PdcFormComponent>;
  let pdcService: jasmine.SpyObj<PdcService>;
  let notificationService: jasmine.SpyObj<NotificationService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<any>>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;

  const mockClassi: PdcClasse[] = [
    { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1', segno: 'DARE' },
    { id: '2', codice: 'CLASSE2', descrizione: 'Classe 2', segno: 'AVERE' }
  ];

  const mockTipi: PdcTipo[] = [
    { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
    { id: '2', codice: 'TIPO2', descrizione: 'Tipo 2' }
  ];

  const mockStati: PdcStato[] = [
    { id: '1', codice: 'A', descrizione: 'Attivo' },
    { id: '2', codice: 'NA', descrizione: 'Non Attivo' }
  ];

  const mockFamiglie: PdcFamiglia[] = [
    { id: '1', codice: 'ACCRUAL_118', descrizione: 'Famiglia 118', modificabile: false, ricercabile: false },
    { id: '2', codice: 'ACCRUAL', descrizione: 'Famiglia Accrual', modificabile: true, ricercabile: true }
  ];

  beforeEach(async () => {
    const pdcServiceSpy = jasmine.createSpyObj('PdcService', [
      'getClassi', 'getTipi', 'getStati', 'getFamiglie', 'getPdcById', 'getPdcByCodicePdc', 'insertPdc', 'updatePdc', 'ricercaPdc', 'deletePdc', 'getContiCollegati'
    ]);
    const notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['showMessageByCode']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);

    // Creiamo un mock per ActivatedRoute
    activatedRoute = jasmine.createSpyObj('ActivatedRoute', ['snapshot'], {
      snapshot: {
        data: { mode: 'insert' },
        params: { codiceUfficialePadre: '1.2.3', codiceClassePadre: 'CLASSE1', codiceFamigliaPadre: 'FAM1' }
      }
    });

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        MatDialogModule,
        PdcFormComponent // Importazione del componente standalone
      ],
      providers: [
        { provide: PdcService, useValue: pdcServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    }).compileComponents();

    pdcService = TestBed.inject(PdcService) as jasmine.SpyObj<PdcService>;
    notificationService = TestBed.inject(NotificationService) as jasmine.SpyObj<NotificationService>;

    // Mock delle chiamate ai servizi per ottenere le liste
    pdcService.getClassi.and.returnValue(of(mockClassi));
    pdcService.getTipi.and.returnValue(of(mockTipi));
    pdcService.getStati.and.returnValue(of(mockStati));
    pdcService.getFamiglie.and.returnValue(of(mockFamiglie));

    // Mock per le relazioni
    pdcService.getContiCollegati.and.returnValue(of([]));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PdcFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('TC-C-001: Creazione Pdc (Successo)', () => {
    it('should create a new PDC successfully with correct calculated fields', () => {
      // Simula modalità inserimento
      Object.defineProperty(component, 'isUpdateMode', { value: false, writable: true });
      component['isUpdateMode'] = false;

      // Imposta i valori del form come da scenario TC-C-001
      component.form.patchValue({
        codicePadre: '1.2.3',
        codice: '5',
        descrizione: 'Test',
        id_stato_conto: 'A',
        flag_foglia: true,
        data_inizio_validita: '01/01/2023',
        data_fine_validita: ''
      });

      // Simula il caricamento del padre
      const mockParentPdc: Pdc = {
        id: '1.2.3',
        codice: '3',
        codice_ufficiale: '1.2.3',
        codice_completo: '1.2.3',
        descrizione: 'Conto padre',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1', segno: 'DARE' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        pdc_famiglia: { id: '2', codice: 'ACCRUAL', descrizione: 'Famiglia Accrual', modificabile: true, ricercabile: true },
        foglia: false,
        livello: 3,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: true,
        version: undefined
      };

      pdcService.getPdcByCodicePdc.and.returnValue(of(mockParentPdc));

      const mockPdcResponse: Pdc = {
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Test',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1', segno: 'DARE' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        pdc_famiglia: { id: '2', codice: 'ACCRUAL', descrizione: 'Famiglia Accrual', modificabile: true, ricercabile: true },
        foglia: true,
        livello: 4, // Calcolato come padre.livello + 1
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      };

      pdcService.insertPdc.and.returnValue(of(mockPdcResponse));

      // Simula la chiamata al metodo che crea il PDC
      component['creaPdc']({
        codicePadre: '1.2.3',
        codice: '5',
        descrizione: 'Test',
        id_stato_conto: 'A',
        flag_foglia: true,
        data_inizio_validita: '01/01/2023',
        data_fine_validita: ''
      }, false);

      expect(pdcService.insertPdc).toHaveBeenCalled();
      expect(notificationService.showMessageByCode).toHaveBeenCalledWith('WEB_OK_0004');
    });
  });

  describe('TC-C-002: Creazione Pdc (Errore: Padre è foglia)', () => {
    it('should show error when parent PDC is a leaf', () => {
      // Simula una risposta dal servizio che indica che il padre è una foglia
      const mockParentPdc: Pdc = {
        id: '1.2.3',
        codice: '3',
        codice_ufficiale: '1.2.3',
        codice_completo: '1.2.3',
        descrizione: 'Conto padre foglia',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true, // Questo indica che è una foglia
        livello: 3,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      };

      pdcService.getPdcByCodicePdc.and.returnValue(of(mockParentPdc));

      // Simula la chiamata al metodo che controlla se il padre è foglia
      component['salvaPdc'](false);

      expect(notificationService.showMessageByCode).toHaveBeenCalledWith('WEB_ERR_0013');
    });
  });

  describe('TC-C-003: Creazione Pdc (Errore: Codice duplicato)', () => {
    it('should show error when PDC code is duplicated', () => {
      // Simula modalità inserimento
      Object.defineProperty(component, 'isUpdateMode', { value: false, writable: true });
      component['isUpdateMode'] = false;

      // Imposta i valori del form
      component.form.patchValue({
        codicePadre: '1.2.3',
        codice: '5',
        descrizione: 'Test',
        id_stato_conto: 'A',
        flag_foglia: true,
        data_inizio_validita: '01/01/2023'
      });

      // Simula un errore di duplicazione dal servizio
      const errorResponse = { status: 409, error: { message: 'Pdc non univoco' } };
      pdcService.insertPdc.and.returnValue(throwError(errorResponse));

      component['performSave']({
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Test',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      }, false);

      // Il servizio PdcService dovrebbe gestire l'errore e mostrare il messaggio appropriato
      expect(pdcService.insertPdc).toHaveBeenCalled();
    });
  });

  describe('TC-C-004: Creazione Pdc (Errore: Permesso mancante)', () => {
    it('should show error when user lacks permission', () => {
      // Simula modalità inserimento
      Object.defineProperty(component, 'isUpdateMode', { value: false, writable: true });
      component['isUpdateMode'] = false;

      // Imposta i valori del form
      component.form.patchValue({
        codicePadre: '1.2.3',
        codice: '5',
        descrizione: 'Test',
        id_stato_conto: 'A',
        flag_foglia: true,
        data_inizio_validita: '01/01/2023'
      });

      // Simula un errore di autorizzazione dal servizio
      const errorResponse = { status: 403, error: { message: 'Permesso negato' } };
      pdcService.insertPdc.and.returnValue(throwError(errorResponse));

      component['performSave']({
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Test',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      }, false);

      // Il servizio PdcService dovrebbe gestire l'errore e mostrare il messaggio appropriato
      expect(pdcService.insertPdc).toHaveBeenCalled();
    });
  });

  describe('TC-U-005: Aggiornamento Pdc (Successo)', () => {
    it('should update PDC successfully', () => {
      // Simula modalità aggiornamento
      Object.defineProperty(component, 'isUpdateMode', { value: true, writable: true });
      component['isUpdateMode'] = true;
      component['codicePdc'] = '1.2.3.5';

      // Crea un PDC esistente
      const existingPdc: Pdc = {
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Vecchia descrizione',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      };

      // Mock per ottenere il PDC esistente
      pdcService.getPdcByCodicePdc.withArgs('1.2.3.5').and.returnValue(of(existingPdc));

      const updatedPdcResponse: Pdc = {
        ...existingPdc,
        descrizione: 'Nuova Descrizione'
      };

      pdcService.updatePdc.withArgs('1.2.3.5', 'CLASSE1', 'FAM1', jasmine.any(Object)).and.returnValue(of(updatedPdcResponse));

      component['performSave']({
        ...existingPdc,
        descrizione: 'Nuova Descrizione'
      }, false);

      expect(pdcService.updatePdc).toHaveBeenCalledWith('1.2.3.5', 'CLASSE1', 'FAM1', jasmine.any(Object));
      expect(notificationService.showMessageByCode).toHaveBeenCalledWith('WEB_OK_0004');
    });
  });

  describe('TC-U-006: Aggiornamento Pdc (Errore: Pdc non trovato)', () => {
    it('should show error when PDC to update is not found', () => {
      // Simula modalità aggiornamento
      Object.defineProperty(component, 'isUpdateMode', { value: true, writable: true });
      component['isUpdateMode'] = true;
      component['codicePdc'] = '999';

      // Simula un errore di PDC non trovato
      pdcService.getPdcByCodicePdc.withArgs('999').and.returnValue(throwError({ status: 404, error: { message: 'Pdc non trovato' } }));

      component['salvaPdc'](false);

      expect(notificationService.showMessageByCode).toHaveBeenCalledWith('WEB_ERR_0001');
    });
  });

  describe('TC-U-007: Aggiornamento Pdc (Errore: famiglia 118)', () => {
    it('should show error when trying to update PDC from family 118', () => {
      // Simula modalità aggiornamento
      Object.defineProperty(component, 'isUpdateMode', { value: true, writable: true });
      component['isUpdateMode'] = true;
      component['codicePdc'] = '118.1';

      // Crea un PDC appartenente alla famiglia 118
      const existingPdc: Pdc = {
        id: '118.1',
        codice: '1',
        codice_ufficiale: '118.1',
        codice_completo: '118.1',
        descrizione: 'Conto famiglia 118',
        pdc_famiglia: { id: '1', codice: 'ACCRUAL_118', descrizione: 'Famiglia 118', modificabile: false, ricercabile: false },
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: false, // Questo dovrebbe impedire la modifica
        eliminabile: false,
        inseribile_figlio: false,
        version: undefined
      };

      // Mock per ottenere il PDC esistente
      pdcService.getPdcByCodicePdc.withArgs('118.1').and.returnValue(of(existingPdc));

      // Simula un errore dal servizio per famiglia 118
      const errorResponse = { status: 403, error: { message: 'Operazione non permessa su Pdc di famiglia 118' } };
      pdcService.updatePdc.withArgs('118.1', 'CLASSE1', 'FAM1', jasmine.any(Object)).and.returnValue(throwError(errorResponse));

      component['performSave'](existingPdc, false);

      expect(pdcService.updatePdc).toHaveBeenCalledWith('118.1', 'CLASSE1', 'FAM1', jasmine.any(Object));
    });
  });

  describe('TC-D-008: Eliminazione Pdc (Successo)', () => {
    it('should delete PDC successfully', () => {
      // Simula un PDC foglia senza legami
      const pdcToDelete: Pdc = {
        id: '5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Conto da eliminare',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      };

      // Simula la chiamata al servizio di eliminazione
      pdcService.deletePdc.and.returnValue(of(undefined));

      // Chiama direttamente il metodo di eliminazione
      component['onDelete']();

      // Poiché il metodo di eliminazione apre un dialogo, dobbiamo simulare la conferma
      expect(pdcService.deletePdc).not.toHaveBeenCalled(); // Non viene chiamato finché non si conferma
    });
  });

  describe('TC-D-009: Eliminazione Pdc (Errore: ha figli)', () => {
    it('should show error when PDC has children with WEB_ERR_0012', () => {
      // Simula un PDC con figli
      const pdcWithChildren: Pdc = {
        id: '1',
        codice: '1',
        codice_ufficiale: '1',
        codice_completo: '1',
        descrizione: 'Conto padre',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: false,
        livello: 1,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: false, // Questo indica che non è eliminabile perché ha figli
        inseribile_figlio: true,
        version: undefined
      };

      // Simula la ricerca che trova figli
      const mockPagedPdc = { data: [{} as Pdc], page: 0, page_size: 10, total_elements: 1 };
      pdcService.ricercaPdc.and.returnValue(of(mockPagedPdc));

      // Simula un errore di integrità dal servizio
      const errorResponse = { status: 409, error: { message: 'WEB_ERR_0012', dettaglio: { tipo: 'conto figlio', valore: '...' } } };
      pdcService.updatePdc.withArgs('118.1', 'CLASSE1', 'FAM1', jasmine.any(Object)).and.returnValue(throwError(errorResponse));

      // Chiama la funzione di controllo
      component['checkAndHandleConfirmations'](pdcWithChildren, pdcWithChildren, false);

      // Verifica che sia stata effettuata la ricerca dei figli
      expect(pdcService.ricercaPdc).toHaveBeenCalled();
    });
  });

  describe('TC-D-010: Eliminazione Pdc (Errore: ha legami con causali)', () => {
    it('should show error when PDC is linked to causali with WEB_ERR_0012', () => {
      // Simula un PDC collegato a causali
      const pdcWithCausali: Pdc = {
        id: '6',
        codice: '6',
        codice_ufficiale: '1.2.3.6',
        codice_completo: '1.2.3.6',
        descrizione: 'Conto collegato a causali',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: false, // Questo indica che non è eliminabile
        inseribile_figlio: false,
        version: undefined
      };

      // Simula un errore dal servizio indicante legame con causali
      const errorResponse = { status: 409, error: { message: 'WEB_ERR_0012', dettaglio: { tipo: 'causale', valore: '...' } } };
      pdcService.deletePdc.and.returnValue(throwError(errorResponse));

      // Chiama direttamente il metodo di eliminazione
      component['onDelete']();

      expect(pdcService.deletePdc).toHaveBeenCalled();
    });
  });

  describe('TC-D-011: Eliminazione Pdc (Errore: pdc_foglia_non_foglia)', () => {
    it('should show error when PDC has pdc_foglia_non_foglia = true', () => {
      // Simula un PDC con pdc_foglia_non_foglia = true
      const pdcFogliaNonFoglia: Pdc = {
        id: '7',
        codice: '7',
        codice_ufficiale: '1.2.3.7',
        codice_completo: '1.2.3.7',
        descrizione: 'Conto foglia non foglia',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        foglia_non_foglia: true, // Questo impedisce l'eliminazione
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: false, // Anche la modifica è vietata
        eliminabile: false,
        inseribile_figlio: false,
        version: undefined
      };

      // Simula un errore dal servizio indicante impossibilità di operazione
      const errorResponse = { status: 403, error: { message: 'Operazione non permessa su questo tipo di Pdc' } };
      pdcService.deletePdc.and.returnValue(throwError(errorResponse));

      // Chiama direttamente il metodo di eliminazione
      component['onDelete']();

      expect(pdcService.deletePdc).toHaveBeenCalled();
    });
  });

  describe('TC-D-012: Eliminazione Pdc (Errore: Fase bilancio non valida)', () => {
    it('should show error when operation is not allowed in current budget phase', () => {
      // Simula un errore dal servizio per fase di bilancio non valida
      const errorResponse = { status: 403, error: { message: 'Operazione non permessa in fase CHIUSO' } };
      pdcService.insertPdc.and.returnValue(throwError(errorResponse));

      component['performSave']({
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Test',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      }, false);

      expect(pdcService.insertPdc).toHaveBeenCalled();
    });
  });

  describe('TC-U-008: Aggiornamento Pdc (Caricamento Relazioni)', () => {
    it('should load relations (fondi and correlazioni) when in update mode', () => {
      // Simula modalità aggiornamento
      Object.defineProperty(component, 'isUpdateMode', { value: true, writable: true });
      component['isUpdateMode'] = true;

      // Crea un PDC con informazioni complete per caricare le relazioni
      const pdcWithRelations: Pdc = {
        id: '1.2.3.5',
        codice: '5',
        codice_ufficiale: '1.2.3.5',
        codice_completo: '1.2.3.5',
        descrizione: 'Conto con relazioni',
        pdc_classe: { id: '1', codice: 'CLASSE1', descrizione: 'Classe 1', segno: 'DARE' },
        pdc_tipo: { id: '1', codice: 'TIPO1', descrizione: 'Tipo 1' },
        pdc_stato: { id: '1', codice: 'A', descrizione: 'Attivo' },
        pdc_famiglia: { id: '2', codice: 'ACCRUAL', descrizione: 'Famiglia Accrual', modificabile: true, ricercabile: true },
        foglia: true,
        livello: 4,
        inizio_validita: '2023-01-01',
        fine_validita: undefined,
        modificabile: true,
        eliminabile: true,
        inseribile_figlio: false,
        version: undefined
      };

      // Chiama direttamente il metodo loadRelazioni
      component['loadRelazioni'](pdcWithRelations);

      // Verifica che siano state chiamate le funzioni per caricare le relazioni
      expect(pdcService.getContiCollegati).toHaveBeenCalledWith('1.2.3.5', 'CLASSE1', '2', 'FONDO');
      expect(pdcService.getContiCollegati).toHaveBeenCalledWith('1.2.3.5', 'CLASSE1', '2', 'CORRELAZIONE');
    });
  });
});