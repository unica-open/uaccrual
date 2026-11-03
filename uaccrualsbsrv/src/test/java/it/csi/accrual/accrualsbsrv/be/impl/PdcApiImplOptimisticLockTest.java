/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.exception.ValidationException;
import it.csi.accrual.accrualsbsrv.be.exception.DuplicateRecordException;
import it.csi.accrual.accrualsbsrv.be.exception.ForbiddenOperationException;
import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.Pdc;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;

/**
 * Test unitari per la gestione dell'Optimistic Locking nell'API PdcApiImpl.
 * 
 * Questi test verificano che l'API gestisca correttamente le eccezioni di
 * concorrenza (OptimisticLockException) lanciate dal service layer, restituendo
 * appropriate risposte HTTP 409 (CONFLICT) con oggetti Error strutturati.
 * 
 * @author ACCRUAL Team
 * @version 1.0
 * @since 2026-03-19
 */
@ExtendWith(MockitoExtension.class)
class PdcApiImplOptimisticLockTest {

    @Mock
    private PdcService pdcService;

    @InjectMocks
    private PdcApiImpl pdcApi;

    private Pdc testPdcDto;

    private static final String CODICE_UFFICIALE = "TEST.001";
    private static final String CODICE_CLASSE = "CLASSE-01";
    private static final String CODICE_FAMIGLIA = "FAMIGLIA-01";
    private static final String X_ENTE_CODICE = "ENTE-001";

    /**
     * Inizializza gli oggetti di test prima di ogni test case.
     */
    @BeforeEach
    void setUp() {
        // Inizializza DTO PDC per i test
        testPdcDto = new Pdc();
        testPdcDto.setId(1L);
        testPdcDto.setCodice("001");
        testPdcDto.setDescrizione("Conto di test");
        testPdcDto.setVersion(1);
    }

    /**
     * TC-OL-API-001: API restituisce HTTP 409 per OptimisticLockException
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia OptimisticLockException
     * durante un tentativo di aggiornamento.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia OptimisticLockException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 409 (CONFLICT)</p>
     * 
     * <p><strong>Verifica:</strong> response.getStatus() == 409</p>
     */
    @Test
    @DisplayName("TC-OL-API-001: API restituisce HTTP 409 per OptimisticLockException")
    void testUpdatePdcByTripleKey_OptimisticLockException_Returns409() {
        // Arrange
        // Configura il service per lanciare OptimisticLockException
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new OptimisticLockException("Il conto è stato modificato da un altro utente"));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.CONFLICT.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 409 (CONFLICT)"
        );
    }

    /**
     * TC-OL-API-002: API restituisce errore strutturato per conflitto
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia OptimisticLockException.
     * L'API deve restituire un oggetto Error strutturato con codice errore specifico.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia OptimisticLockException</p>
     * 
     * <p><strong>Atteso:</strong> Oggetto Error con code="OPTIMISTIC_LOCK"</p>
     * 
     * <p><strong>Verifica:</strong> entity.getCode().equals("OPTIMISTIC_LOCK")</p>
     */
    @Test
    @DisplayName("TC-OL-API-002: API restituisce errore strutturato con code OPTIMISTIC_LOCK")
    void testUpdatePdcByTripleKey_OptimisticLockException_ReturnsStructuredError() {
        // Arrange
        // Configura il service per lanciare OptimisticLockException
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new OptimisticLockException("Il conto è stato modificato da un altro utente"));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertNotNull(entity, "L'entity non dovrebbe essere null");
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        assertEquals(
            "OPTIMISTIC_LOCK",
            error.getCode(),
            "Il codice errore dovrebbe essere 'OPTIMISTIC_LOCK'"
        );
        assertEquals(
            "Conflitto di concorrenza",
            error.getTitle(),
            "Il titolo dovrebbe essere 'Conflitto di concorrenza'"
        );
        assertTrue(
            ((String) error.getDetail()).contains("modificato da un altro utente"),
            "Il dettaglio dovrebbe menzionare che il conto è stato modificato da un altro utente"
        );
    }

    /**
     * TC-OL-API-003: API gestisce aggiornamento con tripletta e conflitto
     * 
     * <p><strong>Scenario:</strong> Simile a TC-OL-API-001 ma verifica il comportamento
     * completo del metodo con tripletta (codiceUfficiale, codiceClasse, codiceFamiglia).</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia OptimisticLockException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 409 e messaggio di errore appropriato</p>
     */
    @Test
    @DisplayName("TC-OL-API-003: API gestisce aggiornamento con tripletta e conflitto")
    void testUpdatePdcByTripleKey_WithTripletKey_OptimisticLockException() {
        // Arrange
        String messaggioErrore = "Il conto TEST.001 è stato modificato da un altro utente. Ricaricare la pagina e riprovare.";
        
        // Configura il service per lanciare OptimisticLockException con messaggio specifico
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new OptimisticLockException(messaggioErrore));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.CONFLICT.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 409 (CONFLICT)"
        );

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertNotNull(entity, "L'entity non dovrebbe essere null");
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        
        // Verifica i campi dell'oggetto Error
        assertNotNull(error.getCode(), "Il codice errore non dovrebbe essere null");
        assertEquals("OPTIMISTIC_LOCK", error.getCode());

        assertNotNull(error.getTitle(), "Il titolo non dovrebbe essere null");
        assertEquals("Conflitto di concorrenza", error.getTitle());

        assertNotNull(error.getDetail(), "Il dettaglio non dovrebbe essere null");
        assertTrue(
            ((String) error.getDetail()).contains("modificato da un altro utente") ||
            ((String) error.getDetail()).contains("Ricaricare la pagina"),
            "Il dettaglio dovrebbe fornire istruzioni per risolvere il conflitto"
        );
    }

    /**
     * TC-OL-API-004: API gestisce aggiornamento riuscito senza conflitti
     * 
     * <p><strong>Scenario:</strong> Il service layer completa l'aggiornamento senza eccezioni.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che restituisce PDC aggiornato</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 200 (OK) e PDC aggiornato</p>
     */
    @Test
    @DisplayName("TC-OL-API-004: API gestisce aggiornamento riuscito senza conflitti")
    void testUpdatePdcByTripleKey_Success_NoConflict() {
        // Arrange
        Pdc updatedPdc = new Pdc();
        updatedPdc.setId(1L);
        updatedPdc.setCodiceUfficiale(CODICE_UFFICIALE);
        updatedPdc.setDescrizione("Conto aggiornato");
        updatedPdc.setVersion(3); // Version incrementata

        // Configura il service per restituire il PDC aggiornato
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenReturn(updatedPdc);

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.OK.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 200 (OK)"
        );

        // Verifica che l'entity sia il PDC aggiornato
        Object entity = response.getEntity();
        assertNotNull(entity, "L'entity non dovrebbe essere null");
        assertTrue(entity instanceof Pdc, "L'entity dovrebbe essere un oggetto Pdc");

        Pdc resultPdc = (Pdc) entity;
        assertEquals(CODICE_UFFICIALE, resultPdc.getCodiceUfficiale());
        assertEquals((long) 3L, (long) resultPdc.getVersion(), "La version dovrebbe essere incrementata");
    }

    /**
     * TC-OL-API-005: API gestisce NotFoundException durante aggiornamento
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia NotFoundException perché
     * il PDC non esiste.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia NotFoundException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 404 (NOT FOUND)</p>
     */
    @Test
    @DisplayName("TC-OL-API-005: API gestisce NotFoundException durante aggiornamento")
    void testUpdatePdcByTripleKey_NotFoundException_Returns404() {
        // Arrange
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new NotFoundException("PDC non trovato con codice ufficiale: " + CODICE_UFFICIALE));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.NOT_FOUND.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 404 (NOT FOUND)"
        );

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        assertEquals("NOT_FOUND", error.getCode());
    }

    /**
     * TC-OL-API-006: API gestisce ValidationException durante aggiornamento
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia ValidationException per
     * dati di input non validi.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia ValidationException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 400 (BAD REQUEST)</p>
     */
    @Test
    @DisplayName("TC-OL-API-006: API gestisce ValidationException durante aggiornamento")
    void testUpdatePdcByTripleKey_ValidationException_Returns400() {
        // Arrange
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new ValidationException("Dati non validi: descrizione obbligatoria"));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.BAD_REQUEST.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 400 (BAD REQUEST)"
        );

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        assertEquals("VALIDATION_ERROR", error.getCode());
    }

    /**
     * TC-OL-API-007: API gestisce DuplicateRecordException durante aggiornamento
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia DuplicateRecordException per
     * un codice duplicato.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia DuplicateRecordException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 409 (CONFLICT)</p>
     */
    @Test
    @DisplayName("TC-OL-API-007: API gestisce DuplicateRecordException durante aggiornamento")
    void testUpdatePdcByTripleKey_DuplicateRecordException_Returns409() {
        // Arrange
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new DuplicateRecordException("Codice già esistente"));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.CONFLICT.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 409 (CONFLICT)"
        );

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        assertEquals("DUPLICATE_RECORD", error.getCode());
    }

    /**
     * TC-OL-API-008: API gestisce ForbiddenOperationException durante aggiornamento
     * 
     * <p><strong>Scenario:</strong> Il service layer lancia ForbiddenOperationException per
     * permessi mancanti.</p>
     * 
     * <p><strong>Input:</strong> Chiamata a updatePdcByTripleKey con service che lancia ForbiddenOperationException</p>
     * 
     * <p><strong>Atteso:</strong> Response con status 403 (FORBIDDEN)</p>
     */
    @Test
    @DisplayName("TC-OL-API-008: API gestisce ForbiddenOperationException durante aggiornamento")
    void testUpdatePdcByTripleKey_ForbiddenOperationException_Returns403() {
        // Arrange
        when(pdcService.updatePdcByTripleKey(
                eq(CODICE_UFFICIALE),
                eq(CODICE_CLASSE),
                eq(CODICE_FAMIGLIA),
                
                any(Pdc.class)))
            .thenThrow(new ForbiddenOperationException("Operazione non consentita su questo conto"));

        // Act
        Response response = pdcApi.updatePdcByTripleKey(
            testPdcDto,
            
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(response, "La response non dovrebbe essere null");
        assertEquals(
            Response.Status.FORBIDDEN.getStatusCode(),
            response.getStatus(),
            "Lo status code dovrebbe essere 403 (FORBIDDEN)"
        );

        // Verifica che l'entity sia un oggetto Error
        Object entity = response.getEntity();
        assertTrue(entity instanceof Error, "L'entity dovrebbe essere un oggetto Error");

        Error error = (Error) entity;
        assertEquals("FORBIDDEN_OPERATION", error.getCode());
    }
}
