/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTParametro;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccrualTParametroRepository extends JpaRepository<AccrualTParametro, Long> {

    // Metodo per trovare un parametro per nome e ente
    Optional<AccrualTParametro> findByParametroNomeAndEnte(String parametroNome, AccrualTEnte ente);
    
    // Metodo per trovare un parametro per nome, ente e abilitato
    Optional<AccrualTParametro> findByParametroNomeAndEnteAndParametroAbilitatoTrue(String parametroNome, AccrualTEnte ente);
}
