/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Necessario per ngModel
import { Pdc } from '../../../features/pdc/model/pdc.model'; // Importa l'interfaccia Pdc

@Component({
  selector: 'app-conto-selector-modal',
  standalone: true,
  imports: [CommonModule, FormsModule], // Aggiungi FormsModule
  templateUrl: './conto-selector-modal.component.html',
  styleUrl: './conto-selector-modal.component.scss',
})
export class ContoSelectorModalComponent {
  @Input() contiRicercati: Pdc[] = []; // Lista di PDC da mostrare nella modale
  @Output() pdcSelected = new EventEmitter<Pdc | null>(); // Emette il PDC selezionato o null

  selectedPdcCodice: string | null = null; // Codice del PDC selezionato dal radio button
  selectedPdc: Pdc | null = null; // Oggetto PDC completo selezionato

  constructor() { }

  // Gestisce la selezione di un PDC tramite radio button
  onPdcSelected(conto: Pdc) {
    this.selectedPdc = conto;
  }

  // Chiude la modale senza selezione
  closeModal() {
    this.pdcSelected.emit(null); // Emette null per indicare che non è stato selezionato nulla
  }

  // Conferma la selezione e chiude la modale
  confermaSelezionePdc() {
    if (this.selectedPdc) {
      this.pdcSelected.emit(this.selectedPdc); // Emette il PDC selezionato
      this.selectedPdc = null; // Resetta la selezione
      this.selectedPdcCodice = null;
    } else {
      // Potresti mostrare una notifica qui se nessun conto è selezionato
      // Per ora, l'HTML disabilita il pulsante se nulla è selezionato
    }
  }
}
