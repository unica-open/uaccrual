/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.util;

import org.springframework.stereotype.Component;

@Component
public class EnteContextHolder {
    
    private static final ThreadLocal<Long> CURRENT_ENTE = new ThreadLocal<>();
    
    public static void setCurrentEnte(Long idEnte) {
        CURRENT_ENTE.set(idEnte);
    }
    
    public static Long getCurrentEnte() {
        return CURRENT_ENTE.get();
    }
    
    public static void clear() {
        CURRENT_ENTE.remove();
    }
}