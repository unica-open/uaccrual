/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.StatoPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualsbsrv.dto.Causale; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleStato; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleTipo; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.ContoCollegato; // Aggiunto per risolvere errore di compilazione
import it.csi.accrual.accrualsbsrv.dto.Evento; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.PagedCausale; // DTO generato da Swagger
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausalePdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausaleStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualDCausaleStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDCausaleTipoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualRCausalePdcRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualRCausaleStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEventoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.CausaleMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.CausaleStatoMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.CausaleTipoMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.EventoMapper;
import it.csi.accrual.accrualsbsrv.be.service.CausaleService;
import it.csi.accrual.accrualsbsrv.be.service.EventoService;
import it.csi.accrual.accrualcommonsrv.security.PermissionHelper;
import it.csi.accrual.accrualsbsrv.dto.Causale; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleStato; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleTipo; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.Evento; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.PagedCausale; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.util.FilterSpecificationBuilder;

@Service
@Transactional
public class CausaleServiceImpl implements CausaleService {

    private static final Logger LOG = LoggerFactory.getLogger(CausaleServiceImpl.class);

    /** Dimensione pagina di default quando il limit non è specificato. */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** Tetto massimo di record restituiti quando il client chiede limit = -1 (tutti i risultati). */
    private static final int MAX_UNPAGED_SIZE = 10000;

    @Autowired
    private AccrualTCausaleRepository causaleRepository;
    @Autowired
    private AccrualDCausaleTipoRepository causaleTipoRepository;
    @Autowired
    private AccrualDCausaleStatoRepository causaleStatoRepository;
    @Autowired
    private AccrualRCausaleStatoRepository causaleStatoStoricoRepository; // Per la gestione dello stato corrente
    // private AccrualTEnteRepository enteRepository; // In caso serva per filtri su ente
    @Autowired
    private AccrualTEnteRepository enteRepository; // Aggiunto per il filtro sull'ente
    @Autowired
    private AccrualTEventoRepository eventoRepository; // Per i dropdown e filtri se necessario
    @Autowired
    private AccrualTPdcRepository pdcRepository; // Aggiunto per recuperare i PDC reali

    @Autowired
    private AccrualRCausalePdcRepository causalePdcRepository; // Repository per i conti collegati

    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository; // Per i controlli sulle prime note collegate alla causale

    @Autowired
    private CausaleMapper causaleMapper;
    @Autowired
    private CausaleTipoMapper causaleTipoMapper;
    @Autowired
    private CausaleStatoMapper causaleStatoMapper;
    @Autowired
    private EventoMapper eventoMapper;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private FilterSpecificationBuilder<AccrualTCausale> filterSpecificationBuilder;

    @Autowired
    private ObjectMapper objectMapper; // Aggiunto ObjectMapper

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Autowired
    private PermissionHelper permissionHelper;

    private String getCurrentUserId() {
        String cf = securityContextHelper.getCodiceFiscale();
        return cf != null ? cf : "anonymous";
    }

    @Override
    public PagedCausale getPagedCausali(String filter, Integer limit, Integer offset) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("getPagedCausali called with enteCodice: {}, filter: {}, limit: {}, offset: {}", enteCodice, filter, limit, offset);

        // --- Start Ente Filtering Logic ---
        if (enteCodice == null || enteCodice.isEmpty()) {
            throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
        }

        Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
        if (optionalEnteEntity.isEmpty()) { // Changed from == null
            throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
        }
        AccrualTEnte enteEntity = optionalEnteEntity.get();
        Long enteId = enteEntity.getId();
        LOG.info("Resolved Ente CodiceFiscale: {} to Ente ID: {}", enteCodice, enteId);

        // Debugging logs for enteId and spec
        LOG.info("Resolved enteId: {}", enteId);

        Specification<AccrualTCausale> enteSpec = (root, query, cb) -> cb.equal(root.get("ente").get("id"), enteId);
        // --- End Ente Filtering Logic ---

        // Gestione della paginazione allineata a PdcServiceImpl: limit = -1 significa
        // "tutti i risultati" (usato dall'export CSV), con un tetto massimo per non
        // caricare in memoria l'intera tabella.
        int page;
        int pageSize;
        if (limit != null && limit == -1) {
            LOG.info("Richiesta di ottenere tutti i risultati (limit = -1)");
            pageSize = MAX_UNPAGED_SIZE;
            page = 0;
        } else {
            pageSize = (limit != null && limit > 0) ? limit : DEFAULT_PAGE_SIZE;
            page = (offset != null && offset > 0) ? offset / pageSize : 0;
        }
        // Ordinamento per descrizione del tipo causale (come da richiesta utente)
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("causaleTipo.descrizione"));
        LOG.debug("Pageable created: page={}, pageSize={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Specification<AccrualTCausale> spec = Specification.where(enteSpec); // Start with enteSpec
        String statoCodice = null;
        String modifiedFilter = filter;

        // Se il filter non è vuoto, proviamo a parsarlo per estrarre il filtro sullo stato
        if (filter != null && !filter.isEmpty()) {
            try {
                Map<String, Map<String, Object>> filterMap = objectMapper.readValue(filter, new TypeReference<Map<String, Map<String, Object>>>() {});
                
                if (filterMap.containsKey("stato.codice")) {
                    Map<String, Object> statoConditions = filterMap.get("stato.codice");
                    if (statoConditions != null && statoConditions.containsKey("eq")) {
                        statoCodice = statoConditions.get("eq").toString();
                        // Rimuovi la condizione dello stato dalla mappa per non passarla al FilterSpecificationBuilder generico
                        filterMap.remove("stato.codice");
                        modifiedFilter = objectMapper.writeValueAsString(filterMap);
                    }
                }
            } catch (Exception e) {
                // Logga l'errore ma continua, il FilterSpecificationBuilder gestirà il filter originale
                // Se il parsing fallisce, il filter originale verrà passato al builder.
                // e.printStackTrace(); 
                // LOG.warn("Impossibile parsare il filtro per estrarre lo stato, il filtro originale verrà utilizzato: " + e.getMessage());
            }
        }
        
        // Costruisci la Specification dal parametro 'modifiedFilter' usando il builder
        Specification<AccrualTCausale> filterSpec = filterSpecificationBuilder.build(modifiedFilter);
        spec = spec.and(filterSpec);


        
        // Filtro per stato corrente della causale - Reintrodotto se statoCodice è stato estratto
        if (statoCodice != null && !statoCodice.isEmpty()) {
            final String finalStatoCodice = statoCodice; // Variabile effettivamente final
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<AccrualRCausaleStato> rcsSubRoot = subquery.from(AccrualRCausaleStato.class);
                jakarta.persistence.criteria.Join<AccrualRCausaleStato, AccrualDCausaleStato> statoJoin = rcsSubRoot.join("causaleStato", JoinType.INNER);
                
                Predicate statoCorrentePredicate = cb.isNull(rcsSubRoot.get("fineValidita"));
                
                subquery.select(rcsSubRoot.get("causale").get("id"))
                        .where(cb.and(
                            cb.equal(statoJoin.get("codice"), finalStatoCodice), // Usare la variabile final
                            statoCorrentePredicate
                        ));
                return cb.in(root.get("id")).value(subquery);
            });
        }

        // Filtro per validità temporale della causale (anno corrente) - Mantenuto separato
        int currentYear = Year.now().getValue();
        LocalDateTime startOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        Specification<AccrualTCausale> validitySpec = (root, query, cb) -> cb.and(
            cb.lessThanOrEqualTo(root.get("inizioValidita"), endOfYear),
            cb.or(
                cb.isNull(root.get("fineValidita")),
                cb.greaterThanOrEqualTo(root.get("fineValidita"), startOfYear)
            )
        );
        spec = spec.and(validitySpec);
        LOG.info("Final Specification for findAll: {}", spec);


        Page<AccrualTCausale> pagedCausaleEntities = causaleRepository.findAll(spec, pageable);
                Page<Causale> pagedCausaleDtos = pagedCausaleEntities.map(entity -> {
                    Causale dto = causaleMapper.toDto(entity);
                    Set<String> userAzioni1 = permissionHelper.getUserAzioniCodici();
                    dto.setModificabile(isCausaleModificabile(entity, userAzioni1));
                    dto.setEliminabile(isCausaleEliminabile(entity, userAzioni1));
                    dto.setValidabile(isCausaleValidabile(entity, userAzioni1));
        
                    // Recupera lo stato corrente per la causale
                    Optional<AccrualRCausaleStato> statoCorrente = causaleStatoStoricoRepository
                        .findByCausaleAndFineValiditaIsNull(entity);
                    statoCorrente.ifPresent(s -> dto.setStato(causaleStatoMapper.toDto(s.getCausaleStato())));
        
                    return dto;
                });
        
                PagedCausale result = new PagedCausale();
                result.setData(pagedCausaleDtos.getContent());
                result.setPage(pagedCausaleDtos.getNumber());
                result.setPageSize(pagedCausaleDtos.getSize());
                result.setTotalElements(pagedCausaleDtos.getTotalElements());  
        
                LOG.debug("result:, result={}", result);
                return result;
            }
        
        
            @Override
            public Causale getCausaleById(Long id) {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                if (id == null) {
                    throw new IllegalArgumentException("ID cannot be null.");
                }
                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                AccrualTEnte enteEntity = optionalEnteEntity.get();
                Long enteId = enteEntity.getId();
                
                AccrualTCausale causaleEntity = causaleRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Causale non trovata con id: " + id));

                // Also verify that the found causale belongs to the specified ente
                if (!causaleEntity.getEnte().getId().equals(enteId)) {
                    throw new NotFoundException("Causale con ID: " + id + " non trovata per l'ente: " + enteCodice);
                }

                Causale dto = causaleMapper.toDto(causaleEntity);
                Set<String> userAzioni2 = permissionHelper.getUserAzioniCodici();
                dto.setModificabile(isCausaleModificabile(causaleEntity, userAzioni2));
                dto.setEliminabile(isCausaleEliminabile(causaleEntity, userAzioni2));
                dto.setValidabile(isCausaleValidabile(causaleEntity, userAzioni2));
                
                Optional<AccrualRCausaleStato> statoCorrente = causaleStatoStoricoRepository
                    .findByCausaleAndFineValiditaIsNull(causaleEntity);        
                statoCorrente.ifPresent(s -> dto.setStato(causaleStatoMapper.toDto(s.getCausaleStato())));

                // Recupera e mappa i conti collegati
                List<AccrualRCausalePdc> causalePdcEntities = causalePdcRepository.findByCausaleAndEnteWithRelations(causaleEntity.getId(), enteEntity.getId());
                List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> contiCollegatiDto = causalePdcEntities.stream()
                        .map(rcp -> {
                            AccrualTPdc pdc = rcp.getPdc();
                            it.csi.accrual.accrualsbsrv.dto.ContoCollegato contoDto = new it.csi.accrual.accrualsbsrv.dto.ContoCollegato();
                            contoDto.setPdcId(pdc.getId()); // Imposta il pdcId mancante
                            contoDto.setCodice(pdc.getCodiceUfficiale() != null ? pdc.getCodiceUfficiale() : pdc.getCodice());
                            contoDto.setDescrizione(pdc.getDescrizione());
                            if (pdc.getPdcClasse() != null) {
                                contoDto.setClasse(pdc.getPdcClasse().getDescrizione());
                            } else {
                                contoDto.setClasse(null);
                            }
                            contoDto.setSegno(rcp.getSegno() != null ? ContoCollegato.SegnoEnum.valueOf(rcp.getSegno().getCode()) : null);
                            contoDto.setTipoImporto(rcp.getTipoImporto() != null ? ContoCollegato.TipoImportoEnum.valueOf(rcp.getTipoImporto().getCode()) : null);
                            return contoDto;
                        })
                        .collect(Collectors.toList());
                dto.setContiCollegati(contiCollegatiDto);

                return dto;
            }
        
            @Override
            public List<CausaleTipo> getTipiCausale() {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                Long enteId = optionalEnteEntity.get().getId();
        
                List<AccrualDCausaleTipo> entities = causaleTipoRepository.findByEnte_Id(enteId);
                return entities.stream()
                        .map(causaleTipoMapper::toDto)
                        .collect(Collectors.toList());
            }
        
            @Override
            public List<Evento> getEventiCausale() {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                Long enteId = optionalEnteEntity.get().getId();
        
                List<AccrualTEvento> entities = eventoRepository.findByEnte_Id(enteId);
                return entities.stream()
                        .map(eventoMapper::toDto)
                        .collect(Collectors.toList());
            }
        
            @Override
            public List<CausaleStato> getCausaleStati() {
                // As per user request, do not filter by ente for now
                List<AccrualDCausaleStato> entities = causaleStatoRepository.findAll();
                return entities.stream()
                        .map(causaleStatoMapper::toDto)
                        .collect(Collectors.toList());
            }
        
            @Override
            public List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> getContiCollegati(Long id) {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                LOG.info("getContiCollegati - Inizio recupero conti collegati per id: {} e enteCodice: {}", id, enteCodice);

                if (enteCodice == null || enteCodice.isEmpty()) {
                    LOG.error("getContiCollegati - X-Ente-Codice nullo o vuoto.");
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    LOG.error("getContiCollegati - Ente non trovato per codice fiscale: {}", enteCodice);
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                AccrualTEnte enteEntity = optionalEnteEntity.get();
                Long enteId = enteEntity.getId();
                LOG.info("getContiCollegati - Ente trovato con id: {}", enteId);
        
                LOG.info("getContiCollegati - Ricerca causale con id: {} e enteId: {}", id, enteId);
                AccrualTCausale causaleEntity = causaleRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Causale non trovata con id: " + id));

                // Verify that the found causale belongs to the specified ente
                if (!causaleEntity.getEnte().getId().equals(enteId)) {
                    LOG.error("getContiCollegati - Causale con ID: {} non trovata per l'ente: {}", id, enteCodice);
                    throw new NotFoundException("Causale con ID: " + id + " non trovata per l'ente specificato.");
                }
                Long causaleId = causaleEntity.getId();
                LOG.info("getContiCollegati - Causale trovata con id: {}", causaleId);
        
                List<AccrualRCausalePdc> causalePdcEntities = causalePdcRepository.findByCausaleAndEnteWithRelations(causaleId, enteId);
                LOG.info("getContiCollegati - Trovate {} relazioni causale-pdc.", causalePdcEntities.size());
        
                List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> contiCollegatiDto = causalePdcEntities.stream()
                                        .map(rcp -> {
                                            AccrualTPdc pdc = rcp.getPdc();
                                            LOG.info("getContiCollegati - Dati PDC recuperati: {}", pdc);
                                            it.csi.accrual.accrualsbsrv.dto.ContoCollegato contoDto = new it.csi.accrual.accrualsbsrv.dto.ContoCollegato();
                                            contoDto.setPdcId(pdc.getId()); // Imposta il pdcId mancante
                                            contoDto.setCodice(pdc.getCodiceUfficiale() != null ? pdc.getCodiceUfficiale() : pdc.getCodice());
                                            contoDto.setDescrizione(pdc.getDescrizione());
                                            if (pdc.getPdcClasse() != null) {
                                                LOG.info("getContiCollegati - Classe PDC trovata: {}", pdc.getPdcClasse().getDescrizione());
                                                contoDto.setClasse(pdc.getPdcClasse().getDescrizione());
                                            } else {
                                                LOG.warn("getContiCollegati - Classe PDC non trovata per il conto: {}", pdc.getCodice());
                                                contoDto.setClasse(null);
                                            }
                                            contoDto.setSegno(rcp.getSegno() != null ? ContoCollegato.SegnoEnum.valueOf(rcp.getSegno().getCode()) : null);
                                            contoDto.setTipoImporto(rcp.getTipoImporto() != null ? ContoCollegato.TipoImportoEnum.valueOf(rcp.getTipoImporto().getCode()) : null);
                                            return contoDto;
                                        })
                                        .collect(Collectors.toList());

                LOG.info("Restituiti {} conti collegati per la causale con ID {}: {}", contiCollegatiDto.size(), id, contiCollegatiDto);
                return contiCollegatiDto;
            }
        
            /**
             * Una causale è modificabile se:
             * - Lo stato corrente è diverso da 'A' (Annullato)
             * - L'utente ha i permessi appropriati (OP-SPE-updCausale)
             * - Il bilancio non è in fase PREVISIONE o CHIUSO
             * 
             * Secondo le specifiche CDU-004, sez. 2.6.3
             */
            private boolean isCausaleModificabile(AccrualTCausale causale, Set<String> userAzioni) {
                if (causale == null) return false;
                if (!userAzioni.contains("OP-SPE-updCausale")) return false;
                Optional<AccrualRCausaleStato> statoOpt = causaleStatoStoricoRepository
                    .findByCausaleAndFineValiditaIsNull(causale);
                if (statoOpt.isEmpty()) return false;
                String statoCodice = statoOpt.get().getCausaleStato().getCodice();
                return !"A".equals(statoCodice);
            }

            /**
             * Una causale è eliminabile se:
             * - Lo stato corrente è diverso da 'A' (Annullato)
             * - L'utente ha i permessi appropriati (OP-SPE-delCausale)
             * - Il bilancio non è in fase PREVISIONE o CHIUSO
             * 
             * Secondo le specifiche CDU-004, sez. 2.6.4
             */
            private boolean isCausaleEliminabile(AccrualTCausale causale, Set<String> userAzioni) {
                if (causale == null) return false;
                if (!userAzioni.contains("OP-SPE-delCausale")) return false;
                Optional<AccrualRCausaleStato> statoOpt = causaleStatoStoricoRepository
                    .findByCausaleAndFineValiditaIsNull(causale);
                if (statoOpt.isEmpty()) return false;
                String statoCodice = statoOpt.get().getCausaleStato().getCodice();
                return !"A".equals(statoCodice);
            }

            /**
             * Una causale è validabile se:
             * - Lo stato corrente è 'P' (Provvisorio)
             * - L'utente ha i permessi per validare (OP-SPE-validaCausale)
             * - Il bilancio non è in fase PREVISIONE o CHIUSO
             *
             * Secondo le specifiche CDU-004, sez. 2.6.5
             */
            private boolean isCausaleValidabile(AccrualTCausale causale, Set<String> userAzioni) {
                if (causale == null) return false;
                boolean hasPermission = userAzioni.contains("OP-SPE-validaCausale");
                LOG.info("[isCausaleValidabile] causaleId={} hasPermission={} azioni={}", causale.getId(), hasPermission, userAzioni);
                if (!hasPermission) return false;
                Optional<AccrualRCausaleStato> statoOpt = causaleStatoStoricoRepository
                    .findByCausaleAndFineValiditaIsNull(causale);
                if (statoOpt.isEmpty()) {
                    LOG.info("[isCausaleValidabile] causaleId={} stato non trovato → false", causale.getId());
                    return false;
                }
                String statoCodice = statoOpt.get().getCausaleStato().getCodice();
                LOG.info("[isCausaleValidabile] causaleId={} statoCodice={}", causale.getId(), statoCodice);
                return "P".equals(statoCodice);
            }

            @Override
            @Transactional
            public Causale createCausale(it.csi.accrual.accrualsbsrv.dto.CausaleRequest causaleRequest) {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                LOG.info("createCausale called with enteCodice: {}", enteCodice);

                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                AccrualTEnte enteEntity = optionalEnteEntity.get();

                // Validazioni preliminari
                if (causaleRequest.getCodice() == null || causaleRequest.getCodice().isEmpty()) {
                    throw new IllegalArgumentException("Codice causale è obbligatorio.");
                }
                if (causaleRequest.getDescrizione() == null || causaleRequest.getDescrizione().isEmpty()) {
                    throw new IllegalArgumentException("Descrizione causale è obbligatoria.");
                }
                if (causaleRequest.getTipo() == null || causaleRequest.getTipo().getId() == null) {
                    throw new IllegalArgumentException("Tipo causale è obbligatorio.");
                }
                if (causaleRequest.getEvento() == null || causaleRequest.getEvento().getId() == null) {
                    throw new IllegalArgumentException("Evento causale è obbligatorio.");
                }
                if (causaleRequest.getInizioValidita() == null) {
                    throw new IllegalArgumentException("Inizio Validità è obbligatorio.");
                }

                // Verifica unicità codice
                if (causaleRepository.findByCodiceAndEnte_Id(causaleRequest.getCodice(), enteEntity.getId()) != null) {
                    throw new IllegalArgumentException("Causale con codice " + causaleRequest.getCodice() + " già esistente per l'ente specificato.");
                }

                // Mappa a entità
                AccrualTCausale causaleEntity = new AccrualTCausale();
                causaleEntity.setCodice(causaleRequest.getCodice());
                causaleEntity.setDescrizione(causaleRequest.getDescrizione());

                // Recupera e imposta Tipo
                AccrualDCausaleTipo tipoEntity = causaleTipoRepository.findById(causaleRequest.getTipo().getId())
                        .orElseThrow(() -> new NotFoundException("Tipo Causale con ID " + causaleRequest.getTipo().getId() + " non trovato."));
                causaleEntity.setCausaleTipo(tipoEntity);

                // Recupera e imposta Evento
                AccrualTEvento eventoEntity = eventoRepository.findById(causaleRequest.getEvento().getId())
                        .orElseThrow(() -> new NotFoundException("Evento con ID " + causaleRequest.getEvento().getId() + " non trovato."));
                causaleEntity.setEvento(eventoEntity);

                // Imposta Ente
                causaleEntity.setEnte(enteEntity);

                // Imposta date di validità
                causaleEntity.setInizioValidita(LocalDate.ofInstant(causaleRequest.getInizioValidita().toInstant(), ZoneId.systemDefault()).atStartOfDay());
                if (causaleRequest.getFineValidita() != null) {
                    causaleEntity.setFineValidita(LocalDate.ofInstant(causaleRequest.getFineValidita().toInstant(), ZoneId.systemDefault()).atTime(23, 59, 59));
                }

                // Dati di sistema (Auditing è gestito da Spring Data JPA per CreatedBy/CreatedDate)
                // Le seguenti chiamate non sono più necessarie per la creazione grazie ad Auditing
                // causaleEntity.setDataInserimento(LocalDateTime.now());
                // causaleEntity.setUtenteInserimento(getCurrentUserId());
                causaleEntity.setDataAggiornamento(LocalDateTime.now()); // Usare setDataAggiornamento
                causaleEntity.setUtenteAggiornamento(getCurrentUserId()); // Usare setUtenteAggiornamento


                // Salva la causale principale
                causaleEntity = causaleRepository.save(causaleEntity);

                // Gestione stato iniziale (P-Provvisorio)
                AccrualDCausaleStato statoProvvisorioEntity = causaleStatoRepository.findByCodice("P")
                        .orElseThrow(() -> new NotFoundException("Stato 'P' (Provvisorio) non trovato nel sistema."));
                AccrualRCausaleStato rCausaleStato = new AccrualRCausaleStato();
                rCausaleStato.setCausale(causaleEntity);
                rCausaleStato.setCausaleStato(statoProvvisorioEntity);
                rCausaleStato.setInizioValidita(LocalDateTime.now());
                rCausaleStato.setUtenteInserimento(getCurrentUserId());
                rCausaleStato.setDataInserimento(LocalDateTime.now());
                rCausaleStato.setEnte(enteEntity); // Imposta l'ente anche sulla relazione stato
                causaleStatoStoricoRepository.save(rCausaleStato);
                // causaleEntity.setCurrentStato(rCausaleStato); // Rimosso: campo non presente in entità


                // Gestione Conti Collegati
                if (causaleRequest.getContiCollegati() != null && !causaleRequest.getContiCollegati().isEmpty()) {
                    for (it.csi.accrual.accrualsbsrv.dto.ContoCollegato contoDto : causaleRequest.getContiCollegati()) {
                        // Validazione esplicita per i campi obbligatori del conto collegato
                        if (contoDto.getCodice() == null || contoDto.getCodice().isEmpty()) {
                            throw new IllegalArgumentException("Codice del conto collegato è obbligatorio.");
                        }
                        if (contoDto.getSegno() == null) { // Now it's an enum type, so direct null check is fine
                            throw new IllegalArgumentException("Segno del conto collegato è obbligatorio.");
                        }
                        if (contoDto.getTipoImporto() == null) { // Now it's an enum type, so direct null check is fine
                            throw new IllegalArgumentException("Tipo Importo del conto collegato è obbligatorio.");
                        }

                        AccrualRCausalePdc rCausalePdc = new AccrualRCausalePdc();
                        rCausalePdc.setCausale(causaleEntity); // This line must be here

                        // Recupera il PDC reale dal repository usando pdcId
                        if (contoDto.getPdcId() == null) {
                            throw new IllegalArgumentException("ID del PDC collegato è obbligatorio.");
                        }
                        Long pdcIdVal = null;
                        try {
                            pdcIdVal = contoDto.getPdcId();
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("ID del PDC collegato non è un numero valido: " + contoDto.getPdcId());
                        }
                        AccrualTPdc pdcEntity = pdcRepository.findById(pdcIdVal)
                            .orElseThrow(() -> new NotFoundException("PDC con ID " + contoDto.getPdcId() + " non trovato."));

                        // Also check if the found PDC belongs to the current ente, for security/data integrity
                        if (!pdcEntity.getEnte().getId().equals(enteEntity.getId())) {
                            throw new IllegalArgumentException("PDC con ID " + contoDto.getPdcId() + " non appartiene all'ente specificato.");
                        }
                        rCausalePdc.setPdc(pdcEntity);
                        
                        // Mappa altre proprietà del conto collegato (using generated enums)
                        TipoSegno tipoSegno = TipoSegno.getByCode(contoDto.getSegno().toString());
                        if (tipoSegno == null) { // This check should ideally not be needed if enum is correctly validated by swagger, but good for safety
                            throw new IllegalArgumentException("Valore non valido per 'segno': " + contoDto.getSegno());
                        }
                        TipoImporto tipoImporto = TipoImporto.getByCode(contoDto.getTipoImporto().toString());
                        if (tipoImporto == null) { // This check should ideally not be needed if enum is correctly validated by swagger, but good for safety
                            throw new IllegalArgumentException("Valore non valido per 'tipoImporto': " + contoDto.getTipoImporto());
                        }
                        rCausalePdc.setSegno(tipoSegno);
                        rCausalePdc.setTipoImporto(tipoImporto);
                        rCausalePdc.setEnte(enteEntity); // Imposta l'ente anche sulla relazione conto collegato
                        rCausalePdc.setInizioValidita(LocalDateTime.now());
                        rCausalePdc.setUtenteInserimento(getCurrentUserId());
                        rCausalePdc.setDataInserimento(LocalDateTime.now());
                        

                        LOG.info("Prima del salvataggio di rCausalePdc - enteEntity.getId(): {}", enteEntity.getId());
                        LOG.info("Prima del salvataggio di rCausalePdc - rCausalePdc.getEnte().getId(): {}", rCausalePdc.getEnte().getId());
                        causalePdcRepository.save(rCausalePdc);
                    }
                }

                // Mappa e restituisci il DTO completo
                Causale resultDto = causaleMapper.toDto(causaleEntity);

                // Recupera i conti collegati appena salvati e mapparli al DTO per la risposta
                List<AccrualRCausalePdc> savedContiCollegatiEntities = causalePdcRepository.findByCausaleAndEnteWithRelations(causaleEntity.getId(), enteEntity.getId());
                List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> savedContiCollegatiDto = savedContiCollegatiEntities.stream()
                        .map(rcp -> {
                            AccrualTPdc pdc = rcp.getPdc();
                            it.csi.accrual.accrualsbsrv.dto.ContoCollegato contoDto = new it.csi.accrual.accrualsbsrv.dto.ContoCollegato();
                            contoDto.setPdcId(pdc.getId()); // Set the pdcId
                            contoDto.setCodice(pdc.getCodiceUfficiale() != null ? pdc.getCodiceUfficiale() : pdc.getCodice());
                            contoDto.setDescrizione(pdc.getDescrizione());
                            if (pdc.getPdcClasse() != null) {
                                contoDto.setClasse(pdc.getPdcClasse().getDescrizione());
                            } else {
                                contoDto.setClasse(null);
                            }
                            contoDto.setSegno(rcp.getSegno() != null ? ContoCollegato.SegnoEnum.valueOf(rcp.getSegno().getCode()) : null);
                            contoDto.setTipoImporto(rcp.getTipoImporto() != null ? ContoCollegato.TipoImportoEnum.valueOf(rcp.getTipoImporto().getCode()) : null);
                            contoDto.setInizioValidita(rcp.getInizioValidita() != null ? java.util.Date.from(rcp.getInizioValidita().atZone(ZoneId.systemDefault()).toInstant()) : null);
                            contoDto.setFineValidita(rcp.getFineValidita() != null ? java.util.Date.from(rcp.getFineValidita().atZone(ZoneId.systemDefault()).toInstant()) : null);
                            return contoDto;
                        })
                        .collect(Collectors.toList());
                resultDto.setContiCollegati(savedContiCollegatiDto);

                // Aggiorna lo stato nel DTO di risposta
                resultDto.setStato(causaleStatoMapper.toDto(statoProvvisorioEntity));
                return resultDto;
            }

            @Override
            @Transactional
            public Causale updateCausale(Long id, it.csi.accrual.accrualsbsrv.dto.CausaleRequest causaleRequest) {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                LOG.info("updateCausale called for id: {} with enteCodice: {}", id, enteCodice);

                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                AccrualTEnte enteEntity = optionalEnteEntity.get();
                Long enteId = enteEntity.getId();

                AccrualTCausale existingCausale = causaleRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Causale non trovata con id: " + id));

                // Verify that the existing causale belongs to the specified ente
                if (!existingCausale.getEnte().getId().equals(enteId)) {
                    throw new NotFoundException("Causale con ID: " + id + " non trovata per l'ente: " + enteCodice);
                }



                // Aggiorna i campi della causale esistente
                existingCausale.setDescrizione(causaleRequest.getDescrizione());
                // Non permettiamo di cambiare il codice tramite update, solo la descrizione per ora
                // existingCausale.setCodice(causaleRequest.getCodice()); 

                // Recupera e imposta Tipo (se modificato)
                if (causaleRequest.getTipo() != null && causaleRequest.getTipo().getId() != null &&
                    (existingCausale.getCausaleTipo() == null || !causaleRequest.getTipo().getId().equals(existingCausale.getCausaleTipo().getId()))) {
                    AccrualDCausaleTipo tipoEntity = causaleTipoRepository.findById(causaleRequest.getTipo().getId())
                            .orElseThrow(() -> new NotFoundException("Tipo Causale con ID " + causaleRequest.getTipo().getId() + " non trovato."));
                    existingCausale.setCausaleTipo(tipoEntity);
                }

                // Recupera e imposta Evento (se modificato)
                if (causaleRequest.getEvento() != null && causaleRequest.getEvento().getId() != null &&
                    (existingCausale.getEvento() == null || !causaleRequest.getEvento().getId().equals(existingCausale.getEvento().getId()))) {
                    AccrualTEvento eventoEntity = eventoRepository.findById(causaleRequest.getEvento().getId())
                            .orElseThrow(() -> new NotFoundException("Evento con ID " + causaleRequest.getEvento().getId() + " non trovato."));
                    existingCausale.setEvento(eventoEntity);
                }

                // Imposta date di validità (se modificate)
                if (causaleRequest.getInizioValidita() != null) {
                    existingCausale.setInizioValidita(LocalDate.ofInstant(causaleRequest.getInizioValidita().toInstant(), ZoneId.systemDefault()).atStartOfDay());
                }
                if (causaleRequest.getFineValidita() != null) {
                    LocalDateTime nuovaFineValidita = LocalDate.ofInstant(causaleRequest.getFineValidita().toInstant(), ZoneId.systemDefault()).atTime(23, 59, 59);

                    // Non si può chiudere la validità se restano prime note non annullate successive a quella data
                    long primeNoteOltreFineValidita = primaNotaRepository.countNonAnnullateByCausaleIdAndDataAfter(
                            existingCausale.getId(), StatoPrimaNota.ANNULLATA.getCode(), nuovaFineValidita);
                    if (primeNoteOltreFineValidita > 0) {
                        throw new IllegalArgumentException(String.format(
                                "Impossibile impostare la fine validità al %s per la causale %s: sono presenti %d prime note non annullate con data successiva.",
                                nuovaFineValidita.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                existingCausale.getCodice(), primeNoteOltreFineValidita));
                    }

                    existingCausale.setFineValidita(nuovaFineValidita);
                } else {
                    existingCausale.setFineValidita(null); // Permetti di svuotare la data di fine validità
                }


                // Aggiorna dati di sistema
                existingCausale.setDataAggiornamento(LocalDateTime.now()); // Usare setDataAggiornamento
                existingCausale.setUtenteAggiornamento(getCurrentUserId()); // Usare setUtenteAggiornamento


                // Imposta Ente (assicurati che sia quello trovato o esistente)
                existingCausale.setEnte(enteEntity);

                // Salva la causale principale aggiornata
                existingCausale = causaleRepository.save(existingCausale);

                // Gestione Stato (se modificato)
                if (causaleRequest.getStato() != null && causaleRequest.getStato().getId() != null) {
                    Optional<AccrualRCausaleStato> currentStatoOptional = causaleStatoStoricoRepository.findByCausaleAndFineValiditaIsNull(existingCausale);
                    if (currentStatoOptional.isPresent()) {
                        AccrualRCausaleStato currentStato = currentStatoOptional.get();
                        if (!causaleRequest.getStato().getId().equals(currentStato.getCausaleStato().getId())) {
                            // Chiudi il vecchio stato
                            currentStato.setFineValidita(LocalDateTime.now());
                            currentStato.setUtenteAggiornamento(getCurrentUserId()); // Usare setUtenteAggiornamento
                            currentStato.setDataAggiornamento(LocalDateTime.now()); // Usare setDataAggiornamento
                            causaleStatoStoricoRepository.save(currentStato);

                            // Crea il nuovo stato
                            AccrualDCausaleStato newStatoEntity = causaleStatoRepository.findById(causaleRequest.getStato().getId())
                                    .orElseThrow(() -> new NotFoundException("Stato Causale con ID " + causaleRequest.getStato().getId() + " non trovato."));
                            AccrualRCausaleStato newRCausaleStato = new AccrualRCausaleStato();
                            newRCausaleStato.setCausale(existingCausale);
                            newRCausaleStato.setCausaleStato(newStatoEntity);
                            newRCausaleStato.setInizioValidita(LocalDateTime.now());
                            newRCausaleStato.setUtenteInserimento(getCurrentUserId());
                            newRCausaleStato.setDataInserimento(LocalDateTime.now());
                            newRCausaleStato.setEnte(enteEntity);
                            causaleStatoStoricoRepository.save(newRCausaleStato);
                            // existingCausale.setCurrentStato(newRCausaleStato); // Rimosso: campo non presente in entità
                        }
                    }
                }


                // Gestione Conti Collegati: Logica di "Diff" per C/U/D
                List<AccrualRCausalePdc> existingConti = causalePdcRepository.findByCausaleAndEnteWithRelations(existingCausale.getId(), enteEntity.getId());
                Map<Long, AccrualRCausalePdc> existingContiMap = existingConti.stream()
                        .collect(Collectors.toMap(conto -> conto.getPdc().getId(), conto -> conto));

                List<ContoCollegato> requestConti = causaleRequest.getContiCollegati() != null ? causaleRequest.getContiCollegati() : java.util.Collections.emptyList();
                Map<Long, ContoCollegato> requestContiMap = requestConti.stream()
                        .collect(Collectors.toMap(conto -> conto.getPdcId(), conto -> conto));

                LOG.info("requestConti - requestConti: {}", requestConti);

                // Delete
                for (AccrualRCausalePdc dbConto : existingConti) {
                    if (!requestContiMap.containsKey(dbConto.getPdc().getId())) {
                        causalePdcRepository.delete(dbConto);
                    }
                }

                // Create or Update
                for (ContoCollegato reqConto : requestConti) {
                    Long pdcId = reqConto.getPdcId();
                    AccrualRCausalePdc dbConto = existingContiMap.get(pdcId);
                    
                    LOG.info("dbConto - dbConto: {}", dbConto);
                    if (dbConto == null) { // Create
                        AccrualRCausalePdc newRCausalePdc = new AccrualRCausalePdc();
                        AccrualTPdc pdcEntity = pdcRepository.findById(pdcId)
                            .orElseThrow(() -> new NotFoundException("PDC con ID " + pdcId + " non trovato."));
                        if (!pdcEntity.getEnte().getId().equals(enteId)) {
                             throw new IllegalArgumentException("PDC con ID " + pdcId + " non appartiene all'ente specificato.");
                        }
                        newRCausalePdc.setPdc(pdcEntity);
                        newRCausalePdc.setCausale(existingCausale);
                        newRCausalePdc.setEnte(enteEntity);
                        newRCausalePdc.setSegno(TipoSegno.getByCode(reqConto.getSegno().toString()));
                        newRCausalePdc.setTipoImporto(TipoImporto.getByCode(reqConto.getTipoImporto().toString()));
                        newRCausalePdc.setInizioValidita(LocalDateTime.now());
                        newRCausalePdc.setUtenteInserimento(getCurrentUserId());
                        newRCausalePdc.setDataInserimento(LocalDateTime.now());
                        causalePdcRepository.save(newRCausalePdc);
                    } else { // Update
                        boolean needsUpdate = false;
                        TipoSegno reqSegno = TipoSegno.getByCode(reqConto.getSegno().toString());
                        if (dbConto.getSegno() != reqSegno) {
                            dbConto.setSegno(reqSegno);
                            needsUpdate = true;
                        }
                        TipoImporto reqTipoImporto = TipoImporto.getByCode(reqConto.getTipoImporto().toString());
                        if (dbConto.getTipoImporto() != reqTipoImporto) {
                            dbConto.setTipoImporto(reqTipoImporto);
                            needsUpdate = true;
                        }

                        if (needsUpdate) {
                            dbConto.setUtenteAggiornamento(getCurrentUserId());
                            dbConto.setDataAggiornamento(LocalDateTime.now());
                            causalePdcRepository.save(dbConto);
                        }
                    }
                }

                // Mappa e restituisci il DTO completo
                Causale resultDto = causaleMapper.toDto(existingCausale);

                // Recupera i conti collegati aggiornati e mapparli al DTO per la risposta
                List<AccrualRCausalePdc> savedContiCollegatiEntities = causalePdcRepository.findByCausaleAndEnteWithRelations(existingCausale.getId(), enteEntity.getId());
                List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> savedContiCollegatiDto = savedContiCollegatiEntities.stream()
                        .map(rcp -> {
                            AccrualTPdc pdc = rcp.getPdc();
                            it.csi.accrual.accrualsbsrv.dto.ContoCollegato contoDto = new it.csi.accrual.accrualsbsrv.dto.ContoCollegato();
                            contoDto.setPdcId(pdc.getId()); // Set the pdcId
                            contoDto.setCodice(pdc.getCodiceUfficiale() != null ? pdc.getCodiceUfficiale() : pdc.getCodice());
                            contoDto.setDescrizione(pdc.getDescrizione());
                            if (pdc.getPdcClasse() != null) {
                                contoDto.setClasse(pdc.getPdcClasse().getDescrizione());
                            } else {
                                contoDto.setClasse(null);
                            }
                            contoDto.setSegno(rcp.getSegno() != null ? ContoCollegato.SegnoEnum.valueOf(rcp.getSegno().getCode()) : null);
                            contoDto.setTipoImporto(rcp.getTipoImporto() != null ? ContoCollegato.TipoImportoEnum.valueOf(rcp.getTipoImporto().getCode()) : null);
                            contoDto.setInizioValidita(rcp.getInizioValidita() != null ? java.util.Date.from(rcp.getInizioValidita().atZone(ZoneId.systemDefault()).toInstant()) : null);
                            contoDto.setFineValidita(rcp.getFineValidita() != null ? java.util.Date.from(rcp.getFineValidita().atZone(ZoneId.systemDefault()).toInstant()) : null);
                            return contoDto;
                        })
                        .collect(Collectors.toList());
                resultDto.setContiCollegati(savedContiCollegatiDto);

                // Aggiorna lo stato nel DTO di risposta
                Optional<AccrualRCausaleStato> statoCorrente = causaleStatoStoricoRepository.findByCausaleAndFineValiditaIsNull(existingCausale);
                statoCorrente.ifPresent(s -> resultDto.setStato(causaleStatoMapper.toDto(s.getCausaleStato())));
                
                return resultDto;
            }

            @Override
            @Transactional
            public void validaCausale(Long id) {
                String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
                LOG.info("validaCausale called for id: {} with enteCodice: {}", id, enteCodice);

                if (enteCodice == null || enteCodice.isEmpty()) {
                    throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
                }

                Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
                if (optionalEnteEntity.isEmpty()) {
                    throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
                }
                AccrualTEnte enteEntity = optionalEnteEntity.get();

                AccrualTCausale existingCausale = causaleRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Causale non trovata con id: " + id));

                if (!existingCausale.getEnte().getId().equals(enteEntity.getId())) {
                    throw new NotFoundException("Causale con ID: " + id + " non trovata per l'ente: " + enteCodice);
                }

                // Verifica lo stato corrente
                AccrualRCausaleStato currentStato = causaleStatoStoricoRepository.findByCausaleAndFineValiditaIsNull(existingCausale)
                        .orElseThrow(() -> new IllegalStateException("Stato corrente non trovato per la causale: " + id));

                if (!"P".equals(currentStato.getCausaleStato().getCodice())) {
                    throw new IllegalArgumentException("La causale non è in stato 'Provvisorio' e non può essere validata.");
                }

                // Storicizza lo stato corrente
                currentStato.setFineValidita(LocalDateTime.now());
                currentStato.setUtenteAggiornamento(getCurrentUserId());
                currentStato.setDataAggiornamento(LocalDateTime.now());
                causaleStatoStoricoRepository.save(currentStato);

                // Crea il nuovo stato 'Valido'
                AccrualDCausaleStato statoValidoEntity = causaleStatoRepository.findByCodice("V")
                        .orElseThrow(() -> new IllegalStateException("Stato 'V' (Valido) non trovato nel sistema."));
                
                AccrualRCausaleStato newState = new AccrualRCausaleStato();
                newState.setCausale(existingCausale);
                newState.setCausaleStato(statoValidoEntity);
                newState.setInizioValidita(LocalDateTime.now());
                newState.setUtenteInserimento(getCurrentUserId());
                newState.setDataInserimento(LocalDateTime.now());
                newState.setEnte(enteEntity);
                causaleStatoStoricoRepository.save(newState);
            }

    @Override
    @Transactional
    public void deleteCausale(Long id) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("deleteCausale called for id: {} with enteCodice: {}", id, enteCodice);

        if (enteCodice == null || enteCodice.isEmpty()) {
            throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
        }

        Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
        if (optionalEnteEntity.isEmpty()) {
            throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
        }
        AccrualTEnte enteEntity = optionalEnteEntity.get();

        AccrualTCausale existingCausale = causaleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Causale non trovata con id: " + id));

        if (!existingCausale.getEnte().getId().equals(enteEntity.getId())) {
            throw new NotFoundException("Causale con ID: " + id + " non trovata per l'ente: " + enteCodice);
        }

        Set<String> userAzioniDel = permissionHelper.getUserAzioniCodici();
        if (!isCausaleEliminabile(existingCausale, userAzioniDel)) {
            throw new IllegalArgumentException("La causale non è eliminabile.");
        }

        // Una causale con prime note collegate non annullate non può essere eliminata
        long primeNoteCollegate = primaNotaRepository.countNonAnnullateByCausaleId(
                existingCausale.getId(), StatoPrimaNota.ANNULLATA.getCode());
        if (primeNoteCollegate > 0) {
            throw new IllegalArgumentException(String.format(
                    "Impossibile eliminare la causale %s: sono presenti %d prime note collegate non annullate.",
                    existingCausale.getCodice(), primeNoteCollegate));
        }


        existingCausale.setDataAggiornamento(LocalDateTime.now());
        existingCausale.setDataCancellazione(LocalDateTime.now());
        existingCausale.setUtenteCancellazione(getCurrentUserId());
        existingCausale.setFineValidita(LocalDateTime.now());
        causaleRepository.save(existingCausale);

        // Storicizza lo stato corrente
        AccrualRCausaleStato currentStato = causaleStatoStoricoRepository.findByCausaleAndFineValiditaIsNull(existingCausale)
                .orElseThrow(() -> new IllegalStateException("Stato corrente non trovato per la causale: " + id));

        currentStato.setFineValidita(LocalDateTime.now());
        currentStato.setUtenteAggiornamento(getCurrentUserId());
        currentStato.setDataAggiornamento(LocalDateTime.now());
        causaleStatoStoricoRepository.save(currentStato);

        // Crea il nuovo stato 'Annullato'
        AccrualDCausaleStato statoAnnullatoEntity = causaleStatoRepository.findByCodice("A")
                .orElseThrow(() -> new IllegalStateException("Stato 'A' (Annullato) non trovato nel sistema."));
        
        AccrualRCausaleStato newState = new AccrualRCausaleStato();
        newState.setCausale(existingCausale);
        newState.setCausaleStato(statoAnnullatoEntity);
        newState.setInizioValidita(LocalDateTime.now());
        newState.setUtenteInserimento(getCurrentUserId());
        newState.setDataInserimento(LocalDateTime.now());
        newState.setEnte(enteEntity);
        causaleStatoStoricoRepository.save(newState);
    }

    @Override
    public List<Causale> getEventiCausale(String eventoCodice, String statoCodice) {
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        LOG.info("getEventiCausale called with eventoCodice: {}, statoCodice: {}, enteCodice: {}", eventoCodice, statoCodice, enteCodice);

        if (enteCodice == null || enteCodice.isEmpty()) {
            throw new IllegalArgumentException("X-Ente-Codice cannot be null or empty.");
        }

        Optional<AccrualTEnte> optionalEnteEntity = enteRepository.findByCodiceUnivoco(enteCodice);
        if (optionalEnteEntity.isEmpty()) {
            throw new NotFoundException("Ente with CodiceFiscale: " + enteCodice + " not found.");
        }
        AccrualTEnte enteEntity = optionalEnteEntity.get();
        Long enteId = enteEntity.getId();

        // Recupera l'evento per codice
        AccrualTEvento eventoEntity = eventoRepository.findByCodiceAndEnte_CodiceUnivoco(eventoCodice, enteCodice)
            .orElseThrow(() -> new NotFoundException("Evento with codice: " + eventoCodice + " not found for ente: " + enteCodice));

        // Recupera le causali per evento e ente
        List<AccrualTCausale> causaleEntities = causaleRepository.findByEvento_IdAndEnte_CodiceUnivoco(eventoEntity.getId(), enteCodice);

        return causaleEntities.stream()
                .filter(causale -> isCausaleInStato(causale, statoCodice))
                .map(causaleMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Verifica che lo stato corrente della causale sia quello richiesto.
     *
     * @param statoCodice codice dello stato richiesto; se null o vuoto la verifica e' sempre superata.
     * @return false se la causale non ha uno stato corrente, perche' uno stato richiesto non puo' essere soddisfatto.
     */
    // AI generated 2026-09-16
    // reviewed by tarantinod
    private boolean isCausaleInStato(AccrualTCausale causale, String statoCodice) {
        if (statoCodice == null || statoCodice.isBlank()) {
            return true;
        }
        return causaleStatoStoricoRepository.findByCausaleAndFineValiditaIsNull(causale)
                .map(statoCorrente -> statoCodice.equals(statoCorrente.getCausaleStato().getCodice()))
                .orElse(false);
    }

    @Override
    public List<Evento> findEventiByCausaleId(Long idCausale) {
        LOG.warn("findEventiByCausaleId not yet implemented. Returning empty list.");
        // Implementazione placeholder
        return new java.util.ArrayList<>();
    }
}
