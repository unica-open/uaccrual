/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Store } from '@ngxs/store';
import { SetUserPermissions } from '../store/context/context.state';
import { environment } from '../../../environments/environment';

interface AzioneDto {
  id: number;
  codice: string;
  descrizione: string;
  ordine: number;
  path: string;
}

@Injectable({ providedIn: 'root' })
export class AzioniService {
  private readonly baseUrl = environment.services.accrualLegacyApiUrl;

  constructor(private http: HttpClient, private store: Store) {}

  loadPermissions(): Observable<string[]> {
    return this.http.get<AzioneDto[]>(`${this.baseUrl}/azioni`).pipe(
      map(azioni => azioni.map(a => a.codice)),
      map(codici => {
        this.store.dispatch(new SetUserPermissions(codici));
        return codici;
      })
    );
  }
}
