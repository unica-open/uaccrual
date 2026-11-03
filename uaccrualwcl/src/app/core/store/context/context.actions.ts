/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

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

export class SetAvailableEnti {
  static readonly type = '[Context] Set Available Enti';
  constructor(public enti: string[]) {}
}