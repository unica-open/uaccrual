/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Type } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';

// ──────────────────────────────────────────────
// TIPI di supporto
// ──────────────────────────────────────────────

export type ParamPattern = string | RegExp | ((nome: string) => boolean);

export type WidgetComponent = Type<ControlValueAccessor & {
  label?: string;
  required?: boolean;
  disabled?: boolean;
  [key: string]: any;
}>;

export interface TemplateConfig {
  defaultValues?: Record<string, (reportInfo: any) => any>;
  widgetOverrides?: Map<ParamPattern, WidgetComponent>;
  hiddenParams?: ParamPattern[];
  labelOverrides?: Map<ParamPattern, string>;
  cssClassOverrides?: Map<ParamPattern, string>;
}

// ──────────────────────────────────────────────
// MAPPE DI CONFIGURAZIONE
// ──────────────────────────────────────────────

import { DataDaWidget } from '../widgets/global/data-da/data-da.widget';
import { AnnoBilancioWidget } from '../widgets/global/anno-bilancio/anno-bilancio.widget';
export const GLOBAL_WIDGET_MAP = new Map<ParamPattern, WidgetComponent>([
  [/^data_da/, DataDaWidget],
  ['anno_bilancio', AnnoBilancioWidget],
]);

export const TEMPLATE_CONFIGS: Record<string, TemplateConfig> = {};

// ──────────────────────────────────────────────
// FUNZIONI DI UTILITÀ
// ──────────────────────────────────────────────

export function matchPattern(nome: string, pattern: ParamPattern): boolean {
  if (typeof pattern === 'string') {
    if (pattern.endsWith('*')) {
      return nome.startsWith(pattern.slice(0, -1));
    }
    if (pattern.startsWith('*')) {
      return nome.endsWith(pattern.slice(1));
    }
    return nome === pattern;
  }
  if (pattern instanceof RegExp) {
    return pattern.test(nome);
  }
  return pattern(nome);
}

export function findMatch<V>(map: Map<ParamPattern, V> | undefined, nome: string): V | null {
  if (!map) return null;
  for (const [pattern, value] of map.entries()) {
    if (matchPattern(nome, pattern)) {
      return value;
    }
  }
  return null;
}

export function matchesAny(patterns: ParamPattern[] | undefined, nome: string): boolean {
  if (!patterns) return false;
  return patterns.some(p => matchPattern(nome, p));
}

export function estraiAnnoBilancio(reportInfo: any): number {
  return new Date().getFullYear();
}
