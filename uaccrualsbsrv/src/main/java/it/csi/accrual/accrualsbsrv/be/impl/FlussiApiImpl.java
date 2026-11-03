/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.FlussiApi;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.service.FlussoService;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.FlussoDetail;
import it.csi.accrual.accrualsbsrv.dto.InlineResponse2006;
import it.csi.accrual.accrualsbsrv.dto.PagedFlussoSearchResult;
import it.csi.accrual.accrualsbsrv.dto.ElaboraFlussoResponseDTO;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FlussiApiImpl implements FlussiApi {

    private static final Logger LOG = LoggerFactory.getLogger(FlussiApiImpl.class);

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Override
    public Response exportResults(
            @PathParam("id") String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        File exportedFile = null;
        try {
            LOG.info("FlussiApiImpl.exportResults - Inizio export risultati flusso ID: {} per ente: {}", id);

            exportedFile = flussoService.exportResults(id);

            byte[] fileContent = Files.readAllBytes(exportedFile.toPath());

            LOG.info("FlussiApiImpl.exportResults - Esiti flusso {} esportati con successo, dimensione: {} bytes", id, fileContent.length);

            return Response.ok(fileContent)
                    .header("Content-Disposition", "attachment; filename=" + exportedFile.getName())
                    .header("Content-Type", "text/csv; charset=UTF-8")
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
            LOG.error("FlussiApiImpl.exportResults - Errore durante l'export del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } finally {
            if (exportedFile != null) {
                try {
                    Files.deleteIfExists(exportedFile.toPath());
                } catch (IOException e) {
                    LOG.warn("Errore eliminazione file temporaneo export: {}", e.getMessage());
                }
            }
        }
    }

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
                    LOG.warn("FlussiApiImpl.uploadFlusso - Errore durante la conversione di 'file_size' a long. Verrà usato il valore di default (-1).", e);
                }
            }

            if (codice.isEmpty() || tipoFile.isEmpty() || fileName.isEmpty()) {
                throw new IllegalArgumentException("Codice, Tipo File e File sono obbligatori.");
            }

            LOG.info("FlussiApiImpl.uploadFlusso - Valore di fileSize prima della chiamata al servizio: {}", fileSize);

            String message = flussoService.uploadFlusso( codice, tipoFile, note, fileContent, fileName, fileSize, fileContentType);
            InlineResponse2006 response = new InlineResponse2006();
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
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response searchFlussi(
            @QueryParam("codiceFile") String codiceFile,
            @QueryParam("fonte") String fonte,
            @QueryParam("tipoFile") String tipoFile,
            @QueryParam("dataCaricamento") String dataCaricamento,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.searchFlussi - Inizio ricerca flussi per ente: {}. Data: {}", dataCaricamento);

            // Conversione locale della data (yyyy-MM-dd)
            Date dataCaricamentoParsed = null;
            if (dataCaricamento != null && !dataCaricamento.trim().isEmpty()) {
                try {
                    dataCaricamentoParsed = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dataCaricamento);
                } catch (java.text.ParseException e) {
                    LOG.warn("Formato data non valido: {}. Atteso yyyy-MM-dd. Errore: {}", dataCaricamento, e.getMessage());
                }
            }

            // Impostazione valori di default per limit e offset
            if (limit == null) limit = 10;
            if (offset == null) offset = 0;

            PagedFlussoSearchResult result = flussoService.searchFlussi( codiceFile, fonte, tipoFile, dataCaricamentoParsed, limit, offset);
            return Response.ok(result).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.searchFlussi - Errore durante la ricerca dei flussi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setDetail(e.getMessage());
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
            LOG.info("FlussiApiImpl.elaboraFlusso - Inizio elaborazione flusso ID: {} per ente: {}", id);

            ElaboraFlussoResponseDTO elaboraResult = flussoService.elaboraFlusso(id);

            LOG.info("FlussiApiImpl.elaboraFlusso - Flusso {} elaborato con successo", id);
            return Response.ok(elaboraResult).build();
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
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response downloadFlusso(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        File downloadedFile = null;
        try {
            LOG.info("FlussiApiImpl.downloadFlusso - Inizio download flusso ID: {} per ente: {}", id);

            downloadedFile = flussoService.downloadFlusso(id);

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
        } catch (IOException e) {
            LOG.error("FlussiApiImpl.downloadFlusso - Errore I/O durante il download del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail("Errore durante la lettura del file: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            LOG.error("FlussiApiImpl.downloadFlusso - Errore generico durante il download del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
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
    public Response getFlussoById(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.getFlussoById - Recupero flusso ID: {} per ente: {}", id);
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
            LOG.error("FlussiApiImpl.getFlussoById - Errore durante il recupero del flusso {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    @DELETE
    @Path("/{id}")
    public Response deleteFlussoById(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.deleteFlussoById - Eliminazione flusso ID: {} per ente: {}", id);
            flussoService.deleteFlussoById(id);
            return Response.noContent().build();
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
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    @GET
    @Path("/flussi/fonti")
    public Response getFontiFlusso(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("FlussiApiImpl.getFontiFlusso - Recupero fonti per ente: {}");
            List<it.csi.accrual.accrualsbsrv.dto.Fonte> fonti = flussoService.getFonti();
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
