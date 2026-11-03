/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRRuoloAzione;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualRRuoloAzioneRepository extends JpaRepository<AccrualRRuoloAzione, Long> {

    @Query("SELECT r FROM AccrualRRuoloAzione r JOIN FETCH r.azione a " +
           "WHERE r.ruoloCodice = :ruoloCodice AND r.ente = :ente " +
           "AND a.gruppoAzione IS NOT NULL AND r.dataCancellazione IS NULL " +
           "AND a.dataCancellazione IS NULL " +
           "AND r.inizioValidita <= CURRENT_TIMESTAMP " +
           "AND (r.fineValidita IS NULL OR r.fineValidita >= CURRENT_TIMESTAMP) " +
           "ORDER BY a.ordine ASC")
    List<AccrualRRuoloAzione> findByRuoloCodiceAndEnteOrderByAzioneOrdineAsc(
        @Param("ruoloCodice") String ruoloCodice, @Param("ente") AccrualTEnte ente);

    @Query("SELECT r FROM AccrualRRuoloAzione r JOIN FETCH r.azione a " +
           "WHERE r.ruoloCodice = :ruoloCodice AND r.ente = :ente " +
           "AND r.dataCancellazione IS NULL AND a.dataCancellazione IS NULL " +
           "AND r.inizioValidita <= CURRENT_TIMESTAMP " +
           "AND (r.fineValidita IS NULL OR r.fineValidita >= CURRENT_TIMESTAMP)")
    List<AccrualRRuoloAzione> findAllAzioniByRuoloCodiceAndEnte(
        @Param("ruoloCodice") String ruoloCodice, @Param("ente") AccrualTEnte ente);
}
