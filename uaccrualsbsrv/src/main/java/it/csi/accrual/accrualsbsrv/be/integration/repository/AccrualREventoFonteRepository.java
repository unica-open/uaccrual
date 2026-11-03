/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualREventoFonte;

@Repository
public interface AccrualREventoFonteRepository extends JpaRepository<AccrualREventoFonte, Long> {

    /**
     * Trova le associazioni evento-fonte filtrate per codice fonte e codice univoco ente
     */
    @Query("SELECT erf FROM AccrualREventoFonte erf JOIN erf.fonte f JOIN erf.evento e WHERE f.codice = :codiceFonte AND e.ente.codiceUnivoco = :enteCodiceUnivoco")
    List<AccrualREventoFonte> findByFonteCodiceAndEnteCodiceUnivoco(@Param("codiceFonte") String codiceFonte, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    /**
     * Trova le associazioni evento-fonte filtrate per ID fonte e ID ente
     */
    @Query("SELECT erf FROM AccrualREventoFonte erf JOIN erf.fonte f JOIN erf.evento e WHERE f.id = :fonteId AND e.ente.id = :enteId")
    List<AccrualREventoFonte> findByFonteIdAndEnteId(@Param("fonteId") Long fonteId, @Param("enteId") Long enteId);
}