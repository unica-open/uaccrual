/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFonte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualDFonteRepository extends JpaRepository<AccrualDFonte, Long> {
    List<AccrualDFonte> findByEnte_CodiceUnivoco(String codiceFiscale);
    Optional<AccrualDFonte> findByCodice(String codice);
    Optional<AccrualDFonte> findByCodiceAndEnte_CodiceUnivoco(String codice, String enteCodiceUnivoco);
}
