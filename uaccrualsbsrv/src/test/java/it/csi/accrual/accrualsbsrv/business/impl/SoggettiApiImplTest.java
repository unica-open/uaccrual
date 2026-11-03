/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.business.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.Soggetto;

/**
 * Test di integrazione per l'API dei Soggetti (SoggettiApiImpl).
 * Avvia l'intera applicazione e testa l'endpoint con chiamate HTTP reali.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("soggetto-test")
class SoggettiApiImplTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void whenGetSoggettoByCodice_withValidCodice_returns200AndSoggettoDto() {
        // 1. Act: Eseguo una vera chiamata HTTP all'endpoint
        // Il codice 'SOGG001' è inserito dallo script soggetto.sql
        ResponseEntity<Soggetto> response = restTemplate.getForEntity("/api/v1/soggetti/{codice}", Soggetto.class, "SOGG001");

        // 2. Assert: Verifico la risposta HTTP e il corpo JSON
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(100L);
        assertThat(response.getBody().getCodice()).isEqualTo("SOGG001");
        assertThat(response.getBody().getCodiceFiscale()).isEqualTo("MRGFNC85M01H501X");
        assertThat(response.getBody().getDescrizione()).isEqualTo("Franco Verdi");
    }

    @Test
    void whenGetSoggettoByCodice_withInvalidCodice_returns404() {
        // 1. Act: Chiamo l'endpoint con un codice che non esiste
        ResponseEntity<Error> response = restTemplate.getForEntity("/api/v1/soggetti/{codice}", Error.class, "INVALID_CODICE");

        // 2. Assert: Verifico che la risposta sia un 404 Not Found e che il corpo sia un DTO Error corretto
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("404");
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().getTitle()).isEqualTo("Risorsa non trovata");
        assertThat(response.getBody().getDetail().toString()).contains("Soggetto con codice INVALID_CODICE non trovato");
    }
}
