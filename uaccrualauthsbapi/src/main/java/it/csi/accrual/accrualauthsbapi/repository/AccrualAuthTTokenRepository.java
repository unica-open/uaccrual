/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTToken;

@Repository
public interface AccrualAuthTTokenRepository extends JpaRepository<AccrualAuthTToken, Long> {
	Optional<AccrualAuthTToken> findByTokenNonce(String tokenNonce);
	List<AccrualAuthTToken> findByUtenteUtenteIdAndFineValiditaIsNull(Integer utenteId);
}
