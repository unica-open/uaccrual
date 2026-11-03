/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualREtichettaPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualREtichettaPrimaNotaRepository extends JpaRepository<AccrualREtichettaPrimaNota, Long> {
    List<AccrualREtichettaPrimaNota> findByPrimaNotaIn(List<AccrualTPrimaNota> pns);
}
