/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnChanges, Output, EventEmitter, Input, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PrimaNotaSearchFilter } from '../../models/primanota.model';
import { PrimanotaService, DropdownOption, FormattedDropdownOption } from '../../../../core/services/primanota.service';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { PdcTreeSearchModalComponent } from '../pdc-tree-search-modal/pdc-tree-search-modal.component';
import { AmountInputDirective } from '../../../../shared/directives/amount-input.directive';
import { PdcService } from '../../../../core/services/pdc.service';
import { PdcTreeNode } from '../../../pdc/model/pdc.model';

@Component({
  selector: 'app-pn-search-filters',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, PdcTreeSearchModalComponent, AmountInputDirective],
  templateUrl: './pn-search-filters.component.html',
  styleUrls: ['./pn-search-filters.component.scss']
})
export class PnSearchFiltersComponent implements OnInit, OnChanges {
  @Input() initialFilter: Partial<PrimaNotaSearchFilter> | null = null;
  @Input() incompleteWarning: boolean | null = false;

  @Output() search = new EventEmitter<Partial<PrimaNotaSearchFilter>>();
  @Output() clear = new EventEmitter<void>();
  @Output() indietro = new EventEmitter<void>();

  searchForm!: FormGroup;

  // Proprietà per controllare la visibilità della modale di ricerca conti
  showPdcTreeSearchModal: boolean = false;
  searchTermForModal: string = '';
  searchCodiceForModal: string = '';
  searchDescrizioneForModal: string = '';

  // Arrays for dropdown data
  tipiPrimaNota: FormattedDropdownOption[] = [];
  fonti: FormattedDropdownOption[] = [];
  eventiContabili: FormattedDropdownOption[] = [];
  causali: FormattedDropdownOption[] = [];
  causaliFiltered: FormattedDropdownOption[] = []; // Causali filtrate per evento selezionato
  statiPrimaNota: FormattedDropdownOption[] = []; // Usa FormattedDropdownOption

  // Subject per gestire il debounce della selezione evento
  private eventoSelectionSubject = new Subject<string>();
  private fonteSelectionSubject = new Subject<string>();

  constructor(
    private fb: FormBuilder,
    private primanotaService: PrimanotaService,
    private pdcService: PdcService
  ) { }

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      codiceTipo: [''],
      codiceFonte: [''],
      codiceEvento: [''],
      codiceCausale: [''],
      numeroProvvisorio: [''],
      numeroDefinitivo: [''],
      codiceStato: [''],
      descrizione: [''],
      codiceConto: [''],
      descrizioneConto: [''],
      dataRegProvvisoriaDa: [''],
      dataRegProvvisoriaA: [''],
      dataRegDefinitivaDa: [''],
      dataRegDefinitivaA: [''],
      importoDa: [''],
      importoA: [''],
      soloNonQuadrate: [false],
    });

    if (this.initialFilter) {
      this.searchForm.patchValue(this.initialFilter, { emitEvent: false });
    }

    // Sottoscrizione per gestire il cambio di evento contabile con debounce
    this.searchForm.get('codiceEvento')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(idEvento => {
        if (idEvento) {
          // Se è selezionato un evento, carica le causali filtrate per quell'evento
          return this.primanotaService.getCausaliByEvento(idEvento);
        } else {
          // Se nessun evento è selezionato, mostra tutte le causali
          return this.primanotaService.getCausali();
        }
      })
    ).subscribe(
      causali => {
        this.causaliFiltered = causali;
        // Se la causale selezionata non è presente nelle causali filtrate, resettala
        const selectedCausale = this.searchForm.get('codiceCausale')?.value;
        if (selectedCausale && !this.causaliFiltered.some(c => c.value === selectedCausale)) {
          this.searchForm.get('codiceCausale')?.setValue(null);
        }
      },
      error => {
        console.error('Error loading filtered causali data:', error);
        this.causaliFiltered = [];
      }
    );

    // Sottoscrizione per gestire il cambio di fonte con debounce
    this.searchForm.get('codiceFonte')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(idFonte => {
        if (idFonte) {
          // Se è selezionata una fonte, carica gli eventi filtrati per quella fonte
          return this.primanotaService.getEventiContabiliByFonte(idFonte);
        } else {
          // Se nessuna fonte è selezionata, mostra tutti gli eventi
          return this.primanotaService.getEventiContabili();
        }
      })
    ).subscribe(
      eventi => {
        this.eventiContabili = eventi;
        // Se l'evento selezionato non è presente negli eventi filtrati, resettalo insieme alle causali
        const selectedEvento = this.searchForm.get('codiceEvento')?.value;
        if (selectedEvento && !this.eventiContabili.some(e => e.value === selectedEvento)) {
          this.searchForm.get('codiceEvento')?.setValue(null);
          this.causaliFiltered = this.causali; // Ripristina tutte le causali
        } else if (!selectedEvento) {
          // Se non c'è un evento selezionato (ad esempio quando si seleziona "tutti i valori" per la fonte), ripristina tutte le causali
          this.causaliFiltered = this.causali;
        }
      },
      error => {
        console.error('Error loading filtered eventi data:', error);
        this.eventiContabili = [];
      }
    );

    // Load dropdown data
    this.loadDropdownData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialFilter'] && this.searchForm) {
      this.searchForm.patchValue(changes['initialFilter'].currentValue, { emitEvent: false });
    }
  }

  loadDropdownData(): void {
    // Load all dropdown data
    this.primanotaService.getTipiPrimaNota().subscribe(
      data => {
        this.tipiPrimaNota = data;
      },
      error => {
        console.error('Error loading tipo prima nota data:', error);
        // Fallback to default option
        this.tipiPrimaNota = [];
      }
    );

    this.primanotaService.getFonti().subscribe(
      data => {
        this.fonti = data;
      },
      error => {
        console.error('Error loading fonte data:', error);
        // Fallback to default option
        this.fonti = [];
      }
    );

    this.primanotaService.getEventiContabili().subscribe(
      data => {
        this.eventiContabili = data;
        // Una volta caricati gli eventi, carica tutte le causali come fallback
        this.primanotaService.getCausali().subscribe(
          causali => {
            this.causali = causali;
            this.causaliFiltered = causali; // Inizialmente mostra tutte le causali
          },
          error => {
            console.error('Error loading causali data:', error);
            // Fallback to default option
            this.causali = [];
            this.causaliFiltered = [];
          }
        );
      },
      error => {
        console.error('Error loading evento contabile data:', error);
        // Fallback to default option
        this.eventiContabili = [];
      }
    );

    this.primanotaService.getStatiPrimaNota().subscribe(
      data => {
        this.statiPrimaNota = data;
      },
      error => {
        console.error('Error loading stato prima nota data:', error);
        // Fallback to default option
        this.statiPrimaNota = [];
      }
    );
  }

  onSearch(): void {
    const formValue = this.searchForm.value;
    
    // Converte le stringhe vuote a null per tutti i campi
    // Quando si seleziona 'Tutti i valori' nelle combo, il valore è '' ma deve essere passato come null
    const cleanedValue: Partial<PrimaNotaSearchFilter> = {};
    
    Object.keys(formValue).forEach(key => {
      const value = (formValue as any)[key];
      if (key === 'soloNonQuadrate') {
        (cleanedValue as any)[key] = value === true ? true : null;
      } else if (value === '' || value === undefined) {
        (cleanedValue as any)[key] = null;
      } else {
        (cleanedValue as any)[key] = value;
      }
    });
    
    this.search.emit(cleanedValue);
  }

  onClear(): void {
    this.clear.emit();
    // Reset form to clear selections - reimposta tutti i campi a stringa vuota
    this.searchForm.reset({
      codiceTipo: '',
      codiceFonte: '',
      codiceEvento: '',
      codiceCausale: '',
      numeroProvvisorio: '',
      numeroDefinitivo: '',
      codiceStato: '',
      descrizione: '',
      codiceConto: '',
      descrizioneConto: '',
      dataRegProvvisoriaDa: '',
      dataRegProvvisoriaA: '',
      dataRegDefinitivaDa: '',
      dataRegDefinitivaA: '',
      importoDa: '',
      importoA: '',
      soloNonQuadrate: false,
    });
    // Reload dropdowns to reset to default state
    this.loadDropdownData();
  }

  onSearchConto(): void {
    console.log('onSearchConto chiamato');
    // Imposta i termini di ricerca separatamente per codice e descrizione
    const searchCodice = this.searchForm.get('codiceConto')?.value || '';
    const searchDescrizione = this.searchForm.get('descrizioneConto')?.value || '';

    console.log('searchCodice:', searchCodice);
    console.log('searchDescrizione:', searchDescrizione);

    // Costruisci i filtri per la ricerca
    const filters: any = {};
    if (searchCodice.trim()) {
      filters.codice = searchCodice.trim();
    }
    if (searchDescrizione.trim()) {
      filters.descrizione = searchDescrizione.trim();
    }

    // Se non ci sono filtri, apri la modale per mostrare tutto l'albero
    if (!searchCodice.trim() && !searchDescrizione.trim()) {
      this.searchCodiceForModal = '';
      this.searchDescrizioneForModal = '';
      this.showPdcTreeSearchModal = true;
      return;
    }

    // Esegui la ricerca per determinare quanti risultati ci sono
    this.pdcService.searchPdcTree(filters).subscribe({
      next: (data) => {
        console.log('Risultati ricerca PDC:', data.length);

        if (data.length === 0) {
          // Nessun risultato: il PdcService imposta automaticamente noResults$ a true
          // Il messaggio WEB_INF_0003 verra' visualizzato automaticamente
        } else if (data.length === 1 && data[0].children &&  data[0].children.length === 0) {
          // Un solo risultato senza figli: selezione automatica
          const account = data[0];
          this.searchForm.patchValue({
            codiceConto: account.codice_ufficiale,
            descrizioneConto: account.descrizione
          });
          // Non aprire la modale
        } else {
          // Più risultati o un solo risultato non-foglia: apri la modale
          this.searchCodiceForModal = searchCodice;
          this.searchDescrizioneForModal = searchDescrizione;
          this.showPdcTreeSearchModal = true;
        }
      },
      error: (error) => {
        console.error('Errore nella ricerca dei conti:', error);
        // In caso di errore, apri comunque la modale per permettere all'utente di provare di nuovo
        this.searchCodiceForModal = searchCodice;
        this.searchDescrizioneForModal = searchDescrizione;
        this.showPdcTreeSearchModal = true;
      }
    });
  }

  // Metodo per gestire la selezione del conto dalla modale
  onPdcSelected(selectedAccount: any): void {
    if (selectedAccount) {
      // Aggiorna i campi del form con i dati del conto selezionato
      this.searchForm.patchValue({
        codiceConto: selectedAccount.codice_ufficiale,
        descrizioneConto: selectedAccount.descrizione
      });
    }

    // Chiudi la modale
    this.showPdcTreeSearchModal = false;
  }

  // Metodo per gestire la chiusura della modale
  onModalClosed(): void {
    this.showPdcTreeSearchModal = false;
  }
}
