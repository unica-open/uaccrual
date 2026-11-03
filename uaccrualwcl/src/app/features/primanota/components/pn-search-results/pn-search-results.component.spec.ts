/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PnSearchResultsComponent } from './pn-search-results.component';

describe('PnSearchResultsComponent', () => {
  let component: PnSearchResultsComponent;
  let fixture: ComponentFixture<PnSearchResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PnSearchResultsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PnSearchResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
