/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import java.util.List;

import it.csi.accrual.accrualsbsrv.dto.Pdc;
import it.csi.accrual.accrualsbsrv.dto.PagedPdc;
import it.csi.accrual.accrualsbsrv.dto.PdcClasse;
import it.csi.accrual.accrualsbsrv.dto.PdcTipo;
import it.csi.accrual.accrualsbsrv.dto.PdcStato;
import it.csi.accrual.accrualsbsrv.dto.PdcFamiglia;

import it.csi.accrual.accrualsbsrv.dto.PdcContoRelazione;
import it.csi.accrual.accrualsbsrv.dto.PdcFilter;

public interface PdcService {

    /**
     * Restituisce un elenco paginato e ordinato dei piani dei conti.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param sort ordinamento (opzionale)
     * @param limit limite di risultati per pagina (opzionale, default 20)
     * @param offset offset per la paginazione (opzionale, default 0)
     * @return pagina di PDC per l'ente
     */
    PagedPdc getPdcList(String sort, Integer limit, Integer offset);

    /**
     * Ricerca i PDC in base a criteri specifici.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param filter DTO contenente i criteri di filtro
     * @param sort ordinamento (opzionale)
     * @param limit limite di risultati per pagina (opzionale, default 20)
     * @param offset offset per la paginazione (opzionale, default 0)
     * @return pagina di PDC filtrata per l'ente
     */
    PagedPdc searchPdc(PdcFilter filter, String sort, Integer limit, Integer offset);

    /**
     * Trova un PDC specifico per codice ufficiale, classe, famiglia.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param codiceUfficiale codice ufficiale del PDC
     * @param codiceClasse codice della classe del PDC
     * @param codiceFamiglia codice della famiglia del PDC
     * @return PDC trovato
     */
    Pdc getPdcByCodiceUfficialeClasseFamiglia(String codiceUfficiale, String codiceClasse, String codiceFamiglia);

    /**
     * Crea un nuovo PDC.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param pdc il PDC da creare
     * @return il PDC creato
     */
    Pdc createPdc(Pdc pdc);

    /**
     * Aggiorna un PDC esistente
     *
     * @param codicePdc codice del PDC da aggiornare
     * @param pdc il PDC con i dati aggiornati
     * @return il PDC aggiornato
     */
    Pdc updatePdc(String codicePdc, Pdc pdc);

    /**
     * Aggiorna un PDC esistente utilizzando la chiave tripla.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param codiceUfficiale codice ufficiale del PDC da aggiornare
     * @param codiceClasse codice classe del PDC da aggiornare
     * @param codiceFamiglia codice famiglia del PDC da aggiornare
     * @param pdc il PDC con i dati aggiornati
     * @return il PDC aggiornato
     */
    Pdc updatePdcByTripleKey(String codiceUfficiale, String codiceClasse, String codiceFamiglia, Pdc pdc);

    /**
     * Elimina un PDC
     *
     * @param codicePdc codice del PDC da eliminare
     */
    void deletePdc(String codicePdc);

    /**
     * Elimina un PDC utilizzando la chiave tripla.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param codiceUfficiale codice ufficiale del PDC da eliminare
     * @param codiceClasse codice classe del PDC da eliminare
     * @param codiceFamiglia codice famiglia del PDC da eliminare
     */
    void deletePdcByTripleKey(String codiceUfficiale, String codiceClasse, String codiceFamiglia);

    /**
     * Ottiene tutte le classi del piano dei conti
     *
     * @return lista di classi PDC
     * @deprecated Usare {@link #getPdcClassiValid(String)} per filtrare per validità
     */
    @Deprecated
    List<PdcClasse> getPdcClassi();

    /**
     * Ottiene le classi del piano dei conti che hanno conti validi nell'anno di bilancio corrente.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @return lista di classi PDC filtrate per validità
     */
    List<PdcClasse> getPdcClassiValid();

    /**
     * Ottiene tutti i tipi del piano dei conti
     *
     * @return lista di tipi PDC
     */
    List<PdcTipo> getPdcTipi();

    /**
     * Ottiene tutti gli stati del piano dei conti
     *
     * @return lista di stati PDC
     */
    List<PdcStato> getPdcStati();

    /**
     * Ottiene tutte le famiglie del piano dei conti
     *
     * @return lista di famiglie PDC
     */
    List<PdcFamiglia> getPdcFamiglie();


    /**
     * Ottiene tutte le relazioni associate a un PdC specifico.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param codiceUfficiale codice ufficiale del PDC principale
     * @param codiceClasse codice classe del PDC principale
     * @param codiceFamiglia codice famiglia del PDC principale
     * @param tipoRelazione tipo di relazione da filtrare (FONDO, CORRELAZIONE) - opzionale
     * @return lista di PDC collegati attraverso relazioni
     */
    List<PdcContoRelazione> getPdcRelazioni(String codiceUfficiale, String codiceClasse, String codiceFamiglia, String tipoRelazione);

    /**
     * Ricerca i PDC in formato gerarchico (albero) in base a criteri specifici.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param filter DTO contenente i criteri di filtro
     * @return risultati della ricerca in formato gerarchico
     */
    it.csi.accrual.accrualsbsrv.dto.PdcTreeResult searchPdcTree(it.csi.accrual.accrualsbsrv.dto.PdcTreeFilter filter);

    /**
     * Esporta i PDC in formato CSV.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     * Il codice ente viene letto automaticamente dal SecurityContext.
     *
     * @param filter filtri di ricerca
     * @param sort ordinamento
     * @return lista di PDC
     */
    List<Pdc> exportPdcList(PdcFilter filter, String sort);

    /**
     * ALG-003: Verifica Conto da inserire nella scrittura.
     * Validazione esistenza, validità temporale rispetto alla data di sistema,
     * e attributo foglia (o foglia_non_foglia) del conto.
     *
     * @param codiceUfficiale codice ufficiale del conto da validare
     * @throws IllegalArgumentException se il conto non è valido (WEB_ERR_021, WEB_ERR_022, WEB_ERR_028)
     */
    void validaContoDaInserire(String codiceUfficiale);

}