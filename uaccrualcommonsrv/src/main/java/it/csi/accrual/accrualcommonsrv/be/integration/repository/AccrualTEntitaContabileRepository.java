/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEntitaContabile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;

@Repository
public interface AccrualTEntitaContabileRepository extends JpaRepository<AccrualTEntitaContabile, Long> {

    @Query("SELECT ec FROM AccrualTEntitaContabile ec JOIN ec.ente e WHERE e.codiceUnivoco = :enteCodiceUnivoco AND ec.anno = :anno AND ec.inizioValidita <= :dataCorrente AND (ec.fineValidita IS NULL OR ec.fineValidita >= :dataCorrente)")
    Optional<AccrualTEntitaContabile> findByEnteCodiceUnivocoAndAnnoAndDataValidita(@Param("enteCodiceUnivoco") String enteCodiceUnivoco, @Param("anno") String anno, @Param("dataCorrente") LocalDateTime dataCorrente);

    @Query("SELECT ec FROM AccrualTEntitaContabile ec WHERE ec.ente = :ente AND ec.anno = :anno AND ec.fineValidita IS NULL ORDER BY ec.inizioValidita DESC")
    java.util.List<AccrualTEntitaContabile> findByEnteAndAnno(@Param("ente") AccrualTEnte ente, @Param("anno") String anno);

    @Query("SELECT ec FROM AccrualTEntitaContabile ec JOIN ec.ente e WHERE e.codiceUnivoco = :enteCodice AND ec.fineValidita IS NULL ORDER BY ec.anno ASC")
    java.util.List<AccrualTEntitaContabile> findAnniByEnteCodice(@Param("enteCodice") String enteCodice);

}