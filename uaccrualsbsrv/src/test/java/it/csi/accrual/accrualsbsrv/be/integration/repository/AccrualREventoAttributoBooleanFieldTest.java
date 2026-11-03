/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualREventoAttributo;

@DataJpaTest
class AccrualREventoAttributoBooleanFieldTest {

    @Test
    void testAccrualREventoAttributoObbligatorioField() {
        // Creazione di un'istanza dell'entità
        AccrualREventoAttributo eventoAttributo = new AccrualREventoAttributo();
        
        // Test del valore di default
        // Il campo obbligatorio dovrebbe avere valore di default false
        assertNotNull(eventoAttributo.getObbligatorio());
        assertEquals(Boolean.FALSE, eventoAttributo.getObbligatorio());
        
        // Test dell'impostazione del valore
        eventoAttributo.setObbligatorio(true);
        assertEquals(Boolean.TRUE, eventoAttributo.getObbligatorio());
        
        eventoAttributo.setObbligatorio(false);
        assertEquals(Boolean.FALSE, eventoAttributo.getObbligatorio());
    }
}