/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    // Scritture fuori da una richiesta autenticata (elaborazioni asincrone dei flussi):
    // lo stesso valore che FlussoServiceImpl imposta esplicitamente.
    private static final String UTENTE_SISTEMA = "SYSTEM";

    @Autowired
    private SecurityContextHelper securityContextHelper;

    // AI generated 2026-09-10
    // reviewed by simonep
    @Override
    public Optional<String> getCurrentAuditor() {
        String codiceOperatore = securityContextHelper.getCodiceFiscale();
        return Optional.of(codiceOperatore != null ? codiceOperatore : UTENTE_SISTEMA);
    }
}
