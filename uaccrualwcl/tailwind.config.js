/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  darkMode: false,
  theme: {
    extend: {
      colors: {
        // ── Theme-reactive colors (follow CSS vars set by ThemeService) ──
        primary: ({ opacityValue }) =>
          opacityValue !== undefined
            ? `rgba(var(--primary-rgb), ${opacityValue})`
            : `rgb(var(--primary-rgb))`,
        'primary-dark': ({ opacityValue }) =>
          opacityValue !== undefined
            ? `rgba(var(--primary-dark-rgb), ${opacityValue})`
            : `rgb(var(--primary-dark-rgb))`,
        'primary-hover': ({ opacityValue }) =>
          opacityValue !== undefined
            ? `rgba(var(--primary-dark-rgb), ${opacityValue})`
            : `rgb(var(--primary-dark-rgb))`,
        'primary-faint':   'var(--primary-faint)',
        'primary-surface': 'var(--primary-surface)',

        // ── Static semantic colors ──
        "background-light":      "#f8f9fa",
        "background-dark":       "#121212",
        "surface-light":         "#ffffff",
        "surface-dark":          "#1e1e1e",
        "surface-accent-light":  "var(--primary-surface)",
        "surface-accent-dark":   "#2C2C2C",
        "text-primary-light":    "#212529",
        "text-primary-dark":     "#e0e0e0",
        "text-secondary-light":  "#6c757d",
        "text-secondary-dark":   "#a0a0a0",
        "border-light":          "#dee2e6",
        "border-dark":           "#333333",
        "link-light": ({ opacityValue }) =>
          opacityValue !== undefined
            ? `rgba(var(--primary-rgb), ${opacityValue})`
            : `rgb(var(--primary-rgb))`,
        "link-dark":    "#64B5F6",
        "success-light": "#198754",
        "success-dark":  "#4caf50",
      },
      fontFamily: {
        display: ["Roboto", "sans-serif"],
      },
      borderRadius: {
        DEFAULT: "0.375rem",
      },
      boxShadow: {
        'soft': '0 1px 3px rgba(0,0,0,0.06), 0 2px 8px rgba(0,0,0,0.08)',
        'card': '0 2px 8px rgba(0,0,0,0.10), 0 1px 3px rgba(0,0,0,0.06)',
      },
    },
  },
  plugins: [],
}
