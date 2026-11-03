/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.security.jwt;

import it.csi.accrual.accrualauthsbapi.exception.MalformedIdTokenException;
import lombok.Getter;
import lombok.Setter;

/**
 * Lettore dell'identit&agrave; digitale
 * @version 1.0.0 - 17/04/2020
 */
@Getter
@Setter
public class Identita {

	private String nome;
	private String cognome;
	private String codFiscale;
	private String idProvider;
	private String timestamp;
	private int livelloAutenticazione;
	private String mac;

	/**
	 * Costruttore con token
	 * @param token il token da parsificare
	 * @throws MalformedIdTokenException nel caso in cui il token non sia formalmente valido
	 */
	public Identita(String token) {
		int slash1Index = token.indexOf(47);
		if (slash1Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.codFiscale = token.substring(0, slash1Index);
		int slash2Index = token.indexOf(47, slash1Index + 1);
		if (slash2Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.nome = token.substring(slash1Index + 1, slash2Index);
		int slash3Index = token.indexOf(47, slash2Index + 1);
		if (slash3Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.cognome = token.substring(slash2Index + 1, slash3Index);
		int slash4Index = token.indexOf(47, slash3Index + 1);
		if (slash4Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.idProvider = token.substring(slash3Index + 1, slash4Index);
		int slash5Index = token.indexOf(47, slash4Index + 1);
		if (slash5Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.timestamp = token.substring(slash4Index + 1, slash5Index);
		int slash6Index = token.indexOf(47, slash5Index + 1);
		if (slash6Index == -1) {
			throw new MalformedIdTokenException(token);
		}
		this.livelloAutenticazione = Integer
				.parseInt(token.substring(slash5Index + 1, slash6Index));
		this.mac = token.substring(slash6Index + 1);
	}


}
