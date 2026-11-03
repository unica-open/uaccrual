/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdcEsempioDetailComponent } from './pdc-esempio-detail.component';

describe('PdcEsempioDetailComponent', () => {
  let component: PdcEsempioDetailComponent;
  let fixture: ComponentFixture<PdcEsempioDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PdcEsempioDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcEsempioDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
