/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

export interface MenuAction {
  id: string;
  title: string;
  description: string;
  icon?: string;
  routerLink: string;
  queryParams?: any;
  category: string;
  categoryIcon?: string;
  requiredPermission?: string;
}

export const MENU_ACTIONS: MenuAction[] = [
  /*
  {
    id: 'insertCausale',
    title: 'Inserisci Causale',
    description: 'Permette di inserire una nuova causale di accrual.',
    routerLink: '/causale/inserisci',
    icon: 'add'
  },
  */
  {
    id: 'caricamentoFlusso',
    title: 'Acquisizione flusso di alimentazione',
    description: 'Permette di caricare flussi di alimentazione.',
    routerLink: '/flusso/upload',
    icon: 'upload',
    category: 'ACQUISIZIONE FLUSSI DI ALIMENTAZIONE',
    categoryIcon: 'cloud_upload',
    requiredPermission: 'OP-GEN-uploadFile'
  },
  {
    id: 'ricercaFlusso',
    title: 'Ricerca flusso di alimentazione',
    description: 'Permette di ricercare i flussi di alimentazione.',
    routerLink: '/flusso/search',
    icon: 'search',
    category: 'ACQUISIZIONE FLUSSI DI ALIMENTAZIONE',
    categoryIcon: 'cloud_upload'
  },
  {
    id: 'ricercaPrimaNota',
    title: 'Ricerca Prima Nota',
    description: 'Permette di ricercare le prime note.',
    routerLink: '/primanota/search',
    icon: 'search',
    category: 'PRIME NOTE',
    categoryIcon: 'edit_note'
  },
  {
    id: 'insertPrimaNota',
    title: 'Inserimento Prima Nota',
    description: 'Permette di inserire una prima nota.',
    icon: 'edit',
    routerLink: '/primanota/nuovo',
    category: 'PRIME NOTE',
    categoryIcon: 'edit_note',
    requiredPermission: 'OP-GEN-addPrimaNota'
  },
  {
    id: 'report',
    title: 'Report',
    description: 'Permette di generare un report.',
    icon: 'search',
    routerLink: '/report',
    category: 'REPORT',
    categoryIcon: 'assessment'
  },
  {
    id: 'ricercaReport',
    title: 'Ricerca Report',
    description: 'Permette di ricercare un report.',
    icon: 'search',
    routerLink: '/report/search',
    category: 'REPORT',
    categoryIcon: 'assessment'
  },
  {
    id: 'modellomif',
    title: 'Popolamento modello di raccordo',
    description: 'Permette di scaricare il modello di raccordo popolato con i saldi e le registrazioni Accrual.',
    icon: 'download',
    routerLink: '/modellomif/upload',
    category: 'REPORT',
    categoryIcon: 'assessment'
  },
  {
    id: 'ricercaContoAccrual',
    title: 'Conto Accrual',
    description: 'Permette di ricercare i conti di accrual esistenti.',
    routerLink: '/pdc/search',
    icon: 'search',
    category: 'ANAGRAFICHE',
    categoryIcon: 'manage_accounts'
  },
  {
    id: 'ricercaCausale',
    title: 'Causale',
    description: 'Permette di ricercare le causali di accrual esistenti.',
    routerLink: '/causale/search',
    icon: 'search',
    category: 'ANAGRAFICHE',
    categoryIcon: 'manage_accounts'
  }

];