/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { State, Action, StateContext, Selector } from '@ngxs/store';
import { tap } from 'rxjs/operators';
import { AuthStorageService } from '../../services/auth-storage.service';

// Azioni per il contesto
export class LoadContext {
  static readonly type = '[Context] Load Context';
  constructor(public contextData: any) {}
}

export class SetFaseBilancio {
  static readonly type = '[Context] Set Fase Bilancio';
  constructor(public fase: string) {}
}

export class SetUserPermissions {
  static readonly type = '[Context] Set User Permissions';
  constructor(public permissions: string[]) {}
}

export class SetCurrentEnte {
  static readonly type = '[Context] Set Current Ente';
  constructor(public ente: string) {}
}

export class SetCurrentEnteDenominazione {
  static readonly type = '[Context] Set Current Ente Denominazione';
  constructor(public denominazione: string) {}
}

export class SetAvailableEnti {
  static readonly type = '[Context] Set Available Enti';
  constructor(public enti: any[]) {}
}

export class SetAnnoBilancio {
  static readonly type = '[Context] Set Anno Bilancio';
  constructor(public anno: number | null) {}
}

export class SetUserInfo {
  static readonly type = '[Context] Set User Info';
  constructor(public username: string | null, public displayName: string | null, public ruolo: string | null, public utenteCodice?: string | null) {}
}

export class ClearContext {
  static readonly type = '[Context] Clear Context';
}

// Modello per lo stato del contesto
export interface ContextStateModel {
  // Informazioni utente
  username: string | null;
  displayName: string | null;
  utenteCodice: string | null;
  email: string | null;
  ruolo: string | null;
  userPermissions: string[];

  // Informazioni di contesto aziendale
  faseBilancio: string | null;
  currentEnte: string | null;
  currentEnteDenominazione: string | null;
  availableEnti: any[];
  annoBilancio: number | null;
}

@State<ContextStateModel>({
  name: 'context',
  defaults: {
    username: null,
    displayName: null,
    utenteCodice: null,
    email: null,
    ruolo: null,
    userPermissions: [],
    faseBilancio: null,
    currentEnte: null,
    currentEnteDenominazione: null,
    availableEnti: [],
    annoBilancio: null
  }
})
@Injectable()
export class ContextState {

  constructor(private authStorage: AuthStorageService) {}

  @Selector()
  static getFaseBilancio(state: ContextStateModel) {
    return state.faseBilancio;
  }

  @Selector()
  static getUserPermissions(state: ContextStateModel) {
    return state.userPermissions;
  }

  @Selector()
  static canAddPdc(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-addPdc');
  }

  @Selector()
  static canUpdatePdc(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-updPdc');
  }

  @Selector()
  static canDeletePdc(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-delPdc');
  }

  @Selector()
  static canSearchCausale(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-ricCausale');
  }

  @Selector()
  static canAddCausale(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-addCausale');
  }

  @Selector()
  static canUpdateCausale(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-updCausale');
  }

  @Selector()
  static canDeleteCausale(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-delCausale');
  }

  @Selector()
  static canValidateCausale(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-validaCausale');
  }

  @Selector()
  static canSearchPrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-ricPrimaNota');
  }

  @Selector()
  static canAddPrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-addPrimaNota');
  }

  @Selector()
  static canUpdatePrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-updPrimaNota');
  }

  @Selector()
  static canAnnullPrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-annPrimaNota');
  }

  @Selector()
  static canRiapriPrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-riapriPrimaNota');
  }

  @Selector()
  static canValidaPrimaNota(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-validaPrimaNota');
  }

  @Selector()
  static canSearchFlusso(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-ricFile');
  }

  @Selector()
  static canAddFlusso(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-uploadFile');
  }

  @Selector()
  static canDeleteFlusso(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-delFlusso');
  }

  @Selector()
  static canElabFlusso(state: ContextStateModel) {
    return state.userPermissions.includes('OP-SPE-elaboraFile');
  }

  @Selector()
  static canGenerateReport(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-getReport');
  }

  @Selector()
  static canSearchReport(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-ricReport');
  }

  @Selector()
  static canUpdateModelloMif(state: ContextStateModel) {
    return state.userPermissions.includes('OP-GEN-updModMF');
  }

  @Selector()
  static isFaseBilancioAllowed(state: ContextStateModel) {
    // Le fasi consentite per le operazioni di modifica sono tutte tranne PLURIENNALE, PREVISIONE, CHIUSO
    const blockedPhases = ['PLURIENNALE', 'PREVISIONE', 'CHIUSO'];
    return state.faseBilancio && !blockedPhases.includes(state.faseBilancio.toUpperCase());
  }

  @Selector()
  static getCurrentEnte(state: ContextStateModel) {
    return state.currentEnte;
  }

  @Selector()
  static getCurrentEnteDenominazione(state: ContextStateModel) {
    return state.currentEnteDenominazione;
  }

  @Selector()
  static getAvailableEnti(state: ContextStateModel) {
    return state.availableEnti;
  }

  @Selector()
  static getAnnoBilancio(state: ContextStateModel) {
    return state.annoBilancio;
  }

  @Selector()
  static getUsername(state: ContextStateModel) {
    return state.username;
  }

  @Selector()
  static getDisplayName(state: ContextStateModel) {
    return state.displayName;
  }

  @Selector()
  static getUtenteCodice(state: ContextStateModel) {
    return state.utenteCodice;
  }

  @Selector()
  static getEmail(state: ContextStateModel) {
    return state.email;
  }

  @Selector()
  static getRuolo(state: ContextStateModel) {
    return state.ruolo;
  }

  @Action(SetUserInfo)
  setUserInfo(ctx: StateContext<ContextStateModel>, action: SetUserInfo) {
    ctx.patchState({
      username: action.username,
      displayName: action.displayName,
      ruolo: action.ruolo,
      utenteCodice: action.utenteCodice ?? null
    });
  }

  @Action(LoadContext)
  loadContext(ctx: StateContext<ContextStateModel>, action: LoadContext) {
    console.log('ContextState - LoadContext action ricevuta con dati:', action.contextData);
    const state = ctx.getState();
    ctx.setState({
      ...state,
      ...action.contextData
    });
    console.log('ContextState - Stato aggiornato, currentEnte:', ctx.getState().currentEnte);
  }

  @Action(SetFaseBilancio)
  setFaseBilancio(ctx: StateContext<ContextStateModel>, action: SetFaseBilancio) {
    ctx.patchState({
      faseBilancio: action.fase
    });
  }

  @Action(SetUserPermissions)
  setUserPermissions(ctx: StateContext<ContextStateModel>, action: SetUserPermissions) {
    console.log('[ContextState] SetUserPermissions:', action.permissions);
    console.log('[ContextState] OP-SPE-elaboraFile:', action.permissions.includes('OP-SPE-elaboraFile'));
    ctx.patchState({
      userPermissions: action.permissions
    });
  }

  @Action(SetCurrentEnte)
  setCurrentEnte(ctx: StateContext<ContextStateModel>, action: SetCurrentEnte) {
    ctx.patchState({
      currentEnte: action.ente
    });

    if (action.ente) {
      this.authStorage.setEnteCodice(action.ente);
    } else {
      this.authStorage.removeEnteCodice();
    }
  }

  @Action(SetCurrentEnteDenominazione)
  setCurrentEnteDenominazione(ctx: StateContext<ContextStateModel>, action: SetCurrentEnteDenominazione) {
    ctx.patchState({
      currentEnteDenominazione: action.denominazione
    });
  }

  @Action(SetAvailableEnti)
  setAvailableEnti(ctx: StateContext<ContextStateModel>, action: SetAvailableEnti) {
    ctx.patchState({
      availableEnti: action.enti
    });
  }

  @Action(SetAnnoBilancio)
  setAnnoBilancio(ctx: StateContext<ContextStateModel>, action: SetAnnoBilancio) {
    ctx.patchState({
      annoBilancio: action.anno
    });
  }

  @Action(ClearContext)
  clearContextAction(ctx: StateContext<ContextStateModel>) {
    ctx.setState({
      username: null, displayName: null, utenteCodice: null, email: null, ruolo: null,
      userPermissions: [], faseBilancio: null, currentEnte: null,
      currentEnteDenominazione: null, availableEnti: [], annoBilancio: null
    });
  }
}
