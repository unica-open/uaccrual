/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualDPdcClasseRepository extends JpaRepository<AccrualDPdcClasse, Long> {

    @Query("SELECT c FROM AccrualDPdcClasse c WHERE c.codice = :codice")
    Optional<AccrualDPdcClasse> findByCodice(@Param("codice") String codice);

    /**
     * Trova le classi che hanno almeno un conto valido nel periodo specificato per un dato ente.
     * @param inizioAnno inizio dell'anno (01/01 anno 00:00:00)
     * @param fineAnno fine dell'anno (31/12 anno 23:59:59)
     * @param enteCodiceUnivoco codice fiscale dell'ente
     * @return lista di classi con conti validi
     */
    @Query("SELECT DISTINCT c FROM AccrualDPdcClasse c " +
           "INNER JOIN AccrualTPdc p ON c.codice = p.pdcClasse.codice " +
           "WHERE p.dataCancellazione IS NULL " +
           "AND p.inizioValidita <= :fineAnno " +
           "AND (p.fineValidita IS NULL OR p.fineValidita >= :inizioAnno) " +
           "AND p.ente.codiceUnivoco = :enteCodiceUnivoco " +
           "ORDER BY c.codice")
    List<AccrualDPdcClasse> findClassiConContiValidi(
        @Param("inizioAnno") LocalDateTime inizioAnno,
        @Param("fineAnno") LocalDateTime fineAnno,
        @Param("enteCodiceUnivoco") String enteCodiceUnivoco);
}