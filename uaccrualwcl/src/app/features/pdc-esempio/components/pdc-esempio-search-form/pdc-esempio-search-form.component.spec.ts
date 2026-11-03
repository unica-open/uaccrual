/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdcEsempioSearchFormComponent } from './pdc-esempio-search-form.component';

describe('PdcEsempioSearchFormComponent', () => {
  let component: PdcEsempioSearchFormComponent;
  let fixture: ComponentFixture<PdcEsempioSearchFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PdcEsempioSearchFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcEsempioSearchFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
