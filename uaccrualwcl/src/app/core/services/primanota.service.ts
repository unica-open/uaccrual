/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { Page } from '../../shared/models/page.model';
import { PrimaNotaDetail, PrimaNotaSearchFilter, PrimaNotaSearchResult, PrimaNotaContoCollegatoPage, PrimaNotaAttributoPage, PrimaNotaCollegataPage, ContiTotali } from '../../features/primanota/models/primanota.model';

// Interfaces for dropdown data
export interface DropdownOption {
  id: string;
  codice: string;
  descrizione: string;
  statoDefaultPrimaNota?: string; // Stato di default per gli eventi
}

export interface FormattedDropdownOption {
  value: string;
  label: string;
  statoDefaultPrimaNota?: string; // Stato di default per gli eventi
}

export interface PrimaNotaDropdowns {
  tipi: FormattedDropdownOption[];
  fonti: FormattedDropdownOption[];
  eventi: FormattedDropdownOption[];
  causali: FormattedDropdownOption[];
  stati: FormattedDropdownOption[];
}

@Injectable({
  providedIn: 'root'
})
export class PrimanotaService {

  private readonly baseUrl = '/accrualsbsrv/api/v1'; // Uniformato con pdc.service.ts
  private readonly apiUrl = `${this.baseUrl}/primanota`; // Costruito in base al baseUrl

  // Gestione stato per la lista
  private primaNotaListSubject = new BehaviorSubject<PrimaNotaSearchResult[]>([]);
  public primaNotaList$ = this.primaNotaListSubject.asObservable();
  private totalElementsSubject = new BehaviorSubject<number>(0);
  public totalElements$ = this.totalElementsSubject.asObservable();
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();
  public loadingSubject$ = this.loadingSubject;
  private filtersSubject = new BehaviorSubject<PrimaNotaSearchFilter>({});
  public filters$ = this.filtersSubject.asObservable();
  private pageSizeSubject = new BehaviorSubject<number>(20); // Valore predefinito
  public pageSize$ = this.pageSizeSubject.asObservable();
  private currentPageSubject = new BehaviorSubject<number>(0); // Pagina corrente
  public currentPage$ = this.currentPageSubject.asObservable();
  private noResultsSubject = new BehaviorSubject<boolean>(false);
  public noResults$ = this.noResultsSubject.asObservable();

  // BehaviorSubject per gestire lo stato di incompleteWarning
  private incompleteWarningSubject = new BehaviorSubject<boolean>(false);
  public incompleteWarning$ = this.incompleteWarningSubject.asObservable();

  // Lista prime note non quadrate
  private incompleteListSubject = new BehaviorSubject<PrimaNotaSearchResult[]>([]);
  public incompleteList$ = this.incompleteListSubject.asObservable();
  private incompleteListLoadingSubject = new BehaviorSubject<boolean>(false);
  public incompleteListLoading$ = this.incompleteListLoadingSubject.asObservable();
  private incompleteListErrorSubject = new BehaviorSubject<boolean>(false);
  public incompleteListError$ = this.incompleteListErrorSubject.asObservable();

  // Getter per accedere ai valori come fa PDC
  get primaNotaListValue(): PrimaNotaSearchResult[] {
    return this.primaNotaListSubject.getValue();
  }

  get totalElementsValue(): number {
    return this.totalElementsSubject.getValue();
  }

  get loadingValue(): boolean {
    return this.loadingSubject.getValue();
  }

  get noResultsValue(): boolean {
    return this.noResultsSubject.getValue();
  }

  get filtersValue(): PrimaNotaSearchFilter {
    return this.filtersSubject.getValue();
  }

  get pageSizeValue(): number {
    return this.pageSizeSubject.getValue();
  }

  get currentPageValue(): number {
    return this.currentPageSubject.getValue();
  }

  constructor(
    private http: HttpClient
  ) {}

  /**
   * Gestisce gli errori HTTP
   * Per il 409 con confirmationRequired, l'errore viene gestito dall'ConfirmationInterceptor
   */
  private handleError(error: HttpErrorResponse) {
    console.log('handleError - Status:', error.status, 'Error:', error);
    // Propaga tutti gli errori normalmente
    return throwError(() => error);
  }

  // Metodo per eseguire la ricerca e aggiornare i BehaviorSubject come fa PDC
  ricercaPrimaNote(filter: PrimaNotaSearchFilter, page: number, size: number, sort: string = 'numeroProvvisorio,asc', updateState: boolean = true): Observable<Page<PrimaNotaSearchResult>> {
    this.loadingSubject.next(true);
    this.filtersSubject.next(filter);
    // Aggiorna la pagina corrente
    this.currentPageSubject.next(page);
    // Resetta il flag noResults quando inizia una nuova ricerca
    this.noResultsSubject.next(false);

    const params = new HttpParams()
        .set('sort', sort) // parametro sort impostato per primo come specificato nella documentazione
        .set('limit', size.toString())
        .set('offset', (page * size).toString()); // offset calcolato come page * size

    const body = { ...filter, soloNonQuadrate: filter.soloNonQuadrate ?? false };
    return this.http.post<Page<PrimaNotaSearchResult>>(`${this.apiUrl}/search`, body, { params }).pipe(
      tap((response: Page<PrimaNotaSearchResult>) => {
        if (updateState) {
          // Aggiorna i BehaviorSubject come fa PDC
          this.primaNotaListSubject.next(response.content);
          this.totalElementsSubject.next(response.totalElements);
          this.pageSizeSubject.next(size);
          this.currentPageSubject.next(page); // Aggiorna la pagina corrente

          // Se non ci sono risultati, emetti il flag
          if (response.totalElements === 0) {
            this.noResultsSubject.next(true);
          } else {
            this.noResultsSubject.next(false);
          }
        }
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        throw error;
      })
    );
  }

  search(filter: PrimaNotaSearchFilter, page: number, size: number, sort: string = 'numeroProvvisorio,asc'): Observable<Page<PrimaNotaSearchResult>> {
    const params = new HttpParams()
        .set('sort', sort) // parametro sort impostato per primo come specificato nella documentazione
        .set('limit', size.toString())
        .set('offset', (page * size).toString()); // offset calcolato come page * size
    const body = { ...filter, soloNonQuadrate: filter.soloNonQuadrate ?? false };
    return this.http.post<Page<PrimaNotaSearchResult>>(`${this.apiUrl}/search`, body, { params });
  }

  getByAnnoAndNumero(anno: number, numero: number): Observable<PrimaNotaDetail> {
    return this.http.get<PrimaNotaDetail>(`${this.apiUrl}/${anno}/${numero}`);
  }

  // Metodi per gli endpoint paginati dei tab nel dettaglio prima nota
  
  /**
   * Recupera i conti collegati a una prima nota con paginazione
   * GET /primanota/{anno}/{numero}/conti
   */
  getContiCollegati(
    anno: number,
    numero: number,
    page: number = 0,
    size: number = 20,
    codiceConto?: string  // Parametro opzionale per filtrare per codice conto
  ): Observable<Page<PrimaNotaContoCollegatoPage>> {
    let params = new HttpParams()
      .set('limit', size.toString())
      .set('offset', (page * size).toString());
    
    if (codiceConto) {
      params = params.set('codiceConto', codiceConto);
    }

    return this.http.get<Page<PrimaNotaContoCollegatoPage>>(
      `${this.apiUrl}/${anno}/${numero}/conti`,
      { params }
    );
  }

  /**
   * Recupera i totali dei conti collegati a una prima nota
   * GET /primanota/{anno}/{numero}/conti/totali
   */
  getTotaliConti(
    anno: number,
    numero: number
  ): Observable<ContiTotali> {
    return this.http.get<ContiTotali>(
      `${this.apiUrl}/${anno}/${numero}/conti/totali`
    );
  }

  /**
   * Recupera gli attributi collegati a una prima nota con paginazione
   * GET /primanota/{anno}/{numero}/attributi
   */
  getAttributiCollegati(
    anno: number,
    numero: number,
    page: number = 0,
    size: number = 20
  ): Observable<Page<PrimaNotaAttributoPage>> {
    const params = new HttpParams()
      .set('limit', size.toString())
      .set('offset', (page * size).toString());

    return this.http.get<Page<PrimaNotaAttributoPage>>(
      `${this.apiUrl}/${anno}/${numero}/attributi`,
      { params }
    );
  }

  /**
   * Recupera le prime note collegate con paginazione
   * GET /primanota/{anno}/{numero}/collegate
   */
  getPrimeNoteCollegate(
    anno: number,
    numero: number,
    page: number = 0,
    size: number = 20
  ): Observable<Page<PrimaNotaCollegataPage>> {
    const params = new HttpParams()
      .set('limit', size.toString())
      .set('offset', (page * size).toString());

    return this.http.get<Page<PrimaNotaCollegataPage>>(
      `${this.apiUrl}/${anno}/${numero}/collegate`,
      { params }
    );
  }

  // Methods to get dropdown data for prima nota search filters
  getTipiPrimaNota(): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.apiUrl}/tipi`).pipe(
      map(tipi =>
        tipi
          .map(tipo => ({
            value: tipo.codice,
            label: `${tipo.codice}-${tipo.descrizione}`
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  getFonti(): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.apiUrl}/fonti`).pipe(
      map(fonti =>
        fonti
          .map(fonte => ({
            value: fonte.codice,
            label: `${fonte.codice}-${fonte.descrizione}`
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  getEventiContabili(): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.baseUrl}/eventi`).pipe(
      map(eventi =>
        eventi
          .map(evento => ({
            value: evento.codice,
            label: `${evento.codice}-${evento.descrizione}`,
            statoDefaultPrimaNota: evento.statoDefaultPrimaNota
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  getEventiContabiliByFonte(fonteCodice: string): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.baseUrl}/eventi/fonte/${fonteCodice}`).pipe(
      map(eventi =>
        eventi
          .map(evento => ({
            value: evento.codice,
            label: `${evento.codice}-${evento.descrizione}`,
            statoDefaultPrimaNota: evento.statoDefaultPrimaNota
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  getCausali(): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.apiUrl}/causali`).pipe(
      map(causali =>
        causali
          .map(causale => ({
            value: causale.codice,
            label: `${causale.codice}-${causale.descrizione}`
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  /**
   * @param statoCodice se valorizzato, restituisce le sole causali il cui stato corrente ha quel codice
   */
  getCausaliByEvento(eventoCodice: string, statoCodice?: string): Observable<FormattedDropdownOption[]> {
    let params = new HttpParams();
    if (statoCodice) {
      params = params.set('statoCodice', statoCodice);
    }

    return this.http.get<DropdownOption[]>(`${this.baseUrl}/causali/evento/${eventoCodice}`, { params }).pipe(
      map(causali =>
        causali
          .map(causale => ({
            value: causale.codice,
            label: `${causale.codice}-${causale.descrizione}`
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  getStatiPrimaNota(): Observable<FormattedDropdownOption[]> {
    return this.http.get<DropdownOption[]>(`${this.apiUrl}/stati`).pipe(
      map(stati =>
        stati
          .map(stato => ({
            value: stato.codice,
            label: `${stato.codice}-${stato.descrizione}`
          }))
          .sort((a, b) => a.value.localeCompare(b.value)) // Ordina per codice
      )
    );
  }

  /**
   * Annulla una prima nota
   * POST /primanota/{anno}/{numero}/annulla
   */
  annullaPrimaNota(anno: number, numero: number): Observable<{ messaggio: string }> {
    return this.http.post<{ messaggio: string }>(
      `${this.apiUrl}/${anno}/${numero}/annulla`,
      null
    );
  }

  /**
   * Riapre una prima nota
   * POST /primanota/{anno}/{numero}/riapri
   */
  riapriPrimaNota(anno: number, numero: number): Observable<{ messaggio: string }> {
    return this.http.post<{ messaggio: string }>(
      `${this.apiUrl}/${anno}/${numero}/riapri`,
      null
    );
  }

  /**
   * Verifica se ci sono prime note incomplete
   * GET /primanota/check-incomplete
   */
  checkIncomplete(): Observable<{ hasIncomplete: boolean }> {
    return this.http.get<{ hasIncomplete: boolean }>(`${this.apiUrl}/check-incomplete`).pipe(
      tap(response => {
        this.incompleteWarningSubject.next(!!response.hasIncomplete);
      })
    );
  }

  /**
   * Recupera la lista delle prime note non quadrate
   * GET /primanota/incomplete
   */
  getIncompleteList(): Observable<PrimaNotaSearchResult[]> {
    this.incompleteListLoadingSubject.next(true);
    this.incompleteListErrorSubject.next(false);
    return this.http.get<PrimaNotaSearchResult[]>(`${this.apiUrl}/incomplete`).pipe(
      tap(list => {
        this.incompleteListSubject.next(list ?? []);
        this.incompleteListLoadingSubject.next(false);
      }),
      catchError(() => {
        this.incompleteListLoadingSubject.next(false);
        this.incompleteListErrorSubject.next(true);
        return of([]);
      })
    );
  }

  /**
   * Inserisce una nuova prima nota
   * POST /primanota
   */
  inserisci(dto: any): Observable<PrimaNotaDetail> {
    return this.http.post<PrimaNotaDetail>(this.apiUrl, dto);
  }

  /**
   * Aggiorna una prima nota esistente
   * PUT /primanota/{anno}/{numero}
   */
  aggiorna(anno: number, numero: number, dto: any): Observable<PrimaNotaDetail> {
    return this.http.put<PrimaNotaDetail>(`${this.apiUrl}/${anno}/${numero}`, dto);
  }

  /**
   * Valida una prima nota singola
   * POST /primanota/{anno}/{numero}/valida
   * Può restituire 409 Conflict se è richiesta conferma per la rinumerazione
   * @param mantieniNumeroDefinitivo true per mantenere il numero esistente, false per riassegnarlo, undefined per prima chiamata
   */
  validaPrimaNota(anno: number, numero: number, mantieniNumeroDefinitivo?: boolean): Observable<{ messaggio: string }> {
    let params = new HttpParams();
    if (mantieniNumeroDefinitivo !== undefined) {
      params = params.set('mantieniNumeroDefinitivo', mantieniNumeroDefinitivo.toString());
    }

    return this.http.post<{ messaggio: string }>(
      `${this.apiUrl}/${anno}/${numero}/valida`,
      null,
      { params }
    ).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Valida massivamente un elenco di prime note
   * POST /primanota/valida-massiva
   * Può restituire 409 Conflict se è richiesta conferma per la rinumerazione
   * @param mantieniNumeri true per mantenere i numeri esistenti, false per riassegnarli, undefined per prima chiamata
   */
  validaPrimeNoteMassiva(primaNoteIds: { anno: number, numero: number }[], mantieniNumeri?: boolean): Observable<{ messaggio: string }> {
    let params = new HttpParams();
    if (mantieniNumeri !== undefined) {
      params = params.set('mantieniNumeri', mantieniNumeri.toString());
    }

    return this.http.post<{ messaggio: string }>(
      `${this.apiUrl}/valida-massiva`,
      primaNoteIds,
      { params }
    ).pipe(
      catchError(error => this.handleError(error))
    );
  }

  /**
   * Esporta i risultati della ricerca Prima Nota in formato CSV tramite backend
   * @param filters Filtri di ricerca
   * @param sort Ordinamento (default: 'numeroProvvisorio,asc')
   * @returns Observable<Blob> contenente il file CSV
   */
  exportPrimaNotaList(filters: PrimaNotaSearchFilter, sort: string = 'numeroProvvisorio,asc'): Observable<Blob> {
    const params = new HttpParams().set('sort', sort);

    // Chiamata al nuovo endpoint POST /primanota/export
    return this.http.post(`${this.apiUrl}/export`, filters, {
      params: params,
      responseType: 'blob'
    }).pipe(
      catchError(error => {
        console.error('Errore durante l\'esportazione completa Prima Nota:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Effettua un refresh della ricerca con gli stessi parametri
   * Utile per aggiornare la lista dopo un inserimento o modifica
   */
  refresh(): Observable<Page<PrimaNotaSearchResult>> | null {
    const currentFilter = this.filtersValue;
    const currentPage = this.currentPageValue;
    const pageSize = this.pageSizeValue;

    if (Object.keys(currentFilter).length === 0 && currentPage === 0) {
      // Nessun filtro applicato, non c'è nulla da refreshare
      return null;
    }

    return this.ricercaPrimaNote(currentFilter, currentPage, pageSize, 'numeroProvvisorio,asc', true);
  }
}
