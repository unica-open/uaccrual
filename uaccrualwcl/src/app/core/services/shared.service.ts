/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Store } from '@ngxs/store';
import { ContextState } from '../store/context/context.state';
import { AuthStorageService } from './auth-storage.service';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  constructor(
    private store: Store,
    private authStorage: AuthStorageService
  ) { }
}
