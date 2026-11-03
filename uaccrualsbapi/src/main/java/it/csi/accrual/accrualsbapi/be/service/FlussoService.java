/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service;

import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaStato;
import it.csi.accrual.accrualsbapi.dto.*;
import java.io.InputStream;
import java.io.File;
import java.util.List;
import java.util.Date;

public interface FlussoService {
    String uploadFlusso(String codice, String tipoFileCodice, String note, InputStream fileContent, String fileName, long fileSize, String fileContentType) throws NotFoundException, IllegalArgumentException;
    List<Fonte> getFonti() throws NotFoundException;
    PagedFlussoSearchResult searchFlussi(String codiceFile, String fonte, String tipoFile, Date dataCaricamento, Integer limit, Integer offset);
    ElaboraFlussoResponseDTO elaboraFlusso(String id) throws NotFoundException;
    File downloadFlusso(String id) throws NotFoundException;
    FlussoDetail getFlussoById(String id) throws NotFoundException;
    void deleteFlussoById(String id) throws NotFoundException;
    ElaboraFlussoResponseDTO resumeElaborazioneFlusso(String resumeToken, boolean userConfirmed) throws NotFoundException, IllegalArgumentException;
    ElaboraFlussoResponseDTO executeGenericProcessing(AccrualTFile file, AccrualTEnte ente);
    AccrualDPrimaNotaStato getStatoPrimaNotaByEvento(AccrualTEvento evento);
    File exportResults(String id) throws NotFoundException;
}