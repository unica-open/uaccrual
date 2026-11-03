/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.csi.accrual.accrualauthsbapi.config.EmailProperties;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTResetPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.repository.AccrualAuthTResetPasswordRepository;
import it.csi.accrual.accrualauthsbapi.service.EmailService;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class EmailServiceImpl implements EmailService {

	@Inject private JavaMailSender javaMailSender;
	@Inject	private AccrualAuthTResetPasswordRepository accrualAuthTResetPasswordRepository;
	@Inject private EmailProperties emailProperties;

	@Async
	public void sendResetPasswordEmail(AccrualAuthTUtente user) {
		String tokenTemp = UUID.randomUUID().toString();

		AccrualAuthTResetPassword accrualAuthTToken = AccrualAuthTResetPassword.builder()
				.resetPasswordTokenValue(tokenTemp).utente(user)
				.resetPasswordExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(emailProperties.getTokenResetPwdExpiration()))).build();

		accrualAuthTResetPasswordRepository.saveAndFlush(accrualAuthTToken);

		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
			String fromAddress = ((org.springframework.mail.javamail.JavaMailSenderImpl) javaMailSender).getUsername();
			messageHelper.setFrom(fromAddress);
			messageHelper.setTo(user.getUtenteEmail());
			messageHelper.setSubject("Reset della Password");
			messageHelper.setText(buildResetPasswordHtmlBody(tokenTemp), true);
			javaMailSender.send(mimeMessage);
			log.info("Email inviata a " + user.getUtenteEmail());
		} catch (MessagingException e) {
			log.error(e.getMessage());
			throw new MailSendException("Errore invio email", e);
		}
	}

	private String buildResetPasswordHtmlBody(String token) {
		return String.format("""
				<html>
				<body style="font-family: Arial, sans-serif;">
				    <h2>Ciao,</h2>

				    <p>Abbiamo ricevuto una richiesta di reset password per il tuo account.</p>

				    <p>Per resettare la tua password, clicca sul pulsante seguente:</p>

				    <a href="%s"
				       style="display:inline-block;
				              padding:10px 15px;
				              background-color:#2e7d32;
				              color:#ffffff;
				              text-decoration:none;
				              border-radius:5px;">
				        Resetta la tua Password
				    </a>

				    <p style="margin-top:20px;">
				        Il link scadra' tra <b>5 minuti</b>.
				    </p>

				    <p>
				        Se non hai richiesto tu il reset, puoi ignorare questa email.
				    </p>
				</body>
				</html>
				""", emailProperties.getResetLink() + token);
	}

}