/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPdcLegame;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualRPdcLegameRepository extends JpaRepository<AccrualRPdcLegame, Long>, JpaSpecificationExecutor<AccrualRPdcLegame> {

    @Query("SELECT arpl FROM AccrualRPdcLegame arpl WHERE arpl.pdcLegameA.id = :pdcLegameAId AND arpl.pdcTipoRelazione.codice = :pdcTipoRelazioneCodice AND arpl.ente.id = :enteId AND arpl.fineValidita IS NULL ORDER BY arpl.inizioValidita DESC")
    List<AccrualRPdcLegame> findByPdcLegameAAndPdcTipoRelazioneCodiceAndEnte(@Param("pdcLegameAId") Long pdcLegameAId, @Param("pdcTipoRelazioneCodice") String pdcTipoRelazioneCodice, @Param("enteId") Long enteId);

    @Query("SELECT arpl FROM AccrualRPdcLegame arpl WHERE arpl.pdcLegameA.id IN :pdcIds AND arpl.pdcTipoRelazione.codice = :tipoRelazione AND arpl.ente.id = :enteId AND arpl.fineValidita IS NULL")
    List<AccrualRPdcLegame> findByPdcIdInAndTipoRelazioneCodiceAndEnteId(@Param("pdcIds") List<Long> pdcIds, @Param("tipoRelazione") String tipoRelazione, @Param("enteId") Long enteId);

    @Query("SELECT arpl FROM AccrualRPdcLegame arpl JOIN arpl.pdcLegameA pdcA WHERE pdcA.codice = :pdc118Codice AND arpl.ente.id = :enteId AND (:anno IS NULL OR (EXTRACT(YEAR FROM arpl.inizioValidita) <= :anno AND (arpl.fineValidita IS NULL OR EXTRACT(YEAR FROM arpl.fineValidita) >= :anno)))")
    Optional<AccrualRPdcLegame> findByPdc118CodiceAndAnnoAndEnteId(@Param("pdc118Codice") String pdc118Codice, @Param("anno") Integer anno, @Param("enteId") Long enteId);
}
