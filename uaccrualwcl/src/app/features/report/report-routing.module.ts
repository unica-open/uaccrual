/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportListComponent } from './components/report-list/report-list.component';
import { ReportFormComponent } from './components/report-form/report-form.component';
import { AsyncReportSearchComponent } from './components/async-report-search/async-report-search.component';
import { AsyncReportDetailComponent } from './components/async-report-detail/async-report-detail.component';

const routes: Routes = [
  {
    path: '',
    component: ReportListComponent
  },
  {
    path: 'form',
    component: ReportFormComponent
  },
  {
    path: 'search',
    children: [
      {
        path: '',
        component: AsyncReportSearchComponent
      },
      {
        path: 'results',
        redirectTo: '',
        pathMatch: 'full'
      },
      {
        path: ':id',
        component: AsyncReportDetailComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule { }
