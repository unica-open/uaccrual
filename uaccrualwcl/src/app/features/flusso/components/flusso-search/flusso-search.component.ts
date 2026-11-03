/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { FlussoSearchFilter, Fonte, TipoFlusso, TipoFile } from '../../model/flusso.model';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FlussoService } from '../../../../core/services/flusso.service';
import { Store } from '@ngxs/store';
import { ContextState } from '../../../../core/store/context/context.state';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AsyncPipe } from '@angular/common';
import { FlussoSearchListComponent } from '../flusso-search-list/flusso-search-list.component';

const searchAnimations = [
  trigger('collapseExpand', [
    transition(':enter', [
      style({ height: '0px', opacity: 0, overflow: 'hidden' }),
      animate('300ms ease-out', style({ height: '*', opacity: 1 }))
    ]),
    transition(':leave', [
      style({ overflow: 'hidden' }),
      animate('250ms ease-in', style({ height: '0px', opacity: 0 }))
    ])
  ]),
  trigger('fadeSlideIn', [
    transition(':enter', [
      style({ opacity: 0, transform: 'translateY(-10px)' }),
      animate('350ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
    ]),
    transition(':leave', [
      animate('200ms ease-in', style({ opacity: 0 }))
    ])
  ])
];

@Component({
  selector: 'app-flusso-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AsyncPipe, FlussoSearchListComponent],
  templateUrl: './flusso-search.component.html',
  styleUrls: ['./flusso-search.component.scss'],
  animations: searchAnimations
})
export class FlussoSearchComponent implements OnInit {
  searchForm: FormGroup;
  fonti$: Observable<Fonte[]>;
  tipiFile$: Observable<TipoFile[]>;
  appliedFilterParams: {label: string, value: string}[] = [];
  searchDone = false;
  filtersCollapsed = false;
  currentFilters: FlussoSearchFilter | null = null;
  annoBilancio$: Observable<number | null>;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    public flussoService: FlussoService,
    private store: Store
  ) {
    this.annoBilancio$ = this.store.select(ContextState.getAnnoBilancio);
    this.searchForm = this.fb.group({
      codiceFile: [''],
      fonte: [''],
      tipoFile: [''],
      dataCaricamento: ['']
    });
    
    this.fonti$ = of([]);
    this.tipiFile$ = of([]);
  }

  ngOnInit(): void {
    this.loadDropdowns();

    const prevUrl = this.router.lastSuccessfulNavigation?.previousNavigation?.finalUrl?.toString() ?? '';
    const saved = this.flussoService.lastFiltersValue;
    if (prevUrl.startsWith('/flusso') && saved !== null) {
      this.searchForm.patchValue(saved);
      this.currentFilters = saved;
      this.searchDone = true;
      this.filtersCollapsed = true;
      this.appliedFilterParams = this.buildFilterParams(saved);
    } else {
      this.flussoService.saveFilters(null);
    }

    this.searchForm.get('fonte')?.valueChanges.subscribe(selectedFonte => {
      console.log('Selected Fonte:', selectedFonte);
    });
  }

  private loadDropdowns(): void {
    this.fonti$ = this.flussoService.getFonti().pipe(
      tap(fonti => console.log('Fonti caricate:', fonti))
    );
    this.tipiFile$ = this.flussoService.getTipiFile().pipe(
      tap(tipi => console.log('Tipi file caricati:', tipi))
    );
  }

  private buildFilterParams(filters: FlussoSearchFilter): {label: string, value: string}[] {
    const params: {label: string, value: string}[] = [];
    if (filters.codiceFile) params.push({ label: 'Codice File', value: filters.codiceFile });
    if (filters.dataCaricamento) params.push({ label: 'Data Caricamento', value: String(filters.dataCaricamento) });
    if (filters.fonte) params.push({ label: 'Fonte', value: filters.fonte });
    if (filters.tipoFile) params.push({ label: 'Tipo File', value: filters.tipoFile });
    return params;
  }

  toggleFilters(): void {
    this.filtersCollapsed = !this.filtersCollapsed;
  }

  cercaFlusso(): void {
    if (this.searchForm.valid) {
      const filters: FlussoSearchFilter = this.searchForm.value;
      this.currentFilters = { ...filters };
      this.flussoService.saveFilters(this.currentFilters);
      this.appliedFilterParams = this.buildFilterParams(this.currentFilters);
      this.filtersCollapsed = true;
      this.searchDone = true;
    }
  }

  annullaRicerca(): void {
    this.searchForm.reset({
      codiceFile: '',
      fonte: '',
      tipoFile: '',
      dataCaricamento: ''
    });
    this.flussoService.saveFilters(null);
    this.searchDone = false;
    this.filtersCollapsed = false;
    this.currentFilters = null;
    this.appliedFilterParams = [];
  }

  indietro(): void {
    this.router.navigate(['/']);
    console.log('Naviga indietro');
  }
}
