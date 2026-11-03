/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

export interface BreadcrumbItem {
  label: string;
  route: string;
  category?: string;
}

interface RouteConfig {
  label: string;
  category: string;
  parentRoute: string;
}

const CAT_FLUSSI   = 'ACQUISIZIONE FLUSSI DI ALIMENTAZIONE';
const CAT_PN       = 'PRIME NOTE';
const CAT_REPORT   = 'REPORT';
const CAT_ANAG     = 'ANAGRAFICHE';

const ROUTE_MAP: { pattern: RegExp; config: RouteConfig }[] = [
  // FLUSSI
  { pattern: /^\/flusso\/upload$/,          config: { label: 'Acquisizione flusso',       category: CAT_FLUSSI,  parentRoute: '/flusso/upload' } },
  { pattern: /^\/flusso\/search$/,          config: { label: 'Ricerca flusso',             category: CAT_FLUSSI,  parentRoute: '/flusso/search' } },
  { pattern: /^\/flusso\/results$/,         config: { label: 'Risultati ricerca',          category: CAT_FLUSSI,  parentRoute: '/flusso/search' } },
  { pattern: /^\/flusso\/detail\//,         config: { label: 'Dettaglio flusso',           category: CAT_FLUSSI,  parentRoute: '/flusso/search' } },

  // PRIME NOTE
  { pattern: /^\/primanota\/search$/,       config: { label: 'Ricerca Prima Nota',         category: CAT_PN,      parentRoute: '/primanota/search' } },
  { pattern: /^\/primanota\/nuovo$/,        config: { label: 'Inserimento Prima Nota',     category: CAT_PN,      parentRoute: '/primanota/search' } },
  { pattern: /^\/primanota\/results$/,      config: { label: 'Risultati ricerca',          category: CAT_PN,      parentRoute: '/primanota/search' } },
  { pattern: /^\/primanota\/dettaglio\//,   config: { label: 'Dettaglio Prima Nota',       category: CAT_PN,      parentRoute: '/primanota/search' } },
  { pattern: /^\/primanota\/modifica\//,    config: { label: 'Modifica Prima Nota',        category: CAT_PN,      parentRoute: '/primanota/search' } },

  // REPORT
  { pattern: /^\/report$/,                  config: { label: 'Report',                     category: CAT_REPORT,  parentRoute: '/report' } },
  { pattern: /^\/report\/form$/,            config: { label: 'Genera Report',              category: CAT_REPORT,  parentRoute: '/report' } },
  { pattern: /^\/report\/search$/,          config: { label: 'Ricerca Report',             category: CAT_REPORT,  parentRoute: '/report/search' } },
  { pattern: /^\/report\/search\/results$/, config: { label: 'Risultati ricerca',          category: CAT_REPORT,  parentRoute: '/report/search' } },
  { pattern: /^\/report\/search\//,         config: { label: 'Dettaglio Report',           category: CAT_REPORT,  parentRoute: '/report/search' } },
  { pattern: /^\/modellomif\/upload$/,      config: { label: 'Modello di raccordo',        category: CAT_REPORT,  parentRoute: '/modellomif/upload' } },

  // ANAGRAFICHE
  { pattern: /^\/pdc\/search$/,             config: { label: 'Conto Accrual',              category: CAT_ANAG,    parentRoute: '/pdc/search' } },
  { pattern: /^\/pdc\/results$/,            config: { label: 'Risultati ricerca',          category: CAT_ANAG,    parentRoute: '/pdc/results' } },
  { pattern: /^\/pdc\/dettaglio\//,         config: { label: 'Dettaglio Conto',            category: CAT_ANAG,    parentRoute: '/pdc/search' } },
  { pattern: /^\/pdc\/new\//,               config: { label: 'Nuovo Conto',                category: CAT_ANAG,    parentRoute: '/pdc/search' } },
  { pattern: /^\/pdc\/edit\//,              config: { label: 'Modifica Conto',             category: CAT_ANAG,    parentRoute: '/pdc/search' } },
  { pattern: /^\/causale\/search$/,         config: { label: 'Causale',                    category: CAT_ANAG,    parentRoute: '/causale/search' } },
  { pattern: /^\/causale\/results$/,        config: { label: 'Risultati ricerca',          category: CAT_ANAG,    parentRoute: '/causale/results' } },
  { pattern: /^\/causale\/dettaglio\//,     config: { label: 'Dettaglio Causale',          category: CAT_ANAG,    parentRoute: '/causale/search' } },
  { pattern: /^\/causale\/inserisci$/,      config: { label: 'Inserisci Causale',          category: CAT_ANAG,    parentRoute: '/causale/search' } },
  { pattern: /^\/causale\/modifica\//,      config: { label: 'Modifica Causale',           category: CAT_ANAG,    parentRoute: '/causale/search' } },
  { pattern: /^\/causale\/[^/]+$/,          config: { label: 'Dettaglio Causale',          category: CAT_ANAG,    parentRoute: '/causale/search' } },
  { pattern: /^\/pdc\/[^/]+\/[^/]+\/[^/]+$/, config: { label: 'Dettaglio Conto',          category: CAT_ANAG,    parentRoute: '/pdc/search' } },
];

const HOME_CRUMB: BreadcrumbItem = { label: 'Home', route: '/' };

@Injectable({ providedIn: 'root' })
export class BreadcrumbService {

  private stackSubject = new BehaviorSubject<BreadcrumbItem[]>([HOME_CRUMB]);
  readonly breadcrumbs$: Observable<BreadcrumbItem[]> = this.stackSubject.asObservable();

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe((e: any) => {
      this.push((e.urlAfterRedirects || e.url) as string);
    });
  }

  private push(url: string): void {
    const path = url.split('?')[0];
    const isHome = path === '/' || path === '/home' || path === '';

    if (isHome) {
      this.stackSubject.next([HOME_CRUMB]);
      return;
    }

    const match = ROUTE_MAP.find(r => r.pattern.test(path));
    if (!match) return;

    const { label, category, parentRoute } = match.config;
    const newItem: BreadcrumbItem = { label, route: parentRoute, category };
    const stack = this.stackSubject.getValue();

    // If the exact item (same label + route) already exists in the stack,
    // the user is navigating back — truncate to that position.
    const existingIndex = stack.findIndex(c => c.label === label && c.route === parentRoute);
    if (existingIndex !== -1) {
      this.stackSubject.next(stack.slice(0, existingIndex + 1));
      return;
    }

    // Determine the active category from the current stack
    // (category items sit at route '/' with a category property)
    const currentCategoryItem = stack.find(c => c.category !== undefined && c.route === '/');
    const currentCategory = currentCategoryItem?.category;

    if (!currentCategory || currentCategory !== category) {
      // Different section (or coming from home) — reset with new category
      this.stackSubject.next([
        HOME_CRUMB,
        { label: category, route: '/', category },
        newItem
      ]);
    } else {
      // Same section — accumulate
      this.stackSubject.next([...stack, newItem]);
    }
  }

  /** Called by sidebar when user clicks a breadcrumb item to go back */
  truncateTo(item: BreadcrumbItem): void {
    const stack = this.stackSubject.getValue();
    const index = stack.indexOf(item);
    if (index !== -1) {
      this.stackSubject.next(stack.slice(0, index + 1));
    }
  }
}
