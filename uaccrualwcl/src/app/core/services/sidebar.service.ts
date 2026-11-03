/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { MENU_ACTIONS } from '../constants/menu-actions.constant';

@Injectable({ providedIn: 'root' })
export class SidebarService {

  private readonly _collapsed = new BehaviorSubject<boolean>(false);
  private readonly _selectedCategory = new BehaviorSubject<string>(this.firstCategory());

  readonly collapsed$ = this._collapsed.asObservable();
  readonly selectedCategory$ = this._selectedCategory.asObservable();

  get selectedCategory(): string {
    return this._selectedCategory.value;
  }

  toggle(): void {
    this._collapsed.next(!this._collapsed.value);
  }

  selectCategory(category: string): void {
    this._selectedCategory.next(category);
  }

  private firstCategory(): string {
    const first = MENU_ACTIONS.find(a => !!a.category);
    return first?.category ?? '';
  }
}
