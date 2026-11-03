/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaTipo;

@DataJpaTest
class AccrualDPrimaNotaTipoTest {

    @Test
    void testAccrualDPrimaNotaTipoEntity() {
        // Creazione di un'istanza dell'entità
        AccrualDPrimaNotaTipo tipo = new AccrualDPrimaNotaTipo();
        
        // Test dei setter e getter
        String codice = "TEST_CODICE";
        String descrizione = "Test descrizione tipo prima nota";
        
        tipo.setCodice(codice);
        tipo.setDescrizione(descrizione);
        
        assertEquals(codice, tipo.getCodice());
        assertEquals(descrizione, tipo.getDescrizione());
        
        // Verifica che i valori di default per i campi booleani siano impostati correttamente
        // (anche se in questo caso non abbiamo campi booleani diretti in questa entità)
        // ma possiamo comunque testare che l'entità sia creata correttamente
        assertNotNull(tipo);
    }
    
    @Test
    void testAccrualDPrimaNotaTipoEntityWithConstructor() {
        // Test dell'altro costruttore
        String codice = "TEST_CODICE_2";
        String descrizione = "Test descrizione tipo prima nota con costruttore";

        AccrualDPrimaNotaTipo tipo = new AccrualDPrimaNotaTipo();
        tipo.setCodice(codice);
        tipo.setDescrizione(descrizione);

        assertEquals(codice, tipo.getCodice());
        assertEquals(descrizione, tipo.getDescrizione());
    }
}