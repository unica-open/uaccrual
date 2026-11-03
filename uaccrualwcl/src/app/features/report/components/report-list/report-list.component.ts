/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReportConfig } from '../../models/report.models';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-report-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.scss']
})
export class ReportListComponent implements OnInit {

  // Lista hardcoded dei report disponibili (CDU-014)
  readonly reportList: ReportConfig[] = [
    {
      codice: 'MASTRINO',
      nome: 'Mastrino',
      descrizione: 'Report contabile dettagliato per conto',
      template: 'ACCRUAL-001_stampa_mastrino.rptdesign'
    },
    {
      codice: 'LIBRO_GIORNALE',
      nome: 'Libro Giornale',
      descrizione: 'Report sequenziale delle registrazioni contabili',
      template: 'ACCRUAL-002_stampa_libro_giornale.rptdesign'
    },
    {
      codice: 'BILANCIO',
      nome: 'Bilancio di Verifica',
      descrizione: 'Report riepilogativo per la verifica contabile',
      template: 'ACCRUAL-003_stampa_bilancio_verifica.rptdesign'
    }
  ];

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  /**
   * Naviga al form di configurazione del report
   * @param report Configurazione del report selezionato
   */
  eseguiReport(report: ReportConfig): void {
    this.router.navigate(['/report/form'], {
      queryParams: { rptdesign: report.template, nome: report.nome, codice: report.codice }
    });
  }
}
