/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;

@DataJpaTest
class AccrualTPdcBooleanFieldTest {

    @Test
    void testAccrualTPdcFogliaNonFogliaField() {
        // Creazione di un'istanza dell'entità
        AccrualTPdc pdc = new AccrualTPdc();
        
        // Test del valore di default
        // Il campo fogliaNonFoglia dovrebbe avere valore di default false
        assertNotNull(pdc.getFogliaNonFoglia());
        assertEquals(Boolean.FALSE, pdc.getFogliaNonFoglia());
        
        // Test dell'impostazione del valore
        pdc.setFogliaNonFoglia(true);
        assertEquals(Boolean.TRUE, pdc.getFogliaNonFoglia());
        
        pdc.setFogliaNonFoglia(false);
        assertEquals(Boolean.FALSE, pdc.getFogliaNonFoglia());
    }
}