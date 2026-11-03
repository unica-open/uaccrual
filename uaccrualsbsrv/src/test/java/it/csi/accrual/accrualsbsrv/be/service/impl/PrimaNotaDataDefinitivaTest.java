/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

// AI generated 2026-09-18
// reviewed by tarantinod

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitari per il calcolo della data di registrazione definitiva (issue 1132).
 *
 * Difetto: inserendo o validando una prima nota con data di registrazione provvisoria futura,
 * la data definitiva veniva scritta con la data di sistema e risultava antecedente alla provvisoria.
 */
@DisplayName("PrimaNotaServiceImpl - Data registrazione definitiva (issue 1132)")
class PrimaNotaDataDefinitivaTest {

    @Test
    @DisplayName("Provvisoria futura: la definitiva e' la mezzanotte del giorno della provvisoria")
    void provvisoriaFuturaRestituisceMezzanotteDellaProvvisoria() {
        LocalDateTime provvisoria = LocalDate.now().plusDays(10).atTime(14, 30);

        LocalDateTime definitiva = PrimaNotaServiceImpl.calcolaDataRegistrazioneDefinitiva(provvisoria);

        assertEquals(provvisoria.toLocalDate().atStartOfDay(), definitiva);
    }

    @Test
    @DisplayName("Provvisoria di oggi: la definitiva e' la data di sistema")
    void provvisoriaOdiernaRestituisceDataDiSistema() {
        LocalDateTime provvisoria = LocalDate.now().atTime(8, 0);

        LocalDateTime definitiva = PrimaNotaServiceImpl.calcolaDataRegistrazioneDefinitiva(provvisoria);

        assertEquals(LocalDate.now(), definitiva.toLocalDate());
    }

    @Test
    @DisplayName("Provvisoria passata: la definitiva e' la data di sistema")
    void provvisoriaPassataRestituisceDataDiSistema() {
        LocalDateTime provvisoria = LocalDate.now().minusDays(5).atStartOfDay();

        LocalDateTime definitiva = PrimaNotaServiceImpl.calcolaDataRegistrazioneDefinitiva(provvisoria);

        assertEquals(LocalDate.now(), definitiva.toLocalDate());
    }

    @Test
    @DisplayName("Provvisoria nulla: la definitiva e' la data di sistema")
    void provvisoriaNullaRestituisceDataDiSistema() {
        LocalDateTime definitiva = PrimaNotaServiceImpl.calcolaDataRegistrazioneDefinitiva(null);

        assertNotNull(definitiva);
        assertEquals(LocalDate.now(), definitiva.toLocalDate());
    }
}
