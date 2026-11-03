/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PdcService } from '../../../../../core/services/pdc.service';
import { NotificationService } from '../../../../../core/services/notification.service';
import { MatDialog } from '@angular/material/dialog';
import { Pdc } from '../../model/pdc.model';

@Component({
  selector: 'app-pdc-form',
  templateUrl: './pdc-form.component.html',
})
export class PdcFormStubComponent implements OnInit {
  form!: FormGroup;
  isUpdateMode = false;
  isLoading = false;
  private codicePdc?: string;

  classi: Array<{ codice: string, descrizione: string, segno?: string }> = [];
  tipi: Array<{ codice: string, descrizione: string }> = [];
  stati: Array<{ codice: string, descrizione: string }> = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private pdcService: PdcService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // Inizializzazione del form
    this.form = this.fb.group({
      codicePadre: [{ value: '', disabled: true }],
      codice: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9._-]+$/)]],
      codice_completo: [{ value: '', disabled: true }],
      descrizione: ['', [Validators.required, Validators.maxLength(200)]],
      id_classe_conto: [{value: '', disabled: true}],
      livello: [{ value: null, disabled: true }],
      id_tipo_conto: [{value: '', disabled: true}],
      id_stato_conto: [null, Validators.required],
      flag_foglia: [true],
      flag_fondo_dotazione: [false],
      flag_a_partita: [false],
      codice_bilancio: [''],
      data_inizio_validita: ['', [Validators.required]],
      data_fine_validita: [''],
      version: [null]
    });
  }

  getStatoDescrizione(): string {
    return '';
  }

  getTipoDescrizione(): string {
    return '';
  }

  onSubmit(): void {
    // Metodo per il salvataggio
  }

  onSubmitAndExit(): void {
    // Metodo per il salvataggio ed uscita
  }

  onBack(): void {
    // Metodo per tornare indietro
  }

  onCancel(): void {
    // Metodo per annullare
  }

  deletePdcByCodicePdc(): void {
    // Metodo per eliminare un PDC
  }
}