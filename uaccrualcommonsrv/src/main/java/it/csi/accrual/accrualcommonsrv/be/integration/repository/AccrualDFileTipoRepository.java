/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileTipo;
import java.util.List;

@Repository
public interface AccrualDFileTipoRepository extends JpaRepository<AccrualDFileTipo, Long>, JpaSpecificationExecutor<AccrualDFileTipo> {
    List<AccrualDFileTipo> findAllByOrderByCodiceAsc();
    AccrualDFileTipo findByCodice(String codice);
    AccrualDFileTipo findByCodiceAndFonteEnteId(String codice, Long enteId);
    List<AccrualDFileTipo> findAllByFonteEnteIdOrderByCodiceAsc(Long enteId);
}
