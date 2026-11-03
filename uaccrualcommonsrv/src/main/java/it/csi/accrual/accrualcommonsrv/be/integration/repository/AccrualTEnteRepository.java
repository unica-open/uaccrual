/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;

@Repository
public interface AccrualTEnteRepository extends JpaRepository<AccrualTEnte, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM AccrualTEnte e WHERE e.codiceUnivoco = :codiceUnivoco AND e.dataCancellazione IS NULL")
    Optional<AccrualTEnte> findByCodiceUnivoco(@org.springframework.data.repository.query.Param("codiceUnivoco") String codiceUnivoco);
}