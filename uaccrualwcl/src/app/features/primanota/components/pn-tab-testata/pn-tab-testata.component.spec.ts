/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { PnTabTestataComponent } from './pn-tab-testata.component';
import { PrimanotaService } from '../../../../core/services/primanota.service';
import { of, throwError } from 'rxjs';
import { FormBuilder } from '@angular/forms';

describe('PnTabTestataComponent', () => {
  let component: PnTabTestataComponent;
  let fixture: ComponentFixture<PnTabTestataComponent>;
  let primanotaServiceSpy: jasmine.SpyObj<PrimanotaService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  const mockTipi = [
    { value: 'MANUALE', label: 'Prima Nota Manuale' },
    { value: 'AUTOMATICA', label: 'Prima Nota Automatica' }
  ];

  const mockFonti = [
    { value: 'INVENTARIO', label: 'Inventario' },
    { value: 'ACQUISTI', label: 'Acquisti' }
  ];

  const mockStati = [
    { value: 'P', label: 'P-PROVVISORIA' },
    { value: 'D', label: 'D-DEFINITIVA' }
  ];

  beforeEach(async () => {
    const primanotaServiceMock = jasmine.createSpyObj('PrimanotaService', [
      'getTipiPrimaNota',
      'getFonti',
      'getStatiPrimaNota',
      'getEventiContabiliByFonte',
      'getCausaliByEvento'
    ]);

    const snackBarMock = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        PnTabTestataComponent,
        ReactiveFormsModule,
        FormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: PrimanotaService, useValue: primanotaServiceMock },
        { provide: MatSnackBar, useValue: snackBarMock },
        FormBuilder
      ]
    }).compileComponents();

    primanotaServiceSpy = TestBed.inject(PrimanotaService) as jasmine.SpyObj<PrimanotaService>;
    snackBarSpy = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Setup default spy behavior
    primanotaServiceSpy.getTipiPrimaNota.and.returnValue(of(mockTipi));
    primanotaServiceSpy.getFonti.and.returnValue(of(mockFonti));
    primanotaServiceSpy.getStatiPrimaNota.and.returnValue(of(mockStati));
    primanotaServiceSpy.getEventiContabiliByFonte.and.returnValue(of([]));
    primanotaServiceSpy.getCausaliByEvento.and.returnValue(of([]));

    fixture = TestBed.createComponent(PnTabTestataComponent);
    component = fixture.componentInstance;

    // Crea un form group di test
    const fb = TestBed.inject(FormBuilder);
    component.testataForm = fb.group({
      tipo: [''],
      fonte: [''],
      evento: [''],
      causale: [''],
      stato: [''],
      dataRegProvvisoria: [''],
      descrizione: ['']
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load dropdowns on init', () => {
    expect(primanotaServiceSpy.getTipiPrimaNota).toHaveBeenCalled();
    expect(primanotaServiceSpy.getFonti).toHaveBeenCalled();
    expect(primanotaServiceSpy.getStatiPrimaNota).toHaveBeenCalled();
    expect(component.tipi.length).toBe(2);
    expect(component.fonti.length).toBe(2);
    expect(component.stati.length).toBe(2);
  });

  it('should set default tipo on non-modifica mode', () => {
    component.isModifica = false;
    component.ngOnInit();
    fixture.detectChanges();

    expect(component.testataForm.get('tipo')?.value).toBe('MANUALE');
  });

  it('should set default stato on non-modifica mode', () => {
    component.isModifica = false;
    component.ngOnInit();
    fixture.detectChanges();

    expect(component.testataForm.get('stato')?.value).toBe('P');
  });

  it('should filter out ANNULLATA stato', () => {
    const statiConAnnullata = [
      { value: 'P', label: 'P-PROVVISORIA' },
      { value: 'D', label: 'D-DEFINITIVA' },
      { value: 'A', label: 'A-ANNULLATA' }
    ];
    primanotaServiceSpy.getStatiPrimaNota.and.returnValue(of(statiConAnnullata));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.stati.length).toBe(2);
    expect(component.stati.find(s => s.value === 'A')).toBeUndefined();
  });

  it('should call onFonteChange and load eventi', () => {
    const mockEventi = [
      { value: 'EV001', label: 'Evento 1' }
    ];
    primanotaServiceSpy.getEventiContabiliByFonte.and.returnValue(of(mockEventi));

    component.onFonteChange('INVENTARIO');
    fixture.detectChanges();

    expect(primanotaServiceSpy.getEventiContabiliByFonte).toHaveBeenCalledWith('INVENTARIO');
    expect(component.eventi.length).toBe(1);
    expect(component.causali.length).toBe(0);
    expect(component.testataForm.get('evento')?.value).toBeNull();
    expect(component.testataForm.get('causale')?.value).toBeNull();
  });

  it('should reset eventi and causali when fonte is empty', () => {
    component.eventi = [{ value: 'EV001', label: 'Evento 1' }];
    component.causali = [{ value: 'CAU001', label: 'Causale 1' }];

    component.onFonteChange('');
    fixture.detectChanges();

    expect(component.eventi.length).toBe(0);
    expect(component.causali.length).toBe(0);
  });

  it('should call onEventoChange and load causali', () => {
    const mockCausali = [
      { value: 'CAU001', label: 'Causale 1' }
    ];
    primanotaServiceSpy.getCausaliByEvento.and.returnValue(of(mockCausali));

    component.onEventoChange('EV001');
    fixture.detectChanges();

    expect(primanotaServiceSpy.getCausaliByEvento).toHaveBeenCalledWith('EV001', 'V');
    expect(component.causali.length).toBe(1);
    expect(component.testataForm.get('causale')?.value).toBeNull();
  });

  // AI generated 2026-09-16
  // reviewed by tarantinod
  it('should not filter causali by stato in modifica', () => {
    primanotaServiceSpy.getCausaliByEvento.and.returnValue(of([]));
    component.isModifica = true;

    component.onEventoChange('EV001');

    expect(primanotaServiceSpy.getCausaliByEvento).toHaveBeenCalledWith('EV001', undefined);
  });

  it('should reset causali when evento is empty', () => {
    component.causali = [{ value: 'CAU001', label: 'Causale 1' }];

    component.onEventoChange('');
    fixture.detectChanges();

    expect(component.causali.length).toBe(0);
  });

  it('should set statoEditabile to false when stato is P (PROVVISORIA)', () => {
    component.verificaStatoEditabile('P');
    expect(component.statoEditabile).toBeFalse();
  });

  it('should set statoEditabile to true when stato is D (DEFINITIVA)', () => {
    component.verificaStatoEditabile('D');
    expect(component.statoEditabile).toBeTrue();
  });

  it('should emit prosegui event when form is valid', () => {
    spyOn(component.prosegui, 'emit');
    component.testataForm.patchValue({
      tipo: 'MANUALE',
      fonte: 'INVENTARIO',
      evento: 'EV001',
      causale: 'CAU001',
      stato: 'P',
      dataRegProvvisoria: '2025-01-01',
      descrizione: 'Test'
    });

    component.onProsegui();

    expect(component.prosegui.emit).toHaveBeenCalled();
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  });

  it('should show error when form is invalid and prosegui is clicked', () => {
    spyOn(component.prosegui, 'emit');
    component.testataForm.patchValue({
      tipo: '',
      fonte: '',
      evento: '',
      causale: '',
      stato: '',
      dataRegProvvisoria: '',
      descrizione: ''
    });

    component.onProsegui();

    expect(component.prosegui.emit).not.toHaveBeenCalled();
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Compilare tutti i campi obbligatori della testata',
      'Chiudi',
      jasmine.objectContaining({ duration: 5000 })
    );
  });

  it('should emit annulla event', () => {
    spyOn(component.annulla, 'emit');
    component.onAnnulla();
    expect(component.annulla.emit).toHaveBeenCalled();
  });

  it('should emit indietro event', () => {
    spyOn(component.indietro, 'emit');
    component.onIndietro();
    expect(component.indietro.emit).toHaveBeenCalled();
  });

  it('should return true for isFieldInvalid when field is invalid and touched', () => {
    component.testataForm.get('tipo')?.setErrors({ required: true });
    component.testataForm.get('tipo')?.markAsTouched();

    expect(component.isFieldInvalid('tipo')).toBeTrue();
  });

  it('should return false for isFieldInvalid when field is valid', () => {
    component.testataForm.get('tipo')?.setErrors(null);

    expect(component.isFieldInvalid('tipo')).toBeFalse();
  });

  it('should return field errors for required field', () => {
    component.testataForm.get('tipo')?.setErrors({ required: true });

    const errors = component.getFieldErrors('tipo');
    expect(errors).toContain('Campo obbligatorio');
  });

  it('should return field errors for maxlength field', () => {
    component.testataForm.get('descrizione')?.setErrors({ maxlength: { requiredLength: 500 } });

    const errors = component.getFieldErrors('descrizione');
    expect(errors).toContain('Massimo 500 caratteri');
  });

  it('should handle error when loading tipi fails', () => {
    primanotaServiceSpy.getTipiPrimaNota.and.returnValue(throwError(() => new Error('Error')));

    component.ngOnInit();
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Errore nel caricamento tipi',
      'Chiudi',
      jasmine.objectContaining({ duration: 5000 })
    );
  });

  it('should handle error when loading fonti fails', () => {
    primanotaServiceSpy.getFonti.and.returnValue(throwError(() => new Error('Error')));

    component.ngOnInit();
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Errore nel caricamento fonti',
      'Chiudi',
      jasmine.objectContaining({ duration: 5000 })
    );
  });

  it('should handle error when loading stati fails', () => {
    primanotaServiceSpy.getStatiPrimaNota.and.returnValue(throwError(() => new Error('Error')));

    component.ngOnInit();
    fixture.detectChanges();

    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Errore nel caricamento stati',
      'Chiudi',
      jasmine.objectContaining({ duration: 5000 })
    );
  });
});
