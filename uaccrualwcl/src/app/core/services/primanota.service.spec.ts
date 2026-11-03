/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';

import { PrimanotaService } from './primanota.service';

describe('PrimanotaService', () => {
  let service: PrimanotaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrimanotaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
