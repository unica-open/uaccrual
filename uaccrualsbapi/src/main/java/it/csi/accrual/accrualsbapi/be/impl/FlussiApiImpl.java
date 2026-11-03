/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualsbapi.be.FlussiApi;
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualsbapi.be.service.FlussoService;
import it.csi.accrual.accrualsbapi.dto.Error;
import it.csi.accrual.accrualsbapi.dto.FlussoUploadResponse;
import it.csi.accrual.accrualsbapi.dto.PagedFlussoSearchResult;
import it.csi.accrual.accrualsbapi.dto.ElaboraFlussoResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.io.File; // Aggiunto
import java.nio.file.Files; // Aggiunto
import java.io.IOException; // Aggiunto
import it.csi.accrual.accrualsbapi.dto.FlussoDetail;
import it.csi.accrual.accrualsbapi.dto.Fonte;
import it.csi.accrual.accrualsbapi.be.service.impl.AsyncElaboraFlussoService;
import it.csi.accrual.accrualsbapi.dto.ProcessStatusResponse;
import java.util.List;


@Component
public class FlussiApiImpl implements FlussiApi {

    private static final Logger LOG = LoggerFactory.getLogger(FlussiApiImpl.class);

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private AsyncElaboraFlussoService asyncElaboraFlussoService;

    @Autowired
    private it.csi.accrual.accrualsbapi.be.service.impl.LogService logService;

    @Override
    public Response uploadFlusso(
            MultipartFormDataInput input,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            // Estrazione dei campi dal MultipartFormDataInput
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            // Estrazione codice
            List<InputPart> codiceInputParts = uploadForm.get("codice");
            if (codiceInputParts == null || codiceInputParts.isEmpty()) {
                throw new IllegalArgumentException("Campo 'codice' non fornito.");
            }
            String codice = codiceInputParts.get(0).getBodyAsString();

            // Estrazione tipoFile
            List<InputPart> tipoFileInputParts = uploadForm.get("tipoFile");
            if (tipoFileInputParts == null || tipoFileInputParts.isEmpty()) {
                throw new IllegalArgumentException("Campo 'tipoFile' non fornito.");
            }
            String tipoFile = tipoFileInputParts.get(0).getBodyAsString();

            // Estrazione note (opzionale)
            String note = null;
            List<InputPart> noteInputParts = uploadForm.get("note");
            if (noteInputParts != null && !noteInputParts.isEmpty()) {
                note = noteInputParts.get(0).getBodyAsString();
            }

            // Estrazione del file
            List<InputPart> fileInputParts = uploadForm.get("file");
            if (fileInputParts == null || fileInputParts.isEmpty()) {
                throw new IllegalArgumentException("Campo 'file' non fornito.");
            }
            InputPart fileInputPart = fileInputParts.get(0);
            InputStream fileContent = fileInputPart.getBody(InputStream.class, null);

            // Estrazione nome file e content type
            String fileName = "unknown";
            String contentDisposition = fileInputPart.getHeaders().getFirst("Content-Disposition");
            if (contentDisposition != null) {
                Pattern pattern = Pattern.compile("filename=\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(contentDisposition);
                if (matcher.find()) {
                    fileName = matcher.group(1);
                }
            }
            String fileContentType = fileInputPart.getMediaType().toString();

            // Estrazione file_size dal MultipartFormDataInput
            long fileSize = -1; // Default
            List<InputPart> fileSizeInputParts = uploadForm.get("file_size");
            if (fileSizeInputParts != null && !fileSizeInputParts.isEmpty()) {
                try {
                    fileSize = Long.parseLong(fileSizeInputParts.get(0).getBodyAsString());
                } catch (NumberFormatException e) {
                    LOG.warn("FlussiApiImpl.uploadFlusso - Errore durante la conversione di 'file_size' a long.", e);
                }
            }

            if (codice.isEmpty() || tipoFile.isEmpty() || fileName.isEmpty()) {
                throw new IllegalArgumentException("Codice, Tipo File e File sono obbligatori.");
            }

            LOG.info("FlussiApiImpl.uploadFlusso - Valore di fileSize prima della chiamata al servizio: {}", fileSize); // INFO LOG

            String message = flussoService.uploadFlusso(codice, tipoFile, note, fileContent, fileName, fileSize, fileContentType);
            FlussoUploadResponse response = new FlussoUploadResponse();
            response.setMessage(message);
            return Response.ok(response).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.uploadFlusso - NotFoundException: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Risorsa non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("FlussiApiImpl.uploadFlusso - IllegalArgumentException: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.uploadFlusso - Errore generico durante il caricamento del flusso", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response searchFlussi(
            @QueryParam("codiceFile") String codiceFile,
            @QueryParam("fonte") String fonte,
            @QueryParam("tipoFile") String tipoFile,
            @QueryParam("dataCaricamento") Date dataCaricamento,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.searchFlussi - Inizio ricerca flussi");

            // Impostazione valori di default per limit e offset
            if (limit == null) {
                limit = 10; // valore di default
            }
            if (offset == null) {
                offset = 0; // valore di default
            }

            PagedFlussoSearchResult result = flussoService.searchFlussi(codiceFile, fonte, tipoFile, dataCaricamento, limit, offset);
            LOG.info("FlussiApiImpl.searchFlussi - Trovati {} flussi", result.getTotalElements());

            return Response.ok(result).build();
        } catch (Exception e) {
            LOG.error("Errore durante l'operazione sui flussi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response elaboraFlusso(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.elaboraFlusso - Elaborazione flusso ID: {}", id);
            // Chiamata sincrona: veloce perché lancia ConfirmationRequiredException prima
            // del bulk processing. Se non c'è conflitto, elabora tutto qui.
            ElaboraFlussoResponseDTO result = flussoService.elaboraFlusso(id);
            return Response.ok(result).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.elaboraFlusso - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.elaboraFlusso - Errore durante l'elaborazione del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getProcessStatus(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.debug("FlussiApiImpl.getProcessStatus - Richiesta stato per flusso ID: {}", id);
            String stato = asyncElaboraFlussoService.getStatoElaborazione(id);
            ProcessStatusResponse response = toProcessStatusResponse(id, stato);
            // In caso di fallimento il frontend mostra `message` nella dialog di avanzamento:
            // vi si riporta il testo dell'errore registrato a log, non il messaggio generico.
            if (ProcessStatusResponse.StatusEnum.FAILED.equals(response.getStatus())) {
                String ultimoErrore = ultimoErrorePerFlusso(id);
                if (ultimoErrore != null) {
                    response.setMessage(ultimoErrore);
                }
            }
            return Response.ok(response).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.getProcessStatus - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.getProcessStatus - Errore per flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    /**
     * Recupera il testo dell'ultimo errore registrato nel log di elaborazione di un flusso.
     *
     * @param id ID del flusso
     * @return il messaggio d'errore più recente, oppure {@code null} se non ce ne sono
     */
    private String ultimoErrorePerFlusso(String id) {
        try {
            // I log arrivano ordinati per data di inserimento decrescente: il primo ERROR è l'ultimo occorso.
            return logService.getLogPerFlusso(id).stream()
                    .map(it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTElaborazioneLog::getOperazione)
                    .filter(op -> op != null && op.contains("] ERROR: "))
                    .findFirst()
                    .map(op -> op.substring(op.indexOf("] ERROR: ") + "] ERROR: ".length()))
                    .orElse(null);
        } catch (Exception e) {
            LOG.warn("FlussiApiImpl.ultimoErrorePerFlusso - Impossibile leggere il log del flusso {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public Response resumeElaborazioneFlusso(
            it.csi.accrual.accrualsbapi.dto.FlussiResumeelaborazioneBody body,
            @QueryParam("resumeToken") String resumeToken,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.resumeElaborazioneFlusso - Ripresa elaborazione con token: {}", resumeToken);

            if (body == null) {
                Error error = new Error();
                error.setStatus("400");
                error.setCode("BAD_REQUEST");
                error.setTitle("Richiesta non valida");
                error.setDetail("Il corpo della richiesta non può essere vuoto.");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            boolean userConfirmed = body.isUserConfirmed() != null ? body.isUserConfirmed() : false;

            if (!userConfirmed) {
                // Utente ha rifiutato: annulla sincrono (veloce)
                ElaboraFlussoResponseDTO result = flussoService.resumeElaborazioneFlusso(resumeToken, false);
                LOG.info("FlussiApiImpl.resumeElaborazioneFlusso - Elaborazione annullata dall'utente.");
                return Response.ok(result).build();
            }

            // Utente ha confermato: estrai idFlusso dal token e avvia elaborazione asincrona
            String decoded = new String(java.util.Base64.getDecoder().decode(resumeToken));
            String idFlusso = extractFromResumeToken(decoded, "idFlusso");
            if (idFlusso == null) {
                throw new IllegalArgumentException("Token di ripresa non valido: campo 'idFlusso' mancante.");
            }
            asyncElaboraFlussoService.elaboraFlussoAsync(idFlusso, true);
            ElaboraFlussoResponseDTO started = new ElaboraFlussoResponseDTO();
            started.setNeedsConfirmation(false);
            LOG.info("FlussiApiImpl.resumeElaborazioneFlusso - Elaborazione confermata e avviata in background per flusso ID: {}", idFlusso);
            return Response.accepted(started).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.resumeElaborazioneFlusso - Token di ripresa non trovato o flusso non valido: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Token di ripresa non valido");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("FlussiApiImpl.resumeElaborazioneFlusso - Richiesta non valida: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.resumeElaborazioneFlusso - Errore durante la ripresa dell'elaborazione", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response downloadFlusso(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        File downloadedFile = null; // Dichiarazione all'esterno del try per la finally
        try {
            LOG.info("FlussiApiImpl.downloadFlusso - Inizio download flusso ID: {}", id);

            downloadedFile = flussoService.downloadFlusso(id); // Ora restituisce File

            // Legge il contenuto del file in byte[]
            byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());

            LOG.info("FlussiApiImpl.downloadFlusso - Flusso {} scaricato con successo, dimensione: {} bytes", id, fileContent.length);

            return Response.ok(fileContent)
                    .header("Content-Disposition", "attachment; filename=" + downloadedFile.getName())
                    .header("Content-Type", "application/octet-stream")
                    .build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.downloadFlusso - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IOException e) { // Aggiunto per gestire errori di lettura del file
            LOG.error("FlussiApiImpl.downloadFlusso - Errore I/O durante il download del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail("Errore durante la lettura del file: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            LOG.error("Errore durante l'operazione sui flussi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } finally {
            if (downloadedFile != null) {
                try {
                    Files.deleteIfExists(downloadedFile.toPath());
                    LOG.debug("FlussiApiImpl.downloadFlusso - File temporaneo {} eliminato con successo", downloadedFile.getName());
                } catch (IOException e) {
                    LOG.warn("FlussiApiImpl.downloadFlusso - Errore durante l'eliminazione del file temporaneo {}: {}", downloadedFile.getName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Response exportResults(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        File exportedFile = null;
        try {
            LOG.info("FlussiApiImpl.exportResults - Inizio export risultati flusso ID: {}", id);

            exportedFile = flussoService.exportResults(id);

            byte[] fileContent = Files.readAllBytes(exportedFile.toPath());

            LOG.info("FlussiApiImpl.exportResults - Export risultati per flusso {} completato, dimensione: {} bytes", id, fileContent.length);

            return Response.ok(fileContent)
                    .header("Content-Disposition", "attachment; filename=" + exportedFile.getName())
                    .header("Content-Type", "application/zip")
                    .build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.exportResults - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Errore durante l'operazione sui flussi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } finally {
            if (exportedFile != null) {
                try {
                    Files.deleteIfExists(exportedFile.toPath());
                } catch (IOException e) {
                    LOG.warn("FlussiApiImpl.exportResults - Errore durante l'eliminazione del file temporaneo {}: {}", exportedFile.getName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Response getFlussoById(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.getFlussoById - Recupero flusso ID: {}", id);
            FlussoDetail flussoDetail = flussoService.getFlussoById(id);
            return Response.ok(flussoDetail).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.getFlussoById - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Errore durante l'operazione sui flussi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    @Override
    public Response deleteFlussoById(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.deleteFlussoById - Eliminazione flusso ID: {}", id);
            flussoService.deleteFlussoById(id);
            return Response.noContent().build(); // 204 No Content
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.deleteFlussoById - Flusso {} non trovato: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Flusso non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.deleteFlussoById - Errore durante l'eliminazione del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    /**
     * Converte lo stato di elaborazione del flusso nello stato di avanzamento esposto dal contratto.
     */
    private ProcessStatusResponse toProcessStatusResponse(String jobId, String rawStato) {
        ProcessStatusResponse response = new ProcessStatusResponse();
        response.setJobId(jobId);
        switch (rawStato != null ? rawStato : "") {
            case "DA_ELABORARE":
                response.setStatus(ProcessStatusResponse.StatusEnum.PENDING);
                response.setProgressPercentage(0);
                response.setCurrentStep("In attesa di elaborazione");
                response.setMessage("Il flusso è in coda per l'elaborazione.");
                break;
            case "IN_ELABORAZIONE":
                response.setStatus(ProcessStatusResponse.StatusEnum.IN_PROGRESS);
                response.setProgressPercentage(40);
                response.setCurrentStep("Elaborazione in corso");
                response.setMessage("Il flusso è in corso di elaborazione.");
                break;
            case "ELABORATO":
                response.setStatus(ProcessStatusResponse.StatusEnum.COMPLETED);
                response.setProgressPercentage(100);
                response.setCurrentStep("Elaborazione completata");
                response.setMessage("Il flusso è stato elaborato con successo.");
                break;
            // "ELABORATO_CON_ERRORI" è il codice effettivamente presente a DB
            // (AccrualBusinessConstants.STATO_FILE_ERRORE); "ERRORE" è mantenuto per compatibilità.
            case "ELABORATO_CON_ERRORI":
            case "ERRORE":
                response.setStatus(ProcessStatusResponse.StatusEnum.FAILED);
                response.setProgressPercentage(-1);
                response.setCurrentStep("Errore");
                response.setMessage("Si è verificato un errore durante l'elaborazione.");
                break;
            case "ANNULLATO":
                response.setStatus(ProcessStatusResponse.StatusEnum.FAILED);
                response.setProgressPercentage(-1);
                response.setCurrentStep("Annullato");
                response.setMessage("L'elaborazione del flusso è stata annullata.");
                break;
            case "CARICATO":
                response.setStatus(ProcessStatusResponse.StatusEnum.PENDING);
                response.setProgressPercentage(0);
                response.setCurrentStep("File caricato");
                response.setMessage("Il flusso è stato caricato ed è pronto per l'elaborazione.");
                break;
            case "IN_ATTESA_CONFERMA_UTENTE":
                response.setStatus(ProcessStatusResponse.StatusEnum.IN_PROGRESS);
                response.setProgressPercentage(15);
                response.setCurrentStep("In attesa di conferma");
                response.setMessage("Il flusso è in attesa di conferma utente.");
                break;
            default:
                response.setStatus(ProcessStatusResponse.StatusEnum.PENDING);
                response.setProgressPercentage(0);
                response.setCurrentStep("Sconosciuto");
                response.setMessage("Stato non riconosciuto: " + rawStato);
        }
        return response;
    }

    private String extractFromResumeToken(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }

    @Override
    public Response getFontiFlusso(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.getFontiFlusso - Recupero fonti");
            List<Fonte> fonti = flussoService.getFonti();
            return Response.ok(fonti).build();
        } catch (NotFoundException e) {
            LOG.warn("FlussiApiImpl.getFontiFlusso - Nessuna fonte trovata: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Fonti non trovate");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.getFontiFlusso - Errore durante il recupero delle fonti", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
