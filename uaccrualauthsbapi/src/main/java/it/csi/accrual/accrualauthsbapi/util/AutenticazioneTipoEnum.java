/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AutenticazioneTipoEnum {
	
	SIAC_SHIB("SIAC-SHIB", "Shibboleth"),
	EXT_SPRING("EXT-SPRING", "SpringSecurity"),
	EXT_TOKEN("EXT-TOKEN", "Token"),
	CSI_JOB("CSI-JOB", "Batch"),
	;
	
	private String codice;
	private String descrizione;
	
	public static AutenticazioneTipoEnum fromDescrizione(String descrizione) {
		for(AutenticazioneTipoEnum tsi : values()) {
			if(tsi.getDescrizione().equals(descrizione)) {
				return tsi;
			}
		}
		return null;
	}
}
