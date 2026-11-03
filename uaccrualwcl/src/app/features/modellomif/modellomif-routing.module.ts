/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ModellomifUploadComponent } from './components/modellomif-upload/modellomif-upload.component';

const routes: Routes = [
  { path: 'upload', component: ModellomifUploadComponent },
  { path: '', redirectTo: 'upload', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModellomifRoutingModule { }
