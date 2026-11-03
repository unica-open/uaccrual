/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccrualTCausaleRepository extends JpaRepository<AccrualTCausale, Long>, JpaSpecificationExecutor<AccrualTCausale> {
    Optional<AccrualTCausale> findByCodice(String codice);
    AccrualTCausale findByCodiceAndEnte_Id(String codice, Long enteId);
    @Query("SELECT c FROM AccrualTCausale c WHERE UPPER(TRIM(c.codice)) = UPPER(TRIM(:codice)) AND c.ente.id = :enteId AND (c.fineValidita IS NULL OR c.fineValidita >= CURRENT_TIMESTAMP) ORDER BY c.inizioValidita DESC")
    List<AccrualTCausale> findByCodiceAndEnte_IdAndFineValiditaIsNull(@Param("codice") String codice, @Param("enteId") Long enteId);

    @Query("SELECT c FROM AccrualTCausale c WHERE UPPER(TRIM(c.descrizione)) = UPPER(TRIM(:descrizione)) AND c.ente.id = :enteId AND (c.fineValidita IS NULL OR c.fineValidita >= CURRENT_TIMESTAMP) ORDER BY c.inizioValidita DESC")
    List<AccrualTCausale> findByDescrizioneAndEnte_IdAndFineValiditaIsNull(@Param("descrizione") String descrizione, @Param("enteId") Long enteId);

    // Metodo per verificare se esistono causali associate a un conto PDC
    @Query("SELECT COUNT(rcp) > 0 FROM AccrualRCausalePdc rcp WHERE rcp.pdc.id = :idPdc AND rcp.dataCancellazione IS NULL")
    boolean existsByPdc_Id(@Param("idPdc") Long idPdc);
	List<AccrualTCausale> findByEnte_CodiceUnivoco(String codiceFiscale);

    @Query("SELECT c FROM AccrualTCausale c WHERE c.evento.codice = :eventoCodice AND c.causaleTipo.codice = :classeCodice AND c.ente = :ente AND c.fineValidita IS NULL")
    Optional<AccrualTCausale> findByEventoAndPdcClasse(@Param("eventoCodice") String eventoCodice, @Param("classeCodice") String classeCodice, @Param("ente") AccrualTEnte ente);

    @Query("SELECT c FROM AccrualTCausale c WHERE c.evento.codice = :eventoCodice AND c.ente = :ente")
    List<AccrualTCausale> findByEvento_CodiceAndEnte(@Param("eventoCodice") String eventoCodice, @Param("ente") AccrualTEnte ente);

    /**
     * Trova tutte le causali per un evento specifico
     */
    List<AccrualTCausale> findByEvento_Id(Long eventoId);

    /**
     * Trova tutte le causali per un evento specifico e un ente specifico
     */
    List<AccrualTCausale> findByEvento_IdAndEnte_CodiceUnivoco(Long eventoId, String codiceFiscale);

    /**
     * Trova gli ID delle causali per codice e ente
     */
    @Query("SELECT c.id FROM AccrualTCausale c WHERE c.codice IN :codici AND c.ente.id = :enteId")
    List<Long> findIdsByCodiciAndEnteId(@Param("codici") List<String> codici, @Param("enteId") Long enteId);
}
