/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDAttributo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFonte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaAttributo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaLegame;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEntitaContabile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTParametro;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNotaImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.StatoPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoFase;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDAttributoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFonteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPrimaNotaStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDPrimaNotaTipoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRPrimaNotaAttributoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRPrimaNotaStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTParametroRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEntitaContabileRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEventoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPrimaNotaImportoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPrimaNotaRepository;
import it.csi.accrual.accrualcommonsrv.be.util.PrimaNotaNumService;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.security.PermissionHelper;
import it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;
import it.csi.accrual.accrualsbsrv.be.mapper.PrimaNotaMapper;
import it.csi.accrual.accrualsbsrv.be.service.PrimaNotaService;
import it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO;
import it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO;
import it.csi.accrual.accrualsbsrv.dto.PagePrimaNotaSearchResultDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaCreateUpdateDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaDetailDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaIdDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchFilterDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchResultDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaTestataDTO;

@Service
@Transactional
public class PrimaNotaServiceImpl implements PrimaNotaService {

    private static final Logger log = LoggerFactory.getLogger(PrimaNotaServiceImpl.class);

    // TASK 4: Causale per apertura patrimoniale (da configurare nel DB)
    private static final String CAUSALE_APERTURA_PATRIMONIALE = "APERTURA_ATTIVO";

    @Autowired
    private AccrualTPrimaNotaRepository primaNotaRepository;

    @Autowired
    private AccrualDPrimaNotaTipoRepository tipoRepository;

    @Autowired
    private AccrualDFonteRepository fonteRepository;

    @Autowired
    private AccrualTCausaleRepository causaleRepository;

    @Autowired
    private AccrualDPrimaNotaStatoRepository statoRepository;

    @Autowired // AGGIUNTO
    private AccrualTPrimaNotaImportoRepository primaNotaImportoRepository;

    @Autowired // AGGIUNTO
    private AccrualRPrimaNotaAttributoRepository primaNotaAttributoRepository;

    @Autowired // AGGIUNTO
    private AccrualRPrimaNotaStatoRepository accrualRPrimaNotaStatoRepository; // Repository per la tabella di relazione storica degli stati

    @Autowired // AGGIUNTO
    private AccrualTEventoRepository eventoRepository; // Repository per gli eventi

    @Autowired // AGGIUNTO
    private AccrualDAttributoRepository attributoRepository;

    @Autowired // AGGIUNTO
    private AccrualTEntitaContabileRepository entitaContabileRepository;

    @Autowired // AGGIUNTO
    private AccrualTPdcRepository pdcRepository;

    @Autowired // AGGIUNTO
    private AccrualTEnteRepository enteRepository;

    @Autowired
    private PrimaNotaNumService primaNotaNumService;

    @Autowired
    private AccrualTParametroRepository parametroRepository;

    @Autowired
    private PrimaNotaMapper primaNotaMapper;

    @Autowired
    private PermissionHelper permissionHelper;

    // AGGIUNTO
    public enum TipoImporto {
        DARE,
        AVERE
    }

    /**
     * Verifica il valore del parametro VERIFICA_RINUMERAZIONE_PRIME_NOTE per l'ente
     * @param ente L'ente per cui verificare il parametro
     * @return true se il parametro è impostato a "SI", false altrimenti
     */
    private boolean isVerificaRinumerazioneAttiva(AccrualTEnte ente) {
        Optional<AccrualTParametro> parametro = parametroRepository.findByParametroNomeAndEnteAndParametroAbilitatoTrue(
            "VERIFICA_RINUMERAZIONE_PRIME_NOTE", ente
        );
        
        return parametro.isPresent() && "SI".equalsIgnoreCase(parametro.get().getParametroValore());
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
    public PagePrimaNotaSearchResultDTO searchPrimeNote(PrimaNotaSearchFilterDTO filter, Integer limit, Integer offset, String sort) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        Integer annoBilancio = SecurityContextHelper.getAnnoBilancioStatic();
        log.info("Richiesta searchPrimeNote con parametri - filter: {}, limit: {}, offset: {}, enteCodice: {}, annoBilancio: {}, sort: {}", filter, limit, offset, enteCodice, annoBilancio, sort);

        int pageSize = (limit != null) ? limit : 20;

        // Filtro fisso per ente + anno di bilancio dal token JWT (analogo a PDC)
        Specification<AccrualTPrimaNota> spec = Specification
                .where(PrimaNotaSpecifications.byEnteCodiceUnivoco())
                .and(PrimaNotaSpecifications.byAnnoBilancio());

        if (filter.getCodiceTipo() != null) {
            spec = spec.and(PrimaNotaSpecifications.byTipo(filter.getCodiceTipo()));
        }
        if (filter.getCodiceFonte() != null) {
            spec = spec.and(PrimaNotaSpecifications.byFonte(filter.getCodiceFonte()));
        }
        if (filter.getCodiceEvento() != null) {
            spec = spec.and(PrimaNotaSpecifications.byEvento(filter.getCodiceEvento()));
        }
        if (filter.getCodiceCausale() != null) {
            spec = spec.and(PrimaNotaSpecifications.byCausale(filter.getCodiceCausale()));
        }
        if (filter.getNumeroProvvisorio() != null) {
            spec = spec.and(PrimaNotaSpecifications.byNumeroProvvisorio(filter.getNumeroProvvisorio()));
        }
        if (filter.getNumeroDefinitivo() != null) {
            spec = spec.and(PrimaNotaSpecifications.byNumeroDefinitivo(filter.getNumeroDefinitivo()));
        }
        if (filter.getCodiceStato() != null) {
            spec = spec.and(PrimaNotaSpecifications.byStato(filter.getCodiceStato()));
        }
        if (filter.getDescrizione() != null) {
            spec = spec.and(PrimaNotaSpecifications.byDescrizione(filter.getDescrizione()));
        }
        if (filter.getCodiceConto() != null) {
            spec = spec.and(PrimaNotaSpecifications.byCodiceConto(filter.getCodiceConto()));
        }
        if (filter.getDataRegProvvisoriaDa() != null) {
            spec = spec.and(PrimaNotaSpecifications.byDataRegProvvisoriaDa(filter.getDataRegProvvisoriaDa()));
        }
        if (filter.getDataRegProvvisoriaA() != null) {
            spec = spec.and(PrimaNotaSpecifications.byDataRegProvvisoriaA(filter.getDataRegProvvisoriaA()));
        }
        if (filter.getDataRegDefinitivaDa() != null) {
            spec = spec.and(PrimaNotaSpecifications.byDataRegDefinitivaDa(filter.getDataRegDefinitivaDa()));
        }
        if (filter.getDataRegDefinitivaA() != null) {
            spec = spec.and(PrimaNotaSpecifications.byDataRegDefinitivaA(filter.getDataRegDefinitivaA()));
        }
        if (filter.getImportoDa() != null) {
            spec = spec.and(PrimaNotaSpecifications.byImportoDa(filter.getImportoDa()));
        }
        if (filter.getImportoA() != null) {
            spec = spec.and(PrimaNotaSpecifications.byImportoA(filter.getImportoA()));
        }
        if (filter.isSoloNonQuadrate()) {
            spec = spec.and(PrimaNotaSpecifications.nonQuadrata());
        }

        // Paginazione e ordinamento delegati al DB
        int page = (offset != null && limit != null && limit > 0) ? offset / limit : 0;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "numero"));

        Page<AccrualTPrimaNota> pagedPrimaNotaEntities = primaNotaRepository.findAll(spec, pageable);
        log.debug("Recuperati {} risultati (pagina {}/{}) per la ricerca", pagedPrimaNotaEntities.getNumberOfElements(), page, pagedPrimaNotaEntities.getTotalPages());

        Set<String> userAzioni = permissionHelper.getUserAzioniCodici();

        List<PrimaNotaSearchResultDTO> dtoList = pagedPrimaNotaEntities.getContent().stream()
                .map(entity -> {
                    PrimaNotaSearchResultDTO dto = primaNotaMapper.toSearchResultDto(entity);
                    dto.setIsAggiornabile(isAggiornabile(entity, userAzioni));
                    dto.setIsAnnullabile(isAnnullabile(entity, userAzioni));
                    dto.setIsRiapribile(isRiapribile(entity, userAzioni));
                    dto.setIsValidabile(isValidabile(entity, userAzioni));
                    return dto;
                })
                .collect(Collectors.toList());

        // Recupero batch dei totali
        List<Long> pnIds = dtoList.stream().map(PrimaNotaSearchResultDTO::getId).collect(Collectors.toList());
        if (!pnIds.isEmpty()) {
            Map<Long, it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota> totaliMap = primaNotaImportoRepository.findTotaliByPrimaNotaIds(pnIds).stream()
                .collect(Collectors.toMap(it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota::getPrimaNotaId, t -> t, (existing, replacement) -> existing));

            for (PrimaNotaSearchResultDTO dto : dtoList) {
                it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota totali = totaliMap.get(dto.getId());
                if (totali != null) {
                    dto.setTotaleDare(totali.getTotaleDare() != null ? totali.getTotaleDare().doubleValue() : 0.0);
                    dto.setTotaleAvere(totali.getTotaleAvere() != null ? totali.getTotaleAvere().doubleValue() : 0.0);
                } else {
                    dto.setTotaleDare(0.0);
                    dto.setTotaleAvere(0.0);
                }
            }
        }

        PagePrimaNotaSearchResultDTO result = new PagePrimaNotaSearchResultDTO();
        result.setContent(dtoList);
        result.setTotalPages(pagedPrimaNotaEntities.getTotalPages());
        result.setTotalElements((int) pagedPrimaNotaEntities.getTotalElements());
        result.setNumber(pagedPrimaNotaEntities.getNumber());
        result.setSize(pagedPrimaNotaEntities.getSize());
        result.setNumberOfElements(pagedPrimaNotaEntities.getNumberOfElements());
        result.setFirst(pagedPrimaNotaEntities.isFirst());
        result.setLast(pagedPrimaNotaEntities.isLast());
        result.setEmpty(pagedPrimaNotaEntities.isEmpty());

        return result;
    }

    @Override
    public PrimaNotaDetailDTO getPrimaNotaById(String idPrimaNota) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getPrimaNotaById con parametri - idPrimaNota: {}, enteCodice: {}", idPrimaNota, enteCodice);
        Long id = Long.parseLong(idPrimaNota);

        // Recupera la prima nota e verifica che appartenga all'ente specificato
        AccrualTPrimaNota primaNota = primaNotaRepository.findByIdAndEnteCodiceUnivoco(id, enteCodice);
        if (primaNota == null) {
            throw new NotFoundException("Prima Nota not found with id: " + idPrimaNota + " for ente: " + enteCodice);
        }

        // Ottieni il DTO base dal mapper
        PrimaNotaDetailDTO detailDto = primaNotaMapper.toDetailDto(primaNota);

        // Popola manualmente le prime note collegate
        List<PrimaNotaCollegataDTO> primeNoteCollegate = new ArrayList<>();
        
        // Aggiungi le prime note collegate dove questa è la destinazione (primaNotaA)
        if (primaNota.getLegamiComeDestinazione() != null) {
            for (AccrualRPrimaNotaLegame legame : primaNota.getLegamiComeDestinazione()) {
                if (legame.getPrimaNotaDa() != null) {
                    PrimaNotaCollegataDTO collegataDto = creaDtoDaLegame(legame, legame.getPrimaNotaDa());
                    if (collegataDto != null) {
                        primeNoteCollegate.add(collegataDto);
                    }
                }
            }
        }
        
        // Aggiungi le prime note collegate dove questa è l'origine (primaNotaDa)
        if (primaNota.getLegamiComeOrigine() != null) {
            for (AccrualRPrimaNotaLegame legame : primaNota.getLegamiComeOrigine()) {
                if (legame.getPrimaNotaA() != null) {
                    PrimaNotaCollegataDTO collegataDto = creaDtoDaLegame(legame, legame.getPrimaNotaA());
                    if (collegataDto != null) {
                        primeNoteCollegate.add(collegataDto);
                    }
                }
            }
        }
        
        detailDto.setPrimeNoteCollegate(primeNoteCollegate);

        return detailDto;
    }

    @Override
    public PrimaNotaDetailDTO getPrimaNotaByAnnoAndNumero(Integer anno, Integer numero) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getPrimaNotaByAnnoAndNumero con parametri - anno: {}, numero: {}, enteCodice: {}", anno, numero, enteCodice);

        // Recupera la prima nota usando query ottimizzata per consultazione (senza importi/attributi)
        AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(anno, numero, enteCodice)
            .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Ottieni il DTO base dal mapper
        PrimaNotaDetailDTO detailDto = primaNotaMapper.toDetailDto(primaNota);

        // Popola manualmente le prime note collegate
        List<PrimaNotaCollegataDTO> primeNoteCollegate = new ArrayList<>();

        // Aggiungi le prime note collegate dove questa è la destinazione (primaNotaA)
        if (primaNota.getLegamiComeDestinazione() != null) {
            for (AccrualRPrimaNotaLegame legame : primaNota.getLegamiComeDestinazione()) {
                if (legame.getPrimaNotaDa() != null) {
                    PrimaNotaCollegataDTO collegataDto = creaDtoDaLegame(legame, legame.getPrimaNotaDa());
                    if (collegataDto != null) {
                        primeNoteCollegate.add(collegataDto);
                    }
                }
            }
        }

        // Aggiungi le prime note collegate dove questa è l'origine (primaNotaDa)
        if (primaNota.getLegamiComeOrigine() != null) {
            for (AccrualRPrimaNotaLegame legame : primaNota.getLegamiComeOrigine()) {
                if (legame.getPrimaNotaA() != null) {
                    PrimaNotaCollegataDTO collegataDto = creaDtoDaLegame(legame, legame.getPrimaNotaA());
                    if (collegataDto != null) {
                        primeNoteCollegate.add(collegataDto);
                    }
                }
            }
        }

        detailDto.setPrimeNoteCollegate(primeNoteCollegate);

        return detailDto;
    }
    
    private PrimaNotaCollegataDTO creaDtoDaLegame(AccrualRPrimaNotaLegame legame, AccrualTPrimaNota notaCollegata) {
        if (notaCollegata == null) {
            return null;
        }

        PrimaNotaCollegataDTO legameDto = new PrimaNotaCollegataDTO();
        legameDto.setIdPrimaNotaCollegata(notaCollegata.getId());

        // Imposta il tipo
        if (notaCollegata.getTipo() != null) {
            legameDto.setTipo(notaCollegata.getTipo().getDescrizione());
        }

        // Imposta la fonte
        if (notaCollegata.getFonte() != null) {
            legameDto.setFonte(notaCollegata.getFonte().getDescrizione());
        }

        // Imposta l'evento
        if (notaCollegata.getCausale() != null && notaCollegata.getCausale().getEvento() != null) {
            legameDto.setEvento(notaCollegata.getCausale().getEvento().getDescrizione());
        }

        // Imposta la causale
        if (notaCollegata.getCausale() != null) {
            legameDto.setCausale(notaCollegata.getCausale().getCodice());
            legameDto.setDescrizioneCausale(notaCollegata.getCausale().getDescrizione());
        }

        // Imposta il numero provvisorio
        legameDto.setNumeroProvvisorio(notaCollegata.getNumero());

        // Imposta la data di registrazione provvisoria
        if (notaCollegata.getData() != null) {
            legameDto.setDataRegProvvisoria(java.sql.Date.valueOf(notaCollegata.getData().toLocalDate()));
        }

        // Imposta il numero definitivo
        legameDto.setNumeroDefinitivo(notaCollegata.getProgressivoGiornale());

        // Imposta la data di registrazione definitiva
        if (notaCollegata.getDataRegistrazioneGiornale() != null) {
            legameDto.setDataRegDefinitiva(java.sql.Date.valueOf(notaCollegata.getDataRegistrazioneGiornale().toLocalDate()));
        }

        // Imposta lo stato
        if (notaCollegata.getStati() != null && !notaCollegata.getStati().isEmpty()) {
            String statoCorrente = getCurrentStato(notaCollegata.getStati());
            legameDto.setStato(statoCorrente);
        }

        // Imposta la descrizione
        legameDto.setDescrizione(notaCollegata.getDescrizione());

        // Imposta la relazione dal legame
        if (legame.getPrimaNotaTipoRelazione() != null) {
            legameDto.setRelazione(legame.getPrimaNotaTipoRelazione().getDescrizione());
        }

        // Imposta la data dell'operazione (potrebbe essere la data della prima nota collegata)
        if (notaCollegata.getData() != null) {
            legameDto.setDataOperazione(java.sql.Timestamp.valueOf(notaCollegata.getData()));
        }

        // Imposta un importo fittizio poiché non abbiamo un importo specifico per il legame
        legameDto.setImporto(0.0);
        legameDto.setValuta("EUR"); // Valuta di default

        return legameDto;
    }

    /**
     * Metodo per ottenere lo stato corrente della prima nota
     * Lo stato corrente è quello con fineValidita nullo nella tabella di relazione AccrualRPrimaNotaStato
     */
    private String getCurrentStato(Set<AccrualRPrimaNotaStato> stati) {
        if (stati == null || stati.isEmpty()) {
            return null;
        }

        // Cerca lo stato con fineValidita nullo (stato corrente)
        for (AccrualRPrimaNotaStato statoRel : stati) {
            if (statoRel.getFineValidita() == null) {
                return statoRel.getPrimaNotaStato().getDescrizione();
            }
        }

        // Se non troviamo uno stato con fineValidita nullo, restituiamo l'ultimo stato per data di inizio validità
        return stati.stream()
            .max((s1, s2) -> s1.getInizioValidita().compareTo(s2.getInizioValidita()))
            .map(s -> s.getPrimaNotaStato().getDescrizione())
            .orElse(null);
    }

    @Override
    public boolean checkIncompletePrimeNote() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        Integer anno = SecurityContextHelper.getAnnoBilancioStatic();
        log.info("Richiesta checkIncompletePrimeNote con parametri - enteCodice: {}, anno: {}", enteCodice, anno);

        // Verifica se esistono prime note incomplete per l'ente e anno corrente
        // Una prima nota è incompleta se:
        // 1. Non ha conti collegati
        // 2. Oppure ha TotaleDare != TotaleAvere
        // Esclude prime note ANNULLATE (stato 'A')

        List<AccrualTPrimaNota> primeNoteIncomplete = primaNotaRepository.findIncompleteByEnteCodiceUnivocoAndAnno(enteCodice, anno);

        return !primeNoteIncomplete.isEmpty();
    }

    @Override
    public List<PrimaNotaSearchResultDTO> getIncompletePrimeNote() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        Integer anno = SecurityContextHelper.getAnnoBilancioStatic();
        log.info("Richiesta getIncompletePrimeNote con parametri - enteCodice: {}, anno: {}", enteCodice, anno);
        List<AccrualTPrimaNota> primeNoteIncomplete = primaNotaRepository.findIncompleteByEnteCodiceUnivocoAndAnno(enteCodice, anno);
        Set<String> userAzioni = permissionHelper.getUserAzioniCodici();

        return primeNoteIncomplete.stream()
        		  .map(entity -> {
                       PrimaNotaSearchResultDTO dto = primaNotaMapper.toSearchResultDto(entity);
                       dto.setIsAggiornabile(isAggiornabile(entity, userAzioni));
                       dto.setIsAnnullabile(isAnnullabile(entity, userAzioni));
                       dto.setIsRiapribile(isRiapribile(entity, userAzioni));
                       dto.setIsValidabile(isValidabile(entity, userAzioni));
                       return dto;
                   })
            .collect(java.util.stream.Collectors.toList());
    }

    // AGGIUNTO: Metodo per verificare l'esistenza di prime note
    public boolean verificaPrimeNoteEsistenti(Long enteId, Integer anno, List<String> causaliCodici) {
        log.info("Verifica esistenza prime note per ente: {}, anno: {}, causali: {}", enteId, anno, causaliCodici);
        
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        // Cerca prime note che non siano state annullate usando il codice della causale e l'ID dello stato
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaliCodici, statoAnnullato.getId());
        return !primeNote.isEmpty();
    }

    // AGGIUNTO: Metodo per verificare l'esistenza di prime note usando ID causali
    public boolean verificaPrimeNoteEsistentiById(Long enteId, Integer anno, List<String> causaliCodici) {
        log.info("Verifica esistenza prime note per ente: {}, anno: {}, causali codici: {}", enteId, anno, causaliCodici);
        
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        // Cerca prime note che non siano state annullate usando il codice della causale e l'ID dello stato
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaliCodici, statoAnnullato.getId());
        return !primeNote.isEmpty();
    }
    
    // AGGIUNTO: Metodo per verificare l'esistenza della prima nota di apertura patrimoniale
    public boolean verificaPrimaNotaAperturaPatrimoniale(Long enteId, Integer anno) {
        log.info("Verifica esistenza prima nota di apertura patrimoniale per ente: {}, anno: {}", enteId, anno);
        // TODO: Definire il codice della causale per l'apertura patrimoniale
        List<String> causaleAperturaPatrimoniale = List.of("APERTURA_ATTIVO");
        
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaleAperturaPatrimoniale, statoAnnullato.getId());
        return !primeNote.isEmpty();
    }

    // AGGIUNTO: Metodo per verificare l'esistenza della prima nota di apertura patrimoniale usando ID causali
    public boolean verificaPrimaNotaAperturaPatrimonialeById(Long enteId, Integer anno) {
        log.info("Verifica esistenza prima nota di apertura patrimoniale per ente: {}, anno: {}", enteId, anno);
        // TODO: Definire il codice della causale per l'apertura patrimoniale
        List<String> causaleAperturaPatrimoniale = List.of("APERTURA_ATTIVO");
        
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        List<AccrualTPrimaNota> primeNote = primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaleAperturaPatrimoniale, statoAnnullato.getId());
        return !primeNote.isEmpty();
    }

    // AGGIUNTO: Metodo per trovare prime note per ente, anno e lista di causali
    public List<AccrualTPrimaNota> findPrimeNoteByEnteAnnoAndCausali(Long enteId, Integer anno, List<String> causaliCodici) {
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        return primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaliCodici, statoAnnullato.getId());
    }

    // AGGIUNTO: Metodo per trovare prime note per ente, anno e lista di ID causali
    public List<AccrualTPrimaNota> findPrimeNoteByEnteAnnoAndCausaliId(Long enteId, Integer anno, List<String> causaliCodici) {
        // Ottieni l'ID dello stato "ANNULLATO"
        AccrualDPrimaNotaStato statoAnnullato = statoRepository.findByCodice("ANNULLATO")
            .orElseThrow(() -> new RuntimeException("Stato ANNULLATO non trovato"));
        
        return primaNotaRepository.findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(enteId, anno, causaliCodici, statoAnnullato.getId());
    }

    // AGGIUNTO: Metodo per creare una nuova Prima Nota
    public AccrualTPrimaNota creaPrimaNota(AccrualTEnte ente, AccrualTCausale causale, AccrualDPrimaNotaTipo tipo, AccrualTEvento evento, String descrizione, AccrualTFile file, String utenteCorrente) {
        AccrualTPrimaNota primaNota = new AccrualTPrimaNota();
        primaNota.setEnte(ente);
        primaNota.setCausale(causale);
        primaNota.setTipo(tipo); // Tipo AUTOMATICA

        if (file != null && file.getFileTipo() != null && file.getFileTipo().getFonte() != null) {
            primaNota.setFonte(file.getFileTipo().getFonte());
        }

        primaNota.setDescrizione(descrizione);
       // primaNota.setFile(file); // Collega il file di flusso
        // TODO: Imposta numero provvisorio/definitivo (auto-generato?)
        primaNota.setNumero(1); // Placeholder
        primaNota.setAnno(file.getAnno()); // Assumendo che il file abbia un anno
        
        primaNota.setDataInserimento(LocalDateTime.now());
        primaNota.setUtenteInserimento(utenteCorrente);
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);

        // Imposta lo stato iniziale (es. PROVVISORIO o in base all'evento)
        AccrualDPrimaNotaStato statoEvento = (evento != null) ? this.getStatoPrimaNotaByEvento(evento) : null;
        AccrualDPrimaNotaStato statoIniziale = getStatoPrimaNotaByCodice(
            (statoEvento != null) ? statoEvento.getCodice() : "P"
        );
        storicizzaEImpostaNuovoStato(primaNota, statoIniziale, utenteCorrente, ente);
        
        return primaNotaRepository.save(primaNota);
    }

    // AGGIUNTO: Metodo per aggiungere un importo alla Prima Nota
    public void aggiungiImporto(AccrualTPrimaNota primaNota, AccrualTPdc pdc, BigDecimal importo, TipoImporto tipoImporto) {
        AccrualTPrimaNotaImporto pni = new AccrualTPrimaNotaImporto();
        pni.setPrimaNota(primaNota);
        pni.setPdc(pdc);
        pni.setImporto(importo);
        pni.setSegno(tipoImporto == TipoImporto.DARE ? TipoSegno.DARE : TipoSegno.AVERE);
        // TODO: Altri campi come tipoImporto (LORDO, IMPONIBILE, IMPOSTA)

        pni.setDataInserimento(LocalDateTime.now());
        pni.setUtenteInserimento(primaNota.getUtenteInserimento());
        pni.setDataAggiornamento(LocalDateTime.now());
        pni.setUtenteAggiornamento(primaNota.getUtenteAggiornamento());

        primaNotaImportoRepository.save(pni);
    }

    // AGGIUNTO: Metodo per aggiungere un attributo alla Prima Nota
    public void aggiungiAttributo(AccrualTPrimaNota primaNota, String codiceAttributo, String valoreAttributo) {
        AccrualRPrimaNotaAttributo pna = new AccrualRPrimaNotaAttributo();
        pna.setPrimaNota(primaNota);

        // Recupera l'AccrualDAttributo tramite il codice
        AccrualDAttributo attributo = attributoRepository.findByCodice(codiceAttributo)
            .orElseThrow(() -> new NotFoundException("Attributo con codice '" + codiceAttributo + "' non trovato."));
        pna.setAttributo(attributo); // Imposta l'entità AccrualDAttributo
        pna.setValore(valoreAttributo);
        
        // Imposta l'ente, la data di inizio validità
        pna.setEnte(primaNota.getEnte()); // Assumendo che primaNota abbia un ente
        pna.setInizioValidita(LocalDateTime.now());
        
        pna.setDataInserimento(LocalDateTime.now());
        pna.setUtenteInserimento(primaNota.getUtenteInserimento());
        pna.setDataAggiornamento(LocalDateTime.now());
        pna.setUtenteAggiornamento(primaNota.getUtenteAggiornamento());

        primaNotaAttributoRepository.save(pna);
    }
    
    // AGGIUNTO: Metodo per finalizzare la Prima Nota (aggiorna stato, numeri definitivi, ecc.)
    public void finalizzaPrimaNota(AccrualTPrimaNota primaNota, String utenteCorrente) {
        // TODO: Implementare la logica per assegnare il numero definitivo se l'evento lo prevede
        // e aggiornare lo stato finale della prima nota se necessario.
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);
        primaNotaRepository.save(primaNota);
    }

    // AGGIUNTO: Metodo per recuperare un oggetto StatoPrimaNota
    public AccrualDPrimaNotaStato getStatoPrimaNotaByCodice(String codiceStato) {
        return statoRepository.findByCodice(codiceStato)
            .orElseThrow(() -> new NotFoundException("Stato Prima Nota '" + codiceStato + "' non trovato."));
    }

    // AGGIUNTO: Metodo per storicizzare lo stato precedente e impostarne uno nuovo
    public void storicizzaEImpostaNuovoStato(AccrualTPrimaNota primaNota, AccrualDPrimaNotaStato nuovoStato, String utenteCorrente, AccrualTEnte ente) {
        // Cerca lo stato corrente della prima nota
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
        nuovoStatoRecord.setEnte(ente); // Associa l'ente
        nuovoStatoRecord.setInizioValidita(LocalDateTime.now());
        nuovoStatoRecord.setDataInserimento(LocalDateTime.now());
        nuovoStatoRecord.setUtenteInserimento(utenteCorrente);
        nuovoStatoRecord.setUtenteAggiornamento(utenteCorrente);
        accrualRPrimaNotaStatoRepository.save(nuovoStatoRecord);

        primaNotaRepository.save(primaNota);
    }


    private boolean isAggiornabile(AccrualTPrimaNota primaNota) {
        return isAggiornabile(primaNota, permissionHelper.getUserAzioniCodici());
    }

    private boolean isAggiornabile(AccrualTPrimaNota primaNota, Set<String> userAzioni) {
        // Algoritmo 2.6.2 Modificabilità (Aggiorna)
        // 1. Utente ha OP-SPE-updPrimaNota
        // 2. EntitaContabile.fase NON è PREVISIONE o CHIUSO
        // 3. Stato PN è PROVVISORIA
        
        if (!userAzioni.contains("OP-SPE-updPrimaNota")) return false;
        
        // Ottenere l'entità contabile per l'ente, l'anno e la data di validità
        String anno = String.valueOf(primaNota.getAnno());
        String enteCodiceUnivoco = primaNota.getEnte().getCodiceUnivoco();
        LocalDateTime dataCorrente = LocalDateTime.now(); // Possiamo usare la data attuale o quella della prima nota

        Optional<AccrualTEntitaContabile> entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(enteCodiceUnivoco, anno, dataCorrente);
        
        if (!entitaContabileOpt.isPresent()) {
            // Se non troviamo l'entità contabile, non possiamo determinare la modificabilità
            // In questo caso, assumiamo che non sia modificabile
            return false;
        }
        
        AccrualTEntitaContabile entitaContabile = entitaContabileOpt.get();
        
        // Controlla che la fase non sia PREVISIONE o CHIUSO
        if (entitaContabile.getFase() == TipoFase.PREVISIONE || entitaContabile.getFase() == TipoFase.CHIUSO) {
            return false;
        }

        // Controlla che lo stato della prima nota sia PROVVISORIA
        AccrualDPrimaNotaStato statoCorrente = getCurrentStato(primaNota);
        if (statoCorrente == null || StatoPrimaNota.getByCode(statoCorrente.getCodice()) != StatoPrimaNota.PROVVISORIA) {
            return false;
        }

        return true;
    }

    private boolean isAnnullabile(AccrualTPrimaNota primaNota) {
        return isAnnullabile(primaNota, permissionHelper.getUserAzioniCodici());
    }

    private boolean isAnnullabile(AccrualTPrimaNota primaNota, Set<String> userAzioni) {
        // Algoritmo 2.6.3 Annullabilità (Annulla)
        // 1. Utente ha OP-SPE-annPrimaNota
        // 2. EntitaContabile.fase NON è PREVISIONE o CHIUSO
    	// 3. Stato PN NON è ANNULLATA
        
        if (!userAzioni.contains("OP-SPE-annPrimaNota")) return false;
        
        // Ottenere l'entità contabile per l'ente, l'anno e la data di validità
        String anno = String.valueOf(primaNota.getAnno());
        String enteCodiceUnivoco = primaNota.getEnte().getCodiceUnivoco();
        LocalDateTime dataCorrente = LocalDateTime.now(); // Possiamo usare la data attuale o quella della prima nota

        Optional<AccrualTEntitaContabile> entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(enteCodiceUnivoco, anno, dataCorrente);
        
        if (!entitaContabileOpt.isPresent()) {
            // Se non troviamo l'entità contabile, non possiamo determinare l'annullabilità
            // In questo caso, assumiamo che non sia annullabile
            return false;
        }
        
        AccrualTEntitaContabile entitaContabile = entitaContabileOpt.get();
        
        // Controlla che la fase non sia PREVISIONE o CHIUSO
        if (entitaContabile.getFase() == TipoFase.PREVISIONE || entitaContabile.getFase() == TipoFase.CHIUSO) {
            return false;
        }
        
        // Controlla che lo stato della prima nota NON sia ANNULLATA
        AccrualDPrimaNotaStato statoCorrente = getCurrentStato(primaNota);
        if (statoCorrente == null || StatoPrimaNota.getByCode(statoCorrente.getCodice()) == StatoPrimaNota.ANNULLATA) {
            return false;
        }
        
        
        // Non ci sono ulteriori vincoli di stato per l'annullabilità
        return true;
    }

    private boolean isRiapribile(AccrualTPrimaNota primaNota) {
        return isRiapribile(primaNota, permissionHelper.getUserAzioniCodici());
    }

    private boolean isRiapribile(AccrualTPrimaNota primaNota, Set<String> userAzioni) {
        // Algoritmo 2.6.4 Riapertura (Riapri)
        // 1. Utente ha OP-SPE-riapriPrimaNota
        // 2. EntitaContabile.fase NON è PREVISIONE o CHIUSO
        // 3. Stato PN è DEFINITIVA
        
        if (!userAzioni.contains("OP-SPE-riapriPrimaNota")) return false;
        
        // Ottenere l'entità contabile per l'ente, l'anno e la data di validità
        String anno = String.valueOf(primaNota.getAnno());
        String enteCodiceUnivoco = primaNota.getEnte().getCodiceUnivoco();
        LocalDateTime dataCorrente = LocalDateTime.now(); // Possiamo usare la data attuale o quella della prima nota

        Optional<AccrualTEntitaContabile> entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(enteCodiceUnivoco, anno, dataCorrente);
        
        if (!entitaContabileOpt.isPresent()) {
            // Se non troviamo l'entità contabile, non possiamo determinare la riapribilità
            // In questo caso, assumiamo che non sia riapribile
            return false;
        }
        
        AccrualTEntitaContabile entitaContabile = entitaContabileOpt.get();
        
        // Controlla che la fase non sia PREVISIONE o CHIUSO
        if (entitaContabile.getFase() == TipoFase.PREVISIONE || entitaContabile.getFase() == TipoFase.CHIUSO) {
            return false;
        }

        // Controlla che lo stato della prima nota sia DEFINITIVA
        AccrualDPrimaNotaStato statoCorrente = getCurrentStato(primaNota);
        if (statoCorrente == null || StatoPrimaNota.getByCode(statoCorrente.getCodice()) != StatoPrimaNota.DEFINITIVA) {
            return false;
        }

        return true;
    }
    
    /**
     * Metodo per ottenere lo stato corrente della prima nota
     * Lo stato corrente è quello con fineValidita nullo nella tabella di relazione AccrualRPrimaNotaStato
     */
    private AccrualDPrimaNotaStato getCurrentStato(AccrualTPrimaNota primaNota) {
        if (primaNota.getStati() == null || primaNota.getStati().isEmpty()) {
            return null;
        }
        
        // Cerca lo stato con fineValidita nullo (stato corrente)
        for (AccrualRPrimaNotaStato statoRel : primaNota.getStati()) {
            if (statoRel.getFineValidita() == null) {
                return statoRel.getPrimaNotaStato();
            }
        }
        
        // Se non troviamo uno stato con fineValidita nullo, restituiamo l'ultimo stato per data di inizio validità
        return primaNota.getStati().stream()
            .max((s1, s2) -> s1.getInizioValidita().compareTo(s2.getInizioValidita()))
            .map(AccrualRPrimaNotaStato::getPrimaNotaStato)
            .orElse(null);
    }

    private boolean isValidabile(AccrualTPrimaNota primaNota) {
        return isValidabile(primaNota, permissionHelper.getUserAzioniCodici());
    }

    private boolean isValidabile(AccrualTPrimaNota primaNota, Set<String> userAzioni) {
        // Algoritmo 2.6.7 Validabilità (Valida)
        // 1. Utente ha OP-SPE-validaPrimaNota (singola) o OP-SPE-validaPrimeNote (massiva)
        // 2. EntitaContabile.fase NON è PREVISIONE o CHIUSO
        // 3. (Aggiunto) La prima nota deve essere in stato Provvisoria per la validazione singola
        
        if (!userAzioni.contains("OP-SPE-validaPrimaNota")) return false;
        
        // Ottenere l'entità contabile per l'ente, l'anno e la data di validità
        String anno = String.valueOf(primaNota.getAnno());
        String enteCodiceUnivoco = primaNota.getEnte().getCodiceUnivoco();
        LocalDateTime dataCorrente = LocalDateTime.now(); // Possiamo usare la data attuale o quella della prima nota

        Optional<AccrualTEntitaContabile> entitaContabileOpt = entitaContabileRepository.findByEnteCodiceUnivocoAndAnnoAndDataValidita(enteCodiceUnivoco, anno, dataCorrente);
        
        if (!entitaContabileOpt.isPresent()) {
            // Se non troviamo l'entità contabile, non possiamo determinare la validabilità
            // In questo caso, assumiamo che non sia validabile
            return false;
        }
        
        AccrualTEntitaContabile entitaContabile = entitaContabileOpt.get();
        
        // Controlla che la fase non sia PREVISIONE o CHIUSO
        if (entitaContabile.getFase() == TipoFase.PREVISIONE || entitaContabile.getFase() == TipoFase.CHIUSO) {
            return false;
        }

        // Controlla che lo stato della prima nota sia PROVVISORIA per la validazione singola
        AccrualDPrimaNotaStato statoCorrente = getCurrentStato(primaNota);
        if (statoCorrente == null || StatoPrimaNota.getByCode(statoCorrente.getCodice()) != StatoPrimaNota.PROVVISORIA) {
            return false;
        }

        return true;
    }

    @Override
    public List<it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota> getTipiPrimaNota() {
        log.info("Richiesta getTipiPrimaNota");
        // Recupera tutti i tipi di prima nota
        List<AccrualDPrimaNotaTipo> tipi = tipoRepository.findAll();

        // Mappa i risultati ai DTO corrispondenti
        return tipi.stream()
            .map(this::mapToTipoPrimaNotaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<it.csi.accrual.accrualsbsrv.dto.FontePrimaNota> getFontiPrimaNota() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getFontiPrimaNota con parametri - enteCodice: {}", enteCodice);
        List<AccrualDFonte> fonti;
        if (enteCodice != null && !enteCodice.trim().isEmpty()) {
            // Assumo che Fonte abbia un legame con l'ente o che possa essere filtrato
            // Modificare se la logica di filtro delle fonti è diversa
            fonti = fonteRepository.findByEnte_CodiceUnivoco(enteCodice);
        } else {
            fonti = fonteRepository.findAll();
        }

        // Mappa i risultati ai DTO corrispondenti
        return fonti.stream()
            .map(this::mapToFontePrimaNotaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota> getCausaliPrimaNota() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getCausaliPrimaNota con parametri - enteCodice: {}", enteCodice);
        List<AccrualTCausale> causali;
        if (enteCodice != null && !enteCodice.trim().isEmpty()) {
            causali = causaleRepository.findByEnte_CodiceUnivoco(enteCodice);
        } else {
            causali = causaleRepository.findAll();
        }

        // Mappa i risultati ai DTO corrispondenti
        return causali.stream()
            .map(this::mapToCausalePrimaNotaDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota> getStatiPrimaNota() {
        log.info("Richiesta getStatiPrimaNota");
        // Recupera tutti gli stati della prima nota
        List<AccrualDPrimaNotaStato> stati = statoRepository.findAll();

        // Mappa i risultati ai DTO corrispondenti
        return stati.stream()
            .map(this::mapToStatoPrimaNotaDto)
            .collect(Collectors.toList());
    }

    // Metodo helper per creare oggetti per i dropdown - DEPRECATO, usa i DTO reali
    // private Map<String, Object> createDropdownItem(Long id, String codice, String descrizione) {
    //     Map<String, Object> item = new HashMap<>();
    //     item.put("id", id);
    //     item.put("codice", codice);
    //     item.put("descrizione", descrizione);
    //     return item;
    // }

    // Metodi di mappatura dalle entità ai DTO
    private it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota mapToTipoPrimaNotaDto(AccrualDPrimaNotaTipo tipo) {
        it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota dto = new it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota();
        dto.setId(tipo.getId());
        dto.setCodice(tipo.getCodice());
        dto.setDescrizione(tipo.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.FontePrimaNota mapToFontePrimaNotaDto(AccrualDFonte fonte) {
        it.csi.accrual.accrualsbsrv.dto.FontePrimaNota dto = new it.csi.accrual.accrualsbsrv.dto.FontePrimaNota();
        dto.setId(fonte.getId());
        dto.setCodice(fonte.getCodice());
        dto.setDescrizione(fonte.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota mapToCausalePrimaNotaDto(AccrualTCausale causale) {
        it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota dto = new it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota();
        dto.setId(causale.getId());
        dto.setCodice(causale.getCodice());
        dto.setDescrizione(causale.getDescrizione());
        return dto;
    }

    private it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota mapToStatoPrimaNotaDto(AccrualDPrimaNotaStato stato) {
        it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota dto = new it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota();
        dto.setId(stato.getId());
        dto.setCodice(stato.getCodice());
        dto.setDescrizione(stato.getDescrizione());
        return dto;
    }

    @Override
    public it.csi.accrual.accrualsbsrv.dto.PagedContoCollegatoDTO getContiPrimaNota(Integer anno, Integer numero, Integer limit, Integer offset, String codiceConto) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getContiPrimaNota con parametri - anno: {}, numero: {}, limit: {}, offset: {}, codiceConto: {}, enteCodice: {}", anno, numero, limit, offset, codiceConto, enteCodice);

        // Calcola paginazione
        int page = (offset != null && limit != null && limit > 0) ? offset / limit : 0;
        int pageSize = (limit != null) ? limit : 20;

        // Recupera gli importi (conti) usando query ottimizzata con FETCH espliciti
        List<AccrualTPrimaNotaImporto> importiList = primaNotaImportoRepository.findImportiForConti(anno, numero, enteCodice, codiceConto);

        // Mappa gli importi in DTO
        List<it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO> allConti = importiList.stream()
            .map(importo -> {
                it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO contoDto = new it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO();
                contoDto.setId(importo.getId());
                contoDto.setCodice(importo.getPdc().getCodiceUfficiale());
                contoDto.setDescrizione(importo.getPdc().getDescrizione());

                // Imposta Dare o Avere in base al segno
                if (importo.getSegno() != null) {
                    if (importo.getSegno() == it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno.DARE) {
                        contoDto.setDare(importo.getImporto() != null ? importo.getImporto().doubleValue() : 0.0);
                        contoDto.setAvere(0.0);
                    } else {
                        contoDto.setAvere(importo.getImporto() != null ? importo.getImporto().doubleValue() : 0.0);
                        contoDto.setDare(0.0);
                    }
                }

                return contoDto;
            })
            .collect(Collectors.toList());

        // Recupera il totale degli elementi filtrati
        Long totalElementsLong = primaNotaImportoRepository.countImportiForConti(anno, numero, enteCodice, codiceConto);
        int totalElements = totalElementsLong.intValue();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Applica paginazione
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO> pagedConti = fromIndex < totalElements
            ? allConti.subList(fromIndex, toIndex)
            : new ArrayList<>();

        // Crea e popola il DTO paginato
        it.csi.accrual.accrualsbsrv.dto.PagedContoCollegatoDTO result = new it.csi.accrual.accrualsbsrv.dto.PagedContoCollegatoDTO();
        result.setContent(pagedConti);
        result.setTotalPages(totalPages);
        result.setTotalElements((long) totalElements);
        result.setNumber(page);
        result.setSize(pageSize);
        result.setNumberOfElements(pagedConti.size());
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        result.setEmpty(pagedConti.isEmpty());

        return result;
    }

    @Override
    public it.csi.accrual.accrualsbsrv.dto.ContiTotaliDTO getTotaliContiPrimaNota(Integer anno, Integer numero) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getTotaliContiPrimaNota con parametri - anno: {}, numero: {}, enteCodice: {}", anno, numero, enteCodice);

        // Recupera i totali dal repository (query già ottimizzata a livello DB)
        it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota totali =
            primaNotaImportoRepository.findTotaliByAnnoAndNumeroAndEnte(anno, numero, enteCodice);

        it.csi.accrual.accrualsbsrv.dto.ContiTotaliDTO totaliDto = new it.csi.accrual.accrualsbsrv.dto.ContiTotaliDTO();
        if (totali != null) {
            totaliDto.setTotaleDare(totali.getTotaleDare() != null ? totali.getTotaleDare() : java.math.BigDecimal.ZERO);
            totaliDto.setTotaleAvere(totali.getTotaleAvere() != null ? totali.getTotaleAvere() : java.math.BigDecimal.ZERO);
        } else {
            totaliDto.setTotaleDare(java.math.BigDecimal.ZERO);
            totaliDto.setTotaleAvere(java.math.BigDecimal.ZERO);
        }

        return totaliDto;
    }

    @Override
    public it.csi.accrual.accrualsbsrv.dto.PagedAttributoCollegatoDTO getAttributiPrimaNota(Integer anno, Integer numero, Integer limit, Integer offset) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getAttributiPrimaNota con parametri - anno: {}, numero: {}, limit: {}, offset: {}, enteCodice: {}", anno, numero, limit, offset, enteCodice);

        // Calcola paginazione
        int page = (offset != null && limit != null && limit > 0) ? offset / limit : 0;
        int pageSize = (limit != null) ? limit : 20;

        // Recupera gli attributi usando query ottimizzata con FETCH espliciti
        List<AccrualRPrimaNotaAttributo> attributiList = primaNotaAttributoRepository.findAttributiForPrimaNota(anno, numero, enteCodice);
        List<it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO> allAttributi = primaNotaMapper.mapAttributiList(attributiList);

        // Applica paginazione
        int totalElements = allAttributi.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO> pagedAttributi = fromIndex < totalElements
            ? allAttributi.subList(fromIndex, toIndex)
            : new ArrayList<>();

        // Crea e popola il DTO paginato
        it.csi.accrual.accrualsbsrv.dto.PagedAttributoCollegatoDTO result = new it.csi.accrual.accrualsbsrv.dto.PagedAttributoCollegatoDTO();
        result.setContent(pagedAttributi);
        result.setTotalPages(totalPages);
        result.setTotalElements((long) totalElements);
        result.setNumber(page);
        result.setSize(pageSize);
        result.setNumberOfElements(pagedAttributi.size());
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        result.setEmpty(pagedAttributi.isEmpty());

        return result;
    }

    @Override
    public it.csi.accrual.accrualsbsrv.dto.PagedPrimaNotaCollegataDTO getPrimeNoteCollegate(Integer anno, Integer numero, Integer limit, Integer offset) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getPrimeNoteCollegate con parametri - anno: {}, numero: {}, limit: {}, offset: {}, enteCodice: {}", anno, numero, limit, offset, enteCodice);

        // Calcola paginazione
        int page = (offset != null && limit != null && limit > 0) ? offset / limit : 0;
        int pageSize = (limit != null) ? limit : 20;

        // Recupera la prima nota usando query ottimizzata per legami (senza importi/attributi)
        AccrualTPrimaNota primaNota = primaNotaRepository.findForCollegate(anno, numero, enteCodice)
            .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Recupera le prime note collegate usando il mapper
        List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO> allPrimeNoteCollegate = primaNotaMapper.mapPrimeNoteCollegate(primaNota);

        // Applica paginazione
        int totalElements = allPrimeNoteCollegate.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO> pagedPrimeNoteCollegate = fromIndex < totalElements
            ? allPrimeNoteCollegate.subList(fromIndex, toIndex)
            : new ArrayList<>();

        // Crea e popola il DTO paginato
        it.csi.accrual.accrualsbsrv.dto.PagedPrimaNotaCollegataDTO result = new it.csi.accrual.accrualsbsrv.dto.PagedPrimaNotaCollegataDTO();
        result.setContent(pagedPrimeNoteCollegate);
        result.setTotalPages(totalPages);
        result.setTotalElements((long) totalElements);
        result.setNumber(page);
        result.setSize(pageSize);
        result.setNumberOfElements(pagedPrimeNoteCollegate.size());
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        result.setEmpty(pagedPrimeNoteCollegate.isEmpty());

        return result;
    }

    /**
     * Metodo helper per chiudere tutti gli stati correnti di una prima nota.
     * Imposta fineValidita = now per tutti gli stati con fineValidita null.
     */
    private void chiudiStatiCorrenti(AccrualTPrimaNota primaNota) {
        List<AccrualRPrimaNotaStato> statiCorrenti = accrualRPrimaNotaStatoRepository.findByPrimaNotaAndFineValiditaIsNull(primaNota);
        if (statiCorrenti != null && !statiCorrenti.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (AccrualRPrimaNotaStato statoCorrente : statiCorrenti) {
                statoCorrente.setFineValidita(now);
                accrualRPrimaNotaStatoRepository.save(statoCorrente);
            }
        }
    }

    /**
     * Metodo helper per impostare un nuovo stato a una prima nota.
     * Crea e salva una nuova relazione AccrualRPrimaNotaStato con lo stato specificato.
     */
    private void impostaNuovoStato(AccrualTPrimaNota primaNota, AccrualDPrimaNotaStato nuovoStato) {
        AccrualRPrimaNotaStato relazioneStato = new AccrualRPrimaNotaStato();
        relazioneStato.setPrimaNota(primaNota);
        relazioneStato.setPrimaNotaStato(nuovoStato);
        relazioneStato.setEnte(primaNota.getEnte());
        relazioneStato.setInizioValidita(LocalDateTime.now());
        relazioneStato.setFineValidita(null);
        accrualRPrimaNotaStatoRepository.save(relazioneStato);
    }

    /**
     * Metodo helper per recuperare uno stato dal database per codice.
     */
    private AccrualDPrimaNotaStato getStatoByCodice(String codice, String descrizioneErrore) {
        return statoRepository.findByCodice(codice)
            .orElseThrow(() -> new RuntimeException(descrizioneErrore));
    }

    @Override
    @Transactional
    public void annullaPrimaNota(Integer anno, Integer numero, String utenteCorrente) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta annullaPrimaNota con parametri - anno: {}, numero: {}, enteCodice: {}, utenteCorrente: {}", anno, numero, enteCodice, utenteCorrente);

        // Step 1: Recupera la prima nota usando query ottimizzata per consultazione (solo intestazione+stati)
        AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(anno, numero, enteCodice)
                .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Step 2: Verifica dell'annullabilità (fase entità contabile)
        if (!isAnnullabile(primaNota)) {
            throw new IllegalArgumentException("La prima nota non può essere annullata: entità contabile in fase non consentita (PREVISIONE o CHIUSO)");
        }

        // Step 3: Recupera lo stato "ANNULLATA"
        AccrualDPrimaNotaStato statoAnnullata = getStatoByCodice("A", "Stato ANNULLATA non trovato nel database");

        // Step 4: Chiudi lo stato precedente
        chiudiStatiCorrenti(primaNota);

        // Step 5: Inserisci nuovo stato 'ANNULLATA'
        impostaNuovoStato(primaNota, statoAnnullata);

        log.info("Prima nota {}/{} annullata con successo", anno, numero);
    }

    @Override
    @Transactional
    public void riapriPrimaNota(Integer anno, Integer numero, String utenteCorrente) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta riapriPrimaNota con parametri - anno: {}, numero: {}, enteCodice: {}, utenteCorrente: {}", anno, numero, enteCodice, utenteCorrente);

        // Step 1: Recupera la prima nota usando query ottimizzata per consultazione (solo intestazione+stati)
        AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(anno, numero, enteCodice)
                .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Step 2: Verifica della riapribilità (fase entità contabile e stato corrente)
        if (!isRiapribile(primaNota)) {
            throw new IllegalArgumentException("La prima nota non può essere riaperta: entità contabile in fase non consentita (PREVISIONE o CHIUSO) o stato non DEFINITIVA");
        }

        // Step 3: Recupera lo stato "PROVVISORIA"
        AccrualDPrimaNotaStato statoProvvisoria = getStatoByCodice("P", "Stato PROVVISORIA non trovato nel database");

        // Step 4: Chiudi lo stato precedente
        chiudiStatiCorrenti(primaNota);

        // Step 5: Inserisci nuovo stato 'PROVVISORIA'
        impostaNuovoStato(primaNota, statoProvvisoria);

        // Step 6: Imposta dataAggiornamento e utenteAggiornamento per tracciare la riapertura
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);

        // Nota: NON modifichiamo numeroDefinitivo e dataRegistrazioneDefinitiva
        // come richiesto dal CDU per la riapertura
        primaNotaRepository.save(primaNota);

        log.info("Prima nota {}/{} riaperta con successo - stato riportato a PROVVISORIA", anno, numero);
    }

    // ============================================================================
    // METODI PER INSERIMENTO E AGGIORNAMENTO (NUOVI)
    // ============================================================================

    @Override
    @Transactional
    public PrimaNotaDetailDTO inserisciPrimaNota(PrimaNotaCreateUpdateDTO dto, String utenteCorrente) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta inserisciPrimaNota per ente: {}, utente: {}", enteCodice, utenteCorrente);

        // Step 1: Validazione DTO
        if (dto == null || dto.getTestata() == null || dto.getConti() == null || dto.getConti().isEmpty()) {
            throw new IllegalArgumentException("Dati di input non validi: testata e conti sono obbligatori");
        }

        // Step 2: Recupero ente
        AccrualTEnte ente = getEnteByCodice(enteCodice);

        // Step 3: Validazione testata e recupero entità correlate
        PrimaNotaTestataDTO testataDTO = dto.getTestata();
        AccrualDPrimaNotaTipo tipo = getTipoPrimaNotaByCodice(testataDTO.getTipo());
        AccrualDFonte fonte = getFonteByCodice(testataDTO.getFonte(), enteCodice);
        AccrualTCausale causale = getCausaleByCodice(testataDTO.getCausale(), enteCodice);
        AccrualDPrimaNotaStato stato = getStatoByCodicePrimaNota(testataDTO.getStato());
        
        // Step 4: Validazione scrittura (ALG-004)
        validaScrittura(dto.getConti());
        
        // Step 5: Creazione testata prima nota
        AccrualTPrimaNota primaNota = new AccrualTPrimaNota();
        primaNota.setEnte(ente);
        primaNota.setTipo(tipo);
        primaNota.setFonte(fonte);
        primaNota.setCausale(causale);
        primaNota.setDescrizione(testataDTO.getDescrizione());
        if (testataDTO.getDataRegProvvisoria() != null) {
            primaNota.setData(testataDTO.getDataRegProvvisoria().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        } else {
            primaNota.setData(java.time.LocalDateTime.now());
        }
        
        // Step 6: Gestione numeri (provvisorio e definitivo)
        boolean isDefinitiva = "D".equals(testataDTO.getStato());

        Integer anno = SecurityContextHelper.getAnnoBilancioStatic();

        // Recupero/generazione numero provvisorio
        primaNota.setNumero(primaNotaNumService.nextNumeroProvvisorio(ente, anno));
        primaNota.setAnno(anno);

        // Se stato = DEFINITIVA, genero anche numero definitivo
        if (isDefinitiva) {
            primaNota.setProgressivoGiornale(primaNotaNumService.nextNumeroDefinitivo(ente, anno));
            primaNota.setDataRegistrazioneGiornale(calcolaDataRegistrazioneDefinitiva(primaNota.getData()));
        }
        
        // Step 7: Salvataggio testata prima nota
        primaNota = primaNotaRepository.save(primaNota);

        // Step 8: Impostazione stato iniziale (dopo aver salvato la prima nota)
        AccrualRPrimaNotaStato statoRelazione = new AccrualRPrimaNotaStato();
        statoRelazione.setPrimaNota(primaNota);
        statoRelazione.setPrimaNotaStato(stato);
        statoRelazione.setEnte(ente);
        statoRelazione.setInizioValidita(LocalDateTime.now());
        accrualRPrimaNotaStatoRepository.save(statoRelazione);

        // Step 9: Salvataggio conti collegati (ALG-006)
        for (ContoCollegatoDTO contoDTO : dto.getConti()) {
            salvaContoCollegato(primaNota, contoDTO, ente);
        }

        // Step 10: Salvataggio attributi (se presenti)
        // FILTRO: salvo solo gli attributi collegati a conti presenti nella lista finale
        if (dto.getAttributi() != null && !dto.getAttributi().isEmpty()) {
            // Creo un set dei codici conto presenti nella lista finale
            java.util.Set<String> codiciContiPresenti = dto.getConti().stream()
                .map(ContoCollegatoDTO::getCodice)
                .collect(java.util.stream.Collectors.toSet());
            
            for (AttributoCollegatoDTO attributoDTO : dto.getAttributi()) {
                // Se l'attributo ha un conto collegato, verifico che sia presente
                if (attributoDTO.getConto() != null && !attributoDTO.getConto().trim().isEmpty()) {
                    if (codiciContiPresenti.contains(attributoDTO.getConto())) {
                        // Conto presente, salvo l'attributo
                        salvaAttributo(primaNota, attributoDTO, ente);
                    } else {
                        // Conto non presente (eliminato dall'utente), salto l'attributo
                        log.debug("Attributo {} ignorato perché collegato a conto eliminato: {}", 
                                 attributoDTO.getTipoAttributo(), attributoDTO.getConto());
                    }
                } else {
                    // Attributo senza conto collegato (attributo globale), salvo
                    salvaAttributo(primaNota, attributoDTO, ente);
                }
            }
        }

        log.info("Prima nota inserita con successo - Numero: {}/{}", primaNota.getNumero(), primaNota.getAnno());

        // Step 11: Restituzione DTO minimale con solo anno e numero
        // Il frontend chiamerà la GET per recuperare il dettaglio completo
        PrimaNotaDetailDTO resultDto = new PrimaNotaDetailDTO();
        resultDto.setAnno(primaNota.getAnno());
        resultDto.setNumeroProvvisorio(primaNota.getNumero());
        return resultDto;
    }
    
    @Override
    @Transactional
    public PrimaNotaDetailDTO aggiornaPrimaNota(Integer anno, Integer numero, PrimaNotaCreateUpdateDTO dto,
                                                 String utenteCorrente) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta aggiornaPrimaNota per anno: {}, numero: {}, ente: {}", anno, numero, enteCodice);

        // Step 1: Recupero prima nota esistente
        AccrualTPrimaNota primaNota = primaNotaRepository.findByAnnoAndNumeroAndEnteCodiceUnivoco(anno, numero, enteCodice)
            .orElseThrow(() -> new NotFoundException("Prima nota non trovata: " + anno + "/" + numero));

        // Step 2: Validazione DTO
        if (dto == null || dto.getTestata() == null || dto.getConti() == null || dto.getConti().isEmpty()) {
            throw new IllegalArgumentException("Dati di input non validi: testata e conti sono obbligatori");
        }

        // Step 3: Validazione scrittura (ALG-004)
        validaScrittura(dto.getConti());

        // Step 4: Aggiornamento campi editabili (Descrizione, Data reg. provvisoria, Tipo, Fonte, Causale)
        PrimaNotaTestataDTO testataDTO = dto.getTestata();
        primaNota.setDescrizione(testataDTO.getDescrizione());
        
        // Aggiorna la data di registrazione provvisoria se fornita
        if (testataDTO.getDataRegProvvisoria() != null) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(testataDTO.getDataRegProvvisoria());
            LocalDateTime dataRegProvvisoria = LocalDateTime.ofInstant(
                testataDTO.getDataRegProvvisoria().toInstant(), 
                java.time.ZoneId.systemDefault()
            );
            primaNota.setData(dataRegProvvisoria);
        }
        
        // Aggiorna Tipo, Fonte e Causale se forniti
        if (testataDTO.getTipo() != null && !testataDTO.getTipo().trim().isEmpty()) {
            AccrualDPrimaNotaTipo tipo = getTipoPrimaNotaByCodice(testataDTO.getTipo());
            primaNota.setTipo(tipo);
        }
        
        if (testataDTO.getFonte() != null && !testataDTO.getFonte().trim().isEmpty()) {
            AccrualDFonte fonte = getFonteByCodice(testataDTO.getFonte(), enteCodice);
            primaNota.setFonte(fonte);
        }
        
        if (testataDTO.getCausale() != null && !testataDTO.getCausale().trim().isEmpty()) {
            AccrualTCausale causale = getCausaleByCodice(testataDTO.getCausale(), enteCodice);
            primaNota.setCausale(causale);
        }

        // Step 5: Aggiornamento audit
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);

        // Step 6: Salvataggio testata
        primaNota = primaNotaRepository.save(primaNota);

        // Step 7: Gestione conti collegati - Approccio IBRIDO
        List<AccrualTPrimaNotaImporto> contiEsistenti = primaNotaImportoRepository.findByPrimaNota_Id(primaNota.getId());
        aggiornaContiIbrido(primaNota, dto.getConti(), contiEsistenti, utenteCorrente);

        // Step 8: Gestione attributi - Approccio IBRIDO
        // FILTRO: mantengo gli attributi collegati a conti presenti nel DTO (conti attivi)
        //         + mantengo gli attributi SENZA conto collegato (conto == null)
        if (dto.getAttributi() != null) {
            // Creo un set dei codici conto presenti nel DTO (conti che l'utente vuole mantenere)
            java.util.Set<String> codiciContiNelDto = dto.getConti().stream()
                .map(ContoCollegatoDTO::getCodice)
                .collect(java.util.stream.Collectors.toSet());

            // Filtro gli attributi: mantengo quelli collegati a conti nel DTO OPPURE quelli senza conto
            List<AttributoCollegatoDTO> attributiFiltrati = dto.getAttributi().stream()
                .filter(attr -> attr.getConto() == null || codiciContiNelDto.contains(attr.getConto()))
                .collect(java.util.stream.Collectors.toList());

            List<AccrualRPrimaNotaAttributo> attributiEsistenti = primaNotaAttributoRepository.findByPrimaNota_Id(primaNota.getId());
            aggiornaAttributiIbridi(primaNota, attributiFiltrati, attributiEsistenti, utenteCorrente);
        }

        log.info("Prima nota {}/{} aggiornata con successo", anno, numero);

        // Step 9: Restituzione DTO
        return getPrimaNotaByAnnoAndNumero(anno, numero);
    }
    
    // ============================================================================
    // METODI DI SUPPORTO PER INSERIMENTO/AGGIORNAMENTO
    // ============================================================================
    
    /**
     * Calcola la data di registrazione definitiva a partire da quella provvisoria.
     * Se la provvisoria e' successiva a oggi restituisce la mezzanotte di quel giorno, altrimenti la
     * data di sistema: senza questa regola una prima nota con provvisoria futura nasce con la
     * definitiva antecedente alla provvisoria (issue 1132).
     *
     * @param dataRegProvvisoria data di registrazione provvisoria, ammesso {@code null}
     * @return la data da registrare sul giornale, mai {@code null}
     */
    // AI generated 2026-09-18
    // reviewed by tarantinod
    static LocalDateTime calcolaDataRegistrazioneDefinitiva(LocalDateTime dataRegProvvisoria) {
        if (dataRegProvvisoria != null && dataRegProvvisoria.toLocalDate().isAfter(LocalDate.now())) {
            return dataRegProvvisoria.toLocalDate().atStartOfDay();
        }
        return LocalDateTime.now();
    }

    /**
     * ALG-003: Verifica Conto da inserire nella scrittura
     * Validazione esistenza, validità temporale, attributo foglia
     */
    private void validaConto(String codiceUfficialeConto, AccrualTEnte ente, LocalDateTime dataSistema) {
        // 1. Verifica esistenza (cerca per codiceUfficiale e ente)
        AccrualTPdc conto = pdcRepository.findByCodiceUfficialeAndEnte_IdAndDataCancellazioneIsNull(codiceUfficialeConto, ente.getId())
            .orElse(null);
        if (conto == null) {
            throw new IllegalArgumentException("WEB_ERR_021 - Conto non esistente: " + codiceUfficialeConto);
        }

        // 2. Verifica validità temporale
        if (conto.getInizioValidita() != null && conto.getInizioValidita().isAfter(dataSistema)) {
            throw new IllegalArgumentException("WEB_ERR_022 - Conto non valido rispetto alla data");
        }
        if (conto.getFineValidita() != null && conto.getFineValidita().isBefore(dataSistema)) {
            throw new IllegalArgumentException("WEB_ERR_022 - Conto non valido rispetto alla data");
        }

        // 3. Verifica attributo foglia
        if (!conto.isFoglia() && (conto.getFogliaNonFoglia() == null || !conto.getFogliaNonFoglia())) {
            throw new IllegalArgumentException("WEB_ERR_028 - Conto non è foglia");
        }
    }
    
    /**
     * ALG-004: Verifiche sul Prosegui dei Conti
     * Validazione elenco conti (min 2, segni diversi, bilanciato, segno univoco)
     */
    private void validaScrittura(List<ContoCollegatoDTO> conti) {
        // 1. Verifica campi obbligatori
        for (ContoCollegatoDTO conto : conti) {
            if (conto.getCodice() == null) {
                throw new IllegalArgumentException("WEB_ERR_0002 - Dato obbligatorio non valorizzato");
            }
        }

        // 2. Verifica minimo 2 righe
        if (conti.size() < 2) {
            throw new IllegalArgumentException("WEB_ERR_018 - Almeno 2 conti con segni diversi");
        }

        // 3. Verifica bilanciamento (usando dare e avere come numeri arrotondati a 2 decimali)
        BigDecimal totaleDare = conti.stream()
            .filter(c -> c.getDare() != null)
            .map(c -> BigDecimal.valueOf(c.getDare()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal totaleAvere = conti.stream()
            .filter(c -> c.getAvere() != null)
            .map(c -> BigDecimal.valueOf(c.getAvere()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, java.math.RoundingMode.HALF_UP);

        if (totaleDare.compareTo(totaleAvere) != 0) {
            throw new IllegalArgumentException("WEB_ERR_019 - Totale Dare e Avere devono coincidere");
        }

        // 4. Verifica segno univoco per conto (un conto non può avere sia dare che avere)
        Map<String, Boolean> contoHaDare = conti.stream()
            .filter(c -> c.getDare() != null && c.getDare() > 0)
            .collect(Collectors.toMap(
                ContoCollegatoDTO::getCodice,
                c -> true,
                (existing, replacement) -> existing
            ));

        Map<String, Boolean> contoHaAvere = conti.stream()
            .filter(c -> c.getAvere() != null && c.getAvere() > 0)
            .collect(Collectors.toMap(
                ContoCollegatoDTO::getCodice,
                c -> true,
                (existing, replacement) -> existing
            ));

        // Verifica se un conto ha sia dare che avere
        for (String codiceConto : contoHaDare.keySet()) {
            if (contoHaAvere.containsKey(codiceConto)) {
                throw new IllegalArgumentException("WEB_ERR_020 - Stesso conto con segni diversi: " + codiceConto);
            }
        }
    }
    
    /**
     * Salva un conto collegato alla prima nota
     */
    private void salvaContoCollegato(AccrualTPrimaNota primaNota, ContoCollegatoDTO contoDTO, AccrualTEnte ente) {
        // Validazione conto (ALG-003) - cerca per codiceUfficiale e ente
        validaConto(contoDTO.getCodice(), ente, LocalDateTime.now());

        // Recupero entity PDC (cerca per codiceUfficiale e ente)
        AccrualTPdc pdc = pdcRepository.findByCodiceUfficialeAndEnte_IdAndDataCancellazioneIsNull(contoDTO.getCodice(), ente.getId())
            .orElseThrow(() -> new IllegalArgumentException("Conto non trovato: " + contoDTO.getCodice()));

        // Determina il segno e l'importo in base a dare/avere
        BigDecimal importo = null;
        TipoSegno segno = null;

        if (contoDTO.getDare() != null && contoDTO.getDare() > 0) {
            importo = BigDecimal.valueOf(contoDTO.getDare()).setScale(2, java.math.RoundingMode.HALF_UP);
            segno = TipoSegno.DARE;
        } else if (contoDTO.getAvere() != null && contoDTO.getAvere() > 0) {
            importo = BigDecimal.valueOf(contoDTO.getAvere()).setScale(2, java.math.RoundingMode.HALF_UP);
            segno = TipoSegno.AVERE;
        } else {
            throw new IllegalArgumentException("WEB_ERR_0002 - Dato obbligatorio non valorizzato: dare o avere");
        }

        // Creazione importo
        AccrualTPrimaNotaImporto importoEntity = new AccrualTPrimaNotaImporto();
        importoEntity.setPrimaNota(primaNota);
        importoEntity.setPdc(pdc);
        importoEntity.setEnte(ente);
        importoEntity.setImporto(importo);
        importoEntity.setSegno(segno);

        primaNotaImportoRepository.save(importoEntity);
    }
    
    /**
     * Salva un attributo collegato alla prima nota
     */
    private void salvaAttributo(AccrualTPrimaNota primaNota, AttributoCollegatoDTO attributoDTO, AccrualTEnte ente) {
        // Recupero attributo - uso query con filtro per ente per evitare duplicati
        AccrualDAttributo attributo = attributoRepository.findByCodiceAndEnteIdAndDataCancellazioneIsNull(
                attributoDTO.getTipoAttributo(), ente.getId())
            .orElseThrow(() -> new IllegalArgumentException("Attributo non trovato: " + attributoDTO.getTipoAttributo()));

        // Creazione relazione
        AccrualRPrimaNotaAttributo relazione = new AccrualRPrimaNotaAttributo();
        relazione.setPrimaNota(primaNota);
        relazione.setAttributo(attributo);
        relazione.setEnte(ente);
        relazione.setValore(attributoDTO.getValore());
        relazione.setInizioValidita(LocalDateTime.now());

        // Recupero eventuale conto collegato
        if (attributoDTO.getConto() != null && !attributoDTO.getConto().isEmpty()) {
            // Trova l'importo corrispondente al conto
            log.info("DEBUG - Query findByPrimaNota_IdAndPdc_CodiceUfficiale: primaNotaId={}, codiceConto={}",
                primaNota.getId(), attributoDTO.getConto());

            List<AccrualTPrimaNotaImporto> importi = primaNotaImportoRepository.findByPrimaNota_IdAndPdc_CodiceUfficiale(
                primaNota.getId(), attributoDTO.getConto());
            
            AccrualTPrimaNotaImporto importoTrovato = (importi != null && !importi.isEmpty()) ? importi.get(0) : null;

            if (importoTrovato != null) {
                log.info("DEBUG - Trovato importo per conto {}: importoId={}",
                    attributoDTO.getConto(), importoTrovato.getId());
                relazione.setPrimaNotaImporto(importoTrovato);
            } else {
                log.info("DEBUG - Nessun importo trovato per conto {}", attributoDTO.getConto());
            }
        }

        primaNotaAttributoRepository.save(relazione);
    }

    /**
     * Approccio IBRIDO per i conti:
     * - Mantiene invariati (nessuna operazione)
     * - Chiude + inserisce modificati (storico)
     * - Chiude eliminati
     * - Inserisce nuovi
     */
    private void aggiornaContiIbrido(
        AccrualTPrimaNota primaNota,
        List<ContoCollegatoDTO> nuoviConti,
        List<AccrualTPrimaNotaImporto> contiEsistenti,
        String utenteCorrente
    ) {
        LocalDateTime now = LocalDateTime.now();
        
        // Mappa conti esistenti per ID
        Map<Long, AccrualTPrimaNotaImporto> mappaEsistenti = contiEsistenti.stream()
            .filter(c -> c.getDataCancellazione() == null)
            .collect(Collectors.toMap(AccrualTPrimaNotaImporto::getId, c -> c));

        // Mappa nuovi conti dal FE per ID (solo quelli che hanno già un ID)
        Map<Long, ContoCollegatoDTO> mappaNuoviConId = nuoviConti.stream()
            .filter(c -> c.getId() != null)
            .collect(Collectors.toMap(ContoCollegatoDTO::getId, c -> c, (existing, replacement) -> existing));

        // 1. Processa conti esistenti
        for (Map.Entry<Long, AccrualTPrimaNotaImporto> entry : mappaEsistenti.entrySet()) {
            Long id = entry.getKey();
            AccrualTPrimaNotaImporto esistente = entry.getValue();
            ContoCollegatoDTO nuovoConto = mappaNuoviConId.get(id);

            if (nuovoConto == null) {
                // CONTO ELIMINATO: chiudi record esistente
                log.debug("Conto ID {} eliminato: chiusura record e attributi collegati", id);
                esistente.setDataCancellazione(now);
                esistente.setUtenteCancellazione(utenteCorrente);
                primaNotaImportoRepository.save(esistente);

                cancellaAttributiCollegati(esistente, now, utenteCorrente);
            } else {
                // CONTO PRESENTE: verifica se è modificato
                Double dareValue = nuovoConto.getDare();
                Double avereValue = nuovoConto.getAvere();

                BigDecimal nuovoImporto;
                TipoSegno nuovoSegno;

                if (dareValue != null && dareValue > 0) {
                    nuovoImporto = BigDecimal.valueOf(dareValue).setScale(2, java.math.RoundingMode.HALF_UP);
                    nuovoSegno = TipoSegno.DARE;
                } else {
                    nuovoImporto = avereValue != null ? BigDecimal.valueOf(avereValue).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
                    nuovoSegno = TipoSegno.AVERE;
                }
                
                boolean eModificato = eContoModificato(esistente, nuovoImporto, nuovoSegno);

                if (eModificato) {
                    log.debug("Conto ID {} modificato: chiusura vecchio record e inserimento nuovo", id);
                    esistente.setDataCancellazione(now);
                    esistente.setUtenteCancellazione(utenteCorrente);
                    primaNotaImportoRepository.save(esistente);

                    cancellaAttributiCollegati(esistente, now, utenteCorrente);

                    AccrualTPrimaNotaImporto nuovoRecord = new AccrualTPrimaNotaImporto();
                    nuovoRecord.setPrimaNota(primaNota);
                    nuovoRecord.setPdc(esistente.getPdc());
                    nuovoRecord.setEnte(primaNota.getEnte());
                    nuovoRecord.setImporto(nuovoImporto);
                    nuovoRecord.setSegno(nuovoSegno);
                    nuovoRecord.setDataInserimento(now);
                    nuovoRecord.setUtenteInserimento(utenteCorrente);
                    primaNotaImportoRepository.save(nuovoRecord);
                }
            }
        }

        // 2. Inserisci conti nuovi (ID nullo nel DTO)
        for (ContoCollegatoDTO nuovoConto : nuoviConti) {
            if (nuovoConto.getId() == null) {
                log.debug("Conto nuovo: inserimento");
                salvaContoCollegato(primaNota, nuovoConto, primaNota.getEnte());
            }
        }
    }

    /**
     * Verifica se un conto è stato modificato
     */
    private boolean eContoModificato(
        AccrualTPrimaNotaImporto esistente,
        BigDecimal nuovoImporto,
        TipoSegno nuovoSegno
    ) {
        // Usa compareTo() invece di equals() per ignorare la scala del BigDecimal
        return esistente.getImporto().compareTo(nuovoImporto) != 0 ||
               esistente.getSegno() != nuovoSegno;
    }

    /**
     * Cancella logicamente gli attributi collegati a un importo della prima nota
     * @param importo l'importo cancellato
     * @param now data/ora corrente
     * @param utenteCorrente utente che ha effettuato l'operazione
     */
    private void cancellaAttributiCollegati(
        AccrualTPrimaNotaImporto importo,
        LocalDateTime now,
        String utenteCorrente
    ) {
        // Trova tutti gli attributi collegati a questo importo
        List<AccrualRPrimaNotaAttributo> attributiDaCancellare = primaNotaAttributoRepository
            .findByPrimaNota_Id(importo.getPrimaNota().getId())
            .stream()
            .filter(attr -> attr.getPrimaNotaImporto() != null && 
                           attr.getPrimaNotaImporto().getId().equals(importo.getId()) &&
                           attr.getDataCancellazione() == null)
            .collect(Collectors.toList());
        
        // Cancella logicamente ogni attributo
        for (AccrualRPrimaNotaAttributo attributo : attributiDaCancellare) {
            log.debug("Cancellazione attributo {} collegato a importo cancellato", 
                     attributo.getAttributo().getCodice());
            attributo.setDataCancellazione(now);
            attributo.setUtenteCancellazione(utenteCorrente);
            primaNotaAttributoRepository.save(attributo);
        }
    }

    /**
     * Approccio IBRIDO per gli attributi:
     * - Mantiene invariati (nessuna operazione)
     * - Chiude + inserisce modificati (storico)
     * - Chiude eliminati
     * - Inserisce nuovi
     * 
     * La chiave di univocità è composta da (tipoAttributo + conto) per gestire
     * attributi dello stesso tipo su conti diversi.
     */
    private void aggiornaAttributiIbridi(
        AccrualTPrimaNota primaNota,
        List<AttributoCollegatoDTO> nuoviAttributi,
        List<AccrualRPrimaNotaAttributo> attributiEsistenti,
        String utenteCorrente
    ) {
        LocalDateTime now = LocalDateTime.now();

        // Funzione helper per creare chiave composta (tipoAttributo + conto + valore)
        java.util.function.Function<AttributoCollegatoDTO, String> chiaveCompostaMapper = attr -> {
            String conto = attr.getConto() != null ? attr.getConto() : "";
            String valore = attr.getValore() != null ? attr.getValore() : "";
            return attr.getTipoAttributo() + "::" + conto + "::" + valore;
        };

        // Mappa attributi esistenti per chiave composta (tipo + conto)
        // FILTRO: escludo attributi cancellati (fineValidita != null OPPURE dataCancellazione != null)
        Map<String, AccrualRPrimaNotaAttributo> mappaEsistenti = attributiEsistenti.stream()
            .filter(a -> a.getFineValidita() == null && a.getDataCancellazione() == null)
            .collect(Collectors.toMap(
                a -> {
                    String conto = a.getPrimaNotaImporto() != null && a.getPrimaNotaImporto().getPdc() != null
                        ? a.getPrimaNotaImporto().getPdc().getCodiceUfficiale()
                        : "";
                    String valore = a.getValore() != null ? a.getValore() : "";
                    return a.getAttributo().getCodice() + "::" + conto + "::" + valore;
                },
                a -> a,
                (existing, replacement) -> existing
            ));

        // Mappa nuovi attributi per chiave composta (tipo + conto)
        Map<String, AttributoCollegatoDTO> mappaNuovi = nuoviAttributi.stream()
            .collect(Collectors.toMap(
                chiaveCompostaMapper,
                a -> a,
                (existing, replacement) -> existing
            ));

        // 1. Processa attributi esistenti
        for (Map.Entry<String, AccrualRPrimaNotaAttributo> entry : mappaEsistenti.entrySet()) {
            String chiave = entry.getKey();
            AccrualRPrimaNotaAttributo esistente = entry.getValue();
            AttributoCollegatoDTO nuovoAttributo = mappaNuovi.get(chiave);

            if (nuovoAttributo == null) {
                // ATTRIBUTO ELIMINATO: chiudi record esistente
                log.debug("Attributo {} (conto: {}) eliminato: chiusura record", 
                    esistente.getAttributo().getCodice(), 
                    esistente.getPrimaNotaImporto() != null && esistente.getPrimaNotaImporto().getPdc() != null 
                        ? esistente.getPrimaNotaImporto().getPdc().getCodiceUfficiale() 
                        : "nessuno");
                esistente.setFineValidita(now);
                esistente.setDataCancellazione(now);
                esistente.setUtenteCancellazione(utenteCorrente);
                primaNotaAttributoRepository.save(esistente);
            } else {
                // ATTRIBUTO PRESENTE: verifica se è modificato
                boolean eModificato = eAttributoModificato(esistente, nuovoAttributo);

                if (eModificato) {
                    // ATTRIBUTO MODIFICATO: chiudi vecchio e inserisci nuovo (storico)
                    log.debug("Attributo {} (conto: {}) modificato: chiusura vecchio record e inserimento nuovo", 
                        esistente.getAttributo().getCodice(),
                        esistente.getPrimaNotaImporto() != null && esistente.getPrimaNotaImporto().getPdc() != null 
                            ? esistente.getPrimaNotaImporto().getPdc().getCodiceUfficiale() 
                            : "nessuno");
                    esistente.setFineValidita(now);
                    esistente.setDataCancellazione(now);
                    esistente.setUtenteCancellazione(utenteCorrente);
                    primaNotaAttributoRepository.save(esistente);

                    // Inserisci nuovo record con valori aggiornati
                    AccrualRPrimaNotaAttributo nuovoRecord = new AccrualRPrimaNotaAttributo();
                    nuovoRecord.setPrimaNota(primaNota);
                    nuovoRecord.setAttributo(esistente.getAttributo());
                    nuovoRecord.setEnte(primaNota.getEnte());
                    nuovoRecord.setValore(nuovoAttributo.getValore());
                    nuovoRecord.setInizioValidita(now);

                    // Gestione conto collegato opzionale
                    if (nuovoAttributo.getConto() != null && !nuovoAttributo.getConto().isEmpty()) {
                        List<AccrualTPrimaNotaImporto> importi = 
                            primaNotaImportoRepository.findByPrimaNota_IdAndPdc_CodiceUfficiale(
                                primaNota.getId(),
                                nuovoAttributo.getConto()
                            );
                        
                        if (importi != null && !importi.isEmpty()) {
                            nuovoRecord.setPrimaNotaImporto(importi.get(0));
                        }
                    }

                    primaNotaAttributoRepository.save(nuovoRecord);
                } else {
                    // Attributo invariato: NESSUNA OPERAZIONE
                    log.debug("Attributo {} (conto: {}) invariato: nessuna operazione", 
                        esistente.getAttributo().getCodice(),
                        esistente.getPrimaNotaImporto() != null && esistente.getPrimaNotaImporto().getPdc() != null 
                            ? esistente.getPrimaNotaImporto().getPdc().getCodiceUfficiale() 
                            : "nessuno");
                }
            }
        }

        // 2. Inserisci attributi nuovi (presenti nel BE ma non nel DB)
        for (Map.Entry<String, AttributoCollegatoDTO> entry : mappaNuovi.entrySet()) {
            String chiave = entry.getKey();
            if (!mappaEsistenti.containsKey(chiave)) {
                AttributoCollegatoDTO nuovoAttributo = entry.getValue();
                log.debug("Attributo {} (conto: {}) nuovo: inserimento", 
                    nuovoAttributo.getTipoAttributo(),
                    nuovoAttributo.getConto() != null && !nuovoAttributo.getConto().isEmpty() 
                        ? nuovoAttributo.getConto() 
                        : "nessuno");
                salvaAttributo(primaNota, nuovoAttributo, primaNota.getEnte());
            }
        }
    }

    /**
     * Verifica se un attributo è stato modificato
     */
    private boolean eAttributoModificato(
        AccrualRPrimaNotaAttributo esistente, 
        AttributoCollegatoDTO nuovo
    ) {
        return !esistente.getValore().equals(nuovo.getValore());
    }
    
    /**
     * Chiude logicamente tutti i conti esistenti (QA-009)
     * 
     * @deprecated Usare approccio IBRIDO con aggiornaContiIbrido()
     */
    @Deprecated
    private void chiudiContiEsistenti(AccrualTPrimaNota primaNota) {
        List<AccrualTPrimaNotaImporto> importi = primaNotaImportoRepository.findByPrimaNota_Id(primaNota.getId());
        for (AccrualTPrimaNotaImporto importo : importi) {
            // Imposto data cancellazione (chiusura logica)
            importo.setDataCancellazione(LocalDateTime.now());
            primaNotaImportoRepository.save(importo);
        }
    }

    /**
     * Chiude logicamente tutti gli attributi esistenti (QA-009)
     * 
     * @deprecated Usare approccio IBRIDO con aggiornaAttributiIbridi()
     */
    @Deprecated
    private void chiudiAttributiEsistenti(AccrualTPrimaNota primaNota) {
        List<AccrualRPrimaNotaAttributo> attributi = primaNotaAttributoRepository.findByPrimaNota_Id(primaNota.getId());
        for (AccrualRPrimaNotaAttributo attributo : attributi) {
            attributo.setFineValidita(LocalDateTime.now());
            primaNotaAttributoRepository.save(attributo);
        }
    }
    
    // Metodi helper per recupero entità

    private AccrualTEnte getEnteByCodice(String codice) {
        return enteRepository.findByCodiceUnivoco(codice)
            .orElseThrow(() -> new NotFoundException("Ente non trovato: " + codice));
    }

    private AccrualDPrimaNotaTipo getTipoPrimaNotaByCodice(String codice) {
        return tipoRepository.findByCodice(codice)
            .orElseThrow(() -> new NotFoundException("Tipo prima nota non trovato: " + codice));
    }

    private AccrualDFonte getFonteByCodice(String codice, String enteCodice) {
        return fonteRepository.findByCodiceAndEnte_CodiceUnivoco(codice, enteCodice)
            .orElseThrow(() -> new NotFoundException("Fonte non trovata: " + codice));
    }

    private AccrualTEvento getEventoByCodice(String codice, String enteCodice) {
        return eventoRepository.findByCodiceAndEnte_CodiceUnivoco(codice, enteCodice)
            .orElseThrow(() -> new NotFoundException("Evento non trovato: " + codice));
    }

    private AccrualTCausale getCausaleByCodice(String codice, String enteCodice) {
        AccrualTEnte ente = getEnteByCodice(enteCodice);
        AccrualTCausale causale = causaleRepository.findByCodiceAndEnte_Id(codice, ente.getId());
        if (causale == null) {
            throw new NotFoundException("Causale non trovata: " + codice);
        }
        return causale;
    }
    
    private AccrualDPrimaNotaStato getStatoByCodicePrimaNota(String codice) {
        return statoRepository.findByCodice(codice)
            .orElseThrow(() -> new NotFoundException("Stato non trovato: " + codice));
    }

    // ============================================================================
    // TASK 1-3: METODI PER VALIDAZIONE PRIMA NOTA
    // ============================================================================

    @Override
    public List<PrimaNotaSearchResultDTO> exportPrimaNotaList(PrimaNotaSearchFilterDTO filter, String sort) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta exportPrimaNotaList - filter: {}, sort: {}, enteCodice: {}", filter, sort, enteCodice);

        // Riutilizziamo la logica di ricerca (senza paginazione)
        // Filtro fisso per ente + anno di bilancio
        Specification<AccrualTPrimaNota> spec = Specification
                .where(PrimaNotaSpecifications.byEnteCodiceUnivoco())
                .and(PrimaNotaSpecifications.byAnnoBilancio());

        if (filter.getCodiceTipo() != null) spec = spec.and(PrimaNotaSpecifications.byTipo(filter.getCodiceTipo()));
        if (filter.getCodiceFonte() != null) spec = spec.and(PrimaNotaSpecifications.byFonte(filter.getCodiceFonte()));
        if (filter.getCodiceEvento() != null) spec = spec.and(PrimaNotaSpecifications.byEvento(filter.getCodiceEvento()));
        if (filter.getCodiceCausale() != null) spec = spec.and(PrimaNotaSpecifications.byCausale(filter.getCodiceCausale()));
        if (filter.getNumeroProvvisorio() != null) spec = spec.and(PrimaNotaSpecifications.byNumeroProvvisorio(filter.getNumeroProvvisorio()));
        if (filter.getNumeroDefinitivo() != null) spec = spec.and(PrimaNotaSpecifications.byNumeroDefinitivo(filter.getNumeroDefinitivo()));
        if (filter.getCodiceStato() != null) spec = spec.and(PrimaNotaSpecifications.byStato(filter.getCodiceStato()));
        if (filter.getDescrizione() != null) spec = spec.and(PrimaNotaSpecifications.byDescrizione(filter.getDescrizione()));
        if (filter.getCodiceConto() != null) spec = spec.and(PrimaNotaSpecifications.byCodiceConto(filter.getCodiceConto()));
        if (filter.getDataRegProvvisoriaDa() != null) spec = spec.and(PrimaNotaSpecifications.byDataRegProvvisoriaDa(filter.getDataRegProvvisoriaDa()));
        if (filter.getDataRegProvvisoriaA() != null) spec = spec.and(PrimaNotaSpecifications.byDataRegProvvisoriaA(filter.getDataRegProvvisoriaA()));
        if (filter.getDataRegDefinitivaDa() != null) spec = spec.and(PrimaNotaSpecifications.byDataRegDefinitivaDa(filter.getDataRegDefinitivaDa()));
        if (filter.getDataRegDefinitivaA() != null) spec = spec.and(PrimaNotaSpecifications.byDataRegDefinitivaA(filter.getDataRegDefinitivaA()));
        if (filter.getImportoDa() != null) spec = spec.and(PrimaNotaSpecifications.byImportoDa(filter.getImportoDa()));
        if (filter.getImportoA() != null) spec = spec.and(PrimaNotaSpecifications.byImportoA(filter.getImportoA()));
        if (Boolean.TRUE.equals(filter.isSoloNonQuadrate())) spec = spec.and(PrimaNotaSpecifications.nonQuadrata());

        List<AccrualTPrimaNota> results = primaNotaRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "numero"));

        List<PrimaNotaSearchResultDTO> dtoList = results.stream()
                .map(entity -> {
                    PrimaNotaSearchResultDTO dto = primaNotaMapper.toSearchResultDto(entity);
                    return dto;
                })
                .collect(Collectors.toList());

        // Recupero batch dei totali
        List<Long> pnIds = dtoList.stream().map(PrimaNotaSearchResultDTO::getId).collect(Collectors.toList());
        if (!pnIds.isEmpty()) {
            Map<Long, it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota> totaliMap = primaNotaImportoRepository.findTotaliByPrimaNotaIds(pnIds).stream()
                .collect(Collectors.toMap(it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota::getPrimaNotaId, t -> t, (existing, replacement) -> existing));

            for (PrimaNotaSearchResultDTO dto : dtoList) {
                it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota totali = totaliMap.get(dto.getId());
                if (totali != null) {
                    dto.setTotaleDare(totali.getTotaleDare() != null ? totali.getTotaleDare().doubleValue() : 0.0);
                    dto.setTotaleAvere(totali.getTotaleAvere() != null ? totali.getTotaleAvere().doubleValue() : 0.0);
                } else {
                    dto.setTotaleDare(0.0);
                    dto.setTotaleAvere(0.0);
                }
            }
        }

        return dtoList;
    }


    @Override
    public boolean verificaEsistenzaNumeroProgressivoDefinitivo(Integer anno, Integer numero) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Verifica esistenza numero progressivo definitivo per prima nota {}/{} - ente: {}", anno, numero, enteCodice);

        // Recupera la prima nota
        AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(anno, numero, enteCodice)
            .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Verifica se il campo progressivoGiornale è valorizzato
        boolean esiste = primaNota.getProgressivoGiornale() != null;
        log.debug("Prima nota {}/{} - progressivoGiornale {}: {}", anno, numero, 
            esiste ? "= " + primaNota.getProgressivoGiornale() : "non valorizzato", 
            esiste);

        return esiste;
    }

    /**
     * Metodo sovraccaricato per retrocompatibilità.
     * Chiama il metodo completo con mantieniNumeroDefinitivo = null e confermato = null.
     */
    public Map<String, String> validaPrimaNota(Integer anno, Integer numero, String utenteCorrente)
        throws ConfirmationRequiredException {
        return validaPrimaNota(anno, numero, utenteCorrente, null, null);
    }

    @Override
    @Transactional
    public Map<String, String> validaPrimaNota(Integer anno, Integer numero, String utenteCorrente,
        Boolean mantieniNumeroDefinitivo, Boolean confermato)
        throws ConfirmationRequiredException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta validaPrimaNota con parametri - anno: {}, numero: {}, enteCodice: {}, utenteCorrente: {}, mantieniNumeroDefinitivo: {}, confermato: {}",
            anno, numero, enteCodice, utenteCorrente, mantieniNumeroDefinitivo, confermato);

        // Step 1: Recupera la prima nota usando query ottimizzata per consultazione
        AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(anno, numero, enteCodice)
            .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + anno + ", numero: " + numero + " for ente: " + enteCodice));

        // Step 2: Verifica che lo stato corrente sia PROVVISORIA
        AccrualDPrimaNotaStato statoCorrente = getCurrentStato(primaNota);
        if (statoCorrente == null || StatoPrimaNota.getByCode(statoCorrente.getCodice()) != StatoPrimaNota.PROVVISORIA) {
            throw new IllegalArgumentException("La prima nota non può essere validata: stato corrente non è PROVVISORIA");
        }

        // Step 3: Verifica entità contabile (fase non PREVISIONE o CHIUSO)
        if (!isValidabile(primaNota)) {
            throw new IllegalArgumentException("La prima nota non può essere validata: entità contabile in fase non consentita (PREVISIONE o CHIUSO)");
        }

        // Step 4: Verifica esistenza numero progressivo definitivo
        boolean haNumeroDefinitivo = verificaEsistenzaNumeroProgressivoDefinitivo(anno, numero);

        // Step 5: Verifica parametro VERIFICA_RINUMERAZIONE_PRIME_NOTE
        boolean verificaRinumerazione = isVerificaRinumerazioneAttiva(primaNota.getEnte());
        
        // La verifica esistenza numero progressivo definitivo:
        // - NON va a buon fine se: NO numero definitivo OPPURE parametro = NO
        // - VA a buon fine se: HA numero definitivo E parametro = SI
        if (haNumeroDefinitivo && verificaRinumerazione) {
            // Verifica VA a buon fine - richiedi conferma specifica (WEB_CON_033)
            if (mantieniNumeroDefinitivo == null) {
                // Prima chiamata senza scelta dell'utente - richiedi conferma
                log.warn("Prima nota {}/{} ha già numero definitivo {} e verifica rinumerazione è attiva. " +
                    "Richiesta conferma all'operatore tramite WEB_CON_033.",
                    anno, numero, primaNota.getProgressivoGiornale());

                throw new ConfirmationRequiredException(
                    "WEB_CON_033",
                    "La prima nota ha già un numero definitivo (" + primaNota.getProgressivoGiornale() + "). " +
                    "Si desidera mantenere il numero esistente? (Si = mantiene, No = assegna nuovo numero e data registrazione definitiva)"
                );
            }
        } else {
            // Verifica NON va a buon fine - richiedi conferma generica (WEB_CON_0016)
            // La validazione procede solo se l'utente ha confermato (confermato = true)
            if (confermato == null) {
                log.debug("Prima nota {}/{} verifica numero definitivo NON andata a buon fine. " +
                    "Richiesta conferma generica all'operatore tramite WEB_CON_0016.",
                    anno, numero);

                throw new ConfirmationRequiredException(
                    "WEB_CON_0016",
                    "Si desidera procedere con la validazione della prima nota?"
                );
            }
            // Se confermato == true, procedi con la validazione
            log.debug("Prima nota {}/{} conferma generica ricevuta (confermato=true). Procedo con la validazione.", anno, numero);
        }

        // Step 6: Gestisci numero definitivo
        boolean mantieneRegistrazioneDefinitiva = false;
        if (!haNumeroDefinitivo) {
            log.debug("Prima nota {}/{} senza numero definitivo: generazione nuovo numero", anno, numero);
            primaNota.setProgressivoGiornale(primaNotaNumService.nextNumeroDefinitivo(primaNota.getEnte(), anno));
        } else if (verificaRinumerazione && mantieniNumeroDefinitivo != null) {
            // L'utente ha già espresso la sua scelta
            if (mantieniNumeroDefinitivo) {
                log.debug("Prima nota {}/{} mantiene numero definitivo esistente: {}", anno, numero, primaNota.getProgressivoGiornale());
                // Mantiene il numero esistente (non fa nulla)
                mantieneRegistrazioneDefinitiva = true;
            } else {
                log.debug("Prima nota {}/{} richiede nuova numerazione. Riassegno numero definitivo.", anno, numero);
                primaNota.setProgressivoGiornale(primaNotaNumService.nextNumeroDefinitivo(primaNota.getEnte(), anno));
            }
        } else {
            // Verifica rinumerazione disattivata o parametro non attivo - mantiene numero esistente
            log.debug("Prima nota {}/{} mantiene numero definitivo esistente (verifica disattivata): {}", 
                anno, numero, primaNota.getProgressivoGiornale());
        }

        // Step 7: Imposta data registrazione definitiva
        // Numero e data della registrazione definitiva sono un dato solo: se l'operatore ha scelto di
        // mantenere il numero esistente (WEB_CON_033 = Si), resta anche la data che lo accompagnava.
        if (mantieneRegistrazioneDefinitiva) {
            log.debug("Prima nota {}/{} mantiene la data di registrazione definitiva esistente: {}",
                anno, numero, primaNota.getDataRegistrazioneGiornale());
        } else {
            primaNota.setDataRegistrazioneGiornale(calcolaDataRegistrazioneDefinitiva(primaNota.getData()));
        }

        // Step 7: Recupera stato "DEFINITIVA"
        AccrualDPrimaNotaStato statoDefinitiva = getStatoByCodice("D", "Stato DEFINITIVA non trovato nel database");

        // Step 8: Chiudi stato precedente
        chiudiStatiCorrenti(primaNota);

        // Step 9: Imposta nuovo stato 'DEFINITIVA'
        impostaNuovoStato(primaNota, statoDefinitiva);

        // Step 10: Aggiorna audit
        primaNota.setDataAggiornamento(LocalDateTime.now());
        primaNota.setUtenteAggiornamento(utenteCorrente);
        primaNotaRepository.save(primaNota);

        log.info("Prima nota {}/{} validata con successo - numero definitivo: {}",
            anno, numero, primaNota.getProgressivoGiornale());

        // Restituisci messaggio di successo
        Map<String, String> result = new HashMap<>();
        result.put("messaggio", "WEB_OK_0004");
        result.put("dettaglio", "Prima nota " + anno + "/" + numero + " validata con successo");
        return result;
    }

    /**
     * Metodo sovraccaricato per retrocompatibilità.
     * Chiama il metodo completo con mantieniNumeri = null e confermato = null.
     */
    public Map<String, String> validaPrimeNoteMassiva(List<PrimaNotaIdDTO> primaNoteIds, String utenteCorrente) {
        return validaPrimeNoteMassiva(primaNoteIds, utenteCorrente, null, null);
    }

    @Override
    @Transactional
    public Map<String, String> validaPrimeNoteMassiva(List<PrimaNotaIdDTO> primaNoteIds, String utenteCorrente,
        Boolean mantieniNumeri, Boolean confermato)
        		throws ConfirmationRequiredException {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta validaPrimeNoteMassiva con parametri - primaNoteIds: {}, enteCodice: {}, utenteCorrente: {}, mantieniNumeri: {}, confermato: {}",
            primaNoteIds, enteCodice, utenteCorrente, mantieniNumeri, confermato);

        if (primaNoteIds == null || primaNoteIds.isEmpty()) {
            throw new IllegalArgumentException("Elenco prime note da validare non può essere vuoto");
        }

        // Se l'utente non ha ancora confermato (confermato == null) e non ha ancora
        // espresso la scelta sulla rinumerazione (mantieniNumeri == null),
        // verifico se almeno una prima nota richiede conferma
        if (confermato == null && mantieniNumeri == null) {
            // Prima passata: verifico se almeno una nota richiede WEB_CON_034 (ha numero definitivo + verifica attiva)
            boolean almenoUnaConNumeroDefinitivo = false;
            for (PrimaNotaIdDTO idDTO : primaNoteIds) {
                AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(idDTO.getAnno(), idDTO.getNumero(), enteCodice)
                    .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + idDTO.getAnno() + ", numero: " + idDTO.getNumero() + " for ente: " + enteCodice));

                boolean haNumeroDefinitivo = primaNota.getProgressivoGiornale() != null;
                boolean verificaRinumerazione = isVerificaRinumerazioneAttiva(primaNota.getEnte());

                if (haNumeroDefinitivo && verificaRinumerazione) {
                    almenoUnaConNumeroDefinitivo = true;
                    break;
                }
            }

            if (almenoUnaConNumeroDefinitivo) {
                // WEB_CON_034: almeno una nota ha numero definitivo e verifica attiva
                log.warn("Validazione massiva: almeno una prima nota ha numero definitivo e verifica rinumerazione attiva. " +
                    "Richiesta conferma all'operatore tramite WEB_CON_034.");
                throw new ConfirmationRequiredException(
                    "WEB_CON_034",
                    "Alcune prime note hanno già un numero definitivo. Si desidera mantenere i numeri esistenti? (Si = mantiene, No = assegna nuovi numeri e data registrazione definitiva)"
                );
            }

            // Seconda passata: verifico se almeno una nota richiede WEB_CON_0017 (senza numero definitivo)
            for (PrimaNotaIdDTO idDTO : primaNoteIds) {
                AccrualTPrimaNota primaNota = primaNotaRepository.findForConsultazione(idDTO.getAnno(), idDTO.getNumero(), enteCodice)
                    .orElseThrow(() -> new NotFoundException("Prima Nota not found with anno: " + idDTO.getAnno() + ", numero: " + idDTO.getNumero() + " for ente: " + enteCodice));

                boolean haNumeroDefinitivo = primaNota.getProgressivoGiornale() != null;
                boolean verificaRinumerazione = isVerificaRinumerazioneAttiva(primaNota.getEnte());

                if (!haNumeroDefinitivo || !verificaRinumerazione) {
                    // WEB_CON_0017: almeno una nota senza numero definitivo
                    log.debug("Validazione massiva: almeno una prima nota richiede conferma generica. " +
                        "Richiesta conferma all'operatore tramite WEB_CON_0017.");
                    throw new ConfirmationRequiredException(
                        "WEB_CON_0017",
                        "Si desidera procedere con la validazione massiva delle prime note selezionate?"
                    );
                }
            }
        }

        // Fase di validazione effettiva (confermato == true oppure nessuna nota richiedeva conferma)
        // Se mantieniNumeri ha un valore (utente ha risposto a WEB_CON_034), 
        // implicitamente l'utente ha già confermato la validazione massiva
        Boolean confermatoEffettivo = (confermato == null && mantieniNumeri != null) ? true : confermato;

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        for (PrimaNotaIdDTO idDTO : primaNoteIds) {
            try {
                log.debug("Validazione prima nota {}/{} con confermatoEffettivo={}", idDTO.getAnno(), idDTO.getNumero(), confermatoEffettivo);
                validaPrimaNota(idDTO.getAnno(), idDTO.getNumero(), utenteCorrente, mantieniNumeri, confermatoEffettivo);
                successCount++;
            } catch (NotFoundException e) {
                log.error("Prima nota {}/{} non trovata durante validazione massiva", idDTO.getAnno(), idDTO.getNumero(), e);
                errors.add("Prima nota " + idDTO.getAnno() + "/" + idDTO.getNumero() + " non trovata");
                errorCount++;
            } catch (IllegalArgumentException e) {
                log.error("Prima nota {}/{} non può essere validata: {}", idDTO.getAnno(), idDTO.getNumero(), e.getMessage());
                errors.add("Prima nota " + idDTO.getAnno() + "/" + idDTO.getNumero() + ": " + e.getMessage());
                errorCount++;
            } catch (ConfirmationRequiredException e) {
                // Se arriva qui, rilancio l'eccezione (non dovrebbe succedere con la logica sopra)
                log.error("Conferma ancora richiesta per prima nota {}/{} durante validazione massiva", idDTO.getAnno(), idDTO.getNumero());
                throw e;
            } catch (Exception e) {
                log.error("Errore imprevisto durante validazione prima nota {}/{}", idDTO.getAnno(), idDTO.getNumero(), e);
                errors.add("Prima nota " + idDTO.getAnno() + "/" + idDTO.getNumero() + ": Errore imprevisto");
                errorCount++;
            }
        }

        log.info("Validazione massiva completata - Successi: {}, Errori: {}", successCount, errorCount);

        // Restituisci messaggio di successo con dettagli
        Map<String, String> result = new HashMap<>();
        if (errorCount == 0) {
            result.put("messaggio", "WEB_OK_0004");
            result.put("dettaglio", "Validate con successo " + successCount + " prime note su " + primaNoteIds.size());
        } else {
            result.put("messaggio", "WEB_OK_0004");
            result.put("dettaglio", "Validate " + successCount + " prime note su " + primaNoteIds.size() +
                ". Errori: " + errorCount);
            result.put("errors", String.join("; ", errors));
        }
        return result;
    }
}
