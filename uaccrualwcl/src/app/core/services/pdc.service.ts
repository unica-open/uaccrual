/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { PagedPdc, Pdc, PdcSearchCriteria, PdcContoRelazione, PdcTreeNode } from '../../features/pdc/model/pdc.model';
import { buildFilterFromCriteria } from '../utils/filter-converter.util';
import { NotificationService } from './notification.service';
import { Store } from '@ngxs/store';
import { ContextState } from '../store/context/context.state';
import { AuthStorageService } from './auth-storage.service';

@Injectable({
  providedIn: 'root'
})
export class PdcService {

  private baseUrl = '/accrualsbsrv/api/v1'; // Endpoint aggiornato per il backend

  // Gestione stato per la lista
  private pdcListSubject = new BehaviorSubject<Pdc[]>([]);
  public pdcList$ = this.pdcListSubject.asObservable();
  private totalElementsSubject = new BehaviorSubject<number>(0);
  public totalElements$ = this.totalElementsSubject.asObservable();
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();
  public loadingSubject$ = this.loadingSubject;
  private filtersSubject = new BehaviorSubject<PdcSearchCriteria>({});
  public filters$ = this.filtersSubject.asObservable();
  private pageSizeSubject = new BehaviorSubject<number>(10); // Valore predefinito
  public pageSize$ = this.pageSizeSubject.asObservable();

  // Subject per notificare quando non ci sono risultati
  private noResultsSubject = new BehaviorSubject<boolean>(false);
  public noResults$ = this.noResultsSubject.asObservable();
  public noResultsSubject$ = this.noResultsSubject;

  // Getter pubblici per accedere ai valori correnti dei BehaviorSubject
  get pdcListValue(): Pdc[] {
    return this.pdcListSubject.value;
  }

  get totalElementsValue(): number {
    return this.totalElementsSubject.value;
  }

  get loadingValue(): boolean {
    return this.loadingSubject.value;
  }

  get filtersValue(): PdcSearchCriteria {
    return this.filtersSubject.value;
  }

  get pageSizeValue(): number {
    return this.pageSizeSubject.value;
  }

  get noResultsValue(): boolean {
    return this.noResultsSubject.value;
  }

  /**
   * Metodo per aggiornare i filtri di ricerca
   */
  updateFilters(filters: PdcSearchCriteria): void {
    this.filtersSubject.next(filters);
  }

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private store: Store,
    private authStorage: AuthStorageService
  ) { }

  // Funzione privata per mappare HttpErrorResponse a codice WEB_...
  private mapErrorToWebCode(error: HttpErrorResponse): string {
    // Esempio di mappatura basata sullo status code e/o sul messaggio di errore
    // Questa logica dipende da come il backend struttura i suoi errori
    if (error.status === 409) { // Conflict
      // Potrebbe essere un errore di unicità
      if (error.error && error.error.message && error.error.message.includes('uniqueness')) {
        return 'WEB_ERR_0005';
      }
      // Potrebbe essere un errore di concorrenza (Optimistic Locking)
      if (error.error && error.error.message && error.error.message.includes('concurrency')) {
        return 'WEB_ERR_0013';
      }
      // Altri errori 409 specifici...
    }
    if (error.status === 400) { // Bad Request
      // Potrebbe essere un errore di validazione lato backend
      // Potrebbe essere un WEB_ERR_0002 o un codice specifico
      // Dipende dal messaggio di errore
      if (error.error && error.error.message) {
        // Aggiungi logica per riconoscere messaggi specifici
        // Ad esempio, se il backend dice "obbligatorio"
        if (error.error.message.includes('obbligatorio')) {
          return 'WEB_ERR_0002';
        }
        // Se il backend dice "univoco"
        if (error.error.message.includes('univoco')) {
          return 'WEB_ERR_0005';
        }
      }
    }
    // Gestione specifica per errori di violazione di unicità (HTTP 409 Conflict)
    if (error.status === 409) {
      if (error.error && error.error.message) {
        // Controlla se il messaggio contiene termini relativi a violazioni di unicità
        if (error.error.message.toLowerCase().includes('unique') ||
            error.error.message.toLowerCase().includes('duplicate') ||
            error.error.message.toLowerCase().includes('esiste') ||
            error.error.message.toLowerCase().includes('gia') ||
            error.error.message.toLowerCase().includes('already')) {
          return 'WEB_ERR_0005';
        }
      }
      // Se non troviamo un messaggio specifico ma è un 409, potrebbe essere comunque un errore di unicità
      return 'WEB_ERR_0005';
    }
    if (error.status === 404) { // Not Found
      // Potrebbe essere un errore specifico come "conto non trovato"
      // Per ora, lo trattiamo come errore generico
    }
    if (error.status === 403) { // Forbidden
      // Potrebbe essere un errore di permessi
      // Per ora, lo trattiamo come errore generico
    }
    if (error.status === 401) { // Unauthorized
      // Potrebbe essere un errore di autenticazione
      // Per ora, lo trattiamo come errore generico
    }
    // Per qualsiasi altro errore, ritorna il codice generico
    return 'WEB_ERR_0001';
  }

  updatePageSize(size: number): void {
    this.pageSizeSubject.next(size);
  }

  private getDefaultEnteCodice(): string {
    // Ottieni il codice ente dallo ContextState (fonte primaria)
    const enteFromState = this.store.selectSnapshot(ContextState.getCurrentEnte);

    // Se non è disponibile, prova a recuperare da sessionStorage come fallback
    return enteFromState || this.authStorage.getEnteCodice() || '';
  }

  private lastRequestTime: number = 0;
  private requestCooldown: number = 1000; // 1 secondo di cooldown tra le richieste

  // Nuovo metodo per la ricerca avanzata utilizzando il nuovo endpoint POST /pdc/search
  ricercaPdc(
    filters: PdcSearchCriteria,
    page: number = 0, // Corrisponde a offset
    size?: number, // Corrisponde a limit - opzionale, usa il valore dallo stato se non fornito
    sort: string = 'codice_ufficiale,asc', // Corrisponde a sort - ordinamento default per codice ufficiale
    bypassCooldown: boolean = false // Parametro per bypassare il cooldown quando necessario
  ): Observable<PagedPdc<Pdc>> {
    const now = Date.now();
    // Preveniamo troppe richieste ravvicinate
    if (!bypassCooldown && now - this.lastRequestTime < this.requestCooldown) {
      console.log('Richiesta ignorata - cooldown attivo');
      // Invece di inviare una richiesta con corpo vuoto, ritorniamo un observable con un errore o un risultato vuoto
      return new Observable<PagedPdc<Pdc>>(observer => {
        observer.next({ data: [], page: 0, page_size: 0, total_elements: 0 } as PagedPdc<Pdc>);
        observer.complete();
      }); // Ritorna un observable con risultato vuoto in caso di cooldown
    }

    this.lastRequestTime = now;
    this.filtersSubject.next(filters);

    // Se size non è fornito, usa il valore corrente dallo stato condiviso
    const actualSize = size !== undefined ? size : this.pageSizeSubject.value;

    this.loadingSubject.next(true);

    // Convertiamo i nomi dei campi dal formato camelCase al formato snake_case richiesto dal backend
    const convertedFilters: any = {};
    Object.keys(filters).forEach(key => {
      switch (key) {
        case 'classe':
          convertedFilters['pdc_classe_codice'] = (filters as any)[key];
          break;
        case 'tipo':
          convertedFilters['pdc_tipo_codice'] = (filters as any)[key];
          break;
        case 'codiceEvento':
          convertedFilters['codice_evento'] = (filters as any)[key];
          break;
        case 'stato':
          convertedFilters['pdc_stato_codice'] = (filters as any)[key];
          break;
        case 'codice':
          // Il campo 'codice' corrisponde al codice ufficiale del conto
          convertedFilters['codice_ufficiale'] = (filters as any)[key];
          break;
        case 'collegamento_bil_itas':
          // CORREZIONE BUG #3: Includere il filtro per il codice di bilancio ITAS
          convertedFilters['collegamento_bil_itas'] = (filters as any)[key];
          break;
        default:
          convertedFilters[key] = (filters as any)[key];
          break;
      }
    });

    // Costruiamo l'oggetto filtro da inviare nel body
    // Secondo la specifica OpenAPI, il body deve essere un oggetto PdcFilter
    const searchBody = convertedFilters; // Solo i filtri convertiti, senza parametri di paginazione

    // Costruiamo i parametri di query
    let params = new HttpParams()
      .set('sort', sort)
      .set('limit', actualSize.toString())
      .set('offset', (page * actualSize).toString());

    // Chiamata al nuovo endpoint POST /pdc/search
    // Gli header X-Ente-Codice e X-Anno-Bilancio sono letti dal JWT dal backend
    return this.http.post<PagedPdc<Pdc>>(`${this.baseUrl}/pdc/search`, searchBody, {
      params: params
    }).pipe(
      tap(response => {
        this.pdcListSubject.next(response.data); // OpenAPI spec uses 'data' for the array
        this.totalElementsSubject.next(response.total_elements); // OpenAPI spec uses 'total_elements'
        this.loadingSubject.next(false);

        // Se non ci sono risultati, emetti un messaggio e rimani nella pagina di ricerca
        if (response.data.length === 0) {
          console.log('WEB_INF_0003 - Nessun dato trovato');
          // Notifica che non ci sono risultati
          this.noResultsSubject.next(true);
          // Il messaggio verrà visualizzato come tooltip nel componente di ricerca
        } else {
          // Assicuriamoci che il flag sia falso quando ci sono risultati
          this.noResultsSubject.next(false);
        }
      }),
      catchError(error => {
        console.error('Errore durante la ricerca PdC:', error);
        this.loadingSubject.next(false);
        // Emetti un messaggio di errore
        console.error('WEB_ERR_0001 - Si è verificato un errore imprevisto:', error.message || error);
        return throwError(() => error);
      })
    );
  }

  // Metodo per ottenere il dettaglio consultato
  getPdcConsultato(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string): Observable<Pdc> {
    return this.http.get<Pdc>(`${this.baseUrl}/pdc/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}`);
  }


  // Metodi per caricare dati per dropdown
  getClassi(): Observable<Array<{ id: string, codice: string, descrizione: string, segno?: string, tipoImporto?: string }>> {
    return this.http.get<Array<{ id: string, codice: string, descrizione: string, segno?: string, tipoImporto?: string }>>(`${this.baseUrl}/pdc/classi`);
  }

  getTipi(): Observable<Array<{ id: string, codice: string, descrizione: string }>> {
    return this.http.get<Array<{ id: string, codice: string, descrizione: string }>>(`${this.baseUrl}/pdc/tipi`);
  }

  getEventi(): Observable<Array<{ id: string, codice: string, descrizione: string }>> {
    // Gli header X-Ente-Codice e X-Anno-Bilancio sono letti dal JWT dal backend
    return this.http.get<Array<{ id: string, codice: string, descrizione: string }>>(`${this.baseUrl}/eventi`);
  }

  getStati(): Observable<Array<{ id: string, codice: string, descrizione: string }>> {
    return this.http.get<Array<{ id: string, codice: string, descrizione: string }>>(`${this.baseUrl}/pdc/stati`);
  }

  getFamiglie(): Observable<Array<{ id: string, codice: string, descrizione: string, modificabile: boolean, ricercabile: boolean }>> {
    return this.http.get<Array<{ id: string, codice: string, descrizione: string, modificabile: boolean, ricercabile: boolean }>>(`${this.baseUrl}/pdc/famiglie`);
  }

  // Metodo per determinare se un conto è modificabile
  isPdcModificabile(pdc: Pdc): boolean {
    // Otteniamo le informazioni dallo store ContextState
   // const canUpdatePdc = this.store.selectSnapshot(ContextState.canUpdatePdc);
    const isFaseBilancioAllowed = this.store.selectSnapshot(ContextState.isFaseBilancioAllowed);

    // La logica completa richiede:
    // 1. Utente ha permesso: OP-SPE-updPdc (verificato tramite ContextState.canUpdatePdc)
    // 2. Fase Bilancio NON è: PLURIENNALE, PREVISIONE, CHIUSO
    // 3. Famiglia.flagModificabile = TRUE
    // return !!canUpdatePdc && !!isFaseBilancioAllowed && !!pdc.modificabile;

    return  !!isFaseBilancioAllowed && !!pdc.modificabile;

  }

  // Metodo per determinare se un conto è eliminabile
  isPdcEliminabile(pdc: Pdc): boolean {
    // Otteniamo le informazioni dallo store ContextState
    const canDeletePdc = this.store.selectSnapshot(ContextState.canDeletePdc);
    const isFaseBilancioAllowed = this.store.selectSnapshot(ContextState.isFaseBilancioAllowed);

    // La logica completa richiede:
    // 1. Utente ha permesso: OP-SPE-delPdc (verificato tramite ContextState.canDeletePdc)
    // 2. Fase Bilancio NON è: PLURIENNALE, PREVISIONE, CHIUSO
    // 3. Famiglia.flagModificabile = TRUE
    // 4. Conto.flagFoglia = TRUE
    //return !!canDeletePdc && !!isFaseBilancioAllowed && !!pdc.eliminabile && !!pdc.foglia;
    return !!isFaseBilancioAllowed && !!pdc.eliminabile && !!pdc.foglia;

  }

  // Metodo per determinare se è possibile inserire un figlio per un conto
  canInsertPdcFiglio(pdc: Pdc): boolean {
    // La logica è demandata al backend che calcola inseribile_figlio
    // considerando: permessi, fase bilancio, foglia_non_foglia,
    // e compatibilità famiglie figli esistenti
    return !!pdc.inseribile_figlio;
  }

  // Metodi per la gestione del conto PdC utilizzando l'entità Pdc
  getPdcById(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string): Observable<Pdc> {
    return this.http.get<Pdc>(`${this.baseUrl}/pdc/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}`);
  }

  // Metodo ausiliario per ottenere un PDC usando solo il codicePdc (per casi specifici dove non abbiamo la tripletta completa)
  getPdcByCodicePdc(codicePdc: string): Observable<Pdc> {
    // Cerchiamo il PDC usando il vecchio endpoint di ricerca con il codicePdc come filtro
    const filters = { codice: codicePdc }; // Cerchiamo per codice ufficiale
    return this.ricercaPdc(filters, 0, 1).pipe( // Cerchiamo solo il primo risultato
      map(result => {
        if (result.data && result.data.length > 0) {
          const pdc = result.data[0];
          // Ora abbiamo tutte le informazioni necessarie
          return pdc;
        } else {
          throw new Error(`PDC con codice ${codicePdc} non trovato`);
        }
      })
    );
  }

  insertPdc(pdc: Pdc): Observable<Pdc> {
    return this.http.post<Pdc>(`${this.baseUrl}/pdc`, pdc).pipe(
      catchError((error: HttpErrorResponse) => {
        const webCode = this.mapErrorToWebCode(error);
        this.notificationService.showMessageByCode(webCode);
        // Rilancia l'errore originale per mantenere il comportamento per i componenti che lo gestiscono
        return throwError(() => error);
      })
    );
  }

  updatePdc(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string, pdc: Pdc): Observable<Pdc> {
    return this.http.put<Pdc>(`${this.baseUrl}/pdc/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}`, pdc).pipe(
      catchError((error: HttpErrorResponse) => {
        const webCode = this.mapErrorToWebCode(error);
        this.notificationService.showMessageByCode(webCode);
        // Rilancia l'errore originale per mantenere il comportamento per i componenti che lo gestiscono
        return throwError(() => error);
      })
    );
  }

  deletePdc(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/pdc/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}`).pipe(
      catchError((error: HttpErrorResponse) => {
        const webCode = this.mapErrorToWebCode(error);
        this.notificationService.showMessageByCode(webCode);
        // Rilancia l'errore originale per mantenere il comportamento per i componenti che lo gestiscono
        return throwError(() => error);
      })
    );
  }

  // Metodo per ottenere tutti i risultati di una ricerca (senza paginazione)
  getAllPdcResults(filters: PdcSearchCriteria, sort: string = 'codice_ufficiale,asc'): Observable<Pdc[]> {
    // Per ottenere tutti i risultati, facciamo una ricerca con limit=-1 (nuovo approccio supportato dal backend)
    const maxSize = -1; // Valore speciale per ottenere tutti i record possibili

    // Convertiamo i nomi dei campi dal formato camelCase al formato snake_case richiesto dal backend
    const convertedFilters: any = {};
    Object.keys(filters).forEach(key => {
      switch (key) {
        case 'classe':
          convertedFilters['pdc_classe_codice'] = (filters as any)[key];
          break;
        case 'tipo':
          convertedFilters['pdc_tipo_codice'] = (filters as any)[key];
          break;
        case 'codiceEvento':
          convertedFilters['codice_evento'] = (filters as any)[key];
          break;
        case 'stato':
          convertedFilters['pdc_stato_codice'] = (filters as any)[key];
          break;
        case 'codice':
          // Il campo 'codice' corrisponde al codice ufficiale del conto
          convertedFilters['codice_ufficiale'] = (filters as any)[key];
          break;
        case 'collegamento_bil_itas':
          // CORREZIONE BUG #3: Includere il filtro per il codice di bilancio ITAS
          convertedFilters['collegamento_bil_itas'] = (filters as any)[key];
          break;
        default:
          convertedFilters[key] = (filters as any)[key];
          break;
      }
    });

    // Costruiamo l'oggetto filtro da inviare nel body
    const searchBody = convertedFilters;

    // Costruiamo i parametri di query
    let params = new HttpParams()
      .set('sort', sort)
      .set('limit', maxSize.toString())
      .set('offset', '0');

    // Chiamata al nuovo endpoint POST /pdc/search
    // Gli header X-Ente-Codice e X-Anno-Bilancio sono letti dal JWT dal backend
    return this.http.post<PagedPdc<Pdc>>(`${this.baseUrl}/pdc/search`, searchBody, {
      params: params
    }).pipe(
      map(response => response.data), // Estraiamo solo l'array dei dati
      catchError(error => {
        console.error('Errore durante la ricerca completa PdC:', error);
        // Emetti un messaggio di errore
        console.error('WEB_ERR_0001 - Si è verificato un errore imprevisto:', error.message || error);
        return throwError(() => error);
      })
    );
  }

  // Metodo per ottenere i conti collegati per tipo relazione
  getContiCollegati(codiceUfficiale: string, codiceClasse: string, codiceFamiglia: string, tipoRelazione: 'FONDO' | 'CORRELAZIONE'): Observable<Pdc[]> {
    // Costruiamo l'URL per l'endpoint delle relazioni usando la tripletta
    const url = `${this.baseUrl}/pdc/${codiceUfficiale}/${codiceClasse}/${codiceFamiglia}/relazioni`;

    // Impostiamo il parametro 'tipo' per filtrare per FONDO o CORRELAZIONE
    const params = new HttpParams().set('tipo', tipoRelazione);

    // Chiamata all'endpoint reale per ottenere i conti collegati
    return this.http.get<PdcContoRelazione[]>(url, {
      params: params
    }).pipe(
      map(relazioni => {
        // Convertiamo le relazioni ricevute in oggetti Pdc per la visualizzazione
        return relazioni.map(relazione => ({
          // Mappiamo i campi della relazione ai campi di un oggetto Pdc
          id: relazione.codice, // Usiamo il codice come ID
          codice: relazione.codice,
          codice_ufficiale: relazione.codice, // Assumiamo che il codice sia anche il codice ufficiale
          descrizione: relazione.descrizione,
          livello: relazione.livello,
          segmento: relazione.segmento,
          // Mappiamo i campi classe e tipo come oggetti con descrizione
          pdc_classe: { id: '', codice: '', descrizione: relazione.classe },
          pdc_tipo: { id: '', codice: '', descrizione: relazione.tipo },
          // Impostiamo alcuni valori di default
          foglia: true, // Assumiamo che i conti collegati siano nodi foglia
          modificabile: false,
          eliminabile: false,
          inseribile_figlio: false,
          // Altri campi necessari per evitare errori
          id_pdc_padre: undefined,
          codice_completo: relazione.codice,
          partita: undefined,
          ordine: undefined,
          collegamento_bil_itas: undefined,
          fondo_dotazione: undefined,
          foglia_non_foglia: undefined,
          pdc_padre: undefined,
          pdc_famiglia: undefined,
          pdc_stato: undefined,
          ente: undefined,
          inizio_validita: undefined,
          fine_validita: undefined,
          classe: undefined,
          tipo: undefined,
          evento: undefined,
          stato: undefined,
          id_classe_conto: undefined,
          id_tipo_conto: undefined,
          id_stato_conto: undefined,
          flag_foglia: undefined,
          flag_fondo_dotazione: undefined,
          flag_a_partita: undefined,
          codice_bilancio: undefined,
          data_inizio_validita: undefined,
          data_fine_validita: undefined,
          version: undefined
        }));
      }),
      catchError(error => {
        console.error('Errore durante il recupero dei conti collegati:', error);
        // In caso di errore, ritorniamo comunque un array vuoto invece di propagare l'errore
        // in modo che l'interfaccia possa continuare a funzionare
        return of([]);
      })
    );
  }

  // Metodo per la ricerca dei conti PDC in formato albero
  searchPdcTree(filters: PdcSearchCriteria): Observable<PdcTreeNode[]> {
    // Interfaccia per la risposta del servizio
    interface PdcTreeResponse {
      nodes: PdcTreeNode[];
    }

    // Chiamata all'endpoint POST /pdc/search-tree
    // Gli header X-Ente-Codice e X-Anno-Bilancio sono letti dal JWT dal backend
    return this.http.post<PdcTreeResponse>(`${this.baseUrl}/pdc/search-tree`, filters).pipe(
      map(response => {
        const nodes = response.nodes;
        // Imposta il flag noResults in base al numero di risultati
        this.noResultsSubject.next(nodes.length === 0);
        return nodes;
      }),
      catchError(error => {
        console.error('Errore durante la ricerca PdC in formato albero:', error);
        // Emetti un messaggio di errore
        console.error('WEB_ERR_0001 - Si è verificato un errore imprevisto:', error.message || error);
        // Resetta il flag noResults in caso di errore
        this.noResultsSubject.next(false);
        return throwError(() => error);
      })
    );
  }

  /**
   * Esporta i risultati della ricerca PDC in formato CSV tramite backend
   * @param filters Filtri di ricerca
   * @param sort Ordinamento (default: 'codice_ufficiale,asc')
   * @returns Observable<Blob> contenente il file CSV
   */
  exportPdcList(filters: PdcSearchCriteria, sort: string = 'codice_ufficiale,asc'): Observable<Blob> {
    // Convertiamo i nomi dei campi dal formato camelCase al formato snake_case richiesto dal backend
    const convertedFilters: any = {};
    Object.keys(filters).forEach(key => {
      switch (key) {
        case 'classe':
          convertedFilters['pdc_classe_codice'] = (filters as any)[key];
          break;
        case 'tipo':
          convertedFilters['pdc_tipo_codice'] = (filters as any)[key];
          break;
        case 'codiceEvento':
          convertedFilters['codice_evento'] = (filters as any)[key];
          break;
        case 'stato':
          convertedFilters['pdc_stato_codice'] = (filters as any)[key];
          break;
        case 'codice':
          // Il campo 'codice' corrisponde al codice ufficiale del conto
          convertedFilters['codice_ufficiale'] = (filters as any)[key];
          break;
        case 'collegamento_bil_itas':
          // CORREZIONE BUG #3: Includere il filtro per il codice di bilancio ITAS
          convertedFilters['collegamento_bil_itas'] = (filters as any)[key];
          break;
        default:
          convertedFilters[key] = (filters as any)[key];
          break;
      }
    });

    // Chiamata all'endpoint POST /pdc/export
    return this.http.post(`${this.baseUrl}/pdc/export`, convertedFilters, {
      params: new HttpParams().set('sort', sort),
      responseType: 'blob'
    });
  }

  /**
   * ALG-003: Verifica conto da inserire nella scrittura.
   * Chiama POST /pdc/valida-conto per validare esistenza, validità temporale e attributo foglia.
   * Risolve con void se ok, altrimenti errore con codice WEB_.
   */
  validaContoDaInserire(codiceConto: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/pdc/valida-conto`, { codice_conto: codiceConto }).pipe(
      catchError(error => {
        console.error('Errore durante la validazione del conto:', error);
        return throwError(() => error);
      })
    );
  }
}