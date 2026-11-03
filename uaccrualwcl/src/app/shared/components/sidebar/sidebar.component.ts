/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { MENU_ACTIONS, MenuAction } from '../../../core/constants/menu-actions.constant';
import { SidebarService } from '../../../core/services/sidebar.service';
import { BreadcrumbService, BreadcrumbItem } from '../../../core/services/breadcrumb.service';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {

  collapsed$: Observable<boolean>;
  selectedCategory$: Observable<string>;
  breadcrumbs$: Observable<BreadcrumbItem[]>;

  groupedCategories: { category: string; categoryIcon: string }[] = [];

  constructor(
    private sidebarService: SidebarService,
    private breadcrumbService: BreadcrumbService,
    private router: Router
  ) {
    this.collapsed$ = this.sidebarService.collapsed$;
    this.selectedCategory$ = this.sidebarService.selectedCategory$;
    this.breadcrumbs$ = this.breadcrumbService.breadcrumbs$;
  }

  ngOnInit(): void {
    const seen = new Set<string>();
    for (const action of MENU_ACTIONS) {
      if (!seen.has(action.category)) {
        seen.add(action.category);
        this.groupedCategories.push({
          category: action.category,
          categoryIcon: action.categoryIcon ?? 'folder'
        });
      }
    }
  }

  toggle(): void {
    this.sidebarService.toggle();
  }

  select(category: string): void {
    this.sidebarService.selectCategory(category);
    if (!this.router.url.startsWith('/home') && this.router.url !== '/') {
      this.router.navigate(['/']);
    }
  }

  navigateCrumb(crumb: BreadcrumbItem): void {
    this.breadcrumbService.truncateTo(crumb);
    if (crumb.category) {
      this.sidebarService.selectCategory(crumb.category);
    }
    this.router.navigate([crumb.route]);
  }

  getShort(category: string): string {
    return category.split(' ').filter(w => w.length > 2).map(w => w[0]).join('').substring(0, 3).toUpperCase();
  }
}
