/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;

@Repository
public interface AccrualAuthTUtenteRepository extends JpaRepository<AccrualAuthTUtente, Long> {

	AccrualAuthTUtente findByUtenteCodiceUnivoco(String utenteCodiceUnivoco);

	AccrualAuthTUtente findByUtenteCodiceUnivocoAndEnte_EnteCodiceUnivoco(String utenteCodiceUnivoco, String enteCodiceUnivoco);
}
