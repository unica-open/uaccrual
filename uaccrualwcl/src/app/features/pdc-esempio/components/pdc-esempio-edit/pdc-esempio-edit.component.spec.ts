/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdcEsempioEditComponent } from './pdc-esempio-edit.component';

describe('PdcEsempioEditComponent', () => {
  let component: PdcEsempioEditComponent;
  let fixture: ComponentFixture<PdcEsempioEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PdcEsempioEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcEsempioEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
