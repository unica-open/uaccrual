/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;

@Repository
public interface AccrualTFileRepository extends JpaRepository<AccrualTFile, Long>, JpaSpecificationExecutor<AccrualTFile> {
    
    Page<AccrualTFile> findAllByEnteId(Long enteId, Pageable pageable);

    // Metodo per la ricerca avanzata, da implementare in un secondo momento con JpaSpecificationExecutor
    // Page<AccrualTFile> findByEnteAndFilters(
    //     Long enteId,
    //     String codiceFile,
    //     String fonte,
    //     String tipoFile,
    //     java.util.Date dataCaricamento,
    //     Pageable pageable
    // );
}

