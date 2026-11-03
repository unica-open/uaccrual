/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTGruppoAzione;

@Repository
public interface AccrualTGruppoAzioneRepository extends JpaRepository<AccrualTGruppoAzione, Long>, JpaSpecificationExecutor<AccrualTGruppoAzione> {
    // Metodi di query specifici se necessari
}
