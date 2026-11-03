/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualTSoggetto;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.SoggettoMapper;
import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;
import it.csi.accrual.accrualsbsrv.dto.PagedSoggetto;
import it.csi.accrual.accrualsbsrv.dto.Soggetto;
import it.csi.accrual.accrualsbsrv.util.FilterSpecificationBuilder;

import org.springframework.transaction.annotation.Transactional;

@Service
public class SoggettoService {

    private static final Logger logger = LoggerFactory.getLogger(SoggettoService.class);

    private final AccrualTSoggettoRepository soggettoRepository;
    private final FilterSpecificationBuilder<AccrualTSoggetto> filterBuilder;
    private final SoggettoMapper soggettoMapper;

    public SoggettoService(AccrualTSoggettoRepository soggettoRepository,
                               FilterSpecificationBuilder<AccrualTSoggetto> filterBuilder,
                               SoggettoMapper soggettoMapper) {
        this.soggettoRepository = soggettoRepository;
        this.filterBuilder = filterBuilder;
        this.soggettoMapper = soggettoMapper;
    }

    @Cacheable("soggetti")
    @Transactional
    public Soggetto getSoggettoByCodice(String codice) {
        logger.info("Attempting to find soggetto with codice: {}", codice);
        Long idEnte = EnteContextHolder.getCurrentEnte();
        
        if (idEnte == null) {
            throw new SecurityException("Nessun contesto multi-ente disponibile");
        }
        
        return soggettoRepository.findByCodiceAndEnte_Id(codice, idEnte)
                .map(accrualTSoggetto -> {
                    // Explicitly access lazy-loaded fields to initialize them
                    accrualTSoggetto.getStato().getCodice(); // Access a field to trigger loading
                    accrualTSoggetto.getTipo().getCodice(); // Access a field to trigger loading
                    return soggettoMapper.toDto(accrualTSoggetto);
                })
                .orElseThrow(() -> new NotFoundException("Soggetto con codice " + codice + " non trovato per l'ente corrente"));
    }

    @Cacheable("soggetti")
    @Transactional
    public Soggetto getSoggettoByCodiceFiscale(String codiceFiscale) {
        logger.info("Attempting to find soggetto with codice fiscale: {}", codiceFiscale);
        Long idEnte = EnteContextHolder.getCurrentEnte();
        
        if (idEnte == null) {
            throw new SecurityException("Nessun contesto multi-ente disponibile");
        }
        
        return soggettoRepository.findByCodiceFiscaleAndEnte_Id(codiceFiscale, idEnte)
                .map(accrualTSoggetto -> {
                    // Explicitly access lazy-loaded fields to initialize them
                    accrualTSoggetto.getStato().getCodice(); // Access a field to trigger loading
                    accrualTSoggetto.getTipo().getCodice(); // Access a field to trigger loading
                    return soggettoMapper.toDto(accrualTSoggetto);
                })
                .orElseThrow(() -> new NotFoundException("Soggetto con codice fiscale " + codiceFiscale + " non trovato per l'ente corrente"));
    }


    @Cacheable("soggetti")
    public Soggetto getSoggettoById(Long id) {
        logger.debug("Attempting to find soggetto with id: {}", id);
        Long idEnte = EnteContextHolder.getCurrentEnte();
        
        if (idEnte == null) {
            throw new SecurityException("Nessun contesto multi-ente disponibile");
        }
        
        // Cerca il soggetto specifico per ID e ente
        Specification<AccrualTSoggetto> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("id"), id),
                cb.equal(root.get("ente").get("id"), idEnte)
            );
        
        return soggettoRepository.findOne(spec)
                .map(soggettoMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Soggetto con id " + id + " non trovato per l'ente corrente"));
    }


    @Cacheable("soggetti")
    public PagedSoggetto getAllSoggetti(String filter, String sort, Integer offset, Integer limit) {
        logger.debug("Entering getAllSoggetti");
        Long idEnte = EnteContextHolder.getCurrentEnte();
        
        if (idEnte == null) {
            throw new SecurityException("Nessun contesto multi-ente disponibile");
        }
        
        int pageNumber = (offset != null && limit != null && limit > 0) ? offset / limit : 0;
        int pageSize = (limit != null && limit > 0) ? limit : 20;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if (sort != null && !sort.isEmpty()) {
            Sort springSort = Sort.unsorted();
            String[] sortParams = sort.split(",");
            for (String param : sortParams) {
                if (param.startsWith("+")) {
                    springSort = springSort.and(Sort.by(param.substring(1)).ascending());
                } else if (param.startsWith("-")) {
                    springSort = springSort.and(Sort.by(param.substring(1)).descending());
                } else {
                    springSort = springSort.and(Sort.by(param).ascending());
                }
            }
            pageable = PageRequest.of(pageNumber, pageSize, springSort);
        }

        Specification<AccrualTSoggetto> specification = filterBuilder.build(filter);
        
        // Aggiungi il filtro per l'ente corrente
        Specification<AccrualTSoggetto> enteFilter = (root, query, cb) -> 
            cb.equal(root.get("ente").get("id"), idEnte);
        
        if (specification != null) {
            specification = specification.and(enteFilter);
        } else {
            specification = enteFilter;
        }

        Page<AccrualTSoggetto> page = soggettoRepository.findAll(specification, pageable);
        logger.info("Found {} soggetti on page {} of {} total elements.", page.getNumberOfElements(), page.getNumber(), page.getTotalElements());

        List<Soggetto> dtoList = page.getContent().stream()
                .map(soggettoMapper::toDto)
                .collect(Collectors.toList());

        PagedSoggetto pagedSoggetto = new PagedSoggetto();
        pagedSoggetto.setData(dtoList);
        pagedSoggetto.setPage(page.getNumber());
        pagedSoggetto.setPageSize(page.getSize());
        pagedSoggetto.setTotalElements(page.getTotalElements());
        return pagedSoggetto;
    }


}
