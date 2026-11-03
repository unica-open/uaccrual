/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.security.jwt;

import org.springframework.stereotype.Component;

@Component
public class ShibbolethValidator {

    public Identita extractData(String shibToken) {
    	return new Identita(shibToken);
    }

    
}
