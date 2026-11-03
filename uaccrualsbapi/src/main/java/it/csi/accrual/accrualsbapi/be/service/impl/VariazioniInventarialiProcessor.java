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
import it.csi.accrual.accrualsbapi.be.service.impl.dto.VariazioniInventarialiFileRowDTO;
import it.csi.accrual.accrualsbapi.util.ElaborazioneUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processore specifico per la gestione del CDU-009: "Elabora variazioni inventariali"
 * Implementa la logica di Discovery e legame Etichetta-PrimaNota.
 * Allineato al documento di analisi V01.
 */
@Service
@Transactional
public class VariazioniInventarialiProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(VariazioniInventarialiProcessor.class);

    @Autowired
    private CsvReader csvReader;

    @Autowired
    private LogService logService;

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;

    @Autowired
    private AccrualTEtichettaRepository accrualTEtichettaRepository;

    @Autowired
    private PrimaNotaServiceImpl primaNotaService;

    @Autowired
    private AccrualTPdcRepository accrualTPdcRepository;

    @Autowired
    private AccrualTCausaleRepository accrualTCausaleRepository;

    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;

    @Autowired
    private AccrualREtichettaPrimaNotaRepository accrualREtichettaPrimaNotaRepository;

    @Autowired
    private AccrualDPrimaNotaTipoRepository accrualDTipoPrimaNotaRepository;

    @Autowired
    private AccrualTParametroRepository parametroRepository;

    @Autowired
    private AccrualRPdcSaldoPrimaNotaRepository accrualRPdcSaldoPrimaNotaRepository;

    @Autowired
    private AccrualTPdcSaldoRepository accrualTPdcSaldoRepository;

    @Autowired
    private AccrualDFonteRepository accrualDFonteRepository;

    private enum SensoConto { DARE, AVERE }

    private void validaContestoElaborazione(VariazioniInventarialiFileRowDTO row, AccrualTEnte ente) {
        ElaborazioneUtil.validaContestoElaborazione(
            row.getCodiceFiscaleEnte(),
            row.getCodiceUnivocoEnte(),
            row.getFonte(),
            row.getAnno(),
            ente,
            accrualDFonteRepository
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlussoElaboraResponse processaVariazioniInventariali(String flussoId, boolean confirmedOverride)
            throws NotFoundException, IllegalArgumentException, ConfirmationRequiredException, Exception {

        String codiceEnte = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("Inizio elaborazione variazioni inventariali per flusso ID: {} e ente: {}", flussoId, codiceEnte);

        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(flussoId))
            .orElseThrow(() -> new NotFoundException("Flusso con ID " + flussoId + " non trovato."));
        AccrualTEnte ente = file.getEnte();

        FlussoElaboraResponse response = new FlussoElaboraResponse();
        response.setSuccess(false);

        try {
            List<VariazioniInventarialiFileRowDTO> rows = csvReader.readAndMapFile(flussoId, VariazioniInventarialiFileRowDTO.class, false);

            if (rows.isEmpty()) throw new IllegalArgumentException("Il file CSV è vuoto.");
            
            VariazioniInventarialiFileRowDTO firstRow = rows.get(0);

            LOG.info("[processaVariazioniInventariali] Validazione contesto elaborazione...");
            validaContestoElaborazione(firstRow, ente);
            LOG.info("[processaVariazioniInventariali] Validazione contesto superata.");

            // 1. Verifica presenza Apertura Patrimoniale (Ref. 2.6.1.2)
            // Recupero il primo codice dal parametro (corrispondente all'Attivo) per la verifica
            LOG.info("Recupero parametro CAUSALI_APERTURA_118 per ente ID: {}", ente.getId());
            AccrualTParametro paramApertura = parametroRepository.findByParametroNomeAndEnteAndParametroAbilitatoTrue("CAUSALI_APERTURA_118", ente)
                    .orElseThrow(() -> new IllegalArgumentException("Parametro CAUSALI_APERTURA_118 non configurato per l'ente."));
            String codiceCausaleAttivo = paramApertura.getParametroValore().split(",")[0].trim();

            LOG.info("Verifica presenza Apertura Patrimoniale per ente ID: {}, anno: {}, causale: {}", ente.getId(), firstRow.getAnno(), codiceCausaleAttivo);
            if (primaNotaRepository.findByEnteAndAnnoAndCausaleCodiceIn(ente, firstRow.getAnno(), List.of(codiceCausaleAttivo)).isEmpty()) {
                LOG.warn("Per l'anno " + firstRow.getAnno() + " non è stata registrata la prima nota di Apertura Patrimoniale (Causale " + codiceCausaleAttivo + "). Si procede comunque per test.");
            }

            // 2. Gestione rettifiche esistenti (Ref. 2.6.1.3)
            LOG.info("Ricerca prime note esistenti per rettifica inventario - ente ID: {}, anno: {}", ente.getId(), firstRow.getAnno());
            List<AccrualTPrimaNota> pnsEsistenti = primaNotaRepository.findByEnteAndAnnoAndEventoCodice(ente, firstRow.getAnno(), AccrualBusinessConstants.EVENTO_RETTIFICA_INVENTARIO);
            List<AccrualTPrimaNota> pnsAttive = pnsEsistenti.stream()
                .filter(pn -> pn.getStati().stream()
                    .filter(s -> s.getFineValidita() == null)
                    .noneMatch(s -> AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO.equals(s.getPrimaNotaStato().getCodice())))
                .collect(Collectors.toList());
            if (!pnsAttive.isEmpty()) {
                if (!confirmedOverride) {
                    throw new ConfirmationRequiredException("WEB_CON_0026", "Esistono già prime note di rettifica per l'anno " + firstRow.getAnno() + ". Annullarle e ricrearle?");
                } else {
                    annullaPrimeNoteAndEtichette(file, ente, firstRow.getAnno(), pnsAttive);
                }
            }

            // 3. Salvataggio Etichette (Ref. 2.6.2)
            LOG.info("Salvataggio etichette per flusso ID: {}", flussoId);
            elaboraEtichette(file, ente, rows);

            // 4. Generazione Prime Note (Ref. 2.6.2.1 - 2.6.2.5)
            LOG.info("Generazione prime note di variazione per flusso ID: {}", flussoId);
            generaPrimeNoteVariazione(file, ente, rows);

            // Log dettagliato finale con statistiche
            logService.registraInfo(file, "Elaborazione completata con successo",
                "Righe elaborate: " + rows.size() +
                " | Anno esercizio: " + firstRow.getAnno() +
                " | Ente: " + ente.getCodiceUnivoco());
            
            LOG.info("Elaborazione CDU-009 COMPLETATA - Righe: {}", rows.size());
            response.setSuccess(true);
            response.setMessage("Elaborazione CDU-009 completata.");
            return response;

        } catch (Exception e) {
            logService.registraErrore(file, "Errore CDU-009", e.getMessage());
            throw e;
        }
    }

    private void annullaPrimeNoteAndEtichette(AccrualTFile file, AccrualTEnte ente, Integer anno, List<AccrualTPrimaNota> pns) {
        List<AccrualTPrimaNota> pnsDaAnnullare = ElaborazioneUtil.filtraPrimeNoteNonAnnullate(pns);

        if (pnsDaAnnullare.isEmpty()) {
            LOG.info("[annullaPrimeNoteAndEtichette] Nessuna prima nota da annullare (tutte già in stato Annullato)");
            return;
        }

        String utente = ElaborazioneUtil.getUtenteCorrente();
        LocalDateTime ora = LocalDateTime.now();

        // Annulla Etichette
        List<AccrualTEtichetta> etichette = accrualTEtichettaRepository.findByEnteAndAnno(ente, anno);
        etichette.forEach(e -> { e.setDataCancellazione(ora); e.setUtenteCancellazione(utente); });
        accrualTEtichettaRepository.saveAll(etichette);

        // Annulla Prime Note
        ElaborazioneUtil.annullaPrimeNote(pnsDaAnnullare, utente, ente, primaNotaService);

        // Annulla Legami (Ref. 2.6.3)
        List<AccrualREtichettaPrimaNota> legami = accrualREtichettaPrimaNotaRepository.findByPrimaNotaIn(pnsDaAnnullare);
        legami.forEach(l -> {
            l.setDataCancellazione(ora);
            l.setUtenteCancellazione(utente);
        });
        accrualREtichettaPrimaNotaRepository.saveAll(legami);

        logService.registraInfo(file, "Annullamento completato", "Annullate etichette e prime note di rettifica per l'anno " + anno);
    }

    private void elaboraEtichette(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> rows) {
        String utente = ElaborazioneUtil.getUtenteCorrente();
        AccrualDFonte fonte = (file.getFileTipo() != null) ? file.getFileTipo().getFonte() : null;
        LocalDateTime now = LocalDateTime.now();

        List<AccrualTEtichetta> etichette = new java.util.ArrayList<>(rows.size());
        for (VariazioniInventarialiFileRowDTO row : rows) {
            AccrualTEtichetta asset = new AccrualTEtichetta();
            asset.setEnte(ente);
            asset.setAnno(row.getAnno());
            asset.setCodice(row.getNumeroInventarioBene());
            asset.setDescrizione(row.getDescrizioneBene());
            asset.setCodiceContoStorico(row.getCodiceContoPatrimonialeInizialeBene());
            asset.setDescrizioneContoStorico(row.getDescrizioneContoPatrimonialeInizialeBene());
            asset.setValoreContoStorico(row.getValoreRealeInizialeBeneBigDecimal());
            asset.setCodiceContoCorrente(row.getCodiceContoPatrimonialeAttualeBene());
            asset.setDescrizioneContoCorrente(row.getDescrizioneContoPatrimonialeAttualeBene());
            asset.setValoreContoCorrente(row.getValoreRealeAttualeBeneBigDecimal());
            asset.setFonte(fonte);
            asset.setDataInserimento(now);
            asset.setUtenteInserimento(utente);
            etichette.add(asset);
        }
        accrualTEtichettaRepository.saveAll(etichette);
        LOG.info("elaboraEtichette - Salvate {} etichette in batch", etichette.size());
    }

    private void generaPrimeNoteVariazione(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> rows) throws Exception {
        String utente = ElaborazioneUtil.getUtenteCorrente();
        Integer annoInt = rows.get(0).getAnno();

        // Ref. Discovery: Cerca il conto Fondo di Dotazione tramite flag pdc_fondo_dotazione=true (indipendente dal codice)
        AccrualTPdc contoFondoDotazione = accrualTPdcRepository.findFondoDotazione(ente)
                .orElseThrow(() -> new IllegalArgumentException("Conto Fondo di Dotazione non trovato per l'ente (verificare flag pdc_fondo_dotazione)."));

        AccrualDPrimaNotaTipo tipoPn = accrualDTipoPrimaNotaRepository.findByCodice(AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA)
                .orElseThrow(() -> new IllegalArgumentException("Tipo di Prima Nota '" + AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA + "' non configurato nel sistema (tabella accrual_d_prima_nota_tipo)."));

        // Conta le righe elaborate per tipo
        List<VariazioniInventarialiFileRowDTO> g1 = rows.stream().filter(r -> !r.getCodiceContoPatrimonialeInizialeBene().equals(r.getCodiceContoPatrimonialeAttualeBene()) && r.getValoreRealeInizialeBeneBigDecimal().compareTo(r.getValoreRealeAttualeBeneBigDecimal()) == 0).collect(Collectors.toList());
        List<VariazioniInventarialiFileRowDTO> g2 = rows.stream().filter(r -> r.getCodiceContoPatrimonialeInizialeBene().equals(r.getCodiceContoPatrimonialeAttualeBene()) && r.getValoreRealeInizialeBeneBigDecimal().compareTo(r.getValoreRealeAttualeBeneBigDecimal()) != 0).collect(Collectors.toList());
        List<VariazioniInventarialiFileRowDTO> g3 = rows.stream().filter(r -> !r.getCodiceContoPatrimonialeInizialeBene().equals(r.getCodiceContoPatrimonialeAttualeBene()) && r.getValoreRealeInizialeBeneBigDecimal().compareTo(r.getValoreRealeAttualeBeneBigDecimal()) != 0).collect(Collectors.toList());

        LOG.info("Variazioni inventariali - Righe totali: {}, Cambio Conto: {}, Cambio Valore: {}, Cambio Conto+Valore: {}",
            rows.size(), g1.size(), g2.size(), g3.size());

        // Lista condivisa per tenere traccia di tutte le prime note create
        List<String> numeriPrimeNote = new ArrayList<>();

        // 1. Cambio Conto (Senza cambio valore)
        processaCambioConto(file, ente, g1, "RETT_CLASS", tipoPn, utente, annoInt, numeriPrimeNote);

        // 2. Cambio Valore (Senza cambio conto)
        processaCambioValore(file, ente, g2, "RETT_VAL", tipoPn, utente, contoFondoDotazione, annoInt, numeriPrimeNote);

        // 3. Cambio Conto e Valore
        processaCambioContoValore(file, ente, g3, "RETT_CLASS_E_VAL", tipoPn, utente, contoFondoDotazione, annoInt, numeriPrimeNote);

        // Log finale con tutte le prime note create
        if (!numeriPrimeNote.isEmpty()) {
            LOG.info("In questa elaborazione sono state inserite le prime note {}", String.join(",", numeriPrimeNote));
            // Registra nel log dell'elaborazione
            logService.registraInfo(file, "Elaborazione completata",
                "Inserite prime note numeri: [" + String.join(",", numeriPrimeNote) + "] per l'esercizio " + annoInt);
        }
    }

    private void processaCambioConto(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> list, String causaleCod, AccrualDPrimaNotaTipo tipoPn, String utente, Integer anno, List<String> numeriPrimeNote) throws Exception {
        if (list.isEmpty()) return;
        LOG.info("processaCambioConto - Ricerca causale per codice: {} ed ente ID: {}", causaleCod, ente.getId());
        AccrualTCausale causale = accrualTCausaleRepository.findByCodiceAndEnte_Id(causaleCod, ente.getId());
        if (causale == null) throw new IllegalArgumentException("Causale " + causaleCod + " non trovata.");

        for (java.util.Map.Entry<String, List<VariazioniInventarialiFileRowDTO>> entry : list.stream().collect(Collectors.groupingBy(r -> r.getCodiceContoPatrimonialeInizialeBene() + "-" + r.getCodiceContoPatrimonialeAttualeBene())).entrySet()) {
            List<VariazioniInventarialiFileRowDTO> subList = entry.getValue();
            BigDecimal somma = subList.stream().map(VariazioniInventarialiFileRowDTO::getValoreRealeAttualeBeneBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
            AccrualTPrimaNota pn = creaPNSalvaLegami(file, ente, subList, causale, tipoPn, utente);

            LOG.info("processaCambioConto - Ricerca conto patrimoniale iniziale per codice: {}, anno: {}, ente ID: {}", subList.get(0).getCodiceContoPatrimonialeInizialeBene(), anno, ente.getId());
            AccrualTPdc cIniziale = accrualTPdcRepository.findByCodiceAndAnnoAndEnteId(subList.get(0).getCodiceContoPatrimonialeInizialeBene(), anno, ente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conto patrimoniale iniziale " + subList.get(0).getCodiceContoPatrimonialeInizialeBene() + " non trovato per l'anno " + anno));

            LOG.info("processaCambioConto - Ricerca conto patrimoniale attuale per codice: {}, anno: {}, ente ID: {}", subList.get(0).getCodiceContoPatrimonialeAttualeBene(), anno, ente.getId());
            AccrualTPdc cAttuale = accrualTPdcRepository.findByCodiceAndAnnoAndEnteId(subList.get(0).getCodiceContoPatrimonialeAttualeBene(), anno, ente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conto patrimoniale attuale " + subList.get(0).getCodiceContoPatrimonialeAttualeBene() + " non trovato per l'anno " + anno));

            AccrualTPrimaNotaImporto importoY = primaNotaService.aggiungiImporto(pn, cAttuale, somma, PrimaNotaServiceImpl.TipoImporto.DARE);
            primaNotaService.aggiungiImporto(pn, cIniziale, somma, PrimaNotaServiceImpl.TipoImporto.AVERE);
            salvaAttributiInventario(pn, ente, subList, importoY);
            primaNotaService.finalizzaPrimaNota(pn, utente);
            numeriPrimeNote.add(String.valueOf(pn.getNumero()));
            LOG.info("processaCambioConto - Inserita prima nota {}", pn.getNumero());
            // Registra nel log dell'elaborazione
            logService.registraInfo(file, "Inserita PN " + pn.getNumero(),
                "Inserita prima nota " + pn.getNumero() + " causale " + causaleCod);
        }
    }

    private void processaCambioValore(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> list, String causaleCod, AccrualDPrimaNotaTipo tipoPn, String utente, AccrualTPdc fondo, Integer anno, List<String> numeriPrimeNote) throws Exception {
        if (list.isEmpty()) return;
        LOG.info("processaCambioValore - Ricerca causale per codice: {} ed ente ID: {}", causaleCod, ente.getId());
        AccrualTCausale causale = accrualTCausaleRepository.findByCodiceAndEnte_Id(causaleCod, ente.getId());
        if (causale == null) throw new IllegalArgumentException("Causale " + causaleCod + " non trovata.");

        // Raggruppa per conto e separa delta positivi e negativi
        for (java.util.Map.Entry<String, List<VariazioniInventarialiFileRowDTO>> entry : list.stream().collect(Collectors.groupingBy(VariazioniInventarialiFileRowDTO::getCodiceContoPatrimonialeAttualeBene)).entrySet()) {
            List<VariazioniInventarialiFileRowDTO> subList = entry.getValue();
            
            // Separa i beni con delta positivo da quelli con delta negativo
            List<VariazioniInventarialiFileRowDTO> deltaPositivi = subList.stream()
                .filter(r -> r.getValoreRealeAttualeBeneBigDecimal().subtract(r.getValoreRealeInizialeBeneBigDecimal()).compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
            
            List<VariazioniInventarialiFileRowDTO> deltaNegativi = subList.stream()
                .filter(r -> r.getValoreRealeAttualeBeneBigDecimal().subtract(r.getValoreRealeInizialeBeneBigDecimal()).compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toList());

            LOG.info("processaCambioValore - Ricerca conto patrimoniale attuale per codice: {}, anno: {}, ente ID: {}", entry.getKey(), anno, ente.getId());
            AccrualTPdc cAttuale = accrualTPdcRepository.findByCodiceAndAnnoAndEnteId(entry.getKey(), anno, ente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conto patrimoniale attuale " + entry.getKey() + " non trovato per l'anno " + anno));

            // Processa delta positivi
            if (!deltaPositivi.isEmpty()) {
                BigDecimal deltaPos = deltaPositivi.stream()
                    .map(r -> r.getValoreRealeAttualeBeneBigDecimal().subtract(r.getValoreRealeInizialeBeneBigDecimal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                AccrualTPrimaNota pnPos = creaPNSalvaLegami(file, ente, deltaPositivi, causale, tipoPn, utente);
                AccrualTPrimaNotaImporto importoYPos = primaNotaService.aggiungiImporto(pnPos, cAttuale, deltaPos, PrimaNotaServiceImpl.TipoImporto.DARE);
                primaNotaService.aggiungiImporto(pnPos, fondo, deltaPos, PrimaNotaServiceImpl.TipoImporto.AVERE);
                salvaAttributiInventario(pnPos, ente, deltaPositivi, importoYPos);
                primaNotaService.finalizzaPrimaNota(pnPos, utente);
                numeriPrimeNote.add(String.valueOf(pnPos.getNumero()));
                LOG.info("processaCambioValore - Inserita prima nota {} conto {}", pnPos.getNumero(), entry.getKey());
                // Registra nel log dell'elaborazione
                logService.registraInfo(file, "Inserita PN " + pnPos.getNumero(),
                    "Inserita prima nota " + pnPos.getNumero() + " conto " + entry.getKey() + " causale " + causaleCod);
            }

            // Processa delta negativi
            // Cambio valore puro (conto invariato): il valore è diminuito di |delta|.
            // Fondo dotazione in DARE per |delta|, conto (y=z) in AVERE per |delta|.
            if (!deltaNegativi.isEmpty()) {
                BigDecimal deltaNeg = deltaNegativi.stream()
                    .map(r -> r.getValoreRealeInizialeBeneBigDecimal().subtract(r.getValoreRealeAttualeBeneBigDecimal()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // deltaNeg > 0 (= |delta|)

                AccrualTPrimaNota pnNeg = creaPNSalvaLegami(file, ente, deltaNegativi, causale, tipoPn, utente);

                // Fondo di dotazione in DARE con |delta|
                primaNotaService.aggiungiImporto(pnNeg, fondo, deltaNeg, PrimaNotaServiceImpl.TipoImporto.DARE);

                // Conto (y) in AVERE con |delta|
                AccrualTPrimaNotaImporto importoYNeg = primaNotaService.aggiungiImporto(pnNeg, cAttuale, deltaNeg, PrimaNotaServiceImpl.TipoImporto.AVERE);

                salvaAttributiInventario(pnNeg, ente, deltaNegativi, importoYNeg);
                primaNotaService.finalizzaPrimaNota(pnNeg, utente);
                numeriPrimeNote.add(String.valueOf(pnNeg.getNumero()));
                LOG.info("processaCambioValore - Inserita prima nota {} conto {}", pnNeg.getNumero(), entry.getKey());
                // Registra nel log dell'elaborazione
                logService.registraInfo(file, "Inserita PN " + pnNeg.getNumero(),
                    "Inserita prima nota " + pnNeg.getNumero() + " conto " + entry.getKey() + " causale " + causaleCod);
            }
        }
    }

    private void processaCambioContoValore(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> list, String causaleCod, AccrualDPrimaNotaTipo tipoPn, String utente, AccrualTPdc fondo, Integer anno, List<String> numeriPrimeNote) throws Exception {
        if (list.isEmpty()) return;
        LOG.info("processaCambioContoValore - Ricerca causale per codice: {} ed ente ID: {}", causaleCod, ente.getId());
        AccrualTCausale causale = accrualTCausaleRepository.findByCodiceAndEnte_Id(causaleCod, ente.getId());
        if (causale == null) throw new IllegalArgumentException("Causale " + causaleCod + " non trovata.");

        for (java.util.Map.Entry<String, List<VariazioniInventarialiFileRowDTO>> entry : list.stream().collect(Collectors.groupingBy(r -> r.getCodiceContoPatrimonialeInizialeBene() + "-" + r.getCodiceContoPatrimonialeAttualeBene())).entrySet()) {
            List<VariazioniInventarialiFileRowDTO> subList = entry.getValue();
            
            // Separa i beni con delta positivo da quelli con delta negativo
            List<VariazioniInventarialiFileRowDTO> deltaPositivi = subList.stream()
                .filter(r -> r.getValoreRealeAttualeBeneBigDecimal().subtract(r.getValoreRealeInizialeBeneBigDecimal()).compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
            
            List<VariazioniInventarialiFileRowDTO> deltaNegativi = subList.stream()
                .filter(r -> r.getValoreRealeAttualeBeneBigDecimal().subtract(r.getValoreRealeInizialeBeneBigDecimal()).compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toList());

            LOG.info("processaCambioContoValore - Ricerca conto patrimoniale iniziale per codice: {}, anno: {}, ente ID: {}", subList.get(0).getCodiceContoPatrimonialeInizialeBene(), anno, ente.getId());
            AccrualTPdc cIni = accrualTPdcRepository.findByCodiceAndAnnoAndEnteId(subList.get(0).getCodiceContoPatrimonialeInizialeBene(), anno, ente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conto patrimoniale iniziale " + subList.get(0).getCodiceContoPatrimonialeInizialeBene() + " non trovato per l'anno " + anno));

            LOG.info("processaCambioContoValore - Ricerca conto patrimoniale attuale per codice: {}, anno: {}, ente ID: {}", subList.get(0).getCodiceContoPatrimonialeAttualeBene(), anno, ente.getId());
            AccrualTPdc cAtt = accrualTPdcRepository.findByCodiceAndAnnoAndEnteId(subList.get(0).getCodiceContoPatrimonialeAttualeBene(), anno, ente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conto patrimoniale attuale " + subList.get(0).getCodiceContoPatrimonialeAttualeBene() + " non trovato per l'anno " + anno));

            // Processa delta positivi
            if (!deltaPositivi.isEmpty()) {
                BigDecimal sommaAttPos = deltaPositivi.stream().map(VariazioniInventarialiFileRowDTO::getValoreRealeAttualeBeneBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sommaIniPos = deltaPositivi.stream().map(VariazioniInventarialiFileRowDTO::getValoreRealeInizialeBeneBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal deltaPos = sommaAttPos.subtract(sommaIniPos);

                AccrualTPrimaNota pnPos = creaPNSalvaLegami(file, ente, deltaPositivi, causale, tipoPn, utente);
                AccrualTPrimaNotaImporto importoYPos = primaNotaService.aggiungiImporto(pnPos, cAtt, sommaAttPos, PrimaNotaServiceImpl.TipoImporto.DARE);
                primaNotaService.aggiungiImporto(pnPos, cIni, sommaIniPos, PrimaNotaServiceImpl.TipoImporto.AVERE);
                primaNotaService.aggiungiImporto(pnPos, fondo, deltaPos, PrimaNotaServiceImpl.TipoImporto.AVERE);
                salvaAttributiInventario(pnPos, ente, deltaPositivi, importoYPos);
                primaNotaService.finalizzaPrimaNota(pnPos, utente);
                numeriPrimeNote.add(String.valueOf(pnPos.getNumero()));
                LOG.info("processaCambioContoValore - Inserita prima nota {} cambio conto da {} a {}", pnPos.getNumero(), subList.get(0).getCodiceContoPatrimonialeInizialeBene(), subList.get(0).getCodiceContoPatrimonialeAttualeBene());
                // Registra nel log dell'elaborazione
                logService.registraInfo(file, "Inserita PN " + pnPos.getNumero(),
                    "Inserita prima nota " + pnPos.getNumero() + " cambio conto da " + subList.get(0).getCodiceContoPatrimonialeInizialeBene() + " a " + subList.get(0).getCodiceContoPatrimonialeAttualeBene() + " causale " + causaleCod);
            }

            // Processa delta negativi
            // Algoritmo 5: Inversione Dare/Avere rispetto ai delta positivi
            if (!deltaNegativi.isEmpty()) {
                BigDecimal sommaAttNeg = deltaNegativi.stream().map(VariazioniInventarialiFileRowDTO::getValoreRealeAttualeBeneBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sommaIniNeg = deltaNegativi.stream().map(VariazioniInventarialiFileRowDTO::getValoreRealeInizialeBeneBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal deltaNeg = sommaIniNeg.subtract(sommaAttNeg); // x3 = x2 - x1 (valore assoluto)

                AccrualTPrimaNota pnNeg = creaPNSalvaLegami(file, ente, deltaNegativi, causale, tipoPn, utente);

                // Conto attuale (y) in DARE con importo x1 (valore attuale)
                AccrualTPrimaNotaImporto importoYNeg = primaNotaService.aggiungiImporto(pnNeg, cAtt, sommaAttNeg, PrimaNotaServiceImpl.TipoImporto.DARE);

                // Fondo di dotazione in DARE con importo |x3| (valore assoluto del delta)
                primaNotaService.aggiungiImporto(pnNeg, fondo, deltaNeg.abs(), PrimaNotaServiceImpl.TipoImporto.DARE);

                // Conto iniziale (z) in AVERE con importo x2 (valore iniziale)
                primaNotaService.aggiungiImporto(pnNeg, cIni, sommaIniNeg, PrimaNotaServiceImpl.TipoImporto.AVERE);

                salvaAttributiInventario(pnNeg, ente, deltaNegativi, importoYNeg);
                primaNotaService.finalizzaPrimaNota(pnNeg, utente);
                numeriPrimeNote.add(String.valueOf(pnNeg.getNumero()));
                LOG.info("processaCambioContoValore - Inserita prima nota {} cambio conto da {} a {}", pnNeg.getNumero(), subList.get(0).getCodiceContoPatrimonialeInizialeBene(), subList.get(0).getCodiceContoPatrimonialeAttualeBene());
                // Registra nel log dell'elaborazione
                logService.registraInfo(file, "Inserita PN " + pnNeg.getNumero(),
                    "Inserita prima nota " + pnNeg.getNumero() + " cambio conto da " + subList.get(0).getCodiceContoPatrimonialeInizialeBene() + " a " + subList.get(0).getCodiceContoPatrimonialeAttualeBene() + " causale " + causaleCod);
            }
        }
    }

    private AccrualTPrimaNota creaPNSalvaLegami(AccrualTFile file, AccrualTEnte ente, List<VariazioniInventarialiFileRowDTO> subList, AccrualTCausale causale, AccrualDPrimaNotaTipo tipoPn, String utente) throws Exception {
        String anno = String.valueOf(subList.get(0).getAnno());
        String descrizione = "ESERCIZIO " + anno + " VARIAZIONI INVENTARIALI";

        AccrualTPrimaNota pn = primaNotaService.creaPrimaNota(ente, causale, file, null, anno, new java.util.Date(), descrizione);

        if (causale.getEvento() != null && causale.getEvento().getPrimaNotaStato() != null
                && AccrualBusinessConstants.STATO_PRIMA_NOTA_DEFINITIVA.equals(causale.getEvento().getPrimaNotaStato().getCodice())) {
            pn.setProgressivoGiornale(primaNotaService.reserveNumeriDefinitivi(ente, Integer.parseInt(anno), 1));
            pn.setDataRegistrazioneGiornale(LocalDateTime.now());
        }

        List<String> codici = subList.stream()
            .map(VariazioniInventarialiFileRowDTO::getNumeroInventarioBene)
            .collect(Collectors.toList());
        // Rielaborando un file con lo stesso numero di etichetta, a DB convivono la riga vecchia e
        // quella appena inserita: il legame deve puntare alla nuova. La query le restituisce dalla
        // piu' recente, e a parita' di codice si tiene quella con l'id piu' alto, senza affidarsi
        // all'ordine di arrivo.
        List<AccrualTEtichetta> tutteEtichette = accrualTEtichettaRepository.findByCodiceInAndEnte(codici, ente);
        Map<String, AccrualTEtichetta> etichetteMap = tutteEtichette.stream()
            .collect(Collectors.toMap(AccrualTEtichetta::getCodice, e -> e,
                (a, b) -> a.getId() != null && b.getId() != null && b.getId() > a.getId() ? b : a));

        LocalDateTime now = LocalDateTime.now();
        List<AccrualREtichettaPrimaNota> legami = new java.util.ArrayList<>(subList.size());
        for (VariazioniInventarialiFileRowDTO r : subList) {
            AccrualTEtichetta etichetta = etichetteMap.get(r.getNumeroInventarioBene());
            if (etichetta != null) {
                AccrualREtichettaPrimaNota legame = new AccrualREtichettaPrimaNota();
                legame.setEnte(ente);
                legame.setEtichetta(etichetta);
                legame.setPrimaNota(pn);
                legame.setInizioValidita(now);
                legami.add(legame);
            }
        }
        if (!legami.isEmpty()) {
            accrualREtichettaPrimaNotaRepository.saveAll(legami);
        }
        LOG.debug("creaPNSalvaLegami - PN {} - salvati {} legami etichetta in batch", pn.getId(), legami.size());
        return pn;
    }

    private void salvaAttributiInventario(AccrualTPrimaNota pn, AccrualTEnte ente,
            List<VariazioniInventarialiFileRowDTO> subList, AccrualTPrimaNotaImporto importoContoY) {
        List<String> valori = subList.stream()
            .map(VariazioniInventarialiFileRowDTO::getNumeroInventarioBene)
            .collect(Collectors.toList());
        primaNotaService.aggiungiAttributiBatch(pn, "INVENTARIO", valori, importoContoY);
    }


}