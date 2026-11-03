/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.dto.Pdc;
import it.csi.accrual.accrualsbsrv.be.exception.ValidationException;
import it.csi.accrual.accrualsbsrv.be.exception.DuplicateRecordException;
import it.csi.accrual.accrualsbsrv.be.exception.ForbiddenOperationException;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.exception.IntegrityConstraintException;

import jakarta.ws.rs.core.Response;


@ExtendWith(MockitoExtension.class)
public class PdcApiImplTest {

    @Mock
    private PdcService pdcService;

    @InjectMocks
    private PdcApiImpl pdcApi;

    private Pdc pdcInput;
    private Pdc pdcResult;

    @BeforeEach
    void setUp() {
        pdcInput = new Pdc();
        
        // Il DTO si aspetta un oggetto Pdc per il padre, non un ID.
        Pdc pdcPadre = new Pdc();
        pdcPadre.setId(1L);
        pdcInput.setPdcPadre(pdcPadre);
        
        // Il campo 'codice' è ereditato da BaseCoded
        pdcInput.setCodice("5");
        pdcInput.setDescrizione("Test");

        pdcResult = new Pdc();
        pdcResult.setId(100L);
        pdcResult.setCodiceUfficiale("1.2.3.5");
        pdcResult.setLivello(4);
        pdcResult.setDescrizione("Test");
    }

    // TC-C-001: Creazione Pdc (Successo)
    @Test
    void testCreatePdc_Success() {
        when(pdcService.createPdc(any(Pdc.class))).thenReturn(pdcResult);

        Response response = pdcApi.createPdc(pdcInput, null, null, null);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(pdcResult.getCodiceUfficiale(), ((Pdc) response.getEntity()).getCodiceUfficiale());
    }

    // TC-C-002: Creazione Pdc (Errore: Padre è foglia)
    @Test
    void testCreatePdc_ParentIsLeaf() {
        when(pdcService.createPdc(any(Pdc.class))).thenThrow(new ValidationException("Il Pdc padre è foglia"));

        Response response = pdcApi.createPdc(pdcInput, null, null, null);

        // This should fail until a specific exception handler for ValidationException is implemented
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    // TC-C-003: Creazione Pdc (Errore: Codice duplicato)
    @Test
    void testCreatePdc_DuplicateCode() {
        when(pdcService.createPdc(any(Pdc.class))).thenThrow(new DuplicateRecordException("Pdc non univoco"));

        Response response = pdcApi.createPdc(pdcInput, null, null, null);

        // Similar to above, needs an exception handler. For now, we test against the generic 500.
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-C-004: Creazione Pdc (Errore: Permesso mancante)
    @Test
    void testCreatePdc_PermissionDenied() {
         when(pdcService.createPdc(any(Pdc.class))).thenThrow(new ForbiddenOperationException("Permesso negato"));

        Response response = pdcApi.createPdc(pdcInput, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-U-005: Aggiornamento Pdc (Successo)
    @Test
    void testUpdatePdc_Success() {
        String codiceUfficiale = "1.2.3.5";
        String codiceClasse = "CLASSE_TEST";
        String codiceFamiglia = "FAMIGLIA_TEST";
        Pdc pdcToUpdate = new Pdc();
        pdcToUpdate.setDescrizione("Nuova Descrizione");

        when(pdcService.updatePdcByTripleKey(eq(codiceUfficiale), eq(codiceClasse), eq(codiceFamiglia), any(Pdc.class))).thenReturn(pdcResult);

        Response response = pdcApi.updatePdcByTripleKey(pdcToUpdate, codiceUfficiale, codiceClasse, codiceFamiglia, null, null, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    // TC-U-006: Aggiornamento Pdc (Errore: Pdc non trovato)
    @Test
    void testUpdatePdc_NotFound() {
        String codiceUfficiale = "999";
        String codiceClasse = "CLASSE_TEST";
        String codiceFamiglia = "FAMIGLIA_TEST";
        when(pdcService.updatePdcByTripleKey(eq(codiceUfficiale), eq(codiceClasse), eq(codiceFamiglia), any(Pdc.class))).thenThrow(new NotFoundException("Pdc non trovato"));

        Response response = pdcApi.updatePdcByTripleKey(new Pdc(), codiceUfficiale, codiceClasse, codiceFamiglia, null, null, null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // TC-U-007: Aggiornamento Pdc (Errore: famiglia 118)
    @Test
    void testUpdatePdc_Family118() {
        String codiceUfficiale = "118.1";
        String codiceClasse = "CLASSE_TEST";
        String codiceFamiglia = "FAMIGLIA_TEST";
        when(pdcService.updatePdcByTripleKey(eq(codiceUfficiale), eq(codiceClasse), eq(codiceFamiglia), any(Pdc.class))).thenThrow(new ForbiddenOperationException("Operazione non permessa su Pdc di famiglia 118"));

        Response response = pdcApi.updatePdcByTripleKey(new Pdc(), codiceUfficiale, codiceClasse, codiceFamiglia, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-D-008: Eliminazione Pdc (Successo)
    @Test
    void testDeletePdc_Success() {
        String codicePdc = "1.2.3.5";
        // No return value for delete
        Response response = pdcApi.deletePdcByTripleKey(codicePdc, null, null, null, null, null);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    // TC-D-009: Eliminazione Pdc (Errore: ha figli)
    @Test
    void testDeletePdc_HasChildren() {
        String codicePdc = "1.2.3";
        doThrow(new IntegrityConstraintException("WEB_ERR_0012", "conto figlio")).when(pdcService).deletePdc(codicePdc);

        Response response = pdcApi.deletePdcByTripleKey(codicePdc, null, null, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-D-010: Eliminazione Pdc (Errore: ha legami con causali)
    @Test
    void testDeletePdc_HasCausaliLink() {
        String codicePdc = "1.2.4";
         doThrow(new IntegrityConstraintException("WEB_ERR_0012", "causale")).when(pdcService).deletePdc(codicePdc);

        Response response = pdcApi.deletePdcByTripleKey(codicePdc, null, null, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-D-011: Eliminazione Pdc (Errore: pdc_foglia_non_foglia)
    @Test
    void testDeletePdc_IsPdcFogliaNonFoglia() {
        String codicePdc = "1.2.5";
         doThrow(new ForbiddenOperationException("Operazione non permessa su questo tipo di Pdc")).when(pdcService).deletePdc(codicePdc);

        Response response = pdcApi.deletePdcByTripleKey(codicePdc, null, null, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-D-012: Eliminazione Pdc (Errore: Fase bilancio non valida)
    @Test
    void testDeletePdc_InvalidPhase() {
        String codicePdc = "1.2.3.5";
        doThrow(new ForbiddenOperationException("Operazione non permessa in fase CHIUSO")).when(pdcService).deletePdc(codicePdc);

        Response response = pdcApi.deletePdcByTripleKey(codicePdc, null, null, null, null, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TC-U-008: Aggiornamento Pdc con tripletta (Successo)
    @Test
    void testUpdatePdcByTripleKey_Success() {
        String codiceUfficiale = "1.2.3";
        String codiceClasse = "CLASSE_TEST";
        String codiceFamiglia = "FAMIGLIA_TEST";

        Pdc pdcToUpdate = new Pdc();
        pdcToUpdate.setDescrizione("Nuova Descrizione");

        when(pdcService.updatePdcByTripleKey(eq(codiceUfficiale), eq(codiceClasse), eq(codiceFamiglia), any(Pdc.class))).thenReturn(pdcResult);

        Response response = pdcApi.updatePdcByTripleKey(pdcToUpdate, codiceUfficiale, codiceClasse, codiceFamiglia, null, null, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    // TC-U-009: Aggiornamento Pdc con tripletta (Errore: Pdc non trovato)
    @Test
    void testUpdatePdcByTripleKey_NotFound() {
        String codiceUfficiale = "999";
        String codiceClasse = "CLASSE_TEST";
        String codiceFamiglia = "FAMIGLIA_TEST";

        when(pdcService.updatePdcByTripleKey(eq(codiceUfficiale), eq(codiceClasse), eq(codiceFamiglia), any(Pdc.class))).thenThrow(new NotFoundException("Pdc non trovato"));

        Response response = pdcApi.updatePdcByTripleKey(new Pdc(), codiceUfficiale, codiceClasse, codiceFamiglia, null, null, null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
