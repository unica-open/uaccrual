/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausaleStato;

import java.util.Optional;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;

@Repository
public interface AccrualRCausaleStatoRepository extends JpaRepository<AccrualRCausaleStato, Long> {
    Optional<AccrualRCausaleStato> findByCausaleAndFineValiditaIsNull(AccrualTCausale causale);
}
