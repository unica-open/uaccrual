/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualDCausaleTipoRepository extends JpaRepository<AccrualDCausaleTipo, Long> {
    List<AccrualDCausaleTipo> findByEnte_Id(Long enteId);
}
