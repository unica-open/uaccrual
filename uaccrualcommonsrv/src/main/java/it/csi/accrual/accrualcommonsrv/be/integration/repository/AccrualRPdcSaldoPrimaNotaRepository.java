/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPdcSaldoPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcSaldo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccrualRPdcSaldoPrimaNotaRepository extends JpaRepository<AccrualRPdcSaldoPrimaNota, Long> {
    @Query("SELECT r FROM AccrualRPdcSaldoPrimaNota r WHERE r.primaNota IN :primeNote AND r.pdcSaldo IN :saldi")
    List<AccrualRPdcSaldoPrimaNota> findByPrimaNotaInAndPdcSaldoIn(@Param("primeNote") List<AccrualTPrimaNota> primeNote, @Param("saldi") List<AccrualTPdcSaldo> saldi);

    @Query("SELECT r FROM AccrualRPdcSaldoPrimaNota r WHERE r.primaNota.id IN :primaNotaIds AND r.pdcSaldo IN :saldi")
    List<AccrualRPdcSaldoPrimaNota> findByPrimaNotaIdInAndPdcSaldoIn(@Param("primaNotaIds") List<Long> primaNotaIds, @Param("saldi") List<AccrualTPdcSaldo> saldi);
}