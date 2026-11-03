/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { PdcService } from '../../../../core/services/pdc.service';
import { CommonModule, NgClass } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { NotificationService } from '../../../../core/services/notification.service';

// Import per NGXS
import { Store } from '@ngxs/store';
import { ClearMessage, SearchPdcConti } from '../../../../core/store/pdc/pdc.actions';
import { PdcListComponent } from '../pdc-list/pdc-list.component';

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
  selector: 'app-pdc-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    NgClass,
    PdcListComponent,
  ],
  templateUrl: './pdc-search.component.html',
  styleUrls: ['./pdc-search.component.scss'],
  animations: searchAnimations
})
export class PdcSearchComponent implements OnInit, OnDestroy {
  searchForm: FormGroup;
  classi: any[] = [];
  famiglie: any[] = [];
  tipi: any[] = [];
  eventi: any[] = [];
  stati: any[] = [];
  erroriValidazione: string[] = [];
  appliedFilterParams: {label: string, value: string}[] = [];
  searchDone = false;
  filtersCollapsed = false;
  searching = false;

  constructor(
    private fb: FormBuilder,
    private pdcService: PdcService, // Mantenuto per caricare i dropdown
    private router: Router,
    private route: ActivatedRoute,
    private store: Store,
    private cdr: ChangeDetectorRef,
    private notificationService: NotificationService
  ) {
    this.searchForm = this.fb.group({
      classe: ['', Validators.required],
      tipo: ['', []],
      codiceEvento: ['', []],
      codice: ['', Validators.required],
      descrizione: ['', []],
      stato: ['', []],
      collegamento_bil_itas: ['', []]
    });
  }

  private buildFilterParams(): {label: string, value: string}[] {
    const params: {label: string, value: string}[] = [];
    const form = this.searchForm.value;
    if (form.classe) {
      const found = this.classi.find((c: any) => c.value === form.classe);
      params.push({ label: 'Classe', value: found ? found.label : form.classe });
    }
    if (form.tipo) {
      const found = this.tipi.find((t: any) => t.value === form.tipo);
      params.push({ label: 'Tipo', value: found ? found.label : form.tipo });
    }
    if (form.codiceEvento) {
      const found = this.eventi.find((e: any) => e.value === form.codiceEvento);
      params.push({ label: 'Evento', value: found ? found.label : form.codiceEvento });
    }
    if (form.codice) params.push({ label: 'Codice', value: form.codice });
    if (form.descrizione) params.push({ label: 'Descrizione', value: form.descrizione });
    if (form.stato) {
      const found = this.stati.find((s: any) => s.value === form.stato);
      params.push({ label: 'Stato', value: found ? found.label : form.stato });
    }
    if (form.collegamento_bil_itas) params.push({ label: 'Codice ITAS1', value: form.collegamento_bil_itas });
    return params;
  }

  toggleFilters(): void {
    this.filtersCollapsed = !this.filtersCollapsed;
  }

  ngOnDestroy() {
    this.store.dispatch(new ClearMessage());
    this.pdcService.noResultsSubject$.next(false);
  }

  ngOnInit() {
    this.loadDataForDropdowns();
    const prevUrl = this.router.lastSuccessfulNavigation?.previousNavigation?.finalUrl?.toString() ?? '';
    if (prevUrl.startsWith('/pdc') && this.pdcService.totalElementsValue > 0) {
      this.searchDone = true;
      this.filtersCollapsed = true;
    }
    // CORREZIONE BUG #7: Rimosso validatore personalizzato, usati solo Validators.required nei singoli campi

    // Prima gestiamo i parametri della query URL
    this.route.queryParams.subscribe(params => {
      if (Object.keys(params).length > 0) {
        this.searchForm.patchValue(params);
        // Aggiorniamo anche i filtri nel servizio con i parametri URL
        this.pdcService.updateFilters(params);
      }
    });

    // Poi recuperiamo i filtri dal servizio e aggiorniamo il form
    // Questo sovrascriverà eventuali valori provenienti dai parametri URL
    // con i filtri effettivi memorizzati nel servizio
    this.pdcService.filters$.subscribe(filters => {
      console.log('Ricevuti filtri dal servizio in pdc-search:', filters);
      if (filters && Object.keys(filters).length > 0) {
        console.log('Applico i filtri al form:', filters);
        this.searchForm.patchValue(filters);
        if (this.searchDone) {
          this.appliedFilterParams = this.buildFilterParams();
        }
      } else {
        console.log('Nessun filtro valido ricevuto dal servizio');
      }
    });
  }

  loadDataForDropdowns() {
    this.pdcService.getFamiglie().subscribe(data => {
      this.famiglie = data.map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
    });
    this.pdcService.getClassi().subscribe(data => {
      this.classi = data.map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
    });
    this.pdcService.getTipi().subscribe(data => {
      this.tipi = data.map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
    });
    this.pdcService.getEventi().subscribe(data => {
      this.eventi = data.map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
    });
    this.pdcService.getStati().subscribe(data => {
      this.stati = data.map(item => ({ value: item.codice, label: `${item.codice} - ${item.descrizione}` }));
    });
  }

  onSubmit() {
    this.erroriValidazione = [];
    this.searchForm.markAllAsTouched();

    // CORREZIONE BUG #7: Validazione separata per classe e codice
    if (this.searchForm.get('classe')?.invalid && this.searchForm.get('classe')?.hasError('required')) {
      this.erroriValidazione.push('WEB_ERR_0002 - Dato obbligatorio non valorizzato: Classe');
    }
    if (this.searchForm.get('codice')?.invalid && this.searchForm.get('codice')?.hasError('required')) {
      this.erroriValidazione.push('WEB_ERR_0002 - Dato obbligatorio non valorizzato: Codice');
    }

    if (this.erroriValidazione.length > 0) {
      return; // Non procedere con la ricerca se ci sono errori di validazione
    }

    if (this.searchForm.valid) {
      this.erroriValidazione = [];
      this.searching = true;
      this.filtersCollapsed = true;
      this.store.dispatch(new SearchPdcConti(this.searchForm.value)).subscribe(() => {
        this.searching = false;
        if (this.pdcService.totalElementsValue > 0) {
          this.searchDone = true;
          this.appliedFilterParams = this.buildFilterParams();
        } else {
          this.searchDone = false;
          this.filtersCollapsed = false;
          this.appliedFilterParams = [];
          this.notificationService.info('Nessun dato trovato.', 'WEB_INF_0003');
        }
        this.cdr.detectChanges();
      });
    }
  }

  onReset() {
    this.searchForm.reset({
      classe: '',
      tipo: '',
      codiceEvento: '',
      stato: '',
      codice: '',
      descrizione: '',
      collegamento_bil_itas: ''
    });
    this.erroriValidazione = [];
    this.searching = false;
    this.searchDone = false;
    this.filtersCollapsed = false;
    this.appliedFilterParams = [];
    this.pdcService.noResultsSubject$.next(false);
  }

  onBack() {
    this.router.navigate(['/']);
  }
}