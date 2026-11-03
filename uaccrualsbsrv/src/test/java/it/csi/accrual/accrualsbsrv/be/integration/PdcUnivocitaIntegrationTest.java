/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration;

import static org.junit.jupiter.api.Assertions.*;

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
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcClasseRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcTipoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcFamigliaRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.be.service.PdcValidator;
import it.csi.accrual.accrualsbsrv.dto.Pdc;

/**
 * Test di integrazione per la validazione di univocità FM-02.
 * Verifica il flusso completo: Service → Validator → Repository → Database
 *
 * Requisito testato: CDU-003 5.1 - La combinazione (ente, codice ufficiale, classe, famiglia) deve essere univoca.
 * Codice errore: WEB_ERR_0005
 *
 * NOTA: Questo test richiede PostgreSQL reale per funzionare correttamente.
 * Attualmente disabilitato per problemi di compatibilità H2/Hibernate 6.
 * Per eseguirlo: configurare un database PostgreSQL di test e aggiornare application.properties
 *
 * @author Team ACCRUAL
 * @since 1.0.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "logging.level.it.csi.accrual=DEBUG"
})
@Transactional
@org.junit.jupiter.api.Disabled("Richiede PostgreSQL reale - vedi nota nel Javadoc")
class PdcUnivocitaIntegrationTest {

    @Autowired
    private PdcService pdcService;

    @Autowired
    private PdcValidator pdcValidator;

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
        // Creo le entità di base per il test (non uso dati SQL esterni)

        // Ente
        enteTest = new AccrualTEnte();
        enteTest.setCodiceUnivoco("TEST001");
        // enteTest.setDenominazione("Ente Test per Integrazione");
        enteTest = enteRepository.save(enteTest);

        // Tipo
        tipoTest = new AccrualDPdcTipo();
        tipoTest.setCodice("TIPO_TEST_INT");
        tipoTest.setDescrizione("Tipo Test Integrazione");
        tipoTest = tipoRepository.save(tipoTest);

        // Famiglia
        famigliaTest = new AccrualTPdcFamiglia();
        famigliaTest.setCodice("FAM_TEST_INT");
        famigliaTest.setDescrizione("Famiglia Test Integrazione");
        famigliaTest.setModificabile(false);
        famigliaTest.setRicercabile(true);
        famigliaTest = famigliaRepository.save(famigliaTest);

        // Classe
        classeTest = new AccrualDPdcClasse();
        classeTest.setCodice("CLASSE_TEST_INT");
        classeTest.setDescrizione("Classe Test Integrazione");
        classeTest.setTipoImporto(it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto.LORDO);
        classeTest.setInizioValidita(java.time.LocalDateTime.now());
        classeTest = classeRepository.save(classeTest);

        // Stato
        statoTest = new AccrualDPdcStato();
        statoTest.setCodice("ATTIVO");
        statoTest.setDescrizione("Attivo");
        statoTest = statoRepository.save(statoTest);
    }

    /**
     * Pulisce i dati di test creati durante i test
     */
    private void cleanupTestData() {
        // Elimina i conti creati per i test usando le query dirette
        pdcRepository.deleteAll(
            pdcRepository.findByCodiceUfficialeAndEnte_Id("COD.UFF.INT.001", enteTest.getId())
                .map(java.util.Collections::singletonList)
                .orElse(java.util.Collections.emptyList())
        );
        pdcRepository.deleteAll(
            pdcRepository.findByCodiceUfficialeAndEnte_Id("COD.UFF.INT.002", enteTest.getId())
                .map(java.util.Collections::singletonList)
                .orElse(java.util.Collections.emptyList())
        );
        // Elimina anche gli altri conti di test
        for (int i = 3; i <= 7; i++) {
            pdcRepository.deleteAll(
                pdcRepository.findByCodiceUfficialeAndEnte_Id("COD.UFF.INT.00" + i, enteTest.getId())
                    .map(java.util.Collections::singletonList)
                    .orElse(java.util.Collections.emptyList())
            );
        }
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - INSERIMENTO
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM02-001: Inserimento conto unico - successo")
    void testInserimentoContoUnico_Success() {
        // Arrange
        Pdc pdcInput = createPdcInput("COD.UFF.INT.001", "Conto Test Unico");

        // Act
        Pdc pdcCreato = pdcService.createPdc(pdcInput);

        // Assert
        assertNotNull(pdcCreato);
        assertNotNull(pdcCreato.getId());
        assertEquals("COD.UFF.INT.001", pdcCreato.getCodiceUfficiale());
        assertEquals("Conto Test Unico", pdcCreato.getDescrizione());
    }

    @Test
    @DisplayName("TC-INT-FM02-002: Inserimento conto duplicato - fallisce con WEB_ERR_0005")
    void testInserimentoContoDuplicato_Fail() {
        // Arrange - Creo il primo conto
        Pdc primoConto = createPdcInput("COD.UFF.INT.002", "Primo Conto");
        Pdc contoCreato = pdcService.createPdc(primoConto);
        assertNotNull(contoCreato);

        // Act - provo a creare un secondo conto con la stessa chiave di univocità
        Pdc secondoConto = createPdcInput("COD.UFF.INT.002", "Secondo Conto Duplicato");

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.createPdc(secondoConto)
        );

        assertTrue(exception.getMessage().contains("Esiste già un conto con codice ufficiale"));
        assertTrue(exception.getMessage().contains("COD.UFF.INT.002"));
    }

    @Test
    @DisplayName("TC-INT-FM02-003: Inserimento con stesso codice ma classe diversa - successo")
    void testInserimentoStessoCodiceClasseDiversa_Success() {
        // Arrange - Creo una seconda classe
        AccrualDPdcClasse classeDiversa = new AccrualDPdcClasse();
        classeDiversa.setCodice("CLASSE_TEST_INT_2");
        classeDiversa.setDescrizione("Classe Test Integrazione 2");
        classeDiversa = classeRepository.save(classeDiversa);

        Pdc primoConto = createPdcInput("COD.UFF.INT.003", "Primo Conto");
        pdcService.createPdc(primoConto);

        // Creo un secondo conto con stesso codice ma classe diversa
        Pdc secondoConto = createPdcInput("COD.UFF.INT.003", "Secondo Conto");
        secondoConto.setPdcClasse(mapToPdcClasse(classeDiversa));

        // Act & Assert - Non deve lanciare eccezione
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    @Test
    @DisplayName("TC-INT-FM02-004: Inserimento con stesso codice ma famiglia diversa - successo")
    void testInserimentoStessoCodiceFamigliaDiversa_Success() {
        // Arrange - Creo una seconda famiglia
        AccrualTPdcFamiglia famigliaDiversa = new AccrualTPdcFamiglia();
        famigliaDiversa.setCodice("FAM_TEST_INT_2");
        famigliaDiversa.setDescrizione("Famiglia Test Integrazione 2");
        famigliaDiversa = famigliaRepository.save(famigliaDiversa);

        Pdc primoConto = createPdcInput("COD.UFF.INT.004", "Primo Conto");
        pdcService.createPdc(primoConto);

        // Creo un secondo conto con stesso codice ma famiglia diversa
        Pdc secondoConto = createPdcInput("COD.UFF.INT.004", "Secondo Conto");
        secondoConto.setPdcFamiglia(mapToPdcFamiglia(famigliaDiversa));

        // Act & Assert - Non deve lanciare eccezione
        assertDoesNotThrow(() -> {
            Pdc risultato = pdcService.createPdc(secondoConto);
            assertNotNull(risultato);
        });
    }

    // ============================================================================
    // TEST DI INTEGRAZIONE - AGGIORNAMENTO
    // ============================================================================

    @Test
    @DisplayName("TC-INT-FM02-005: Aggiornamento stesso conto - successo")
    void testAggiornamentoStessoConto_Success() {
        // Arrange - Creo un conto
        Pdc pdcInput = createPdcInput("COD.UFF.INT.005", "Conto Originale");
        Pdc contoCreato = pdcService.createPdc(pdcInput);

        // Act - Aggiorno la descrizione (senza cambiare la chiave di univocità)
        Pdc pdcDaAggiornare = new Pdc();
        pdcDaAggiornare.setDescrizione("Conto Modificato");
        pdcDaAggiornare.setPdcClasse(contoCreato.getPdcClasse());
        pdcDaAggiornare.setPdcFamiglia(contoCreato.getPdcFamiglia());
        pdcDaAggiornare.setVersion(contoCreato.getVersion());

        Pdc contoAggiornato = pdcService.updatePdc("COD.UFF.INT.005", pdcDaAggiornare);

        // Assert
        assertNotNull(contoAggiornato);
        assertEquals("Conto Modificato", contoAggiornato.getDescrizione());
    }

    @Test
    @DisplayName("TC-INT-FM02-006: Aggiornamento che crea duplicato - fallisce")
    void testAggiornamentoCheCreaDuplicato_Fail() {
        // Arrange - Creo due conti con codici diversi
        Pdc primoConto = createPdcInput("COD.UFF.INT.006", "Primo Conto");
        pdcService.createPdc(primoConto);

        Pdc secondoConto = createPdcInput("COD.UFF.INT.007", "Secondo Conto");
        pdcService.createPdc(secondoConto);

        // Act - provo ad aggiornare il secondo conto per avere lo stesso codice del primo
        Pdc pdcDaAggiornare = new Pdc();
        pdcDaAggiornare.setCodiceUfficiale("COD.UFF.INT.006"); // Stesso del primo
        pdcDaAggiornare.setDescrizione("Secondo Conto Modificato");
        pdcDaAggiornare.setPdcClasse(secondoConto.getPdcClasse());
        pdcDaAggiornare.setPdcFamiglia(secondoConto.getPdcFamiglia());
        pdcDaAggiornare.setVersion(secondoConto.getVersion());

        // Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcService.updatePdc("COD.UFF.INT.007", pdcDaAggiornare)
        );

        assertTrue(exception.getMessage().contains("Esiste già un conto con codice ufficiale"));
        assertTrue(exception.getMessage().contains("COD.UFF.INT.006"));
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
        pdc.setInizioValidita(new java.util.Date());
        
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
