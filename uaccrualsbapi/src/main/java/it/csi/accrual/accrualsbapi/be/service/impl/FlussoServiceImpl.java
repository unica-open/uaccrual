/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;

import it.csi.accrual.accrualsbapi.be.exception.ConfirmationRequiredException; // AGGIUNTO
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.util.AccrualBusinessConstants;
import it.csi.accrual.accrualsbapi.be.service.FlussoService;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbapi.dto.FlussoElaboraResponse;
import it.csi.accrual.accrualsbapi.dto.FlussoSearchResult;
import it.csi.accrual.accrualsbapi.dto.FlussoDetail;
import it.csi.accrual.accrualsbapi.dto.PagedFlussoSearchResult;
import it.csi.accrual.accrualsbapi.dto.Fonte;
import it.csi.accrual.accrualsbapi.dto.ElaboraFlussoResponseDTO; // AGGIUNTO
import it.csi.accrual.accrualsbapi.util.ElaborazioneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64; // AGGIUNTO
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.Date;
import java.math.BigDecimal;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.charset.StandardCharsets;
import it.csi.accrual.accrualsbapi.be.service.impl.FileSpecifications;
import org.springframework.data.jpa.domain.Specification;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


@Service
@Transactional
public class FlussoServiceImpl implements FlussoService {

    private static final Logger LOG = LoggerFactory.getLogger(FlussoServiceImpl.class);

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;
    @Autowired
    private AccrualDFileTipoRepository accrualDFileTipoRepository;
    @Autowired
    private AccrualDFileStatoRepository accrualDFileStatoRepository;
    @Autowired
    private AccrualTEnteRepository accrualTEnteRepository;
    @Autowired
    private AccrualRFileStatoRepository accrualRFileStatoRepository;
    @Autowired
    private AccrualDFonteRepository accrualDFonteRepository;
    
    @Autowired
    private LogService logService;

    @Autowired
    private NotificaService notificaService;

    @Autowired
    private AccrualTCausaleRepository causaleRepository;
    
    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;
    
    @Autowired
    private AccrualRPrimaNotaStatoRepository accrualRPrimaNotaStatoRepository;

    @Autowired
    private AccrualTElaborazioneLogRepository logRepository;

    @Autowired
    private Pdc118Processor pdc118Processor;

    @Autowired
    private VariazioniInventarialiProcessor variazioniInventarialiProcessor;


    @Override
    public String uploadFlusso(String codice, String tipoFileCodice, String note, InputStream fileContent, String fileName, long fileSize, String fileContentType) throws NotFoundException, IllegalArgumentException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.uploadFlusso - Inizio caricamento flusso per ente: {}", enteCodice);

        // 1. Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // 2. Recupera il TipoFile
        AccrualDFileTipo fileTipo = accrualDFileTipoRepository.findByCodiceAndFonteEnteId(tipoFileCodice, ente.getId());
        if (fileTipo == null) {
            throw new IllegalArgumentException("Tipo file " + tipoFileCodice + " non valido per l'ente specificato.");
        }

        // 3. Recupera lo Stato del file (DA ELABORARE)
        AccrualDFileStato fileStato = accrualDFileStatoRepository.findByCodice("DA_ELABORARE")
            .orElseThrow(() -> new NotFoundException("Stato 'DA ELABORARE' per i file non configurato nel sistema."));

        // 4. Crea l'entità AccrualTFile
        AccrualTFile accrualTFile = new AccrualTFile();
        accrualTFile.setCodice(codice);
        accrualTFile.setNome(fileName);
        accrualTFile.setTipo(fileContentType);
        accrualTFile.setNote(note);
        accrualTFile.setDimensione(BigDecimal.valueOf(fileSize));

        try {
            accrualTFile.setFile(fileContent.readAllBytes());
        } catch (IOException e) {
            LOG.error("Errore durante la lettura del contenuto del file: {}", e.getMessage());
            throw new IllegalArgumentException("Impossibile leggere il contenuto del file.");
        }

        accrualTFile.setFileTipo(fileTipo);
        accrualTFile.setEnte(ente);

        // Campi di audit
        String utenteCorrente = "SYSTEM";
        accrualTFile.setDataInserimento(LocalDateTime.now());
        accrualTFile.setUtenteInserimento(utenteCorrente);
        accrualTFile.setUtenteAggiornamento(utenteCorrente);

        // Imposta lo stato iniziale tramite il nuovo metodo
        accrualTFile.setCurrentStato(fileStato, utenteCorrente, ente);

        // 5. Salva l'entità
        accrualTFileRepository.save(accrualTFile);

        logService.registraInfo(accrualTFile, "Upload Flusso", "File " + fileName + " caricato con successo. Codice: " + codice);
        LOG.info("FlussoServiceImpl.uploadFlusso - Flusso caricato con successo con codice: {}", codice);
        return "File caricato con successo.";
    }

    @Override
    public List<Fonte> getFonti() throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.getFonti - Recupero fonti per ente: {}", enteCodice);

        // Recupera l'Ente per verificare l'esistenza, anche se per ora le fonti non sono filtrate per ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        List<AccrualDFonte> jpaFonti = accrualDFonteRepository.findAll(); // Recupera tutte le fonti

        if (jpaFonti.isEmpty()) {
            throw new NotFoundException("Nessuna fonte configurata nel sistema.");
        }

        return jpaFonti.stream()
                .map(jpaFonte -> {
                    Fonte dtoFonte = new Fonte();
                    dtoFonte.setId(jpaFonte.getId());
                    dtoFonte.setCodice(jpaFonte.getCodice());
                    dtoFonte.setDescrizione(jpaFonte.getDescrizione());
                    return dtoFonte;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public PagedFlussoSearchResult searchFlussi(String codiceFile, String fonte, String tipoFile, Date dataCaricamento, Integer limit, Integer offset) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.searchFlussi - Inizio ricerca flussi per ente: {}", enteCodice);

        // Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Creazione della paginazione
        int page = offset != null ? offset : 0;
        int size = limit != null ? limit : 20;

        Pageable pageable = PageRequest.of(page, size);

        // Costruisci la Specification combinando tutti i filtri
        Specification<AccrualTFile> spec = FileSpecifications.withFilters(
            ente.getId(),
            codiceFile,
            fonte,
            tipoFile,
            dataCaricamento
        );

        // Effettua la ricerca con i filtri
        Page<AccrualTFile> filePage = accrualTFileRepository.findAll(spec, pageable);


        // Converti i risultati in DTO
        List<FlussoSearchResult> searchResults = filePage.getContent().stream()
                .map(this::convertToFileSearchResultDto)
                .collect(Collectors.toList());

        // Crea il risultato paginato
        PagedFlussoSearchResult pagedResult = new PagedFlussoSearchResult();
        pagedResult.setData(searchResults);
        pagedResult.setPage(filePage.getNumber());
        pagedResult.setPageSize(filePage.getSize());
        pagedResult.setTotalElements(filePage.getTotalElements());

        LOG.info("FlussoServiceImpl.searchFlussi - Trovati {} flussi", filePage.getTotalElements());
        return pagedResult;
    }

    @Override
    public ElaboraFlussoResponseDTO elaboraFlusso(String id) throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();

        LOG.info("FlussoServiceImpl.elaboraFlusso - Inizio elaborazione flusso ID: {} per ente: {}", id, enteCodice);

        // Recupera l'Ente
        LOG.debug("Ricerca ente per codice fiscale: {}", enteCodice);
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Recupera il file dal repository
        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        // Verifica che il file appartenga all'ente specificato
        if (!file.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        // Recupera lo stato "IN_ELABORAZIONE"
        AccrualDFileStato statoInElaborazione = accrualDFileStatoRepository.findByCodice("IN_ELABORAZIONE")
                .orElseThrow(() -> new NotFoundException("Stato 'IN_ELABORAZIONE' per i file non configurato nel sistema."));

        // Aggiorna lo stato del file a "IN_ELABORAZIONE"
        file.setCurrentStato(statoInElaborazione, "SYSTEM", ente);
        file.setDataAggiornamento(LocalDateTime.now());
        file.setUtenteAggiornamento("SYSTEM");

        // Salva le modifiche con stato "IN_ELABORAZIONE"
        accrualTFileRepository.save(file);

        // Identifica il tipo di flusso e delega l'elaborazione al processor appropriato
        String tipoFileCodice = file.getFileTipo().getCodice();
        ElaboraFlussoResponseDTO responseDTO = new ElaboraFlussoResponseDTO();
        FlussoElaboraResponse flussoElaboraResponse = new FlussoElaboraResponse();

        try {
            // Chiama il processor appropriato in base al tipo di file.
            // confirmedOverride=false: se esistono già dati, il processor lancia ConfirmationRequiredException
            // e il FE mostra la modale di conferma. Il resume avviene tramite resumeElaborazioneFlusso con confirmedOverride=true.
            if ("SALDI_PDC_118".equals(tipoFileCodice)) {
                flussoElaboraResponse = pdc118Processor.processaSaldiPdc118(id, false);
            } else if ("VARIAZIONI_INVENTARIALI".equals(tipoFileCodice)) {
                flussoElaboraResponse = variazioniInventarialiProcessor.processaVariazioniInventariali(id, false);
            } else {
                flussoElaboraResponse.setSuccess(true);
                flussoElaboraResponse.setMessage("Tipo di file " + tipoFileCodice + " non richiede elaborazione specifica.");
            }

            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

            // Se l'elaborazione è completata senza richieste di conferma, imposta lo stato a ELABORATO
            if (flussoElaboraResponse.isSuccess()) {
                AccrualDFileStato statoElaborato = accrualDFileStatoRepository.findByCodice("ELABORATO")
                        .orElseThrow(() -> new NotFoundException("Stato 'ELABORATO' per i file non configurato nel sistema."));
                file.setCurrentStato(statoElaborato, "SYSTEM", ente);
                file.setDataAggiornamento(LocalDateTime.now());
                file.setUtenteAggiornamento("SYSTEM");
                accrualTFileRepository.save(file);
            }

        } catch (ConfirmationRequiredException cre) {
            // Richiesta conferma utente (es. esistono già prime note)
            LOG.info("Richiesta conferma utente per flusso ID: {} - Codice: {}", id, cre.getConfirmationCode());
            responseDTO.setNeedsConfirmation(true);
            responseDTO.setConfirmationCode(cre.getConfirmationCode());
            responseDTO.setConfirmationMessage(cre.getConfirmationMessage());
            responseDTO.setTipoMessaggio("CONFIRM");

            // Popola la lista delle prime note da annullare
            if (cre.getPrimaNoteDaAnnullare() != null && !cre.getPrimaNoteDaAnnullare().isEmpty()) {
                List<it.csi.accrual.accrualsbapi.dto.PrimaNotaConfermaInfo> pnInfoList = cre.getPrimaNoteDaAnnullare().stream()
                    .map(pn -> {
                        it.csi.accrual.accrualsbapi.dto.PrimaNotaConfermaInfo info = new it.csi.accrual.accrualsbapi.dto.PrimaNotaConfermaInfo();
                        info.setId(pn.getId());
                        info.setAnno(pn.getAnno());
                        info.setNumeroProvvisorio(pn.getNumero() != null ? pn.getNumero().toString() : null);
                        info.setDescrizione(pn.getDescrizione());
                        info.setCausale(pn.getCausale() != null ? pn.getCausale().getCodice() : null);
                        String statoCorrente = pn.getStati() != null ? pn.getStati().stream()
                            .filter(s -> s.getFineValidita() == null)
                            .findFirst()
                            .map(s -> s.getPrimaNotaStato() != null ? s.getPrimaNotaStato().getCodice() : null)
                            .orElse(null) : null;
                        info.setStato(statoCorrente);
                        return info;
                    })
                    .collect(java.util.stream.Collectors.toList());
                responseDTO.setPrimaNoteDaAnnullare(pnInfoList);
            }

            // Crea un token per la ripresa dell'elaborazione dopo la conferma
            String resumeContext = String.format("{\"idFlusso\":\"%s\",\"codiceEnte\":\"%s\",\"tipoFileCodice\":\"%s\",\"confirmationCode\":\"%s\"}",
                    id, enteCodice, tipoFileCodice, cre.getConfirmationCode());
            String resumeToken = Base64.getEncoder().encodeToString(resumeContext.getBytes());
            responseDTO.setResumeToken(resumeToken);

            // La richiesta di conferma diventa una notifica persistente: l'utente la ritrova nel
            // centro avvisi anche dopo aver cambiato sessione, senza perdere il token di ripresa.
            notificaService.chiudiPerFlussoECodice(file, cre.getConfirmationCode());
            notificaService.crea(ente, ElaborazioneUtil.getUtenteCorrente(), NotificaService.TIPO_CONFIRM,
                    cre.getConfirmationCode(), "Conferma richiesta", cre.getConfirmationMessage(), file, resumeToken);

            // Imposta lo stato a IN_ATTESA_CONFERMA_UTENTE: resumeElaborazioneFlusso lo
            // ripristinerà a DA_ELABORARE se l'utente non procede, oppure proseguirà se conferma.
            AccrualDFileStato statoAttesaConferma = accrualDFileStatoRepository.findByCodice("IN_ATTESA_CONFERMA_UTENTE")
                    .orElseThrow(() -> new NotFoundException("Stato 'IN_ATTESA_CONFERMA_UTENTE' non configurato nel sistema."));
            file.setCurrentStato(statoAttesaConferma, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

            flussoElaboraResponse = new FlussoElaboraResponse();
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Richiesta conferma utente: " + cre.getConfirmationMessage());
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

        } catch (Exception e) {
            LOG.error("Errore durante l'elaborazione del flusso ID: {} - {}", id, e.getMessage(), e);

            logService.registraErrore(file, "Errore elaborazione flusso", e.getMessage());

            notificaService.crea(ente, ElaborazioneUtil.getUtenteCorrente(), NotificaService.TIPO_ERROR, null,
                    "Elaborazione non riuscita",
                    "Il flusso " + file.getCodice() + " non è stato elaborato: " + e.getMessage(), file, null);

            // Se lo stato di errore non fosse configurato il file resterebbe su IN_ELABORAZIONE,
            // quindi non più rilanciabile: va almeno segnalato a log.
            Optional<AccrualDFileStato> statoErrore = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_ERRORE);
            if (statoErrore.isPresent()) {
                file.setCurrentStato(statoErrore.get(), "SYSTEM", ente);
                file.setDataAggiornamento(LocalDateTime.now());
                file.setUtenteAggiornamento("SYSTEM");
                accrualTFileRepository.save(file);
            } else {
                LOG.error("Stato '{}' non configurato: il flusso {} resta in elaborazione.",
                        AccrualBusinessConstants.STATO_FILE_ERRORE, id);
            }

            flussoElaboraResponse = new FlussoElaboraResponse();
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Errore durante l'elaborazione: " + e.getMessage());
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);
            responseDTO.setTipoMessaggio("ERROR");
        }

        LOG.info("FlussoServiceImpl.elaboraFlusso - Flusso {} elaborato. Richiesta conferma: {}", id, responseDTO.isNeedsConfirmation());
        return responseDTO;
    }
    


    @Override
    public File downloadFlusso(String id) throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.downloadFlusso - Inizio download flusso ID: {} per ente: {}", id, enteCodice);

        // Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Recupera il file dal repository
        AccrualTFile fileEntity = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        // Verifica che il file appartenga all'ente specificato
        if (!fileEntity.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        // Crea un file temporaneo per il download
        try {
            Path tempDir = Files.createTempDirectory("accrual-downloads");
            Path tempFilePath = tempDir.resolve(fileEntity.getNome());
            Files.write(tempFilePath, fileEntity.getFile());
            LOG.info("FlussoServiceImpl.downloadFlusso - Flusso {} scaricato con successo in file temporaneo: {}", id, tempFilePath.toString());
            // Restituisce il file temporaneo. Sarà responsabilità del chiamante gestirlo (es. cancellarlo dopo l'invio).
            return tempFilePath.toFile();
        } catch (IOException e) {
            LOG.error("Errore durante la creazione o scrittura del file temporaneo per il download del flusso {}: {}", id, e.getMessage());
            throw new RuntimeException("Errore durante la preparazione del file per il download.", e);
        }
    }

    @Override
    public FlussoDetail getFlussoById(String id) throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.getFlussoById - Recupero flusso ID: {} per ente: {}", id, enteCodice);

        // Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Recupera il file dal repository
        AccrualTFile fileEntity = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        // Verifica che il file appartenga all'ente specificato
        if (!fileEntity.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        return convertToFileDetailDto(fileEntity);
    }

    // Metodo privato di supporto per convertire l'entità in DTO di dettaglio
    private FlussoDetail convertToFileDetailDto(AccrualTFile file) {
        FlussoDetail result = new FlussoDetail();
        result.setId(String.valueOf(file.getId()));
        result.setCodice(file.getCodice());
        result.setNome(file.getNome());
        // CDU-011: La fonte deve essere quella associata al tipo di file
        result.setFonte(file.getFileTipo().getFonte() != null ? file.getFileTipo().getFonte().getCodice() : "Sconosciuta");
        result.setTipo(file.getFileTipo().getCodice());
        result.setDataCaricamento(Date.from(file.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
        result.setStato(file.getCurrentStato() != null ? file.getCurrentStato().getCodice() : "Sconosciuto");
        result.setNote(file.getNote());
        
        // CDU-011: Caricamento esiti elaborazione dai log
        List<AccrualTElaborazioneLog> logs = logRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(file.getId());
        if (!logs.isEmpty()) {
            List<it.csi.accrual.accrualsbapi.dto.EsitoElaborazione> esiti = logs.stream().map(log -> {
                it.csi.accrual.accrualsbapi.dto.EsitoElaborazione esito = new it.csi.accrual.accrualsbapi.dto.EsitoElaborazione();
                esito.setDataElaborazione(Date.from(log.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
                // Estrai il tipo di log dal messaggio e mappa su Elaborato/Elaborato con errori
                String operazione = log.getOperazione();
                String esitoTestuale;
                // Il formato del log è: [timestamp] TIPO: messaggio
                if (operazione.contains("] ERROR:") || operazione.contains("] WARN:")) {
                    esitoTestuale = "Elaborato con errori";
                } else {
                    // INFO e altri tipi sono considerati Elaborato
                    esitoTestuale = "Elaborato";
                }
                esito.setEsito(esitoTestuale);
                esito.setMessaggio(operazione);
                return esito;
            }).collect(Collectors.toList());
            result.setEsitiElaborazione(esiti);
        }
        
        return result;
    }

    // Metodo privato di supporto per convertire l'entità in DTO di ricerca
    private FlussoSearchResult convertToFileSearchResultDto(AccrualTFile file) {
        FlussoSearchResult result = new FlussoSearchResult();
        result.setId(String.valueOf(file.getId()));
        // CDU-011: La fonte deve essere quella associata al tipo di file
        result.setFonte(file.getFileTipo().getFonte() != null ? file.getFileTipo().getFonte().getCodice() : "Sconosciuta");
        result.setTipo(file.getFileTipo().getCodice());
        result.setCodice(file.getCodice());
        result.setNome(file.getNome());
        // Conversione da LocalDateTime a java.util.Date
        result.setDataCaricamento(Date.from(file.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
        // Recupera lo stato corrente usando il nuovo metodo
        result.setStato(file.getCurrentStato() != null ? file.getCurrentStato().getCodice() : "DA_ELABORARE");

        return result;
    }

    // Metodo privato di supporto per elaborare il contenuto del file
    private void elaboraContenutoFile(AccrualTFile file) {
        // In una implementazione reale, qui ci sarebbe la logica specifica per elaborare
        // il contenuto del file in base al suo tipo e formato
        LOG.info("Elaborazione del contenuto del file ID: {} iniziata", file.getId());

        // Simulazione elaborazione
        try {
            Thread.sleep(100); // Simula un po' di elaborazione
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Elaborazione del file {} interrotta: {}", file.getId(), e.getMessage());
        }

        LOG.info("Elaborazione del contenuto del file ID: {} completata", file.getId());
    }

    @Override
    public void deleteFlussoById(String id) throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.deleteFlussoById - Eliminazione flusso ID: {} per ente: {}", id, enteCodice);

        // 1. Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // 2. Recupera il file dal repository
        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        // 3. Verifica che il file appartenga all'ente specificato
        if (!file.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        // 4. Elimina il file
        accrualTFileRepository.delete(file);

        LOG.info("FlussoServiceImpl.deleteFlussoById - Flusso {} eliminato con successo", id);
    }

    @Override
    public ElaboraFlussoResponseDTO resumeElaborazioneFlusso(String resumeToken, boolean userConfirmed) throws NotFoundException, IllegalArgumentException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.resumeElaborazioneFlusso - Ripresa elaborazione con token: {} per ente: {}", resumeToken, enteCodice);

        // Decodifica il resumeToken
        String decodedContext = new String(Base64.getDecoder().decode(resumeToken));
        // Supponiamo che il contesto sia un JSON semplice per estrarre i dati
        // In una soluzione reale, useremmo una libreria JSON per il parsing
        String idFlusso = extractFromJson(decodedContext, "idFlusso");
        String tipoFileCodice = extractFromJson(decodedContext, "tipoFileCodice");
        String confirmationCode = extractFromJson(decodedContext, "confirmationCode");

        // Recupera l'Ente e il File
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));
        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(idFlusso))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + idFlusso + " non trovato."));

        // Verifica che il file appartenga all'ente corrente
        if (!file.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + idFlusso + " non trovato per l'ente specificato.");
        }

        // Verifica che il file sia in uno stato valido per la ripresa
        String statoCorrenteCodice = file.getCurrentStato() != null ? file.getCurrentStato().getCodice() : "";
        if (!"IN_ATTESA_CONFERMA_UTENTE".equals(statoCorrenteCodice) && !"DA_ELABORARE".equals(statoCorrenteCodice)) {
            throw new IllegalStateException("Il flusso " + idFlusso + " non è in uno stato valido per la ripresa. Stato attuale: " + statoCorrenteCodice);
        }

        ElaboraFlussoResponseDTO responseDTO = new ElaboraFlussoResponseDTO();
        FlussoElaboraResponse flussoElaboraResponse = new FlussoElaboraResponse();
        responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

        if (!userConfirmed) {
            // Utente ha scelto di non procedere: ripristina DA_ELABORARE
            AccrualDFileStato statoDAElaborare = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_DA_ELABORARE)
                    .orElseThrow(() -> new NotFoundException("Stato '" + AccrualBusinessConstants.STATO_FILE_DA_ELABORARE + "' per i file non configurato nel sistema."));
            file.setCurrentStato(statoDAElaborare, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

            logService.registraInfo(file, "Elaborazione non proceduta", "Utente ha scelto di non procedere per il flusso " + idFlusso + ".");

            // La richiesta di conferma è stata evasa, quindi si chiude; al suo posto resta la
            // notifica dell'esito, che deve restare visibile anche cambiando sessione.
            notificaService.chiudiPerFlussoECodice(file, confirmationCode);
            notificaService.crea(ente, ElaborazioneUtil.getUtenteCorrente(), NotificaService.TIPO_INFO,
                    confirmationCode, "Elaborazione non proceduta",
                    "Le prime note esistenti non sono state annullate: il flusso " + file.getCodice()
                            + " è tornato disponibile per l'elaborazione.", file, null);

            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Elaborazione non proceduta.");
            responseDTO.setNeedsConfirmation(false);
            return responseDTO;
        }

        // Utente ha confermato: la richiesta di conferma è evasa e non va più mostrata.
        notificaService.chiudiPerFlussoECodice(file, confirmationCode);

        // Riprendi l'elaborazione: imposta lo stato del file a "IN_ELABORAZIONE"
        AccrualDFileStato statoInElaborazione = accrualDFileStatoRepository.findByCodice("IN_ELABORAZIONE")
                .orElseThrow(() -> new NotFoundException("Stato 'IN_ELABORAZIONE' per i file non configurato nel sistema."));
        file.setCurrentStato(statoInElaborazione, "SYSTEM", ente);
        file.setDataAggiornamento(LocalDateTime.now());
        file.setUtenteAggiornamento("SYSTEM");
        accrualTFileRepository.save(file);

        try {
            // Rilancia l'elaborazione chiamando il processore appropriato.
            // Passiamo true al parametro confirmedOverride perché siamo nella fase di ripresa dopo conferma utente.
            if ("SALDI_PDC_118".equals(tipoFileCodice)) {
                flussoElaboraResponse = pdc118Processor.processaSaldiPdc118(idFlusso, true);
            } else if ("VARIAZIONI_INVENTARIALI".equals(tipoFileCodice)) {
                flussoElaboraResponse = variazioniInventarialiProcessor.processaVariazioniInventariali(idFlusso, true);
            } else {
                flussoElaboraResponse = new FlussoElaboraResponse();
                flussoElaboraResponse.setSuccess(true);
                flussoElaboraResponse.setMessage("Ripresa elaborazione per tipo file " + tipoFileCodice + " non ancora implementata specificamente, ma marcata come ripresa.");
            }

            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

            // Se l'elaborazione ripresa è completata, imposta lo stato a ELABORATO
            if (flussoElaboraResponse.isSuccess()) {
                AccrualDFileStato statoElaborato = accrualDFileStatoRepository.findByCodice("ELABORATO")
                        .orElseThrow(() -> new NotFoundException("Stato 'ELABORATO' per i file non configurato nel sistema."));
                file.setCurrentStato(statoElaborato, "SYSTEM", ente);
                file.setDataAggiornamento(LocalDateTime.now());
                file.setUtenteAggiornamento("SYSTEM");
                accrualTFileRepository.save(file);
            }

        } catch (ConfirmationRequiredException cre) {
            // Se anche dopo la ripresa è richiesta un'altra conferma (scenario raro, ma possibile)
            LOG.info("Nuova richiesta conferma utente durante ripresa flusso ID: {} - Codice: {}", idFlusso, cre.getConfirmationCode());
            responseDTO.setNeedsConfirmation(true);
            responseDTO.setConfirmationCode(cre.getConfirmationCode());
            responseDTO.setConfirmationMessage(cre.getConfirmationMessage());
            responseDTO.setTipoMessaggio("CONFIRM");

            String newResumeContext = String.format("{\"idFlusso\":\"%s\",\"codiceEnte\":\"%s\",\"tipoFileCodice\":\"%s\",\"confirmationCode\":\"%s\"}",
                    idFlusso, enteCodice, tipoFileCodice, cre.getConfirmationCode());
            responseDTO.setResumeToken(Base64.getEncoder().encodeToString(newResumeContext.getBytes()));

            // Aggiorna lo stato del file a "IN_ATTESA_CONFERMA_UTENTE"
            AccrualDFileStato statoAttesaConferma = accrualDFileStatoRepository.findByCodice("IN_ATTESA_CONFERMA_UTENTE")
                    .orElseThrow(() -> new NotFoundException("Stato 'IN_ATTESA_CONFERMA_UTENTE' per i file non configurato nel sistema."));
            file.setCurrentStato(statoAttesaConferma, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

            flussoElaboraResponse = new FlussoElaboraResponse(); // Riutilizzo variabile esistente
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Richiesta conferma utente durante la ripresa.");
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

        } catch (Exception e) {
            LOG.error("Errore durante la ripresa dell'elaborazione del flusso ID: {} - {}", idFlusso, e.getMessage(), e);

            logService.registraErrore(file, "Errore ripresa elaborazione flusso", e.getMessage());

            accrualDFileStatoRepository.findByCodice("ELABORATO_CON_ERRORI").ifPresent(stato -> {
                file.setCurrentStato(stato, "SYSTEM", ente);
                file.setDataAggiornamento(LocalDateTime.now());
                file.setUtenteAggiornamento("SYSTEM");
                accrualTFileRepository.save(file);
            });

            flussoElaboraResponse = new FlussoElaboraResponse();
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Errore durante la ripresa dell'elaborazione: " + e.getMessage());
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);
            responseDTO.setTipoMessaggio("ERROR");
        }
        
        LOG.info("FlussoServiceImpl.resumeElaborazioneFlusso - Flusso {} ripreso. Richiesta conferma: {}", idFlusso, responseDTO.isNeedsConfirmation());
        return responseDTO;
    }

    // Metodo helper per estrarre valori da una stringa JSON semplice
    // In una soluzione reale, si userebbe una libreria JSON come Jackson/Gson
    private String extractFromJson(String jsonString, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = jsonString.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchKey.length();
        int endIndex = jsonString.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return jsonString.substring(startIndex, endIndex);
    }
    
    // Metodo aggiunto per soddisfare la chiamata in AsyncElaboraFlussoService
    public ElaboraFlussoResponseDTO executeGenericProcessing(AccrualTFile file, AccrualTEnte ente) {
        // Implementazione base per elaborazione generica
        // Questo metodo può essere espanso in base alle esigenze specifiche
        LOG.info("Esecuzione elaborazione generica per il file: {}", file.getId());
        
        ElaboraFlussoResponseDTO response = new ElaboraFlussoResponseDTO();
        FlussoElaboraResponse flussoResponse = new FlussoElaboraResponse();
        flussoResponse.setSuccess(true);
        flussoResponse.setMessage("Elaborazione generica completata con successo per il file: " + file.getCodice());
        response.setFlussoElaboraResponse(flussoResponse);
        response.setTipoMessaggio("SUCCESS"); 

        return response;
    }
    
    /**
     * Metodo per ottenere lo stato della prima nota associato all'evento
     * attraverso la relazione: Evento → Causale → PrimaNota → Stato
     */
    public AccrualDPrimaNotaStato getStatoPrimaNotaByEvento(AccrualTEvento evento) {
        if (evento == null) {
            return null;
        }
        
        // Trova tutte le causali associate a questo evento
        List<AccrualTCausale> causali = causaleRepository.findByEvento_Id(evento.getId());
        
        if (causali.isEmpty()) {
            return null; // Nessuna causale associata all'evento
        }
        
        // Trova tutte le prime note associate a queste causali
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByCausaleIn(causali);
        
        if (primeNote.isEmpty()) {
            return null; // Nessuna prima nota associata alle causali dell'evento
        }
        
        // Per ogni prima nota, cerca lo stato più recente
        LocalDateTime maxDate = null;
        AccrualDPrimaNotaStato statoPiuRecente = null;
        
        for (AccrualTPrimaNota pn : primeNote) {
            List<AccrualRPrimaNotaStato> stati = accrualRPrimaNotaStatoRepository.findByPrimaNotaAndFineValiditaIsNull(pn);
                
            for (AccrualRPrimaNotaStato statoRel : stati) {
                if (maxDate == null || statoRel.getInizioValidita().isAfter(maxDate)) {
                    maxDate = statoRel.getInizioValidita();
                    statoPiuRecente = statoRel.getPrimaNotaStato();
                }
            }
        }
        
        return statoPiuRecente;
    }

    @Override
    public File exportResults(String id) throws NotFoundException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("FlussoServiceImpl.exportResults - Inizio export risultati flusso ID: {} per ente: {}", id, enteCodice);

        // Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Recupera il file dal repository
        AccrualTFile fileEntity = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        // Verifica che il file appartenga all'ente specificato
        if (!fileEntity.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        // Recupera i log di elaborazione
        List<AccrualTElaborazioneLog> logs = logRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(fileEntity.getId());

        // Crea un file ZIP temporaneo
        try {
            Path tempDir = Files.createTempDirectory("accrual-exports");
            String zipFileName = "risultati_elaborazione_" + fileEntity.getCodice() + ".zip";
            Path zipPath = tempDir.resolve(zipFileName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                // Aggiungi file di log testuale
                String logFileName = "log_elaborazione_" + fileEntity.getCodice() + ".txt";
                ZipEntry entry = new ZipEntry(logFileName);
                zos.putNextEntry(entry);

                StringBuilder logContent = new StringBuilder();
                logContent.append("REPORT ELABORAZIONE FLUSSO: ").append(fileEntity.getCodice()).append("\n");
                logContent.append("NOME FILE: ").append(fileEntity.getNome()).append("\n");
                logContent.append("DATA CARICAMENTO: ").append(fileEntity.getDataInserimento()).append("\n");
                logContent.append("----------------------------------------------------------------------\n\n");

                if (logs.isEmpty()) {
                    logContent.append("Nessun log di elaborazione trovato per questo flusso.\n");
                } else {
                    for (AccrualTElaborazioneLog logEntry : logs) {
                        logContent.append("[").append(logEntry.getDataInserimento()).append("] ")
                                  .append(logEntry.getOperazione()).append("\n");
                    }
                }

                zos.write(logContent.toString().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            LOG.info("FlussoServiceImpl.exportResults - ZIP generato con successo: {}", zipPath.toString());
            return zipPath.toFile();
        } catch (IOException e) {
            LOG.error("Errore durante la creazione del file ZIP per l'export dei risultati del flusso {}: {}", id, e.getMessage());
            throw new RuntimeException("Errore durante la preparazione dell'export dei risultati.", e);
        }
    }
}


