/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Store } from '@ngxs/store';
import { Observable, Subscription } from 'rxjs';
import { FlussoService } from '../../../../core/services/flusso.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { take, pairwise, filter } from 'rxjs/operators';
import { ContextState } from '../../../../core/store/context/context.state';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule } from '@angular/material/dialog';

import { FlussoDetail, EsitoElaborazione } from '../../model/flusso.model';

@Component({
  selector: 'app-flusso-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule // Aggiunto MatDialogModule
  ],
  templateUrl: './flusso-detail.component.html',
  styleUrls: ['./flusso-detail.component.scss']
})
export class FlussoDetailComponent implements OnInit, OnDestroy {

  flussoId: string | null = null;
  flusso: FlussoDetail | null = null;
  loading: boolean = false;
  canElabFlusso$!: Observable<boolean>;
  private progressSub?: Subscription;

  // Proprietà per la paginazione degli esiti
  esitiCurrentPage: number = 0;
  esitiPageSize: number = 10;
  public Math = Math;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private flussoService: FlussoService,
    private datePipe: DatePipe,
    private store: Store,
    private notificationService: NotificationService
  ) {
    this.canElabFlusso$ = this.store.select(ContextState.canElabFlusso);
    const perms = this.store.selectSnapshot(ContextState.getUserPermissions);
    console.log('[FlussoDetail] constructor - permessi nello store:', perms.length, 'canElabFlusso:', perms.includes('OP-SPE-elaboraFile'));
  }

  ngOnInit(): void {
    this.activatedRoute.paramMap.pipe(take(1)).subscribe(params => {
      this.flussoId = params.get('id');
      if (this.flussoId) {
        this.loadFlussoDetail(this.flussoId);
      } else {
        this.router.navigate(['/flusso/search']);
      }
    });

    // Ricarica quando l'elaborazione (o il resume) finisce: progress passa da valore → null
    this.progressSub = this.flussoService.elaborationProgress$.pipe(
      pairwise(),
      filter(([prev, curr]) => prev !== null && curr === null)
    ).subscribe(() => {
      if (this.flussoId) this.loadFlussoDetail(this.flussoId);
    });
  }

  ngOnDestroy(): void {
    this.progressSub?.unsubscribe();
  }

  loadFlussoDetail(id: string): void {
    this.loading = true;
    this.flussoService.getFlussoById(id).subscribe({
      next: (data) => {
        this.flusso = data;
        this.loading = false;
        console.log('[FlussoDetail] flusso caricato - stato:', JSON.stringify(data.stato), '- canElaborare (stato check):', data.stato === 'Da elaborare');
        const perms = this.store.selectSnapshot(ContextState.getUserPermissions);
        console.log('[FlussoDetail] permessi al caricamento:', perms.includes('OP-SPE-elaboraFile'), '- totale:', perms.length);
      },
      error: (err) => {
        console.error('Error fetching flusso detail:', err);
        this.loading = false;
        // Optionally, navigate to an error page or show a notification
        this.router.navigate(['/error']); // Example
      }
    });
  }

  /**
   * Converte la data di elaborazione in timestamp per l'ordinamento.
   * Le date mancanti o non parsabili finiscono in testa, senza rompere il sort.
   */
  private getTimestampEsito(esito: EsitoElaborazione): number {
    if (!esito?.dataElaborazione) {
      return 0;
    }
    const t = new Date(esito.dataElaborazione).getTime();
    return isNaN(t) ? 0 : t;
  }

  /**
   * Restituisce gli esiti delle elaborazioni in ordine cronologico crescente, dal più vecchio
   * al più recente: il log si legge dall'inizio dell'elaborazione alla fine.
   *
   * È la stessa sequenza usata sia per la tabella a video sia per l'estrazione CSV. Il sort è
   * stabile, quindi le righe con lo stesso istante mantengono l'ordine deciso dal backend.
   */
  getEsitiOrdinati(): EsitoElaborazione[] {
    if (!this.flusso?.esitiElaborazione) {
      return [];
    }
    return [...this.flusso.esitiElaborazione]
      .sort((a, b) => this.getTimestampEsito(a) - this.getTimestampEsito(b));
  }

  /**
   * Restituisce gli esiti paginati
   */
  getEsitiPaginati(): EsitoElaborazione[] {
    const esitiOrdinati = this.getEsitiOrdinati();
    const start = this.esitiCurrentPage * this.esitiPageSize;
    const end = start + this.esitiPageSize;
    return esitiOrdinati.slice(start, end);
  }

  /**
   * Restituisce il numero totale di pagine
   */
  getTotalEsitiPages(): number {
    if (!this.flusso?.esitiElaborazione) {
      return 0;
    }
    return Math.ceil(this.flusso.esitiElaborazione.length / this.esitiPageSize);
  }

  /**
   * Gestisce il cambio della dimensione della pagina
   */
  onEsitiPageSizeChange(): void {
    this.esitiCurrentPage = 0; // Reset alla prima pagina
  }

  /**
   * Restituisce l'array di pagine per il paginator
   */
  getEsitiPages(): (number | -1)[] {
    const pages: (number | -1)[] = [];
    const totalPages = this.getTotalEsitiPages();
    
    if (totalPages <= 7) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(0);
      if (this.esitiCurrentPage > 3) {
        pages.push(-1);
      }
      for (let i = Math.max(1, this.esitiCurrentPage - 1); i <= Math.min(totalPages - 2, this.esitiCurrentPage + 1); i++) {
        pages.push(i);
      }
      if (this.esitiCurrentPage < totalPages - 4) {
        pages.push(-1);
      }
      pages.push(totalPages - 1);
    }
    return pages;
  }

  getEsitoBadgeClass(esito: string): string {
    const v = (esito || '').toUpperCase();
    if (v === 'SUCCESS' || v === 'OK')      return 'badge-success';
    if (v === 'WARNING' || v === 'WARN')    return 'badge-warning';
    if (v === 'ERROR' || v === 'KO' || v === 'FAILED') return 'badge-danger';
    return 'badge-neutral';
  }

  onBack(): void {
    this.activatedRoute.queryParams.pipe(take(1)).subscribe(params => {
      const returnParams: any = {};
      if (params['retPage'] != null) returnParams.page = params['retPage'];
      if (params['retPageSize'] != null) returnParams.pageSize = params['retPageSize'];
      if (params['retCodiceFile']) returnParams.codiceFile = params['retCodiceFile'];
      if (params['retFonte']) returnParams.fonte = params['retFonte'];
      if (params['retTipoFile']) returnParams.tipoFile = params['retTipoFile'];
      if (params['retDataCaricamento']) returnParams.dataCaricamento = params['retDataCaricamento'];
      this.router.navigate(['/flusso/results'], { queryParams: returnParams });
    });
  }

  onExport(): void {
    if (this.flussoId && this.flusso?.esitiElaborazione) {
      // Prepara i dati per l'export CSV con colonne separate, nello stesso
      // ordine (data crescente) mostrato nella tabella a video
      const esitiOrdinati = this.getEsitiOrdinati();

      const headers = [
        { key: 'dataElaborazione', label: 'Data Elaborazione' },
        { key: 'esito', label: 'Esito' },
        { key: 'messaggio', label: 'Messaggio' }
      ];
      
      // Usa il service per esportare come CSV
      const csvContent = this.buildCsvContent(esitiOrdinati, headers);
      this.downloadCsv(csvContent, `esito_elaborazione_${this.flusso.codice || this.flussoId}.csv`);
    }
  }

  /**
   * Costruisce il contenuto CSV dai dati forniti
   */
  private buildCsvContent(data: any[], headers: { key: string, label: string }[]): string {
    const separator = ';'; // Usiamo il punto e virgola per compatibilità con Excel in Europa
    const headerLabels = headers.map(h => h.label);
    const headerKeys = headers.map(h => h.key);

    const csvLines = [
      headerLabels.join(separator),
      ...data.map(item =>
        headerKeys.map(key => {
          let value = item[key];
          // Gestisce valori null o undefined
          value = value === null || value === undefined ? '' : String(value);
          // Se il valore contiene il separatore o virgolette, lo racchiudiamo tra virgolette
          if (value.includes(separator) || value.includes('"')) {
            // Escape delle virgolette raddoppiandole
            value = value.replace(/"/g, '""');
            value = `"${value}"`;
          }
          return value;
        }).join(separator)
      )
    ];

    return csvLines.join('\n');
  }

  /**
   * Scarica il contenuto CSV come file
   */
  private downloadCsv(csvContent: string, fileName: string): void {
    // Aggiungiamo il BOM per la corretta visualizzazione dei caratteri speciali in Excel
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', fileName);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  onElaborate(confirmedOverride: boolean = false): void {
    if (this.flussoId) {
      this.loading = true;
      this.flussoService.elaboraFlusso(this.flussoId!, confirmedOverride).subscribe({
        next: (response) => {
          this.loading = false;
          if (response?.tipoMessaggio === 'ERROR') {
            const msg = response.flussoElaboraResponse?.message || 'Elaborazione terminata con errori.';
            this.notificationService.error(msg);
          }
          this.loadFlussoDetail(this.flussoId!);
        },
        error: (err) => {
          this.loading = false;
          console.error('Error elaborating flusso:', err);
          this.notificationService.error('Errore durante l\'elaborazione del flusso.');
          this.loadFlussoDetail(this.flussoId!);
        }
      });
    }
  }
}

