/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// AI generated 2026-09-14
// reviewed by simonep

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { PdcState } from './pdc.state';
import { DeletePdc } from './pdc.actions';
import { PdcService } from '../../services/pdc.service';
import { NotificationService } from '../../services/notification.service';

/**
 * Regressione task-1186: la cancellazione di un conto PdC partiva dal solo codice e
 * ricostruiva la tripletta con una ricerca su POST /pdc/search, che però pretende anche
 * pdc_classe_codice e risponde 400. La DELETE non veniva mai emessa e l'utente vedeva
 * "WEB_ERR_0001 - Si è verificato un errore durante la cancellazione".
 */
describe('PdcState - cancellazione conto (task-1186)', () => {
  let store: Store;
  let pdcServiceSpy: jasmine.SpyObj<PdcService>;

  const CODICE_UFFICIALE = '1.2.3.10.2.5.999';
  const CODICE_CLASSE = 'AP';
  const CODICE_FAMIGLIA = 'ACCRUAL-UTENTE';

  beforeEach(() => {
    pdcServiceSpy = jasmine.createSpyObj('PdcService',
      ['deletePdc', 'ricercaPdc'],
      { pdcListValue: [], totalElementsValue: 0 }
    );
    pdcServiceSpy.ricercaPdc.and.returnValue(of({ data: [], page: 0, page_size: 10, total_elements: 0 } as any));

    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([PdcState])],
      providers: [
        { provide: PdcService, useValue: pdcServiceSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: NotificationService, useValue: jasmine.createSpyObj('NotificationService', ['showMessageByCode', 'error', 'success', 'warning', 'info']) }
      ]
    });

    store = TestBed.inject(Store);
  });

  it('emette la DELETE con la tripletta ricevuta nell\'azione', () => {
    pdcServiceSpy.deletePdc.and.returnValue(of(undefined));

    store.dispatch(new DeletePdc(CODICE_UFFICIALE, CODICE_CLASSE, CODICE_FAMIGLIA));

    expect(pdcServiceSpy.deletePdc).toHaveBeenCalledWith(CODICE_UFFICIALE, CODICE_CLASSE, CODICE_FAMIGLIA);
  });

  it('non esegue alcuna ricerca per ricostruire la tripletta', () => {
    // Se la cancellazione fallisce non deve restare traccia di ricerche: quella di
    // ricostruzione non esiste più, e quella di aggiornamento risultati parte solo in caso di successo.
    pdcServiceSpy.deletePdc.and.returnValue(throwError(() => new Error('errore di rete')));

    store.dispatch(new DeletePdc(CODICE_UFFICIALE, CODICE_CLASSE, CODICE_FAMIGLIA));

    expect(pdcServiceSpy.deletePdc).toHaveBeenCalled();
    expect(pdcServiceSpy.ricercaPdc).not.toHaveBeenCalled();
  });

  it('dopo la cancellazione riesegue la ricerca e azzera il loading', () => {
    pdcServiceSpy.deletePdc.and.returnValue(of(undefined));

    store.dispatch(new DeletePdc(CODICE_UFFICIALE, CODICE_CLASSE, CODICE_FAMIGLIA));

    expect(pdcServiceSpy.ricercaPdc).toHaveBeenCalled();
    expect(store.selectSnapshot(PdcState.getMessage)?.type).toBe('success');
  });
});
