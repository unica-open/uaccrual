/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

// AI generated 2026-09-14
// reviewed by tarantinod

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEntitaContabile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTParametro;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.StatoPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoFase;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPrimaNotaStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRPrimaNotaStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEntitaContabileRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTParametroRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPrimaNotaRepository;
import it.csi.accrual.accrualcommonsrv.be.util.PrimaNotaNumService;
import it.csi.accrual.accrualcommonsrv.security.PermissionHelper;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;

/**
 * Validazione di una prima nota riaperta con parametro ente
 * VERIFICA_RINUMERAZIONE_PRIME_NOTE = SI: numero e data della registrazione definitiva
 * devono seguire la stessa scelta dell'operatore su WEB_CON_033.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PrimaNotaServiceImplValidaRinumerazioneTest {

    private static final String ENTE_CODICE = "ENTE_TEST";
    private static final Integer ANNO = 2026;
    private static final Integer NUMERO = 42;
    private static final Integer PROGRESSIVO_ESISTENTE = 7;
    private static final int PROGRESSIVO_RIASSEGNATO = 99;
    private static final LocalDateTime DATA_REG_DEFINITIVA_ORIGINARIA = LocalDateTime.of(2026, 3, 10, 9, 30);

    @Mock
    private AccrualTPrimaNotaRepository primaNotaRepository;

    @Mock
    private AccrualDPrimaNotaStatoRepository statoRepository;

    @Mock
    private AccrualRPrimaNotaStatoRepository accrualRPrimaNotaStatoRepository;

    @Mock
    private AccrualTEntitaContabileRepository entitaContabileRepository;

    @Mock
    private AccrualTParametroRepository parametroRepository;

    @Mock
    private PrimaNotaNumService primaNotaNumService;

    @Mock
    private PermissionHelper permissionHelper;

    @InjectMocks
    private PrimaNotaServiceImpl primaNotaService;

    private MockedStatic<SecurityContextHelper> securityContextHelperMock;
    private AccrualTPrimaNota primaNota;

    @BeforeEach
    void setUp() {
        securityContextHelperMock = mockStatic(SecurityContextHelper.class);
        securityContextHelperMock.when(SecurityContextHelper::getEnteCodiceUnivocoStatic).thenReturn(ENTE_CODICE);

        AccrualTEnte ente = new AccrualTEnte();
        ente.setId(1L);
        ente.setCodiceUnivoco(ENTE_CODICE);

        AccrualDPrimaNotaStato statoProvvisoria = new AccrualDPrimaNotaStato();
        statoProvvisoria.setCodice(StatoPrimaNota.PROVVISORIA.getCode());

        AccrualDPrimaNotaStato statoDefinitiva = new AccrualDPrimaNotaStato();
        statoDefinitiva.setCodice(StatoPrimaNota.DEFINITIVA.getCode());

        // Prima nota riaperta: torna PROVVISORIA conservando numero e data della registrazione definitiva
        primaNota = new AccrualTPrimaNota();
        primaNota.setId(100L);
        primaNota.setAnno(ANNO);
        primaNota.setNumero(NUMERO);
        primaNota.setEnte(ente);
        primaNota.setProgressivoGiornale(PROGRESSIVO_ESISTENTE);
        primaNota.setDataRegistrazioneGiornale(DATA_REG_DEFINITIVA_ORIGINARIA);

        AccrualRPrimaNotaStato relazioneProvvisoria = new AccrualRPrimaNotaStato();
        relazioneProvvisoria.setPrimaNota(primaNota);
        relazioneProvvisoria.setPrimaNotaStato(statoProvvisoria);
        relazioneProvvisoria.setEnte(ente);
        relazioneProvvisoria.setInizioValidita(LocalDateTime.of(2026, 3, 12, 8, 0));
        Set<AccrualRPrimaNotaStato> stati = new HashSet<>();
        stati.add(relazioneProvvisoria);
        primaNota.setStati(stati);

        AccrualTEntitaContabile entitaContabile = new AccrualTEntitaContabile();
        entitaContabile.setEnte(ente);
        entitaContabile.setAnno(String.valueOf(ANNO));
        entitaContabile.setFase(TipoFase.GESTIONE);

        AccrualTParametro parametro = new AccrualTParametro();
        parametro.setParametroNome("VERIFICA_RINUMERAZIONE_PRIME_NOTE");
        parametro.setParametroValore("SI");
        parametro.setParametroAbilitato(Boolean.TRUE);
        parametro.setEnte(ente);

        when(primaNotaRepository.findForConsultazione(ANNO, NUMERO, ENTE_CODICE))
            .thenReturn(Optional.of(primaNota));
        when(primaNotaRepository.save(any(AccrualTPrimaNota.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionHelper.getUserAzioniCodici())
            .thenReturn(Collections.singleton("OP-SPE-validaPrimaNota"));
        when(entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(
                eq(ENTE_CODICE), eq(String.valueOf(ANNO)), any(LocalDateTime.class)))
            .thenReturn(Optional.of(entitaContabile));
        when(parametroRepository.findByParametroNomeAndEnteAndParametroAbilitatoTrue(
                "VERIFICA_RINUMERAZIONE_PRIME_NOTE", ente))
            .thenReturn(Optional.of(parametro));
        when(statoRepository.findByCodice(StatoPrimaNota.DEFINITIVA.getCode()))
            .thenReturn(Optional.of(statoDefinitiva));
        when(accrualRPrimaNotaStatoRepository.findByPrimaNotaAndFineValiditaIsNull(primaNota))
            .thenReturn(Collections.emptyList());
        when(primaNotaNumService.nextNumeroDefinitivo(eq(ente), anyInt()))
            .thenReturn(PROGRESSIVO_RIASSEGNATO);
    }

    @AfterEach
    void tearDown() {
        securityContextHelperMock.close();
    }

    @Test
    @DisplayName("WEB_CON_033 = Si: numero e data della registrazione definitiva restano quelli esistenti")
    void mantieneNumeroEDataQuandoOperatoreRispondeSi() throws Exception {
        primaNotaService.validaPrimaNota(ANNO, NUMERO, "tester", Boolean.TRUE, null);

        assertEquals(PROGRESSIVO_ESISTENTE, primaNota.getProgressivoGiornale(),
            "il numero definitivo esistente deve essere mantenuto");
        assertEquals(DATA_REG_DEFINITIVA_ORIGINARIA, primaNota.getDataRegistrazioneGiornale(),
            "la data di registrazione definitiva esistente deve essere mantenuta");
    }

    @Test
    @DisplayName("WEB_CON_033 = No: numero e data della registrazione definitiva vengono riassegnati")
    void riassegnaNumeroEDataQuandoOperatoreRispondeNo() throws Exception {
        primaNotaService.validaPrimaNota(ANNO, NUMERO, "tester", Boolean.FALSE, null);

        assertEquals(PROGRESSIVO_RIASSEGNATO, primaNota.getProgressivoGiornale().intValue(),
            "il numero definitivo deve essere riassegnato");
        assertNotEquals(DATA_REG_DEFINITIVA_ORIGINARIA, primaNota.getDataRegistrazioneGiornale(),
            "la data di registrazione definitiva deve essere aggiornata");
    }

    @Test
    @DisplayName("Prima chiamata senza scelta: WEB_CON_033 dichiara che il No riassegna anche la data")
    void chiedeConfermaDichiarandoCheIlNoRiassegnaAncheLaData() {
        ConfirmationRequiredException conferma = assertThrows(ConfirmationRequiredException.class,
            () -> primaNotaService.validaPrimaNota(ANNO, NUMERO, "tester", null, null));

        assertEquals("WEB_CON_033", conferma.getConfirmationCode(),
            "il codice del messaggio di conferma");
        assertEquals("La prima nota ha già un numero definitivo (" + PROGRESSIVO_ESISTENTE + "). "
                + "Si desidera mantenere il numero esistente? "
                + "(Si = mantiene, No = assegna nuovo numero e data registrazione definitiva)",
            conferma.getConfirmationMessage(),
            "il testo deve dire che il No riassegna numero e data");
    }
}
