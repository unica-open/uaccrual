/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { Store } from '@ngxs/store';
import { PrimanotaService } from '../../../../core/services/primanota.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PnTabTestataComponent } from '../pn-tab-testata/pn-tab-testata.component';
import { PnTabContiCollegatiComponent } from '../pn-tab-conti-collegati/pn-tab-conti-collegati.component';
import { PnTabAttributiComponent } from '../pn-tab-attributi/pn-tab-attributi.component';
import { PrimaNotaDetail, PrimaNotaContoCollegato, PrimaNotaAttributo } from '../../models/primanota.model';
import { ContextState } from '../../../../core/store/context/context.state';

/**
 * Componente container per il form Gestisci Prima Nota
 *
 * Gestisce:
 * - Navigazione tra i tab (Testata → Conti Collegati → Attributi)
 * - Stato del form
 * - Chiamate API per salvataggio
 * - Caricamento dati in modalità modifica
 *
 * NON usa PrimeNG - solo Angular Material + HTML/Tailwind
 */
@Component({
  selector: 'app-pn-container',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    PnTabTestataComponent,
    PnTabContiCollegatiComponent,
    PnTabAttributiComponent
  ],
  templateUrl: './pn-container.component.html',
  styleUrl: './pn-container.component.scss'
})
export class PnContainerComponent implements OnInit {

  /** Form group della testata */
  testataForm!: FormGroup;

  /** Snapshot dei valori iniziali del form per il reset */
  private formValoriIniziali: any = null;

  /** Snapshot dei conti iniziali per il reset */
  private contiIniziali: PrimaNotaContoCollegato[] = [];

  /** Snapshot degli attributi iniziali per il reset */
  private attributiIniziali: any[] = [];

  /** Tab attivo (1 = Testata, 2 = Conti, 3 = Attributi) */
  activeTab = 1;

  /** Tab attivo prima dell'annullamento (per mantenere il tab corrente) */
  private activeTabPreAnnulla: number = 1;

  /** Flag per modalità modifica */
  isModifica = false;

  /** Anno della prima nota (per modifica) */
  anno!: number;

  /** Numero della prima nota (per modifica) */
  numero!: number;

  /** Flag per stato di caricamento */
  loading = false;

  /** Flag per indicare che il form è stato popolato (per modifica) */
  formPopolato = false;

  /** Conti collegati */
  conti: PrimaNotaContoCollegato[] = [];

  /** Attributi */
  attributi: PrimaNotaAttributo[] = [];

  /** Pagina di provenienza ('search', 'results' o 'home') */
  fromPage: string = 'search';

  /** Query params del chiamante (filtri di ricerca) */
  callerQueryParams: any = {};

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private primanotaService: PrimanotaService,
    private notificationService: NotificationService,
    private store: Store
  ) {}

  ngOnInit(): void {
    this.inizializzaForm();

    // Gestisce i query parameters per la provenienza e i filtri
    this.route.queryParams.subscribe(params => {
      this.fromPage = params['from'] || 'search';

      // Salva i query params del chiamante (escluso 'from' e 'tab')
      this.callerQueryParams = { ...params };
      delete this.callerQueryParams['from'];
      delete this.callerQueryParams['tab'];

      if (params['tab'] === 'testata') {
        this.activeTab = 1;
      }
    });

    // Verifica se è modifica o inserimento
    this.route.paramMap.subscribe(params => {
      const annoParam = params.get('anno');
      const numeroParam = params.get('numero');

      if (annoParam && numeroParam) {
        this.isModifica = true;
        this.anno = +annoParam;
        this.numero = +numeroParam;
        this.caricaPrimaNota();
      }
    });
  }

  /**
   * Inizializza il form della testata
   */
  inizializzaForm(): void {
    // Inizializza dataRegProvvisoria con la data di sistema (solo per inserimento)
    const dataOdierna = this.formatDateForInput(new Date());

    this.testataForm = this.fb.group({
      tipo: [''],
      fonte: ['', Validators.required],
      evento: ['', Validators.required],
      causale: ['', Validators.required],
      numeroProvvisorio: [{ value: '', disabled: true }],
      dataRegProvvisoria: [dataOdierna, Validators.required],
      numeroDefinitivo: [{ value: '', disabled: true }],
      dataRegDefinitiva: [{ value: null, disabled: true }],
      stato: ['', Validators.required],
      descrizione: ['', [Validators.required, Validators.maxLength(500)]]
    });

    // Salva i valori iniziali per il reset (saranno aggiornati dopo il caricamento dropdown)
    this.formValoriIniziali = null;
  }

  /**
   * Carica i dati della prima nota in modalità modifica
   */
  caricaPrimaNota(): void {
    this.loading = true;
    this.primanotaService.getByAnnoAndNumero(this.anno, this.numero).subscribe({
      next: (data: PrimaNotaDetail) => {
        this.popolaForm(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('[PN-Container] Errore nel caricamento della prima nota:', err);
        this.mostraErrore('Errore nel caricamento della prima nota');
        this.loading = false;
      }
    });
  }

  /**
   * Popola il form con i dati della prima nota
   */
  popolaForm(data: PrimaNotaDetail): void {
    // Popola testata - usa i campi *Codice per le combo
    this.testataForm.patchValue({
      tipo: data.tipo,
      fonte: data.fonte,
      evento: data.evento,
      causale: data.causale,
      numeroProvvisorio: data.numeroProvvisorio || '',
      dataRegProvvisoria: data.dataRegProvvisoria ? this.formatDateForInput(data.dataRegProvvisoria) : null,
      numeroDefinitivo: data.numeroDefinitivo || '',
      dataRegDefinitiva: data.dataRegDefinitiva ? this.formatDateForInput(data.dataRegDefinitiva) : null,
      stato: data.stato,
      descrizione: data.descrizione
    });

    // Popola conti
    if (data.conti) {
      this.conti = [...data.conti];
    }

    // Popola attributi
    if (data.attributi) {
      this.attributi = [...data.attributi];
    }

    // Segnala al componente figlio che il form è stato popolato
    // Questo triggera il caricamento delle correlazioni in modifica
    if (this.isModifica) {
      this.formPopolato = true;
    }

    // Salva i valori iniziali per il reset (dopo che il form è stato popolato)
    setTimeout(() => {
      this.formValoriIniziali = {
        ...this.testataForm.getRawValue(),
        conti: [...this.conti],
        attributi: [...this.attributi]
      };
      // Salva snapshot separati per conti e attributi
      this.contiIniziali = [...this.conti];
      this.attributiIniziali = [...this.attributi];
    }, 100);
  }

  /**
   * Converte una data in formato YYYY-MM-DD per input HTML type="date"
   */
  private formatDateForInput(date: Date | string): string {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Gestisce il passaggio dal tab Testata al tab Conti
   */
  onProseguiDaTestata(): void {
    if (this.testataForm.invalid) {
      this.mostraErrore('Compilare tutti i campi obbligatori della testata');
      return;
    }
    this.activeTab = 2;
  }

  /**
   * Gestisce il passaggio dal tab Conti al tab Attributi
   */
  onProseguiDaConti(): void {
    // La validazione è già eseguita dal componente figlio PnTabContiCollegatiComponent
    // prima di emettere l'evento 'prosegui'.
    this.activeTab = 3;
  }

  /**
   * Naviga al chiamante preservando i filtri
   */
  private tornaAlChiamante(): void {
    if (this.fromPage === 'results') {
      this.router.navigate(['/primanota/results'], {
        queryParams: {
          ...this.callerQueryParams,
          _refresh: Date.now().toString()  // Timestamp per forzare il refresh
        },
        queryParamsHandling: 'merge'
      });
    } else if (this.fromPage === 'home') {
      this.router.navigate(['/']);
    } else {
      // Default: search page
      this.router.navigate(['/primanota/search'], {
        queryParams: {
          ...this.callerQueryParams,
          _refresh: Date.now().toString()  // Timestamp per forzare il refresh
        },
        queryParamsHandling: 'merge'
      });
    }
  }

  /**
   * Gestisce il salvataggio della prima nota
   */
  onSalva(): void {
    if (this.testataForm.invalid) {
      this.mostraErrore('Form non valido');
      return;
    }

    // Ottieni i valori correnti dal form
    const rawForm = this.testataForm.getRawValue();

    const testataDto = {
      tipo: rawForm.tipo,
      fonte: rawForm.fonte,
      evento: rawForm.evento,
      causale: rawForm.causale,
      dataRegProvvisoria: rawForm.dataRegProvvisoria,
      stato: rawForm.stato,
      descrizione: rawForm.descrizione
    };

    const dto = {
      testata: testataDto,
      conti: this.conti,
      attributi: this.attributi
    };

    this.loading = true;

    if (this.isModifica) {
      this.primanotaService.aggiorna(this.anno, this.numero, dto).subscribe({
        next: () => {
          this.mostraSuccesso('WEB_OK_0004 - Salvataggio effettuato correttamente');
          // Rimane nel form con tab Testata attivo
          this.activeTab = 1;
          this.loading = false;
        },
        error: (error: any) => {
          this.mostraErrore(error.error?.detail || 'Errore nel salvataggio');
          this.loading = false;
        }
      });
    } else {
      this.primanotaService.inserisci(dto).subscribe({
        next: (data) => {
          console.log('Prima nota inserita:', data);
          this.mostraSuccesso('WEB_OK_0004 - Salvataggio effettuato correttamente');
          // Naviga alla pagina di modifica della prima nota appena inserita, tab testata
          if (data && data.anno && data.numeroProvvisorio) {
            setTimeout(() => {
              this.router.navigate(['/primanota/modifica', data.anno, data.numeroProvvisorio], {
                queryParams: {
                  tab: 'testata',
                  from: this.fromPage,
                  ...this.callerQueryParams
                }
              });
            }, 100);
          } else {
            console.error('Dati prima nota non validi:', data);
            // Fallback: torna al chiamante
            this.tornaAlChiamante();
          }
        },
        error: (error: any) => {
          this.mostraErrore(error.error?.detail || 'Errore nel salvataggio');
          this.loading = false;
        }
      });
    }
  }

  /**
   * Gestisce l'annullamento dell'operazione
   * Secondo il CDU-007, il tasto ANNULLA deve ripristinare i dati alla situazione iniziale
   * e mantenere l'operatore nel tab corrente
   */
  onAnnulla(): void {
    const tabCorrente = this.activeTab;

    if (this.formValoriIniziali) {
      // Modalità modifica: ripristina i valori iniziali solo del tab corrente
      if (tabCorrente === 1) {
        // Tab Testata: resetta solo la testata ai valori iniziali
        this.testataForm.patchValue({
          tipo: this.formValoriIniziali.tipo,
          fonte: this.formValoriIniziali.fonte,
          evento: this.formValoriIniziali.evento,
          causale: this.formValoriIniziali.causale,
          numeroProvvisorio: this.formValoriIniziali.numeroProvvisorio,
          dataRegProvvisoria: this.formValoriIniziali.dataRegProvvisoria,
          numeroDefinitivo: this.formValoriIniziali.numeroDefinitivo,
          dataRegDefinitiva: this.formValoriIniziali.dataRegDefinitiva,
          stato: this.formValoriIniziali.stato,
          descrizione: this.formValoriIniziali.descrizione
        });
        this.conti = this.formValoriIniziali.conti ? [...this.formValoriIniziali.conti] : [];
        this.attributi = this.formValoriIniziali.attributi ? [...this.formValoriIniziali.attributi] : [];
      } else if (tabCorrente === 2) {
        // Tab Conti: resetta solo i conti, mantieni testata e attributi correnti
        this.conti = this.contiIniziali.length > 0 ? [...this.contiIniziali] : [];
      } else if (tabCorrente === 3) {
        // Tab Attributi: resetta solo gli attributi, mantieni testata e conti correnti
        this.attributi = this.attributiIniziali.length > 0 ? [...this.attributiIniziali] : [];
      }
    } else {
      // Modalità inserimento
      if (tabCorrente === 1) {
        // Tab Testata: resetta solo la testata mantenendo il Tipo di default
        this.inizializzaForm();
        // Dopo l'inizializzazione, ripristina il tipo 'M' di default
        setTimeout(() => {
          this.testataForm.patchValue({ tipo: 'M' });
        }, 50);
        this.conti = [];
        this.attributi = [];
      } else if (tabCorrente === 2) {
        // Tab Conti: resetta solo i conti, mantieni testata corrente
        this.conti = [];
      } else if (tabCorrente === 3) {
        // Tab Attributi: resetta solo gli attributi, mantieni testata e conti correnti
        this.attributi = [];
      }
    }

    // Mantieni il tab corrente (non tornare al tab 1)
    // this.activeTab rimane invariato
  }

  /**
   * Gestisce il salvataggio con uscita della prima nota
   */
  onSalvaEdEsci(): void {
    if (this.testataForm.invalid) {
      this.mostraErrore('Form non valido');
      return;
    }

    // Ottieni i valori correnti dal form
    const rawForm = this.testataForm.getRawValue();

    const testataDto = {
      tipo: rawForm.tipo,
      fonte: rawForm.fonte,
      evento: rawForm.evento,
      causale: rawForm.causale,
      dataRegProvvisoria: rawForm.dataRegProvvisoria,
      stato: rawForm.stato,
      descrizione: rawForm.descrizione
    };

    const dto = {
      testata: testataDto,
      conti: this.conti,
      attributi: this.attributi
    };

    this.loading = true;

    if (this.isModifica) {
      this.primanotaService.aggiorna(this.anno, this.numero, dto).subscribe({
        next: () => {
          this.mostraSuccesso('WEB_OK_0004 - Salvataggio effettuato correttamente');
          // Torna al chiamante preservando i filtri
          this.tornaAlChiamante();
        },
        error: (error: any) => {
          this.mostraErrore(error.error?.detail || 'Errore nel salvataggio');
          this.loading = false;
        }
      });
    } else {
      this.primanotaService.inserisci(dto).subscribe({
        next: (data) => {
          console.log('Prima nota inserita:', data);
          this.mostraSuccesso('WEB_OK_0004 - Salvataggio effettuato correttamente');
          // Torna al chiamante preservando i filtri
          this.tornaAlChiamante();
        },
        error: (error: any) => {
          this.mostraErrore(error.error?.detail || 'Errore nel salvataggio');
          this.loading = false;
        }
      });
    }
  }

  /**
   * Gestisce il ritorno al tab precedente o al chiamante
   */
  onIndietro(): void {
    if (this.activeTab > 1) {
      // Se siamo nei tab Conti o Attributi, torniamo al tab precedente
      this.activeTab--;
    } else {
      // Se siamo nel tab Testata, torniamo al chiamante
      this.location.back();
    }
  }

  /**
   * Mostra messaggio di errore
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
   * Mostra messaggio di successo
   */
  private mostraSuccesso(messaggio: string): void {
    // Verifica se il messaggio contiene un codice WEB_
    const webCodeMatch = messaggio.match(/(WEB_(OK|ERR|WRN|INF)_\d+)/);
    if (webCodeMatch && webCodeMatch[1]) {
      this.notificationService.showMessageByCode(webCodeMatch[1]);
    } else {
      this.notificationService.showSuccess(messaggio);
    }
  }
}
