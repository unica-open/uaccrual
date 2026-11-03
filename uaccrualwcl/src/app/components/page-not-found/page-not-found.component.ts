/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component } from '@angular/core';

@Component({
  selector: 'app-page-not-found',
  template: `
    <div class="container">
      <div class="row">
        <div class="col-md-12">
          <div class="error-template">
            <h1>Oops!</h1>
            <h2>404 Non Trovato</h2>
            <div class="error-details">
              La pagina che stai cercando non esiste.
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class PageNotFoundComponent {}