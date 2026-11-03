/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausalePdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;

import java.util.List;

@Repository
public interface AccrualRCausalePdcRepository extends JpaRepository<AccrualRCausalePdc, Long> {

    @Query("SELECT rcp FROM AccrualRCausalePdc rcp " +
           "LEFT JOIN FETCH rcp.pdc pdc " +
           "LEFT JOIN FETCH pdc.pdcClasse " +
           "WHERE rcp.causale.id = :causaleId AND rcp.ente.id = :enteId")
    List<AccrualRCausalePdc> findByCausaleAndEnteWithRelations(@Param("causaleId") Long causaleId, @Param("enteId") Long enteId);

    void deleteByCausale(AccrualTCausale causale);
}