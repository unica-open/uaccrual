/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PdcEsempioRoutingModule } from './pdc-esempio-routing.module';
import { PdcEsempioSearchFormComponent } from './components/pdc-esempio-search-form/pdc-esempio-search-form.component';
import { PdcEsempioDetailComponent } from './components/pdc-esempio-detail/pdc-esempio-detail.component';
import { PdcEsempioCreateComponent } from './components/pdc-esempio-create/pdc-esempio-create.component';
import { PdcEsempioEditComponent } from './components/pdc-esempio-edit/pdc-esempio-edit.component';


@NgModule({
  declarations: [
    PdcEsempioSearchFormComponent,
    PdcEsempioDetailComponent,
    PdcEsempioCreateComponent,
    PdcEsempioEditComponent
  ],
  imports: [
    CommonModule,
    PdcEsempioRoutingModule
  ]
})
export class PdcEsempioModule { }
