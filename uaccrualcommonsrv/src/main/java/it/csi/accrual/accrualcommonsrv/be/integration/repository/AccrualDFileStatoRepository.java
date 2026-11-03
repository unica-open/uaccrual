/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileStato;

@Repository
public interface AccrualDFileStatoRepository extends JpaRepository<AccrualDFileStato, Long>, JpaSpecificationExecutor<AccrualDFileStato> {
    Optional<AccrualDFileStato> findByCodice(String codice);
}
