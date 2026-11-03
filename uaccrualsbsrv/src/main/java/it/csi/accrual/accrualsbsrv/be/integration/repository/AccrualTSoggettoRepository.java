/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualTSoggetto;

@Repository
public interface AccrualTSoggettoRepository extends JpaRepository<AccrualTSoggetto, Long>, JpaSpecificationExecutor<AccrualTSoggetto> {
    Optional<AccrualTSoggetto> findByCodice(String codice);

    Optional<AccrualTSoggetto> findByCodiceFiscale(String codiceFiscale);

    Optional<AccrualTSoggetto> findByCodiceFiscaleAndDataCancellazioneIsNull(String codiceFiscale);

    Optional<AccrualTSoggetto> findByCodiceAndEnte_Id(String codice, Long enteId);

    Optional<AccrualTSoggetto> findByCodiceFiscaleAndEnte_Id(String codiceFiscale, Long enteId);

    // Versioni con controllo di cancellazione e ente
    Optional<AccrualTSoggetto> findByCodiceFiscaleAndEnte_IdAndDataCancellazioneIsNull(String codiceFiscale, Long enteId);

    // Metodo per verificare se esistono soggetti associati a un conto PDC
    @Query("SELECT COUNT(rps) > 0 FROM AccrualRPdcSoggetto rps WHERE rps.pdc.id = :idPdc AND rps.dataCancellazione IS NULL")
    boolean existsByPdc_Id(@Param("idPdc") Long idPdc);

    /**
     * Verifica l'esistenza di un soggetto dato un codice e un id ente.
     * @param codice il codice del soggetto
     * @param enteId l'id dell'ente
     * @return true se esiste un soggetto con il codice e l'ente specificati, false altrimenti
     */
    boolean existsByCodiceAndEnte_Id(String codice, Long enteId);

    /**
     * Verifica l'esistenza di un soggetto non cancellato dato un codice e un id ente.
     * @param codice il codice del soggetto
     * @param enteId l'id dell'ente
     * @return true se esiste un soggetto non cancellato con il codice e l'ente specificati, false altrimenti
     */
    boolean existsByCodiceAndEnte_IdAndDataCancellazioneIsNull(String codice, Long enteId);
}