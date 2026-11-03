/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Page } from '../../../../shared/models/page.model';
import { PrimaNotaDetail, PrimaNotaContoCollegato, PrimaNotaContoCollegatoPage, PrimaNotaAttributoPage, PrimaNotaCollegataPage, ContiTotali } from '../../models/primanota.model';
import { PrimanotaService } from '../../../../core/services/primanota.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AmountFormatPipe } from '../../../../shared/pipes/amount-format.pipe';

@Component({
  selector: 'app-pn-detail-view',
  standalone: true,
  imports: [CommonModule, FormsModule, AmountFormatPipe],
  templateUrl: './pn-detail-view.component.html',
  styleUrls: ['./pn-detail-view.component.scss'],
})
export class PnDetailViewComponent implements OnInit, OnDestroy {
  selectedTab: string = 'testata';
  primaNota: PrimaNotaDetail | null = null;
  isLoading: boolean = false;
  errorMessage: string | null = null;

  // Proprietà per la paginazione dei tab
  currentPage: number = 0;
  pageSize: number = 10;
  pageSizeOptions: number[] = [10, 20, 50, 100];

  // Conti
  contiPage: Page<PrimaNotaContoCollegatoPage> | null = null;
  contiLoading: boolean = false;
  contoFilterValue: string = '';
  contiCollegatiDisplay: PrimaNotaContoCollegatoPage[] = [];

  // Totali conti (caricati una sola volta)
  totaleDareGenerale: number = 0;
  totaleAvereGenerale: number = 0;
  totaliLoaded: boolean = false;

  // Attributi
  attributiPage: Page<PrimaNotaAttributoPage> | null = null;
  attributiLoading: boolean = false;

  // Prime Note Collegate
  primeNoteCollegatePage: Page<PrimaNotaCollegataPage> | null = null;
  primeNoteCollegateLoading: boolean = false;

  // Parametri della prima nota corrente
  private currentAnno: number = 0;
  private currentNumero: number = 0;

  // Expose Math for template
  Math = Math;

  private paramSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private primanotaService: PrimanotaService
  ) {}

  ngOnInit(): void {
    this.paramSub = this.route.paramMap.subscribe(params => {
      const anno = Number(params.get('anno'));
      const numero = Number(params.get('numero'));

      if (anno && numero) {
        this.resetState();
        this.currentAnno = anno;
        this.currentNumero = numero;

        this.isLoading = true;
        this.primanotaService.getByAnnoAndNumero(anno, numero).subscribe({
          next: (detail: PrimaNotaDetail) => {
            this.primaNota = detail;
            this.isLoading = false;
          },
          error: (error: any) => {
            console.error('Errore durante il caricamento della prima nota:', error);
            this.errorMessage = 'Errore durante il caricamento dei dati. Riprovare.';
            this.isLoading = false;
            this.router.navigate(['/primanota/results']);
          }
        });
      } else {
        this.router.navigate(['/primanota/results']);
      }
    });
  }

  ngOnDestroy(): void {
    this.paramSub?.unsubscribe();
  }

  private resetState(): void {
    this.primaNota = null;
    this.errorMessage = null;
    this.selectedTab = 'testata';
    this.currentPage = 0;
    this.contiPage = null;
    this.contiCollegatiDisplay = [];
    this.contoFilterValue = '';
    this.attributiPage = null;
    this.primeNoteCollegatePage = null;
    this.totaleDareGenerale = 0;
    this.totaleAvereGenerale = 0;
    this.totaliLoaded = false;
  }

  selectTab(tab: string): void {
    this.selectedTab = tab;
    this.currentPage = 0; // Reset pagina quando si cambia tab

    // Carica i dati solo alla selezione del tab
    if (tab === 'conti' && !this.contiPage) {
      this.loadConti();
      this.loadTotaliConti(); // Carica i totali una sola volta all'apertura del tab
    } else if (tab === 'attributi' && !this.attributiPage) {
      this.loadAttributi();
    } else if (tab === 'noteCollegate' && !this.primeNoteCollegatePage) {
      this.loadPrimeNoteCollegate();
    }
  }

  // Metodi per caricare i dati paginati

  loadConti(page: number = 0): void {
    this.contiLoading = true;
    this.primanotaService.getContiCollegati(
      this.currentAnno, 
      this.currentNumero, 
      page, 
      this.pageSize,
      this.contoFilterValue || undefined  // Passa il filtro solo se valorizzato
    )
      .subscribe({
        next: (response: Page<PrimaNotaContoCollegatoPage>) => {
          this.contiPage = response;
          this.contiCollegatiDisplay = response.content;
          this.currentPage = page;
          this.contiLoading = false;
        },
        error: (error: any) => {
          console.error('Errore durante il caricamento dei conti:', error);
          this.contiLoading = false;
        }
      });
  }

  loadTotaliConti(): void {
    if (this.totaliLoaded) {
      return; // Evita di ricaricare i totali se già caricati
    }
    
    this.primanotaService.getTotaliConti(this.currentAnno, this.currentNumero)
      .subscribe({
        next: (totali: ContiTotali) => {
          this.totaleDareGenerale = totali.totaleDare;
          this.totaleAvereGenerale = totali.totaleAvere;
          this.totaliLoaded = true;
        },
        error: (error: any) => {
          console.error('Errore durante il caricamento dei totali:', error);
        }
      });
  }

  loadAttributi(page: number = 0): void {
    this.attributiLoading = true;
    this.primanotaService.getAttributiCollegati(this.currentAnno, this.currentNumero, page, this.pageSize)
      .subscribe({
        next: (response: Page<PrimaNotaAttributoPage>) => {
          this.attributiPage = response;
          this.currentPage = page;
          this.attributiLoading = false;
        },
        error: (error: any) => {
          console.error('Errore durante il caricamento degli attributi:', error);
          this.attributiLoading = false;
        }
      });
  }

  loadPrimeNoteCollegate(page: number = 0): void {
    this.primeNoteCollegateLoading = true;
    this.primanotaService.getPrimeNoteCollegate(this.currentAnno, this.currentNumero, page, this.pageSize)
      .subscribe({
        next: (response: Page<PrimaNotaCollegataPage>) => {
          this.primeNoteCollegatePage = response;
          this.currentPage = page;
          this.primeNoteCollegateLoading = false;
        },
        error: (error: any) => {
          console.error('Errore durante il caricamento delle prime note collegate:', error);
          this.primeNoteCollegateLoading = false;
        }
      });
  }

  // Metodi per la paginazione

  onContiPageChange(page: number): void {
    this.loadConti(page);
  }

  onAttributiPageChange(page: number): void {
    this.loadAttributi(page);
  }

  onPrimeNoteCollegatePageChange(page: number): void {
    this.loadPrimeNoteCollegate(page);
  }

  onPageSizeChange(event: Event): void {
    const newSize = +(event.target as HTMLSelectElement).value;
    this.pageSize = newSize;
    this.currentPage = 0;

    // Ricarica i dati del tab corrente con la nuova dimensione
    if (this.selectedTab === 'conti') {
      this.loadConti(0);
    } else if (this.selectedTab === 'attributi') {
      this.loadAttributi(0);
    } else if (this.selectedTab === 'noteCollegate') {
      this.loadPrimeNoteCollegate(0);
    }
  }

  // Filtro conti (lato server)
  applyContoFilter(): void {
    this.currentPage = 0;  // Reset alla prima pagina quando si applica un filtro
    this.loadConti(0);     // Chiama il backend con il filtro
  }

  resetContoFilter(): void {
    this.contoFilterValue = '';  // Pulisce il campo di filtro
    this.currentPage = 0;
    this.loadConti(0);  // Ricarica tutti i conti senza filtro
  }

  getTotalDare(): number {
    return this.totaleDareGenerale;  // Usa il totale generale caricato una sola volta
  }

  getTotalAvere(): number {
    return this.totaleAvereGenerale;  // Usa il totale generale caricato una sola volta
  }

  // Helper per la paginazione
  get totalPages(): number {
    const page = this.getCurrentPageData();
    return page ? page.totalPages : 0;
  }

  get totalElements(): number {
    const page = this.getCurrentPageData();
    return page ? page.totalElements : 0;
  }

  private getCurrentPageData(): Page<any> | null {
    if (this.selectedTab === 'conti') return this.contiPage;
    if (this.selectedTab === 'attributi') return this.attributiPage;
    if (this.selectedTab === 'noteCollegate') return this.primeNoteCollegatePage;
    return null;
  }

  getPages(): number[] {
    const pages: number[] = [];
    const maxPagesToShow = 5;
    const current = this.currentPage;
    const total = this.totalPages;

    if (total <= maxPagesToShow) {
      for (let i = 0; i < total; i++) {
        pages.push(i);
      }
    } else {
      let startPage = Math.max(0, current - Math.floor(maxPagesToShow / 2));
      let endPage = Math.min(total - 1, startPage + maxPagesToShow - 1);

      if (endPage - startPage + 1 < maxPagesToShow) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
      }

      if (startPage > 0) {
        pages.push(0);
        if (startPage > 1) pages.push(-1);
      }

      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }

      if (endPage < total - 1) {
        if (endPage < total - 2) pages.push(-1);
        pages.push(total - 1);
      }
    }

    return pages;
  }

  onBack(): void {
    this.router.navigate(['/primanota/results']);
  }
}
