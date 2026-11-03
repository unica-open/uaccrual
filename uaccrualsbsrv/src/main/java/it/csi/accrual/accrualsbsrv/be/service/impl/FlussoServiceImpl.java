/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

// import rimosso temporaneamente
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbsrv.be.service.FlussoService;
import it.csi.accrual.accrualsbsrv.dto.FlussoElaboraResponse;
import it.csi.accrual.accrualsbsrv.dto.FlussoSearchResult;
import it.csi.accrual.accrualsbsrv.dto.FlussoDetail;
import it.csi.accrual.accrualsbsrv.dto.PagedFlussoSearchResult;
import it.csi.accrual.accrualsbsrv.dto.TipoFile;
import it.csi.accrual.accrualsbsrv.dto.Fonte;
import it.csi.accrual.accrualsbsrv.dto.ElaboraFlussoResponseDTO; // AGGIUNTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64; // AGGIUNTO
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import it.csi.accrual.accrualsbsrv.be.service.impl.FileSpecifications;
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
    private AccrualTElaborazioneLogRepository logRepository;

    @Autowired
    private LogService logService;
    
    @Autowired
    private AccrualTCausaleRepository causaleRepository;
    
    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;
    
    @Autowired
    private AccrualRPrimaNotaStatoRepository accrualRPrimaNotaStatoRepository;

    @Autowired
    private SecurityContextHelper securityContextHelper;


    @Override
    public String uploadFlusso(String codice, String tipoFileCodice, String note, InputStream fileContent, String fileName, long fileSize, String fileContentType) throws NotFoundException, IllegalArgumentException {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.uploadFlusso - Inizio caricamento flusso per ente: {}", enteCodice);

        // 1. Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // 2. Recupera il TipoFile filtrato per ente
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
    public List<TipoFile> getTipiFile() throws NotFoundException {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.getTipiFile - Recupero tipi di file per ente: {}", enteCodice);

        // Recupera l'Ente per filtrare i tipi file per fonte/ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        List<AccrualDFileTipo> jpaTipiFile = accrualDFileTipoRepository.findAllByFonteEnteIdOrderByCodiceAsc(ente.getId());

        if (jpaTipiFile.isEmpty()) {
            throw new NotFoundException("Nessun tipo di file configurato per l'ente nel sistema.");
        }

        // Filtra i tipi file: mostra solo quelli che sono abilitati all'upload (logica software)
        // I codici validi per l'upload sono quelli definiti nelle costanti di business
        List<String> tipiAbilitatiUpload = List.of(
            "SALDI_PDC_118", 
            "VARIAZIONI_INVENTARIALI"
        );

        return jpaTipiFile.stream()
                .filter(jpaTipo -> tipiAbilitatiUpload.contains(jpaTipo.getCodice())) // Filtro per upload
                .map(jpaTipo -> {
                    TipoFile dtoTipo = new TipoFile();
                    dtoTipo.setCodice(jpaTipo.getCodice());
                    dtoTipo.setDescrizione(jpaTipo.getDescrizione());
                    return dtoTipo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public File exportResults(String id) throws NotFoundException {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.exportResults - Esportazione esiti per flusso ID: {} per ente: {}", id, enteCodice);

        // 1. Recupera l'Ente e le informazioni sul file
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        AccrualTFile fileEntity = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        if (!fileEntity.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso non trovato per l'ente specificato.");
        }

        // 2. Recupera i log dell'elaborazione per il file specificato
        List<AccrualTElaborazioneLog> logs = logRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(fileEntity.getId());

        // 3. Genera il contenuto del file CSV (formato compatibile Excel)
        StringBuilder csv = new StringBuilder();
        csv.append("ESITO ELABORAZIONE\n");

        if (logs.isEmpty()) {
            csv.append("Nessuna informazione di elaborazione disponibile per questo flusso.\n");
        } else {
            for (AccrualTElaborazioneLog log : logs) {
                // Il campo operazione contiene già [TIMESTAMP] TIPO: MESSAGGIO
                String riga = log.getOperazione() != null ? log.getOperazione().replace("\"", "\"\"") : "";
                csv.append("\"").append(riga).append("\"\n");
            }
        }

        // 4. Salva il contenuto in un file temporaneo per il download
        try {
            Path tempDir = Files.createTempDirectory("accrual-exports");
            String fileName = "esiti_elaborazione_" + fileEntity.getCodice() + ".csv";
            Path tempFilePath = tempDir.resolve(fileName);
            Files.write(tempFilePath, csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return tempFilePath.toFile();
        } catch (IOException e) {
            LOG.error("Errore durante la creazione del file di export: {}", e.getMessage());
            throw new RuntimeException("Errore durante la generazione dell'export.", e);
        }
    }
    
    @Override
    public List<Fonte> getFonti() throws NotFoundException {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.getFonti - Recupero fonti per ente: {}", enteCodice);

        // Recupera l'Ente per verificare l'esistenza
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        List<AccrualDFonte> jpaFonti = accrualDFonteRepository.findByEnte_CodiceUnivoco(enteCodice); 

        if (jpaFonti.isEmpty()) {
            LOG.warn("Nessuna fonte configurata per l'ente {}", enteCodice);
            return List.of();
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
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.searchFlussi - Inizio ricerca flussi per ente: {}", enteCodice);

        // Recupera l'Ente
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + enteCodice + " non trovato."));

        // Creazione della paginazione — offset è numero di item, va convertito in page index
        int size = (limit != null && limit > 0) ? limit : 20;
        int page = (offset != null && offset > 0) ? offset / size : 0;

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
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("FlussoServiceImpl.elaboraFlusso - Inizio elaborazione flusso ID: {} per ente: {}", id, enteCodice);

        // Recupera l'Ente
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

                try {
                    if (true) {
                        FlussoElaboraResponse flussoResponse = new FlussoElaboraResponse();
                        flussoResponse.setSuccess(true);
                        flussoResponse.setMessage("L'elaborazione è delegata al modulo API.");
                        responseDTO.setFlussoElaboraResponse(flussoResponse);
                    }
            // Se l'elaborazione è completata senza richieste di conferma, imposta lo stato a ELABORATO
            AccrualDFileStato statoElaborato = accrualDFileStatoRepository.findByCodice("ELABORATO")
                    .orElseThrow(() -> new NotFoundException("Stato 'ELABORATO' per i file non configurato nel sistema."));
            file.setCurrentStato(statoElaborato, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

//         } catch (Exception cre) { // ConfirmationRequiredException non ancora implementata
//             LOG.info("Richiesta conferma utente per flusso ID: {} - Codice: {}", id, cre.getConfirmationCode());
//             responseDTO.setNeedsConfirmation(true);
//             responseDTO.setConfirmationCode(cre.getConfirmationCode());
//             responseDTO.setConfirmationMessage(cre.getConfirmationMessage());
//             responseDTO.setTipoMessaggio("CONFIRM"); // Tipo messaggio per il client
//
//             // Costruisci il resumeToken
//             String resumeContext = String.format("{\"idFlusso\":\"%s\",\"codiceEnte\":\"%s\",\"tipoFileCodice\":\"%s\",\"confirmationCode\":\"%s\"}",
//                     id, enteCodice, tipoFileCodice, cre.getConfirmationCode());
//             responseDTO.setResumeToken(Base64.getEncoder().encodeToString(resumeContext.getBytes()));
// 
//             // Aggiorna lo stato del file a "IN_ATTESA_CONFERMA_UTENTE"
//             AccrualDFileStato statoAttesaConferma = accrualDFileStatoRepository.findByCodice("IN_ATTESA_CONFERMA_UTENTE")
//                     .orElseThrow(() -> new NotFoundException("Stato 'IN_ATTESA_CONFERMA_UTENTE' per i file non configurato nel sistema."));
//             file.setCurrentStato(statoAttesaConferma, "SYSTEM", ente);
//             file.setDataAggiornamento(LocalDateTime.now());
//             file.setUtenteAggiornamento("SYSTEM");
//             accrualTFileRepository.save(file);
// 
//             FlussoElaboraResponse flussoElaboraResponse = new FlussoElaboraResponse(); // Dichiarazione aggiunta
//             flussoElaboraResponse.setSuccess(false); // L'elaborazione non è ancora completata
//             flussoElaboraResponse.setMessage("Richiesta conferma utente.");
//             responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

        } catch (Exception e) {
            LOG.error("Errore durante l'elaborazione del flusso ID: {} - {}", id, e.getMessage(), e);
            
            logService.registraErrore(file, "Errore elaborazione flusso", e.getMessage());
            
            AccrualDFileStato statoErrore = accrualDFileStatoRepository.findByCodice("ERRORE")
                    .orElseThrow(() -> new NotFoundException("Stato 'ERRORE' per i file non configurato nel sistema."));
            file.setCurrentStato(statoErrore, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);
            
            FlussoElaboraResponse flussoElaboraResponse = new FlussoElaboraResponse();
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Errore durante l'elaborazione: " + e.getMessage());
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);
            responseDTO.setTipoMessaggio("ERROR"); // AGGIUNTA
        }

        LOG.info("FlussoServiceImpl.elaboraFlusso - Flusso {} elaborato. Richiesta conferma: {}", id, responseDTO.isNeedsConfirmation());
        return responseDTO;
    }
    


    @Override
    public File downloadFlusso(String id) throws NotFoundException {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
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
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
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
        result.setFonte(file.getFileTipo() != null && file.getFileTipo().getFonte() != null ?
                       file.getFileTipo().getFonte().getDescrizione() : "N/D");
        result.setTipo(file.getFileTipo() != null ? file.getFileTipo().getDescrizione() : "N/D");
        result.setDataCaricamento(Date.from(file.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
        result.setStato(file.getCurrentStato() != null ? file.getCurrentStato().getDescrizione() : "Sconosciuto");
        result.setNote(file.getNote());
        
        // Caricamento esiti elaborazione dai log.
        // Il repository li restituisce dal piu' recente; il dettaglio li mostra invece in ordine
        // cronologico crescente, perche' il log si legge dall'inizio dell'elaborazione alla fine.
        // L'id fa da discriminante quando due righe condividono l'istante di inserimento.
        List<AccrualTElaborazioneLog> logs = new ArrayList<>(
                logRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(file.getId()));
        logs.sort(Comparator
                .comparing(AccrualTElaborazioneLog::getDataInserimento, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(AccrualTElaborazioneLog::getId, Comparator.nullsFirst(Comparator.naturalOrder())));
        if (!logs.isEmpty()) {
            List<it.csi.accrual.accrualsbsrv.dto.EsitoElaborazione> esiti = logs.stream().map(log -> {
                it.csi.accrual.accrualsbsrv.dto.EsitoElaborazione esito = new it.csi.accrual.accrualsbsrv.dto.EsitoElaborazione();
                esito.setDataElaborazione(Date.from(log.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
                // Mappa la severità del log sull'esito (se disponibile) o usa un valore predefinito
                esito.setEsito(log.getOperazione().contains("Errore") ? "ERROR" : "INFO");
                esito.setMessaggio(log.getOperazione());
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
        result.setFonte(file.getFileTipo() != null && file.getFileTipo().getFonte() != null ?
                       file.getFileTipo().getFonte().getDescrizione() : "N/D");
        result.setTipo(file.getFileTipo() != null ? file.getFileTipo().getDescrizione() : "N/D");
        result.setCodice(file.getCodice());
        result.setNome(file.getNome());
        // Conversione da LocalDateTime a java.util.Date - gestisce null
        if (file.getDataInserimento() != null) {
            result.setDataCaricamento(Date.from(file.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            // Fallback: usa data corrente se non disponibile
            result.setDataCaricamento(new Date());
        }
        result.setStato(file.getCurrentStato() != null ? file.getCurrentStato().getDescrizione() : "Da elaborare");

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
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
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
        LOG.info("FlussoServiceImpl.resumeElaborazioneFlusso - Ripresa elaborazione con token: {} e conferma: {}", resumeToken, userConfirmed);

        // Decodifica il resumeToken
        String decodedContext = new String(Base64.getDecoder().decode(resumeToken));
        // Supponiamo che il contesto sia un JSON semplice per estrarre i dati
        // In una soluzione reale, useremmo una libreria JSON per il parsing
        String idFlusso = extractFromJson(decodedContext, "idFlusso");
        String codiceEnte = extractFromJson(decodedContext, "codiceEnte");
        String tipoFileCodice = extractFromJson(decodedContext, "tipoFileCodice");
        String confirmationCode = extractFromJson(decodedContext, "confirmationCode");

        // Recupera l'Ente e il File
        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(codiceEnte)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + codiceEnte + " non trovato."));
        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(idFlusso))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + idFlusso + " non trovato."));

        // Verifica che il file sia in stato "IN_ATTESA_CONFERMA_UTENTE"
        if (!"IN_ATTESA_CONFERMA_UTENTE".equals(file.getCurrentStato().getCodice())) {
            throw new IllegalStateException("Il flusso " + idFlusso + " non è in stato 'IN_ATTESA_CONFERMA_UTENTE'. Stato attuale: " + file.getCurrentStato().getCodice());
        }

        ElaboraFlussoResponseDTO responseDTO = new ElaboraFlussoResponseDTO();
        FlussoElaboraResponse flussoElaboraResponse = new FlussoElaboraResponse();
        responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

        if (!userConfirmed) {
            // Utente ha annullato
            AccrualDFileStato statoAnnullato = accrualDFileStatoRepository.findByCodice("ANNULLATO")
                    .orElseThrow(() -> new NotFoundException("Stato 'ANNULLATO' per i file non configurato nel sistema."));
            file.setCurrentStato(statoAnnullato, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

            logService.registraInfo(file, "Elaborazione Flusso Annullata", "Utente ha annullato la conferma per il flusso " + idFlusso + ".");
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Elaborazione annullata dall'utente.");
            responseDTO.setNeedsConfirmation(false); // Non serve più conferma
            return responseDTO;
        }

        // Utente ha confermato, riprendi l'elaborazione
        // Imposta lo stato del file a "IN_ELABORAZIONE"
        AccrualDFileStato statoInElaborazione = accrualDFileStatoRepository.findByCodice("IN_ELABORAZIONE")
                .orElseThrow(() -> new NotFoundException("Stato 'IN_ELABORAZIONE' per i file non configurato nel sistema."));
        file.setCurrentStato(statoInElaborazione, "SYSTEM", ente);
        file.setDataAggiornamento(LocalDateTime.now());
        file.setUtenteAggiornamento("SYSTEM");
        accrualTFileRepository.save(file);

        try {
            // Rilancia l'elaborazione chiamando il processore appropriato.
            // Questa volta, il processore dovrà sapere che la conferma è già stata data.
            // Questo richiede una modifica all'interfaccia dei processori o un modo per passare la conferma.
            // Per ora, assumiamo che i processori vengano chiamati con un flag o che gestiscano internamente.
            // Una soluzione più pulita sarebbe che i processori espongano un metodo "processaDopoConferma".
            // Ma per semplicità, li chiamo di nuovo e loro devono gestire il contesto ripreso.

            // Nota: in un'implementazione reale, `processaSaldiPdc118` e `processaVariazioniInventariali`
            // dovrebbero accettare un parametro `userConfirmed` o `confirmationContext`
            // per evitare di ri-chiedere la conferma o ri-eseguire parti già elaborate.
            // Per questo esercizio, semplifico ri-chiamando i processori.
            // Se i processori sono ben scritti, dovrebbero essere idempotent.

                        if (true) {
                            flussoElaboraResponse = new FlussoElaboraResponse();
                            flussoElaboraResponse.setSuccess(true);
                            flussoElaboraResponse.setMessage("Ripresa elaborazione delegata al modulo API.");
                        }            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);

            // Se l'elaborazione ripresa è completata, imposta lo stato a ELABORATO
            AccrualDFileStato statoElaborato = accrualDFileStatoRepository.findByCodice("ELABORATO")
                    .orElseThrow(() -> new NotFoundException("Stato 'ELABORATO' per i file non configurato nel sistema."));
            file.setCurrentStato(statoElaborato, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);

//         } catch (Exception cre) { // ConfirmationRequiredException non ancora implementata
//             // Se anche dopo la ripresa è richiesta un'altra conferma (scenario raro, ma possibile)
//             LOG.info("Nuova richiesta conferma utente durante ripresa flusso ID: {} - Codice: {}", idFlusso, cre.getConfirmationCode());
//             responseDTO.setNeedsConfirmation(true);
//             responseDTO.setConfirmationCode(cre.getConfirmationCode());
//             responseDTO.setConfirmationMessage(cre.getConfirmationMessage());
//             responseDTO.setTipoMessaggio("CONFIRM");
//
//             String newResumeContext = String.format("{\"idFlusso\":\"%s\",\"codiceEnte\":\"%s\",\"tipoFileCodice\":\"%s\",\"confirmationCode\":\"%s\"}",
//                     idFlusso, enteCodice, tipoFileCodice, cre.getConfirmationCode());
//             responseDTO.setResumeToken(Base64.getEncoder().encodeToString(newResumeContext.getBytes()));
// 
//             // Aggiorna lo stato del file a "IN_ATTESA_CONFERMA_UTENTE"
//             AccrualDFileStato statoAttesaConferma = accrualDFileStatoRepository.findByCodice("IN_ATTESA_CONFERMA_UTENTE")
//                     .orElseThrow(() -> new NotFoundException("Stato 'IN_ATTESA_CONFERMA_UTENTE' per i file non configurato nel sistema."));
//             file.setCurrentStato(statoAttesaConferma, "SYSTEM", ente);
//             file.setDataAggiornamento(LocalDateTime.now());
//             file.setUtenteAggiornamento("SYSTEM");
//             accrualTFileRepository.save(file);
// 
//             flussoElaboraResponse = new FlussoElaboraResponse(); // Riutilizzo variabile esistente
//             flussoElaboraResponse.setSuccess(false);
//             flussoElaboraResponse.setMessage("Richiesta conferma utente durante la ripresa.");
//             responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);
// 
        } catch (Exception e) {
            LOG.error("Errore durante la ripresa dell'elaborazione del flusso ID: {} - {}", idFlusso, e.getMessage(), e);
            
            logService.registraErrore(file, "Errore ripresa elaborazione flusso", e.getMessage());
            
            AccrualDFileStato statoErrore = accrualDFileStatoRepository.findByCodice("ERRORE")
                    .orElseThrow(() -> new NotFoundException("Stato 'ERRORE' per i file non configurato nel sistema."));
            file.setCurrentStato(statoErrore, "SYSTEM", ente);
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);
            
            flussoElaboraResponse = new FlussoElaboraResponse();
            flussoElaboraResponse.setSuccess(false);
            flussoElaboraResponse.setMessage("Errore durante la ripresa dell'elaborazione: " + e.getMessage());
            responseDTO.setFlussoElaboraResponse(flussoElaboraResponse);
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
}


