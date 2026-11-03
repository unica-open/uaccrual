/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdcTreeSearchModalComponent } from './pdc-tree-search-modal.component';

describe('PdcTreeSearchModalComponent', () => {
  let component: PdcTreeSearchModalComponent;
  let fixture: ComponentFixture<PdcTreeSearchModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PdcTreeSearchModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcTreeSearchModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
