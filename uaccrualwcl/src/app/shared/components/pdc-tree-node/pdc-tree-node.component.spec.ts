/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PdcTreeNodeComponent } from './pdc-tree-node.component';
import { PdcTreeNode } from '../../../features/pdc/model/pdc.model';

describe('PdcTreeNodeComponent', () => {
  let component: PdcTreeNodeComponent;
  let fixture: ComponentFixture<PdcTreeNodeComponent>;

  const mockNode: PdcTreeNode = {
    id: '1',
    codice: '1.1',
    descrizione: 'Conto di test',
    foglia: true,
    livello: 1,
    children: [],
    modificabile: true,
    eliminabile: true,
    inseribile_figlio: false
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PdcTreeNodeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PdcTreeNodeComponent);
    component = fixture.componentInstance;
    component.node = mockNode;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with node input', () => {
    expect(component.node).toEqual(mockNode);
  });

  it('should toggle expand', () => {
    expect(component.isExpanded).toBe(false);
    
    component.toggleExpand();
    expect(component.isExpanded).toBe(true);
    
    component.toggleExpand();
    expect(component.isExpanded).toBe(false);
  });

  it('should select account and emit event', () => {
    spyOn(component.accountSelected, 'emit');
    
    component.selectAccount();
    
    expect(component.isSelected).toBe(true);
    expect(component.accountSelected.emit).toHaveBeenCalledWith(mockNode);
  });

  it('should match search term', () => {
    expect(component.matchesSearch(mockNode, 'conto')).toBe(true);
    expect(component.matchesSearch(mockNode, '1.1')).toBe(true);
    expect(component.matchesSearch(mockNode, 'nonesistente')).toBe(false);
  });

  it('should return true for empty search term', () => {
    expect(component.matchesSearch(mockNode, '')).toBe(true);
  });

  it('should check if node is leaf', () => {
    expect(component.isLeaf()).toBe(true);
    
    const nonLeafNode: PdcTreeNode = {
      ...mockNode,
      foglia: false,
      children: [mockNode]
    };
    component.node = nonLeafNode;
    
    expect(component.isLeaf()).toBe(false);
  });

  it('should show node when no search term', () => {
    component.searchTerm = '';
    expect(component.shouldShowNode()).toBe(true);
  });

  it('should show node when it matches search term', () => {
    component.searchTerm = 'conto';
    expect(component.shouldShowNode()).toBe(true);
  });

  it('should hide node when it does not match and has no matching children', () => {
    component.searchTerm = 'nonesistente';
    expect(component.shouldShowNode()).toBe(false);
  });

  it('should show node when it has matching children', () => {
    const childNode: PdcTreeNode = {
      id: '2',
      codice: '1.2',
      descrizione: 'Figlio corrispondente',
      foglia: true,
      livello: 2,
      children: [],
      modificabile: true,
      eliminabile: true,
      inseribile_figlio: false
    };
    
    component.node = {
      ...mockNode,
      foglia: false,
      children: [childNode]
    };
    component.searchTerm = 'corrispondente';
    
    expect(component.shouldShowNode()).toBe(true);
  });
});
