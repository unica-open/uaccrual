/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor, DatePipe } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PdcService } from '../../../../core/services/pdc.service';
import { Pdc } from '../../model/pdc.model';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { InfoDialogComponent } from '../../../../shared/components/info-dialog/info-dialog.component';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-pdc-detail',
  standalone: true,
  imports: [
    CommonModule,
    NgFor,
    DatePipe,
    FormsModule,
  ],
  templateUrl: './pdc-detail.component.html',
  styleUrls: ['./pdc-detail.component.scss']
})
export class PdcDetailComponent implements OnInit {
  pdc: Pdc | null = null;
  fondi: Pdc[] = [];
  correlazioni: Pdc[] = [];

  constructor(
    private pdcService: PdcService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {

    const codiceUfficiale = this.route.snapshot.paramMap.get('codiceUfficiale');
    const codiceClasse = this.route.snapshot.paramMap.get('codiceClasse');
    const codiceFamiglia = this.route.snapshot.paramMap.get('codiceFamiglia');

    if (codiceUfficiale && codiceClasse && codiceFamiglia) {
      // Carica i dati del conto consultato
      this.pdcService.getPdcById(codiceUfficiale, codiceClasse, codiceFamiglia).subscribe({
        next: (data) => {
          this.pdc = data;
          // Carica anche i conti collegati usando i parametri di route (già validati)
          this.loadContiCollegati(codiceUfficiale, codiceClasse, codiceFamiglia);
        },
        error: (error) => {
          console.error('Errore durante il caricamento del conto:', error);
          // Mostra messaggio di errore in pagina invece di dialog
          this.notificationService.error('WEB_ERR_0001 - Si è verificato un errore imprevisto: ' + (error.message || error), 'Errore');

          // Gestisci l'errore, ad esempio tornando alla ricerca
          this.router.navigate(['/pdc/search']);
        }
      });
    }
  }

  loadContiCollegati(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string) {
    this.pdcService.getContiCollegati(codiceUfficiale, codiceClasse, codiceFamiglia, 'FONDO').subscribe({
      next: (fondi) => {
        this.fondi = fondi;
      },
      error: (error) => {
        console.error('Errore durante il caricamento dei conti collegati tipo FONDO:', error);
        this.fondi = [];
      }
    });

    this.pdcService.getContiCollegati(codiceUfficiale, codiceClasse, codiceFamiglia, 'CORRELAZIONE').subscribe({
      next: (correlazioni) => {
        this.correlazioni = correlazioni;
      },
      error: (error) => {
        console.error('Errore durante il caricamento dei conti collegati tipo CORRELAZIONE:', error);
        this.correlazioni = [];
      }
    });
  }

  onBack() {
    // Torna al risultato di ricerca
    this.router.navigate(['/pdc/results']);
  }
}