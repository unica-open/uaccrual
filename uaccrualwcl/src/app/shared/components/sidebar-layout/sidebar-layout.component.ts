/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy, Output, EventEmitter, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter, map, startWith } from 'rxjs/operators';
import { ContextState } from '../../../core/store/context/context.state';
import { Store } from '@ngxs/store';
import { MENU_ACTIONS } from '../../../core/constants/menu-actions.constant';

interface NavSection {
  label: string;
  hue: number;
  bulletIcon: string;
  items: NavItem[];
}

interface NavItem {
  id: string;
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sb" [class.sb--collapsed]="collapsed" aria-label="Navigazione principale">
      <div class="sb-brand" [routerLink]="['/']">
        <div class="sb-name" [class.sb-hidden]="collapsed">
          <strong>UNICA</strong>
          <span>Accrual</span>
        </div>
        <button class="sb-brand-action" (click)="toggle(); $event.stopPropagation()" title="Comprimi sidebar" aria-label="Comprimi">
          <span class="material-icons-round">{{ collapsed ? 'last_page' : 'first_page' }}</span>
        </button>
      </div>

      <nav class="sb-body" aria-label="Moduli">
        <section class="sb-section" *ngFor="let sec of sections" [style.--g-hue]="sec.hue">
          <header class="sb-section-head">
            <span class="sb-section-label" [class.sb-hidden]="collapsed">{{ sec.label }}</span>
            <span class="sb-section-count" *ngIf="sec.items.length && !collapsed">{{ sec.items.length }}</span>
          </header>
          <button *ngFor="let item of sec.items"
            class="sb-item"
            [class.is-active]="(currentRoute$ | async) === item.route"
            (click)="navigate(item.route)"
            [title]="collapsed ? item.label : ''">
            <span class="sb-item-icon material-icons-round">{{ item.icon }}</span>
            <span class="sb-item-label" [class.sb-hidden]="collapsed">{{ item.label }}</span>
            <span class="sb-item-chev material-icons-round" [class.sb-hidden]="collapsed">chevron_right</span>
          </button>
        </section>
      </nav>

    </aside>
  `,
  styles: [`
    :host { display: contents; }

    .sb {
      background:
        radial-gradient(120% 50% at 0% 0%, rgba(66,110,220,0.12) 0%, transparent 55%),
        radial-gradient(80% 40% at 100% 100%, rgba(100,60,200,0.08) 0%, transparent 60%),
        linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
      color: #f1f5f9;
      border-right: 1px solid #334155;
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      position: relative;
      isolation: isolate;
    }
    .sb::before {
      content: "";
      position: absolute;
      inset: 0;
      background-image: radial-gradient(rgba(255,255,255,0.012) 1px, transparent 1px);
      background-size: 6px 6px;
      pointer-events: none;
      z-index: 0;
    }
    .sb > * { position: relative; z-index: 1; }

    .sb-hidden { display: none !important; }

    /* ── Brand ── */
    .sb-brand {
      display: flex; align-items: center; gap: 12px;
      padding: 18px 18px 16px;
      border-bottom: 1px solid rgba(255,255,255,0.06);
      cursor: pointer;
    }
    .sb--collapsed .sb-brand { padding: 18px 12px 16px; justify-content: center; gap: 8px; }
    .sb-name { display: flex; flex-direction: column; line-height: 1.05; }
    .sb-name strong { font-size: 15px; font-weight: 700; color: #fff; letter-spacing: -0.01em; }
    .sb-name span {
      font-size: 10.5px; color: #64748b; letter-spacing: 0.12em;
      text-transform: uppercase; margin-top: 3px; font-weight: 500;
    }
    .sb-brand-action {
      margin-left: auto; width: 28px; height: 28px; border-radius: 7px;
      background: transparent; border: 1px solid #334155;
      color: #94a3b8; display: grid; place-items: center;
      cursor: pointer; transition: background 140ms, color 140ms, border-color 140ms;
      flex-shrink: 0;
    }
    .sb-brand-action:hover { background: rgba(255,255,255,0.06); color: #fff; border-color: #475569; }
    .sb-brand-action .material-icons-round { font-size: 18px; }
    .sb--collapsed .sb-brand-action { margin-left: 0; }

    /* ── Nav body ── */
    .sb-body {
      flex: 1; overflow-y: auto; padding: 8px 0 12px;
      scrollbar-width: thin; scrollbar-color: #334155 transparent;
    }
    .sb-body::-webkit-scrollbar { width: 6px; }
    .sb-body::-webkit-scrollbar-thumb { background: #334155; border-radius: 999px; }
    .sb-body::-webkit-scrollbar-track { background: transparent; }

    /* ── Section ── */
    .sb-section { padding: 6px 12px 10px; }
    .sb-section + .sb-section { padding-top: 16px; }
    .sb--collapsed .sb-section { padding: 6px 4px 10px; }
    .sb--collapsed .sb-section + .sb-section { padding-top: 8px; }

    .sb-section-head {
      display: flex; align-items: center; gap: 10px;
      padding: 6px 10px 10px; cursor: default;
    }
    .sb--collapsed .sb-section-head { padding: 6px 0 10px; justify-content: center; }

    .sb-section-label {
      font-size: 10.5px; font-weight: 700; letter-spacing: 0.1em;
      text-transform: uppercase; color: #64748b; flex: 1; line-height: 1.2;
    }
    .sb-section-count {
      font-size: 10.5px; color: #475569; font-variant-numeric: tabular-nums;
      background: rgba(15,23,42,0.5); border: 1px solid rgba(255,255,255,0.06);
      padding: 1px 7px; border-radius: 999px; font-weight: 600;
    }

    /* ── Nav item ── */
    .sb-item {
      display: flex; align-items: center;       gap: 11px;
      padding: 8px 10px; margin: 1px 0;
      border-radius: 8px; color: #94a3b8;
      cursor: pointer; font-size: 13.5px; font-weight: 500;
      border: none; background: transparent; width: 100%; text-align: left;
      transition: background 140ms, color 140ms; position: relative; letter-spacing: -0.005em;
    }
    .sb--collapsed .sb-item { justify-content: center; padding: 8px 4px; }
    .sb-item:hover { background: rgba(255,255,255,0.06); color: #fff; }
    .sb-item-icon {
      width: 18px; display: grid; place-items: center; flex-shrink: 0;
      transition: filter 160ms, transform 160ms;
    }
    .sb-item-icon .material-icons-round { font-size: 18px; }
    .sb-item:hover .sb-item-icon { filter: brightness(1.2) saturate(1.15); }
    .sb-item-label { flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .sb-item-chev {
      color: #475569; opacity: 0; transform: translateX(-4px);
      transition: opacity 160ms, transform 160ms, color 140ms;
    }
    .sb-item-chev .material-icons-round { font-size: 16px; }
    .sb-item:hover .sb-item-chev { opacity: 1; transform: translateX(0); color: #94a3b8; }

    /* Active state */
    .sb-item.is-active {
      background: linear-gradient(90deg,
        hsla(var(--g-hue, 220), 55%, 55%, 0.15) 0%,
        hsla(var(--g-hue, 220), 55%, 55%, 0.04) 100%);
      color: #fff; font-weight: 600;
    }
    .sb-item.is-active::before {
      content: ""; position: absolute; left: -12px; top: 9px; bottom: 9px; width: 3px;
      border-radius: 0 3px 3px 0;
      background: hsl(var(--g-hue, 220), 55%, 55%);
      box-shadow: 0 0 12px hsla(var(--g-hue, 220), 55%, 55%, 0.6), 0 0 24px hsla(var(--g-hue, 220), 55%, 55%, 0.25);
    }
    .sb--collapsed .sb-item.is-active::before { left: -4px; }
    .sb-item.is-active .sb-item-icon {
      filter: brightness(1.25) saturate(1.2);
    }

    /* ── Footer ── */

  `]
})
export class SidebarLayoutComponent implements OnInit, OnDestroy {
  @Output() collapseChange = new EventEmitter<boolean>();

  collapsed = false;
  currentRoute$: Observable<string>;
  displayName$: Observable<string | null>;
  ruolo$: Observable<string | null>;
  initials$: Observable<string>;
  sections: NavSection[] = [];
  private permissionsSubscription?: Subscription;

  private readonly categoryMeta: Record<string, { label: string; hue: number; bulletIcon: string }> = {
    'ACQUISIZIONE FLUSSI DI ALIMENTAZIONE': { label: 'Acquisizione flussi', hue: 170, bulletIcon: 'cloud_upload' },
    'PRIME NOTE': { label: 'Prime note', hue: 245, bulletIcon: 'description' },
    'REPORT': { label: 'Report', hue: 220, bulletIcon: 'bar_chart' },
    'ANAGRAFICHE': { label: 'Anagrafiche', hue: 155, bulletIcon: 'people' },
  };

  constructor(
    private store: Store,
    private router: Router
  ) {
    this.currentRoute$ = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map(() => this.router.url.split('?')[0]),
      startWith(this.router.url.split('?')[0])
    );
    this.displayName$ = this.store.select(ContextState.getDisplayName);
    this.ruolo$ = this.store.select(ContextState.getRuolo);
    this.initials$ = new Observable<string>(obs => {
      this.displayName$.subscribe(name => {
        if (!name) { obs.next('?'); return; }
        obs.next(name.split(' ').filter(w => w.length > 0).slice(0, 2).map(w => w[0].toUpperCase()).join(''));
      });
    });
  }

  ngOnInit(): void {
    this.permissionsSubscription = this.store.select(ContextState.getUserPermissions)
      .subscribe(userPermissions => {
        this.sections = this.buildSections(userPermissions);
      });
  }

  ngOnDestroy(): void {
    this.permissionsSubscription?.unsubscribe();
  }

  private buildSections(userPermissions: string[]): NavSection[] {
    const groups: { category: string; items: NavItem[] }[] = [];
    for (const action of MENU_ACTIONS) {
      if (action.requiredPermission && !userPermissions.includes(action.requiredPermission)) {
        continue;
      }
      let group = groups.find(g => g.category === action.category);
      if (!group) {
        group = { category: action.category, items: [] };
        groups.push(group);
      }
      group.items.push({
        id: action.id || action.routerLink || '/',
        label: action.title,
        icon: action.icon || 'folder',
        route: action.routerLink || '/',
      });
    }
    return groups
      .filter(g => g.items.length > 0)
      .map(g => {
        const meta = this.categoryMeta[g.category] || { label: g.category, hue: 220, bulletIcon: 'folder' };
        return { label: meta.label, hue: meta.hue, bulletIcon: meta.bulletIcon, items: g.items };
      });
  }

  toggle(): void {
    this.collapsed = !this.collapsed;
    this.collapseChange.emit(this.collapsed);
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.router.navigate(['/logout']);
  }
}
