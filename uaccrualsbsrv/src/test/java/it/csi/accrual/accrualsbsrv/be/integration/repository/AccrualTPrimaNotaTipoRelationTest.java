/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;

@DataJpaTest
class AccrualTPrimaNotaTipoRelationTest {

    @Test
    void testAccrualTPrimaNotaTipoRelation() {
        // Creazione delle entità
        AccrualDPrimaNotaTipo tipo = new AccrualDPrimaNotaTipo();
        tipo.setCodice("TEST_CODICE");
        tipo.setDescrizione("Test descrizione tipo prima nota");
        AccrualTPrimaNota primaNota = new AccrualTPrimaNota();

        // Impostazione della relazione
        primaNota.setTipo(tipo);

        // Verifica della relazione
        assertEquals(tipo, primaNota.getTipo());
        assertEquals("TEST_CODICE", primaNota.getTipo().getCodice());
        assertEquals("Test descrizione tipo prima nota", primaNota.getTipo().getDescrizione());
    }
}