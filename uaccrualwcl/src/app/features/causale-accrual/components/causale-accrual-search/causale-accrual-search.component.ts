/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CausaleAccrualService } from '../../../../core/services/causale-accrual.service';
import { CommonModule, NgClass } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';

// Import for NGXS
import { Store } from '@ngxs/store';
import { SearchCausali } from '../../../../core/store/causale/causale.actions';
import { CausaleState } from '../../../../core/store/causale/causale.state';
import { CausaleSearchCriteria } from '../../model/causale.model';
import { CausaleAccrualListComponent } from '../causale-accrual-list/causale-accrual-list.component';
import { NotificationService } from '../../../../core/services/notification.service';

// Validatore personalizzato per assicurare che almeno uno dei campi obbligatori sia valorizzato
function atLeastOneRequiredFieldValidator(): (control: AbstractControl) => ValidationErrors | null {
  return (control: AbstractControl): ValidationErrors | null => {
    const formGroup = control as FormGroup;
    if (!formGroup) return null;

    const tipoCausale = formGroup.get('tipoCausale');
    const eventoCausale = formGroup.get('eventoCausale');
    const codice = formGroup.get('codice');
    const descrizione = formGroup.get('descrizione');
    const stato = formGroup.get('stato');


    // Se i controlli non esistono ancora, non validare
    if (!tipoCausale || !eventoCausale || !codice || !descrizione || !stato) {
      return null;
    }

    if (!tipoCausale.value && !eventoCausale.value && !codice.value && !descrizione.value && !stato.value) {
      return { atLeastOneRequiredField: true };
    }

    return null;
  };
}

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
  selector: 'app-causale-accrual-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    CausaleAccrualListComponent,
  ],
  templateUrl: './causale-accrual-search.component.html',
  styleUrls: ['./causale-accrual-search.component.scss'],
  animations: searchAnimations
})
export class CausaleAccrualSearchComponent implements OnInit {
  searchForm: FormGroup;
  tipiCausale: any[] = [];
  eventiCausale: any[] = [];
  stati: any[] = [];
  appliedFilterParams: {label: string, value: string}[] = [];
  searchDone = false;
  filtersCollapsed = false;
  searching = false;

  constructor(
    private fb: FormBuilder,
    private causaleAccrualService: CausaleAccrualService,
    private router: Router,
    private store: Store,
    private notificationService: NotificationService
  ) {
    this.searchForm = this.fb.group({
      tipoCausale: [''],
      eventoCausale: [''],
      codice: [''],
      descrizione: [''],
      stato: ['V'], // Default a 'Valido' come da specifiche
    });
  }

  ngOnInit() {
    this.loadDataForDropdowns();
    this.searchForm.setValidators(atLeastOneRequiredFieldValidator());
    this.searchForm.updateValueAndValidity();

    const prevUrl = this.router.lastSuccessfulNavigation?.previousNavigation?.finalUrl?.toString() ?? '';
    const results = this.store.selectSnapshot(CausaleState.getSearchResults);
    if (prevUrl.startsWith('/causale') && results && results.total_elements > 0) {
      this.searchDone = true;
      this.filtersCollapsed = true;
    }
  }

  loadDataForDropdowns() {
    this.causaleAccrualService.getTipiCausale().subscribe({
      next: (data) => {
        this.tipiCausale = data
          .sort((a, b) => (a.descrizione || '').localeCompare(b.descrizione || ''))
          .map(item => ({ value: item.codice, label: `${item.descrizione}` }));
      },
      error: (error) => {
        console.error('Errore nel caricamento tipi causale:', error);
      }
    });

    this.causaleAccrualService.getEventiCausale().subscribe({
      next: (data) => {
        this.eventiCausale = data
          .sort((a, b) => (a.codice || '').localeCompare(b.codice || ''))
          .map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
      },
      error: (error) => {
        console.error('Errore nel caricamento eventi causale:', error);
      }
    });

    this.causaleAccrualService.getStati().subscribe({
      next: (data) => {
        this.stati = data
          .map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
      },
      error: (error) => {
        console.error('Errore nel caricamento stati:', error);
      }
    });
  }

  private buildFilterParams(): {label: string, value: string}[] {
    const params: {label: string, value: string}[] = [];
    const form = this.searchForm.value;
    if (form.tipoCausale) {
      const found = this.tipiCausale.find((t: any) => t.value === form.tipoCausale);
      params.push({ label: 'Tipo', value: found ? found.label : form.tipoCausale });
    }
    if (form.eventoCausale) {
      const found = this.eventiCausale.find((e: any) => e.value === form.eventoCausale);
      params.push({ label: 'Evento', value: found ? found.label : form.eventoCausale });
    }
    if (form.codice) params.push({ label: 'Codice', value: form.codice });
    if (form.descrizione) params.push({ label: 'Descrizione', value: form.descrizione });
    if (form.stato) {
      const found = this.stati.find((s: any) => s.value === form.stato);
      params.push({ label: 'Stato', value: found ? found.label : form.stato });
    }
    return params;
  }

  toggleFilters(): void {
    this.filtersCollapsed = !this.filtersCollapsed;
  }

  onSubmit() {
    this.searchForm.markAllAsTouched();

    if (this.searchForm.valid) {
      const filter = this.searchForm.value as CausaleSearchCriteria;
      this.searching = true;
      this.filtersCollapsed = true;
      this.store.dispatch(new SearchCausali(filter)).subscribe(() => {
        this.searching = false;
        const results = this.store.selectSnapshot(CausaleState.getSearchResults);
        if (results && results.total_elements > 0) {
          this.searchDone = true;
          this.appliedFilterParams = this.buildFilterParams();
        } else {
          this.searchDone = false;
          this.filtersCollapsed = false;
          this.appliedFilterParams = [];
          this.notificationService.info('Nessun dato trovato.', 'WEB_INF_0003');
        }
      });
    } else {
      if (this.searchForm.errors?.['atLeastOneRequiredField']) {
        this.notificationService.error('Almeno un campo di ricerca deve essere valorizzato.', 'WEB_ERR_0002');
      }
    }
  }

  onReset() {
    this.searchForm.reset({
      tipoCausale: '',
      eventoCausale: '',
      codice: '',
      descrizione: '',
      stato: 'V'
    });

    this.searching = false;
    this.searchDone = false;
    this.filtersCollapsed = false;
    this.appliedFilterParams = [];
  }

  onBack() {
    this.router.navigate(['/']);
  }
}
