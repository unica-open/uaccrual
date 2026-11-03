/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdcEsempioCreateComponent } from './pdc-esempio-create.component';

describe('PdcEsempioCreateComponent', () => {
  let component: PdcEsempioCreateComponent;
  let fixture: ComponentFixture<PdcEsempioCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PdcEsempioCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcEsempioCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
