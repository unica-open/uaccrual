/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.repository;

import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsyncReportRepository
        extends JpaRepository<AsyncReport, UUID>,
                JpaSpecificationExecutor<AsyncReport> {

    Optional<AsyncReport> findById(UUID id);

    /**
     * Verifica appartenenza report a ente contando i record
     */
    long countByIdAndEnteId(UUID id, Long enteId);
}
