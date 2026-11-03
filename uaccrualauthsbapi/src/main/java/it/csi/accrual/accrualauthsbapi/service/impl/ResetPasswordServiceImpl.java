/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTResetPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTPasswordRepository;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTResetPasswordRepository;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTUtenteRepository;
import it.csi.accrual.accrualauthsbapi.service.ResetPasswordService;
import it.csi.accrual.accrualauthsbapi.util.PasswordUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResetPasswordServiceImpl implements ResetPasswordService {

	@Inject private AccrualAuthTResetPasswordRepository accrualAuthTResetPasswordRepository;
	@Inject private AccrualAuthTPasswordRepository accrualAuthTPasswordRepository;
	@Inject private AccrualAuthTUtenteRepository accrualAuthTUtenteRepository;
	@Inject	PasswordUtil passwordUtil;

	public AccrualAuthTResetPassword updateResetPassword(AccrualAuthTResetPassword accrualAuthTResetPassword) {
		log.debug("Invalidazione token di reset password");
		accrualAuthTResetPassword.setGestDataDel(LocalDateTime.now());
		accrualAuthTResetPassword.setGestDataUpd(LocalDateTime.now());
		return accrualAuthTResetPasswordRepository.saveAndFlush(accrualAuthTResetPassword);
	}

	public AccrualAuthTResetPassword findByResetPasswordTokenValue(String resetPasswordTokenValue) {
		log.debug("Ricerca token di reset password: {}", resetPasswordTokenValue);
		AccrualAuthTResetPassword accrualAuthTResetPassword = accrualAuthTResetPasswordRepository.findByResetPasswordTokenValue(resetPasswordTokenValue);
		if (accrualAuthTResetPassword == null) {
			log.warn("Token di reset password non trovato: {}", resetPasswordTokenValue);
			throw new NotFoundException("Token non valido o scaduto");
		}
		log.debug("Token di reset password trovato per utente: {}", accrualAuthTResetPassword.getUtente().getUtenteCodiceUnivoco());
		return accrualAuthTResetPassword;
	}

	public AccrualAuthTUtente confirmResetPassword(AccrualAuthTUtente utente, String password) {
		log.info("Esecuzione reset password per utente: {}", utente.getUtenteCodiceUnivoco());
		AccrualAuthTUtente accrualAuthTUtente = accrualAuthTUtenteRepository.findByUtenteCodiceUnivoco(utente.getUtenteCodiceUnivoco());

		accrualAuthTUtente.setUtentePrimoAccesso(false);
		accrualAuthTUtente.setGestDataUpd(LocalDateTime.now());

		AccrualAuthTPassword accrualAuthTPassword = accrualAuthTPasswordRepository.findByUtente(accrualAuthTUtente);

		if (accrualAuthTPassword != null) {
			log.debug("Aggiornamento password esistente");
			accrualAuthTPassword.setPasswordValue(passwordUtil.passwordEncoder().encode(password));
			accrualAuthTPassword.setGestDataUpd(LocalDateTime.now());
			accrualAuthTPasswordRepository.saveAndFlush(accrualAuthTPassword);
		} else {
			log.debug("Creazione nuova password");
			AccrualAuthTPassword accrualAuthTPassword2 = AccrualAuthTPassword.builder()
					.passwordValue(passwordUtil.passwordEncoder().encode(password)).utente(accrualAuthTUtente).build();
			accrualAuthTPasswordRepository.saveAndFlush(accrualAuthTPassword2);
		}

		log.info("Password aggiornata con successo per utente: {}", utente.getUtenteCodiceUnivoco());
		return accrualAuthTUtenteRepository.saveAndFlush(accrualAuthTUtente);
	}
}
