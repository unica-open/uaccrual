/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PnDetailViewComponent } from './pn-detail-view.component';

describe('PnDetailViewComponent', () => {
  let component: PnDetailViewComponent;
  let fixture: ComponentFixture<PnDetailViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PnDetailViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PnDetailViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
