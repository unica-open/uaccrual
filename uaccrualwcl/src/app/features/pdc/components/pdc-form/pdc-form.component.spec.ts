/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PdcFormComponent } from './pdc-form.component';
import { PdcService } from '../../../../core/services/pdc.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatDialog } from '@angular/material/dialog';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { Store } from '@ngxs/store';

// Mock per il servizio PdcService
const mockPdcService = {
  getClassi: () => of([
    { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' },
    { id: 'CLASSE2', codice: 'CLASSE2', descrizione: 'Classe 2 (AVERE)', segno: 'A' }
  ]),
  getTipi: () => of([
    { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
    { id: 'TIPO2', codice: 'TIPO2', descrizione: 'Tipo 2' }
  ]),
  getStati: () => of([
    { id: 'A', codice: 'A', descrizione: 'Attivo' },
    { id: 'NA', codice: 'NA', descrizione: 'Non Attivo' }
  ]),
  getPdcById: (codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string) => of({
    id: '1',
    codice: '5',
    codice_ufficiale: '5',
    codice_completo: '1.2.3.5',
    descrizione: 'Test PDC',
    id_classe: 'CLASSE1',
    id_tipo: 'TIPO1',
    id_stato: 'A',
    foglia: false,
    pdc_padre: {
      id: 'parent1',
      codice: '1.2.3',
      codice_ufficiale: '1.2.3',
      codice_completo: '1.2.3',
      descrizione: 'PDC Padre',
      id_classe: 'CLASSE1',
      id_tipo: 'TIPO1',
      id_stato: 'A',
      foglia: false,
      pdc_padre: undefined,
      pdc_famiglia: undefined,
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' },
      pdc_tipo: { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
      pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
      ente: undefined,
      livello: 1,
      modificabile: true,
      eliminabile: true,
      inseribile_figlio: true,
      data_inizio_validita: '01/01/2023',
      data_fine_validita: '',
      version: undefined
    },
    pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' },
    pdc_tipo: { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
    pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
    data_inizio_validita: '01/01/2023',
    data_fine_validita: '',
    version: undefined,
    livello: 1,
    modificabile: true,
    eliminabile: true,
    inseribile_figlio: true
  }),
  getPdcByCodicePdc: (codicePdc: string) => of({
    id: '1',
    codice: '1.2.3',
    codice_ufficiale: '1.2.3',
    codice_completo: '1.2.3',
    descrizione: 'PDC Padre',
    foglia: false,
    pdc_padre: undefined,
    pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' },
    pdc_tipo: { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
    pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
    data_inizio_validita: '01/01/2023',
    data_fine_validita: '',
    version: undefined,
    livello: 1,
    modificabile: true,
    eliminabile: true,
    inseribile_figlio: true
  }),
  insertPdc: (pdc: any) => of(pdc),
  updatePdc: (codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string, pdc: any) => of(pdc),
  ricercaPdc: (filters: any, page: number, pageSize: number) => of({ data: [], page: 0, page_size: 10, total_elements: 0 }),
  deletePdc: (codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string) => of({})
};

// Mock per il servizio NotificationService
const mockNotificationService = {
  showMessageByCode: jasmine.createSpy('showMessageByCode')
};

// Mock per MatDialog
const mockMatDialog = {
  open: jasmine.createSpy('open').and.returnValue({
    afterClosed: () => of(true)
  })
};

// Mock per Store
const mockStore = {
  dispatch: jasmine.createSpy('dispatch'),
  select: jasmine.createSpy('select'),
  selectSnapshot: jasmine.createSpy('selectSnapshot').and.returnValue(2025)
};

describe('PdcFormComponent', () => {
  let component: PdcFormComponent;
  let fixture: ComponentFixture<PdcFormComponent>;
  let pdcService: PdcService;
  let notificationService: NotificationService;
  let matDialog: MatDialog;
  let store: Store;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ ReactiveFormsModule, FormsModule, RouterTestingModule ],
      declarations: [ PdcFormComponent ],
      providers: [
        FormBuilder,
        { provide: PdcService, useValue: mockPdcService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: Store, useValue: mockStore },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: { mode: 'insert' },
              params: {
                codiceUfficialePadre: '1.2.3',
                codiceClassePadre: 'CLASSE1',
                codiceFamigliaPadre: 'FAM1',
                codiceUfficiale: '5',
                codiceClasse: 'CLASSE1',
                codiceFamiglia: 'FAM1'
              }
            }
          }
        }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcFormComponent);
    component = fixture.componentInstance;
    pdcService = TestBed.inject(PdcService);
    notificationService = TestBed.inject(NotificationService);
    matDialog = TestBed.inject(MatDialog);
    store = TestBed.inject(Store);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    fixture.detectChanges();
    expect(component.form.contains('codice')).toBe(true);
    expect(component.form.contains('descrizione')).toBe(true);
    expect(component.form.contains('id_stato_conto')).toBe(true);
    expect(component.form.contains('id_classe_conto')).toBe(true);
    expect(component.form.contains('id_tipo_conto')).toBe(true);
    expect(component.form.contains('data_inizio_validita')).toBe(true);

    // Verifica che i validatori siano impostati correttamente
    const codiceControl = component.form.get('codice');
    expect(codiceControl?.hasValidator(Validators.required)).toBe(true);

    const descrizioneControl = component.form.get('descrizione');
    expect(descrizioneControl?.hasValidator(Validators.required)).toBe(true);

    const statoControl = component.form.get('id_stato_conto');
    expect(statoControl?.hasValidator(Validators.required)).toBe(true);
  });

  it('should load parent data when in insert mode', () => {
    spyOn(pdcService, 'getPdcById').and.returnValue(of({
      id: '1',
      codice: '1.2.3',
      codice_ufficiale: '1.2.3',
      codice_completo: '1.2.3',
      descrizione: 'PDC Padre',
      foglia: false,
      pdc_padre: undefined,
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' },
      pdc_tipo: { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
      pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
      data_inizio_validita: '01/01/2023',
      data_fine_validita: '',
      version: undefined,
      livello: 1,
      modificabile: true,
      eliminabile: true,
      inseribile_figlio: true
    }));

    spyOn(pdcService, 'getClassi').and.returnValue(of([
      { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)', segno: 'D' }
    ]));
    spyOn(pdcService, 'getTipi').and.returnValue(of([
      { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' }
    ]));
    spyOn(pdcService, 'getStati').and.returnValue(of([
      { id: 'A', codice: 'A', descrizione: 'Attivo' }
    ]));

    fixture.detectChanges();

    expect(pdcService.getPdcById).toHaveBeenCalledWith('1.2.3', 'CLASSE1', 'FAM1');
    expect(component.form.get('codicePadre')?.value).toBe('1.2.3');
  });

  it('should validate date range correctly', () => {
    fixture.detectChanges();

    const startDateControl = component.form.get('data_inizio_validita');
    const endDateControl = component.form.get('data_fine_validita');

    startDateControl?.setValue('2023-01-01');
    endDateControl?.setValue('2022-01-01'); // Data fine precedente alla data inizio

    component.form.updateValueAndValidity();

    expect(component.form.hasError('endDateBeforeStartDate')).toBe(true);
  });

  it('should validate date format correctly', () => {
    fixture.detectChanges();

    const dateControl = component.form.get('data_inizio_validita');
    dateControl?.setValue('invalid-date');

    expect(dateControl?.errors?.['invalidDateFormat']).toBeTrue();
  });

  it('should set isUpdateMode correctly based on route data', () => {
    // Test in modalità inserimento
    expect(component.isUpdateMode).toBe(false);

    // Test in modalità aggiornamento
    const activatedRoute = TestBed.inject(ActivatedRoute);
    Object.defineProperty(activatedRoute.snapshot, 'data', { value: { mode: 'update' } });

    const updatedComponent = new PdcFormComponent(
      TestBed.inject(FormBuilder),
      activatedRoute,
      TestBed.inject(Router),
      TestBed.inject(PdcService),
      TestBed.inject(NotificationService),
      TestBed.inject(MatDialog),
      TestBed.inject(Store)
    );

    expect(updatedComponent.isUpdateMode).toBe(true);
  });
});
