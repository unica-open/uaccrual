/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PdcService } from '../../../../core/services/pdc.service';
import { Pdc, PdcClasse, PdcTipo, PdcStato, PdcSearchCriteria, PagedPdc } from '../../model/pdc.model';
import { switchMap, tap, forkJoin } from 'rxjs';

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';

// Import per NGXS
import { Store } from '@ngxs/store';
import { ClearMessage, SearchPdcConti } from '../../../../core/store/pdc/pdc.actions';
import { ContextState } from '../../../../core/store/context/context.state';

@Component({
  selector: 'app-pdc-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],
  templateUrl: './pdc-form.component.html',
})
export class PdcFormComponent implements OnInit {
  form!: FormGroup;
  isUpdateMode = false;
  isLoading = false; // Variabile per lo stato di caricamento
  private codicePdc?: string; // Cambiato da ID numerico a codice stringa
  private codiceUfficiale?: string;
  private codiceClasse?: string;
  private codiceFamiglia?: string;
  
  // Proprietà per memorizzare la tripletta del padre in inserimento
  private padreCodiceUfficiale?: string;
  private padreCodiceClasse?: string;
  private padreCodiceFamiglia?: string;
  private padreId?: string;
  
  classi: Array<{ codice: string, descrizione: string, segno?: string, id: string }> = [];
  tipi: Array<{ codice: string, descrizione: string, id: string }> = [];
  stati: Array<{ codice: string, descrizione: string, id: string }> = [];
  famiglie: Array<{ codice: string, descrizione: string, id: string, modificabile: boolean, ricercabile: boolean }> = [];

  // Proprietà per le relazioni
  fondi: Pdc[] = [];
  correlazioni: Pdc[] = [];

  // Snapshot dei valori originali del form in modalità aggiornamento (usato da Annulla)
  private originalFormValues: any = null;
  // Snapshot dei valori preimpostati dal padre in modalità inserimento (usato da Annulla)
  private originalInsertValues: any = null;

  // Proprietà per memorizzare i filtri originali
  private originalFilters: PdcSearchCriteria = {};

  /**
   * Metodo per ottenere la descrizione del tipo in base al codice
   */
  getTipoDescrizione(): string {
    const tipoCodice = this.form.get('id_tipo_conto')?.value;
    if (tipoCodice) {
      const tipo = this.tipi.find(t => t.codice === tipoCodice);
      return tipo ? `${tipo.codice} - ${tipo.descrizione}` : tipoCodice;
    }
    return '';
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private pdcService: PdcService,
    private notificationService: NotificationService,
    private dialog: MatDialog,
    private store: Store
  ) {}

  ngOnInit(): void {

    // Memorizza i filtri originali quando il componente viene inizializzato
    this.originalFilters = { ...this.pdcService.filtersValue };
    console.log('Filtri originali memorizzati:', this.originalFilters);

    this.isUpdateMode = this.route.snapshot.data['mode'] === 'update';

    this.form = this.fb.group({
      codicePadre: [{ value: '', disabled: true }, [Validators.required]], // Obbligatorio secondo CDU (validazione interna)
      codice: [{value: '', disabled: false}, [Validators.required, Validators.pattern(/^[A-Za-z0-9._-]+$/)]], // Obbligatorio secondo CDU
      codice_completo: [{ value: '', disabled: true }, [Validators.required]], // Obbligatorio secondo CDU (validazione interna)
      descrizione: ['', [Validators.required, Validators.maxLength(200)]],
      id_classe_conto: [{value: '', disabled: true}, [Validators.required]], // Obbligatorio secondo CDU (validazione interna)
      livello: [{ value: null, disabled: true }, [Validators.required]], // Obbligatorio secondo CDU (validazione interna)
      id_tipo_conto: [{value: '', disabled: false}, [Validators.required]], // Obbligatorio secondo CDU
      id_stato_conto: [null, Validators.required], // Obbligatorio ma ereditato dal padre
      id_famiglia_conto: [{value: '', disabled: true}, []], // Campo nascosto, assegnato automaticamente (ACCRUAL-UTENTE)
      flag_foglia: [true], // Preimpostato a true come da specifiche
      flag_fondo_dotazione: [false],
      flag_a_partita: [false],
      codice_bilancio: [''],
      data_inizio_validita: ['', [Validators.required, this.dateValidator()]], // Obbligatorio secondo CDU
      data_fine_validita: ['', this.dateValidator()],
      version: [null] // Per optimistic locking
    }, { validators: [this.endDateValidator()] }); // Aggiunto validatore per controllare che la data di fine validità sia maggiore o uguale a quella di inizio validità

    // Carica le liste per i dropdown
    forkJoin({
      classi: this.pdcService.getClassi(),
      tipi: this.pdcService.getTipi(),
      stati: this.pdcService.getStati(),
      famiglie: this.pdcService.getFamiglie()
    }).subscribe(({ classi, tipi, stati, famiglie }) => {
      this.classi = classi;
      this.tipi = tipi;
      this.stati = stati;
      this.famiglie = famiglie;
    });

    if (this.isUpdateMode) {
      this.isLoading = true; // Mostra lo spinner
      this.route.params.pipe(
        switchMap(params => this.pdcService.getPdcById(params['codiceUfficiale'], params['codiceClasse'], params['codiceFamiglia']))
      ).subscribe({
        next: (pdc) => {
          console.log('Caricamento dati PDC in modalità aggiornamento:', pdc);
          this.codicePdc = pdc.codice; // Cambiato da id_pdc a codice
          // Memorizza i valori della tripletta per l'aggiornamento
          this.codiceUfficiale = pdc.codice_ufficiale || pdc.codice;
          this.codiceClasse = pdc.pdc_classe?.codice || 'N/A';
          this.codiceFamiglia = pdc.pdc_famiglia?.codice || 'N/A';

          // Disabilita il campo codice PRIMA di impostare i valori per evitare che valueChanges si attivi
          this.form.get('codice')?.disable();

          console.log('Valori estratti dal PDC:', {
            codice: pdc.codice,
            codice_completo: pdc.codice_completo,
            descrizione: pdc.descrizione,
            tipo: pdc.pdc_tipo,
            stato: pdc.pdc_stato,
            foglia: pdc.foglia,
            fondo_dotazione: pdc.fondo_dotazione,
            partita: pdc.partita,
            collegamento_bil_itas: pdc.collegamento_bil_itas,
            inizio_validita: pdc.inizio_validita,
            fine_validita: pdc.fine_validita,
            version: pdc.version,
            pdc_padre: pdc.pdc_padre
          });

          // Poiché alcuni campi sono disabilitati, dobbiamo impostare i valori separatamente
          // Prepara i valori da impostare (escludendo codice_completo e codicePadre perché sono disabilitati)
          // CDU-003: Il campo "Codice" deve mostrare solo l'ultima parte del codice ufficiale
          const codiceCompleto = pdc.codice_completo || pdc.codice_ufficiale || pdc.codice;
          const codiceLocale = codiceCompleto.split('.').pop() || '';
          
          const formValues: any = {
            codice: codiceLocale,
            descrizione: pdc.descrizione,
            id_tipo_conto: pdc.pdc_tipo?.codice, // Usa il codice invece dell'id
            flag_foglia: pdc.foglia,
            flag_fondo_dotazione: pdc.fondo_dotazione,
            flag_a_partita: pdc.partita,
            codice_bilancio: pdc.collegamento_bil_itas,
            data_inizio_validita: pdc.inizio_validita ? this.formatDate(pdc.inizio_validita) : '',
            data_fine_validita: pdc.fine_validita ? this.formatDate(pdc.fine_validita) : '',
            version: pdc.version
          };

          console.log('Valori da impostare nel form:', formValues);

          // Imposta il valore per lo stato
          if (pdc.pdc_stato?.codice) {
            formValues.id_stato_conto = pdc.pdc_stato.codice;
          }

          console.log('Prima di patchValue, form values:', formValues);
          // Usa emitEvent: false per non attivare i listener valueChanges
          this.form.patchValue(formValues, { emitEvent: false });
          console.log('Dopo patchValue, stato del form:', this.form.value);

          // Impostiamo separatamente il valore della classe disabilitata
          if (pdc.pdc_classe?.codice) {
            // Imposta il valore del codice classe
            this.form.patchValue({ id_classe_conto: pdc.pdc_classe.codice }, { emitEvent: false });
          }

          // Impostiamo separatamente il valore del tipo (ora modificabile)
          if (pdc.pdc_tipo?.codice) {
            console.log('Impostazione tipo:', pdc.pdc_tipo.codice);
            this.form.patchValue({ id_tipo_conto: pdc.pdc_tipo.codice }, { emitEvent: false });
          }

          // Impostiamo separatamente il valore del livello disabilitato
          this.form.patchValue({ livello: pdc.livello }, { emitEvent: false });

          // Impostiamo separatamente il valore del codice completo disabilitato
          let codiceCompletoDaUsare = pdc.codice_completo;

          if (!codiceCompletoDaUsare && pdc.pdc_padre) {
            // Se il codice completo non è presente ma abbiamo il padre, lo calcoliamo
            // NB: pdc.codice contiene il codice completo (es. "1.2.3.10.2.5"), quindi usiamo codiceLocale (solo l'ultima parte)
            codiceCompletoDaUsare = `${pdc.pdc_padre.codice_ufficiale || pdc.pdc_padre.codice}.${codiceLocale}`;
            console.log('Calcolato codice completo dal padre e codice locale:', codiceCompletoDaUsare);
          } else if (!codiceCompletoDaUsare) {
            console.log('ATTENZIONE: né codice_completo né pdc_padre disponibili per calcolare il codice completo');
          }

          if (codiceCompletoDaUsare) {
            console.log('Impostazione codice completo:', codiceCompletoDaUsare);
            // Imposta il valore direttamente nel form senza attivare la validazione
            this.form.controls['codice_completo'].setValue(codiceCompletoDaUsare, { emitEvent: false });
            console.log('Valore impostato, valore dopo:', this.form.controls['codice_completo'].value);
          } else {
            console.log('ATTENZIONE: codice_completo è ancora vuoto dopo tentativo di calcolo');
          }

          // Impostiamo separatamente il valore del codice padre disabilitato
          if (pdc.pdc_padre) {
            console.log('Impostazione codice padre:', pdc.pdc_padre.codice_ufficiale || pdc.pdc_padre.codice);
            // Imposta il valore direttamente nel form senza attivare la validazione
            this.form.controls['codicePadre'].setValue(pdc.pdc_padre.codice_ufficiale || pdc.pdc_padre.codice, { emitEvent: false });
            console.log('Valore impostato, valore dopo:', this.form.controls['codicePadre'].value);
          } else {
            console.log('ATTENZIONE: pdc_padre è vuoto o nullo nel PDC caricato');
          }

          // Imposta il valore della famiglia
          if (pdc.pdc_famiglia?.id) {
            this.form.patchValue({ id_famiglia_conto: pdc.pdc_famiglia.id });
          }

          // Disabilita i campi come richiesto dalle specifiche CDU-003
          // La classe non è modificabile (ereditata dal conto padre)
          this.form.get('id_classe_conto')?.disable();

          // I flag foglia e fondo_dotazione non devono essere editabili come specificato nel CDU-003
          this.form.get('flag_foglia')?.disable();
          this.form.get('flag_fondo_dotazione')?.disable();

          // Il campo stato è modificabile come specificato nel CDU-003
          this.form.get('id_stato_conto')?.enable();

          // Il campo tipo è modificabile come specificato nel CDU-003
          this.form.get('id_tipo_conto')?.enable();

          // Snapshot dei valori originali: usato da Annulla per ripristinare lo stato
          this.originalFormValues = this.form.getRawValue();

          // Carica anche le relazioni (fondi e correlazioni)
          this.loadRelazioni(pdc);

          this.isLoading = false; // Nasconde lo spinner
        },
        error: (err) => {
          console.error('Errore nel caricamento dei dati per l\'aggiornamento:', err);
          this.notificationService.error('WEB_ERR_0001 - Si è verificato un errore imprevisto', 'Errore');
          this.isLoading = false;
        }
      });
    } else {
      // Logica per l'inserimento
      console.log('Modalità inserimento attiva');

      // Otteniamo i parametri del padre dal routing
      const codiceUfficialePadre = this.route.snapshot.params['codiceUfficialePadre'];
      const codiceClassePadre = this.route.snapshot.params['codiceClassePadre'];
      const codiceFamigliaPadre = this.route.snapshot.params['codiceFamigliaPadre'];

      if (codiceUfficialePadre && codiceClassePadre && codiceFamigliaPadre) {
        this.isLoading = true; // Mostra lo spinner
        console.log('Chiamata al servizio per ottenere il padre con tripletta:', codiceUfficialePadre, codiceClassePadre, codiceFamigliaPadre);

        // Carichiamo prima i dati necessari (classi, tipi, stati) e poi il padre
        forkJoin({
          padre: this.pdcService.getPdcById(codiceUfficialePadre, codiceClassePadre, codiceFamigliaPadre),
          classi: this.pdcService.getClassi(),
          tipi: this.pdcService.getTipi(),
          stati: this.pdcService.getStati()
        }).subscribe({
          next: ({ padre, classi, tipi, stati }) => {
            // Aggiorna le liste locali
            this.classi = classi;
            this.tipi = tipi;
            this.stati = stati;

            // Memorizza la tripletta del padre per il salvataggio
            this.padreCodiceUfficiale = padre.codice_ufficiale || padre.codice;
            this.padreCodiceClasse = padre.pdc_classe?.codice;
            this.padreCodiceFamiglia = padre.pdc_famiglia?.codice;
            this.padreId = padre.id;

            console.log('Dati del padre ricevuti:', padre);
            console.log('Valore codice padre:', padre.codice);
            console.log('Valore codice_ufficiale padre:', padre.codice_ufficiale);
            const valoreDaImpostare = padre.codice_ufficiale || padre.codice;
            console.log('Valore da impostare nel campo codicePadre:', valoreDaImpostare);

            this.form.controls['codicePadre'].setValue(valoreDaImpostare);
            console.log('Valore impostato nel campo codicePadre:', this.form.controls['codicePadre'].value);

            // Imposta il valore per lo stato ereditato dal padre
            console.log('=== DEBUG STATO EREDITATO ===');
            console.log('padre.pdc_stato:', padre.pdc_stato);
            console.log('padre.pdc_stato?.codice:', padre.pdc_stato?.codice);
            console.log('this.stati:', this.stati);
            if (padre.pdc_stato?.codice) {
              // Verifica che lo stato esista nella lista degli stati disponibili
              const statoEsistente = this.stati.some(stato => stato.codice === padre.pdc_stato!.codice);
              console.log('statoEsistente:', statoEsistente);
              if (statoEsistente) {
                const statoCodice = padre.pdc_stato.codice;
                // Imposto il valore immediatamente
                this.form.patchValue({ id_stato_conto: statoCodice });
                console.log('STATO IMPOSTATO A:', statoCodice);
                console.log('Valore form dopo patchValue:', this.form.get('id_stato_conto')?.value);
                // Uso setTimeout per dare tempo ad Angular di renderizzare il DOM
                setTimeout(() => {
                  const selectElement = document.getElementById('id_stato_conto') as HTMLSelectElement;
                  if (selectElement) {
                    selectElement.value = statoCodice;
                    console.log('Select nativo aggiornato a:', selectElement.value);
                  } else {
                    console.error('Elemento select id_stato_conto non trovato!');
                  }
                }, 100);
              } else {
                console.warn(`Lo stato ereditato "${padre.pdc_stato.codice}" non esiste nella lista degli stati disponibili`);
                // Imposta un valore di default se lo stato ereditato non esiste
                if (this.stati && this.stati.length > 0) {
                  const defaultCodice = this.stati[0].codice;
                  this.form.patchValue({ id_stato_conto: defaultCodice });
                  console.log('STATO IMPOSTATO A DEFAULT:', defaultCodice);
                  setTimeout(() => {
                    const selectElement = document.getElementById('id_stato_conto') as HTMLSelectElement;
                    if (selectElement) {
                      selectElement.value = defaultCodice;
                    }
                  }, 100);
                }
              }
            } else {
              // Se per qualche motivo il padre non ha uno stato, impostiamo un valore di default
              // ma solo se esiste almeno uno stato disponibile
              console.warn('padre.pdc_stato?.codice non valorizzato');
              if (this.stati && this.stati.length > 0) {
                const defaultCodice = this.stati[0].codice;
                this.form.patchValue({ id_stato_conto: defaultCodice });
                console.log('STATO IMPOSTATO A DEFAULT (padre senza stato):', defaultCodice);
                setTimeout(() => {
                  const selectElement = document.getElementById('id_stato_conto') as HTMLSelectElement;
                  if (selectElement) {
                    selectElement.value = defaultCodice;
                  }
                }, 100);
              }
            }
            console.log('=== FINE DEBUG STATO EREDITATO ===');
            // Imposta Inizio validità a 01/01/AAAA (anno di bilancio corrente) come da specifiche CDU-003
            // Recupera l'anno di bilancio dallo store NGXS invece di usare l'anno solare
            const annoBilancio = this.store.selectSnapshot(ContextState.getAnnoBilancio);
            const annoDaUsare = annoBilancio || new Date().getFullYear();
            this.form.controls['data_inizio_validita'].setValue(`${annoDaUsare}-01-01`);

            // Eredita la classe dal conto padre (come richiesto dalle specifiche)
            if (padre.pdc_classe) {
              // Verifica che la classe esista nella lista delle classi disponibili
              const classeEsistente = this.classi.some(classe => classe.codice === padre.pdc_classe!.codice);
              if (classeEsistente && padre.pdc_classe.codice) {
                this.form.patchValue({ id_classe_conto: padre.pdc_classe.codice });
              } else {
                console.warn(`La classe ereditata "${padre.pdc_classe.codice || padre.pdc_classe.id}" non esiste nella lista delle classi disponibili`);
                // Imposta un valore di default se la classe ereditata non esiste
                if (this.classi && this.classi.length > 0) {
                  this.form.patchValue({ id_classe_conto: this.classi[0].codice });
                }
              }
              console.log('Valore codice classe padre impostato:', padre.pdc_classe.codice || padre.pdc_classe.id);
            }

            // Eredita il tipo dal conto padre (come richiesto dalle specifiche)
            if (padre.pdc_tipo?.codice) {
              // Verifica che il tipo esista nella lista dei tipi disponibili
              const tipoEsistente = this.tipi.some(tipo => tipo.codice === padre.pdc_tipo!.codice);
              if (tipoEsistente) {
                this.form.patchValue({ id_tipo_conto: padre.pdc_tipo.codice });
              } else {
                console.warn(`Il tipo ereditato "${padre.pdc_tipo.codice}" non esiste nella lista dei tipi disponibili`);
                // Imposta un valore di default se il tipo ereditato non esiste
                if (this.tipi && this.tipi.length > 0) {
                  this.form.patchValue({ id_tipo_conto: this.tipi[0].codice });
                }
              }
              console.log('Valore tipo padre impostato:', padre.pdc_tipo.codice);
            }

            // Calcola il livello del nuovo conto
            const nuovoLivello = (padre.livello || 0) + 1;
            this.form.patchValue({ livello: nuovoLivello });
            console.log('Valore livello impostato:', nuovoLivello);

            // Assegna automaticamente la famiglia ACCRUAL-UTENTE (come richiesto dal CDU-003)
            const famigliaUtente = this.famiglie.find(f => f.codice === 'ACCRUAL-UTENTE');
            if (famigliaUtente) {
              this.form.patchValue({ id_famiglia_conto: famigliaUtente.id }, { emitEvent: false });
              console.log('Famiglia ACCRUAL-UTENTE assegnata automaticamente:', famigliaUtente.id);
            } else {
              console.warn('Famiglia ACCRUAL-UTENTE non trovata!');
            }

            // Snapshot dei valori iniziali preimpostati: usato da Annulla per ripristinare lo stato
            this.originalInsertValues = this.form.getRawValue();

            this.isLoading = false; // Nasconde lo spinner
          },
          error: (err) => {
            console.error('Errore nel caricamento dei dati del padre o delle liste per l\'inserimento:', err);
            this.notificationService.error('WEB_ERR_0001 - Si è verificato un errore imprevisto', 'Errore');
            this.isLoading = false;
          }
        });
      } else {
        console.log('Nessun parametro codicePadre trovato nella route');
        console.log('Tutti i parametri della route:', this.route.snapshot.params);
      }
    }

    // Aggiungi validazione personalizzata per il codice completo
    // Il listener viene aggiunto SOLO in modalità inserimento per evitare duplicazioni in aggiornamento
    if (!this.isUpdateMode) {
      this.form.get('codice')?.valueChanges.subscribe(value => {
        // In modalità inserimento, calcola il codice completo in base al codice padre
        const codicePadre = this.form.get('codicePadre')?.value;
        console.log('Cambio valore codice figlio, codice padre:', codicePadre, 'valore figlio:', value);
        if (codicePadre && value) {
          const nuovoCodiceCompleto = `${codicePadre}.${value}`;
          console.log('Imposto codice completo a:', nuovoCodiceCompleto);
          this.form.controls['codice_completo'].setValue(nuovoCodiceCompleto, { emitEvent: false });
        }
      });
      console.log('Listener valueChanges aggiunto per modalità INSERIMENTO');
    } else {
      console.log('Modalità AGGIORNAMENTO: listener valueChanges NON aggiunto, codice completo gestito manualmente');
    }

    // Il campo stato deve essere NON editabile in inserimento (come richiesto dal CDU-003)
    // ma NON lo disabilitiamo per evitare problemi di visualizzazione del valore selezionato.
    // La disabilitazione visiva è gestita nel template HTML con le classi CSS.
    // In modalità aggiornamento, lo stato è invece modificabile.

    // Disabilita i campi classe in modalità inserimento come richiesto dal CDU
    // Il tipo deve essere modificabile in inserimento e aggiornamento
    if (!this.isUpdateMode) {
      this.form.get('id_classe_conto')?.disable();
      // Il tipo deve essere modificabile in inserimento, quindi non lo disabilitiamo
      // I flag foglia e fondo dotazione non devono essere editabili in inserimento
      this.form.get('flag_foglia')?.disable();
      this.form.get('flag_fondo_dotazione')?.disable();
    } else {
      // Il campo codice è già stato disabilitato all'inizio del caricamento dati
      // Disabilita solo la famiglia come richiesto dal CDU
      this.form.get('id_famiglia_conto')?.disable();
      // I flag foglia e fondo dotazione non devono essere editabili in aggiornamento
      this.form.get('flag_foglia')?.disable();
      this.form.get('flag_fondo_dotazione')?.disable();
    }
  }

  /**
   * Formatta una data in formato ISO YYYY-MM-DD per l'input type="date"
   */
  private formatDate(date: Date | string): string {
    if (typeof date === 'string') {
      // Se è già una stringa ISO, la ritorno così com'è
      if (date.includes('-')) {
        return date.split('T')[0];
      }
      // Altrimenti provo a parsearla come DD/MM/YYYY
      const [day, month, year] = date.split('/');
      const parsedDate = new Date(Number(year), Number(month) - 1, Number(day));
      return this.formatDate(parsedDate);
    }

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Mese è 0-indexed
    const year = date.getFullYear();

    return `${year}-${month}-${day}`; // Formato ISO per input type="date"
  }

  /**
   * Converte una data dal formato ISO YYYY-MM-DD (input type="date") al formato ISO per il backend
   */
  private parseDate(dateString: string): string {
    if (!dateString) return '';

    // L'input type="date" restituisce già il formato ISO YYYY-MM-DD
    // Quindi possiamo usarlo direttamente
    return dateString;
  }

  /**
   * Metodo per caricare le relazioni (fondi e correlazioni) per un conto PDC
   */
  private loadRelazioni(pdc: Pdc): void {
    if (pdc.codice_ufficiale && pdc.pdc_classe?.codice && pdc.pdc_famiglia?.codice) {
      // Carica i conti collegati con tipo relazione FONDO
      this.pdcService.getContiCollegati(pdc.codice_ufficiale, pdc.pdc_classe.codice, pdc.pdc_famiglia.codice, 'FONDO').subscribe({
        next: (fondi) => {
          this.fondi = fondi;
        },
        error: (error) => {
          console.error('Errore durante il caricamento dei conti collegati tipo FONDO:', error);
          this.fondi = [];
        }
      });

      // Carica i conti collegati con tipo relazione CORRELAZIONE
      this.pdcService.getContiCollegati(pdc.codice_ufficiale, pdc.pdc_classe.codice, pdc.pdc_famiglia.codice, 'CORRELAZIONE').subscribe({
        next: (correlazioni) => {
          this.correlazioni = correlazioni;
        },
        error: (error) => {
          console.error('Errore durante il caricamento dei conti collegati tipo CORRELAZIONE:', error);
          this.correlazioni = [];
        }
      });
    }
  }

  /**
   * Validatore personalizzato per il formato della data (ISO YYYY-MM-DD per input type="date")
   */
  private dateValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null; // Se il campo è vuoto, lascia che altri validatori gestiscano il requisito
      }

      // Formato ISO: YYYY-MM-DD
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      if (!dateRegex.test(control.value)) {
        return { invalidDateFormat: true };
      }

      // Controlla se la data è valida
      const [year, month, day] = control.value.split('-');
      const date = new Date(Number(year), Number(month) - 1, Number(day));

      if (
        date.getDate() !== Number(day) ||
        date.getMonth() !== Number(month) - 1 ||
        date.getFullYear() !== Number(year)
      ) {
        return { invalidDate: true };
      }

      return null;
    };
  }

  /**
   * Validatore personalizzato per controllare che la data di fine validità sia maggiore o uguale a quella di inizio validità
   */
  private endDateValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      // Fare il cast a FormGroup per accedere ai singoli controlli
      const group = control as FormGroup;

      const startDateCtrl = group.get('data_inizio_validita');
      const endDateCtrl = group.get('data_fine_validita');

      if (!startDateCtrl || !endDateCtrl) {
        return null; // Se uno dei due controlli non esiste, non possiamo fare il controllo
      }

      const startDateStr = startDateCtrl.value;
      const endDateStr = endDateCtrl.value;

      // Se una delle due date è vuota, non facciamo il controllo
      if (!startDateStr || !endDateStr) {
        return null;
      }

      // Convertiamo le stringhe in formato data (ISO: YYYY-MM-DD)
      const [startYear, startMonth, startDay] = startDateStr.split('-').map(Number);
      const [endYear, endMonth, endDay] = endDateStr.split('-').map(Number);

      const startDate = new Date(startYear, startMonth - 1, startDay);
      const endDate = new Date(endYear, endMonth - 1, endDay);

      // Controlliamo se la data di fine è precedente alla data di inizio
      if (endDate < startDate) {
        return { endDateBeforeStartDate: true };
      }

      return null;
    };
  }

  onSubmit(): void {
    console.log('Form values:', this.form.value);
    console.log('Form validity:', this.form.valid);
    console.log('Form errors:', this.form.errors);
    console.log('Form controls validity:', this.getFormControlsValidity());
    this.salvaPdc(false);
  }

  onSubmitAndExit(): void {
    console.log('Form values:', this.form.value);
    console.log('Form validity:', this.form.valid);
    console.log('Form errors:', this.form.errors);
    console.log('Form controls validity:', this.getFormControlsValidity());
    this.salvaPdc(true);
  }

  private getFormControlsValidity(): any {
    const controlsValidity: any = {};
    Object.keys(this.form.controls).forEach(key => {
      const control = this.form.get(key);
      controlsValidity[key] = {
        valid: control?.valid,
        errors: control?.errors,
        value: control?.value,
        touched: control?.touched
      };
    });
    return controlsValidity;
  }

  private salvaPdc(exitAfterSave: boolean): void {
    if (this.form.invalid) {
      // Segna tutti i controlli come "touched" per far apparire gli errori immediatamente
      this.form.markAllAsTouched();
      // Non mostrare più il messaggio globale WEB_ERR_0002 qui.
      // Gli errori specifici saranno mostrati nel template vicino ai campi.
      return;
    }

    this.isLoading = true; // Mostra lo spinner durante il salvataggio

    // Ottieni i valori dal form
    const formValue = this.form.getRawValue();

    // Prima di tutto, controlliamo se siamo in modalità inserimento
    if (!this.isUpdateMode) {
      // In modalità inserimento, il backend gestisce automaticamente l'aggiornamento
      // del flag foglia del padre quando si inserisce un figlio
      this.creaPdc(formValue, exitAfterSave);
    } else {
      // In modalità aggiornamento, procediamo direttamente al salvataggio
      const pdc: Pdc = this.costruisciOggettoPdc(formValue);
      this.pdcService.getPdcByCodicePdc(this.codicePdc!).subscribe(
        (existingPdc: Pdc) => {
          // Verifichiamo se sono necessarie conferme basate sui cambiamenti
          this.checkAndHandleConfirmations(existingPdc, pdc, exitAfterSave);
        },
        (err: any) => {
          console.error('Errore nel caricamento del conto esistente per le conferme:', err);
          this.isLoading = false;
          // Procediamo comunque con il salvataggio se non riusciamo a caricare il conto esistente
          this.performSave(pdc, exitAfterSave);
        }
      );
    }
  }

  /**
   * Metodo ausiliario per costruire l'oggetto Pdc dal form
   */
  private costruisciOggettoPdc(formValue: any): Pdc {
    // Costruiamo l'oggetto Pdc senza includere l'id quando siamo in modalità aggiornamento
    // poiché l'identificazione avviene tramite la tripletta (codiceUfficiale, codiceClasse, codiceFamiglia)
    const pdc: Partial<Pdc> = {
      // CDU-003 sez. 2.10.4: il campo codice deve contenere il codice completo (es. 2.3.4.0.0.5)
      codice: formValue.codice_completo,
      descrizione: formValue.descrizione,
      // Poiché il campo classe è disabilitato e mostra codice-descrizione, estraiamo il codice
      pdc_classe: formValue.id_classe_conto
        ? (() => {
            // Estrai il codice dalla stringa codice - descrizione (eventualmente con segno)
            const codiceClasse = typeof formValue.id_classe_conto === 'string' && formValue.id_classe_conto.includes(' - ')
              ? formValue.id_classe_conto.split(' - ')[0]
              : formValue.id_classe_conto;
            const classeTrovata = this.classi.find(c => c.codice === codiceClasse);
            return classeTrovata
              ? { id: classeTrovata.id, codice: codiceClasse, descrizione: '' }
              : undefined;
          })()
        : undefined,
      pdc_tipo: formValue.id_tipo_conto ? (() => {
        const tipoTrovato = this.tipi.find(t => t.codice === formValue.id_tipo_conto);
        return tipoTrovato ? { id: tipoTrovato.id, codice: formValue.id_tipo_conto, descrizione: '' } : undefined;
      })() : undefined, // Il tipo ora è selezionabile
      pdc_stato: formValue.id_stato_conto
        ? (() => {
            const statoTrovato = this.stati.find(s => s.codice === formValue.id_stato_conto);
            return statoTrovato
              ? { id: statoTrovato.id, codice: formValue.id_stato_conto, descrizione: '' }
              : undefined;
          })()
        : undefined,
      pdc_famiglia: formValue.id_famiglia_conto ? (() => {
        const famigliaTrovata = this.famiglie.find(f => f.id === formValue.id_famiglia_conto);
        return famigliaTrovata ? {
          id: famigliaTrovata.id,
          codice: famigliaTrovata.codice,
          descrizione: famigliaTrovata.descrizione,
          modificabile: famigliaTrovata.modificabile,
          ricercabile: famigliaTrovata.ricercabile
        } : undefined;
      })() : undefined,
      foglia: formValue.flag_foglia,
      fondo_dotazione: formValue.flag_fondo_dotazione,
      partita: formValue.flag_a_partita,
      collegamento_bil_itas: formValue.codice_bilancio,
      inizio_validita: this.parseDate(formValue.data_inizio_validita), // Converti la data in formato ISO
      fine_validita: formValue.data_fine_validita ? this.parseDate(formValue.data_fine_validita) : null, // Converti la data in formato ISO (null esplicito per PostgreSQL)
      version: formValue.version,
      // Altri campi necessari mantenuti con valori di default
      // In modalità aggiornamento, mantieni il codice ufficiale originale per evitare duplicazioni
      codice_ufficiale: this.isUpdateMode && this.codiceUfficiale ? this.codiceUfficiale : formValue.codice_completo,
      livello: (formValue.livello !== undefined && formValue.livello !== null) ? formValue.livello : 1,
      modificabile: true,
      eliminabile: true,
      inseribile_figlio: true, // Il backend ricalcolerà questo valore in base alle regole CDU 1.1
      ente: undefined,
      // Il padre viene passato tramite l'ID, recuperato dai parametri di routing
      pdc_padre: this.padreId ? { id: this.padreId } as Pdc : undefined
    };

    // Cast esplicito a Pdc
    return pdc as Pdc;
  }

  /**
   * Metodo per creare un nuovo PDC
   */
  private creaPdc(formValue: any, exitAfterSave: boolean): void {
    const pdc: Pdc = this.costruisciOggettoPdc(formValue);
    this.performSave(pdc, exitAfterSave);
  }

  /**
   * Metodo per verificare e gestire le conferme richieste dal CDU-003
   */
  private checkAndHandleConfirmations(existingPdc: Pdc, updatedPdc: Pdc, exitAfterSave: boolean): void {
    // Verifichiamo se il conto ha figli cercando i conti il cui padre è il conto corrente
    // Dobbiamo cercare per codice padre, non per codice uguale
    this.pdcService.ricercaPdc({ codice: updatedPdc.codice_ufficiale }, 0, 10).subscribe(
      (pagedResult: PagedPdc<Pdc>) => {
        // Cerchiamo i figli controllando se il campo codice_ufficiale del conto padre corrisponde
        // ai conti figli (cioè quei conti il cui codice inizia con il codice_ufficiale del padre + '.')
        // Tuttavia, l'API attuale non supporta direttamente questa ricerca
        // Quindi dobbiamo cercare tutti i conti e filtrare lato client

        // Per semplificare, assumiamo che il backend abbia un endpoint specifico per ottenere i figli
        // In alternativa, possiamo cercare i conti con un filtro specifico
        // Creiamo un filtro per cercare i conti figli (quelli il cui codice ufficiale inizia con il codice del padre + '.')
        const codicePadre = updatedPdc.codice_ufficiale || updatedPdc.codice;
        const filtroFigli: PdcSearchCriteria = {
          codice: codicePadre + '.'  // Cerca i conti il cui codice inizia con il codice padre + '.'
        };

        this.pdcService.ricercaPdc(filtroFigli, 0, 10).subscribe(
          (result: PagedPdc<Pdc>) => {
            const hasChildren = result.data && result.data.length > 0;

            // Verifichiamo se si sta aggiungendo fineValidità (passaggio da undefined/null a un valore)
            const addingFineValidita = !existingPdc.fine_validita && updatedPdc.fine_validita;
            // Verifichiamo se si sta rimuovendo fineValidità (passaggio da un valore a undefined/null)
            const removingFineValidita = existingPdc.fine_validita && !updatedPdc.fine_validita;

            // Verifichiamo se si sta cambiando stato da 'Attivo' a 'Non Attivo'
            // Dobbiamo confrontare i codici stato invece che le descrizioni perché queste potrebbero non essere disponibili
            const changingStatusToInactive = existingPdc.pdc_stato?.codice === 'A' && updatedPdc.pdc_stato?.codice === 'NA'; // Assumendo A=Attivo, NA=Non Attivo
            // Verifichiamo se si sta cambiando stato da 'Non Attivo' a 'Attivo'
            const changingStatusToActive = existingPdc.pdc_stato?.codice === 'NA' && updatedPdc.pdc_stato?.codice === 'A';

            // Determiniamo quale messaggio di conferma mostrare
            let confirmationMessageCode: string | null = null;

            if (hasChildren && addingFineValidita) {
              confirmationMessageCode = 'WEB_CON_0008';
            } else if (hasChildren && removingFineValidita) {
              confirmationMessageCode = 'WEB_CON_0009';
            } else if (hasChildren && changingStatusToInactive) {
              confirmationMessageCode = 'WEB_CON_0010';
            } else if (hasChildren && changingStatusToActive) {
              confirmationMessageCode = 'WEB_CON_0011';
            }

            if (confirmationMessageCode) {
              let confirmationMessage = '';
              switch (confirmationMessageCode) {
                case 'WEB_CON_0008':
                  confirmationMessage = 'La fine validità verrà assegnata anche ai conti figli, si vuole procedere?';
                  break;
                case 'WEB_CON_0009':
                  confirmationMessage = 'La fine validità verrà eliminata anche dai conti figli, si vuole procedere?';
                  break;
                case 'WEB_CON_0010':
                  confirmationMessage = 'Anche ai conti figli verrà impostato lo stato Non attivo, si vuole procedere?';
                  break;
                case 'WEB_CON_0011':
                  confirmationMessage = 'Anche ai conti figli verrà impostato lo stato Attivo, si vuole procedere?';
                  break;
              }

              const dialogRef = this.dialog.open(ConfirmDialogComponent, {
                data: { message: `${confirmationMessageCode} - ${confirmationMessage}`, title: 'Conferma Operazione' } as ConfirmDialogData
              });
              dialogRef.afterClosed().subscribe(result => {
                if (result === true) {
                  this.performSave(updatedPdc, exitAfterSave);
                } else {
                  this.isLoading = false;
                }
              });
            } else {
              // Nessuna conferma richiesta, procediamo direttamente
              this.performSave(updatedPdc, exitAfterSave);
            }
          },
          (err: any) => {
            console.error('Errore nel controllo dei figli del conto:', err);
            // In caso di errore, procediamo comunque con il salvataggio
            this.performSave(updatedPdc, exitAfterSave);
          }
        );
      },
      (err: any) => {
        console.error('Errore nel controllo dei figli del conto:', err);
        // In caso di errore, procediamo comunque con il salvataggio
        this.performSave(updatedPdc, exitAfterSave);
      }
    );
  }

  /**
   * Metodo per eseguire effettivamente il salvataggio
   */
  private performSave(pdc: Pdc, exitAfterSave: boolean): void {
    let action$: any;
    if (this.isUpdateMode && this.codiceUfficiale && this.codiceClasse && this.codiceFamiglia) {
      // Prima di effettuare l'aggiornamento, assicuriamoci che la versione nel pdc sia aggiornata
      // rispetto a quella attualmente nel form (potrebbe essere stata aggiornata dopo un precedente salvataggio)
      if (this.form.get('version')?.value !== undefined) {
        pdc.version = this.form.get('version')?.value;
      }
      action$ = this.pdcService.updatePdc(this.codiceUfficiale, this.codiceClasse, this.codiceFamiglia, pdc);
    } else {
      action$ = this.pdcService.insertPdc(pdc);
    }

    action$.subscribe({
      next: (result: Pdc) => {
        this.notificationService.success('WEB_OK_0004 - Salvataggio effettuato correttamente', 'Successo');
        this.isLoading = false;

        // Aggiorna la versione nel form con quella restituita dal server dopo il salvataggio
        if (result.version !== undefined) {
          this.form.patchValue({ version: result.version });
          console.log('Versione aggiornata dopo il salvataggio:', result.version);
        }

        if (exitAfterSave) {
          // Nascondi lo spinner immediatamente
          this.isLoading = false;
          console.log('Spinner nascosto immediatamente dopo il salvataggio');

          // Usa Promise.then per uscire dal contesto corrente e differire l'esecuzione
          Promise.resolve().then(() => {
            // Ripristina i filtri originali prima di navigare
            this.pdcService.updateFilters(this.originalFilters);
            console.log('Filtri originali ripristinati:', this.originalFilters);

            // Cancella il messaggio prima di navigare
            this.store.dispatch(new ClearMessage());
            console.log('Messaggio cancellato dallo stato');

            // Torna immediatamente alla pagina dei risultati dopo il salvataggio
            this.router.navigate(['/pdc/results']).then(() => {
              // Dopo aver navigato, forza una nuova ricerca con i filtri originali ripristinati
              // Usiamo direttamente i filtri originali che sono stati ripristinati precedentemente
              this.store.dispatch(new SearchPdcConti(this.originalFilters));
            });
          });
        } else {
          // Aggiorna il form con i dati salvati e rimani nella pagina
          if (!this.isUpdateMode) {
            // Passa alla modalità aggiornamento con il nuovo codice
            this.codicePdc = result.codice;
            this.isUpdateMode = true;
          }
        }
      },
      error: (err: any) => {
        // Controlla se l'errore è un OptimisticLockException
        if (err.status === 409 || (err.error && err.error.message && err.error.message.includes('OptimisticLock'))) {
          // Ricarica i dati del PDC dal server
          if (this.isUpdateMode && this.codiceUfficiale && this.codiceClasse && this.codiceFamiglia) {
            this.isLoading = true;
            this.pdcService.getPdcById(this.codiceUfficiale, this.codiceClasse, this.codiceFamiglia).subscribe({
              next: (pdc: Pdc) => {
                // Aggiorna il form con i dati più recenti
                this.form.patchValue({ version: pdc.version });

                this.notificationService.warning('WEB_WRN_0007 - Il record è stato modificato da un altro utente. I dati sono stati aggiornati.', 'Attenzione');

                // Nasconde lo spinner
                this.isLoading = false;
              },
              error: (loadErr: any) => {
                console.error('Errore durante il ricaricamento dei dati:', loadErr);
                this.notificationService.error('WEB_ERR_0001 - Si è verificato un errore imprevisto', 'Errore');
                this.isLoading = false;
              }
            });
          }
        } else {
          // Per altri errori, mostra il messaggio di errore normale
          // Il servizio PdcService si occupa di mostrare il messaggio di errore specifico tramite NotificationService
          // Qui gestiamo solo la logica di UI come nascondere lo spinner
          console.error('Errore durante il salvataggio:', err);
          this.notificationService.error('WEB_ERR_0001 - Si è verificato un errore imprevisto', 'Errore');
          this.isLoading = false;

          // Se siamo in modalità exitAfterSave ma c'è un errore, comunque mostriamo il messaggio e rimaniamo
          if (exitAfterSave) {
            console.log('Errore durante il salvataggio ma exitAfterSave era true - rimango in pagina per permettere all\'utente di correggere');
          }
        }
      }
    });
  }

  private resetForm(): void {
    if (this.isUpdateMode) {
      if (this.originalFormValues) {
        this.form.patchValue(this.originalFormValues, { emitEvent: false });
      }
      return;
    }

    // In modalità inserimento ripristina i valori preimpostati dal padre (classe, livello, stato, ecc.)
    // mantenendo vuoti solo i campi che l'utente deve compilare.
    if (this.originalInsertValues) {
      this.form.patchValue(this.originalInsertValues, { emitEvent: false });
    } else {
      // Fallback (snapshot non disponibile): pulisce solo i campi editabili
      // e ripristina la data di inizio con l'anno di bilancio, non l'anno di sistema.
      const annoBilancio = this.store.selectSnapshot(ContextState.getAnnoBilancio);
      const annoDaUsare = annoBilancio || new Date().getFullYear();
      this.form.patchValue({
        codice: '',
        codice_completo: '',
        descrizione: '',
        flag_a_partita: false,
        codice_bilancio: '',
        data_inizio_validita: `${annoDaUsare}-01-01`,
        data_fine_validita: ''
      }, { emitEvent: false });
    }

    // Resetta anche i touched/dirty status
    this.form.markAsPristine();
    this.form.markAsUntouched();
  }

  onBack(): void {
    // Ripristina i filtri originali prima di navigare
    this.pdcService.updateFilters(this.originalFilters);
    console.log('Filtri originali ripristinati in onBack:', this.originalFilters);
    
    // Cancella il messaggio prima di navigare
    this.store.dispatch(new ClearMessage());
    
    this.router.navigate(['/pdc', 'results']); // Torna alla pagina dei risultati della ricerca
  }

  onCancel(): void {
    this.resetForm();
    this.store.dispatch(new ClearMessage());

    // Rimane nella pagina di inserimento (non naviga ai risultati)
  }

  onDelete(): void {
    // Per ora, continuiamo a utilizzare il dialogo di conferma, ma in futuro potremmo sostituirlo con un messaggio in pagina
    if (this.codiceUfficiale && this.codiceClasse && this.codiceFamiglia) {
      // Salva i valori in variabili locali per aiutare il compilatore
      const codiceUfficiale = this.codiceUfficiale;
      const codiceClasse = this.codiceClasse;
      const codiceFamiglia = this.codiceFamiglia;
      // Apre il dialogo di conferma
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: { message: 'WEB_WRN_0006 - Si vuole eliminare il dato selezionato?', title: 'Conferma Eliminazione' } as ConfirmDialogData
      });

      // Gestisce la chiusura del dialogo
      dialogRef.afterClosed().subscribe(result => {
        if (result === true) { // Se l'utente ha confermato
          this.isLoading = true; // Mostra lo spinner durante la cancellazione
          this.pdcService.deletePdc(codiceUfficiale, codiceClasse, codiceFamiglia).subscribe({
            next: () => {
              this.notificationService.success('WEB_OK_0004 - Salvataggio effettuato correttamente', 'Successo');
              this.isLoading = false;
              // Torna alla pagina dei risultati precedente invece che alla ricerca generica
              this.router.navigate(['/pdc', 'results']);
            },
            error: (err: any) => {
              // Il servizio PdcService si occupa di mostrare il messaggio di errore specifico tramite NotificationService
              // Qui gestiamo solo la logica di UI come nascondere lo spinner
              this.isLoading = false; // Nasconde lo spinner
            }
          });
        }
        // Se result è false o undefined, l'utente ha annullato o chiuso il dialogo
      });
    }
  }

}