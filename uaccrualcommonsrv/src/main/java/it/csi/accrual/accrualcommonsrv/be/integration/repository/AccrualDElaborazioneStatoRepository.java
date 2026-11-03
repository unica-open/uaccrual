/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDElaborazioneStato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccrualDElaborazioneStatoRepository extends JpaRepository<AccrualDElaborazioneStato, Long> {
    Optional<AccrualDElaborazioneStato> findByCodice(String codice);
}
