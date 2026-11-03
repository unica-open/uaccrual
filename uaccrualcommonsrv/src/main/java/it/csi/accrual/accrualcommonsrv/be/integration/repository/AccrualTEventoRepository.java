/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualTEventoRepository extends JpaRepository<AccrualTEvento, Long> {

    /**
     * Trova eventi filtrati per codice fiscale dell'ente
     */
    List<AccrualTEvento> findByEnte_CodiceUnivoco(String codiceFiscale);
    List<AccrualTEvento> findByEnte_Id(Long enteId);
    Optional<AccrualTEvento> findByCodiceAndEnte_CodiceUnivoco(String codice, String enteCodiceUnivoco);
    
    /**
     * Trova tutti gli eventi ordinati per codice
     */
    @Query("SELECT e FROM AccrualTEvento e ORDER BY e.codice ASC")
    List<AccrualTEvento> findAllByOrderByCodiceAsc();
    
    /**
     * Trova eventi filtrati per ente ordinati per codice
     */
    @Query("SELECT e FROM AccrualTEvento e WHERE e.ente.codiceUnivoco = :codiceFiscale ORDER BY e.codice ASC")
    List<AccrualTEvento> findAllByEnte_CodiceUnivocoOrderByCodiceAsc(String codiceFiscale);
}