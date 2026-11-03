/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcSaldo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualTPdcSaldoRepository extends JpaRepository<AccrualTPdcSaldo, Long> {
    boolean existsByPdc_IdAndDataCancellazioneIsNull(Long idPdc);
    
    @Query("SELECT s FROM AccrualTPdcSaldo s WHERE s.ente = :ente AND s.anno = :anno AND s.dataCancellazione IS NULL")
    List<AccrualTPdcSaldo> findByEnteAndAnno(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno);

    @Query("SELECT s.pdc.collegamentoBilItas, SUM(s.importo) " +
           "FROM AccrualTPdcSaldo s " +
           "WHERE s.ente = :ente " +
           "AND s.anno = :anno " +
           "AND s.pdc.collegamentoBilItas IS NOT NULL " +
           "AND s.dataCancellazione IS NULL " +
           "GROUP BY s.pdc.collegamentoBilItas")
    List<Object[]> findSaldiAggregatiByItas(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno);
}
