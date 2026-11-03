/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccrualTPdcFamigliaRepository extends JpaRepository<AccrualTPdcFamiglia, Long> {

    @Query("SELECT f FROM AccrualTPdcFamiglia f WHERE f.codice = :codice")
    Optional<AccrualTPdcFamiglia> findByCodice(@Param("codice") String codice);
}
