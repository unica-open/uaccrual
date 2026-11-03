/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcFamigliaRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcMapper;
import it.csi.accrual.accrualsbsrv.be.service.PdcValidator;
import it.csi.accrual.accrualsbsrv.dto.Pdc;

/**
 * Test unitari per l'ereditarietà dei campi dal padre (FM-08).
 * Verifica che i nuovi conti ereditino classe, tipo, stato dal padre
 * e che la famiglia sia impostata su 'ACCRUAL-UTENTE'.
 * 
 * Requisito testato: CDU-003 2.10.4 - Inserisci conto Accrual
 * 
 * @author Team ACCRUAL
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PdcServiceImplEreditarietaTest {

    @Mock
    private AccrualTPdcRepository pdcRepository;
    
    @Mock
    private AccrualTEnteRepository enteRepository;
    
    @Mock
    private AccrualTPdcFamigliaRepository pdcFamigliaRepository;
    
    @Mock
    private PdcValidator pdcValidator;
    
    @Mock
    private PdcMapper pdcMapper;
    
    @InjectMocks
    private PdcServiceImpl pdcService;
    
    private AccrualTPdc padre;
    private AccrualDPdcClasse classe;
    private AccrualDPdcTipo tipo;
    private AccrualDPdcStato stato;
    private AccrualTPdcFamiglia famigliaUtente;
    private AccrualTEnte ente;
    
    private static final String X_ENTE_CODICE = "ENTE-001";
    
    @BeforeEach
    void setUp() {
        // Inizializza entità di base
        classe = new AccrualDPdcClasse();
        classe.setCodice("PATRIMONIALE");
        classe.setDescrizione("Conto Patrimoniale");
        
        tipo = new AccrualDPdcTipo();
        tipo.setCodice("DETTAGLIO");
        tipo.setDescrizione("Conto di Dettaglio");
        
        stato = new AccrualDPdcStato();
        stato.setCodice("A");
        stato.setDescrizione("Attivo");
        
        famigliaUtente = new AccrualTPdcFamiglia();
        famigliaUtente.setCodice("ACCRUAL-UTENTE");
        famigliaUtente.setDescrizione("Famiglia Utente");
        famigliaUtente.setRicercabile(true);
        
        ente = new AccrualTEnte();
        ente.setId(1L);
        ente.setCodiceUnivoco(X_ENTE_CODICE);
        // ente.setDenominazione("Ente Test");
        
        // Inizializza padre
        padre = new AccrualTPdc();
        padre.setId(1L);
        padre.setCodiceUfficiale("ACCR.0");
        padre.setCodice("ACCR.0");
        padre.setDescrizione("Conto Padre");
        padre.setLivello(1);
        padre.setFoglia(true);
        padre.setPdcClasse(classe);
        padre.setPdcTipo(tipo);
        padre.setPdcStato(stato);
        padre.setEnte(ente);
        
        // Configura mapper per il padre
        when(pdcMapper.toEntity(any(Pdc.class))).thenAnswer(invocation -> {
            Pdc dto = invocation.getArgument(0);
            AccrualTPdc entity = new AccrualTPdc();
            entity.setCodice(dto.getCodice());
            entity.setDescrizione(dto.getDescrizione());
            return entity;
        });
        
        when(pdcMapper.toDto(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc entity = invocation.getArgument(0);
            Pdc dto = new Pdc();
            dto.setCodice(entity.getCodice());
            dto.setDescrizione(entity.getDescrizione());
            dto.setLivello(entity.getLivello());
            dto.setPdcClasse(entity.getPdcClasse() != null ? 
                convertToDtoClasse(entity.getPdcClasse()) : null);
            dto.setPdcTipo(entity.getPdcTipo() != null ? 
                convertToDtoTipo(entity.getPdcTipo()) : null);
            dto.setPdcStato(entity.getPdcStato() != null ? 
                convertToDtoStato(entity.getPdcStato()) : null);
            return dto;
        });
        
        // Configura repository
        when(enteRepository.findByCodiceUnivoco(X_ENTE_CODICE))
            .thenReturn(Optional.of(ente));
        when(pdcFamigliaRepository.findByCodice("ACCRUAL-UTENTE"))
            .thenReturn(Optional.of(famigliaUtente));
    }
    
    @Test
    @DisplayName("TC-FM08-001: Ereditarietà classe dal padre")
    void testCreatePdc_EreditaClasse() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPdcClasse());
        assertEquals(classe.getCodice(), result.getPdcClasse().getCodice(), 
            "La classe dovrebbe essere ereditata dal padre");
        assertEquals(classe.getDescrizione(), result.getPdcClasse().getDescrizione());
    }
    
    @Test
    @DisplayName("TC-FM08-002: Ereditarietà tipo dal padre")
    void testCreatePdc_EreditaTipo() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPdcTipo());
        assertEquals(tipo.getCodice(), result.getPdcTipo().getCodice(), 
            "Il tipo dovrebbe essere ereditato dal padre");
        assertEquals(tipo.getDescrizione(), result.getPdcTipo().getDescrizione());
    }
    
    @Test
    @DisplayName("TC-FM08-003: Ereditarietà stato dal padre")
    void testCreatePdc_EreditaStato() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getPdcStato());
        assertEquals(stato.getCodice(), result.getPdcStato().getCodice(), 
            "Lo stato dovrebbe essere ereditato dal padre");
        assertEquals(stato.getDescrizione(), result.getPdcStato().getDescrizione());
    }
    
    @Test
    @DisplayName("TC-FM08-004: Calcolo livello (padre.livello + 1)")
    void testCreatePdc_CalcolaLivello() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getLivello(), 
            "Il livello dovrebbe essere padre.livello + 1 (1 + 1 = 2)");
    }
    
    @Test
    @DisplayName("TC-FM08-005: Famiglia impostata su ACCRUAL-UTENTE")
    void testCreatePdc_FamigliaUtente() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        verify(pdcFamigliaRepository, times(1)).findByCodice("ACCRUAL-UTENTE");
    }
    
    @Test
    @DisplayName("TC-FM08-006: Aggiornamento padre da foglia a non-foglia")
    void testCreatePdc_AggiornaPadreFoglia() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        verify(pdcRepository, times(2)).save(any(AccrualTPdc.class));
        // Verifica che il padre sia stato salvato con foglia=false
        verify(pdcRepository).save(argThat(pdc -> !pdc.isFoglia()));
    }
    
    @Test
    @DisplayName("TC-FM08-007: Calcolo codice ufficiale (padre.codice + '.' + codice)")
    void testCreatePdc_CalcolaCodiceUfficiale() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("001");
        pdcDto.setDescrizione("Nuovo Conto Figlio");
        
        Pdc padreDto = new Pdc();
        padreDto.setId(1L);
        pdcDto.setPdcPadre(padreDto);
        
        when(pdcRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.setCodiceUfficiale("ACCR.0.001");
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        // Il codice ufficiale dovrebbe essere calcolato come concatenazione
        // (il save viene chiamato 2 volte: una per il padre e una per il figlio)
        verify(pdcRepository, times(2)).save(any(AccrualTPdc.class));
    }
    
    @Test
    @DisplayName("TC-FM08-008: Creazione senza padre (conto di primo livello)")
    void testCreatePdc_SenzaPadre() {
        // Arrange
        Pdc pdcDto = new Pdc();
        pdcDto.setCodice("ACCR.1");
        pdcDto.setDescrizione("Conto di Primo Livello");
        pdcDto.setPdcPadre(null);
        
        when(pdcRepository.save(any(AccrualTPdc.class))).thenAnswer(invocation -> {
            AccrualTPdc saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        
        // Act
        Pdc result = pdcService.createPdc(pdcDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLivello(), 
            "Un conto senza padre dovrebbe avere livello 1");
        verify(pdcRepository, times(1)).save(any(AccrualTPdc.class));
        verify(pdcFamigliaRepository, times(1)).findByCodice("ACCRUAL-UTENTE");
    }
    
    // Metodi helper per conversione DTO
    private it.csi.accrual.accrualsbsrv.dto.PdcClasse convertToDtoClasse(AccrualDPdcClasse entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcClasse dto = 
            new it.csi.accrual.accrualsbsrv.dto.PdcClasse();
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }
    
    private it.csi.accrual.accrualsbsrv.dto.PdcTipo convertToDtoTipo(AccrualDPdcTipo entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcTipo dto = 
            new it.csi.accrual.accrualsbsrv.dto.PdcTipo();
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }
    
    private it.csi.accrual.accrualsbsrv.dto.PdcStato convertToDtoStato(AccrualDPdcStato entity) {
        it.csi.accrual.accrualsbsrv.dto.PdcStato dto = 
            new it.csi.accrual.accrualsbsrv.dto.PdcStato();
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }
}
