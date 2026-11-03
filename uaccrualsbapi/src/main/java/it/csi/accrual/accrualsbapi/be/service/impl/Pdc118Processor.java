/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;
import it.csi.accrual.accrualcommonsrv.be.util.AccrualBusinessConstants;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbapi.be.exception.ConfirmationRequiredException;
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;


import it.csi.accrual.accrualsbapi.dto.FlussoElaboraResponse;
import it.csi.accrual.accrualsbapi.be.service.impl.dto.Pdc118SaldoFileRowDTO;
import it.csi.accrual.accrualsbapi.util.ElaborazioneUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Processore specifico per la gestione del CDU-008: "Elabora saldi PdC 118"
 * Implementa la logica di Discovery per eliminare la dipendenza da parametri hardcoded.
 * Allineato al documento di analisi V01.
 */
@Service
@Transactional
public class Pdc118Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Pdc118Processor.class);

    @Autowired
    private CsvReader csvReader;

    @Autowired
    private LogService logService;

    @Autowired
    private AccrualTPdcSaldoRepository pdcSaldoRepository;

    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;

    @Autowired
    private PrimaNotaServiceImpl primaNotaService;

    @Autowired
    private AccrualTPdcRepository accrualTPdcRepository;

    @Autowired
    private AccrualTCausaleRepository accrualTCausaleRepository;

    @Autowired
    private AccrualRPdcLegameRepository accrualRPdcLegameRepository;

    @Autowired
    private AccrualDPrimaNotaTipoRepository accrualDTipoPrimaNotaRepository;

    @Autowired
    private AccrualRPdcSaldoPrimaNotaRepository accrualRPdcSaldoPrimaNotaRepository;

    @Autowired
    private AccrualTEntitaContabileRepository accrualTEntitaContabileRepository;

    @Autowired
    private AccrualTParametroRepository parametroRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${batch-insert.pdc-saldo.size:50}")
    private int batchSizePdcSaldo;

    @Value("${batch-insert.pdc-saldo-prima-nota.size:50}")
    private int batchSizePdcSaldoPrimaNota;

    @Autowired
    private AccrualDFonteRepository fonteRepository;

    private enum SensoConto { DARE, AVERE }

    private String[] getCodiciCausaliApertura(AccrualTEnte ente) {
        LOG.debug("[getCodiciCausaliApertura] INIZIO - ente: {}, id: {}", ente.getCodiceUnivoco(), ente.getId());

        AccrualTParametro param = parametroRepository.findByParametroNomeAndEnteAndParametroAbilitatoTrue("CAUSALI_APERTURA_118", ente)
                .orElseThrow(() -> new IllegalArgumentException("Parametro CAUSALI_APERTURA_118 non configurato per l'ente " + ente.getCodiceUnivoco()));

        LOG.debug("[getCodiciCausaliApertura] Parametro trovato: id={}, nome={}, valore={}",
            param.getId(), param.getParametroNome(), param.getParametroValore());

        if (param.getParametroValore() == null) {
            throw new IllegalArgumentException("Il valore del parametro CAUSALI_APERTURA_118 è vuoto per l'ente " + ente.getCodiceUnivoco());
        }

        String[] codici = param.getParametroValore().split(",");
        LOG.debug("[getCodiciCausaliApertura] Codici causali estratti: {}", String.join(", ", codici));

        if (codici.length < 4) {
            throw new IllegalArgumentException("Il parametro CAUSALI_APERTURA_118 deve contenere 4 codici separati da virgola (Attivo, Passivo, Costi, Ricavi).");
        }
        return codici;
    }

    private AccrualTCausale getCausale(String valore, AccrualTEnte ente) {
        LOG.debug("[getCausale] INIZIO - valore: {}, ente: {}, id: {}", valore, ente.getCodiceUnivoco(), ente.getId());

        // 1. Prova a cercare per CODICE (esatto)
        List<AccrualTCausale> causaliByCod = accrualTCausaleRepository.findByCodiceAndEnte_IdAndFineValiditaIsNull(valore, ente.getId());
        LOG.debug("[getCausale] Causali trovate per codice: {}", causaliByCod != null ? causaliByCod.size() : 0);

        if (!causaliByCod.isEmpty()) {
            LOG.debug("[getCausale] Restituisco causale per codice: id={}, codice={}",
                causaliByCod.get(0).getId(), causaliByCod.get(0).getCodice());
            return causaliByCod.get(0);
        }

        // 2. Prova a cercare per DESCRIZIONE (esatto, case-insensitive)
        List<AccrualTCausale> causaliByDesc = accrualTCausaleRepository.findByDescrizioneAndEnte_IdAndFineValiditaIsNull(valore, ente.getId());
        LOG.debug("[getCausale] Causali trovate per descrizione: {}", causaliByDesc != null ? causaliByDesc.size() : 0);

        if (!causaliByDesc.isEmpty()) {
            LOG.debug("[getCausale] Restituisco causale per descrizione: id={}, descrizione={}",
                causaliByDesc.get(0).getId(), causaliByDesc.get(0).getDescrizione());
            return causaliByDesc.get(0);
        }

        throw new IllegalArgumentException("Causale con codice o descrizione '" + valore + "' non trovata per l'ente " + ente.getCodiceUnivoco() + " (ID: " + ente.getId() + ")");
    }

    private void validaContestoElaborazione(Pdc118SaldoFileRowDTO row, AccrualTEnte ente) {
        ElaborazioneUtil.validaContestoElaborazione(
            row.getCodiceFiscaleEnte(),
            row.getCodiceUnivocoEnte(),
            row.getFonte(),
            row.getAnno(),
            ente,
            fonteRepository
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlussoElaboraResponse processaSaldiPdc118(String flussoId, boolean confirmedOverride)
            throws NotFoundException, IllegalArgumentException, ConfirmationRequiredException, Exception {

        String codiceEnte = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("=================================================================================");
        LOG.info("[processaSaldiPdc118] INIZIO - flussoId: {}, codiceEnte: {}, confirmedOverride: {}", flussoId, codiceEnte, confirmedOverride);
        LOG.info("=================================================================================");

        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(flussoId))
            .orElseThrow(() -> new NotFoundException("Flusso con ID " + flussoId + " non trovato."));
        AccrualTEnte ente = file.getEnte();

        LOG.info("[processaSaldiPdc118] File trovato: id={}, nome={}, ente: {}", file.getId(), file.getNome(), ente.getCodiceUnivoco());

        FlussoElaboraResponse response = new FlussoElaboraResponse();
        response.setSuccess(false);

        try {
            // Step 1: Lettura file CSV
            LOG.info("[processaSaldiPdc118] Step 1: Lettura file CSV...");
            List<Pdc118SaldoFileRowDTO> saldiPdc118Rows = csvReader.readAndMapFile(flussoId, Pdc118SaldoFileRowDTO.class, false);
            LOG.info("[processaSaldiPdc118] Righe CSV lette: {}", saldiPdc118Rows != null ? saldiPdc118Rows.size() : 0);

            if (saldiPdc118Rows.isEmpty()) {
                throw new IllegalArgumentException("Il file CSV non contiene righe di dati valide.");
            }

            Pdc118SaldoFileRowDTO firstRow = saldiPdc118Rows.get(0);
            LOG.info("[processaSaldiPdc118] Prima riga - Anno: {}, Codice Univoco Ente: {}", firstRow.getAnno(), firstRow.getCodiceUnivocoEnte());

            // Step 1b: Validazione contesto elaborazione
            LOG.info("[processaSaldiPdc118] Step 1b: Validazione contesto elaborazione...");
            validaContestoElaborazione(firstRow, ente);
            LOG.info("[processaSaldiPdc118] Validazione contesto superata.");

            // Step 2: Trova entità contabile
            LOG.info("[processaSaldiPdc118] Step 2: Ricerca entità contabile...");
            List<AccrualTEntitaContabile> entitaContabili = accrualTEntitaContabileRepository.findByEnteAndAnno(ente, String.valueOf(firstRow.getAnno()));
            LOG.info("[processaSaldiPdc118] Entità contabili trovate: {}", entitaContabili != null ? entitaContabili.size() : 0);
            
            if (entitaContabili.isEmpty()) {
                throw new NotFoundException("Entità contabile non trovata per l'ente " + codiceEnte + " e anno " + firstRow.getAnno());
            }
            AccrualTEntitaContabile entitaContabile = entitaContabili.get(0);
            LOG.info("[processaSaldiPdc118] Entità contabile selezionata: id={}, fase: {}", entitaContabile.getId(), entitaContabile.getFase());

            if (entitaContabile.getFase() == TipoFase.PREVISIONE || entitaContabile.getFase() == TipoFase.CHIUSO) {
                throw new IllegalArgumentException("Impossibile elaborare i saldi: il bilancio per l'anno " + firstRow.getAnno() + " è in fase " + entitaContabile.getFase().name());
            }

            // Step 3: Trova conto di apertura patrimoniale
            LOG.info("[processaSaldiPdc118] Step 3: Ricerca conto di apertura patrimoniale...");
            List<AccrualTPdc> contiApertura = accrualTPdcRepository.findContoAperturaPatrimoniale(ente);
            LOG.info("[processaSaldiPdc118] Conti di apertura trovati: {}", contiApertura != null ? contiApertura.size() : 0);
            
            AccrualTPdc contoAperturaPatrimoniale = contiApertura.isEmpty() ? null : contiApertura.get(0);
            if (contoAperturaPatrimoniale != null) {
                LOG.info("[processaSaldiPdc118] Conto di apertura selezionato: id={}, codice={}, descrizione={}", 
                    contoAperturaPatrimoniale.getId(), contoAperturaPatrimoniale.getCodice(), contoAperturaPatrimoniale.getDescrizione());
            }

            if (contoAperturaPatrimoniale == null) {
                throw new IllegalArgumentException("Conto di apertura patrimoniale (99.01.01.01, 99.1.0.0.0 o APERTURA PATRIMONIALE) non trovato per l'ente " + ente.getCodiceUnivoco());
            }

            // Step 4: Gestione causali e prime note esistenti
            LOG.info("[processaSaldiPdc118] Step 4: Recupero codici causali apertura...");
            String[] codiciCausali = getCodiciCausaliApertura(ente);
            LOG.info("[processaSaldiPdc118] Codici causali: {}", String.join(", ", codiciCausali));

            LOG.info("[processaSaldiPdc118] Step 5: Ricerca prime note esistenti...");
            if (!confirmedOverride) {
                // Carica le entity complete solo per poterle passare alla ConfirmationRequiredException
                List<AccrualTPrimaNota> pnsEsistenti = primaNotaRepository.findByEnteAndAnnoAndCausaleCodiceIn(ente, firstRow.getAnno(), java.util.Arrays.asList(codiciCausali));
                List<AccrualTPrimaNota> pnsNonAnnullate = pnsEsistenti.stream()
                    .filter(pn -> pn.getStati().stream()
                        .filter(s -> s.getFineValidita() == null)
                        .noneMatch(s -> AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO.equals(s.getPrimaNotaStato().getCodice())))
                    .collect(Collectors.toList());
                LOG.info("[processaSaldiPdc118] Prime note non annullate: {}", pnsNonAnnullate.size());
                if (!pnsNonAnnullate.isEmpty()) {
                    throw new ConfirmationRequiredException("WEB_CON_0024", "Per l'anno " + firstRow.getAnno() + " esistono già le prime note di apertura patrimoniale e dei costi, ricavi. Si vuole procedere ad annullarle e a ricrearle?", pnsNonAnnullate);
                }
            } else {
                // Path confermato: query leggera — solo ID, nessun JOIN FETCH su entity intere
                List<Long> pnIdsNonAnnullate = primaNotaRepository.findIdsDiNonAnnullateByEnteAndAnnoAndCausaleCodiceIn(
                    ente, firstRow.getAnno(), java.util.Arrays.asList(codiciCausali), AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO);
                LOG.info("[processaSaldiPdc118] Prime note non annullate (IDs): {}", pnIdsNonAnnullate.size());
                if (!pnIdsNonAnnullate.isEmpty()) {
                    LOG.info("[processaSaldiPdc118] confirmedOverride=true, procedo con annullamento bulk...");
                    annullaPrimeNoteAndSaldi(file, ente, firstRow.getAnno(), pnIdsNonAnnullate);
                }
            }

            // Step 6: Caricamento Saldi
            LOG.info("[processaSaldiPdc118] Step 6: Caricamento saldi PdC 118...");
            List<AccrualTPdcSaldo> saldiSalvati = elaboraSaldiPdc118(file, ente, saldiPdc118Rows);
            LOG.info("[processaSaldiPdc118] Saldi salvati: {}", saldiSalvati.size());

            if (saldiSalvati.isEmpty()) {
                throw new IllegalArgumentException("Nessun saldo è stato caricato. Verificare che i conti PdC 118 indicati siano censiti a sistema per l'anno " + firstRow.getAnno());
            }

            // Step 7: Generazione Prime Note
            LOG.info("[processaSaldiPdc118] Step 7: Generazione prime note di apertura...");
            generaPrimeNoteApertura(file, ente, saldiPdc118Rows, contoAperturaPatrimoniale, saldiSalvati);

            // Log dettagliato finale con statistiche
            logService.registraInfo(file, "Elaborazione completata con successo",
                "Saldi caricati: " + saldiSalvati.size() +
                " | Anno esercizio: " + firstRow.getAnno() +
                " | Ente: " + ente.getCodiceUnivoco());

            LOG.info("[processaSaldiPdc118] COMPLETATO CON SUCCESSO - Saldi: {}", saldiSalvati.size());
            response.setSuccess(true);
            response.setMessage("Elaborazione CDU-008 completata con successo. Caricati " + saldiSalvati.size() + " saldi.");
            return response;

        } catch (IncorrectResultSizeDataAccessException e) {
            LOG.error("[processaSaldiPdc118] ERRORE DATABASE - Query non restituisce risultato unico: {}", e.getMessage(), e);
            logService.registraErrore(file, "Errore elaborazione CDU-008", "Query non restituisce risultato unico: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("[processaSaldiPdc118] ERRORE GENERALE - {}", e.getMessage(), e);
            logService.registraErrore(file, "Errore elaborazione CDU-008", e.getMessage());
            throw e;
        }
    }

    private void annullaPrimeNoteAndSaldi(AccrualTFile file, AccrualTEnte ente, Integer anno, List<Long> pnIds) {
        if (pnIds.isEmpty()) {
            LOG.info("[annullaPrimeNoteAndSaldi] Nessuna prima nota da annullare");
            return;
        }

        String utente = ElaborazioneUtil.getUtenteCorrente();
        LocalDateTime ora = LocalDateTime.now();

        // 1. Invalida i saldi (Ref. 2.6.3 punto 1)
        List<AccrualTPdcSaldo> saldi = pdcSaldoRepository.findByEnteAndAnno(ente, anno);
        saldi.forEach(s -> { s.setDataCancellazione(ora); s.setUtenteCancellazione(utente); });
        pdcSaldoRepository.saveAll(saldi);

        // 2. Bulk annullamento prime note: 1 UPDATE + batch INSERT invece di N×5 query (Ref. 2.6.3 punto 2)
        primaNotaService.bulkAnnullaPrimeNote(pnIds, ente, utente);

        // 3. Invalida i legami SaldoConto-PrimaNota (Ref. 2.6.3 punto 3)
        List<AccrualRPdcSaldoPrimaNota> legami = accrualRPdcSaldoPrimaNotaRepository.findByPrimaNotaIdInAndPdcSaldoIn(pnIds, saldi);
        legami.forEach(l -> { l.setDataCancellazione(ora); l.setUtenteCancellazione(utente); });
        accrualRPdcSaldoPrimaNotaRepository.saveAll(legami);

        logService.registraInfo(file, "Annullamento completato", "Annullate " + pnIds.size() + " prime note e relativi saldi per l'esercizio " + anno);
    }

    private List<AccrualTPdcSaldo> elaboraSaldiPdc118(AccrualTFile file, AccrualTEnte ente, List<Pdc118SaldoFileRowDTO> rows) {
        String utente = ElaborazioneUtil.getUtenteCorrente();
        LocalDateTime now = LocalDateTime.now();

        // Bulk load all PDC records in one query
        List<String> codiciNonZero = rows.stream()
                .filter(r -> r.getImporto() != null && r.getImporto().compareTo(java.math.BigDecimal.ZERO) != 0)
                .map(Pdc118SaldoFileRowDTO::getCodiceConto118)
                .distinct()
                .collect(Collectors.toList());
        List<AccrualTPdc> pdcList = accrualTPdcRepository.findByCodiceInAndEnteId(codiciNonZero, ente.getId());
        Map<String, AccrualTPdc> pdcMap = new java.util.HashMap<>();
        for (AccrualTPdc pdc : pdcList) {
            pdcMap.putIfAbsent(pdc.getCodice(), pdc);
            if (pdc.getCodiceUfficiale() != null) pdcMap.putIfAbsent(pdc.getCodiceUfficiale(), pdc);
        }

        List<AccrualTPdcSaldo> daInserire = new ArrayList<>(codiciNonZero.size());
        List<String> codiciNonTrovati = new ArrayList<>();
        for (Pdc118SaldoFileRowDTO row : rows) {
            if (row.getImporto() == null || row.getImporto().compareTo(java.math.BigDecimal.ZERO) == 0) {
                LOG.debug("elaboraSaldiPdc118 - Riga scartata per importo zero: conto={}", row.getCodiceConto118());
                continue;
            }
            AccrualTPdc pdc118 = pdcMap.get(row.getCodiceConto118());
            if (pdc118 != null) {
                AccrualTPdcSaldo saldo = new AccrualTPdcSaldo();
                saldo.setEnte(ente);
                saldo.setAnno(row.getAnno());
                saldo.setCodice(row.getCodiceConto118());
                saldo.setPdc(pdc118);
                saldo.setImporto(row.getImporto());
                saldo.setDataInserimento(now);
                saldo.setUtenteInserimento(utente);
                daInserire.add(saldo);
            } else {
                codiciNonTrovati.add(row.getCodiceConto118());
            }
        }
        if (!codiciNonTrovati.isEmpty()) {
            logService.registraErrore(file, "Righe scartate: codice PdC non trovato",
                codiciNonTrovati.size() + " conti PdC 118 non trovati nel sistema per ente " + ente.getCodiceUnivoco() +
                ": " + String.join(", ", codiciNonTrovati));
        }

        for (int i = 0; i < daInserire.size(); i++) {
            entityManager.persist(daInserire.get(i));
            if ((i + 1) % batchSizePdcSaldo == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();
        LOG.info("elaboraSaldiPdc118 - Salvati {} saldi in batch ({} righe scartate)", daInserire.size(), rows.size() - daInserire.size());
        return daInserire;
    }

    private void generaPrimeNoteApertura(AccrualTFile file, AccrualTEnte ente, List<Pdc118SaldoFileRowDTO> rows, AccrualTPdc contoApertura, List<AccrualTPdcSaldo> saldiSalvati) throws Exception {
        LOG.info("Avvio generazione Prime Note di Apertura per ente: {}", ente.getCodiceUnivoco());
        String utente = ElaborazioneUtil.getUtenteCorrente();
        String[] codici = getCodiciCausaliApertura(ente);

        AccrualTCausale cAttivo = getCausale(codici[0].trim(), ente);
        AccrualTCausale cPassivo = getCausale(codici[1].trim(), ente);
        AccrualTCausale cCosti = getCausale(codici[2].trim(), ente);
        AccrualTCausale cRicavi = getCausale(codici[3].trim(), ente);

        // Pre-load shared data once for all 4 groups
        AccrualDPrimaNotaTipo tipo = primaNotaService.getTipoPrimaNotaAutomatica();
        AccrualDAttributo attributo118 = primaNotaService.getAttributoByCodice("CONTO118", ente.getId());

        processaGruppoPerSingoloSaldo(file, ente, rows, cAttivo, AccrualBusinessConstants.CLASSE_ATTIVO, SensoConto.DARE, contoApertura, utente, saldiSalvati, tipo, attributo118);
        processaGruppoPerSingoloSaldo(file, ente, rows, cPassivo, AccrualBusinessConstants.CLASSE_PASSIVO, SensoConto.AVERE, contoApertura, utente, saldiSalvati, tipo, attributo118);
        processaGruppoPerSingoloSaldo(file, ente, rows, cCosti, AccrualBusinessConstants.CLASSE_COSTI, SensoConto.DARE, contoApertura, utente, saldiSalvati, tipo, attributo118);
        processaGruppoPerSingoloSaldo(file, ente, rows, cRicavi, AccrualBusinessConstants.CLASSE_RICAVI, SensoConto.AVERE, contoApertura, utente, saldiSalvati, tipo, attributo118);
    }

    private void processaGruppoPerSingoloSaldo(AccrualTFile file, AccrualTEnte ente, List<Pdc118SaldoFileRowDTO> rows,
                               AccrualTCausale causale, String classe, SensoConto senso, AccrualTPdc contoApertura,
                               String utente, List<AccrualTPdcSaldo> saldiSalvati,
                               AccrualDPrimaNotaTipo tipo, AccrualDAttributo attributo118) throws Exception {

        Map<String, AccrualTPdcSaldo> saldiMap = saldiSalvati.stream().collect(Collectors.toMap(AccrualTPdcSaldo::getCodice, s -> s, (s1, s2) -> s1));

        List<Pdc118SaldoFileRowDTO> filtrati = new ArrayList<>();
        for (Pdc118SaldoFileRowDTO r : rows) {
            if (saldiMap.containsKey(r.getCodiceConto118())) {
                AccrualTPdc pdc = saldiMap.get(r.getCodiceConto118()).getPdc();
                if (pdc.getPdcClasse().getCodice().equals(classe)) {
                    filtrati.add(r);
                }
            }
        }

        if (filtrati.isEmpty()) {
            LOG.info("Nessuna riga trovata per la classe {} e causale {}", classe, causale.getCodice());
            return;
        }

        Integer anno = filtrati.get(0).getAnno();

        // 1. Bulk load all legami for this group in one query
        List<Long> pdcIds = filtrati.stream()
                .map(r -> saldiMap.get(r.getCodiceConto118()).getPdc().getId())
                .distinct()
                .collect(Collectors.toList());
        List<AccrualRPdcLegame> tuttiLegami = accrualRPdcLegameRepository.findByPdcIdInAndTipoRelazioneCodiceAndEnteId(
                pdcIds, AccrualBusinessConstants.TIPO_RELAZIONE_CORRELAZIONE, ente.getId());
        Map<Long, AccrualTPdc> pdcIdToAccrual = tuttiLegami.stream()
                .collect(Collectors.toMap(l -> l.getPdcLegameA().getId(), l -> l.getPdcLegameB(), (a, b) -> a));

        // 2. Determine initial stato from causale (once)
        AccrualDPrimaNotaStato statoIniziale;
        if (causale.getEvento() != null && causale.getEvento().getPrimaNotaStato() != null) {
            statoIniziale = causale.getEvento().getPrimaNotaStato();
        } else {
            statoIniziale = primaNotaService.getStatoPrimaNotaByCodice(AccrualBusinessConstants.STATO_PRIMA_NOTA_PROVVISORIA);
        }

        // 3. Filter to rows that have a valid legame
        List<Pdc118SaldoFileRowDTO> valide = new ArrayList<>(filtrati.size());
        List<String> codiciSenzaLegame = new ArrayList<>();
        for (Pdc118SaldoFileRowDTO row : filtrati) {
            Long pdcId = saldiMap.get(row.getCodiceConto118()).getPdc().getId();
            if (pdcIdToAccrual.containsKey(pdcId)) {
                valide.add(row);
            } else {
                codiciSenzaLegame.add(row.getCodiceConto118());
            }
        }
        if (!codiciSenzaLegame.isEmpty()) {
            logService.registraErrore(file, "Righe scartate: conto Accrual correlato non trovato",
                codiciSenzaLegame.size() + " conti PdC 118 senza legame Accrual per classe " + classe +
                " (ente " + ente.getCodiceUnivoco() + "): " + String.join(", ", codiciSenzaLegame));
        }

        if (valide.isEmpty()) {
            return;
        }

        // 4. Reserve all sequential numbers in one DB roundtrip
        int primoNumero = primaNotaService.reserveNumeriProvvisori(ente, anno, valide.size());

        boolean isDefinitivo = AccrualBusinessConstants.STATO_PRIMA_NOTA_DEFINITIVA.equals(statoIniziale.getCodice());
        int primoNumeroDefinitivo = isDefinitivo
                ? primaNotaService.reserveNumeriDefinitivi(ente, anno, valide.size())
                : 0;

        // 5. Build all prima nota entities in memory
        List<AccrualTPrimaNota> primeNoteInMemoria = new ArrayList<>(valide.size());
        List<SensoConto> sensiEffettivi = new ArrayList<>(valide.size());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < valide.size(); i++) {
            Pdc118SaldoFileRowDTO row = valide.get(i);
            boolean saldoNegativo = row.getImporto().compareTo(BigDecimal.ZERO) < 0;
            sensiEffettivi.add(saldoNegativo ? (senso == SensoConto.DARE ? SensoConto.AVERE : SensoConto.DARE) : senso);
            String descrizione = causale.getDescrizione() + " ESERCIZIO " + anno + " SALDO " + row.getCodiceConto118();
            AccrualTPrimaNota pn = primaNotaService.buildPrimaNota(ente, causale, file, primoNumero + i, anno, descrizione, tipo);
            if (isDefinitivo) {
                pn.setProgressivoGiornale(primoNumeroDefinitivo + i);
                pn.setDataRegistrazioneGiornale(now);
            }
            primeNoteInMemoria.add(pn);
        }

        // 6. Batch save prima note → get IDs back
        List<AccrualTPrimaNota> pnSalvate = primaNotaService.salvaAll(primeNoteInMemoria);

        // 7. Build and batch save stati (no existing stati to close for new PN)
        List<AccrualRPrimaNotaStato> stati = new ArrayList<>(pnSalvate.size());
        for (AccrualTPrimaNota pn : pnSalvate) {
            stati.add(primaNotaService.buildStatoRecord(pn, statoIniziale, ente, utente));
        }
        primaNotaService.salvaStatiAll(stati);

        // 8. Build all importi in memory (2 per PN: pdcAccrual + contoApertura)
        List<AccrualTPrimaNotaImporto> tuttiImporti = new ArrayList<>(pnSalvate.size() * 2);
        List<Integer> indiciImportoConto = new ArrayList<>(pnSalvate.size());
        for (int i = 0; i < pnSalvate.size(); i++) {
            AccrualTPrimaNota pn = pnSalvate.get(i);
            Pdc118SaldoFileRowDTO row = valide.get(i);
            BigDecimal importoAssoluto = row.getImporto().abs();
            Long pdcId = saldiMap.get(row.getCodiceConto118()).getPdc().getId();
            AccrualTPdc pdcAccrual = pdcIdToAccrual.get(pdcId);
            indiciImportoConto.add(tuttiImporti.size());
            if (sensiEffettivi.get(i) == SensoConto.DARE) {
                tuttiImporti.add(primaNotaService.buildImporto(pn, pdcAccrual, importoAssoluto, PrimaNotaServiceImpl.TipoImporto.DARE));
                tuttiImporti.add(primaNotaService.buildImporto(pn, contoApertura, importoAssoluto, PrimaNotaServiceImpl.TipoImporto.AVERE));
            } else {
                tuttiImporti.add(primaNotaService.buildImporto(pn, pdcAccrual, importoAssoluto, PrimaNotaServiceImpl.TipoImporto.AVERE));
                tuttiImporti.add(primaNotaService.buildImporto(pn, contoApertura, importoAssoluto, PrimaNotaServiceImpl.TipoImporto.DARE));
            }
        }

        // 9. Batch save importi → get IDs back
        List<AccrualTPrimaNotaImporto> importiSalvati = primaNotaService.salvaImportiAll(tuttiImporti);

        // 10. Build and batch save attributi (1 per PN, linked to importoConto)
        List<AccrualRPrimaNotaAttributo> attributi = new ArrayList<>(pnSalvate.size());
        for (int i = 0; i < pnSalvate.size(); i++) {
            AccrualTPrimaNotaImporto importoConto = importiSalvati.get(indiciImportoConto.get(i));
            attributi.add(primaNotaService.buildAttributo(pnSalvate.get(i), importoConto, attributo118, valide.get(i).getCodiceConto118()));
        }
        primaNotaService.salvaAttributiAll(attributi);

        // 11. Batch save legami saldo-PN
        List<AccrualRPdcSaldoPrimaNota> legamiDaSalvare = new ArrayList<>(pnSalvate.size());
        for (int i = 0; i < pnSalvate.size(); i++) {
            AccrualRPdcSaldoPrimaNota legame = new AccrualRPdcSaldoPrimaNota();
            legame.setEnte(ente);
            legame.setPrimaNota(pnSalvate.get(i));
            legame.setPdcSaldo(saldiMap.get(valide.get(i).getCodiceConto118()));
            legame.setInizioValidita(now);
            legamiDaSalvare.add(legame);
        }
        for (int i = 0; i < legamiDaSalvare.size(); i++) {
            entityManager.persist(legamiDaSalvare.get(i));
            if ((i + 1) % batchSizePdcSaldoPrimaNota == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();

        LOG.info("Completata generazione prime note per classe {} e causale {} — {} PN create in batch",
            classe, causale.getCodice(), pnSalvate.size());
    }


}
