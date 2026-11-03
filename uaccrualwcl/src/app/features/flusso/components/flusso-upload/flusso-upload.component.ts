/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Observable } from 'rxjs';
import { FlussoService } from '../../../../core/services/flusso.service';
import { TipoFile, FlussoUploadResponse } from '../../model/flusso.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { Router, RouterModule } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';


@Component({
  selector: 'app-flusso-upload',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './flusso-upload.component.html',
  styleUrls: ['./flusso-upload.component.scss']
})
export class FlussoUploadComponent implements OnInit {

  uploadForm: FormGroup;
  tipiFile$!: Observable<TipoFile[]>;
  selectedFile: File | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private flussoService: FlussoService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.uploadForm = this.fb.group({
      codice: ['', Validators.required],
      tipoFile: ['', Validators.required],
      note: ['']
    });
  }

  ngOnInit(): void {
    this.tipiFile$ = this.flussoService.getTipiFile();
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0] ?? null;
  }

  onSubmit(): void {
    if (this.uploadForm.valid && this.selectedFile) {
      this.loading = true;
      const { codice, tipoFile, note } = this.uploadForm.value;
      this.flussoService.uploadFlusso({ codice, tipoFile, note, file: this.selectedFile }).subscribe({
        next: (response: FlussoUploadResponse) => {
          this.notificationService.showSuccess(response.message || 'File caricato con successo.', 'Successo');
          this.loading = false;
          this.uploadForm.reset();
          this.selectedFile = null;
          // Optionally navigate or clear form
        },
        error: (error: any) => {
          // Error handling is done in the service, but can add specific handling here if needed
          this.loading = false;
          console.error('Errore nel componente:', error);
        }
      });
    } else {
      this.notificationService.showError('Compilare tutti i campi obbligatori e selezionare un file.', 'Errore di validazione');
    }
  }

  onCancel(): void {
    this.uploadForm.reset({
      codice: '',
      tipoFile: '',
      note: ''
    });
    this.selectedFile = null;
  }

  onBack(): void {
    this.router.navigate(['/']); // Naviga alla homepage
  }
}