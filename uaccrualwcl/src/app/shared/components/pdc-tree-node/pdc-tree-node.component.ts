/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PdcTreeNode } from '../../../features/pdc/model/pdc.model';

@Component({
  selector: 'app-pdc-tree-node',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pdc-tree-node.component.html',
  styleUrls: ['./pdc-tree-node.component.scss']
})
export class PdcTreeNodeComponent {
  @Input() node!: PdcTreeNode;
  @Input() searchTerm: string = '';
  @Output() accountSelected = new EventEmitter<PdcTreeNode>();

  isExpanded = false;
  isSelected = false;

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded;
  }

  selectAccount(): void {
    this.isSelected = true;
    this.accountSelected.emit(this.node);
  }

  // Metodo per verificare se un nodo corrisponde al termine di ricerca
  matchesSearch(node: PdcTreeNode, term: string): boolean {
    if (!term) return true;
    const lowerTerm = term.toLowerCase();
    return (
      node.codice.toLowerCase().includes(lowerTerm) ||
      node.descrizione.toLowerCase().includes(lowerTerm)
    );
  }

  // Metodo per verificare se un nodo ha figli corrispondenti
  hasMatchingChildren(node: PdcTreeNode, term: string): boolean {
    if (!node.children || node.children.length === 0) {
      return false;
    }

    return node.children.some(child =>
      this.matchesSearch(child, term) || this.hasMatchingChildren(child, term)
    );
  }

  // Metodo per determinare se mostrare un nodo
  shouldShowNode(): boolean {
    // Se non c'è termine di ricerca, mostra il nodo e i suoi figli
    if (!this.searchTerm) {
      return true;
    }

    // Se il nodo corrisponde direttamente, mostralo
    if (this.matchesSearch(this.node, this.searchTerm)) {
      return true;
    }

    // Se ha figli corrispondenti, mostralo
    if (this.hasMatchingChildren(this.node, this.searchTerm)) {
      return true;
    }

    // Altrimenti nascondilo
    return false;
  }

  // Metodo per verificare se il nodo è una foglia
  isLeaf(): boolean {
    return !!(this.node.foglia || (this.node as any).fogliaNonFoglia);
  }
}
