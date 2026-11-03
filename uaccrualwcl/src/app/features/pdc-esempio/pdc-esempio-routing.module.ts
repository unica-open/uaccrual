/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PdcEsempioSearchFormComponent } from './components/pdc-esempio-search-form/pdc-esempio-search-form.component';
import { PdcEsempioDetailComponent } from './components/pdc-esempio-detail/pdc-esempio-detail.component';
import { PdcEsempioCreateComponent } from './components/pdc-esempio-create/pdc-esempio-create.component';
import { PdcEsempioEditComponent } from './components/pdc-esempio-edit/pdc-esempio-edit.component';

const routes: Routes = [
  {
    path: '', // Corrisponde a /pdc-esempio
    component: PdcEsempioSearchFormComponent // Componente predefinito per /pdc-esempio (es. form di ricerca o lista)
  },
  {
    path: 'ricerca', // Corrisponde a /pdc-esempio/ricerca
    component: PdcEsempioSearchFormComponent // Componente per i risultati della ricerca
  },
  {
    path: ':id', // Corrisponde a /pdc-esempio/:id per il dettaglio di un PdcEsempio
    component: PdcEsempioDetailComponent
  },
  {
    path: 'nuovo', // Corrisponde a /pdc-esempio/nuovo per l'inserimento di un nuovo PdcEsempio
    component: PdcEsempioCreateComponent
  },
  {
    path: ':id/modifica', // Corrisponde a /pdc-esempio/:id/modifica per la modifica di un PdcEsempio esistente
    component: PdcEsempioEditComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PdcEsempioRoutingModule { }
