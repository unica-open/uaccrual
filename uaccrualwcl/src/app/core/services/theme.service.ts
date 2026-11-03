/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';

export interface ThemePreset {
  name: string;
  color: string;
}

export const THEME_PRESETS: ThemePreset[] = [
  { name: 'Indigo',     color: '#173eac' },
  { name: 'Terracotta', color: '#c2410c' },
  { name: 'Oro',        color: '#ca8a04' },
  { name: 'Scuro',      color: '#374151' },
];

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'accrual_theme_color';
  readonly DEFAULT_COLOR = '#173eac';

  private currentColor = this.DEFAULT_COLOR;
  get color(): string { return this.currentColor; }

  loadSaved(): void {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    this.apply(saved ?? this.DEFAULT_COLOR);
  }

  apply(hex: string): void {
    if (!this.isValidHex(hex)) return;
    this.currentColor = hex;

    // Classe famiglia colore sul body (per varianti card/sezioni)
    const h = hex.toLowerCase();
    document.body.classList.remove('theme-warm', 'theme-gold', 'theme-dark');
    if      (h === '#c2410c') document.body.classList.add('theme-warm');
    else if (h === '#ca8a04') document.body.classList.add('theme-gold');
    else if (h === '#374151') document.body.classList.add('theme-dark');

    const rgb      = this.hexToRgb(hex)!;
    const hsl      = this.rgbToHsl(rgb.r, rgb.g, rgb.b);

    // Primary shades
    const isDefault  = hex.toLowerCase() === this.DEFAULT_COLOR.toLowerCase();
    const darkL      = Math.max(0, hsl.l - 12);
    const darkHex    = this.hslToHex(hsl.h, hsl.s, darkL);
    const darkRgb    = this.hexToRgb(darkHex)!;
    const lightHex   = this.hslToHex(hsl.h, hsl.s, Math.min(100, hsl.l + 20));
    const faintHex   = this.hslToHex(hsl.h, Math.min(hsl.s, 60), Math.min(97, hsl.l + 48));
    // Form surface: slightly tinted gray, always from primary hue
    const surfaceHex = isDefault ? '#eaecf0' : this.hslToHex(hsl.h, 20, 91);
    // Page background: primary hue at very low saturation — cream-tinted on default, theme-tinted on others
    const pageBg = isDefault ? '#faf8f4' : this.hslToHex(hsl.h, 14, 96);
    // Topbar: same hue, saturazione ridotta, luminosità molto bassa (11%)
    const topbarHex   = isDefault ? '#0f172a' : this.hslToHex(hsl.h, Math.min(hsl.s * 0.75, 65), 11);

    this.setVar('--primary',          hex);
    this.setVar('--primary-rgb',      `${rgb.r}, ${rgb.g}, ${rgb.b}`);
    this.setVar('--primary-dark',     darkHex);
    this.setVar('--primary-dark-rgb', `${darkRgb.r}, ${darkRgb.g}, ${darkRgb.b}`);
    this.setVar('--primary-light',    lightHex);
    this.setVar('--primary-faint',    faintHex);
    this.setVar('--primary-surface',  surfaceHex);
    this.setVar('--page-bg',          pageBg);
    this.setVar('--topbar-bg',        topbarHex);

    localStorage.setItem(this.STORAGE_KEY, hex);
  }

  private setVar(name: string, value: string): void {
    document.documentElement.style.setProperty(name, value);
  }

  private isValidHex(hex: string): boolean {
    return /^#[0-9a-fA-F]{6}$/.test(hex);
  }

  private hexToRgb(hex: string): { r: number; g: number; b: number } | null {
    const r = /^#([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return r ? { r: parseInt(r[1], 16), g: parseInt(r[2], 16), b: parseInt(r[3], 16) } : null;
  }

  private rgbToHsl(r: number, g: number, b: number): { h: number; s: number; l: number } {
    r /= 255; g /= 255; b /= 255;
    const max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h = 0, s = 0;
    const l = (max + min) / 2;
    if (max !== min) {
      const d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      switch (max) {
        case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break;
        case g: h = ((b - r) / d + 2) / 6; break;
        case b: h = ((r - g) / d + 4) / 6; break;
      }
    }
    return { h: h * 360, s: s * 100, l: l * 100 };
  }

  private hslToHex(h: number, s: number, l: number): string {
    h /= 360; s /= 100; l /= 100;
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1;
      if (t > 1) t -= 1;
      if (t < 1 / 6) return p + (q - p) * 6 * t;
      if (t < 1 / 2) return q;
      if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
      return p;
    };
    let r: number, g: number, b: number;
    if (s === 0) { r = g = b = l; } else {
      const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      const p = 2 * l - q;
      r = hue2rgb(p, q, h + 1 / 3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1 / 3);
    }
    const toHex = (x: number) => Math.round(x * 255).toString(16).padStart(2, '0');
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
  }
}
