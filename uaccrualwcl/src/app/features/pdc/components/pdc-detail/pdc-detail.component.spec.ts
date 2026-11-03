/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PdcDetailComponent } from './pdc-detail.component';
import { PdcService } from '../../../../core/services/pdc.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { Pdc } from '../../model/pdc.model';

// Mock per il servizio PdcService
const mockPdcService = {
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
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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
    pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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
  getContiCollegati: (codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string, tipoRelazione: 'FONDO' | 'CORRELAZIONE') => of([])
};

describe('PdcDetailComponent', () => {
  let component: PdcDetailComponent;
  let fixture: ComponentFixture<PdcDetailComponent>;
  let pdcService: PdcService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ PdcDetailComponent ],
      providers: [
        { provide: PdcService, useValue: mockPdcService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (param: string) => {
                  if (param === 'codiceUfficiale') return '5';
                  if (param === 'codiceClasse') return 'CLASSE1';
                  if (param === 'codiceFamiglia') return 'FAM1';
                  return null;
                }
              }
            }
          }
        }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcDetailComponent);
    component = fixture.componentInstance;
    pdcService = TestBed.inject(PdcService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load PDC data on initialization', () => {
    spyOn(pdcService, 'getPdcById').and.returnValue(of({
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
        pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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

    fixture.detectChanges();

    expect(pdcService.getPdcById).toHaveBeenCalledWith('5', 'CLASSE1', 'FAM1');
    expect(component.pdc?.descrizione).toBe('Test PDC');
  });

  it('should handle error when loading PDC data', () => {
    spyOn(pdcService, 'getPdcById').and.returnValue(throwError(() => new Error('Error')));

    fixture.detectChanges();

    expect(pdcService.getPdcById).toHaveBeenCalledWith('5', 'CLASSE1', 'FAM1');
  });

  it('should load related accounts (FONDO and CORRELAZIONE) when PDC data is loaded', () => {
    // Creiamo oggetti Pdc completi per i mock
    const mockFondo: Pdc = {
      id: 'fondo1',
      codice: 'F1',
      codice_ufficiale: 'F1',
      codice_completo: 'F1',
      descrizione: 'Fondo 1',
      livello: 1,
      foglia: true,
      modificabile: false,
      eliminabile: false,
      inseribile_figlio: false,
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1' },
      pdc_tipo: { id: 'TIPO1', codice: 'TIPO1', descrizione: 'Tipo 1' },
      pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
      pdc_famiglia: { id: 'FAM1', codice: 'FAM1', descrizione: 'Famiglia 1', modificabile: true, ricercabile: true },
      id_pdc_padre: undefined,
      partita: undefined,
      ordine: undefined,
      collegamento_bil_itas: undefined,
      fondo_dotazione: undefined,
      foglia_non_foglia: undefined,
      pdc_padre: undefined,
      ente: undefined,
      inizio_validita: undefined,
      fine_validita: undefined,
      classe: undefined,
      tipo: undefined,
      evento: undefined,
      segmento: undefined,
      stato: undefined,
      id_classe_conto: undefined,
      id_tipo_conto: undefined,
      id_stato_conto: undefined,
      flag_foglia: undefined,
      flag_fondo_dotazione: undefined,
      flag_a_partita: undefined,
      codice_bilancio: undefined,
      data_inizio_validita: undefined,
      data_fine_validita: undefined,
      version: undefined
    };

    const mockCorrelazione: Pdc = {
      id: 'corr1',
      codice: 'C1',
      codice_ufficiale: 'C1',
      codice_completo: 'C1',
      descrizione: 'Correlazione 1',
      livello: 1,
      foglia: true,
      modificabile: false,
      eliminabile: false,
      inseribile_figlio: false,
      pdc_classe: { id: 'CLASSE2', codice: 'CLASSE2', descrizione: 'Classe 2' },
      pdc_tipo: { id: 'TIPO2', codice: 'TIPO2', descrizione: 'Tipo 2' },
      pdc_stato: { id: 'A', codice: 'A', descrizione: 'Attivo' },
      pdc_famiglia: { id: 'FAM1', codice: 'FAM1', descrizione: 'Famiglia 1', modificabile: true, ricercabile: true },
      id_pdc_padre: undefined,
      partita: undefined,
      ordine: undefined,
      collegamento_bil_itas: undefined,
      fondo_dotazione: undefined,
      foglia_non_foglia: undefined,
      pdc_padre: undefined,
      ente: undefined,
      inizio_validita: undefined,
      fine_validita: undefined,
      classe: undefined,
      tipo: undefined,
      evento: undefined,
      segmento: undefined,
      stato: undefined,
      id_classe_conto: undefined,
      id_tipo_conto: undefined,
      id_stato_conto: undefined,
      flag_foglia: undefined,
      flag_fondo_dotazione: undefined,
      flag_a_partita: undefined,
      codice_bilancio: undefined,
      data_inizio_validita: undefined,
      data_fine_validita: undefined,
      version: undefined
    };

    spyOn(pdcService, 'getPdcById').and.returnValue(of({
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
        pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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
      pdc_classe: { id: 'CLASSE1', codice: 'CLASSE1', descrizione: 'Classe 1 (DARE)' },
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

    spyOn(pdcService, 'getContiCollegati').and.callFake((codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string, tipoRelazione: 'FONDO' | 'CORRELAZIONE') => {
      if (tipoRelazione === 'FONDO') {
        return of([mockFondo]);
      } else {
        return of([mockCorrelazione]);
      }
    });

    fixture.detectChanges();

    expect(pdcService.getContiCollegati).toHaveBeenCalledWith('5', 'CLASSE1', 'FAM1', 'FONDO');
    expect(pdcService.getContiCollegati).toHaveBeenCalledWith('5', 'CLASSE1', 'FAM1', 'CORRELAZIONE');
  });
});