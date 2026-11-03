/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;


import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausalePdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEventoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPdcLegame;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcClasseRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPdcTipoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualDPdcTipoRelazioneRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRPdcLegameRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcFamigliaRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcSaldoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEntitaContabileRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.EventoMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcClasseMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcFamigliaMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcStatoMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.PdcTipoMapper;
import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.be.service.PdcValidator;
import it.csi.accrual.accrualcommonsrv.security.PermissionHelper;
import it.csi.accrual.accrualsbsrv.dto.Pdc;
import it.csi.accrual.accrualsbsrv.dto.PagedPdc;
import it.csi.accrual.accrualsbsrv.dto.PdcClasse;
import it.csi.accrual.accrualsbsrv.dto.PdcFamiglia;
import it.csi.accrual.accrualsbsrv.dto.PdcStato;
import it.csi.accrual.accrualsbsrv.dto.PdcTipo;
import it.csi.accrual.accrualsbsrv.dto.PdcContoRelazione;
import it.csi.accrual.accrualsbsrv.dto.PdcFilter;
import it.csi.accrual.accrualsbsrv.util.FilterSpecificationBuilder;
import it.csi.accrual.accrualsbsrv.be.service.impl.PdcSpecifications;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.be.util.BilancioContextHolder;

@Service
@Transactional
public class PdcServiceImpl implements PdcService {

    private static final Logger log = LoggerFactory.getLogger(PdcServiceImpl.class);

    @Autowired
    private AccrualTPdcRepository pdcRepository;

    @Autowired
    private AccrualDPdcClasseRepository pdcClasseRepository;

    @Autowired
    private AccrualDPdcTipoRepository pdcTipoRepository;

    @Autowired
    private AccrualDPdcStatoRepository pdcStatoRepository;

    @Autowired
    private AccrualTPdcFamigliaRepository pdcFamigliaRepository;

    @Autowired
    private AccrualTEventoRepository eventoRepository;

    @Autowired
    private AccrualRPdcLegameRepository pdcLegameRepository;

    @Autowired
    private AccrualDPdcTipoRelazioneRepository pdcTipoRelazioneRepository;


    @Autowired
    private AccrualTCausaleRepository causaleRepository;

    @Autowired
    private AccrualTSoggettoRepository soggettoRepository;

    @Autowired
    private AccrualTPdcSaldoRepository saldoRepository;

    @Autowired
    private AccrualTEnteRepository enteRepository;

    @Autowired
    private AccrualTEntitaContabileRepository entitaContabileRepository;

    @Autowired
    private EventoMapper eventoMapper;

    @Autowired
    private PdcMapper pdcMapper;

    @Autowired
    private PdcValidator pdcValidator;

    @Autowired
    private PdcClasseMapper pdcClasseMapper;

    @Autowired
    private PdcTipoMapper pdcTipoMapper;

    @Autowired
    private PdcStatoMapper pdcStatoMapper;

    @Autowired
    private PdcFamigliaMapper pdcFamigliaMapper;

    @Autowired
    private SecurityContextHelper securityContextHelper;
    @Autowired
    private PermissionHelper permissionHelper;

    /**
     * Recupera l'anno del bilancio corrente dall'EntitaContabile per l'ente corrente.
     * Se non trova un bilancio valido, usa l'anno solare corrente.
     * Legge il codice ente e l'anno bilancio dal JWT tramite SecurityContextHelper.
     *
     * @return anno del bilancio corrente o anno solare corrente
     */
    private Integer getAnnoBilancioCorrente() {
        try {
            // Leggo il codice ente dal JWT
            String enteCodice = securityContextHelper.getEnteCodiceUnivoco();

            if (enteCodice != null && !enteCodice.isEmpty()) {
                // Leggo l'anno bilancio dal JWT
                Integer annoBilancioJwt = SecurityContextHelper.getAnnoBilancioStatic();
                String annoDaUsare = annoBilancioJwt != null ? String.valueOf(annoBilancioJwt) : String.valueOf(Year.now().getValue());

                // Cerco l'entità contabile valida per la data corrente
                LocalDateTime dataCorrente = LocalDateTime.now();
                var entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(
                    enteCodice,
                    annoDaUsare,
                    dataCorrente
                );

                if (entitaContabileOpt.isPresent()) {
                    String annoStr = entitaContabileOpt.get().getAnno();
                    try {
                        return Integer.parseInt(annoStr);
                    } catch (NumberFormatException e) {
                        log.warn("Anno bilancio non numerico: {}, uso anno corrente", annoStr);
                    }
                } else {
                    log.debug("Nessun bilancio trovato per ente {} e anno {}, uso anno solare",
                              enteCodice, Year.now().getValue());
                }
            }
        } catch (Exception e) {
            log.warn("Errore nel recupero anno bilancio: {}", e.getMessage());
        }

        // Fallback: anno solare corrente
        return Year.now().getValue();
    }

    @Override
    public PagedPdc getPdcList(String sort, Integer limit, Integer offset) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        Integer annoBilancio = BilancioContextHolder.getCurrentAnnoBilancio();
        if (annoBilancio == null) {
            throw new IllegalStateException("Anno di bilancio non disponibile. Verificare che il token JWT contenga il claim 'anno_bilancio'.");
        }

        log.info("Richiesta getPdcList con parametri - sort: {}, limit: {}, offset: {}, enteCodice: {}, annoBilancio: {}",
                 sort, limit, offset, enteCodice, annoBilancio);

        // Log per capire i valori ricevuti
        log.info("Valori ricevuti - limit: {}, offset: {}", limit, offset);

        // Parse del parametro sort
        Sort sortOrder = parseSortParameter(sort);
        log.debug("Ordinamento parsato per getPdcList: {}", sortOrder);

        int page = 0;
        int pageSize = 20; // Valore di default
        Pageable pageable;

        if (limit != null && limit > 0) {
            log.info("Limit è positivo: {}", limit);
            // Se il limit è un valore molto alto (> Integer.MAX_VALUE), interpretarlo come "restituisci tutti i risultati"
            if (limit > Integer.MAX_VALUE) {
                log.warn("Limit oltre Integer.MAX_VALUE ricevuto: {}. Interpretato come 'restituisci tutti i risultati'", limit);
                // In questo caso, impostiamo un valore massimo ragionevole per evitare problemi di memoria
                pageSize = 10000; // Limitiamo a 10000 per sicurezza
                log.info("Impostato pageSize a {} per gestire il limit molto alto", pageSize);
                page = (offset != null && offset > 0) ? offset / pageSize : 0;
                // Assicuriamoci che pageSize sia sempre >= 1
                pageSize = Math.max(1, pageSize);
                pageable = PageRequest.of(page, pageSize, sortOrder);
                log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per limit molto alto", page, pageSize, sortOrder);
            } else {
                pageSize = limit;
                page = (offset != null && offset > 0) ? offset / limit : 0;
                // Assicuriamoci che pageSize sia sempre >= 1
                pageSize = Math.max(1, pageSize);
                pageable = PageRequest.of(page, pageSize, sortOrder);
                log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per limit normale", page, pageSize, sortOrder);
            }
        } else if (limit != null && limit == -1) {
            log.info("Richiesta di ottenere tutti i risultati (limit = -1)");
            // In questo caso, impostiamo un valore massimo ragionevole per ottenere tutti i risultati possibili
            pageSize = 10000; // Limitiamo a 10000 per sicurezza
            page = (offset != null && offset > 0) ? offset / pageSize : 0;
            // Assicuriamoci che pageSize sia sempre >= 1
            pageSize = Math.max(1, pageSize);
            pageable = PageRequest.of(page, pageSize, sortOrder);
            log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per ottenere tutti i risultati", page, pageSize, sortOrder);
        } else {
            log.info("Limit non positivo o nullo, usando valori di default");
            // Valore di default se limit non è specificato o non è positivo
            page = (offset != null && offset > 0) ? offset / 20 : 0;
            // Assicuriamoci che pageSize sia sempre >= 1
            pageSize = Math.max(1, 20);
            pageable = PageRequest.of(page, pageSize, sortOrder);
            log.info("Creato PageRequest con page: {}, pageSize: 20, sortOrder: {} per valori di default", page, sortOrder);
        }

        // Assicuriamoci che pageSize sia sempre >= 1 come ulteriore precauzione
        pageSize = Math.max(1, pageSize);

        Specification<AccrualTPdc> spec = Specification.where(PdcSpecifications.isRicercabile())
                .and(PdcSpecifications.isValidInYear(annoBilancio));

        if (enteCodice != null && !enteCodice.isEmpty()) {
            spec = spec.and(PdcSpecifications.byEnteCodiceUnivoco(enteCodice));
        }

        Page<AccrualTPdc> pagedPdcEntities = pdcRepository.findAll(spec, pageable);

        // Applichiamo l'ordinamento naturale per codice_ufficiale se specificato nel parametro sort
        List<AccrualTPdc> sortedContent = pagedPdcEntities.getContent();
        if (sort != null && (sort.contains("codice_ufficiale") || sort.contains("codiceUfficiale"))) {
            sortedContent.sort(this::compareCodiceUfficialeEntity);
            log.debug("Applicato ordinamento naturale per codice_ufficiale su {} elementi", sortedContent.size());
        }

        // Creiamo una nuova pagina con il contenuto ordinato
        Page<AccrualTPdc> pagedPdcEntitiesSorted = new PageImpl<>(
            sortedContent,
            pagedPdcEntities.getPageable(),
            pagedPdcEntities.getTotalElements()
        );

        return mapToPagedPdc(pagedPdcEntitiesSorted, annoBilancio);
    }

    @Override
    public PagedPdc searchPdc(PdcFilter filter, String sort, Integer limit, Integer offset) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        Integer annoBilancio = BilancioContextHolder.getCurrentAnnoBilancio();
        if (annoBilancio == null) {
            throw new IllegalStateException("Anno di bilancio non disponibile. Verificare che il token JWT contenga il claim 'anno_bilancio'.");
        }

        log.info("Inizio elaborazione searchPdc nel servizio - filter: {}, sort: {}, limit: {}, offset: {}, enteCodice: {}, annoBilancio: {}",
                 filter != null ? "presente" : "null", sort, limit, offset, enteCodice, annoBilancio);

        // Log per capire i valori ricevuti
        log.info("Valori ricevuti - limit: {}, offset: {}", limit, offset);

        if (filter != null) {
            log.debug("Valori del PdcFilter ricevuto: famiglia={}, codiceEvento={}, pdcClasseCodice={}, codiceUfficiale={}, pdcStatoCodice={}, descrizione={}, livello={}, modificabile={}, pdcTipoCodice={}",
                filter.getFamiglia(), filter.getCodiceEvento(), filter.getPdcClasseCodice(),
                filter.getCodiceUfficiale(), filter.getPdcStatoCodice(), filter.getDescrizione(),
                filter.getLivello(), filter.isModificabile(),
                filter.getPdcTipoCodice());
        } else {
            log.warn("PdcFilter ricevuto è null - questo potrebbe indicare un problema di parsing del corpo della richiesta");
        }

        // Parse del parametro sort
        Sort sortOrder = parseSortParameter(sort);
        log.debug("Ordinamento parsato per searchPdc: {}", sortOrder);

        int page = 0;
        int pageSize = 20; // Valore di default
        Pageable pageable;

        if (limit != null && limit > 0) {
            log.info("Limit è positivo: {}", limit);
            // Se il limit è un valore molto alto (> Integer.MAX_VALUE), interpretarlo come "restituisci tutti i risultati"
            if (limit > Integer.MAX_VALUE) {
                log.warn("Limit oltre Integer.MAX_VALUE ricevuto: {}. Interpretato come 'restituisci tutti i risultati'", limit);
                // In questo caso, impostiamo un valore massimo ragionevole per evitare problemi di memoria
                pageSize = 10000; // Limitiamo a 10000 per sicurezza
                log.info("Impostato pageSize a {} per gestire il limit molto alto", pageSize);
                page = (offset != null && offset > 0) ? offset / pageSize : 0;
                // Assicuriamoci che pageSize sia sempre >= 1
                pageSize = Math.max(1, pageSize);
                pageable = PageRequest.of(page, pageSize, sortOrder);
                log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per limit molto alto", page, pageSize, sortOrder);
            } else {
                pageSize = limit;
                page = (offset != null && offset > 0) ? offset / limit : 0;
                // Assicuriamoci che pageSize sia sempre >= 1
                pageSize = Math.max(1, pageSize);
                pageable = PageRequest.of(page, pageSize, sortOrder);
                log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per limit normale", page, pageSize, sortOrder);
            }
        } else if (limit != null && limit == -1) {
            log.info("Richiesta di ottenere tutti i risultati (limit = -1)");
            // In questo caso, impostiamo un valore massimo ragionevole per ottenere tutti i risultati possibili
            pageSize = 10000; // Limitiamo a 10000 per sicurezza
            page = (offset != null && offset > 0) ? offset / pageSize : 0;
            // Assicuriamoci che pageSize sia sempre >= 1
            pageSize = Math.max(1, pageSize);
            pageable = PageRequest.of(page, pageSize, sortOrder);
            log.info("Creato PageRequest con page: {}, pageSize: {}, sortOrder: {} per ottenere tutti i risultati", page, pageSize, sortOrder);
        } else {
            log.info("Limit non positivo o nullo, usando valori di default");
            // Valore di default se limit non è specificato o non è positivo
            page = (offset != null && offset > 0) ? offset / 20 : 0;
            // Assicuriamoci che pageSize sia sempre >= 1
            pageSize = Math.max(1, 20);
            pageable = PageRequest.of(page, pageSize, sortOrder);
            log.info("Creato PageRequest con page: {}, pageSize: 20, sortOrder: {} per valori di default", page, sortOrder);
        }

        // Assicuriamoci che pageSize sia sempre >= 1 come ulteriore precauzione
        pageSize = Math.max(1, pageSize);

        Specification<AccrualTPdc> spec = Specification.where(PdcSpecifications.isRicercabile())
                .and(PdcSpecifications.isValidInYear(annoBilancio));

        if (enteCodice != null && !enteCodice.isEmpty()) {
            log.debug("Applicazione filtro per ente: {}", enteCodice);
            spec = spec.and(PdcSpecifications.byEnteCodiceUnivoco(enteCodice));
        }

        if (filter != null) {
            if (StringUtils.hasText(filter.getFamiglia())) {
                log.debug("Applicazione filtro per famiglia: {}", filter.getFamiglia());
                spec = spec.and(PdcSpecifications.byFamiglia(filter.getFamiglia()));
            }
            if (StringUtils.hasText(filter.getCodiceEvento())) {
                log.debug("Applicazione filtro per codice evento: {}", filter.getCodiceEvento());
                spec = spec.and(PdcSpecifications.byCodiceEvento(filter.getCodiceEvento()));
            }
            if (StringUtils.hasText(filter.getPdcClasseCodice())) {
                log.debug("Applicazione filtro per classe codice: {}", filter.getPdcClasseCodice());
                spec = spec.and(PdcSpecifications.byPdcClasseCodice(filter.getPdcClasseCodice()));
            }
            if (StringUtils.hasText(filter.getCodiceUfficiale())) {
                log.debug("Applicazione filtro per codice ufficiale: {}", filter.getCodiceUfficiale());
                spec = spec.and(PdcSpecifications.byCodiceUfficiale(filter.getCodiceUfficiale()));
            }
            if (StringUtils.hasText(filter.getPdcStatoCodice())) {
                log.debug("Applicazione filtro per stato codice: {}", filter.getPdcStatoCodice());
                spec = spec.and(PdcSpecifications.byPdcStatoCodice(filter.getPdcStatoCodice()));
            }
            if (StringUtils.hasText(filter.getDescrizione())) {
                log.debug("Applicazione filtro per descrizione: {}", filter.getDescrizione());
                spec = spec.and(PdcSpecifications.byDescrizione(filter.getDescrizione()));
            }
            if (filter.getLivello() != null) {
                log.debug("Applicazione filtro per livello: {}", filter.getLivello());
                spec = spec.and(PdcSpecifications.byLivello(filter.getLivello()));
            }
            if (filter.isModificabile() != null) {
                log.debug("Applicazione filtro per modificabile: {}", filter.isModificabile());
                spec = spec.and(PdcSpecifications.byModificabile(filter.isModificabile()));
            }
            if (StringUtils.hasText(filter.getPdcTipoCodice())) {
                log.debug("Applicazione filtro per tipo codice: {}", filter.getPdcTipoCodice());
                spec = spec.and(PdcSpecifications.byPdcTipoCodice(filter.getPdcTipoCodice()));
            }
            // CORREZIONE BUG #3: Aggiunta filtro per codice di bilancio ITAS
            if (StringUtils.hasText(filter.getCollegamentoBilItas())) {
                log.debug("Applicazione filtro per codice bilancio ITAS: {}", filter.getCollegamentoBilItas());
                spec = spec.and(PdcSpecifications.byCollegamentoBilItas(filter.getCollegamentoBilItas()));
            }
        } else {
            log.warn("Nessun filtro applicabile perchè il PdcFilter è null");
        }

        log.debug("Specificazione creata, esecuzione query con pageable: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AccrualTPdc> pagedPdcEntities = pdcRepository.findAll(spec, pageable);
        log.info("Query eseguita con successo, trovati {} risultati su un totale di {}", pagedPdcEntities.getContent().size(), pagedPdcEntities.getTotalElements());

        // CORREZIONE BUG #6: Applicare SEMPRE l'ordinamento naturale per codice_ufficiale nelle ricerche PdC
        // Creiamo una copia mutabile della lista per poterla ordinare
        List<AccrualTPdc> sortedContent = new ArrayList<>(pagedPdcEntities.getContent());
        sortedContent.sort(this::compareCodiceUfficialeEntity);
        log.debug("Applicato ordinamento naturale per codice_ufficiale su {} elementi", sortedContent.size());

        // Creiamo una nuova pagina con il contenuto ordinato
        Page<AccrualTPdc> pagedPdcEntitiesSorted = new PageImpl<>(
            sortedContent,
            pagedPdcEntities.getPageable(),
            pagedPdcEntities.getTotalElements()
        );

        PagedPdc result = mapToPagedPdc(pagedPdcEntitiesSorted, annoBilancio);
        log.info("Fine elaborazione searchPdc - risultato: {} elementi", result.getData() != null ? result.getData().size() : 0);
        return result;
    }

    private PagedPdc mapToPagedPdc(Page<AccrualTPdc> pagedPdcEntities, Integer annoBilancio) {
        log.info("Trovati {} risultati per la query", pagedPdcEntities.getTotalElements());
        Set<String> userAzioni = permissionHelper.getUserAzioniCodici();
        Page<Pdc> pagedPdcDtos = pagedPdcEntities.map(entity -> {
            Pdc dto = pdcMapper.toDto(entity);
            dto.setModificabile(isContoModificabile(entity, userAzioni));
            dto.setEliminabile(isContoEliminabile(entity, userAzioni));
            dto.setInseribileFiglio(isContoPadre(entity, userAzioni));

            // Popola il campo segmento con il codice della famiglia
            if (entity.getPdcFamiglia() != null && entity.getPdcFamiglia().getCodice() != null) {
                dto.setSegmento(entity.getPdcFamiglia().getCodice());
            }

            return dto;
        });

        PagedPdc result = new PagedPdc();
        result.setData(pagedPdcDtos.getContent());
        result.setPage(pagedPdcDtos.getNumber());
        result.setPageSize(pagedPdcDtos.getSize());
        result.setTotalElements(pagedPdcDtos.getTotalElements());
        log.info("Ritornando risultato con {} elementi su un totale di {}", result.getData().size(), result.getTotalElements());
        return result;
    }



    @Override
    @Transactional
    public Pdc getPdcByCodiceUfficialeClasseFamiglia(String codiceUfficiale, String codiceClasse, String codiceFamiglia) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        log.info("Richiesta getPdcByCodiceUfficialeClasseFamiglia per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {} e enteCodice: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia, enteCodice);

        // Cerchiamo usando la chiave logica completa: codice ufficiale + classe + famiglia + ente
        // Otteniamo l'ente per ottenere l'id
        Optional<AccrualTEnte> enteOpt = enteRepository.findByCodiceUnivoco(enteCodice);
        if (!enteOpt.isPresent()) {
            throw new NotFoundException("Ente non trovato con codice fiscale: " + enteCodice);
        }
        AccrualTEnte ente = enteOpt.get();

        // Otteniamo la classe dal codice
        Optional<AccrualDPdcClasse> classeOpt = pdcClasseRepository.findByCodice(codiceClasse);
        if (!classeOpt.isPresent()) {
            throw new NotFoundException("Classe non trovata con codice: " + codiceClasse);
        }
        AccrualDPdcClasse classe = classeOpt.get();

        // Otteniamo la famiglia dal codice
        Optional<AccrualTPdcFamiglia> famigliaOpt = pdcFamigliaRepository.findByCodice(codiceFamiglia);
        if (!famigliaOpt.isPresent()) {
            throw new NotFoundException("Famiglia non trovata con codice: " + codiceFamiglia);
        }
        AccrualTPdcFamiglia famiglia = famigliaOpt.get();

        // Cerchiamo il record usando la chiave completa (escludendo i record cancellati)
        Optional<AccrualTPdc> pdcEntityOpt = pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(codiceUfficiale, classe.getId(), famiglia.getId(), ente.getId());

        if (!pdcEntityOpt.isPresent()) {
            throw new NotFoundException("PDC non trovato con codice ufficiale: " + codiceUfficiale +
                                        ", classe: " + codiceClasse +
                                        ", famiglia: " + codiceFamiglia +
                                        " per l'ente: " + enteCodice);
        }

        AccrualTPdc pdcEntity = pdcEntityOpt.get();

        Set<String> userAzioni = permissionHelper.getUserAzioniCodici();
        Pdc dto = pdcMapper.toDto(pdcEntity);
        dto.setModificabile(isContoModificabile(pdcEntity, userAzioni));
        dto.setEliminabile(isContoEliminabile(pdcEntity, userAzioni));
        dto.setInseribileFiglio(isContoPadre(pdcEntity, userAzioni));

        // Calcola il codice_completo secondo la logica specificata
        String codiceCompleto = calcolaCodiceCompleto(pdcEntity);
        dto.setCodiceCompleto(codiceCompleto);

        // Popola il campo segmento con il codice della famiglia
        if (pdcEntity.getPdcFamiglia() != null && pdcEntity.getPdcFamiglia().getCodice() != null) {
            dto.setSegmento(pdcEntity.getPdcFamiglia().getCodice());
        }

        // Popola il campo pdc_padre se esiste
        if (pdcEntity.getPdcPadre() != null) {
            Pdc padreDto = pdcMapper.toDto(pdcEntity.getPdcPadre());
            dto.setPdcPadre(padreDto);
        }

        return dto;
    }

    @Override
    @Transactional
    public Pdc createPdc(Pdc pdcDto) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        // CDU-003 5.3: Logica di salvataggio (Inserimento) - Mapping salvataggio DB e side effect
        // Implementare "Inserisci conto Accrual"
        // 1. Trova padre
        AccrualTPdc padre = null;
        if (pdcDto.getPdcPadre() != null && pdcDto.getPdcPadre().getId() != null) {
            padre = pdcRepository.findById(pdcDto.getPdcPadre().getId())
                .orElseThrow(() -> new IllegalArgumentException("Conto padre non trovato: " + pdcDto.getPdcPadre().getId()));
        }

        // 2. Recupera l'ente dal codice fiscale fornito nell'header
        AccrualTEnte ente = null;
        if (enteCodice != null && !enteCodice.trim().isEmpty()) {
            ente = enteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice));
        } else {
            throw new IllegalArgumentException("Il codice fiscale dell'ente è obbligatorio per la creazione del conto");
        }

        // 3. Mappa il DTO in una nuova entità
        AccrualTPdc pdcEntity = pdcMapper.toEntity(pdcDto);

        // 4. Calcola i campi derivati
        // CDU-003 2.10.4: codice e codiceUfficiale devono contenere entrambi il codice completo
        // Il frontend invia già entrambi i campi valorizzati correttamente, non ricalcolare
        // Commentato perché il codice ufficiale è già valorizzato dal DTO
        /*
        if ((pdcEntity.getCodiceUfficiale() == null || pdcEntity.getCodiceUfficiale().isEmpty()) && padre != null) {
            String codiceUfficiale = padre.getCodiceUfficiale() + "." + pdcEntity.getCodice();
            pdcEntity.setCodiceUfficiale(codiceUfficiale);
        } else if (padre == null && (pdcEntity.getCodiceUfficiale() == null || pdcEntity.getCodiceUfficiale().isEmpty())) {
            // Se non c'è padre e il codice ufficiale non è stato fornito, usiamo solo il codice
            pdcEntity.setCodiceUfficiale(pdcEntity.getCodice());
        }
        */
        // Se il codice ufficiale è già presente nel DTO e non c'è un padre, lo lasciamo così com'è

        Integer livello = 1;
        if (padre != null) {
            livello = padre.getLivello() + 1;
        }
        pdcEntity.setLivello(livello);

        // 4.1 CDU-003 2.10.4: La classe, il tipo e lo stato devono essere ereditati dal conto padre
        if (padre != null) {
            // Ereditarietà classe dal padre
            pdcEntity.setPdcClasse(padre.getPdcClasse());
            // Ereditarietà tipo dal padre (CDU-003 2.10.4: "tipo: stesso tipo del conto padre")
            pdcEntity.setPdcTipo(padre.getPdcTipo());
            // Ereditarietà stato dal padre (CDU-003 2.10.4: "stato: stesso stato del conto padre")
            pdcEntity.setPdcStato(padre.getPdcStato());
        }

        // 4.2 CDU-003 2.10.4: Impostazione famiglia 'ACCRUAL-UTENTE' per nuovi conti
        AccrualTPdcFamiglia famigliaUtente = pdcFamigliaRepository.findByCodice("ACCRUAL-UTENTE")
            .orElseThrow(() -> new IllegalArgumentException("Famiglia 'ACCRUAL-UTENTE' non trovata"));
        pdcEntity.setPdcFamiglia(famigliaUtente);

        // 5. Assegna l'ente all'entità PDC
        pdcEntity.setEnte(ente);

        pdcEntity.setFoglia(true); // I nuovi conti sono sempre foglia inizialmente
        pdcEntity.setPdcPadre(padre);

        // 6. Esegui la validazione sull'entità popolata
        pdcValidator.validateForSave(pdcEntity, true);

        // 7. Aggiorna `flag_foglia` del padre
        if (padre != null) {
            aggiornaFogliaPadre(padre.getId(), false);
        }

        // 8. Salva nuovo conto
        AccrualTPdc savedPdc = pdcRepository.save(pdcEntity);
        return pdcMapper.toDto(savedPdc);
    }

    /**
     * Aggiorna solo il flag foglia di un conto padre.
     * Utilizzato quando si inserisce un nuovo conto figlio.
     * @param padreId ID del conto padre
     * @param foglia valore del flag foglia
     */
    private void aggiornaFogliaPadre(Long padreId, boolean foglia) {
        AccrualTPdc padre = pdcRepository.findById(padreId)
            .orElseThrow(() -> new IllegalArgumentException("Padre non trovato con ID: " + padreId));
        padre.setFoglia(foglia);
        pdcRepository.save(padre);
        log.debug("Aggiornato flag foglia del padre {} a {}", padreId, foglia);
    }

    @Override
    @Transactional
    public Pdc updatePdc(String codicePdc, Pdc pdcDto) {
        log.info("Richiesta updatePdc per codice: {}", codicePdc);

        // 1. Trovare entity by id (solo se non cancellata)
        AccrualTPdc existingPdc = pdcRepository.findByCodiceAndDataCancellazioneIsNull(codicePdc);
        if (existingPdc == null) {
            log.error("PDC non trovato con codice: {}", codicePdc);

            // Cerchiamo se esiste un record cancellato
            AccrualTPdc existingPdcDeleted = pdcRepository.findByCodice(codicePdc);
            if (existingPdcDeleted != null) {
                log.warn("Trovato PDC cancellato con codice: {} e data cancellazione: {}",
                         codicePdc, existingPdcDeleted.getDataCancellazione());
            } else {
                log.warn("Nessun PDC trovato con codice: {}", codicePdc);
            }

            throw new NotFoundException("PDC non trovato con codice: " + codicePdc);
        }

        log.info("PDC trovato con ID: {}", existingPdc.getId());

        // 2. Check di concorrenza con il campo 'version'
        if (pdcDto.getVersion() != null && !pdcDto.getVersion().equals(existingPdc.getVersion())) {
            throw new OptimisticLockException("Il conto è stato modificato da un altro utente. Ricaricare la pagina e riprovare.");
        }

        // 3. Salvare i vecchi valori per confronto successivo
        Boolean oldFineValidita = existingPdc.getFineValidita() != null;
        String oldStato = existingPdc.getPdcStato() != null ? existingPdc.getPdcStato().getCodice() : null;
        boolean hasChildren = pdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(existingPdc.getId());

        // 4. Aggiorna i campi dell'entitá esistente con i nuovi valori
        // Non aggiornare la versione tramite mapper per evitare conflitti con il controllo di concorrenza
        existingPdc.setCodice(pdcDto.getCodice());
        existingPdc.setDescrizione(pdcDto.getDescrizione());
        existingPdc.setFoglia(pdcDto.isFoglia());
        existingPdc.setFondoDotazione(pdcDto.isFondoDotazione());
        existingPdc.setPartita(pdcDto.isPartita());
        existingPdc.setInizioValidita(pdcDto.getInizioValidita() != null ? pdcDto.getInizioValidita().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        existingPdc.setFineValidita(pdcDto.getFineValidita() != null ? pdcDto.getFineValidita().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        existingPdc.setOrdine(pdcDto.getOrdine());
        existingPdc.setCollegamentoBilItas(pdcDto.getCollegamentoBilItas());
        existingPdc.setLivello(pdcDto.getLivello());
        // I campi siac e ammPartita non sono presenti nel DTO Pdc generato da Swagger
        // quindi non possiamo aggiornarli direttamente dal DTO
        // Lasciamo i valori esistenti o impostiamo valori di default se necessario
        // existingPdc.setSiac(pdcDto.isSiac()); // Campo non disponibile nel DTO
        // existingPdc.setAmmPartita(pdcDto.isAmmPartita()); // Campo non disponibile nel DTO

        // CDU-003 2.10.3: Il codice_ufficiale NON viene ricalcolato in update perché è una chiave logica
        // che non dovrebbe mai cambiare dopo l'inserimento. Il frontend invia già codice e codiceUfficiale
        // entrambi valorizzati correttamente, quindi non è necessario alcun ricalcolo

        // 6. Verifica che Fine Validità sia maggiore o uguale a Inizio Validità - CDU-003 5.1: La data di fine validità deve essere maggiore o uguale alla data di inizio validità (WEB_ERR_0007)
        if (existingPdc.getInizioValidita() != null && existingPdc.getFineValidita() != null) {
            if (existingPdc.getFineValidita().isBefore(existingPdc.getInizioValidita())) {
                throw new IllegalStateException("La data di fine validità deve essere successiva o uguale alla data di inizio validità");
            }
        }

        // 7. Verifica che non esistano prime note successive alla nuova data di fine validità - CDU-003 5.1: Se fineValidità è valorizzata, verifica che non esistano prime note in stato diverso da 'Annullato' collegate al conto oppure ai suoi conti figli, con data registrazione successiva a fineValidità (WEB_ERR_0027)
        if (existingPdc.getFineValidita() != null) {
            List<Long> idConti = getIdContiESottoConti(existingPdc.getId());
            boolean primeNoteSuccessive = pdcRepository.esistonoPrimeNoteSuccessive(
                idConti,
                existingPdc.getFineValidita().toLocalDate()
            );
            if (primeNoteSuccessive) {
                throw new IllegalStateException("Impossibile impostare la fine validità perché esistono prime note successive a questa data");
            }
        }

        // 8. Esegui la validazione sull'entitá aggiornata
        pdcValidator.validateForSave(existingPdc, false);

        // 7. Gestione aggiornamento conti figli come richiesto dal CDU-003 V01
        if (hasChildren) {
            // Se il conto ha figli e l'operatore ha aggiunto fineValidita
            if (!oldFineValidita && existingPdc.getFineValidita() != null) {
                // Impostare la stessa fine validità ai figli
                aggiornaFigliConNuovaFineValidita(existingPdc.getId(), existingPdc.getFineValidita());
            }
            // Se il conto ha figli e l'operatore ha tolto fineValidita
            else if (oldFineValidita && existingPdc.getFineValidita() == null) {
                // Rimuovere la fine validità dai figli
                aggiornaFigliRimuovendoFineValidita(existingPdc.getId());
            }
            // Se il conto ha figli e l'operatore ha cambiato lo stato da 'Attivo' a 'Non Attivo'
            else if ("ATTIVO".equals(oldStato) && existingPdc.getPdcStato() != null && "NON_ATTIVO".equals(existingPdc.getPdcStato().getCodice())) {
                // Impostare lo stato 'Non Attivo' ai figli
                aggiornaFigliConStato(existingPdc.getId(), "NON_ATTIVO");
            }
            // Se il conto ha figli e l'operatore ha cambiato lo stato da 'Non Attivo' a 'Attivo'
            else if ("NON_ATTIVO".equals(oldStato) && existingPdc.getPdcStato() != null && "ATTIVO".equals(existingPdc.getPdcStato().getCodice())) {
                // Impostare lo stato 'Attivo' ai figli
                aggiornaFigliConStato(existingPdc.getId(), "ATTIVO");
            }
        }

        // 8. Salva le modifiche
        AccrualTPdc updatedPdc = pdcRepository.save(existingPdc);
        return pdcMapper.toDto(updatedPdc);
    }

    @Override
    @Transactional
    public Pdc updatePdcByTripleKey(String codiceUfficiale, String codiceClasse, String codiceFamiglia, Pdc pdcDto) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        log.info("Richiesta updatePdcByTripleKey per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {} e enteCodice: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia, enteCodice);

        // 1. Recupero ente, classe e famiglia
        log.debug("Recupero ente con codice fiscale: {}", enteCodice);
        Optional<AccrualTEnte> enteOpt = enteRepository.findByCodiceUnivoco(enteCodice);
        if (!enteOpt.isPresent()) {
            log.error("Ente non trovato con codice fiscale: {}", enteCodice);
            throw new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice);
        }
        AccrualTEnte ente = enteOpt.get();
        log.debug("Ente trovato con ID: {}", ente.getId());

        log.debug("Recupero classe con codice: {}", codiceClasse);
        Optional<AccrualDPdcClasse> classeOpt = pdcClasseRepository.findByCodice(codiceClasse);
        if (!classeOpt.isPresent()) {
            log.error("Classe non trovata con codice: {}", codiceClasse);
            throw new IllegalArgumentException("Classe non trovata con codice: " + codiceClasse);
        }
        AccrualDPdcClasse classe = classeOpt.get();
        log.debug("Classe trovata con ID: {}", classe.getId());

        log.debug("Recupero famiglia con codice: {}", codiceFamiglia);
        Optional<AccrualTPdcFamiglia> famigliaOpt = pdcFamigliaRepository.findByCodice(codiceFamiglia);
        if (!famigliaOpt.isPresent()) {
            log.error("Famiglia non trovata con codice: {}", codiceFamiglia);
            throw new IllegalArgumentException("Famiglia non trovata con codice: " + codiceFamiglia);
        }
        AccrualTPdcFamiglia famiglia = famigliaOpt.get();
        log.debug("Famiglia trovata con ID: {}", famiglia.getId());

        // 2. Trovare entity by triple key (solo se non cancellata)
        log.debug("Ricerca PDC con codiceUfficiale: {}, classeId: {}, famigliaId: {}, enteId: {}",
                  codiceUfficiale, classe.getId(), famiglia.getId(), ente.getId());

        Optional<AccrualTPdc> pdcOpt = pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(codiceUfficiale, classe.getId(), famiglia.getId(), ente.getId());

        if (!pdcOpt.isPresent()) {
            log.error("PDC non trovato con codice ufficiale: {} (classe: {}, famiglia: {}) per l'ente: {}",
                      codiceUfficiale, codiceClasse, codiceFamiglia, enteCodice);

            // Cerchiamo di capire se esiste un record cancellato o con altri parametri
            Optional<AccrualTPdc> pdcWithCode = pdcRepository.findByCodiceUfficialeAndEnte_Id(codiceUfficiale, ente.getId());
            if (pdcWithCode.isPresent()) {
                log.warn("Trovato PDC con codice ufficiale {} ma con diversa classe o famiglia", codiceUfficiale);
                log.warn("Dettagli PDC trovato: classeId={}, classeCodice={}, famigliaId={}, famigliaCodice={}, dataCancellazione={}",
                         pdcWithCode.get().getPdcClasse().getId(), pdcWithCode.get().getPdcClasse().getCodice(),
                         pdcWithCode.get().getPdcFamiglia().getId(), pdcWithCode.get().getPdcFamiglia().getCodice(),
                         pdcWithCode.get().getDataCancellazione());
            } else {
                log.warn("Nessun PDC attivo trovato con codice ufficiale {} per l'ente {}", codiceUfficiale, enteCodice);

                // Cerchiamo anche record cancellati
                Optional<AccrualTPdc> pdcWithCodeDeleted = pdcRepository.findByCodiceUfficialeAndEnte_Id(codiceUfficiale, ente.getId());
                if (pdcWithCodeDeleted.isPresent() && pdcWithCodeDeleted.get().getDataCancellazione() != null) {
                    log.warn("Trovato PDC cancellato con codice ufficiale {}: classeCodice={}, famigliaCodice={}",
                             codiceUfficiale, pdcWithCodeDeleted.get().getPdcClasse().getCodice(),
                             pdcWithCodeDeleted.get().getPdcFamiglia().getCodice());
                }
            }

            throw new NotFoundException("PDC non trovato con codice ufficiale: " + codiceUfficiale +
                                        ", classe: " + codiceClasse +
                                        ", famiglia: " + codiceFamiglia +
                                        " per l'ente: " + enteCodice);
        }

        AccrualTPdc existingPdc = pdcOpt.get();
        log.info("PDC trovato con ID: {}", existingPdc.getId());

        // 3. Check di concorrenza con il campo 'version'
        if (pdcDto.getVersion() != null && !pdcDto.getVersion().equals(existingPdc.getVersion())) {
            throw new OptimisticLockException("Il conto è stato modificato da un altro utente. Ricaricare la pagina e riprovare.");
        }

        // 4. Salvare i vecchi valori per confronto successivo
        Boolean oldFineValidita = existingPdc.getFineValidita() != null;
        String oldStato = existingPdc.getPdcStato() != null ? existingPdc.getPdcStato().getCodice() : null;
        boolean hasChildren = pdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(existingPdc.getId());

        // 5. Aggiorna i campi dell'entitá esistente con i nuovi valori
        // Controllare se il DTO contiene un campo id e loggare un warning se presente
        if (pdcDto.getId() != null) {
            log.warn("Campo 'id' ricevuto nel payload ma ignorato. L'identificazione del record avviene tramite tripletta (codiceUfficiale={}, codiceClasse={}, codiceFamiglia={})",
                     codiceUfficiale, codiceClasse, codiceFamiglia);
        }

        // Aggiornare solo i campi diretti del PDC senza toccare le entità correlate
        // per evitare problemi di mapping con i tipi enum nel database
        existingPdc.setCodice(pdcDto.getCodice());
        existingPdc.setDescrizione(pdcDto.getDescrizione());
        existingPdc.setFoglia(pdcDto.isFoglia() != null ? pdcDto.isFoglia() : false);
        existingPdc.setFondoDotazione(pdcDto.isFondoDotazione() != null ? pdcDto.isFondoDotazione() : false);
        existingPdc.setPartita(pdcDto.isPartita() != null ? pdcDto.isPartita() : false);
        existingPdc.setInizioValidita(pdcDto.getInizioValidita() != null ? pdcDto.getInizioValidita().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        existingPdc.setFineValidita(pdcDto.getFineValidita() != null ? pdcDto.getFineValidita().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        existingPdc.setOrdine(pdcDto.getOrdine());
        existingPdc.setCollegamentoBilItas(pdcDto.getCollegamentoBilItas());
        // I campi siac e ammPartita non sono presenti nel DTO Pdc generato da Swagger
        // quindi non possiamo aggiornarli direttamente dal DTO
        // Lasciamo i valori esistenti o impostiamo valori di default se necessario
        // existingPdc.setSiac(pdcDto.isSiac()); // Campo non disponibile nel DTO
        // existingPdc.setAmmPartita(pdcDto.isAmmPartita()); // Campo non disponibile nel DTO
        existingPdc.setLivello(pdcDto.getLivello());
        // Incrementare la versione per il lock ottimistico (il framework JPA si occuperà del controllo)
        // Non impostiamo la versione dal DTO, ma lasciamo che JPA la gestisca automaticamente

        // CDU-003 V01: La classe non è modificabile, si eredita quella del conto padre
        // Pertanto, non aggiorniamo la classe dal DTO ma manteniamo quella esistente
        // o quella ereditata dal padre se il padre cambia
        AccrualDPdcClasse classeEsistente = existingPdc.getPdcClasse();

        // Se il padre cambia, ereditiamo la classe dal nuovo padre
        if (pdcDto.getPdcPadre() != null && pdcDto.getPdcPadre().getId() != null) {
            AccrualTPdc nuovoPdcPadre = pdcRepository.findById(pdcDto.getPdcPadre().getId())
                .orElseThrow(() -> new IllegalArgumentException("PDC Padre non trovato con ID: " + pdcDto.getPdcPadre().getId()));

            // Se non aveva padre o il padre sta cambiando, ereditiamo la classe dal nuovo padre
            if (existingPdc.getPdcPadre() == null || !existingPdc.getPdcPadre().getId().equals(nuovoPdcPadre.getId())) {
                // Carichiamo esplicitamente solo l'ID della classe per evitare aggiornamenti imprevisti
                // Questo previene il problema di tipo SQL per il campo pdc_classe_segno
                AccrualDPdcClasse classeDelPadre = nuovoPdcPadre.getPdcClasse();
                AccrualDPdcClasse classeCaricata = pdcClasseRepository.findById(classeDelPadre.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Classe del padre non trovata con ID: " + classeDelPadre.getId()));

                existingPdc.setPdcClasse(classeCaricata);
                log.info("Ereditata classe dal nuovo padre: {}", nuovoPdcPadre.getPdcClasse().getCodice());
            } else {
                // Altrimenti manteniamo la classe esistente
                // Carichiamo esplicitamente solo l'ID della classe per evitare aggiornamenti imprevisti
                AccrualDPdcClasse classeEsistenteCaricata = pdcClasseRepository.findById(classeEsistente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Classe esistente non trovata con ID: " + classeEsistente.getId()));

                existingPdc.setPdcClasse(classeEsistenteCaricata);
            }

            existingPdc.setPdcPadre(nuovoPdcPadre);
        } else if (pdcDto.getPdcPadre() != null && pdcDto.getPdcPadre().getId() == null) {
            // Se il padre viene rimosso (impostato a null), manteniamo la classe esistente
            // Carichiamo esplicitamente solo l'ID della classe per evitare aggiornamenti imprevisti
            AccrualDPdcClasse classeEsistenteCaricata = pdcClasseRepository.findById(classeEsistente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Classe esistente non trovata con ID: " + classeEsistente.getId()));

            existingPdc.setPdcPadre(null);
            existingPdc.setPdcClasse(classeEsistenteCaricata);
        } else {
            // Se il padre non cambia, manteniamo la classe esistente
            // Carichiamo esplicitamente solo l'ID della classe per evitare aggiornamenti imprevisti
            AccrualDPdcClasse classeEsistenteCaricata = pdcClasseRepository.findById(classeEsistente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Classe esistente non trovata con ID: " + classeEsistente.getId()));

            existingPdc.setPdcClasse(classeEsistenteCaricata);
        }

        if (pdcDto.getPdcTipo() != null && pdcDto.getPdcTipo().getId() != null) {
            AccrualDPdcTipo pdcTipo = pdcTipoRepository.findById(pdcDto.getPdcTipo().getId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo PDC non trovato con ID: " + pdcDto.getPdcTipo().getId()));
            existingPdc.setPdcTipo(pdcTipo);
        }

        if (pdcDto.getPdcStato() != null && pdcDto.getPdcStato().getId() != null) {
            AccrualDPdcStato pdcStato = pdcStatoRepository.findById(pdcDto.getPdcStato().getId())
                .orElseThrow(() -> new IllegalArgumentException("Stato PDC non trovato con ID: " + pdcDto.getPdcStato().getId()));
            existingPdc.setPdcStato(pdcStato);
        }

        if (pdcDto.getPdcFamiglia() != null && pdcDto.getPdcFamiglia().getId() != null) {
            AccrualTPdcFamiglia pdcFamiglia = pdcFamigliaRepository.findById(pdcDto.getPdcFamiglia().getId())
                .orElseThrow(() -> new IllegalArgumentException("Famiglia PDC non trovata con ID: " + pdcDto.getPdcFamiglia().getId()));
            existingPdc.setPdcFamiglia(pdcFamiglia);
        }

        // Aggiornare il riferimento al padre se presente
        if (pdcDto.getPdcPadre() != null && pdcDto.getPdcPadre().getId() != null) {
            AccrualTPdc pdcPadre = pdcRepository.findById(pdcDto.getPdcPadre().getId())
                .orElseThrow(() -> new IllegalArgumentException("PDC Padre non trovato con ID: " + pdcDto.getPdcPadre().getId()));
            existingPdc.setPdcPadre(pdcPadre);
        }

        // 6. Codice ufficiale
        // CDU-003 2.10.3: codice e codiceUfficiale devono contenere entrambi il codice completo
        // Il frontend invia già entrambi i campi valorizzati correttamente, non ricalcolare
        // existingPdc.setCodiceUfficiale() è già stato impostato dal mapper dal DTO
        /*
        if (existingPdc.getPdcPadre() != null) {
            existingPdc.setCodiceUfficiale(existingPdc.getPdcPadre().getCodiceUfficiale() + "." + existingPdc.getCodice());
        } else {
            existingPdc.setCodiceUfficiale(existingPdc.getCodice());
        }
        */

        // 7. Verifica che Fine Validità sia maggiore o uguale a Inizio Validità - CDU-003 5.1: La data di fine validità deve essere maggiore o uguale alla data di inizio validità (WEB_ERR_0007)
        if (existingPdc.getInizioValidita() != null && existingPdc.getFineValidita() != null) {
            if (existingPdc.getFineValidita().isBefore(existingPdc.getInizioValidita())) {
                throw new IllegalStateException("La data di fine validità deve essere successiva o uguale alla data di inizio validità");
            }
        }

        // 8. Verifica che non esistano prime note successive alla nuova data di fine validità - CDU-003 5.1: Se fineValidità è valorizzata, verifica che non esistano prime note in stato diverso da 'Annullato' collegate al conto oppure ai suoi conti figli, con data registrazione successiva a fineValidità (WEB_ERR_0027)
        if (existingPdc.getFineValidita() != null) {
            List<Long> idConti = getIdContiESottoConti(existingPdc.getId());
            boolean primeNoteSuccessive = pdcRepository.esistonoPrimeNoteSuccessive(
                idConti,
                existingPdc.getFineValidita().toLocalDate()
            );
            if (primeNoteSuccessive) {
                throw new IllegalStateException("Impossibile impostare la fine validità perché esistono prime note successive a questa data");
            }
        }

        // 9. Esegui la validazione sull'entitá aggiornata
        pdcValidator.validateForSave(existingPdc, false);

        // 8. Gestione aggiornamento conti figli come richiesto dal CDU-003 V01
        if (hasChildren) {
            // Se il conto ha figli e l'operatore ha aggiunto fineValidita
            if (!oldFineValidita && existingPdc.getFineValidita() != null) {
                // Impostare la stessa fine validità ai figli
                aggiornaFigliConNuovaFineValidita(existingPdc.getId(), existingPdc.getFineValidita());
            }
            // Se il conto ha figli e l'operatore ha tolto fineValidita
            else if (oldFineValidita && existingPdc.getFineValidita() == null) {
                // Rimuovere la fine validità dai figli
                aggiornaFigliRimuovendoFineValidita(existingPdc.getId());
            }
            // Se il conto ha figli e l'operatore ha cambiato lo stato da 'Attivo' a 'Non Attivo'
            else if ("ATTIVO".equals(oldStato) && existingPdc.getPdcStato() != null && "NON_ATTIVO".equals(existingPdc.getPdcStato().getCodice())) {
                // Impostare lo stato 'Non Attivo' ai figli
                aggiornaFigliConStato(existingPdc.getId(), "NON_ATTIVO");
            }
            // Se il conto ha figli e l'operatore ha cambiato lo stato da 'Non Attivo' a 'Attivo'
            else if ("NON_ATTIVO".equals(oldStato) && existingPdc.getPdcStato() != null && "ATTIVO".equals(existingPdc.getPdcStato().getCodice())) {
                // Impostare lo stato 'Attivo' ai figli
                aggiornaFigliConStato(existingPdc.getId(), "ATTIVO");
            }
        }

        // 9. Salva le modifiche
        AccrualTPdc updatedPdc = pdcRepository.save(existingPdc);
        return pdcMapper.toDto(updatedPdc);
    }

    /**
     * CDU-003 2.10.4:
     * entity.codice contiene già il codice completo (es. 2.3.4.0.0.5)
     */
    private String calcolaCodiceCompleto(AccrualTPdc pdcEntity) {
        return pdcEntity.getCodice();
    }

    /**
     * Aggiorna tutti i figli di un conto impostando la stessa fine validità
     */
    private void aggiornaFigliConNuovaFineValidita(Long idPadre, LocalDateTime nuovaFineValidita) {
        List<AccrualTPdc> figli = pdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(idPadre);
        for (AccrualTPdc figlio : figli) {
            figlio.setFineValidita(nuovaFineValidita);
            // Impostare data e utente modifica
            figlio.setDataAggiornamento(LocalDateTime.now());
            figlio.setUtenteAggiornamento(getCurrentUserId());
            pdcRepository.save(figlio);

            // Richiamare ricorsivamente per aggiornare i sottofigli
            aggiornaFigliConNuovaFineValidita(figlio.getId(), nuovaFineValidita);
        }
    }

    /**
     * Aggiorna tutti i figli di un conto rimuovendo la fine validità
     */
    private void aggiornaFigliRimuovendoFineValidita(Long idPadre) {
        List<AccrualTPdc> figli = pdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(idPadre);
        for (AccrualTPdc figlio : figli) {
            figlio.setFineValidita(null);
            // Impostare data e utente modifica
            figlio.setDataAggiornamento(LocalDateTime.now());
            figlio.setUtenteAggiornamento(getCurrentUserId());
            pdcRepository.save(figlio);

            // Richiamare ricorsivamente per aggiornare i sottofigli
            aggiornaFigliRimuovendoFineValidita(figlio.getId());
        }
    }

    /**
     * Aggiorna tutti i figli di un conto impostando un nuovo stato
     */
    private void aggiornaFigliConStato(Long idPadre, String nuovoStatoCodice) {
        List<AccrualTPdc> figli = pdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(idPadre);
        for (AccrualTPdc figlio : figli) {
            // Trovare lo stato dal codice e aggiornare
            // Dobbiamo ottenere lo stato dal repository usando il codice
            AccrualDPdcStato nuovoStato = pdcStatoRepository.findByCodice(nuovoStatoCodice);
            if (nuovoStato != null) {
                figlio.setPdcStato(nuovoStato);

                // Aggiornare data e utente modifica
                figlio.setDataAggiornamento(LocalDateTime.now());
                figlio.setUtenteAggiornamento(getCurrentUserId());
            }

            pdcRepository.save(figlio);

            // Richiamare ricorsivamente per aggiornare i sottofigli
            aggiornaFigliConStato(figlio.getId(), nuovoStatoCodice);
        }
    }

    @Override
    @Transactional
    public void deletePdc(String codicePdc) {
        // 1. Trovare entity by id (solo se non cancellata)
        AccrualTPdc pdc = pdcRepository.findByCodiceAndDataCancellazioneIsNull(codicePdc);
        if (pdc == null) {
            throw new NotFoundException("PDC non trovato con codice: " + codicePdc);
        }

        // 2. Implementare "Verifica conto Accrual da eliminare"
        pdcValidator.validateForDelete(pdc.getId());

        // 3. Se non ci sono legami, eseguire "Elimina conto Accrual" (cancellazione logica)
        pdc.setDataCancellazione(LocalDateTime.now());
        pdc.setUtenteCancellazione(getCurrentUserId());
        pdcRepository.save(pdc);
    }

    @Override
    @Transactional
    public void deletePdcByTripleKey(String codiceUfficiale, String codiceClasse, String codiceFamiglia) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        log.info("Richiesta deletePdcByTripleKey per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {} e enteCodice: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia, enteCodice);

        // 1. Recupero ente, classe e famiglia
        AccrualTEnte ente = enteRepository.findByCodiceUnivoco(enteCodice)
            .orElseThrow(() -> new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice));

        AccrualDPdcClasse classe = pdcClasseRepository.findByCodice(codiceClasse)
            .orElseThrow(() -> new IllegalArgumentException("Classe non trovata con codice: " + codiceClasse));

        AccrualTPdcFamiglia famiglia = pdcFamigliaRepository.findByCodice(codiceFamiglia)
            .orElseThrow(() -> new IllegalArgumentException("Famiglia non trovata con codice: " + codiceFamiglia));

        // 2. Trovare entity by triple key (solo se non cancellata)
        Optional<AccrualTPdc> pdcOpt = pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(codiceUfficiale, classe.getId(), famiglia.getId(), ente.getId());

        if (!pdcOpt.isPresent()) {
            log.error("PDC non trovato con codice ufficiale: {} (classe: {}, famiglia: {}) per l'ente: {}",
                      codiceUfficiale, codiceClasse, codiceFamiglia, enteCodice);

            // Cerchiamo anche record cancellati
            Optional<AccrualTPdc> pdcWithCodeDeleted = pdcRepository.findByCodiceUfficialeAndEnte_Id(codiceUfficiale, ente.getId());
            if (pdcWithCodeDeleted.isPresent() && pdcWithCodeDeleted.get().getDataCancellazione() != null) {
                log.warn("Trovato PDC cancellato con codice ufficiale {}: classeCodice={}, famigliaCodice={}",
                         codiceUfficiale, pdcWithCodeDeleted.get().getPdcClasse().getCodice(),
                         pdcWithCodeDeleted.get().getPdcFamiglia().getCodice());
            }

            throw new NotFoundException("PDC non trovato con codice ufficiale: " + codiceUfficiale +
                                        ", classe: " + codiceClasse +
                                        ", famiglia: " + codiceFamiglia +
                                        " per l'ente: " + enteCodice);
        }

        AccrualTPdc pdc = pdcOpt.get();
        log.info("PDC trovato con ID: {} da eliminare", pdc.getId());

        // 3. Implementare "Verifica conto Accrual da eliminare"
        pdcValidator.validateForDelete(pdc.getId());

        // 4. Se non ci sono legami, eseguire "Elimina conto Accrual" (cancellazione logica)
        pdc.setDataCancellazione(LocalDateTime.now());
        pdc.setUtenteCancellazione(getCurrentUserId());
        pdcRepository.save(pdc);

        log.info("PDC con ID: {} eliminato con successo", pdc.getId());
    }

    private String getCurrentUserId() {
        String cf = securityContextHelper.getCodiceFiscale();
        return cf != null ? cf : "anonymous";
    }


    /**
     * Traduce l'algoritmo 2.6.3 del CDU.
     * Un conto e' modificabile se: utente ha OP-SPE-updPdc e famiglia.flagModificabile=true
     */
    private boolean isContoModificabile(AccrualTPdc conto, Set<String> userAzioni) {
        boolean hasPermission = userAzioni.contains("OP-SPE-updPdc");
        boolean isFamigliaModificabile = conto.getPdcFamiglia() != null && conto.getPdcFamiglia().getModificabile();
        return hasPermission && isFamigliaModificabile;
    }

    /**
     * Traduce l'algoritmo 2.6.4 del CDU.
     * Un conto e' eliminabile se: utente ha OP-SPE-delPdc, famiglia.flagModificabile=true, conto.flagFoglia=true
     */
    private boolean isContoEliminabile(AccrualTPdc conto, Set<String> userAzioni) {
        boolean hasPermission = userAzioni.contains("OP-SPE-delPdc");
        boolean isFamigliaModificabile = conto.getPdcFamiglia() != null && conto.getPdcFamiglia().getModificabile();
        boolean isFoglia = conto.isFoglia();
        return hasPermission && isFamigliaModificabile && isFoglia;
    }

    /**
     * Traduce l'algoritmo 2.6.5 del CDU.
     * Un conto puo' avere figli se:
     * 1. L'utente ha il permesso OP-SPE-addPdc
     * 2. La fase operativa del bilancio non e' PREVISIONE o CHIUSO
     * 3. Il conto NON e' di segmento A con figli di segmento B (CORREZIONE BUG #4)
     */
    private boolean isContoPadre(AccrualTPdc conto, Set<String> userAzioni) {
        // 1. Controllo permesso utente OP-SPE-addPdc
        boolean hasPermission = userAzioni.contains("OP-SPE-addPdc");

        // 2. Controllo fase operativa bilancio (no PREVISIONE/CHIUSO)
        boolean isFaseOperativaValida = isFaseOperativaValida(conto.getEnte().getCodiceUnivoco());

        // 3. Controllo se il conto è di segmento A e ha figli di segmento B (CDU-002 2.6.5)
        // Blocco solo se esiste almeno un figlio di famiglia ACCRUAL-B: figli ACCRUAL-UTENTE
        // sono compatibili e non devono impedire l'inserimento di ulteriori figli.
        boolean isSegmentoAConFigliSegmentoB = false;
        if (conto.getPdcFamiglia() != null && "ACCRUAL-A".equals(conto.getPdcFamiglia().getCodice())) {
            if (conto.getId() != null) {
                List<AccrualTPdc> figli = pdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(conto.getId());
                isSegmentoAConFigliSegmentoB = figli.stream()
                    .anyMatch(figlio -> figlio.getPdcFamiglia() != null &&
                                       "ACCRUAL-B".equals(figlio.getPdcFamiglia().getCodice()));
            }
        }

        boolean isNotSegmentoAConFigliB = !isSegmentoAConFigliSegmentoB;

        return hasPermission && isFaseOperativaValida && isNotSegmentoAConFigliB;
    }

    /**
     * Verifica se la fase operativa del bilancio corrente consente l'inserimento di nuovi conti.
     * Le fasi PREVISIONE e CHIUSO non consentono modifiche.
     * L'anno di bilancio viene letto automaticamente da BilancioContextHolder.
     *
     * @param enteCodiceUnivoco codice fiscale dell'ente
     * @return true se la fase operativa è valida (GESTIONE o PREDISPOSIZIONE_CONSUNTIVO)
     * @throws IllegalStateException se anno bilancio non è disponibile nel JWT
     */
    private boolean isFaseOperativaValida(String enteCodice) {
        try {
            if (enteCodice == null || enteCodice.isEmpty()) {
                log.warn("Ente codice nullo o vuoto, assumo fase operativa valida");
                return true;
            }

            // Leggi anno bilancio da BilancioContextHolder
            Integer annoBilancio = BilancioContextHolder.getCurrentAnnoBilancio();
            if (annoBilancio == null) {
                throw new IllegalStateException("Anno di bilancio non disponibile nel contesto JWT. Verificare che il token contenga il claim 'anno_bilancio'.");
            }

            // Recupero il bilancio corrente per l'ente
            LocalDateTime dataCorrente = LocalDateTime.now();
            var entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(
                enteCodice,
                String.valueOf(annoBilancio),
                dataCorrente
            );

            if (entitaContabileOpt.isPresent()) {
                TipoFase fase = entitaContabileOpt.get().getFase();
                // Le fasi PREVISIONE e CHIUSO non consentono l'inserimento di nuovi conti
                boolean isValid = fase != TipoFase.PREVISIONE && fase != TipoFase.CHIUSO;
                log.debug("Fase operativa per ente {} anno {}: {} - valida: {}", enteCodice, annoBilancio, fase, isValid);
                return isValid;
            } else {
                log.warn("Nessun bilancio trovato per ente {} e anno {}, assumo fase operativa valida",
                         enteCodice, annoBilancio);
                return true; // Fallback: se non trovo bilancio, permetto l'operazione
            }
        } catch (IllegalStateException e) {
            // Rilancia errori di contesto (anno bilancio mancante)
            throw e;
        } catch (Exception e) {
            log.error("Errore nel recupero fase operativa per ente: {}", enteCodice, e);
            return true; // Fallback: in caso di errore, permetto l'operazione
        }
    }

    @Override
    @Deprecated
    public List<PdcClasse> getPdcClassi() {
        List<AccrualDPdcClasse> entities = pdcClasseRepository.findAll();
        return entities.stream()
                .map(pdcClasseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdcClasse> getPdcClassiValid() {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        Integer annoBilancio = BilancioContextHolder.getCurrentAnnoBilancio();
        if (annoBilancio == null) {
            throw new IllegalStateException("Anno di bilancio non disponibile. Verificare che il token JWT contenga il claim 'anno_bilancio'.");
        }

        // Il parametro enteCodice è già valorizzato dal SecurityContext
        if (enteCodice == null || enteCodice.isBlank()) {
            throw new IllegalStateException("Codice fiscale ente non disponibile. Verificare che il token JWT contenga il claim 'codice_ente'.");
        }

        log.info("Richiesta getPdcClassiValid con parametri - annoBilancio: {}, enteCodiceUnivoco: {}",
                 annoBilancio, enteCodice);

        LocalDateTime startOfYear = LocalDateTime.of(annoBilancio, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(annoBilancio, 12, 31, 23, 59, 59);

        List<AccrualDPdcClasse> entities = pdcClasseRepository.findClassiConContiValidi(startOfYear, endOfYear, enteCodice);

        log.info("Trovate {} classi con conti validi per anno {} ed ente {}", entities.size(), annoBilancio, enteCodice);

        return entities.stream()
                .map(pdcClasseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdcTipo> getPdcTipi() {
        List<AccrualDPdcTipo> entities = pdcTipoRepository.findAll();
        return entities.stream()
                .map(pdcTipoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdcStato> getPdcStati() {
        List<AccrualDPdcStato> entities = pdcStatoRepository.findAll();
        return entities.stream()
                .map(pdcStatoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdcFamiglia> getPdcFamiglie() {
        List<AccrualTPdcFamiglia> entities = pdcFamigliaRepository.findAll();
        return entities.stream()
                .map(pdcFamigliaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdcContoRelazione> getPdcRelazioni(String codiceUfficiale, String codiceClasse, String codiceFamiglia, String tipoRelazione) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        log.info("Richiesta getPdcRelazioni per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}, tipoRelazione: {}, enteCodice: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia, tipoRelazione, enteCodice);

        // Trova il PDC principale utilizzando la chiave logica tripla
        Optional<AccrualTEnte> enteOpt = enteRepository.findByCodiceUnivoco(enteCodice);
        if (!enteOpt.isPresent()) {
            throw new NotFoundException("Ente non trovato con codice fiscale: " + enteCodice);
        }
        AccrualTEnte ente = enteOpt.get();

        // Otteniamo la classe dal codice
        Optional<AccrualDPdcClasse> classeOpt = pdcClasseRepository.findByCodice(codiceClasse);
        if (!classeOpt.isPresent()) {
            throw new NotFoundException("Classe non trovata con codice: " + codiceClasse);
        }
        AccrualDPdcClasse classe = classeOpt.get();

        // Otteniamo la famiglia dal codice
        Optional<AccrualTPdcFamiglia> famigliaOpt = pdcFamigliaRepository.findByCodice(codiceFamiglia);
        if (!famigliaOpt.isPresent()) {
            throw new NotFoundException("Famiglia non trovata con codice: " + codiceFamiglia);
        }
        AccrualTPdcFamiglia famiglia = famigliaOpt.get();

        // Cerchiamo il record usando la chiave completa (escludendo i record cancellati)
        Optional<AccrualTPdc> pdcPrincipaleOpt = pdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(codiceUfficiale, classe.getId(), famiglia.getId(), ente.getId());

        if (!pdcPrincipaleOpt.isPresent()) {
            throw new NotFoundException("PDC non trovato con codice ufficiale: " + codiceUfficiale +
                                        ", classe: " + codiceClasse +
                                        ", famiglia: " + codiceFamiglia +
                                        " per l'ente: " + enteCodice);
        }

        AccrualTPdc pdcPrincipale = pdcPrincipaleOpt.get();

        // Costruisci la specificazione per trovare le relazioni
        final Long pdcPrincipaleId = pdcPrincipale.getId();
        final Long enteId = ente.getId();

        Specification<AccrualRPdcLegame> specBase = Specification.where(null);

        // Filtro per ente
        specBase = specBase.and((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("ente").get("id"), enteId));

        // Filtro per PDC principale (può essere in A o in B)
        specBase = specBase.and((root, query, criteriaBuilder) ->
            criteriaBuilder.or(
                criteriaBuilder.equal(root.get("pdcLegameA").get("id"), pdcPrincipaleId),
                criteriaBuilder.equal(root.get("pdcLegameB").get("id"), pdcPrincipaleId)
            ));

        // Carica tutti i legami per ente+pdc (senza filtro tipo) per diagnostica e per filtrare in memoria
        List<AccrualRPdcLegame> tuttiLegami = pdcLegameRepository.findAll(specBase);
        if (tuttiLegami.isEmpty()) {
            log.warn("getPdcRelazioni: nessun legame trovato per pdcId={}, enteId={}", pdcPrincipaleId, enteId);
        } else {
            log.info("getPdcRelazioni: trovati {} legami totali per pdcId={}, enteId={}. Tipi presenti: [{}]",
                tuttiLegami.size(), pdcPrincipaleId, enteId,
                tuttiLegami.stream()
                    .map(l -> l.getPdcTipoRelazione() != null ? l.getPdcTipoRelazione().getCodice() : "NULL")
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(", ")));
        }

        // Applica filtro per tipo di relazione in memoria
        List<AccrualRPdcLegame> legami = tuttiLegami;
        if (tipoRelazione != null && !tipoRelazione.trim().isEmpty()) {
            final String tipoFiltro = tipoRelazione.trim();
            legami = tuttiLegami.stream()
                .filter(l -> l.getPdcTipoRelazione() != null && tipoFiltro.equals(l.getPdcTipoRelazione().getCodice()))
                .collect(java.util.stream.Collectors.toList());
        }
        log.info("getPdcRelazioni: {} legami dopo filtro tipo='{}' su pdcId={}", legami.size(), tipoRelazione, pdcPrincipaleId);

        // Mappa i risultati alle relazioni appropriate
        return legami.stream()
            .map(legame -> {
                // Determina quale PDC è il conto collegato (l'altro rispetto a quello richiesto)
                AccrualTPdc pdcCollegato;
                if (legame.getPdcLegameA().getId().equals(pdcPrincipaleId)) {
                    pdcCollegato = legame.getPdcLegameB();
                } else {
                    pdcCollegato = legame.getPdcLegameA();
                }

                String tipoCodice = legame.getPdcTipoRelazione() != null ? legame.getPdcTipoRelazione().getCodice() : tipoRelazione;
                return createPdcContoRelazioneFromPdc(pdcCollegato, tipoCodice);
            })
            .collect(Collectors.toList());
    }

    private PdcContoRelazione createPdcContoRelazioneFromPdc(AccrualTPdc pdc, String tipoRelazione) {
        // Questo metodo crea un oggetto PdcContoRelazione dal PDC e tipo di relazione
        PdcContoRelazione relazione = new PdcContoRelazione();
        relazione.setTipoRelazione(tipoRelazione);
        relazione.setClasse(pdc.getPdcClasse() != null ? pdc.getPdcClasse().getDescrizione() : "");
        relazione.setTipo(pdc.getPdcTipo() != null ? pdc.getPdcTipo().getDescrizione() : "");
        relazione.setLivello(pdc.getLivello());
        // CORREZIONE BUG #1: Il segmento deve essere il codice della famiglia, non il codice del conto
        relazione.setSegmento(pdc.getPdcFamiglia() != null ? pdc.getPdcFamiglia().getCodice() : "");
        String codice = pdc.getCodiceUfficiale() != null ? pdc.getCodiceUfficiale() : pdc.getCodice();
        relazione.setCodice(codice);
        relazione.setDescrizione(pdc.getDescrizione());
        log.info("createPdcContoRelazioneFromPdc: pdcId={}, codice={}, descrizione={}, tipo={}", pdc.getId(), codice, pdc.getDescrizione(), tipoRelazione);

        return relazione;
    }

    /**
     * Ottiene tutti gli ID dei conti e sottoconti (ricorsivamente) a partire da un conto padre
     */
    private List<Long> getIdContiESottoConti(Long idContoPadre) {
        List<Long> result = new ArrayList<>();
        result.add(idContoPadre);

        // Ottieni tutti i figli diretti
        List<AccrualTPdc> figli = pdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(idContoPadre);

        // Per ogni figlio, aggiungi il suo ID e tutti i suoi sottoconti ricorsivamente
        for (AccrualTPdc figlio : figli) {
            result.addAll(getIdContiESottoConti(figlio.getId()));
        }

        return result;
    }

    @Override
    public it.csi.accrual.accrualsbsrv.dto.PdcTreeResult searchPdcTree(it.csi.accrual.accrualsbsrv.dto.PdcTreeFilter filter) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        log.info("Inizio elaborazione searchPdcTree nel servizio - filter: {}, enteCodice: {}",
                 filter != null ? "presente" : "null", enteCodice);

        if (filter != null) {
            log.debug("Valori del PdcTreeFilter ricevuto: codice={}, descrizione={}, famiglia={}, codiceEvento={}, pdcClasseCodice={}, pdcStatoCodice={}, livello={}, treeFormat={}",
                filter.getCodice(), filter.getDescrizione(), filter.getFamiglia(), filter.getCodiceEvento(), 
                filter.getPdcClasseCodice(), filter.getPdcStatoCodice(), filter.getLivello(), filter.isTreeFormat());
        } else {
            log.warn("PdcTreeFilter ricevuto è null");
        }

        // Costruiamo la specifica di ricerca utilizzando i filtri forniti
        Specification<AccrualTPdc> spec = buildSearchTreeSpecification(filter, enteCodice);

        // Recuperiamo tutti i risultati (senza paginazione per la struttura ad albero)
        List<AccrualTPdc> pdcEntities = pdcRepository.findAll(spec);

        // Costruiamo la struttura ad albero dai risultati
        List<it.csi.accrual.accrualsbsrv.dto.PdcTreeNode> treeNodes = buildPdcTree(pdcEntities);

        // Creiamo e restituiamo il risultato
        it.csi.accrual.accrualsbsrv.dto.PdcTreeResult result = new it.csi.accrual.accrualsbsrv.dto.PdcTreeResult();
        result.setNodes(treeNodes);

        log.info("Fine elaborazione searchPdcTree - risultato: {} nodi radice", result.getNodes() != null ? result.getNodes().size() : 0);
        return result;
    }

    /**
     * Costruisce la specifica JPA per la ricerca dei PDC in formato gerarchico
     */
    private Specification<AccrualTPdc> buildSearchTreeSpecification(it.csi.accrual.accrualsbsrv.dto.PdcTreeFilter filter, String enteCodice) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filtro per ente
            Optional<AccrualTEnte> enteOpt = enteRepository.findByCodiceUnivoco(enteCodice);
            if (!enteOpt.isPresent()) {
                throw new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice);
            }
            predicates.add(criteriaBuilder.equal(root.get("ente").get("id"), enteOpt.get().getId()));

            // Filtro per codice (se presente)
            if (filter != null && filter.getCodice() != null && !filter.getCodice().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("codiceUfficiale")),
                    "%" + filter.getCodice().toLowerCase() + "%"
                ));
            }

            // Filtro per descrizione (se presente)
            if (filter != null && filter.getDescrizione() != null && !filter.getDescrizione().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("descrizione")),
                    "%" + filter.getDescrizione().toLowerCase() + "%"
                ));
            }

            // Altri filtri dal PdcFilter ereditato
            if (filter != null) {
                if (StringUtils.hasText(filter.getFamiglia())) {
                    predicates.add(criteriaBuilder.equal(root.get("pdcFamiglia").get("codice"), filter.getFamiglia()));
                }
                if (StringUtils.hasText(filter.getCodiceEvento())) {
                    predicates.add(criteriaBuilder.equal(root.get("evento").get("codice"), filter.getCodiceEvento()));
                }
                if (StringUtils.hasText(filter.getPdcClasseCodice())) {
                    predicates.add(criteriaBuilder.equal(root.get("pdcClasse").get("codice"), filter.getPdcClasseCodice()));
                }
                if (StringUtils.hasText(filter.getCodiceUfficiale())) {
                    predicates.add(criteriaBuilder.equal(root.get("codiceUfficiale"), filter.getCodiceUfficiale()));
                }
                if (StringUtils.hasText(filter.getPdcStatoCodice())) {
                    predicates.add(criteriaBuilder.equal(root.get("pdcStato").get("codice"), filter.getPdcStatoCodice()));
                }
                if (filter.getLivello() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("livello"), filter.getLivello()));
                }
                if (filter.isModificabile() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("modificabile"), filter.isModificabile()));
                }
                if (StringUtils.hasText(filter.getPdcTipoCodice())) {
                    predicates.add(criteriaBuilder.equal(root.get("pdcTipo").get("codice"), filter.getPdcTipoCodice()));
                }
            }

            // Filtro per validità temporale - COMMENTATO perché non corretto rispetto ai requisiti
            // Secondo il documento @docs/sviluppo-funzionalita/primanota/CDU_Ricerca_prima_nota-primanota_006_V0.md paragrafo 2.6.10,
            // il controllo dovrebbe essere fatto rispetto all'anno dell'entità contabile (EntitaContabile.anno) 
            // e non rispetto all'anno corrente (currentYear)
            /*
            int currentYear = java.time.Year.now().getValue();
            java.time.LocalDateTime startOfYear = java.time.LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
            java.time.LocalDateTime endOfYear = java.time.LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);
            
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inizioValidita"), endOfYear));
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("fineValidita")),
                criteriaBuilder.greaterThanOrEqualTo(root.get("fineValidita"), startOfYear)
            ));
            */

            // Filtro per essere ricercabile - deve accedere tramite la famiglia
            predicates.add(criteriaBuilder.isTrue(root.get("pdcFamiglia").get("ricercabile")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Costruisce la struttura ad albero dei PDC dai risultati piatti
     * L'alberatura viene ordinata per codiceUfficiale usando un ordinamento naturale dei segmenti numerici
     */
    private List<it.csi.accrual.accrualsbsrv.dto.PdcTreeNode> buildPdcTree(List<AccrualTPdc> flatList) {
        // Creiamo una mappa per accedere rapidamente ai nodi per ID
        Map<Long, it.csi.accrual.accrualsbsrv.dto.PdcTreeNode> nodeMap = new HashMap<>();
        List<it.csi.accrual.accrualsbsrv.dto.PdcTreeNode> rootNodes = new ArrayList<>();

        // Creiamo i nodi per ogni PDC
        for (AccrualTPdc pdc : flatList) {
            it.csi.accrual.accrualsbsrv.dto.PdcTreeNode node = convertToTreeNode(pdc);
            nodeMap.put(pdc.getId(), node);
        }

        // Costruiamo la struttura gerarchica
        for (AccrualTPdc pdc : flatList) {
            it.csi.accrual.accrualsbsrv.dto.PdcTreeNode currentNode = nodeMap.get(pdc.getId());

            // Se il PDC ha un padre e questo è nella lista dei risultati
            if (pdc.getPdcPadre() != null && nodeMap.containsKey(pdc.getPdcPadre().getId())) {
                it.csi.accrual.accrualsbsrv.dto.PdcTreeNode parentNode = nodeMap.get(pdc.getPdcPadre().getId());

                // Assicuriamoci che la lista dei figli esista
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }

                // Aggiungiamo il nodo corrente come figlio del nodo padre
                parentNode.getChildren().add(currentNode);
            } else {
                // Se non ha padre o il padre non è nei risultati, è un nodo radice
                rootNodes.add(currentNode);
            }
        }

        // Ordiniamo i nodi radice per codiceUfficiale
        rootNodes.sort(this::compareCodiceUfficiale);

        // Ordiniamo ricorsivamente anche tutti i figli per codiceUfficiale
        sortTreeByCodiceUfficiale(rootNodes);

        return rootNodes;
    }

    /**
     * Ordina ricorsivamente l'albero dei PDC per codiceUfficiale
     */
    private void sortTreeByCodiceUfficiale(List<it.csi.accrual.accrualsbsrv.dto.PdcTreeNode> nodes) {
        for (it.csi.accrual.accrualsbsrv.dto.PdcTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                // Ordiniamo i figli per codiceUfficiale
                node.getChildren().sort(this::compareCodiceUfficiale);
                // Richiamiamo ricorsivamente per ordinare i sottofigli
                sortTreeByCodiceUfficiale(node.getChildren());
            }
        }
    }

    /**
     * Confronta due codici ufficiali gestendo correttamente l'ordinamento naturale dei segmenti numerici
     * Esempio: 1.1.0 < 11.1.0 (non come ordinamento lessicografico puro dove "11.1.0" < "1.1.0")
     */
    private int compareCodiceUfficiale(it.csi.accrual.accrualsbsrv.dto.PdcTreeNode node1, 
                                        it.csi.accrual.accrualsbsrv.dto.PdcTreeNode node2) {
        String codice1 = node1.getCodiceUfficiale() != null ? node1.getCodiceUfficiale() : "";
        String codice2 = node2.getCodiceUfficiale() != null ? node2.getCodiceUfficiale() : "";

        // Suddividiamo i codici in segmenti separati da punto
        String[] segments1 = codice1.split("\\.");
        String[] segments2 = codice2.split("\\.");

        // Confrontiamo segmento per segmento
        int maxLen = Math.max(segments1.length, segments2.length);
        for (int i = 0; i < maxLen; i++) {
            // Se un codice ha meno segmenti, viene prima (es. "1.1" < "1.1.0")
            if (i >= segments1.length) {
                return -1;
            }
            if (i >= segments2.length) {
                return 1;
            }

            String seg1 = segments1[i];
            String seg2 = segments2[i];

            // Tentiamo di convertire i segmenti in numeri per un confronto numerico corretto
            try {
                Integer num1 = Integer.parseInt(seg1);
                Integer num2 = Integer.parseInt(seg2);
                // Se entrambi sono numeri, confrontiamoli numericamente
                int comparison = num1.compareTo(num2);
                if (comparison != 0) {
                    return comparison;
                }
            } catch (NumberFormatException e) {
                // Se almeno uno non è un numero, usiamo il confronto lessicografico
                int comparison = seg1.compareTo(seg2);
                if (comparison != 0) {
                    return comparison;
                }
            }
        }

        // I codici sono uguali
        return 0;
    }

    /**
     * Crea un oggetto Sort a partire dal parametro sort ricevuto dalla richiesta HTTP.
     * Per codice_ufficiale, ritorna Sort.unsorted() perché l'ordinamento naturale sarà applicato in memoria.
     *
     * Supporta due formati:
     * 1. Formato con prefisso: '+campo' o '-campo' (es. '+codice_ufficiale')
     * 2. Formato con direzione: 'campo,asc' o 'campo,desc' (es. 'codice,asc')
     *
     * @param sort la stringa di ordinamento
     * @return l'oggetto Sort da applicare alla query
     */
    private Sort parseSortParameter(String sort) {
        if (sort == null || sort.isEmpty()) {
            // Ordinamento di default: nessun ordinamento specifico, sarà applicato l'ordinamento naturale in memoria
            return Sort.unsorted();
        }

        Sort sortResult = Sort.unsorted();
        
        // Gestione formato con direzione: 'campo,asc' o 'campo,desc'
        if (sort.contains(",") && !sort.startsWith("+") && !sort.startsWith("-")) {
            String[] parts = sort.split(",", 2);
            if (parts.length == 2) {
                String field = parts[0].trim();
                String directionStr = parts[1].trim().toLowerCase();
                
                Sort.Direction direction = "desc".equals(directionStr) ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                
                // Per codice_ufficiale, usiamo Sort.unsorted() perché l'ordinamento naturale
                // sarà applicato in memoria dopo la query
                if ("codice_ufficiale".equals(field) || "codiceUfficiale".equals(field)) {
                    log.debug("Rilevato ordinamento per codice_ufficiale - sarà applicato in memoria");
                    return Sort.unsorted();
                }
                
                if (direction == Sort.Direction.ASC) {
                    sortResult = sortResult.and(Sort.by(field).ascending());
                } else {
                    sortResult = sortResult.and(Sort.by(field).descending());
                }
                
                log.debug("Sort generato (formato campo,direzione): {}", sortResult);
                return sortResult;
            }
        }
        
        // Gestione formato con prefisso: '+campo' o '-campo'
        String[] sortParams = sort.split(",");

        for (String param : sortParams) {
            String field;
            Sort.Direction direction = Sort.Direction.ASC;

            if (param.startsWith("+")) {
                field = param.substring(1);
                direction = Sort.Direction.ASC;
            } else if (param.startsWith("-")) {
                field = param.substring(1);
                direction = Sort.Direction.DESC;
            } else {
                // Se non specificato, default ad ASC
                field = param;
                direction = Sort.Direction.ASC;
            }

            // Per codice_ufficiale, usiamo Sort.unsorted() perché l'ordinamento naturale
            // sarà applicato in memoria dopo la query
            if ("codice_ufficiale".equals(field) || "codiceUfficiale".equals(field)) {
                log.debug("Rilevato ordinamento per codice_ufficiale - sarà applicato in memoria");
                // Non aggiungiamo Sort.by() perché l'ordinamento naturale sarà fatto in memoria
                continue;
            } else {
                // Per altri campi, usiamo l'ordinamento standard del database
                if (direction == Sort.Direction.ASC) {
                    sortResult = sortResult.and(Sort.by(field).ascending());
                } else {
                    sortResult = sortResult.and(Sort.by(field).descending());
                }
            }
        }

        log.debug("Sort generato (formato prefisso): {}", sortResult);
        return sortResult;
    }

    /**
     * Confronta due entity AccrualTPdc per codiceUfficiale gestendo correttamente l'ordinamento naturale dei segmenti numerici
     * Esempio: 1.1.0 < 11.1.0 (non come ordinamento lessicografico puro dove "11.1.0" < "1.1.0")
     */
    private int compareCodiceUfficialeEntity(AccrualTPdc pdc1, AccrualTPdc pdc2) {
        String codice1 = pdc1.getCodiceUfficiale() != null ? pdc1.getCodiceUfficiale() : "";
        String codice2 = pdc2.getCodiceUfficiale() != null ? pdc2.getCodiceUfficiale() : "";

        // Suddividiamo i codici in segmenti separati da punto
        String[] segments1 = codice1.split("\\.");
        String[] segments2 = codice2.split("\\.");

        // Confrontiamo segmento per segmento
        int maxLen = Math.max(segments1.length, segments2.length);
        for (int i = 0; i < maxLen; i++) {
            // Se un codice ha meno segmenti, viene prima (es. "1.1" < "1.1.0")
            if (i >= segments1.length) {
                return -1;
            }
            if (i >= segments2.length) {
                return 1;
            }

            String seg1 = segments1[i];
            String seg2 = segments2[i];

            // Tentiamo di convertire i segmenti in numeri per un confronto numerico corretto
            try {
                Integer num1 = Integer.parseInt(seg1);
                Integer num2 = Integer.parseInt(seg2);
                // Se entrambi sono numeri, confrontiamoli numericamente
                int comparison = num1.compareTo(num2);
                if (comparison != 0) {
                    return comparison;
                }
            } catch (NumberFormatException e) {
                // Se almeno uno non è un numero, usiamo il confronto lessicografico
                int comparison = seg1.compareTo(seg2);
                if (comparison != 0) {
                    return comparison;
                }
            }
        }

        // I codici sono uguali
        return 0;
    }

    /**
     * Converte un oggetto AccrualTPdc in un PdcTreeNode
     */
    private it.csi.accrual.accrualsbsrv.dto.PdcTreeNode convertToTreeNode(AccrualTPdc pdc) {
        // Convertiamo prima l'entità in un DTO base Pdc usando il mapper
        it.csi.accrual.accrualsbsrv.dto.Pdc basePdc = pdcMapper.toDto(pdc);

        // Creiamo un nuovo oggetto PdcTreeNode e copiamo tutte le proprietà da Pdc
        it.csi.accrual.accrualsbsrv.dto.PdcTreeNode node = new it.csi.accrual.accrualsbsrv.dto.PdcTreeNode();

        // Copiamo tutte le proprietà dall'oggetto basePdc al nuovo nodo
        node.setId(basePdc.getId());
        node.setCodice(basePdc.getCodice());
        node.setCodiceUfficiale(basePdc.getCodiceUfficiale());
        node.setCodiceCompleto(basePdc.getCodiceCompleto());
        node.setDescrizione(basePdc.getDescrizione());
        node.setPartita(basePdc.isPartita());
        node.setOrdine(basePdc.getOrdine());
        node.setLivello(basePdc.getLivello());
        node.setCollegamentoBilItas(basePdc.getCollegamentoBilItas());
        node.setFondoDotazione(basePdc.isFondoDotazione());
        node.setFoglia(basePdc.isFoglia());
        node.setFogliaNonFoglia(basePdc.isFogliaNonFoglia());
        node.setPdcFamiglia(basePdc.getPdcFamiglia());
        node.setPdcClasse(basePdc.getPdcClasse());
        node.setPdcTipo(basePdc.getPdcTipo());
        node.setPdcStato(basePdc.getPdcStato());
        node.setEnte(basePdc.getEnte());
        node.setInizioValidita(basePdc.getInizioValidita());
        node.setFineValidita(basePdc.getFineValidita());
        node.setModificabile(basePdc.isModificabile());
        node.setEliminabile(basePdc.isEliminabile());
        node.setInseribileFiglio(basePdc.isInseribileFiglio());
        node.setVersion(basePdc.getVersion());
        node.setInseribileFiglio(basePdc.isInseribileFiglio());
        node.setPdcPadre(basePdc.getPdcPadre());

        // Impostiamo la lista dei figli come vuota inizialmente
        node.setChildren(new ArrayList<>());

        return node;
    }

    @Override
    public List<Pdc> exportPdcList(PdcFilter filter, String sort) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        Integer annoBilancio = BilancioContextHolder.getCurrentAnnoBilancio();
        if (annoBilancio == null) {
            throw new IllegalStateException("Anno di bilancio non disponibile. Verificare che il token JWT contenga il claim 'anno_bilancio'.");
        }

        log.info("Preparazione export PDC - filter: {}, sort: {}, enteCodice: {}, annoBilancio: {}", filter, sort, enteCodice, annoBilancio);

        // Costruisci specifiche filtro
        Specification<AccrualTPdc> spec = Specification.where(PdcSpecifications.isRicercabile())
                .and(PdcSpecifications.isValidInCurrentYear());

        if (enteCodice != null && !enteCodice.isEmpty()) {
            spec = spec.and(PdcSpecifications.byEnteCodiceUnivoco(enteCodice));
        }

        if (filter != null) {
            // Applica filtri dalla richiesta
            if (StringUtils.hasText(filter.getFamiglia())) {
                spec = spec.and(PdcSpecifications.byFamiglia(filter.getFamiglia()));
            }
            if (StringUtils.hasText(filter.getCodiceEvento())) {
                spec = spec.and(PdcSpecifications.byCodiceEvento(filter.getCodiceEvento()));
            }
            if (StringUtils.hasText(filter.getPdcClasseCodice())) {
                spec = spec.and(PdcSpecifications.byPdcClasseCodice(filter.getPdcClasseCodice()));
            }
            if (StringUtils.hasText(filter.getCodiceUfficiale())) {
                spec = spec.and(PdcSpecifications.byCodiceUfficiale(filter.getCodiceUfficiale()));
            }
            if (StringUtils.hasText(filter.getPdcStatoCodice())) {
                spec = spec.and(PdcSpecifications.byPdcStatoCodice(filter.getPdcStatoCodice()));
            }
            if (StringUtils.hasText(filter.getDescrizione())) {
                spec = spec.and(PdcSpecifications.byDescrizione(filter.getDescrizione()));
            }
            if (filter.getLivello() != null) {
                spec = spec.and(PdcSpecifications.byLivello(filter.getLivello()));
            }
            if (filter.isModificabile() != null) {
                spec = spec.and(PdcSpecifications.byModificabile(filter.isModificabile()));
            }
            if (StringUtils.hasText(filter.getPdcTipoCodice())) {
                spec = spec.and(PdcSpecifications.byPdcTipoCodice(filter.getPdcTipoCodice()));
            }
        }

        // Recupera TUTTI i risultati (senza paginazione) usando limit=-1
        // Usiamo un valore massimo ragionevole per evitare problemi di memoria
        Sort sortOrder = parseSortParameter(sort);
        log.debug("Ordinamento parsato per exportPdcList: {}", sortOrder);
        int maxSize = 10000;
        Pageable pageable = PageRequest.of(0, maxSize, sortOrder);
        
        log.debug("Esecuzione query export con pageable: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AccrualTPdc> pagedPdcEntities = pdcRepository.findAll(spec, pageable);

        // Applica ordinamento - creo una copia mutabile perché getContent() restituisce una lista immutabile
        List<AccrualTPdc> entities = new ArrayList<>(pagedPdcEntities.getContent());
        if (sort != null && (sort.contains("codice_ufficiale") || sort.contains("codiceUfficiale"))) {
            entities.sort(this::compareCodiceUfficialeEntity);
            log.debug("Applicato ordinamento naturale per codice_ufficiale su {} elementi", entities.size());
        }

        // Mappa e ritorna
        Set<String> userAzioni = permissionHelper.getUserAzioniCodici();
        List<Pdc> result = entities.stream()
                .map(entity -> {
                    Pdc dto = pdcMapper.toDto(entity);
                    dto.setModificabile(isContoModificabile(entity, userAzioni));
                    dto.setEliminabile(isContoEliminabile(entity, userAzioni));
                    dto.setInseribileFiglio(isContoPadre(entity, userAzioni));
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Export PDC completato - {} elementi recuperati", result.size());
        return result;
    }

    @Override
    public void validaContoDaInserire(String codiceUfficiale) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        if (enteCodice == null || enteCodice.isEmpty()) {
            throw new IllegalArgumentException("WEB_ERR_0002 - Ente non specificato");
        }

        AccrualTEnte ente = enteRepository.findByCodiceUnivoco(enteCodice)
            .orElseThrow(() -> new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice));

        LocalDateTime dataSistema = LocalDateTime.now();

        // ALG-003: Verifica Conto da inserire nella scrittura

        // 1. Verifica esistenza (cerca per codiceUfficiale e ente)
        AccrualTPdc conto = pdcRepository.findByCodiceUfficialeAndEnte_IdAndDataCancellazioneIsNull(codiceUfficiale, ente.getId())
            .orElse(null);
        if (conto == null) {
            throw new IllegalArgumentException("WEB_ERR_021 - Conto non esistente: " + codiceUfficiale);
        }

        // 2. Verifica validità temporale
        if (conto.getInizioValidita() != null && conto.getInizioValidita().isAfter(dataSistema)) {
            throw new IllegalArgumentException("WEB_ERR_022 - Conto non valido rispetto alla data");
        }
        if (conto.getFineValidita() != null && conto.getFineValidita().isBefore(dataSistema)) {
            throw new IllegalArgumentException("WEB_ERR_022 - Conto non valido rispetto alla data");
        }

        // 3. Verifica attributo foglia (o foglia_non_foglia)
        if (!conto.isFoglia() && (conto.getFogliaNonFoglia() == null || !conto.getFogliaNonFoglia())) {
            throw new IllegalArgumentException("WEB_ERR_028 - Il conto indicato non è foglia");
        }
    }

}
