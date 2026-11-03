/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDAttributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualDAttributoRepository extends JpaRepository<AccrualDAttributo, Long> {
    Optional<AccrualDAttributo> findByCodice(String codice);
    
    /**
     * Trova un attributo per codice e ente, escludendo quelli cancellati.
     * Utilizzato per garantire l'unicità del risultato per ente.
     */
    Optional<AccrualDAttributo> findByCodiceAndEnteIdAndDataCancellazioneIsNull(String codice, Long enteId);
    
    /**
     * Trova tutti gli attributi non cancellati per un ente
     */
    @Query("SELECT a FROM AccrualDAttributo a WHERE a.ente.codiceUnivoco = :enteCodiceUnivoco AND a.dataCancellazione IS NULL ORDER BY a.descrizione")
    List<AccrualDAttributo> findByEnteCodiceUnivocoAndDataCancellazioneIsNull(@Param("enteCodiceUnivoco") String enteCodiceUnivoco);
}
