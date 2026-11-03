/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NotificationService } from '../../../../core/services/notification.service';
import { PrimanotaService, FormattedDropdownOption } from '../../../../core/services/primanota.service';

/** Codice dello stato "Valido" delle causali (accrual_d_causale_stato) */
const STATO_CAUSALE_VALIDO = 'V';

/**
 * Componente per il Tab "Testata" del form Gestisci Prima Nota
 *
 * Gestisce i campi:
 * - Tipo, Fonte, Evento, Causale, Stato (dropdown)
 * - Numero Provvisorio, Data Reg. Provvisoria
 * - Numero Definitivo, Data Reg. Definitiva
 * - Descrizione
 *
 * Implementa la correlazione: Fonte → Evento → Causale
 *
 * In modalità modifica:
 * - Legge i valori dal form dopo che il padre lo ha popolato
 * - Carica le correlazioni mantenendo i valori esistenti
 */
@Component({
  selector: 'app-pn-tab-testata',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './pn-tab-testata.component.html',
  styleUrl: './pn-tab-testata.component.scss'
})
export class PnTabTestataComponent implements OnInit {

  /** Form group della testata (passato dal padre) */
  @Input() testataForm!: FormGroup;

  /** Flag per modalità modifica (true) o inserimento (false) */
  @Input() isModifica = false;

  /** Input per segnalare al componente figlio che il form è stato popolato (solo per modifica) */
  @Input() set formPopolato(value: boolean) {
    if (value && this.isModifica && this.dropdownCaricati) {
      setTimeout(() => {
        // In modifica, carica le correlazioni se le liste sono vuote
        if (this.eventi.length === 0 || this.causali.length === 0) {
          this.caricaCorrelazioniInModifica();
        }
        this.correlazioniCaricate = true;

        // In modifica, assicura che lo stato sia disabilitato
        const statoVal = this.testataForm?.get('stato')?.value;
        if (statoVal) {
          this.verificaStatoEditabile(statoVal);
        }
      }, 50);
    }
  }

  /** Evento emesso quando l'utente preme "Prosegui" */
  @Output() prosegui = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Annulla" */
  @Output() annulla = new EventEmitter<void>();

  /** Evento emesso quando l'utente preme "Indietro" */
  @Output() indietro = new EventEmitter<void>();

  /** Dropdown options - Tipo Prima Nota */
  tipi: FormattedDropdownOption[] = [];

  /** Dropdown options - Fonte */
  fonti: FormattedDropdownOption[] = [];

  /** Dropdown options - Evento Contabile */
  eventi: FormattedDropdownOption[] = [];

  /** Dropdown options - Causale */
  causali: FormattedDropdownOption[] = [];

  /** Dropdown options - Stato */
  stati: FormattedDropdownOption[] = [];

  /** Flag per indicare se lo stato è editabile (solo se PROVVISORIA) */
  statoEditabile = true;
  fonteEditabile = true;
  eventoEditabile = true;
  causaleEditabile = true;

  /** Flag per tracciare se le correlazioni sono già state caricate in modifica */
  private correlazioniCaricate = false;

  /** Flag per tracciare se i dropdown sono stati caricati */
  private dropdownCaricati = false;

  constructor(
    private primanotaService: PrimanotaService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.caricaDropdown();
    this.aggiornaStatoControlli();
  }

  /**
   * Gestisce lo stato abilitato/disabilitato dei controlli
   */
  private aggiornaStatoControlli(): void {
    if (!this.testataForm) return;

    // Tipo: sempre disabilitato
    this.testataForm.get('tipo')?.disable();

    if (this.isModifica) {
      // In modifica: Fonte, Evento, Causale disabilitati
      this.testataForm.get('fonte')?.disable();
      this.testataForm.get('evento')?.disable();
      this.testataForm.get('causale')?.disable();
    } else {
      // In inserimento: Fonte, Evento, Causale abilitati
      this.testataForm.get('fonte')?.enable();
      this.testataForm.get('evento')?.enable();
      this.testataForm.get('causale')?.enable();
    }
  }

  /**
   * Carica tutti i dropdown iniziali
   */
  caricaDropdown(): void {
    // Carica tipi
    this.primanotaService.getTipiPrimaNota().subscribe({
      next: (data: FormattedDropdownOption[]) => {
        this.tipi = data;
        // Se non è modifica, imposta Tipo a 'M' (Manuale)
        if (!this.isModifica && this.testataForm) {
          this.testataForm.patchValue({ tipo: 'M' });
        }
        this.checkDropdownCaricati();
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento tipi:', err);
        this.mostraErrore('Errore nel caricamento tipi');
      }
    });

    // Carica fonti
    this.primanotaService.getFonti().subscribe({
      next: (data: FormattedDropdownOption[]) => {
        this.fonti = data;
        this.checkDropdownCaricati();
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento fonti:', err);
        this.mostraErrore('Errore nel caricamento fonti');
      }
    });

    // Carica stati
    this.primanotaService.getStatiPrimaNota().subscribe({
      next: (data: FormattedDropdownOption[]) => {
        // Filtra lo stato ANNULLATA (codice 'A')
        this.stati = data.filter(s => s.value !== 'A');
        // In inserimento, lo stato verrà impostato quando si seleziona l'evento
        // In modifica, lo stato è già valorizzato dal form
        this.checkDropdownCaricati();
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento stati:', err);
        this.mostraErrore('Errore nel caricamento stati');
      }
    });
  }

  /**
   * Verifica se tutti i dropdown sono stati caricati
   */
  private checkDropdownCaricati(): void {
    // Verifica se tutti i dropdown sono stati caricati
    if (this.tipi.length > 0 && this.fonti.length > 0 && this.stati.length > 0) {
      this.dropdownCaricati = true;

      // In modifica, se il form è già popolato, carica le correlazioni
      if (this.isModifica && this.testataForm) {
        const fonteVal = this.testataForm.get('fonte')?.value;
        const eventoVal = this.testataForm.get('evento')?.value;
        if (fonteVal && eventoVal && (this.eventi.length === 0 || this.causali.length === 0)) {
          setTimeout(() => {
            this.caricaCorrelazioniInModifica();
            this.correlazioniCaricate = true;
          }, 50);
        }
        // In modifica, assicura che lo stato sia disabilitato
        const statoVal = this.testataForm.get('stato')?.value;
        if (statoVal) {
          this.verificaStatoEditabile(statoVal);
        }
      } else if (!this.isModifica && this.testataForm) {
        // In inserimento, se il form ha già un valore per lo stato (es. quando si torna indietro),
        // assicura che statoEditabile sia corretto
        const statoVal = this.testataForm.get('stato')?.value;
        if (statoVal) {
          this.verificaStatoEditabile(statoVal);
        }

        // Se il form ha valori per fonte e evento ma i dropdown sono vuoti,
        // significa che si è tornati indietro dal tab conti: ricaricare le correlazioni
        const fonteVal = this.testataForm.get('fonte')?.value;
        const eventoVal = this.testataForm.get('evento')?.value;
        if (fonteVal && eventoVal && this.eventi.length === 0) {
          setTimeout(() => {
            this.caricaCorrelazioniInInserimento();
          }, 50);
        }
      }
    }
  }

  /**
   * In modalità modifica, carica le correlazioni (Evento e Causale)
   * basandosi sui valori già presenti nel form
   */
  private caricaCorrelazioniInModifica(): void {
    if (!this.testataForm) {
      return;
    }

    const fonteVal = this.testataForm.get('fonte')?.value;

    if (fonteVal) {
      // Carica eventi per la fonte selezionata
      this.primanotaService.getEventiContabiliByFonte(fonteVal).subscribe({
        next: (eventi) => {
          this.eventi = eventi;
          const eventoVal = this.testataForm.get('evento')?.value;

          if (eventoVal) {
            // Mantiene l'evento esistente e carica le causali
            this.testataForm.patchValue({ evento: eventoVal });
            this.caricaCausaliPerEvento(eventoVal);
          }
        },
        error: (err) => {
          console.error('[PN-Tab-Testata] Errore nel caricamento eventi:', err);
          this.mostraErrore('Errore nel caricamento eventi contabili');
        }
      });
    }
  }

  /**
   * In inserimento sono selezionabili le sole causali valide; in modifica la causale non e'
   * modificabile e va mostrata comunque, anche se nel frattempo non e' piu' valida.
   */
  private filtroStatoCausale(): string | undefined {
    return this.isModifica ? undefined : STATO_CAUSALE_VALIDO;
  }

  /**
   * Carica le causali per un evento (usato in modifica)
   */
  private caricaCausaliPerEvento(eventoCodice: string): void {
    this.primanotaService.getCausaliByEvento(eventoCodice, this.filtroStatoCausale()).subscribe({
      next: (causali) => {
        this.causali = causali;
        const causaleVal = this.testataForm.get('causale')?.value;

        if (causaleVal) {
          // Mantiene la causale esistente
          this.testataForm.patchValue({ causale: causaleVal });
        }
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento causali:', err);
        this.mostraErrore('Errore nel caricamento causali');
      }
    });
  }

  /**
   * In modalità inserimento, carica le correlazioni (Evento e Causale)
   * quando si torna indietro dal tab Conti
   */
  private caricaCorrelazioniInInserimento(): void {
    if (!this.testataForm) {
      return;
    }

    const fonteVal = this.testataForm.get('fonte')?.value;

    if (fonteVal) {
      // Carica eventi per la fonte selezionata
      this.primanotaService.getEventiContabiliByFonte(fonteVal).subscribe({
        next: (eventi) => {
          this.eventi = eventi;
          const eventoVal = this.testataForm.get('evento')?.value;

          if (eventoVal) {
            // Mantiene l'evento esistente e carica le causali
            this.caricaCausaliPerEvento(eventoVal);

            // Assicura che statoEditabile sia corretto per l'evento selezionato
            const eventoSelezionato = this.eventi.find(e => e.value === eventoVal);
            if (eventoSelezionato?.statoDefaultPrimaNota) {
              this.verificaStatoEditabile(eventoSelezionato.statoDefaultPrimaNota);
            }
          }
        },
        error: (err) => {
          console.error('[PN-Tab-Testata] Errore nel caricamento eventi:', err);
          this.mostraErrore('Errore nel caricamento eventi contabili');
        }
      });
    }
  }

  /**
   * Gestisce il cambio della Fonte
   * Correlazione: Fonte → Evento Contabile
   */
  onFonteChange(fonteCodice: string): void {
    if (!fonteCodice) {
      // Se fonte vuota, resetta evento e causale
      this.eventi = [];
      this.causali = [];
      this.testataForm?.patchValue({ evento: null, causale: null });
      return;
    }

    this.primanotaService.getEventiContabiliByFonte(fonteCodice).subscribe({
      next: (data: FormattedDropdownOption[]) => {
        this.eventi = data;
        this.causali = [];
        // Resetta evento e causale solo se non siamo in modalità modifica
        if (!this.isModifica) {
          this.testataForm?.patchValue({ evento: null, causale: null });
        }
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento eventi contabili:', err);
        this.mostraErrore('Errore nel caricamento eventi contabili');
      }
    });
  }

  /**
   * Gestisce il cambio dell'Evento Contabile
   * Correlazione: Evento → Causale
   * Imposta anche lo stato di default dall'evento selezionato
   */
  onEventoChange(eventoCodice: string): void {
    if (!eventoCodice) {
      // Se evento vuoto, resetta causale
      this.causali = [];
      this.testataForm?.patchValue({ causale: null });
      return;
    }

    // Trova l'evento selezionato per ottenere lo stato di default
    const eventoSelezionato = this.eventi.find(e => e.value === eventoCodice);

    this.primanotaService.getCausaliByEvento(eventoCodice, this.filtroStatoCausale()).subscribe({
      next: (data: FormattedDropdownOption[]) => {
        this.causali = data;
        // Resetta causale solo se non siamo in modalità modifica
        if (!this.isModifica) {
          this.testataForm?.patchValue({ causale: null });

          // Imposta lo stato di default dall'evento selezionato
          if (eventoSelezionato?.statoDefaultPrimaNota) {
            this.testataForm?.patchValue({ stato: eventoSelezionato.statoDefaultPrimaNota });
            this.verificaStatoEditabile(eventoSelezionato.statoDefaultPrimaNota);
          }
        }
      },
      error: (err) => {
        console.error('[PN-Tab-Testata] Errore nel caricamento causali:', err);
        this.mostraErrore('Errore nel caricamento causali');
      }
    });
  }

  /**
   * Verifica se lo stato è editabile
   * In modifica: MAI editabile (lo stato è una proprietà intrinseca della prima nota)
   * In inserimento: NON editabile se stato = P (PROVVISORIA - stato di default dell'evento)
   */
  verificaStatoEditabile(stato: string): void {
    // Esegui nella NgZone per assicurarsi che il change detection funzioni
    this.ngZone.run(() => {
      // In modifica, lo stato non è mai editabile
      if (this.isModifica) {
        this.statoEditabile = false;
        this.cdr.detectChanges();
        return;
      }

      // In inserimento, NON editabile se P (PROVVISORIA - stato di default dell'evento)
      this.statoEditabile = stato !== 'P';

      // Forza il change detection
      this.cdr.detectChanges();
    });
  }

  /**
   * Gestisce il cambio di stato
   */
  onStatoChange(stato: string): void {
    this.verificaStatoEditabile(stato);
  }

  /**
   * Azione "Prosegui" - valida il form e abilita il tab Conti
   */
  onProsegui(): void {
    if (this.testataForm?.invalid) {
      this.mostraErrore('Compilare tutti i campi obbligatori della testata');
      return;
    }

    this.prosegui.emit();
  }

  /**
   * Azione "Annulla" - resetta il form
   */
  onAnnulla(): void {
    this.annulla.emit();
  }

  /**
   * Azione "Indietro" - torna alla ricerca
   */
  onIndietro(): void {
    this.indietro.emit();
  }

  /**
   * Mostra messaggio di errore tramite NotificationService
   */
  private mostraErrore(messaggio: string): void {
    // Verifica se il messaggio contiene un codice WEB_
    const webCodeMatch = messaggio.match(/(WEB_(OK|ERR|WRN|INF)_\d+)/);
    if (webCodeMatch && webCodeMatch[1]) {
      this.notificationService.showMessageByCode(webCodeMatch[1], messaggio.split(' - ')[1]);
    } else {
      this.notificationService.showError(messaggio);
    }
  }

  /**
   * Verifica se un campo è valido e toccato
   */
  isFieldInvalid(fieldName: string): boolean {
    const field = this.testataForm?.get(fieldName);
    return field ? (field.invalid && (field.dirty || field.touched)) : false;
  }

  /**
   * Ottieni i messaggi di errore per un campo
   */
  getFieldErrors(fieldName: string): string[] {
    const field = this.testataForm?.get(fieldName);
    if (!field || !field.errors) {
      return [];
    }

    const errors: string[] = [];

    if (field.errors['required']) {
      errors.push('Campo obbligatorio');
    }

    if (field.errors['maxlength']) {
      errors.push(`Massimo ${field.errors['maxlength'].requiredLength} caratteri`);
    }

    return errors;
  }
}
