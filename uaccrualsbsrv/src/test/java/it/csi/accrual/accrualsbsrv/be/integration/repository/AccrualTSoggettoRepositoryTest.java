/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoStato;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoTipo;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualTSoggetto;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;

/**
 * Test di integrazione per AccrualTSoggettoRepository.
 * Utilizza un database in-memory (H2) per verificare le query e il mapping JPA.
 */
@DataJpaTest
class AccrualTSoggettoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccrualTSoggettoRepository soggettoRepository;

    @Test
    void whenFindById_andSoggettoExists_thenReturnSoggetto() {
        // 1. Setup: Recupero le entità di test esistenti dal database in-memory
        AccrualDSoggettoStato stato = entityManager.find(AccrualDSoggettoStato.class, 1L); // ID 1 from soggetto.sql
        AccrualDSoggettoTipo tipo = entityManager.find(AccrualDSoggettoTipo.class, 1L);   // ID 1 from soggetto.sql
        AccrualTEnte ente = entityManager.find(AccrualTEnte.class, 1L);     // ID 1 from soggetto.sql

        AccrualTSoggetto soggettoToSave = new AccrualTSoggetto();
        soggettoToSave.setCodice("SOGG001");
        soggettoToSave.setDescrizione("Franco Verdi");
        soggettoToSave.setCodiceFiscale("MRGFNC85M01H501X");
        soggettoToSave.setPa(false);
        soggettoToSave.setIstitutoDiCredito(false);
        soggettoToSave.setStato(stato);
        soggettoToSave.setTipo(tipo);
        soggettoToSave.setEnte(ente);
        soggettoToSave.setDataInserimento(java.time.LocalDateTime.now());
        soggettoToSave.setUtenteInserimento("test_user");
        
        AccrualTSoggetto savedSoggetto = entityManager.persistAndFlush(soggettoToSave);

        // 2. Act: Eseguo il metodo del repository da testare
        Optional<AccrualTSoggetto> foundSoggetto = soggettoRepository.findById(savedSoggetto.getId());

        // 3. Assert: Verifico che il risultato sia quello atteso
        assertThat(foundSoggetto).isPresent();
        assertThat(foundSoggetto.get().getCodiceFiscale()).isEqualTo("MRGFNC85M01H501X");
        assertThat(foundSoggetto.get().getDescrizione()).isEqualTo("Franco Verdi");
    }

    @Test
    void whenFindByCodiceFiscale_andSoggettoExists_thenReturnSoggetto() {
        // 1. Setup: Recupero le entità di test esistenti dal database in-memory
        AccrualDSoggettoStato stato = entityManager.find(AccrualDSoggettoStato.class, 1L); // ID 1 from soggetto.sql
        AccrualDSoggettoTipo tipo = entityManager.find(AccrualDSoggettoTipo.class, 1L);   // ID 1 from soggetto.sql
        AccrualTEnte ente = entityManager.find(AccrualTEnte.class, 1L);     // ID 1 from soggetto.sql

        AccrualTSoggetto soggettoToSave = new AccrualTSoggetto();
        soggettoToSave.setCodice("SOGG002");
        soggettoToSave.setDescrizione("Mario Rossi");
        soggettoToSave.setCodiceFiscale("RSSMRA80A01H501X");
        soggettoToSave.setPa(false);
        soggettoToSave.setIstitutoDiCredito(false);
        soggettoToSave.setStato(stato);
        soggettoToSave.setTipo(tipo);
        soggettoToSave.setEnte(ente);
        soggettoToSave.setDataInserimento(java.time.LocalDateTime.now());
        soggettoToSave.setUtenteInserimento("test_user");
        
        entityManager.persistAndFlush(soggettoToSave);

        // 2. Act: Eseguo il metodo del repository da testare
        Optional<AccrualTSoggetto> foundSoggetto = soggettoRepository.findByCodiceFiscale("RSSMRA80A01H501X");

        // 3. Assert: Verifico che il risultato sia quello atteso
        assertThat(foundSoggetto).isPresent();
        assertThat(foundSoggetto.get().getCodiceFiscale()).isEqualTo("RSSMRA80A01H501X");
        assertThat(foundSoggetto.get().getDescrizione()).isEqualTo("Mario Rossi");
    }
    
    @Test
    void whenFindById_andSoggettoNotExists_thenReturnEmpty() {
        // 1. Setup: non inserisco dati

        // 2. Act: Cerco un ID non esistente
        Optional<AccrualTSoggetto> foundSoggetto = soggettoRepository.findById(-1L);

        // 3. Assert: Verifico che il risultato sia vuoto
        assertThat(foundSoggetto).isNotPresent();
    }
}
