/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PnSearchFiltersComponent } from './pn-search-filters.component';

describe('PnSearchFiltersComponent', () => {
  let component: PnSearchFiltersComponent;
  let fixture: ComponentFixture<PnSearchFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PnSearchFiltersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PnSearchFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
