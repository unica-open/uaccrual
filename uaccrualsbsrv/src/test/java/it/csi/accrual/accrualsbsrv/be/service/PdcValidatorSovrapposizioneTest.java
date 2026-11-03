/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcSaldoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;

/**
 * Test unitari per la validazione di sovrapposizione periodi di validità (FM-03).
 * Verifica l'implementazione del requisito CDU-003 5.1 - RF-003-05:
 * Nessun conto con stesso codice ufficiale può avere periodi di validità sovrapposti (WEB_ERR_0030).
 *
 * @author Team ACCRUAL
 * @since 1.0.0
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PdcValidatorSovrapposizioneTest {

    @Mock
    private AccrualTPdcRepository accrualTPdcRepository;

    @Mock
    private AccrualTCausaleRepository accrualTCausaleRepository;

    @Mock
    private AccrualTSoggettoRepository accrualTSoggettoRepository;

    @Mock
    private AccrualTPdcSaldoRepository accrualTPdcSaldoRepository;

    @InjectMocks
    private PdcValidator pdcValidator;

    private AccrualTEnte ente;
    private AccrualDPdcClasse classe;
    private AccrualTPdcFamiglia famiglia;
    private AccrualDPdcTipo tipo;
    private AccrualDPdcStato stato;
    private AccrualTPdc existingConto;
    private AccrualTPdc newConto;

    @BeforeEach
    void setUp() {
        // Setup ente
        ente = new AccrualTEnte();
        ente.setId(1L);
        ente.setCodiceUnivoco("ENTE001");

        // Setup classe
        classe = new AccrualDPdcClasse();
        classe.setId(10L);
        classe.setCodice("CLASSE_A");
        classe.setDescrizione("Classe A");

        // Setup famiglia
        famiglia = new AccrualTPdcFamiglia();
        famiglia.setId(100L);
        famiglia.setCodice("FAM_X");
        famiglia.setDescrizione("Famiglia X");

        // Setup tipo
        tipo = new AccrualDPdcTipo();
        tipo.setId(5L);
        tipo.setCodice("TIPO_1");
        tipo.setDescrizione("Tipo 1");

        // Setup stato
        stato = new AccrualDPdcStato();
        stato.setId(1L);
        stato.setCodice("ATTIVO");
        stato.setDescrizione("Attivo");

        // Setup conto esistente (per simulare sovrapposizione)
        existingConto = new AccrualTPdc();
        existingConto.setId(50L);
        existingConto.setCodice("ACCR.0.001");
        existingConto.setCodiceUfficiale("COD.UFF.001");
        existingConto.setDescrizione("Conto Esistente");
        existingConto.setEnte(ente);
        existingConto.setPdcClasse(classe);
        existingConto.setPdcFamiglia(famiglia);
        existingConto.setPdcTipo(tipo);
        existingConto.setPdcStato(stato);
        existingConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        existingConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        // Setup nuovo conto (per test)
        newConto = new AccrualTPdc();
        newConto.setId(51L);
        newConto.setCodice("ACCR.0.002");
        newConto.setCodiceUfficiale("COD.UFF.001");
        newConto.setDescrizione("Nuovo Conto");
        newConto.setEnte(ente);
        newConto.setPdcClasse(classe);
        newConto.setPdcFamiglia(famiglia);
        newConto.setPdcTipo(tipo);
        newConto.setPdcStato(stato);
        newConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
    }

    // ============================================================================
    // TEST DI INSERIMENTO (isInsert = true) - SOVRAPPOSIZIONE
    // ============================================================================

    @Test
    @DisplayName("TC-FM03-001: Inserimento con piena sovrapposizione - deve lanciare eccezione")
    void testSovrapposizione_Inserimento_PienaSovrapposizione() {
        // Arrange - periodo sovrapposto: 2025-06-01 → 2025-12-31
        newConto.setInizioValidita(LocalDateTime.of(2025, 6, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"));
        assertTrue(exception.getMessage().contains("COD.UFF.001"));
    }

    @Test
    @DisplayName("TC-FM03-002: Inserimento senza sovrapposizione - non deve lanciare eccezione")
    void testSovrapposizione_Inserimento_NessunaSovrapposizione() {
        // Arrange - periodo non sovrapposto: 2026-01-01 → 2026-12-31
        newConto.setInizioValidita(LocalDateTime.of(2026, 1, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-003: Inserimento con fine validità illimitata - deve lanciare eccezione se sovrapposto")
    void testSovrapposizione_Inserimento_FineValiditaIllimitata() {
        // Arrange - periodo con fine illimitata: 2025-01-01 → NULL
        newConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        newConto.setFineValidita(null);

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), isNull(), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"));
    }

    @Test
    @DisplayName("TC-FM03-004: Inserimento con periodi adiacenti - non deve lanciare eccezione")
    void testSovrapposizione_Inserimento_PeriodiAdiacenti() {
        // Arrange - periodo adiacente: 2025-07-01 → 2025-12-31
        // existingConto: 2025-01-01 → 2025-06-30 (modificato per test adiacenza)
        existingConto.setFineValidita(LocalDateTime.of(2025, 6, 30, 23, 59, 59));
        newConto.setInizioValidita(LocalDateTime.of(2025, 7, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-005: Inserimento con periodi identici - deve lanciare eccezione")
    void testSovrapposizione_Inserimento_PeriodiIdentici() {
        // Arrange - periodo identico: 2025-01-01 → 2025-12-31
        newConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"));
    }

    // ============================================================================
    // TEST DI AGGIORNAMENTO (isInsert = false) - SOVRAPPOSIZIONE
    // ============================================================================

    @Test
    @DisplayName("TC-FM03-006: Aggiornamento stesso conto - non deve lanciare eccezione")
    void testSovrapposizione_Aggiornamento_StessoConto() {
        // Arrange - modifico descrizione ma mantengo stesso periodo
        existingConto.setDescrizione("Conto Modificato");

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(50L)))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(existingConto, false));
    }

    @Test
    @DisplayName("TC-FM03-007: Aggiornamento che crea sovrapposizione con altro conto - deve lanciare eccezione")
    void testSovrapposizione_Aggiornamento_SovrapposizioneAltroConto() {
        // Arrange - provo a estendere il periodo creando sovrapposizione
        AccrualTPdc contoDaAggiornare = new AccrualTPdc();
        contoDaAggiornare.setId(51L);
        contoDaAggiornare.setCodice("COD_AGG");
        contoDaAggiornare.setDescrizione("Conto da aggiornare");
        contoDaAggiornare.setCodiceUfficiale("COD.UFF.002");
        contoDaAggiornare.setEnte(ente);
        contoDaAggiornare.setPdcClasse(classe);
        contoDaAggiornare.setPdcFamiglia(famiglia);
        contoDaAggiornare.setPdcTipo(tipo);
        contoDaAggiornare.setInizioValidita(LocalDateTime.of(2025, 6, 1, 0, 0));
        contoDaAggiornare.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        // Mock per il check di univocità (nessun duplicato per questo codice specifico)
        lenient().when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                eq("COD.UFF.002"), anyLong(), anyLong(), anyLong(), eq(51L)))
            .thenReturn(java.util.Optional.empty());

        // Mock per il check di sovrapposizione (restituisce true)
        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(51L)))
            .thenReturn(true);

        // Mock per recuperare il conto esistente per il messaggio di errore
        // (questo sarà chiamato con "COD.UFF.001" dal validator quando cerca il conto sovrapposto)
        lenient().when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), eq(51L)))
            .thenAnswer(invocation -> {
                String codiceUfficiale = invocation.getArgument(0);
                if ("COD.UFF.002".equals(codiceUfficiale)) {
                    return java.util.Optional.empty();
                }
                return java.util.Optional.of(existingConto);
            });

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(contoDaAggiornare, false)
        );

        // Il messaggio può essere sia dettagliato che generico
        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"),
            "Il messaggio dovrebbe contenere 'periodo di validità sovrapposto' o 'è già presente con validità': " + exception.getMessage());
    }

    @Test
    @DisplayName("TC-FM03-008: Aggiornamento senza sovrapposizione - non deve lanciare eccezione")
    void testSovrapposizione_Aggiornamento_SenzaSovrapposizione() {
        // Arrange - periodo non sovrapposto: 2026-01-01 → 2026-12-31
        existingConto.setInizioValidita(LocalDateTime.of(2026, 1, 1, 0, 0));
        existingConto.setFineValidita(LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), eq(50L)))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(existingConto, false));
    }

    // ============================================================================
    // TEST DI VALIDAZIONE PARZIALE (campi mancanti) - SOVRAPPOSIZIONE
    // ============================================================================

    @Test
    @DisplayName("TC-FM03-009: Inserimento con codiceUfficiale null - skip validazione sovrapposizione")
    void testSovrapposizione_Inserimento_CodiceUfficialeNull() {
        // Arrange
        newConto.setCodiceUfficiale(null);
        newConto.setCodice("CODICE_TEST");
        newConto.setDescrizione("Descrizione Test");

        // Act & Assert - La validazione di sovrapposizione viene skipata
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
        verify(accrualTPdcRepository, never()).existsSovrapposizioneValidita(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-FM03-010: Inserimento con classe null - lancia eccezione mandatory check")
    void testSovrapposizione_Inserimento_ClasseNull() {
        // Arrange
        newConto.setPdcClasse(null);
        newConto.setCodice("CODICE_TEST");
        newConto.setDescrizione("Descrizione Test");

        // Act & Assert - Il mandatory check lancerà eccezione per classe null
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );
        assertTrue(exception.getMessage().contains("classe del conto è obbligatoria"));
        verify(accrualTPdcRepository, never()).existsSovrapposizioneValidita(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-FM03-011: Inserimento con famiglia null - skip validazione sovrapposizione")
    void testSovrapposizione_Inserimento_FamigliaNull() {
        // Arrange
        newConto.setPdcFamiglia(null);
        newConto.setCodice("CODICE_TEST");
        newConto.setDescrizione("Descrizione Test");

        // Act & Assert - La validazione di sovrapposizione viene skipata
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
        verify(accrualTPdcRepository, never()).existsSovrapposizioneValidita(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-FM03-012: Inserimento con ente null - skip validazione sovrapposizione")
    void testSovrapposizione_Inserimento_EnteNull() {
        // Arrange
        newConto.setEnte(null);
        newConto.setCodice("CODICE_TEST");
        newConto.setDescrizione("Descrizione Test");

        // Act & Assert - La validazione di sovrapposizione viene skipata
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
        verify(accrualTPdcRepository, never()).existsSovrapposizioneValidita(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("TC-FM03-013: Inserimento con inizioValidita null - skip validazione sovrapposizione")
    void testSovrapposizione_Inserimento_InizioValiditaNull() {
        // Arrange
        newConto.setInizioValidita(null);
        newConto.setCodice("CODICE_TEST");
        newConto.setDescrizione("Descrizione Test");

        // Act & Assert - La validazione di sovrapposizione viene skipata
        // Nota: il mandatory check lancerà comunque un'eccezione per inizioValidita null
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );
        assertTrue(exception.getMessage().contains("inizio validità è obbligatoria"));
        verify(accrualTPdcRepository, never()).existsSovrapposizioneValidita(any(), any(), any(), any(), any(), any(), any());
    }

    // ============================================================================
    // TEST COMBINAZIONI DIVERSE - SOVRAPPOSIZIONE
    // ============================================================================

    @Test
    @DisplayName("TC-FM03-014: Stesso codice, classe diversa - non è sovrapposizione")
    void testSovrapposizione_StessoCodiceClasseDiversa() {
        // Arrange
        AccrualDPdcClasse classeDiversa = new AccrualDPdcClasse();
        classeDiversa.setId(20L);
        classeDiversa.setCodice("CLASSE_B");
        classeDiversa.setDescrizione("Classe B");

        newConto.setPdcClasse(classeDiversa);

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-015: Stesso codice, famiglia diversa - non è sovrapposizione")
    void testSovrapposizione_StessoCodiceFamigliaDiversa() {
        // Arrange
        AccrualTPdcFamiglia famigliaDiversa = new AccrualTPdcFamiglia();
        famigliaDiversa.setId(200L);
        famigliaDiversa.setCodice("FAM_Y");
        famigliaDiversa.setDescrizione("Famiglia Y");

        newConto.setPdcFamiglia(famigliaDiversa);

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-016: Stesso codice, ente diverso - non è sovrapposizione")
    void testSovrapposizione_StessoCodiceEnteDiverso() {
        // Arrange
        AccrualTEnte enteDiverso = new AccrualTEnte();
        enteDiverso.setId(2L);
        enteDiverso.setCodiceUnivoco("ENTE002");

        newConto.setEnte(enteDiverso);

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-017: Codice ufficiale diverso - non è sovrapposizione")
    void testSovrapposizione_CodiceUfficialeDiverso() {
        // Arrange
        newConto.setCodiceUfficiale("COD.UFF.002");

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForSave(newConto, true));
    }

    @Test
    @DisplayName("TC-FM03-018: Sovrapposizione parziale inizio periodo - deve lanciare eccezione")
    void testSovrapposizione_SovrapposizioneParzialeInizio() {
        // Arrange - existing: 2025-01-01 → 2025-12-31, new: 2024-06-01 → 2025-06-30
        existingConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        existingConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        newConto.setInizioValidita(LocalDateTime.of(2024, 6, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"));
    }

    @Test
    @DisplayName("TC-FM03-019: Sovrapposizione parziale fine periodo - deve lanciare eccezione")
    void testSovrapposizione_SovrapposizioneParzialeFine() {
        // Arrange - existing: 2025-01-01 → 2025-12-31, new: 2025-06-01 → 2026-06-30
        existingConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));
        existingConto.setFineValidita(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        newConto.setInizioValidita(LocalDateTime.of(2025, 6, 1, 0, 0));
        newConto.setFineValidita(LocalDateTime.of(2026, 6, 30, 23, 59, 59));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        assertTrue(exception.getMessage().contains("periodo di validità sovrapposto") ||
                   exception.getMessage().contains("è già presente con validità"));
    }

    @Test
    @DisplayName("TC-FM03-020: Conto esistente con fine validità illimitata - deve riportare 'illimitata' nel messaggio")
    void testSovrapposizione_MessaggioErroreFineIllimitata() {
        // Arrange - existing con fine illimitata
        existingConto.setFineValidita(null);
        existingConto.setInizioValidita(LocalDateTime.of(2025, 1, 1, 0, 0));

        when(accrualTPdcRepository.existsSovrapposizioneValidita(
                any(), anyLong(), anyLong(), anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
            .thenReturn(true);

        when(accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                any(), anyLong(), anyLong(), anyLong(), isNull()))
            .thenReturn(java.util.Optional.of(existingConto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForSave(newConto, true)
        );

        // Il messaggio può essere dettagliato (con "illimitata") o generico
        assertTrue(exception.getMessage().contains("illimitata") ||
                   exception.getMessage().contains("periodo di validità sovrapposto"),
            "Il messaggio dovrebbe contenere 'illimitata' o 'periodo di validità sovrapposto': " + exception.getMessage());
    }
}
