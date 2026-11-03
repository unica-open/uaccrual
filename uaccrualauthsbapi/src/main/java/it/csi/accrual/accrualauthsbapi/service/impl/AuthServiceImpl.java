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
import it.csi.accrual.accrualauthsbapi.exception.IllegalArgumentException;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTPasswordRepository;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTResetPasswordRepository;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTUtenteRepository;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.util.AutenticazioneTipoEnum;
import it.csi.accrual.accrualauthsbapi.util.PasswordUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
	@Inject	private AccrualAuthTUtenteRepository accrualAuthTUtenteRepository;
	@Inject	private AccrualAuthTPasswordRepository accrualAuthTPasswordRepository;
	@Inject	private AccrualAuthTResetPasswordRepository accrualAuthTResetPasswordRepository;
	@Inject	PasswordUtil passwordUtil;

	@Override
	public AccrualAuthTUtente login(String codiceFiscale,String enteCodiceUnivoco, String password) {
		log.info("Tentativo di autenticazione per CF: {}, Ente: {}", codiceFiscale, enteCodiceUnivoco);
		AccrualAuthTUtente user = findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(codiceFiscale, enteCodiceUnivoco);

		if(user == null) {
			log.debug("Utente non trovato: {}", codiceFiscale);
			throw new NotFoundException("Utente non trovato");
		}
		
		if(!AutenticazioneTipoEnum.EXT_SPRING.getCodice().equals(user.getAutenticazioneTipo().getAutenticazioneTipoCodice())) {
			log.warn("Utente {} non ha accesso a questa modalita' di autenticazione: {}", 
					user.getUtenteCodiceUnivoco(), AutenticazioneTipoEnum.EXT_SPRING.getDescrizione());
				throw new BadRequestException("Utente non ha accesso a questa modalita' di autenticazione");
		}
		
		if (!user.getUtentePrimoAccesso()) {
			AccrualAuthTPassword pwd = accrualAuthTPasswordRepository.findByUtente(user);

			if (pwd == null) {
				log.info("Password non inserita per utente: {}", codiceFiscale);
				throw new IllegalArgumentException("Password non inserita");
			}

			if (!passwordUtil.passwordEncoder().matches(password, pwd.getPasswordValue())) {
				log.info("Password errata per utente: {}", codiceFiscale);
				throw new BadRequestException("Username o Password errati!");
			}
		} else {
			log.info("Utente al primo accesso: {}", codiceFiscale);
			throw new BadRequestException("Sei al primo accesso, devi effettuare la procedura di reset password!!");
		}

		log.info("Autenticazione riuscita per utente: {}", codiceFiscale);
		return user;
	}

	public AccrualAuthTResetPassword updateResetPassword(AccrualAuthTResetPassword accrualAuthTResetPassword) {
		accrualAuthTResetPassword.setGestDataDel(LocalDateTime.now());
		accrualAuthTResetPassword.setGestDataUpd(LocalDateTime.now());
		return accrualAuthTResetPasswordRepository.saveAndFlush(accrualAuthTResetPassword);
	}

	public AccrualAuthTUtente findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(String codiceFiscale, String enteCodiceUnivoco) {
		AccrualAuthTUtente accrualAuthTUtente = accrualAuthTUtenteRepository.findByUtenteCodiceUnivocoAndEnte_EnteCodiceUnivoco(codiceFiscale, enteCodiceUnivoco);
		if(accrualAuthTUtente == null) 
			throw new NotFoundException("Utente non trovato");
		return accrualAuthTUtente;
	}
	public AccrualAuthTUtente findByUtenteCodiceUnivoco(String codiceFiscale) {
		AccrualAuthTUtente accrualAuthTUtente = accrualAuthTUtenteRepository.findByUtenteCodiceUnivoco(codiceFiscale);
		if(accrualAuthTUtente == null) 
			throw new NotFoundException("Utente non trovato");
		return accrualAuthTUtente;
	}
	
}
