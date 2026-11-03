/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { Store, Select } from '@ngxs/store';
import { Observable, Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { PrimanotaSearchState } from '../../state/primanota-search.state';
import { CheckIncomplete, ClearSearch, SearchPrimeNote, ChangePage } from '../../state/primanota-search.actions';
import { PrimaNotaSearchFilter, PrimaNotaSearchResult, PrimaNotaDetail } from '../../models/primanota.model';

// Import child components
import { PnSearchFiltersComponent } from '../pn-search-filters/pn-search-filters.component';
import { PnSearchListComponent } from '../pn-search-list/pn-search-list.component';

// Import service for incomplete warning
import { PrimanotaService } from '../../../../core/services/primanota.service';
import { NotificationService } from '../../../../core/services/notification.service';

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
  selector: 'app-pn-search',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    PnSearchFiltersComponent,
    PnSearchListComponent
  ],
  templateUrl: './pn-search.component.html',
  styleUrls: ['./pn-search.component.scss'],
  animations: searchAnimations
})
export class PrimanotaSearchComponent implements OnInit, OnDestroy {
  @Select(PrimanotaSearchState.filter) filter$!: Observable<Partial<PrimaNotaSearchFilter>>;
  @Select(PrimanotaSearchState.results) results$!: Observable<PrimaNotaSearchResult[]>;
  @Select(PrimanotaSearchState.loading) loading$!: Observable<boolean>;
  @Select(PrimanotaSearchState.totalElements) totalElements$!: Observable<number>;
  @Select(PrimanotaSearchState.currentPage) currentPage$!: Observable<number>;
  @Select(PrimanotaSearchState.pageSize) pageSize$!: Observable<number>;

  incompleteWarning$: Observable<boolean>;

  // Filtro corrente dai query parameters
  currentFilter: Partial<PrimaNotaSearchFilter> = {};
  appliedFilterParams: {label: string, value: string}[] = [];
  searchDone = false;
  filtersCollapsed = false;
  searching = false;

  private subscriptions = new Subscription();

  constructor(
    private store: Store,
    private primanotaService: PrimanotaService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.incompleteWarning$ = this.primanotaService.incompleteWarning$;
  }

  ngOnInit(): void {
    // Chiamiamo il servizio direttamente invece di dispatchare l'azione
    this.primanotaService.checkIncomplete().subscribe();

    const prevUrl = this.router.lastSuccessfulNavigation?.previousNavigation?.finalUrl?.toString() ?? '';
    const total = this.store.selectSnapshot(PrimanotaSearchState.totalElements);
    if (prevUrl.startsWith('/primanota') && total > 0) {
      this.searchDone = true;
      this.filtersCollapsed = true;
    }

    // Leggi i filtri dai query parameters all'inizializzazione
    this.subscriptions.add(
      this.route.queryParams.subscribe(params => {
        // Estrai solo i parametri che corrispondono ai campi del filtro
        const filter: Partial<PrimaNotaSearchFilter> = {};
        const validFilterKeys: (keyof PrimaNotaSearchFilter)[] = [
          'codiceTipo', 'codiceFonte', 'codiceEvento', 'codiceCausale',
          'numeroProvvisorio', 'numeroDefinitivo', 'codiceStato',
          'descrizione', 'codiceConto', 'descrizioneConto',
          'dataRegProvvisoriaDa', 'dataRegProvvisoriaA',
          'dataRegDefinitivaDa', 'dataRegDefinitivaA',
          'importoDa', 'importoA', 'soloNonQuadrate'
        ];

        validFilterKeys.forEach(key => {
          if (params[key] !== undefined && params[key] !== '') {
            let value: any = params[key];

            if (key === 'importoDa' || key === 'importoA') {
              value = parseFloat(value);
            } else if (key === 'soloNonQuadrate') {
              value = value === 'true' || value === true;
            }

            (filter as any)[key] = value;
          }
        });

        this.currentFilter = filter;
        this.appliedFilterParams = this.buildFilterParams(filter);

        // Esegue un refresh dei dati quando si torna alla pagina di search
        // dopo un inserimento o modifica (rilevato dal parametro _refresh)
        if (params['_refresh']) {
          // Esegue il refresh dopo un breve delay per assicurarsi che il componente sia pronto
          setTimeout(() => {
            if (Object.keys(filter).length > 0) {
              // Se ci sono filtri, esegui la ricerca con gli stessi filtri
              this.store.dispatch(new SearchPrimeNote(filter));
            }
          }, 100);
        }
      })
    );
  }

  toggleFilters(): void {
    this.filtersCollapsed = !this.filtersCollapsed;
  }

  private buildFilterParams(filter: Partial<PrimaNotaSearchFilter>): {label: string, value: string}[] {
    const params: {label: string, value: string}[] = [];
    if (!filter) return params;
    const labelMap: Partial<Record<keyof PrimaNotaSearchFilter, string>> = {
      codiceTipo: 'Tipo', codiceFonte: 'Fonte', codiceEvento: 'Evento',
      codiceCausale: 'Causale', numeroProvvisorio: 'N. Provvisorio',
      numeroDefinitivo: 'N. Definitivo', codiceStato: 'Stato',
      descrizione: 'Descrizione', codiceConto: 'Conto',
      dataRegProvvisoriaDa: 'Reg. Prov. Da', dataRegProvvisoriaA: 'Reg. Prov. A',
      dataRegDefinitivaDa: 'Reg. Def. Da', dataRegDefinitivaA: 'Reg. Def. A',
      importoDa: 'Importo Da', importoA: 'Importo A', soloNonQuadrate: 'Solo Non Quadrate',
    };
    (Object.keys(labelMap) as (keyof PrimaNotaSearchFilter)[]).forEach(key => {
      const value = (filter as any)[key];
      if (value === null || value === undefined || value === '' || value === false) return;
      if (key === 'codiceConto') {
        const desc = (filter as any)['descrizioneConto'];
        params.push({ label: labelMap[key]!, value: desc ? `${value} – ${desc}` : String(value) });
      } else if (key === 'soloNonQuadrate') {
        params.push({ label: labelMap[key]!, value: 'Sì' });
      } else {
        params.push({ label: labelMap[key]!, value: String(value) });
      }
    });
    return params;
  }

  onSearch(filter: Partial<PrimaNotaSearchFilter>): void {
    this.appliedFilterParams = this.buildFilterParams(filter);
    this.router.navigate(['/primanota/search'], {
      queryParams: filter,
      queryParamsHandling: 'merge',
      skipLocationChange: false
    });

    this.searching = true;
    this.filtersCollapsed = true;
    this.store.dispatch(new SearchPrimeNote(filter)).subscribe(() => {
      this.searching = false;
      const total = this.store.selectSnapshot(PrimanotaSearchState.totalElements);
      if (total > 0) {
        this.searchDone = true;
      } else {
        this.searchDone = false;
        this.filtersCollapsed = false;
        this.notificationService.showMessageByCode('WEB_INF_0003');
      }
    });
  }

  onClear(): void {
    this.router.navigate(['/primanota/search'], {
      queryParams: null,
      queryParamsHandling: 'merge'
    });

    this.store.dispatch(new ClearSearch());
    this.searching = false;
    this.searchDone = false;
    this.filtersCollapsed = false;
    this.appliedFilterParams = [];
  }

  onInserisci(): void {
    // Naviga alla pagina di inserimento nuova prima nota passando la provenienza
    this.router.navigate(['/primanota/nuovo'], {
      queryParams: { from: 'search' }
    });
  }

  onBack(): void {
    this.router.navigate(['/']);
  }

  onPageChange(event: { page: number; size: number }): void {
    this.store.dispatch(new ChangePage(event.page, event.size));
  }

  hasAppliedFilters(): boolean {
    // Usa currentFilter dai query parameters invece dello stato NGXS
    if (!this.currentFilter) return false;

    // Controlla se almeno un campo del filtro ha un valore significativo
    return Object.values(this.currentFilter).some(value =>
      value !== undefined &&
      value !== null &&
      value !== '' &&
      !(typeof value === 'object' && Object.keys(value).length === 0)
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
    // Non resettiamo più lo stato quando si lascia il componente
    // per mantenere i filtri durante la navigazione
  }
}
