/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngxs/store';
import { Subscription } from 'rxjs';
import { MenuAction, MENU_ACTIONS } from '../../core/constants/menu-actions.constant';
import { ContextState } from '../../core/store/context/context.state';

interface GroupTheme {
  color: string;
  color2: string;
  hue: string;
}

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit, OnDestroy {

  groupedActions: { category: string; categoryIcon: string; actions: MenuAction[]; theme: GroupTheme }[] = [];

  private readonly groupTheme: Record<string, GroupTheme> = {
    'ACQUISIZIONE FLUSSI DI ALIMENTAZIONE': { color: '#006D77', color2: '#004B52', hue: '170' },
    'PRIME NOTE': { color: '#1E3A8A', color2: '#0F1F55', hue: '245' },
    'REPORT': { color: '#0369A1', color2: '#024E7A', hue: '220' },
    'ANAGRAFICHE': { color: '#0D9488', color2: '#076059', hue: '160' },
  };

  private sub!: Subscription;

  constructor(private router: Router, private store: Store) {}

  ngOnInit(): void {
    this.sub = this.store.select(ContextState.getUserPermissions).subscribe(permissions => {
      this.buildGroups(permissions);
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  private buildGroups(permissions: string[]): void {
    const visibleActions = MENU_ACTIONS.filter(action =>
      !action.requiredPermission || permissions.includes(action.requiredPermission)
    );
    const groups: { [key: string]: MenuAction[] } = {};
    visibleActions.forEach(action => {
      if (!groups[action.category]) groups[action.category] = [];
      groups[action.category].push(action);
    });
    this.groupedActions = Object.keys(groups).map(category => ({
      category,
      categoryIcon: visibleActions.find(a => a.category === category)?.categoryIcon ?? 'apps',
      actions: groups[category],
      theme: this.groupTheme[category] ?? { color: '#1E3A8A', color2: '#0F1F55', hue: '220' },
    }));
  }
}
