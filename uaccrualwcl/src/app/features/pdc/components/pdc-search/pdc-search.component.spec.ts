/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { PdcSearchComponent } from './pdc-search.component';
import { PdcService } from '../../../../core/services/pdc.service';

describe('PdcSearchComponent', () => {
  let component: PdcSearchComponent;
  let fixture: ComponentFixture<PdcSearchComponent>;
  let mockPdcService: jasmine.SpyObj<PdcService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const pdcServiceSpy = jasmine.createSpyObj('PdcService', [
      'ricercaPdc',
      'getClassi',
      'getTipi',
      'getEventi',
      'getStati',
      'getFamiglie'
    ]);
    
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        PdcSearchComponent // Import standalone component
      ],
      providers: [
        { provide: PdcService, useValue: pdcServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    mockPdcService = TestBed.inject(PdcService) as jasmine.SpyObj<PdcService>;
    mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Mock return values for dropdowns
    mockPdcService.getFamiglie.and.returnValue(of([]));
    mockPdcService.getClassi.and.returnValue(of([]));
    mockPdcService.getTipi.and.returnValue(of([]));
    mockPdcService.getEventi.and.returnValue(of([]));
    mockPdcService.getStati.and.returnValue(of([]));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PdcSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('dovrebbe creare il componente e inizializzare il form', () => {
    expect(component).toBeTruthy();
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.valid).toBe(false); // Inizialmente non valido per il validatore di gruppo
  });

  it('dovrebbe chiamare i servizi per caricare i dati dei dropdown on init', () => {
    expect(mockPdcService.getFamiglie).toHaveBeenCalled();
    expect(mockPdcService.getClassi).toHaveBeenCalled();
    expect(mockPdcService.getTipi).toHaveBeenCalled();
    expect(mockPdcService.getEventi).toHaveBeenCalled();
    expect(mockPdcService.getStati).toHaveBeenCalled();
  });

  describe('Validazione del Form', () => {
    it('dovrebbe essere invalido se nè "classe" nè "codice" sono valorizzati', () => {
      component.searchForm.patchValue({ classe: '', codice: '' });
      fixture.detectChanges();
      expect(component.searchForm.invalid).toBe(true);
      expect(component.searchForm.hasError('atLeastOneRequiredField')).toBe(true);
    });

    it('dovrebbe essere valido se "codice" è valorizzato', () => {
      component.searchForm.patchValue({ codice: '123' });
      fixture.detectChanges();
      expect(component.searchForm.valid).toBe(true);
    });

    it('dovrebbe essere valido se "classe" è valorizzata', () => {
      component.searchForm.patchValue({ classe: 'CL1' });
      fixture.detectChanges();
      expect(component.searchForm.valid).toBe(true);
    });
  });

  describe('onSubmit', () => {
    it('non dovrebbe chiamare ricercaPdc e non dovrebbe navigare se il form è invalido', () => {
      component.searchForm.patchValue({ classe: '', codice: '' });
      fixture.detectChanges();

      component.onSubmit();

      expect(mockPdcService.ricercaPdc).not.toHaveBeenCalled();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.erroriValidazione).toContain('WEB_ERR_0002 - Dato obbligatorio non valorizzato: Classe e Codice');
    });

    it('dovrebbe chiamare ricercaPdc e navigare a /pdc/results se il form è valido', () => {
      const searchCriteria = { classe: 'CL1', codice: '', descrizione: '', collegamento_bil_itas: '', codiceEvento: '', stato: '', tipo: '' };
      component.searchForm.patchValue(searchCriteria);
      fixture.detectChanges();

      component.onSubmit();

      expect(mockPdcService.ricercaPdc).toHaveBeenCalledWith(component.searchForm.value);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/pdc/results']);
      expect(component.erroriValidazione.length).toBe(0);
    });
  });

  describe('onReset', () => {
    it('dovrebbe resettare il form e pulire gli errori di validazione', () => {
      component.searchForm.patchValue({ codice: '123' });
      component.erroriValidazione.push('un errore');
      fixture.detectChanges();

      component.onReset();

      expect(component.searchForm.value).toEqual({
        classe: null,
        tipo: null,
        codiceEvento: null,
        codice: null,
        descrizione: null,
        stato: null,
        collegamento_bil_itas: null
      });
      expect(component.erroriValidazione.length).toBe(0);
    });
  });
});