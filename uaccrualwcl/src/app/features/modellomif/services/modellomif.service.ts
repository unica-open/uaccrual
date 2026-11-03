/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { saveAs } from 'file-saver';
import { firstValueFrom } from 'rxjs';
import ExcelJS from 'exceljs';

export interface MifSaldo {
  codiceConto: string;
  importo: number;
}

export interface MifRettifica {
  codiceConto: string;
  importo: number;
}

export interface MifSaldoItas {
  codiceBilancioItas: string;
  importo: number;
}

export interface ModelloMifData {
  saldiFoglio1: MifSaldo[];
  rettificheFoglio2: MifRettifica[];
  saldiBilancioItas: MifSaldoItas[];
}

@Injectable({
  providedIn: 'root'
})
export class ModellomifService {

  private readonly baseUrl = '/accrualsbsrv/api/v1';
  private readonly TEMPLATE_PATH = './templates/Mod 2-a_Raccordo PdC eettDlgs118_2011_PdC unico_28012026 1.xlsx';

  private readonly FOGLIO_1_NAME = '1 PdC DLGS 118_2011_PE e PP';
  private readonly FOGLIO_2_NAME = '2 Pdc unico (segm A)';
  private readonly FOGLIO_3_NAME = '3 Stato Patrimoniale-All1ITAS1';
  private readonly FOGLIO_4_NAME = '4 Schema aggiuntivo SP';
  private readonly FOGLIO_5_NAME = '5 Conto Economico-All2 ITAS1';

  constructor(private http: HttpClient) {}

  private getCellText(cellValue: any): string | null {
    if (cellValue === null || cellValue === undefined) return null;
    if (typeof cellValue === 'string') return cellValue.trim();
    if (typeof cellValue === 'number') return cellValue.toString();
    if (typeof cellValue === 'object') {
      if (cellValue.richText) return cellValue.richText.map((rt: any) => rt.text).join('').trim();
      if (cellValue.result !== undefined) return cellValue.result?.toString()?.trim() ?? null;
      if (cellValue.formula) return null; // formula senza risultato calcolato
    }
    return cellValue.toString().trim();
  }

  async getAnniDisponibili(): Promise<{ anno: number; fase: string }[]> {
    return firstValueFrom(this.http.get<{ anno: number; fase: string }[]>(`${this.baseUrl}/modellomif/anni`));
  }

  async getDatiModelloMif(anno: number): Promise<ModelloMifData> {
    return firstValueFrom(this.http.get<ModelloMifData>(`${this.baseUrl}/modellomif/dati`, { params: { anno: anno.toString() } }));
  }

  async popolaTemplate(anno: number): Promise<Blob> {
    try {
      // 1. Fetch dei dati dal backend
      const dati = await this.getDatiModelloMif(anno);
      console.log('Dati ricevuti dal backend:', dati);
      console.log('Numero saldi Foglio 1:', dati.saldiFoglio1?.length);
      console.log('Numero rettifiche Foglio 2:', dati.rettificheFoglio2?.length);
      console.log('Numero saldi ITAS:', dati.saldiBilancioItas?.length);

      // Log dettagliati per diagnosticare i segni negativi
      if (dati.saldiFoglio1) {
        const saldiNegativi = dati.saldiFoglio1.filter(s => s.importo < 0);
        console.log('Saldi negativi nel Foglio 1:', saldiNegativi);
      }
      if (dati.rettificheFoglio2) {
        const rettificheNegative = dati.rettificheFoglio2.filter(r => r.importo < 0);
        console.log('Rettifiche negative nel Foglio 2:', rettificheNegative);
      }
      if (dati.saldiBilancioItas) {
        const saldiItasNegativi = dati.saldiBilancioItas.filter(s => s.importo < 0);
        console.log('Saldi ITAS negativi:', saldiItasNegativi);
      }

      // 2. Fetch del template come arrayBuffer
      const buffer = await firstValueFrom(
        this.http.get(this.TEMPLATE_PATH, {
          responseType: 'arraybuffer',
          observe: 'response'
        })
      );

      const contentType = buffer.headers.get('Content-Type');
      if (contentType && contentType.includes('text/html')) {
        throw new Error('Il file template non è stato trovato.');
      }

      // 3. Carica il workbook con ExcelJS
      const workbook = new ExcelJS.Workbook();
      await workbook.xlsx.load(buffer.body as ArrayBuffer);

      // 4. Popola Foglio 1 - Colonna H (SALDO CONTABILE)
      // NOTA: Il codice conto è nella colonna C (cella 3), non nella colonna A
      const ws1 = workbook.getWorksheet(this.FOGLIO_1_NAME);
      console.log('Foglio 1 trovato:', ws1 ? 'SI' : 'NO');
      console.log('Nome foglio cercato:', this.FOGLIO_1_NAME);
      console.log('Fogli disponibili:', workbook.worksheets.map(ws => ws.name));
      
      if (ws1 && dati.saldiFoglio1) {
        // Raccoglie tutti i codici validi presenti nel template (colonna C)
        const validCodes = new Set<string>();
        ws1.eachRow((row, rowNumber) => {
          if (rowNumber >= 5) {
            const code = this.getCellText(row.getCell(3).value);
            if (code) validCodes.add(code);
          }
        });
        console.log('Codici validi nel template Foglio 1:', validCodes.size);

        // Per ogni saldo: abbinamento diretto, oppure risale la gerarchia (.xxx)
        // finché trova un codice presente nel template (rollup conti non di legge)
        const saldoAccumulato = new Map<string, number>();
        let nonAbbinati = 0;
        for (const saldo of dati.saldiFoglio1) {
          let codice: string | null = saldo.codiceConto;
          let trovato = false;
          while (codice) {
            if (validCodes.has(codice)) {
              saldoAccumulato.set(codice, (saldoAccumulato.get(codice) ?? 0) + saldo.importo);
              trovato = true;
              break;
            }
            const lastDot = codice.lastIndexOf('.');
            if (lastDot === -1) break;
            codice = codice.substring(0, lastDot);
          }
          if (!trovato) nonAbbinati++;
        }
        console.log('Saldi accumulati per', saldoAccumulato.size, 'conti template;', nonAbbinati, 'saldi senza corrispondenza');

        let matchCount = 0;
        ws1.eachRow((row, rowNumber) => {
          if (rowNumber >= 5) {
            const cellC = row.getCell(3);
            const cellH = row.getCell(8);
            const codice = this.getCellText(cellC.value);
            if (codice && saldoAccumulato.has(codice)) {
              cellH.value = saldoAccumulato.get(codice);
              matchCount++;
              if (matchCount <= 5) {
                console.log(`Match #${matchCount} riga ${rowNumber}: ${codice} = ${cellH.value}`);
              }
            }
          }
        });
        console.log('Totale match Foglio 1:', matchCount);
      }

      // 5. Popola Foglio 2 - Colonna L (EVENTUALI RETTIFICHE/INTEGRAZIONI +/-)
      // Colonna F = Codice conto, Colonna L = Rettifiche/Integrazioni
      const ws2 = workbook.getWorksheet(this.FOGLIO_2_NAME);
      console.log('Foglio 2 trovato:', ws2 ? 'SI' : 'NO');
      console.log('Numero rettifiche:', dati.rettificheFoglio2?.length ?? 0);

      if (ws2 && dati.rettificheFoglio2 && dati.rettificheFoglio2.length > 0) {
        const rettificheMap = new Map(dati.rettificheFoglio2.map(r => [r.codiceConto, r.importo]));
        console.log('Mappa rettifiche creata con', rettificheMap.size, 'elementi');
        console.log('Codici rettifiche dal backend (TUTTI):', Array.from(rettificheMap.keys()));
        console.table(dati.rettificheFoglio2.map(r => ({ codiceConto: r.codiceConto, importo: r.importo })));

        // Raccogli TUTTI i codici dal foglio 2 per confronto diagnostico
        const codiciTemplate: string[] = [];
        ws2.eachRow((row, rowNumber) => {
          if (rowNumber >= 4) {
            const cellF = row.getCell(6);
            const t = this.getCellText(cellF.value);
            if (t) codiciTemplate.push(t);
          }
        });
        console.log('Codici colonna F del template (TUTTI):', codiciTemplate);

        // Log confronto: quali codici backend sono presenti/assenti nel template
        const codiciTemplateSet = new Set(codiciTemplate);
        const codiciBackend = Array.from(rettificheMap.keys());
        const presenti = codiciBackend.filter(c => codiciTemplateSet.has(c));
        const assenti = codiciBackend.filter(c => !codiciTemplateSet.has(c));
        console.log('Codici backend TROVATI nel template:', presenti);
        console.log('Codici backend NON trovati nel template:', assenti);

        let matchCount = 0;
        ws2.eachRow((row, rowNumber) => {
          if (rowNumber >= 4) {
            const cellF = row.getCell(6);  // Colonna F - Codice conto
            const cellL = row.getCell(12); // Colonna L - Eventuali Rettifiche/Integrazioni (+/-)
            const codice = this.getCellText(cellF.value);
            if (codice) {
              if (rettificheMap.has(codice)) {
                cellL.value = rettificheMap.get(codice);
                matchCount++;
                console.log(`Foglio2 match riga ${rowNumber}: ${codice} = ${rettificheMap.get(codice)}`);
              }
            }
          }
        });
        console.log('Totale match Foglio 2:', matchCount);
      } else if (ws2) {
        console.log('Nessuna rettifica da popolare nel Foglio 2');
      }

      // 6. Popola Fogli 3, 4, 5 (Saldi Bilancio ITAS - Anno precedente)
      const fogliItas = [this.FOGLIO_3_NAME, this.FOGLIO_4_NAME, this.FOGLIO_5_NAME];
      if (dati.saldiBilancioItas) {
        const itasMap = new Map(dati.saldiBilancioItas.map(s => [s.codiceBilancioItas, s.importo]));
        fogliItas.forEach(nomeFoglio => {
          const ws = workbook.getWorksheet(nomeFoglio);
          if (ws) {
            ws.eachRow((row, rowNumber) => {
              // Ipotizziamo che la colonna con il codice ITAS sia la A
              // e la colonna da popolare sia la C (Anno precedente)
              // Queste colonne andrebbero verificate sul template reale
              const cellA = row.getCell(1);
              const cellC = row.getCell(3);
              const codice = this.getCellText(cellA.value);
              if (codice) {
                if (itasMap.has(codice)) {
                  cellC.value = itasMap.get(codice);
                }
              }
            });
          }
        });
      }

      // 7. Scrivi il workbook
      const bufferOut = await workbook.xlsx.writeBuffer();

      return new Blob([bufferOut], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      
    } catch (error) {
      console.error('Errore durante il popolamento del template:', error);
      throw error;
    }
  }

  async scaricaModello(anno: number, codiceFiscaleEnte: string): Promise<void> {
    const blob = await this.popolaTemplate(anno);
    const filename = codiceFiscaleEnte
      ? `Modello_MEF_compilato_${anno}_${codiceFiscaleEnte}.xlsx`
      : `Modello_MEF_compilato_${anno}.xlsx`;
    saveAs(blob, filename);
  }
}
