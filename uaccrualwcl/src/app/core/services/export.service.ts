/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Pdc } from '../../features/pdc/model/pdc.model';

@Injectable({
  providedIn: 'root'
})
export class ExportService {

  constructor() { }

  exportToExcel(data: Pdc[], fileName: string): void {
    // In una implementazione completa, utilizzeremmo una libreria come xlsx
    // Per ora, creiamo un file CSV simulato
    
    // Creiamo l'intestazione del file
    const headers = [
      'Classe', 'Tipo', 'Evento', 'Livello', 'Segmento', 
      'Codice', 'Descrizione', 'Foglia', 'Stato'
    ];
    
    // Creiamo i dati come stringa CSV
    const csvContent = [
      headers.join(','),
      ...data.map(item => [
        item.pdc_classe?.descrizione || item.classe || '',
        item.pdc_tipo?.descrizione || item.tipo || '',
        item.evento?.descrizione || '',
        item.livello || '',
        item.segmento || '',
        item.codice || '',
        item.descrizione || '',
        item.foglia ? 'Si' : 'No',
        item.pdc_stato?.descrizione || item.stato || ''
      ].join(','))
    ].join('\n');
    
    this.downloadCsv(csvContent, fileName);
  }

  /**
   * Esporta un array di dati generici in un file CSV.
   * @param data L'array di oggetti da esportare.
   * @param headers Un array di oggetti con key (anche notazione dot per proprietà nidificate) e label per le intestazioni.
   * @param fileName Il nome del file CSV da scaricare (senza estensione).
   */
  exportAsCsv(data: any[], headers: { key: string, label: string }[], fileName: string): void {
    const headerLabels = headers.map(h => h.label);
    const headerKeys = headers.map(h => h.key);
    const separator = ';'; // Usiamo il punto e virgola come separatore per compatibilità con Excel in Europa

    const csvContent = [
      headerLabels.join(separator),
      ...data.map(item =>
        headerKeys.map(key => {
          // Gestisce le proprietà nidificate con notazione dot (es. 'tipo.descrizione')
          const value = this.getNestedPropertyValue(item, key);
          // Gestisce valori null o undefined
          let formattedValue = value === null || value === undefined ? '' : String(value);
          // Se il valore contiene il separatore o virgolette, lo racchiudiamo tra virgolette
          if (formattedValue.includes(separator) || formattedValue.includes('"')) {
            // Escape delle virgolette raddoppiandole
            formattedValue = formattedValue.replace(/"/g, '""');
            formattedValue = `"${formattedValue}"`;
          }
          return formattedValue;
        }).join(separator)
      )
    ].join('\n');

    this.downloadCsv(csvContent, fileName);
  }

  /**
   * Recupera il valore di una proprietà nidificata da un oggetto usando la notazione dot.
   * @param obj L'oggetto da cui estrarre la proprietà.
   * @param path Il percorso della proprietà (es. 'tipo.descrizione').
   * @returns Il valore della proprietà o undefined se non trovata.
   */
  private getNestedPropertyValue(obj: any, path: string): any {
    if (!obj || !path) {
      return undefined;
    }
    
    const keys = path.split('.');
    let result = obj;
    
    for (const key of keys) {
      if (result === null || result === undefined) {
        return undefined;
      }
      result = result[key];
    }
    
    return result;
  }

  private downloadCsv(csvContent: string, fileName: string): void {
    // Creiamo un blob con i dati CSV
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    
    // Creiamo un link per scaricare il file
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `${fileName}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}