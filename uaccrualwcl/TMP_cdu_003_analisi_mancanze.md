# Analisi delle mancanze rispetto al CDU-003: Gestisci conto PdC Accrual

## Documento di riferimento
- **Documento:** CDU_pdc_003_V01-Gestisci_conto_PdC_Accrual.html
- **Versione:** V01
- **Data:** 2026-01-08

## Situazione attuale
L'applicazione ha una buona base funzionale per la gestione dei conti PdC Accrual, ma molte delle validazioni e logiche di business specificate nel documento CDU-003 sono mancanti o incomplete, specialmente quelle relative alla gestione delle dipendenze tra conti, alle conferme durante l'aggiornamento e alla verifica della presenza di prime note.

## Mancanze identificate

### 1. Validazioni lato frontend mancanti

#### 1.1 Verifica univocità conto (codice ufficiale + classe)
- **Descrizione:** Il documento richiede che la combinazione codice ufficiale conto e classe sia univoca
- **Stato attuale:** Manca completamente
- **Riferimento:** Sezione 2.10.1 "Verifica conto Accrual da salvare"

#### 1.2 Verifica presenza prime note quando si imposta fineValidità
- **Descrizione:** Verificare che non esistano prime note in stato diverso da 'Annullato' collegate al conto oppure ai suoi conti figli, con data registrazione successiva a fineValidità
- **Stato attuale:** Manca completamente (nessun servizio per le prime note)
- **Riferimento:** Sezione 2.10.1 "Verifica conto Accrual da salvare"
- **Messaggio di errore:** WEB_ERR_0027

#### 1.3 Gestione conferme durante l'aggiornamento
- **Descrizione:** Mancano le conferme specifiche richieste dal documento:
  - Se il conto ha figli e si aggiunge fineValidita: messaggio WEB_CON_0008
  - Se il conto ha figli e si toglie fineValidita: messaggio WEB_CON_0009
  - Se il conto ha figli e si cambia stato da 'Attivo' a 'Non Attivo': messaggio WEB_CON_0010
  - Se il conto ha figli e si cambia stato da 'Non Attivo' ad 'Attivo': messaggio WEB_CON_0011
- **Stato attuale:** Parzialmente implementata
- **Riferimento:** Sezione 2.10.1 "Verifica conto Accrual da salvare"

### 2. Servizi mancanti

#### 2.1 Servizio per la gestione delle prime note
- **Descrizione:** Non esiste alcun servizio per la gestione delle prime note
- **Stato attuale:** Manca completamente
- **Impatto:** Impedisce di implementare la validazione richiesta dal documento (WEB_ERR_0027)

#### 2.2 Metodi per ottenere i conti figli di un conto
- **Descrizione:** Non esiste alcun metodo per ottenere ricorsivamente i conti figli di un conto
- **Stato attuale:** Manca completamente
- **Impatto:** Essenziale per le validazioni e per l'aggiornamento ricorsivo

### 3. Logica di business mancante

#### 3.1 Aggiornamento ricorsivo dei conti figli
- **Descrizione:** Quando si aggiorna un conto padre, il sistema dovrebbe aggiornare ricorsivamente anche i figli
- **Stato attuale:** Manca completamente
- **Riferimento:** Sezione 2.10.3 "Aggiorna conto Accrual"

#### 3.2 Algoritmo "Verifica conto Accrual da eliminare"
- **Descrizione:** Il documento richiede di verificare che il conto non abbia legami validi con entità valide: conti figli, causali, prime note, soggetti, saldi
- **Stato attuale:** Implementazione parziale
- **Riferimento:** Sezione 2.10.2 "Verifica conto Accrual da eliminare"

### 4. Interfaccia utente

#### 4.1 Messaggi di conferma mancanti
- **Descrizione:** I messaggi specifici richiesti dal documento (WEB_CON_0008, WEB_CON_0009, WEB_CON_0010, WEB_CON_0011) non sono implementati
- **Stato attuale:** Manca completamente
- **Riferimento:** Sezione 2.10.1 "Verifica conto Accrual da salvare"

#### 4.2 Visualizzazione delle sezioni dinamiche
- **Descrizione:** La Sezione b (conti collegati tipo FONDO) e la Sezione c (conti collegati tipo CORRELAZIONE) dovrebbero essere mostrate solo se esistono effettivamente dei legami
- **Stato attuale:** Potenzialmente incompleta
- **Riferimento:** Sezione 2.8.3.2 "Tabella 2 – Aggiorna conto"

### 5. Altre considerazioni

#### 5.1 Mapping completo messaggi di errore
- **Descrizione:** Il documento specifica molti messaggi di errore specifici che devono essere mostrati in determinate situazioni
- **Stato attuale:** Non tutti questi messaggi sono mappati nel servizio di notifica

#### 5.2 Validazioni backend
- **Descrizione:** Molte delle validazioni richieste devono essere implementate anche nel backend
- **Stato attuale:** Non verificabili dal frontend

## Priorità di implementazione

### Alta priorità
1. Implementazione della verifica univocità conto (codice ufficiale + classe)
2. Implementazione della gestione delle conferme durante l'aggiornamento
3. Implementazione dell'aggiornamento ricorsivo dei conti figli

### Media priorità
1. Servizio per la gestione delle prime note
2. Metodi per ottenere i conti figli di un conto
3. Visualizzazione condizionale delle sezioni FONDO e CORRELAZIONE

### Bassa priorità
1. Mapping completo messaggi di errore
2. Implementazione completa dell'algoritmo "Verifica conto Accrual da eliminare"

## Note
Molte delle funzionalità mancanti principalmente riguardano:
- Validazioni di business complesse
- Interazioni tra conti correlati
- Gestione delle dipendenze e conseguenze ricorsive delle modifiche
- Integrazione con altri moduli (prime note)

Queste lacune potrebbero compromettere la correttezza e la coerenza dei dati nel sistema, specialmente quando si tratta di modificare conti che hanno relazioni con altri conti o con documenti contabili.