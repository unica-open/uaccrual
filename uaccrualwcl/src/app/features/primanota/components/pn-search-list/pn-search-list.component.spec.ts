/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PnSearchListComponent } from './pn-search-list.component';

describe('PnSearchListComponent', () => {
  let component: PnSearchListComponent;
  let fixture: ComponentFixture<PnSearchListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PnSearchListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PnSearchListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
