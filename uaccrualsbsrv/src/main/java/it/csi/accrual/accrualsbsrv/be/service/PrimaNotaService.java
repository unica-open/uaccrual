/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import it.csi.accrual.accrualsbsrv.dto.PrimaNotaDetailDTO;
import it.csi.accrual.accrualsbsrv.dto.PagePrimaNotaSearchResultDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchFilterDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchResultDTO;
import it.csi.accrual.accrualsbsrv.dto.PagedContoCollegatoDTO;
import it.csi.accrual.accrualsbsrv.dto.PagedAttributoCollegatoDTO;
import it.csi.accrual.accrualsbsrv.dto.PagedPrimaNotaCollegataDTO;
import it.csi.accrual.accrualsbsrv.dto.ContiTotaliDTO;
import java.util.List;

public interface PrimaNotaService {

    PagePrimaNotaSearchResultDTO searchPrimeNote(PrimaNotaSearchFilterDTO filter, Integer limit, Integer offset, String sort);

    PrimaNotaDetailDTO getPrimaNotaById(String idPrimaNota);

    PrimaNotaDetailDTO getPrimaNotaByAnnoAndNumero(Integer anno, Integer numero);

    boolean checkIncompletePrimeNote();

    List<PrimaNotaSearchResultDTO> getIncompletePrimeNote();

    List<it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota> getTipiPrimaNota();

    List<it.csi.accrual.accrualsbsrv.dto.FontePrimaNota> getFontiPrimaNota();

    List<it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota> getCausaliPrimaNota();

    List<it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota> getStatiPrimaNota();

    PagedContoCollegatoDTO getContiPrimaNota(Integer anno, Integer numero, Integer limit, Integer offset, String codiceConto);

    ContiTotaliDTO getTotaliContiPrimaNota(Integer anno, Integer numero);

    PagedAttributoCollegatoDTO getAttributiPrimaNota(Integer anno, Integer numero, Integer limit, Integer offset);

    PagedPrimaNotaCollegataDTO getPrimeNoteCollegate(Integer anno, Integer numero, Integer limit, Integer offset);

    /**
     * Annulla una prima nota identificata dall'anno e dal numero.
     * L'operazione imposta lo stato della prima nota ad 'ANNULLATA' e traccia l'utente che ha richiesto l'operazione.
     *
     * @param anno anno della prima nota
     * @param numero numero della prima nota
     * @param utenteCorrente codice univoco dell'operatore che esegue l'annullamento
     * @throws IllegalArgumentException se la prima nota non può essere annullata (es. entità contabile in fase PREVISIONE o CHIUSO)
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se la prima nota non esiste
     */
    void annullaPrimaNota(Integer anno, Integer numero, String utenteCorrente);

    /**
     * Riapre una prima nota identificata dall'anno e dal numero.
     * L'operazione riporta lo stato della prima nota da 'DEFINITIVA' a 'PROVVISORIA',
     * mantenendo il numero definitivo e la data di registrazione definitiva già assegnati.
     *
     * @param anno anno della prima nota
     * @param numero numero della prima nota
     * @param utenteCorrente codice univoco dell'operatore che esegue la riapertura
     * @throws IllegalArgumentException se la prima nota non può essere riaperta (es. entità contabile in fase PREVISIONE o CHIUSO, o stato non DEFINITIVA)
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se la prima nota non esiste
     */
    void riapriPrimaNota(Integer anno, Integer numero, String utenteCorrente);

    /**
     * Inserisce una nuova prima nota nel sistema.
     * L'operazione crea una nuova prima nota con i dati forniti, inclusi testata, conti collegati e attributi opzionali.
     *
     * @param dto DTO con i dati della prima nota da inserire
     * @param utenteCorrente codice univoco dell'operatore che esegue l'inserimento
     * @return PrimaNotaDetailDTO con i dati della prima nota inserita
     * @throws IllegalArgumentException se i dati non sono validi (es. scrittura non bilanciata, conti non validi)
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se entità correlate non esistono
     */
    it.csi.accrual.accrualsbsrv.dto.PrimaNotaDetailDTO inserisciPrimaNota(
        it.csi.accrual.accrualsbsrv.dto.PrimaNotaCreateUpdateDTO dto,
        String utenteCorrente
    );

    /**
     * Aggiorna una prima nota esistente identificata dall'anno e dal numero.
     * L'operazione modifica i dati della prima nota, inclusi testata, conti collegati e attributi.
     * I campi Tipo, Fonte, Evento, Causale, Numero provvisorio, Numero definitivo, Stato NON sono modificabili.
     * Sono modificabili solo: Descrizione e Data registrazione provvisoria.
     *
     * @param anno anno della prima nota da aggiornare
     * @param numero numero della prima nota da aggiornare
     * @param dto DTO con i dati aggiornati della prima nota
     * @param utenteCorrente codice univoco dell'operatore che esegue l'aggiornamento
     * @return PrimaNotaDetailDTO con i dati della prima nota aggiornata
     * @throws IllegalArgumentException se i dati non sono validi o si tenta di modificare campi non editabili
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se la prima nota non esiste
     */
    it.csi.accrual.accrualsbsrv.dto.PrimaNotaDetailDTO aggiornaPrimaNota(
        Integer anno,
        Integer numero,
        it.csi.accrual.accrualsbsrv.dto.PrimaNotaCreateUpdateDTO dto,
        String utenteCorrente
    );

    /**
     * Valida una prima nota singola identificata dall'anno e dal numero.
     * L'operazione imposta lo stato della prima nota da 'PROVVISORIA' a 'DEFINITIVA',
     * assegna il numero progressivo definitivo se non presente e imposta la data di registrazione definitiva.
     *
     * @param anno anno della prima nota
     * @param numero numero della prima nota
     * @param utenteCorrente codice univoco dell'operatore che esegue la validazione
     * @param mantieniNumeroDefinitivo true se si vuole mantenere il numero definitivo esistente (solo se parametro VERIFICA_RINUMERAZIONE_PRIME_NOTE = SI)
     * @param confermato true se l'utente ha confermato la validazione generica (WEB_CON_0016/WEB_CON_0017)
     * @return messaggio di successo
     * @throws IllegalArgumentException se la prima nota non può essere validata (es. stato non PROVVISORIA)
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se la prima nota non esiste
     * @throws it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException se è richiesta conferma per la rinumerazione
     */
    java.util.Map<String, String> validaPrimaNota(Integer anno, Integer numero, String utenteCorrente,
        Boolean mantieniNumeroDefinitivo, Boolean confermato)
        throws it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;

    /**
     * Valida massivamente un elenco di prime note.
     * L'operazione applica la stessa logica di validaPrimaNota per ciascuna prima nota nell'elenco.
     *
     * @param primaNoteIds elenco di identificativi delle prime note da validare
     * @param utenteCorrente codice univoco dell'operatore che esegue la validazione
     * @param mantieniNumeri true se si vogliono mantenere i numeri definitivi esistenti (solo se parametro VERIFICA_RINUMERAZIONE_PRIME_NOTE = SI)
     * @param confermato true se l'utente ha confermato la validazione massiva generica (WEB_CON_0017)
     * @return messaggio di successo
     * @throws IllegalArgumentException se una o più prime note non possono essere validate
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se una o più prime note non esistono
     */
    java.util.Map<String, String> validaPrimeNoteMassiva(List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaIdDTO> primaNoteIds, String utenteCorrente,
        Boolean mantieniNumeri, Boolean confermato)
        		throws it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;

    /**
     * Verifica se una prima nota ha già un numero progressivo definitivo assegnato.
     *
     * @param anno anno della prima nota
     * @param numero numero della prima nota
     * @return true se la prima nota ha un progressivoGiornale valorizzato, false altrimenti
     * @throws it.csi.accrual.accrualsbsrv.be.exception.NotFoundException se la prima nota non esiste
     */
    boolean verificaEsistenzaNumeroProgressivoDefinitivo(Integer anno, Integer numero);

    /**
     * Esporta le prime note in formato CSV.
     *
     * @param filter filtro di ricerca
     * @param sort ordinamento
     * @return lista di oggetti PrimaNota per l'export
     */
    List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchResultDTO> exportPrimaNotaList(PrimaNotaSearchFilterDTO filter, String sort);
}
