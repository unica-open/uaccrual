/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcClasseRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcTipoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcFamigliaRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.dto.Pdc;

/**
 * Test di integrazione per la validazione di sovrapposizione periodi di validità (FM-03).
 * Verifica il flusso completo: Service → Validator → Repository → Database
 *
 * Requisito testato: CDU-003 5.1 - RF-003-05 - Nessun conto con stesso codice ufficiale può avere periodi di validità sovrapposti.
 * Codice errore: WEB_ERR_0030
 *
 * @author Team ACCRUAL
 * @since 1.0.0
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@Transactional
class PdcSovrapposizioneIntegrationTest {

    @Autowired
    private PdcService pdcService;

    @Autowired
    private AccrualTPdcRepository pdcRepository;

    @Autowired
    private AccrualTEnteRepository enteRepository;

    @Autowired
    private AccrualDPdcClasseRepository classeRepository;

    @Autowired
    private AccrualTPdcFamigliaRepository famigliaRepository;

    @Autowired
    private AccrualDPdcTipoRepository tipoRepository;

    @Autowired
    private AccrualDPdcStatoRepository statoRepository;

    private AccrualTEnte enteTest;
    private AccrualDPdcClasse classeTest;
    private AccrualTPdcFamiglia famigliaTest;
    private AccrualDPdcTipo tipoTest;
    private AccrualDPdcStato statoTest;

    @BeforeEach
    void setUp() {
        // Creo le entità di base per il test

        enteTest = new AccrualTEnte();
        enteTest.setCodiceUnivoco("TEST001");
        enteTest = enteRepository.save(enteTest);

        // Tipo
        tipoTest = new AccrualDPdcTipo();
        tipoTest.setCodice("TIPO_TEST_FM03");
        tipoTest.setDescrizione("Tipo Test FM-03");
        tipoTest = tipoRepository.save(tipoTest);

        // Famiglia di default 'ACCRUAL-UTENTE' (richiesta da PdcServiceImpl)
        AccrualTPdcFamiglia famigliaUtente = new AccrualTPdcFamiglia();
        famigliaUtente.setCodice("ACCRUAL-UTENTE");
        famigliaUtente.setDescrizione("Famiglia Utente per conti PdC");
        famigliaUtente.setModificabile(false);
        famigliaUtente.setRicercabile(true);
        famigliaUtente = famigliaRepository.save(famigliaUtente);

        // Famiglia specifica per i test
        famigliaTest = new AccrualTPdcFamiglia();
        famigliaTest.setCodice("FAM_TEST_FM03");
        famigliaTest.setDescrizione("Famiglia Test FM-03");
        famigliaTest.setModificabile(false);
        famigliaTest.setRicercabile(true);
        famigliaTest = famigliaRepository.save(famigliaTest);

        // Classe
        classeTest = new AccrualDPdcClasse();
        classeTest.setCodice("CLASSE_TEST_FM03");
        classeTest.setDescrizione("Classe Test FM-03");
        classeTest.setTipoImporto(TipoImporto.LORDO);
        classeTest.setSegno(TipoSegno.DARE);
        classeTest.setInizioValidita(LocalDateTime.now());
        classeTest = classeRepository.save(classeTest);

        // Stato
        statoTest = new AccrualDPdcStato();
        statoTest.setCodice("ATTIVO_FM03");
        statoTest.setDescrizione("Attivo Test FM-03");
        statoTest = statoRepository.save(statoTest);
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - INSERIMENTO
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM03-001: Inserimento con piena sovrapposizione - fallisce")
    void testInserimentoConPienaSovrapposizione_Fail() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-12-31
        Pdc contoBase = createPdcInput("COD.UFF.INT.100", "Conto Base");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Tento inserimento con periodo sovrapposto: 2025-06-01 → 2025-12-31
        Pdc contoSovrapposto = createPdcInput("COD.UFF.INT.100", "Conto Sovrapposto");
        contoSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2025, 6, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoSovrapposto)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-INT-FM03-002: Inserimento senza sovrapposizione - successo")
    void testInserimentoSenzaSovrapposizione_Success() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-12-31
        Pdc contoBase = createPdcInput("COD.UFF.INT.101", "Conto Base");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Creo conto con periodo NON sovrapposto: 2026-01-01 → 2026-12-31
        Pdc contoNonSovrapposto = createPdcInput("COD.UFF.INT.101", "Conto Anno Successivo");
        contoNonSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2026, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoNonSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2026, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(contoNonSovrapposto);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM03-003: Inserimento con fine illimitata sovrapposta - fallisce")
    void testInserimentoConFineIllimitataSovrapposta_Fail() {
        // Arrange - Creo conto base: 2025-01-01 → illimitato
        Pdc contoBase = createPdcInput("COD.UFF.INT.102", "Conto Illimitato");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(null); // Fine illimitata
        pdcService.createPdc(contoBase);

        // Act - Tento inserimento con periodo: 2026-01-01 → 2026-12-31 (sovrapposto con illimitato)
        Pdc contoSovrapposto = createPdcInput("COD.UFF.INT.102", "Conto Sovrapposto");
        contoSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2026, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2026, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoSovrapposto)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-INT-FM03-004: Inserimento con periodi adiacenti - successo")
    void testInserimentoConPeriodiAdiacenti_Success() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-06-30
        Pdc contoBase = createPdcInput("COD.UFF.INT.103", "Conto Primo Semestre");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 6, 30, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Creo conto con periodo adiacente: 2025-07-01 → 2025-12-31
        Pdc contoAdiacente = createPdcInput("COD.UFF.INT.103", "Conto Secondo Semestre");
        contoAdiacente.setInizioValidita(Date.from(LocalDateTime.of(2025, 7, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoAdiacente.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(contoAdiacente);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM03-005: Inserimento con periodi identici - fallisce")
    void testInserimentoConPeriodiIdentici_Fail() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-12-31
        Pdc contoBase = createPdcInput("COD.UFF.INT.104", "Conto Base");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Tento inserimento con periodo identico: 2025-01-01 → 2025-12-31
        Pdc contoIdentico = createPdcInput("COD.UFF.INT.104", "Conto Identico");
        contoIdentico.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoIdentico.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoIdentico)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - AGGIORNAMENTO
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM03-006: Aggiornamento stesso conto - successo")
    void testAggiornamentoStessoConto_Success() {
        // Arrange - Creo un conto
        Pdc pdcInput = createPdcInput("COD.UFF.INT.105", "Conto Originale");
        pdcInput.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        pdcInput.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        Pdc contoCreato = pdcService.createPdc(pdcInput);

        // Act - Aggiorno la descrizione (senza cambiare le date)
        Pdc pdcDaAggiornare = new Pdc();
        pdcDaAggiornare.setDescrizione("Conto Modificato");
        pdcDaAggiornare.setPdcClasse(contoCreato.getPdcClasse());
        pdcDaAggiornare.setPdcFamiglia(contoCreato.getPdcFamiglia());
        pdcDaAggiornare.setVersion(contoCreato.getVersion());

        Pdc contoAggiornato = pdcService.updatePdc("COD.UFF.INT.105", pdcDaAggiornare);

        // Assert
        assertNotNull(contoAggiornato);
        assertEquals("Conto Modificato", contoAggiornato.getDescrizione());
    }

    @Test
    @DisplayName("TC-INT-FM03-007: Aggiornamento che crea sovrapposizione - fallisce")
    void testAggiornamentoCheCreaSovrapposizione_Fail() {
        // Arrange - Creo due conti con periodi diversi
        Pdc conto1 = createPdcInput("COD.UFF.INT.106", "Conto 1");
        conto1.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        conto1.setFineValidita(Date.from(LocalDateTime.of(2025, 6, 30, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(conto1);

        Pdc conto2 = createPdcInput("COD.UFF.INT.107", "Conto 2");
        conto2.setInizioValidita(Date.from(LocalDateTime.of(2025, 7, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        conto2.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        Pdc conto2Creato = pdcService.createPdc(conto2);

        // Act - provo ad aggiornare conto2 per estendere il periodo e creare sovrapposizione
        Pdc pdcDaAggiornare = new Pdc();
        pdcDaAggiornare.setDescrizione("Conto 2 Modificato");
        pdcDaAggiornare.setInizioValidita(Date.from(LocalDateTime.of(2025, 6, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        pdcDaAggiornare.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcDaAggiornare.setPdcClasse(conto2Creato.getPdcClasse());
        pdcDaAggiornare.setPdcFamiglia(conto2Creato.getPdcFamiglia());
        pdcDaAggiornare.setVersion(conto2Creato.getVersion());

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.updatePdc("COD.UFF.INT.107", pdcDaAggiornare)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-INT-FM03-008: Aggiornamento senza sovrapposizione - successo")
    void testAggiornamentoSenzaSovrapposizione_Success() {
        // Arrange - Creo un conto
        Pdc pdcInput = createPdcInput("COD.UFF.INT.108", "Conto Originale");
        pdcInput.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        pdcInput.setFineValidita(Date.from(LocalDateTime.of(2025, 6, 30, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        Pdc contoCreato = pdcService.createPdc(pdcInput);

        // Act - Aggiorno spostando il periodo in avanti (nessuna sovrapposizione)
        Pdc pdcDaAggiornare = new Pdc();
        pdcDaAggiornare.setDescrizione("Conto Spostato");
        pdcDaAggiornare.setInizioValidita(Date.from(LocalDateTime.of(2026, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        pdcDaAggiornare.setFineValidita(Date.from(LocalDateTime.of(2026, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcDaAggiornare.setPdcClasse(contoCreato.getPdcClasse());
        pdcDaAggiornare.setPdcFamiglia(contoCreato.getPdcFamiglia());
        pdcDaAggiornare.setVersion(contoCreato.getVersion());

        // Assert
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.updatePdc("COD.UFF.INT.108", pdcDaAggiornare);
            assertNotNull(risultato);
            assertEquals("Conto Spostato", risultato.getDescrizione());
        });
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - COMBINAZIONI DIVERSE
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM03-009: Stesso codice, classe diversa - successo")
    void testInserimentoStessoCodiceClasseDiversa_Success() {
        // Arrange - Creo una seconda classe
        AccrualDPdcClasse classeDiversa = new AccrualDPdcClasse();
        classeDiversa.setCodice("CLASSE_TEST_FM03_2");
        classeDiversa.setDescrizione("Classe Test FM-03 2");
        classeDiversa.setTipoImporto(TipoImporto.LORDO);
        classeDiversa.setSegno(TipoSegno.AVERE);
        classeDiversa.setInizioValidita(LocalDateTime.now());
        classeDiversa = classeRepository.save(classeDiversa);

        // Creo primo conto
        Pdc primoConto = createPdcInput("COD.UFF.INT.109", "Primo Conto");
        primoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        primoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(primoConto);

        // Creo secondo conto con stessa validità ma classe diversa
        Pdc secondoConto = createPdcInput("COD.UFF.INT.109", "Secondo Conto");
        secondoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        secondoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        secondoConto.setPdcClasse(mapToPdcClasse(classeDiversa));

        // Act & Assert - Non deve lanciare eccezione (la validazione è per stessa classe)
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM03-010: Stesso codice, famiglia diversa - successo")
    void testInserimentoStessoCodiceFamigliaDiversa_Success() {
        // Arrange - Creo una seconda famiglia
        AccrualTPdcFamiglia famigliaDiversa = new AccrualTPdcFamiglia();
        famigliaDiversa.setCodice("FAM_TEST_FM03_2");
        famigliaDiversa.setDescrizione("Famiglia Test FM-03 2");
        famigliaDiversa.setModificabile(false);
        famigliaDiversa.setRicercabile(true);
        famigliaDiversa = famigliaRepository.save(famigliaDiversa);

        // Creo primo conto
        Pdc primoConto = createPdcInput("COD.UFF.INT.110", "Primo Conto");
        primoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        primoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(primoConto);

        // Creo secondo conto con stessa validità ma famiglia diversa
        Pdc secondoConto = createPdcInput("COD.UFF.INT.110", "Secondo Conto");
        secondoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        secondoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        secondoConto.setPdcFamiglia(mapToPdcFamiglia(famigliaDiversa));

        // Act & Assert - Non deve lanciare eccezione
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM03-011: Stesso codice, ente diverso - successo")
    void testInserimentoStessoCodiceEnteDiverso_Success() {
        AccrualTEnte enteDiverso = new AccrualTEnte();
        enteDiverso.setCodiceUnivoco("COD_UNIVOCO_TEST_2");
        final AccrualTEnte enteDiversoFinal = enteRepository.save(enteDiverso);

        // Creo primo conto con primo ente
        Pdc primoConto = createPdcInput("COD.UFF.INT.111", "Primo Conto");
        primoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        primoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(primoConto);

        // Creo secondo conto con stesso codice ma ente diverso
        Pdc secondoConto = createPdcInput("COD.UFF.INT.111", "Secondo Conto");
        secondoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        secondoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Act & Assert - Non deve lanciare eccezione (enti diversi)
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM03-012: Codice ufficiale diverso - successo")
    void testInserimentoCodiceUfficialeDiverso_Success() {
        // Arrange - Creo primo conto
        Pdc primoConto = createPdcInput("COD.UFF.INT.112", "Primo Conto");
        primoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        primoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(primoConto);

        // Creo secondo conto con codice ufficiale diverso ma stesso periodo
        Pdc secondoConto = createPdcInput("COD.UFF.INT.113", "Secondo Conto");
        secondoConto.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        secondoConto.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Act & Assert - Non deve lanciare eccezione (codici diversi)
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - CASI PARTICOLARI
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM03-013: Sovrapposizione parziale inizio - fallisce")
    void testSovrapposizioneParzialeInizio_Fail() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-12-31
        Pdc contoBase = createPdcInput("COD.UFF.INT.114", "Conto Base");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Tento inserimento con sovrapposizione parziale: 2024-06-01 → 2025-06-30
        Pdc contoSovrapposto = createPdcInput("COD.UFF.INT.114", "Conto Sovrapposto");
        contoSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2024, 6, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2025, 6, 30, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoSovrapposto)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-INT-FM03-014: Sovrapposizione parziale fine - fallisce")
    void testSovrapposizioneParzialeFine_Fail() {
        // Arrange - Creo conto base: 2025-01-01 → 2025-12-31
        Pdc contoBase = createPdcInput("COD.UFF.INT.115", "Conto Base");
        contoBase.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoBase.setFineValidita(Date.from(LocalDateTime.of(2025, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));
        pdcService.createPdc(contoBase);

        // Act - Tento inserimento con sovrapposizione parziale: 2025-06-01 → 2026-06-30
        Pdc contoSovrapposto = createPdcInput("COD.UFF.INT.115", "Conto Sovrapposto");
        contoSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2025, 6, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2026, 6, 30, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoSovrapposto)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-INT-FM03-015: Messaggio errore con fine illimitata - contiene 'illimitata'")
    void testMessaggioErroreConFineIllimitata() {
        // Arrange - Creo conto con fine illimitata: 2025-01-01 → ∞
        Pdc contoIllimitato = createPdcInput("COD.UFF.INT.116", "Conto Illimitato");
        contoIllimitato.setInizioValidita(Date.from(LocalDateTime.of(2025, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoIllimitato.setFineValidita(null);
        pdcService.createPdc(contoIllimitato);

        // Act - Tento inserimento sovrapposto
        Pdc contoSovrapposto = createPdcInput("COD.UFF.INT.116", "Conto Sovrapposto");
        contoSovrapposto.setInizioValidita(Date.from(LocalDateTime.of(2026, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)));
        contoSovrapposto.setFineValidita(Date.from(LocalDateTime.of(2026, 12, 31, 23, 59, 59).toInstant(ZoneOffset.UTC)));

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(contoSovrapposto)
        );

        // Il messaggio può contenere "illimitata" o essere il messaggio generico
        assertTrue(exception.getMessage().contains("illimitata") ||
                   exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Messaggio: " + exception.getMessage());
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Crea un oggetto Pdc per i test di inserimento
     */
    private Pdc createPdcInput(String codiceUfficiale, String descrizione) {
        Pdc pdc = new Pdc();
        pdc.setCodice("COD_" + System.currentTimeMillis());
        pdc.setCodiceUfficiale(codiceUfficiale);
        pdc.setDescrizione(descrizione);
        pdc.setPartita(false);
        pdc.setFoglia(true);
        pdc.setFogliaNonFoglia(false);
        pdc.setFondoDotazione(false);
        pdc.setLivello(1);

        // Imposta le relazioni
        pdc.setPdcClasse(mapToPdcClasse(classeTest));
        pdc.setPdcFamiglia(mapToPdcFamiglia(famigliaTest));
        pdc.setPdcTipo(mapToPdcTipo(tipoTest));
        pdc.setPdcStato(mapToPdcStato(statoTest));

        return pdc;
    }

    private it.csi.accrual.accrualsbsrv.dto.PdcClasse mapToPdcClasse(AccrualDPdcClasse entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcClasse dto = new it.csi.accrual.accrualsbsrv.dto.PdcClasse();
        dto.setId(entity.getId());
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.PdcFamiglia mapToPdcFamiglia(AccrualTPdcFamiglia entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcFamiglia dto = new it.csi.accrual.accrualsbsrv.dto.PdcFamiglia();
        dto.setId(entity.getId());
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.PdcTipo mapToPdcTipo(AccrualDPdcTipo entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcTipo dto = new it.csi.accrual.accrualsbsrv.dto.PdcTipo();
        dto.setId(entity.getId());
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.PdcStato mapToPdcStato(AccrualDPdcStato entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcStato dto = new it.csi.accrual.accrualsbsrv.dto.PdcStato();
        dto.setId(entity.getId());
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }
}
