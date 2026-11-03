/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleStato;

@Repository
public interface AccrualDCausaleStatoRepository extends JpaRepository<AccrualDCausaleStato, Long> {
    Optional<AccrualDCausaleStato> findByCodice(String codice);
}
