/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcSaldoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;

/**
 * Test unitari per la validazione di eliminazione conti PdC (FM-06).
 * Verifica i controlli sui legami prima di procedere con l'eliminazione logica.
 * 
 * Requisito testato: CDU-003 5.2 - Impedire eliminazione se esistono legami (WEB_ERR_0012)
 * 
 * @author Team ACCRUAL
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PdcValidatorDeleteTest {

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
    
    private static final Long CONTO_ID = 1L;
    
    @BeforeEach
    void setUp() {
        // Reset dei mock prima di ogni test
        reset(accrualTPdcRepository, accrualTCausaleRepository, 
              accrualTSoggettoRepository, accrualTPdcSaldoRepository);
    }
    
    @Test
    @DisplayName("TC-FM06-001: Eliminazione con figli lancia eccezione")
    void testValidateForDelete_WithFigli_ThrowsException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("figli associati"));
        verify(accrualTPdcRepository, times(1))
            .existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID);
        // Gli altri repository non dovrebbero essere chiamati (short-circuit)
        verify(accrualTCausaleRepository, never()).existsByPdc_Id(anyLong());
        verify(accrualTSoggettoRepository, never()).existsByPdc_Id(anyLong());
        verify(accrualTPdcSaldoRepository, never()).existsByPdc_IdAndDataCancellazioneIsNull(anyLong());
    }
    
    @Test
    @DisplayName("TC-FM06-002: Eliminazione con prime note lancia eccezione")
    void testValidateForDelete_WithPrimeNote_ThrowsException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcRepository.existsPrimeNoteAssociate(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("prime note associate"));
        verify(accrualTPdcRepository, times(1)).existsPrimeNoteAssociate(CONTO_ID);
        verify(accrualTCausaleRepository, never()).existsByPdc_Id(anyLong());
    }
    
    @Test
    @DisplayName("TC-FM06-003: Eliminazione con causali lancia eccezione")
    void testValidateForDelete_WithCausali_ThrowsException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcRepository.existsPrimeNoteAssociate(CONTO_ID))
            .thenReturn(false);
        when(accrualTCausaleRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("causali associate"));
        verify(accrualTCausaleRepository, times(1)).existsByPdc_Id(CONTO_ID);
        verify(accrualTSoggettoRepository, never()).existsByPdc_Id(anyLong());
    }
    
    @Test
    @DisplayName("TC-FM06-004: Eliminazione con soggetti lancia eccezione")
    void testValidateForDelete_WithSoggetti_ThrowsException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcRepository.existsPrimeNoteAssociate(CONTO_ID))
            .thenReturn(false);
        when(accrualTCausaleRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(false);
        when(accrualTSoggettoRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("soggetti associati"));
        verify(accrualTSoggettoRepository, times(1)).existsByPdc_Id(CONTO_ID);
        verify(accrualTPdcSaldoRepository, never()).existsByPdc_IdAndDataCancellazioneIsNull(anyLong());
    }
    
    @Test
    @DisplayName("TC-FM06-005: Eliminazione con saldi lancia eccezione")
    void testValidateForDelete_WithSaldi_ThrowsException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcRepository.existsPrimeNoteAssociate(CONTO_ID))
            .thenReturn(false);
        when(accrualTCausaleRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(false);
        when(accrualTSoggettoRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcSaldoRepository.existsByPdc_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("saldi associati"));
        verify(accrualTPdcSaldoRepository, times(1)).existsByPdc_IdAndDataCancellazioneIsNull(CONTO_ID);
    }
    
    @Test
    @DisplayName("TC-FM06-006: Eliminazione senza legami non lancia eccezioni")
    void testValidateForDelete_NoLegami_NoException() {
        // Arrange
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcRepository.existsPrimeNoteAssociate(CONTO_ID))
            .thenReturn(false);
        when(accrualTCausaleRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(false);
        when(accrualTSoggettoRepository.existsByPdc_Id(CONTO_ID))
            .thenReturn(false);
        when(accrualTPdcSaldoRepository.existsByPdc_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(false);
        
        // Act & Assert
        assertDoesNotThrow(() -> pdcValidator.validateForDelete(CONTO_ID));
        
        // Verifica che tutti i repository siano stati chiamati
        verify(accrualTPdcRepository, times(1)).existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID);
        verify(accrualTPdcRepository, times(1)).existsPrimeNoteAssociate(CONTO_ID);
        verify(accrualTCausaleRepository, times(1)).existsByPdc_Id(CONTO_ID);
        verify(accrualTSoggettoRepository, times(1)).existsByPdc_Id(CONTO_ID);
        verify(accrualTPdcSaldoRepository, times(1)).existsByPdc_IdAndDataCancellazioneIsNull(CONTO_ID);
    }
    
    @Test
    @DisplayName("TC-FM06-007: Eliminazione con più legami lancia al primo trovato")
    void testValidateForDelete_MultipleLegami_ThrowsAtFirst() {
        // Arrange - solo il primo legame è necessario (short-circuit)
        when(accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID))
            .thenReturn(true);
        
        // Act & Assert - dovrebbe lanciare al primo controllo (figli)
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pdcValidator.validateForDelete(CONTO_ID)
        );
        
        assertTrue(exception.getMessage().contains("figli associati"));
        // Solo il primo repository dovrebbe essere chiamato
        verify(accrualTPdcRepository, times(1)).existsByPdcPadre_IdAndDataCancellazioneIsNull(CONTO_ID);
        verify(accrualTPdcRepository, never()).existsPrimeNoteAssociate(anyLong());
    }
}
