/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNotaNum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccrualTPrimaNotaNumRepository extends JpaRepository<AccrualTPrimaNotaNum, Long> {
    Optional<AccrualTPrimaNotaNum> findByEnteAndAnnoAndIsDefinitiva(AccrualTEnte ente, String anno, boolean isDefinitiva);
}
