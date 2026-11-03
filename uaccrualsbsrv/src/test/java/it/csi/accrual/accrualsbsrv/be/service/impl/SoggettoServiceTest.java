/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoStato;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoTipo;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualTSoggetto;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.SoggettoMapper;
import it.csi.accrual.accrualsbsrv.be.service.SoggettoService;
import it.csi.accrual.accrualsbsrv.dto.Soggetto;
import it.csi.accrual.accrualsbsrv.util.FilterSpecificationBuilder;

/**
 * Test unitari per SoggettoServiceImpl.
 * La logica di business viene testata in isolamento, mockando le dipendenze (es. il repository).
 */
@ExtendWith(MockitoExtension.class)
class SoggettoServiceTest {
/*
    @Mock
    private AccrualTSoggettoRepository soggettoRepository;
    @Mock
    private FilterSpecificationBuilder<AccrualTSoggetto> filterBuilder;
    @Mock
    private SoggettoMapper soggettoMapper;

    @InjectMocks
    private SoggettoService soggettoService;

    @Test
    void whenGetSoggettoById_andSoggettoExists_thenReturnsSoggetto() {
        // 1. Setup: Definisco il comportamento del repository mock
        AccrualTSoggetto soggetto = new AccrualTSoggetto();
        soggetto.setId(1L);
        soggetto.setCodice("SOGG001");
        soggetto.setCodiceUnivoco("RSSMRA80A01H501Z");
        soggetto.setDescrizione("Mario Rossi");

        Soggetto expectedDto = new Soggetto();
        expectedDto.setId(1L);
        expectedDto.setCodice("SOGG001");
        expectedDto.setCodiceUnivoco("RSSMRA80A01H501Z");
        expectedDto.setDescrizione("Mario Rossi");

        when(soggettoRepository.findById(1L)).thenReturn(Optional.of(soggetto));
        when(soggettoMapper.toDto(soggetto)).thenReturn(expectedDto); // Mock del mapper

        // 2. Act: Chiamo il metodo del service da testare
        Soggetto resultDto = soggettoService.getSoggettoById(1L);

        // 3. Assert: Verifico che il DTO restituito sia corretto
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getId()).isEqualTo(1L);
        assertThat(resultDto.getCodice()).isEqualTo("SOGG001");
        assertThat(resultDto.getCodiceUnivoco()).isEqualTo("RSSMRA80A01H501Z");
        assertThat(resultDto.getDescrizione()).isEqualTo("Mario Rossi");

        // Opzionale: verifico che il metodo del mock sia stato chiamato
        verify(soggettoRepository).findById(1L);
        verify(soggettoMapper).toDto(soggetto); // Verifica anche la chiamata al mapper
    }

    @Test
    void whenGetSoggettoByCodice_andSoggettoExists_thenReturnsSoggetto() {
        // 1. Setup: Definisco il comportamento del repository mock
        AccrualTSoggetto soggetto = new AccrualTSoggetto();
        soggetto.setId(1L);
        soggetto.setCodice("SOGG001");
        soggetto.setCodiceUnivoco("RSSMRA80A01H501Z");
        soggetto.setDescrizione("Mario Rossi");

        AccrualDSoggettoStato stato = new AccrualDSoggettoStato();
        stato.setCodice("ATTIVO");
        soggetto.setStato(stato);

        AccrualDSoggettoTipo tipo = new AccrualDSoggettoTipo();
        tipo.setCodice("PF");
        soggetto.setTipo(tipo);

        AccrualTEnte ente = new AccrualTEnte();
        // ente.setDenominazione("CSI Piemonte");
        soggetto.setEnte(ente);

        Soggetto expectedDto = new Soggetto();
        expectedDto.setId(1L);
        expectedDto.setCodice("SOGG001");
        expectedDto.setCodiceUnivoco("RSSMRA80A01H501Z");
        expectedDto.setDescrizione("Mario Rossi");

        when(soggettoRepository.findByCodice("SOGG001")).thenReturn(Optional.of(soggetto));
        when(soggettoMapper.toDto(soggetto)).thenReturn(expectedDto);

        // 2. Act: Chiamo il metodo del service da testare
        Soggetto resultDto = soggettoService.getSoggettoByCodice("SOGG001");

        // 3. Assert: Verifico che il DTO restituito sia corretto
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getId()).isEqualTo(1L);
        assertThat(resultDto.getCodice()).isEqualTo("SOGG001");
        assertThat(resultDto.getCodiceUnivoco()).isEqualTo("RSSMRA80A01H501Z");
        assertThat(resultDto.getDescrizione()).isEqualTo("Mario Rossi");

        // Opzionale: verifico che il metodo del mock sia stato chiamato
        verify(soggettoRepository).findByCodice("SOGG001");
        verify(soggettoMapper).toDto(soggetto);
    }

    @Test
    void whenGetSoggettoByCodice_andSoggettoNotFound_thenThrowsNotFoundException() {
        // 1. Setup: Simulo che il repository non trovi l'entità
        String codiceNonEsistente = "NON_ESISTENTE";
        when(soggettoRepository.findByCodice(codiceNonEsistente)).thenReturn(Optional.empty());

        // 2. Act & Assert: Verifico che venga lanciata l'eccezione attesa
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            soggettoService.getSoggettoByCodice(codiceNonEsistente);
        });
        
        assertThat(exception.getMessage()).isEqualTo("Soggetto con codice " + codiceNonEsistente + " non trovato");
    }

    @Test
    void whenGetSoggettoById_andSoggettoNotFound_thenThrowsNotFoundException() {
        // 1. Setup: Simulo che il repository non trovi l'entità
        long idNonEsistente = 999L;
        when(soggettoRepository.findById(idNonEsistente)).thenReturn(Optional.empty());

        // 2. Act & Assert: Verifico che venga lanciata l'eccezione attesa
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            soggettoService.getSoggettoById(idNonEsistente);
        });
        
        assertThat(exception.getMessage()).isEqualTo("Soggetto con id " + idNonEsistente + " non trovato");
    }*/
}
