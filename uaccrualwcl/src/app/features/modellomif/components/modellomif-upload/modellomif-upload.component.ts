/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { ModellomifService } from '../../services/modellomif.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AuthStorageService } from '../../../../core/services/auth-storage.service';
import { Location } from '@angular/common';
import ExcelJS from 'exceljs';

@Component({
  selector: 'app-modellomif-upload',
  templateUrl: './modellomif-upload.component.html',
  styleUrls: ['./modellomif-upload.component.scss'],
  standalone: false
})
export class ModellomifUploadComponent implements OnInit {

  loading: boolean = false;
  anniLoading: boolean = false;
  showPreview: boolean = false;
  previewData: { [key: string]: any[][] } | null = null;
  previewLoading: boolean = false;
  activeFoglio: number = 0;
  annoSelezionato: number | null = null;
  anniDisponibili: { label: string; value: number }[] = [];

  templateStructure = {
    fogli: [
      { nome: '1 PdC DLGS 118_2011_PE e PP', descrizione: 'Piano dei Conti integrato ex Dlgs 118/2011', righeTotali: 5623, righeDaPopolare: '5-5623', colonneDaPopolare: ['H'] },
      { nome: '2 Pdc unico (segm A)', descrizione: 'Piano dei conti unico - Segmento A (ITAS)', righeTotali: 723, righeDaPopolare: '4-723', colonneDaPopolare: ['L'] },
      { nome: '3 Stato Patrimoniale-All1ITAS1', descrizione: 'Stato Patrimoniale - Allegato 1 ITAS1', righeTotali: 165, righeDaPopolare: '-', colonneDaPopolare: [] },
      { nome: '4 Schema aggiuntivo SP', descrizione: 'Schema aggiuntivo Stato Patrimoniale', righeTotali: 85, righeDaPopolare: '-', colonneDaPopolare: [] },
      { nome: '5 Conto Economico-All2 ITAS1', descrizione: 'Conto Economico - Allegato 2 ITAS1', righeTotali: 55, righeDaPopolare: '-', colonneDaPopolare: [] },
      { nome: '6 Pdc unico completo', descrizione: 'Piano dei conti unico completo', righeTotali: 4437, righeDaPopolare: '-', colonneDaPopolare: [] }
    ]
  };

  constructor(
    private modellomifService: ModellomifService,
    private notificationService: NotificationService,
    private authStorage: AuthStorageService,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.loadAnniDisponibili();
  }

  private async loadAnniDisponibili(): Promise<void> {
    this.anniLoading = true;
    try {
      const anni = await this.modellomifService.getAnniDisponibili();
      if (anni && anni.length > 0) {
        this.anniDisponibili = anni.map(a => ({
          label: `${a.anno} - ${a.fase}`,
          value: a.anno
        }));
        this.annoSelezionato = anni[0].anno;
      }
    } catch (error) {
      console.error('Errore caricamento anni disponibili:', error);
    } finally {
      this.anniLoading = false;
    }
  }

  async scaricaModello(): Promise<void> {
    if (!this.annoSelezionato) return;
    this.loading = true;

    try {
      const codiceFiscaleEnte = this.authStorage.getEnteCodice() || '';
      await this.modellomifService.scaricaModello(this.annoSelezionato, codiceFiscaleEnte);
      this.notificationService.showSuccess('Il modello MEF è stato generato e scaricato con successo', 'Operazione completata');
    } catch (error) {
      console.error('Errore durante la generazione del modello:', error);
      this.notificationService.showError('Si è verificato un errore durante la generazione del modello. Riprova più tardi.', 'Errore');
    } finally {
      this.loading = false;
    }
  }

  onBack(): void {
    this.location.back();
  }

  async togglePreview(): Promise<void> {
    this.showPreview = !this.showPreview;
    if (this.showPreview && !this.previewData) {
      await this.loadPreview();
    }
  }

  async loadPreview(): Promise<void> {
    this.previewLoading = true;
    try {
      const response = await fetch('./templates/Mod 2-a_Raccordo PdC eettDlgs118_2011_PdC unico_28012026 1.xlsx');
      const buffer = await response.arrayBuffer();
      
      const workbook = new ExcelJS.Workbook();
      await workbook.xlsx.load(buffer);
      
      const previewData: { [key: string]: any[][] } = {};
      
      // Leggi tutti i fogli
      workbook.eachSheet((worksheet, sheetId) => {
        const rowData: any[][] = [];
        
        worksheet.eachRow((row, rowNumber) => {
          if (rowNumber <= 25) { // Prime 25 righe per ogni foglio
            const cellData: any[] = [];
            // Leggi tutte le colonne del foglio
            for (let i = 1; i <= worksheet.actualColumnCount; i++) {
              const cell = row.getCell(i);
              // Converte il valore in stringa in modo sicuro
              let cellValue: any = cell.value;
              
              // Gestisce oggetti complessi (formule, rich text, ecc.)
              if (cellValue && typeof cellValue === 'object') {
                if (cellValue.richText) {
                  cellValue = cellValue.richText.map((rt: any) => rt.text).join('');
                } else if (cellValue.formula) {
                  cellValue = cellValue.formula;
                } else if (cellValue.result) {
                  cellValue = cellValue.result;
                } else {
                  cellValue = JSON.stringify(cellValue);
                }
              }
              
              cellData.push(cellValue);
            }
            rowData.push(cellData);
          }
        });
        
        previewData[worksheet.name] = rowData;
      });
      
      this.previewData = previewData;
    } catch (error) {
      console.error('Errore nel caricamento anteprima:', error);
      this.notificationService.showWarning('Impossibile caricare l\'anteprima. Puoi comunque scaricare il template.', 'Attenzione');
    } finally {
      this.previewLoading = false;
    }
  }

  downloadTemplate(): void {
    const link = document.createElement('a');
    link.href = './templates/Mod 2-a_Raccordo PdC eettDlgs118_2011_PdC unico_28012026 1.xlsx';
    link.download = 'Mod 2-a_Raccordo PdC eettDlgs118_2011_PdC unico_28012026 1.xlsx';
    link.click();
  }

  selectFoglio(index: number): void {
    this.activeFoglio = index;
  }

  // Converte indice numerico in lettera colonna (1 -> A, 2 -> B, ..., 27 -> AA, ecc.)
  getColumnLetter(colNum: number): string {
    let letter = '';
    let num = colNum;
    while (num > 0) {
      num--;
      letter = String.fromCharCode(65 + (num % 26)) + letter;
      num = Math.floor(num / 26);
    }
    return letter;
  }

  // Verifica se una colonna deve essere popolata
  isColonnaDaPopolare(foglio: any, colIndex: number): boolean {
    const letter = this.getColumnLetter(colIndex);
    return foglio.colonneDaPopolare.includes(letter);
  }
}
