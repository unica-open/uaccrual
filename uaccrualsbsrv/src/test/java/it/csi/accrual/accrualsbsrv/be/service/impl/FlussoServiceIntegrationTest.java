/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualsbsrv.dto.ElaboraFlussoResponseDTO;
import it.csi.accrual.accrualsbsrv.dto.FlussoElaboraResponse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTFileRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFileTipoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFileStatoRepository;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FlussoServiceIntegrationTest {

    @Mock
    private AccrualTFileRepository accrualTFileRepository;

    @Mock
    private AccrualTEnteRepository accrualTEnteRepository;

    @Mock
    private AccrualDFileTipoRepository accrualDFileTipoRepository;

    @Mock
    private AccrualDFileStatoRepository accrualDFileStatoRepository;

    @Mock
    private CsvReader csvReader;

    @Mock
    private LogService logService;

    @Mock
    private InterazioneUtenteService interazioneUtenteService;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private FlussoServiceImpl flussoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testElaboraFlusso_Generic_Success() throws Exception {
        // Given
        String flussoId = "1";
        String enteCodiceMock = "ENTE001";

        AccrualTFile mockFile = new AccrualTFile();
        AccrualTEnte mockEnte = new AccrualTEnte();
        AccrualDFileTipo mockTipoFile = new AccrualDFileTipo();

        // Impostiamo un tipo di file generico
        mockTipoFile.setCodice("TIPO_GENERICO");

        mockFile.setFileTipo(mockTipoFile);
        mockFile.setEnte(mockEnte);

        // Mock del SecurityContextHelper per restituire il codice ente
        when(securityContextHelper.getEnteCodiceUnivoco()).thenReturn(enteCodiceMock);

        // Mock delle dipendenze
        when(accrualTEnteRepository.findByCodiceUnivoco(enteCodiceMock)).thenReturn(java.util.Optional.of(mockEnte));
        when(accrualTFileRepository.findById(Long.parseLong(flussoId))).thenReturn(java.util.Optional.of(mockFile));

        // When
        ElaboraFlussoResponseDTO responseDTO = flussoService.elaboraFlusso(flussoId);

        // Then
        assertNotNull(responseDTO);
        assertNotNull(responseDTO.getFlussoElaboraResponse());
        assertTrue(responseDTO.getFlussoElaboraResponse().isSuccess());
    }
}
