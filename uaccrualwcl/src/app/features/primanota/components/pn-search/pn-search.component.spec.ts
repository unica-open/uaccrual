/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrimanotaSearchComponent } from './pn-search.component';

describe('PrimanotaSearchComponent', () => {
  let component: PrimanotaSearchComponent;
  let fixture: ComponentFixture<PrimanotaSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrimanotaSearchComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrimanotaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
