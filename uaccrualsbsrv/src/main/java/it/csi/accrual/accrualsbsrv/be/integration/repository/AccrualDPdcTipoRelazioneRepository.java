/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipoRelazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccrualDPdcTipoRelazioneRepository extends JpaRepository<AccrualDPdcTipoRelazione, Long>, JpaSpecificationExecutor<AccrualDPdcTipoRelazione> {
}
