/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

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
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcClasseRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcTipoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcFamigliaRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcMapper;
import it.csi.accrual.accrualsbsrv.be.service.PdcValidator;
import it.csi.accrual.accrualsbsrv.dto.Pdc;
import jakarta.persistence.OptimisticLockException;

/**
 * Test unitari per la gestione dell'Optimistic Locking nel servizio PdcServiceImpl.
 * 
 * Questi test verificano che il meccanismo di concorrenza ottimistica basato sul campo
 * 'version' funzioni correttamente durante l'aggiornamento dei conti del piano dei conti.
 * 
 * @author ACCRUAL Team
 * @version 1.0
 * @since 2026-03-19
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PdcServiceImplOptimisticLockTest {

    @Mock
    private AccrualTPdcRepository pdcRepository;

    @Mock
    private AccrualTEnteRepository enteRepository;

    @Mock
    private AccrualDPdcClasseRepository pdcClasseRepository;

    @Mock
    private AccrualDPdcTipoRepository pdcTipoRepository;

    @Mock
    private AccrualDPdcStatoRepository pdcStatoRepository;

    @Mock
    private AccrualTPdcFamigliaRepository pdcFamigliaRepository;

    @Mock
    private PdcMapper pdcMapper;

    @Mock
    private PdcValidator pdcValidator;

    @InjectMocks
    private PdcServiceImpl pdcService;

    private AccrualTPdc testPdcEntity;
    private AccrualTEnte testEnte;
    private AccrualDPdcClasse testClasse;
    private AccrualTPdcFamiglia testFamiglia;

    private static final String CODICE_UFFICIALE = "TEST.001";
    private static final String CODICE_CLASSE = "CLASSE-01";
    private static final String CODICE_FAMIGLIA = "FAMIGLIA-01";

    /**
     * Inizializza gli oggetti di test prima di ogni test case.
     * Configura i dati di base per ente, classe, famiglia e PDC.
     */
    @BeforeEach
    void setUp() {
        // Inizializza ente
        testEnte = new AccrualTEnte();
        testEnte.setId(1L);

        // Inizializza classe
        testClasse = new AccrualDPdcClasse();
        testClasse.setId(1L);
        testClasse.setCodice(CODICE_CLASSE);

        // Inizializza famiglia
        testFamiglia = new AccrualTPdcFamiglia();
        testFamiglia.setId(1L);
        testFamiglia.setCodice(CODICE_FAMIGLIA);

        // Inizializza entity PDC
        testPdcEntity = new AccrualTPdc();
        testPdcEntity.setId(1L);
        testPdcEntity.setCodiceUfficiale(CODICE_UFFICIALE);
        testPdcEntity.setCodice("001");
        testPdcEntity.setDescrizione("Conto di test");
        testPdcEntity.setEnte(testEnte);
        testPdcEntity.setPdcClasse(testClasse);
        testPdcEntity.setPdcFamiglia(testFamiglia);
        testPdcEntity.setVersion(2); // Versione iniziale nel DB
        // Inizializza i campi booleani obbligatori
        testPdcEntity.setFoglia(false);
        testPdcEntity.setPartita(false);
        testPdcEntity.setFondoDotazione(false);
        testPdcEntity.setFogliaNonFoglia(false);
        testPdcEntity.setPdcSiac(false);
        testPdcEntity.setLivello(1);

        // Configura i mock per i repository di lookup (usando lenient per evitare UnnecessaryStubbingException)
        lenient().when(enteRepository.findById(1L)).thenReturn(Optional.of(testEnte));
        lenient().when(pdcClasseRepository.findByCodice(CODICE_CLASSE)).thenReturn(Optional.of(testClasse));
        lenient().when(pdcFamigliaRepository.findByCodice(CODICE_FAMIGLIA)).thenReturn(Optional.of(testFamiglia));

        // Configura il validator per non lanciare eccezioni (verrà sovrascritto nei test specifici se necessario)
        lenient().doNothing().when(pdcValidator).validateForSave(any(AccrualTPdc.class), eq(false));
        
        // Configura il repository save per restituire l'entity aggiornata
        lenient().when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc entity = invocation.getArgument(0);
            // Incrementa la version per simulare il comportamento di JPA
            entity.setVersion(entity.getVersion() != null ? entity.getVersion() + 1 : 1);
            return entity;
        });
        
        // Configura il controllo esistenza figli (default: nessun figlio)
        lenient().when(pdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(any(Long.class))).thenReturn(false);
        
        // Nota: Il mapper non viene usato in updatePdcByTripleKey, quindi non serve mockarlo
        // Il service usa direttamente il DTO passato come parametro
    }

    /**
     * TC-OL-001: Aggiornamento con version mismatch
     * 
     * <p><strong>Scenario:</strong> L'utente tenta di aggiornare un conto con version=1,
     * ma il database ha version=2 (il record è stato modificato da un altro utente).</p>
     * 
     * <p><strong>Input:</strong> PdcDto con version=1, repository restituisce entity con version=2</p>
     * 
     * <p><strong>Atteso:</strong> OptimisticLockException lanciata dal service</p>
     * 
     * <p><strong>Verifica:</strong> Il messaggio dell'eccezione contiene "modificato da un altro utente"</p>
     */
    @Test
    @DisplayName("TC-OL-001: Aggiornamento con version mismatch lancia OptimisticLockException")
    void testUpdatePdcByTripleKey_VersionMismatch_ThrowsOptimisticLockException() {
        // Arrange
        // Crea un DTO con tutti i campi necessari valorizzati
        Pdc inputDto = new Pdc();
        inputDto.setId(1L);
        inputDto.setCodice("001");
        inputDto.setDescrizione("Conto di test aggiornato");
        inputDto.setVersion(1); // Version nel DTO (più vecchia)
        inputDto.setFoglia(Boolean.FALSE);
        inputDto.setPartita(Boolean.FALSE);
        inputDto.setFondoDotazione(Boolean.FALSE);
        inputDto.setLivello(1);
        
        // Configura il repository per restituire l'entity con version=2 (più recente)
        when(pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(
                CODICE_UFFICIALE, testClasse.getId(), testFamiglia.getId(), testEnte.getId()))
            .thenReturn(Optional.of(testPdcEntity));

        // Act & Assert
        OptimisticLockException exception = assertThrows(
            OptimisticLockException.class,
            () -> pdcService.updatePdcByTripleKey(
                    CODICE_UFFICIALE,
                    CODICE_CLASSE,
                    CODICE_FAMIGLIA,
                    inputDto
                ),
            "Dovrebbe lanciare OptimisticLockException quando le version non coincidono"
        );

        // Verifica che il messaggio dell'eccezione sia significativo
        assertTrue(
            exception.getMessage().contains("modificato da un altro utente"),
            "Il messaggio dell'eccezione dovrebbe indicare che il record è stato modificato da un altro utente"
        );

        // Verifica che il save non venga mai chiamato
        verify(pdcRepository, never()).save(any(AccrualTPdc.class));
    }

    /**
     * TC-OL-002: Aggiornamento con version matching
     * 
     * <p><strong>Scenario:</strong> L'utente aggiorna un conto con version=2,
     * e il database ha version=2 (nessuna modifica concorrente).</p>
     * 
     * <p><strong>Input:</strong> PdcDto con version=2, repository restituisce entity con version=2</p>
     * 
     * <p><strong>Atteso:</strong> Aggiornamento completato con successo, version incrementata a 3</p>
     * 
     * <p><strong>Verifica:</strong> repository.save() chiamato una volta, entity salvata ha version=3</p>
     */
    @Test
    @DisplayName("TC-OL-002: Aggiornamento con version matching riesce")
    void testUpdatePdcByTripleKey_VersionMatching_Success() {
        // Arrange
        // Crea un DTO con tutti i campi necessari valorizzati
        Pdc inputDto = new Pdc();
        inputDto.setId(1L);
        inputDto.setCodice("001");
        inputDto.setDescrizione("Conto di test aggiornato");
        inputDto.setVersion(2); // Version matching con il DB
        inputDto.setFoglia(Boolean.FALSE);
        inputDto.setPartita(Boolean.FALSE);
        inputDto.setFondoDotazione(Boolean.FALSE);
        inputDto.setLivello(1);

        // Debug: verifica che i campi siano valorizzati
        assertNotNull(inputDto.isFoglia(), "foglia non dovrebbe essere null");
        assertEquals(Boolean.FALSE, inputDto.isFoglia(), "foglia dovrebbe essere FALSE");

        // Configura il repository per restituire l'entity con version=2
        when(pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(
                CODICE_UFFICIALE, testClasse.getId(), testFamiglia.getId(), testEnte.getId()))
            .thenReturn(Optional.of(testPdcEntity));

        // Act
        Pdc result = pdcService.updatePdcByTripleKey(
            CODICE_UFFICIALE,
            CODICE_CLASSE,
            CODICE_FAMIGLIA,
            inputDto
        );

        // Assert
        assertNotNull(result, "Il risultato non dovrebbe essere null");
        assertEquals((long) 3L, (long) result.getVersion(), "La version dovrebbe essere incrementata a 3");

        // Verifica che il save sia stato chiamato esattamente una volta
        verify(pdcRepository, times(1)).save(any(AccrualTPdc.class));
    }

    /**
     * TC-OL-003: Aggiornamento con version null
     * 
     * <p><strong>Scenario:</strong> Primo aggiornamento o version non specificata nel DTO.
     * In questo caso, il controllo di concorrenza viene saltato.</p>
     * 
     * <p><strong>Input:</strong> PdcDto con version=null</p>
     * 
     * <p><strong>Atteso:</strong> Aggiornamento completato senza eccezioni</p>
     * 
     * <p><strong>Verifica:</strong> repository.save() chiamato, nessuna eccezione lanciata</p>
     */
    @Test
    @DisplayName("TC-OL-003: Aggiornamento con version null riesce")
    void testUpdatePdcByTripleKey_VersionNull_Success() {
        // Arrange
        // Crea un DTO con tutti i campi necessari valorizzati e version=null
        Pdc inputDto = new Pdc();
        inputDto.setId(1L);
        inputDto.setCodice("001");
        inputDto.setDescrizione("Conto di test aggiornato");
        inputDto.setVersion(null); // Version null
        inputDto.setFoglia(Boolean.FALSE);
        inputDto.setPartita(Boolean.FALSE);
        inputDto.setFondoDotazione(Boolean.FALSE);
        inputDto.setLivello(1);

        // Configura il repository per restituire l'entity
        when(pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(
                CODICE_UFFICIALE, testClasse.getId(), testFamiglia.getId(), testEnte.getId()))
            .thenReturn(Optional.of(testPdcEntity));

        // Act
        Pdc result = assertDoesNotThrow(
            () -> pdcService.updatePdcByTripleKey(
                    CODICE_UFFICIALE,
                    CODICE_CLASSE,
                    CODICE_FAMIGLIA,
                    inputDto
                ),
            "Non dovrebbe lanciare eccezioni quando version è null"
        );

        // Assert
        assertNotNull(result, "Il risultato non dovrebbe essere null");

        // Verifica che il save sia stato chiamato
        verify(pdcRepository, times(1)).save(any(AccrualTPdc.class));
    }

    /**
     * TC-OL-004: Aggiornamento con PDC non trovato
     * 
     * <p><strong>Scenario:</strong> L'utente tenta di aggiornare un conto che non esiste.</p>
     * 
     * <p><strong>Input:</strong> Tripletta che non corrisponde a nessun record</p>
     * 
     * <p><strong>Atteso:</strong> NotFoundException lanciata dal service</p>
     */
    @Test
    @DisplayName("TC-OL-004: Aggiornamento con PDC non trovato lancia NotFoundException")
    void testUpdatePdcByTripleKey_PdcNotFound_ThrowsNotFoundException() {
        // Arrange
        // Crea un DTO con tutti i campi necessari valorizzati
        Pdc inputDto = new Pdc();
        inputDto.setId(1L);
        inputDto.setCodice("001");
        inputDto.setDescrizione("Conto di test aggiornato");
        inputDto.setVersion(1);
        inputDto.setFoglia(Boolean.FALSE);
        inputDto.setPartita(Boolean.FALSE);
        inputDto.setFondoDotazione(Boolean.FALSE);
        inputDto.setLivello(1);
        
        // Configura il repository per restituire Optional.empty (PDC non trovato)
        when(pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(
                CODICE_UFFICIALE, testClasse.getId(), testFamiglia.getId(), testEnte.getId()))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> pdcService.updatePdcByTripleKey(
                    CODICE_UFFICIALE,
                    CODICE_CLASSE,
                    CODICE_FAMIGLIA,
                    inputDto
                ),
            "Dovrebbe lanciare NotFoundException quando il PDC non esiste"
        );

        // Verifica che il messaggio dell'eccezione sia significativo
        assertTrue(
            exception.getMessage().contains("PDC non trovato"),
            "Il messaggio dell'eccezione dovrebbe indicare che il PDC non è stato trovato"
        );

        // Verifica che il save non venga mai chiamato
        verify(pdcRepository, never()).save(any(AccrualTPdc.class));
    }
}
