/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PdcService } from '../../../../core/services/pdc.service';
import { PdcTreeNode } from '../../../pdc/model/pdc.model';
import { PdcTreeNodeComponent } from '../../../../shared/components/pdc-tree-node/pdc-tree-node.component';

@Component({
  selector: 'app-pdc-tree-search-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, PdcTreeNodeComponent],
  templateUrl: './pdc-tree-search-modal.component.html',
  styleUrls: ['./pdc-tree-search-modal.component.scss']
})
export class PdcTreeSearchModalComponent implements OnInit {
  @Input() searchTerm: string = '';
  @Input() searchCodice: string = '';
  @Input() searchDescrizione: string = '';
  @Input() autoSelectSingleResult: boolean = true;
  @Output() pdcSelected = new EventEmitter<PdcTreeNode | null>();
  @Output() close = new EventEmitter<boolean>(); // Emesso quando la modale deve essere chiusa

  searchResults: PdcTreeNode[] = [];
  filteredResults: PdcTreeNode[] = [];
  loading = false;
  errorMessage: string | null = null;

  constructor(
    private pdcService: PdcService
  ) {}

  ngOnInit(): void {
    // Se abbiamo searchCodice o searchDescrizione espliciti, ignoriamo searchTerm
    const hasExplicitFilters = this.searchCodice.trim() || this.searchDescrizione.trim();
    const hasSearchTerm = this.searchTerm.trim();

    if (hasExplicitFilters || hasSearchTerm) {
      this.searchAccounts();
    } else {
      this.loadTreeData();
    }
  }

  loadTreeData(): void {
    this.loading = true;
    this.errorMessage = null;

    // Filtro per la ricerca - inizialmente vuoto per ottenere tutti i conti
    const filters = {};

    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data) => {
        this.searchResults = data;
        this.filteredResults = this.searchResults;
        this.loading = false;
      },
      error: (error) => {
        console.error('Errore nel caricamento dell\'albero dei conti:', error);
        this.errorMessage = 'Errore nel caricamento dei dati';
        this.loading = false;
      }
    });
  }

  searchAccounts(): void {
    const hasExplicitFilters = this.searchCodice.trim() || this.searchDescrizione.trim();

    // Se non ci sono filtri, mostra i risultati già caricati
    if (!hasExplicitFilters && !this.searchTerm.trim()) {
      this.filteredResults = this.searchResults;
      return;
    }

    this.loading = true;
    const filters: any = {};

    // Priorità ai filtri espliciti (searchCodice/searchDescrizione)
    if (hasExplicitFilters) {
      if (this.searchCodice.trim()) {
        filters.codice = this.searchCodice.trim();
      }
      if (this.searchDescrizione.trim()) {
        filters.descrizione = this.searchDescrizione.trim();
      }
    } else {
      // Fallback: searchTerm vecchio stile (cerca sia in codice che descrizione)
      filters.codice = this.searchTerm;
      filters.descrizione = this.searchTerm;
    }

    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data) => {
        this.searchResults = data;
        this.filteredResults = data;
        this.loading = false;

        // Se c'è un solo risultato, è una foglia e autoSelectSingleResult è true, selezionalo automaticamente
        if (this.autoSelectSingleResult && data.length === 1 && data[0].foglia) {
          this.selectAccount(data[0]);
        }
      },
      error: (error) => {
        console.error('Errore nella ricerca dei conti:', error);
        this.errorMessage = 'Errore nella ricerca dei dati';
        this.loading = false;
      }
    });
  }

  selectAccount(account: PdcTreeNode): void {
    this.pdcSelected.emit(account);
    this.close.emit(true); // Chiudi la modale dopo la selezione
  }

  dismiss(): void {
    this.pdcSelected.emit(null);
    this.close.emit(false); // Chiudi la modale senza selezione
  }

  // Metodo per filtrare ricorsivamente l'albero in base al termine di ricerca
  filterTree(nodes: PdcTreeNode[], term: string): PdcTreeNode[] {
    if (!term) {
      return nodes;
    }

    const lowerTerm = term.toLowerCase();
    
    const filterNode = (node: PdcTreeNode): PdcTreeNode | null => {
      // Controlla se il nodo corrente corrisponde al termine di ricerca
      const matches = node.codice.toLowerCase().includes(lowerTerm) || 
                     node.descrizione.toLowerCase().includes(lowerTerm);
      
      // Filtra ricorsivamente i figli
      const filteredChildren = (node.children || [])
        .map(child => filterNode(child))
        .filter(child => child !== null) as PdcTreeNode[];

      // Se il nodo corrisponde o ha figli corrispondenti, includilo nel risultato
      if (matches || filteredChildren.length > 0) {
        return {
          ...node,
          children: filteredChildren
        };
      }
      
      return null;
    };

    return nodes
      .map(node => filterNode(node))
      .filter(node => node !== null) as PdcTreeNode[];
  }
}
