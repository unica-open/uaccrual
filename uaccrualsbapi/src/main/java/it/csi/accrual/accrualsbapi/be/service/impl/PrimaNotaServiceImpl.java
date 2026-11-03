/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.util.AccrualBusinessConstants;
import it.csi.accrual.accrualcommonsrv.be.util.PrimaNotaNumService;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualsbapi.be.service.PrimaNotaService;

/**
 * Implementazione del servizio PrimaNota per il modulo API.
 * Focalizzato esclusivamente sulla creazione automatica da flussi batch.
 * Le funzionalità di ricerca e gestione manuale risiedono nel modulo SRV.
 */
@Service
@Transactional
public class PrimaNotaServiceImpl implements PrimaNotaService {

    private static final Logger log = LoggerFactory.getLogger(PrimaNotaServiceImpl.class);

    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;

    @Autowired
    private AccrualDPrimaNotaTipoRepository tipoRepository;

    @Autowired
    private AccrualTCausaleRepository causaleRepository;

    @Autowired
    private AccrualDPrimaNotaStatoRepository statoRepository;

    @Autowired
    private AccrualTPrimaNotaImportoRepository primaNotaImportoRepository;

    @Autowired
    private AccrualRPrimaNotaAttributoRepository primaNotaAttributoRepository;

    @Autowired
    private AccrualRPrimaNotaStatoRepository accrualRPrimaNotaStatoRepository;

    @Autowired
    private AccrualDAttributoRepository attributoRepository;

    @Autowired
    private PrimaNotaNumService primaNotaNumService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${batch-insert.prima-nota.size:50}")
    private int batchSizePrimaNota;

    @Value("${batch-insert.prima-nota-importo.size:100}")
    private int batchSizePrimaNotaImporto;

    @Value("${batch-insert.prima-nota-stato.size:50}")
    private int batchSizePrimaNotaStato;

    @Value("${batch-insert.prima-nota-attributo.size:50}")
    private int batchSizePrimaNotaAttributo;

    public enum TipoImporto {
        DARE,
        AVERE
    }
    
    public int reserveNumeriProvvisori(AccrualTEnte ente, Integer anno, int count) {
        return primaNotaNumService.reserveNumeriProvvisori(ente, anno, count);
    }

    public int reserveNumeriDefinitivi(AccrualTEnte ente, Integer anno, int count) {
        return primaNotaNumService.reserveNumeriDefinitivi(ente, anno, count);
    }

    public AccrualDPrimaNotaTipo getTipoPrimaNotaAutomatica() {
        return tipoRepository.findByCodice(AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA)
                .orElseThrow(() -> new IllegalStateException("Tipo prima nota '" + AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA + "' non configurato nel sistema"));
    }

    public AccrualDAttributo getAttributoByCodice(String codice, Long enteId) {
        return attributoRepository.findByCodiceAndEnteIdAndDataCancellazioneIsNull(codice, enteId)
                .orElseThrow(() -> new NotFoundException("Attributo con codice '" + codice + "' non trovato per l'ente " + enteId));
    }

    public AccrualTPrimaNota buildPrimaNota(AccrualTEnte ente, AccrualTCausale causale, AccrualTFile file,
            int numero, Integer anno, String note, AccrualDPrimaNotaTipo tipo) {
        AccrualTPrimaNota pn = new AccrualTPrimaNota();
        pn.setEnte(ente);
        pn.setCausale(causale);
        if (file != null && file.getFileTipo() != null && file.getFileTipo().getFonte() != null) {
            pn.setFonte(file.getFileTipo().getFonte());
        }
        pn.setTipo(tipo);
        pn.setDescrizione(note);
        pn.setAnno(anno);
        pn.setNumero(numero);
        pn.setData(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        pn.setDataInserimento(now);
        pn.setUtenteInserimento("system_user");
        pn.setDataAggiornamento(now);
        pn.setUtenteAggiornamento("system_user");
        return pn;
    }

    public List<AccrualTPrimaNota> salvaAll(List<AccrualTPrimaNota> primeNote) {
        for (int i = 0; i < primeNote.size(); i++) {
            entityManager.persist(primeNote.get(i));
            if ((i + 1) % batchSizePrimaNota == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();
        return primeNote;
    }

    public AccrualRPrimaNotaStato buildStatoRecord(AccrualTPrimaNota pn, AccrualDPrimaNotaStato stato,
            AccrualTEnte ente, String utente) {
        AccrualRPrimaNotaStato r = new AccrualRPrimaNotaStato();
        r.setPrimaNota(pn);
        r.setPrimaNotaStato(stato);
        r.setEnte(ente);
        LocalDateTime now = LocalDateTime.now();
        r.setInizioValidita(now);
        r.setDataInserimento(now);
        r.setUtenteInserimento(utente);
        r.setUtenteAggiornamento(utente);
        return r;
    }

    public List<AccrualRPrimaNotaStato> salvaStatiAll(List<AccrualRPrimaNotaStato> stati) {
        for (int i = 0; i < stati.size(); i++) {
            entityManager.persist(stati.get(i));
            if ((i + 1) % batchSizePrimaNotaStato == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();
        return stati;
    }

    public AccrualTPrimaNotaImporto buildImporto(AccrualTPrimaNota pn, AccrualTPdc pdc, BigDecimal importo, TipoImporto tipoImporto) {
        AccrualTPrimaNotaImporto pni = new AccrualTPrimaNotaImporto();
        pni.setPrimaNota(pn);
        pni.setPdc(pdc);
        pni.setImporto(importo);
        pni.setSegno(tipoImporto == TipoImporto.DARE ? TipoSegno.DARE : TipoSegno.AVERE);
        LocalDateTime now = LocalDateTime.now();
        pni.setDataInserimento(now);
        pni.setUtenteInserimento(pn.getUtenteInserimento());
        pni.setDataAggiornamento(now);
        pni.setUtenteAggiornamento(pn.getUtenteAggiornamento());
        pni.setEnte(pn.getEnte());
        return pni;
    }

    public List<AccrualTPrimaNotaImporto> salvaImportiAll(List<AccrualTPrimaNotaImporto> importi) {
        for (int i = 0; i < importi.size(); i++) {
            entityManager.persist(importi.get(i));
            if ((i + 1) % batchSizePrimaNotaImporto == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();
        return importi;
    }

    public AccrualRPrimaNotaAttributo buildAttributo(AccrualTPrimaNota pn, AccrualTPrimaNotaImporto importo,
            AccrualDAttributo attributo, String valore) {
        AccrualRPrimaNotaAttributo pna = new AccrualRPrimaNotaAttributo();
        pna.setPrimaNota(pn);
        pna.setPrimaNotaImporto(importo);
        pna.setAttributo(attributo);
        pna.setValore(valore);
        pna.setEnte(pn.getEnte());
        LocalDateTime now = LocalDateTime.now();
        pna.setInizioValidita(now);
        pna.setDataInserimento(now);
        pna.setUtenteInserimento(pn.getUtenteInserimento());
        pna.setDataAggiornamento(now);
        pna.setUtenteAggiornamento(pn.getUtenteAggiornamento());
        return pna;
    }

    public void salvaAttributiAll(List<AccrualRPrimaNotaAttributo> attributi) {
        for (int i = 0; i < attributi.size(); i++) {
            entityManager.persist(attributi.get(i));
            if ((i + 1) % batchSizePrimaNotaAttributo == 0) {
                entityManager.flush();
            }
        }
        entityManager.flush();
    }

    private Integer getSuccessivoNumeroProvvisorio(AccrualTEnte ente, String anno) {
        return primaNotaNumService.nextNumeroProvvisorio(ente, Integer.parseInt(anno));
    }

    /**
     * Metodo per ottenere lo stato della prima nota associato all'evento
     * attraverso la relazione: Evento → Causale → PrimaNota → Stato
     */
    public AccrualDPrimaNotaStato getStatoPrimaNotaByEvento(AccrualTEvento evento) {
        if (evento == null) {
            return null;
        }
        
        List<AccrualTCausale> causali = causaleRepository.findByEvento_Id(evento.getId());
        if (causali.isEmpty()) {
            return null;
        }
        
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByCausaleIn(causali);
        if (primeNote.isEmpty()) {
            return null;
        }
        
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
    public AccrualTPrimaNota creaPrimaNota(AccrualTEnte ente, AccrualTCausale causale, AccrualTFile file, String numero, String anno, java.util.Date data, String note) {
        AccrualTPrimaNota primaNota = new AccrualTPrimaNota();
        primaNota.setEnte(ente);
        primaNota.setCausale(causale);

        if (file != null && file.getFileTipo() != null && file.getFileTipo().getFonte() != null) {
            primaNota.setFonte(file.getFileTipo().getFonte());
        } else {
            // Se non riusciamo a determinare la fonte dal file, proviamo dalla causale o lanciamo errore
            if (causale != null && causale.getEvento() != null) {
               // Potremmo cercare una fonte predefinita per l'evento, ma per ora lanciamo eccezione se obbligatoria
               log.warn("Impossibile determinare la fonte per la prima nota: file o tipo file nullo");
            }
        }

        // Imposta il tipo della prima nota come AUTOMATICA (obbligatorio per le prime note generate da flussi)
        AccrualDPrimaNotaTipo tipo = tipoRepository.findByCodice(AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA).orElse(null);
        if (tipo == null) {
            throw new IllegalStateException("Tipo prima nota '" + AccrualBusinessConstants.TIPO_PRIMA_NOTA_AUTOMATICA + "' non configurato nel sistema");
        }
        primaNota.setTipo(tipo);

        primaNota.setDescrizione(note);

        if (anno != null) {
            primaNota.setAnno(Integer.parseInt(anno));
        }

        // Se il numero non è fornito, lo calcoliamo automaticamente (Ref. 2.6.2.1)
        if (numero == null || numero.isEmpty()) {
            primaNota.setNumero(getSuccessivoNumeroProvvisorio(ente, String.valueOf(primaNota.getAnno())));
        } else {
            try {
                primaNota.setNumero(Integer.parseInt(numero));
            } catch (NumberFormatException e) {
                log.warn("Formato numero non valido: {}", numero);
            }
        }

        if (data != null) {
            primaNota.setData(data.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }

        String utenteCorrente = "system_user";

        primaNota.setDataInserimento(LocalDateTime.now());
        primaNota.setUtenteInserimento(utenteCorrente);
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);

        AccrualDPrimaNotaStato statoIniziale = null;
        if (causale != null && causale.getEvento() != null && causale.getEvento().getPrimaNotaStato() != null) {
            statoIniziale = causale.getEvento().getPrimaNotaStato();
            log.debug("Stato prima nota dall'evento '{}': {}", causale.getEvento().getCodice(), statoIniziale.getCodice());
        } else {
            statoIniziale = getStatoPrimaNotaByCodice(AccrualBusinessConstants.STATO_PRIMA_NOTA_PROVVISORIA);
            log.warn("Stato prima nota non configurato sull'evento, uso fallback PROVVISORIA");
        }
        storicizzaEImpostaNuovoStato(primaNota, statoIniziale, utenteCorrente, ente);

        return primaNotaRepository.save(primaNota);
    }

    public AccrualTPrimaNotaImporto aggiungiImporto(AccrualTPrimaNota primaNota, AccrualTPdc pdc, BigDecimal importo, TipoImporto tipoImporto) {
        AccrualTPrimaNotaImporto pni = new AccrualTPrimaNotaImporto();
        pni.setPrimaNota(primaNota);
        pni.setPdc(pdc);
        pni.setImporto(importo);
        pni.setSegno(tipoImporto == TipoImporto.DARE ? TipoSegno.DARE : TipoSegno.AVERE);

        pni.setDataInserimento(LocalDateTime.now());
        pni.setUtenteInserimento(primaNota.getUtenteInserimento());
        pni.setDataAggiornamento(LocalDateTime.now());
        pni.setUtenteAggiornamento(primaNota.getUtenteAggiornamento());
        pni.setEnte(primaNota.getEnte());

        return primaNotaImportoRepository.save(pni);
    }

    public void aggiungiAttributo(AccrualTPrimaNota primaNota, String codiceAttributo, String valoreAttributo) {
        aggiungiAttributo(primaNota, codiceAttributo, valoreAttributo, null);
    }
    
    public void aggiungiAttributo(AccrualTPrimaNota primaNota, String codiceAttributo, String valoreAttributo, AccrualTPrimaNotaImporto primaNotaImporto) {
        AccrualRPrimaNotaAttributo pna = new AccrualRPrimaNotaAttributo();
        pna.setPrimaNota(primaNota);
        pna.setPrimaNotaImporto(primaNotaImporto);

        AccrualDAttributo attributo = attributoRepository.findByCodiceAndEnteIdAndDataCancellazioneIsNull(codiceAttributo, primaNota.getEnte().getId())
            .orElseThrow(() -> new NotFoundException("Attributo con codice '" + codiceAttributo + "' non trovato per l'ente " + primaNota.getEnte().getId()));
        pna.setAttributo(attributo);
        pna.setValore(valoreAttributo);
        pna.setEnte(primaNota.getEnte());
        pna.setInizioValidita(LocalDateTime.now());

        pna.setDataInserimento(LocalDateTime.now());
        pna.setUtenteInserimento(primaNota.getUtenteInserimento());
        pna.setDataAggiornamento(LocalDateTime.now());
        pna.setUtenteAggiornamento(primaNota.getUtenteAggiornamento());

        primaNotaAttributoRepository.save(pna);

        log.debug("Attributo salvato: codice={}, valore={}, primaNotaImportoId={}",
            codiceAttributo, valoreAttributo, primaNotaImporto != null ? primaNotaImporto.getId() : "null");
    }

    public void aggiungiAttributiBatch(AccrualTPrimaNota primaNota, String codiceAttributo,
            List<String> valori, AccrualTPrimaNotaImporto primaNotaImporto) {
        AccrualDAttributo attributo = attributoRepository.findByCodiceAndEnteIdAndDataCancellazioneIsNull(codiceAttributo, primaNota.getEnte().getId())
            .orElseThrow(() -> new NotFoundException("Attributo con codice '" + codiceAttributo + "' non trovato per l'ente " + primaNota.getEnte().getId()));

        LocalDateTime now = LocalDateTime.now();
        List<AccrualRPrimaNotaAttributo> attributi = new ArrayList<>(valori.size());
        for (String valore : valori) {
            AccrualRPrimaNotaAttributo pna = new AccrualRPrimaNotaAttributo();
            pna.setPrimaNota(primaNota);
            pna.setPrimaNotaImporto(primaNotaImporto);
            pna.setAttributo(attributo);
            pna.setValore(valore);
            pna.setEnte(primaNota.getEnte());
            pna.setInizioValidita(now);
            pna.setDataInserimento(now);
            pna.setUtenteInserimento(primaNota.getUtenteInserimento());
            pna.setDataAggiornamento(now);
            pna.setUtenteAggiornamento(primaNota.getUtenteAggiornamento());
            attributi.add(pna);
        }
        primaNotaAttributoRepository.saveAll(attributi);
        log.debug("Attributi batch salvati: {} per codice={}, primaNotaImportoId={}",
            attributi.size(), codiceAttributo, primaNotaImporto != null ? primaNotaImporto.getId() : "null");
    }
    
    public AccrualTPrimaNotaImporto getUltimoImporto(AccrualTPrimaNota primaNota) {
        List<AccrualTPrimaNotaImporto> importi = primaNotaImportoRepository.findByPrimaNotaIdOrderByIdDesc(primaNota.getId());
        return importi != null && !importi.isEmpty() ? importi.get(0) : null;
    }
    
    public void finalizzaPrimaNota(AccrualTPrimaNota primaNota, String utenteCorrente) {
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);
        primaNotaRepository.save(primaNota);
    }

    public AccrualDPrimaNotaStato getStatoPrimaNotaByCodice(String codiceStato) {
        return statoRepository.findByCodice(codiceStato)
            .orElseThrow(() -> new NotFoundException("Stato Prima Nota '" + codiceStato + "' non trovato."));
    }

    public void storicizzaEImpostaNuovoStato(AccrualTPrimaNota primaNota, AccrualDPrimaNotaStato nuovoStato, String utenteCorrente, AccrualTEnte ente) {
        // Fix: Se la prima nota non è ancora stata salvata (ID null), dobbiamo salvarla prima di poter associare uno stato
        if (primaNota.getId() == null) {
            primaNotaRepository.save(primaNota);
        }

        List<AccrualRPrimaNotaStato> statoCorrenteList = accrualRPrimaNotaStatoRepository.findByPrimaNotaAndFineValiditaIsNull(primaNota);

        if (!statoCorrenteList.isEmpty()) {
            AccrualRPrimaNotaStato statoCorrente = statoCorrenteList.get(0);
            statoCorrente.setFineValidita(LocalDateTime.now());
            statoCorrente.setUtenteAggiornamento(utenteCorrente);
            accrualRPrimaNotaStatoRepository.save(statoCorrente);
        }

        AccrualRPrimaNotaStato nuovoStatoRecord = new AccrualRPrimaNotaStato();
        nuovoStatoRecord.setPrimaNota(primaNota);
        nuovoStatoRecord.setPrimaNotaStato(nuovoStato);
        nuovoStatoRecord.setEnte(ente);
        nuovoStatoRecord.setInizioValidita(LocalDateTime.now());
        nuovoStatoRecord.setDataInserimento(LocalDateTime.now());
        nuovoStatoRecord.setUtenteInserimento(utenteCorrente);
        nuovoStatoRecord.setUtenteAggiornamento(utenteCorrente);
        accrualRPrimaNotaStatoRepository.save(nuovoStatoRecord);

        primaNotaRepository.save(primaNota);
    }

    /**
     * Annulla in bulk N prime note: un singolo UPDATE per chiudere tutti gli stati aperti,
     * poi un batch INSERT di nuovi stati ANNULLATA — sostituisce N×5 query individuali.
     */
    public void bulkAnnullaPrimeNote(List<Long> ids, AccrualTEnte ente, String utente) {
        if (ids == null || ids.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();

        // 1. Bulk UPDATE: chiudi tutti gli stati attivi in un solo round-trip
        accrualRPrimaNotaStatoRepository.chiudiStatiByPrimaNotaIds(ids, now, utente);

        // 2. Leggi il codice ANNULLATA una volta sola
        AccrualDPrimaNotaStato statoAnnullato = getStatoPrimaNotaByCodice(AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO);

        // 3. Costruisci i nuovi stati usando proxy (nessun SELECT per entity)
        List<AccrualRPrimaNotaStato> nuoviStati = new ArrayList<>(ids.size());
        for (Long id : ids) {
            AccrualRPrimaNotaStato r = new AccrualRPrimaNotaStato();
            r.setPrimaNota(entityManager.getReference(AccrualTPrimaNota.class, id));
            r.setPrimaNotaStato(statoAnnullato);
            r.setEnte(ente);
            r.setInizioValidita(now);
            r.setDataInserimento(now);
            r.setUtenteInserimento(utente);
            r.setUtenteAggiornamento(utente);
            nuoviStati.add(r);
        }

        // 4. Batch INSERT
        salvaStatiAll(nuoviStati);
        log.info("[bulkAnnullaPrimeNote] Annullate {} prime note in bulk", ids.size());
    }
}
