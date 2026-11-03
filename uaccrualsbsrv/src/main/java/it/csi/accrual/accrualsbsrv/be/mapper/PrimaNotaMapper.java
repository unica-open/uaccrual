/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;


import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNotaImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaAttributo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaLegame;

import it.csi.accrual.accrualsbsrv.dto.PrimaNotaDetailDTO;
import it.csi.accrual.accrualsbsrv.dto.PrimaNotaSearchResultDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import jakarta.persistence.EntityNotFoundException;

@Mapper(
    componentModel = "spring",
    uses = { CausaleMapper.class }, // Use CausaleMapper
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PrimaNotaMapper {

    PrimaNotaMapper INSTANCE = Mappers.getMapper(PrimaNotaMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "tipo.codice", target = "tipo")
    @Mapping(source = "fonte.codice", target = "fonte")
    @Mapping(source = "causale.evento.descrizione", target = "evento")
    @Mapping(source = "causale.descrizione", target = "causale")
    @Mapping(source = "numero", target = "numeroProvvisorio")
    @Mapping(source = "data", target = "dataRegProvvisoria")
    @Mapping(source = "stati", target = "stato", qualifiedByName = "getCurrentStato")
    @Mapping(source = "progressivoGiornale", target = "numeroDefinitivo")
    @Mapping(source = "dataRegistrazioneGiornale", target = "dataRegDefinitiva")
    @Mapping(source = "descrizione", target = "descrizione")
    @Mapping(source = "anno", target = "anno")
    // TODO: implement logic for isAggiornabile, isAnnullabile, isRiapribile, isValidabile
    @Mapping(target = "isAggiornabile", ignore = true)
    @Mapping(target = "isAnnullabile", ignore = true)
    @Mapping(target = "isRiapribile", ignore = true)
    @Mapping(target = "isValidabile", ignore = true)
    PrimaNotaSearchResultDTO toSearchResultDto(AccrualTPrimaNota entity);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "tipo.codice", target = "tipo")
    @Mapping(source = "tipo.descrizione", target = "tipoDescrizione")
    @Mapping(source = "fonte.codice", target = "fonte")
    @Mapping(source = "fonte.descrizione", target = "fonteDescrizione")
    @Mapping(source = "causale.evento.codice", target = "evento")
    @Mapping(source = "causale.evento.descrizione", target = "eventoDescrizione")
    @Mapping(source = "causale.codice", target = "causale")
    @Mapping(source = "causale.descrizione", target = "causaleDescrizione")
    @Mapping(source = "numero", target = "numeroProvvisorio")
    @Mapping(source = "data", target = "dataRegProvvisoria")
    @Mapping(source = "stati", target = "stato", qualifiedByName = "getCurrentStatoCodice")
    @Mapping(source = "stati", target = "statoDescrizione", qualifiedByName = "getCurrentStatoDescrizione")
    @Mapping(source = "progressivoGiornale", target = "numeroDefinitivo")
    @Mapping(source = "dataRegistrazioneGiornale", target = "dataRegDefinitiva")
    @Mapping(source = "descrizione", target = "descrizione")
    @Mapping(source = "importi", target = "conti", qualifiedByName = "mapImportiToConti")
    @Mapping(source = "attributi", target = "attributi", qualifiedByName = "mapAttributi")
    @Mapping(target = "primeNoteCollegate", ignore = true)
    PrimaNotaDetailDTO toDetailDto(AccrualTPrimaNota entity);
    
    // Reverse mapping for creating/updating
    @Mapping(source = "id", target = "id")
    @Mapping(target = "tipo", ignore = true) // Will be set by service logic
    @Mapping(target = "fonte", ignore = true) // Will be set by service logic
    @Mapping(target = "causale", ignore = true) // Will be set by service logic
    @Mapping(target = "stati", ignore = true) // Will be set by service logic
    @Mapping(target = "ente", ignore = true) // Will be set by service logic
    AccrualTPrimaNota toEntity(PrimaNotaDetailDTO dto);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "fonte", ignore = true)
    @Mapping(target = "causale", ignore = true)
    @Mapping(target = "stati", ignore = true)
    @Mapping(target = "ente", ignore = true)
    void toEntity(PrimaNotaDetailDTO dto, @MappingTarget AccrualTPrimaNota entity);

    @Named("getCurrentStatoCodice")
    default String getCurrentStatoCodice(Set<AccrualRPrimaNotaStato> stati) {
        if (stati == null || stati.isEmpty()) {
            return null;
        }
        return stati.stream()
                .filter(s -> s.getFineValidita() == null)
                .max(Comparator.comparing(AccrualRPrimaNotaStato::getInizioValidita))
                .map(s -> s.getPrimaNotaStato().getCodice())
                .orElse(null);
    }

    @Named("getCurrentStatoDescrizione")
    default String getCurrentStatoDescrizione(Set<AccrualRPrimaNotaStato> stati) {
        if (stati == null || stati.isEmpty()) {
            return null;
        }
        return stati.stream()
                .filter(s -> s.getFineValidita() == null)
                .max(Comparator.comparing(AccrualRPrimaNotaStato::getInizioValidita))
                .map(s -> s.getPrimaNotaStato().getDescrizione())
                .orElse(null);
    }

    @Named("getCurrentStato")
    default String getCurrentStato(Set<AccrualRPrimaNotaStato> stati) {
        // Metodo mantenuto per retrocompatibilità con PrimaNotaSearchResultDTO
        return getCurrentStatoDescrizione(stati);
    }

    @Named("mapStringToLong")
    default Long mapStringToLong(String value) {
        return value != null ? Long.parseLong(value) : null;
    }

    @Named("mapImportiToConti")
    default List<it.csi.accrual.accrualsbsrv.dto.ContoCollegatoDTO> mapImportiToConti(Set<AccrualTPrimaNotaImporto> importi) {
        if (importi == null || importi.isEmpty()) {
            return new ArrayList<>();
        }

        return importi.stream()
            .filter(importo -> importo.getDataCancellazione() == null) // Filtra importi cancellati
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
    }

    @Named("mapAttributi")
    default List<it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO> mapAttributi(Set<AccrualRPrimaNotaAttributo> attributi) {
        if (attributi == null || attributi.isEmpty()) {
            return new ArrayList<>();
        }

        return attributi.stream()
            .map(attr -> {
                it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO attrDto = new it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO();
                attrDto.setTipoAttributo(attr.getAttributo().getCodice());
                attrDto.setTipoAttributoDescrizione(attr.getAttributo().getDescrizione());
                attrDto.setValore(attr.getValore());
                
                // Gestisce il caso in cui primaNotaImporto è stato cancellato (EntityNotFoundException)
                try {
                    if (attr.getPrimaNotaImporto() != null && attr.getPrimaNotaImporto().getPdc() != null) {
                        attrDto.setConto(attr.getPrimaNotaImporto().getPdc().getCodiceUfficiale());
                    } else {
                        attrDto.setConto(null);
                    }
                } catch (EntityNotFoundException e) {
                    // L'importo associato è stato cancellato, imposta conto a null
                    attrDto.setConto(null);
                }
                
                return attrDto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Metodo per mappare una lista di attributi (usato da query ottimizzate)
     * @param attributi lista di AccrualRPrimaNotaAttributo
     * @return lista di AttributoCollegatoDTO
     */
    default List<it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO> mapAttributiList(List<AccrualRPrimaNotaAttributo> attributi) {
        if (attributi == null || attributi.isEmpty()) {
            return new ArrayList<>();
        }

        return attributi.stream()
            .map(attr -> {
                it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO attrDto = new it.csi.accrual.accrualsbsrv.dto.AttributoCollegatoDTO();
                attrDto.setTipoAttributo(attr.getAttributo().getCodice());
                attrDto.setTipoAttributoDescrizione(attr.getAttributo().getDescrizione());
                attrDto.setValore(attr.getValore());
                
                // Gestisce il caso in cui primaNotaImporto è stato cancellato (EntityNotFoundException)
                try {
                    if (attr.getPrimaNotaImporto() != null && attr.getPrimaNotaImporto().getPdc() != null) {
                        attrDto.setConto(attr.getPrimaNotaImporto().getPdc().getCodiceUfficiale());
                    } else {
                        attrDto.setConto(null);
                    }
                } catch (EntityNotFoundException e) {
                    // L'importo associato è stato cancellato, imposta conto a null
                    attrDto.setConto(null);
                }
                
                return attrDto;
            })
            .collect(Collectors.toList());
    }

    @Named("mapPrimeNoteCollegate")
    default List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO> mapPrimeNoteCollegate(AccrualTPrimaNota entity) {
        if (entity == null) {
            return new ArrayList<>();
        }
        
        Long idPrimaNotaCorrente = entity.getId();
        List<it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO> risultato = new ArrayList<>();
        
        // Processa legami in cui la prima nota corrente è la destinazione (primaNotaA)
        if (entity.getLegamiComeDestinazione() != null) {
            risultato.addAll(entity.getLegamiComeDestinazione().stream()
                .map(legame -> creaDtoDaLegame(legame, idPrimaNotaCorrente))
                .collect(Collectors.toList()));
        }
        
        // Processa legami in cui la prima nota corrente è l'origine (primaNotaDa)
        if (entity.getLegamiComeOrigine() != null) {
            risultato.addAll(entity.getLegamiComeOrigine().stream()
                .map(legame -> creaDtoDaLegame(legame, idPrimaNotaCorrente))
                .collect(Collectors.toList()));
        }
        
        return risultato;
    }

    default it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO creaDtoDaLegame(AccrualRPrimaNotaLegame legame, Long idPrimaNotaCorrente) {
        it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO legameDto = new it.csi.accrual.accrualsbsrv.dto.PrimaNotaCollegataDTO();

        // Determina quale delle due prime note è quella collegata (diversa da quella corrente)
        AccrualTPrimaNota notaCollegata = null;
        if (legame.getPrimaNotaDa() != null && !legame.getPrimaNotaDa().getId().equals(idPrimaNotaCorrente)) {
            notaCollegata = legame.getPrimaNotaDa();
        } else if (legame.getPrimaNotaA() != null && !legame.getPrimaNotaA().getId().equals(idPrimaNotaCorrente)) {
            notaCollegata = legame.getPrimaNotaA();
        }

        if (notaCollegata != null) {
            legameDto.setIdPrimaNotaCollegata(notaCollegata.getId());

            // Imposta il tipo
            if (notaCollegata.getTipo() != null) {
                legameDto.setTipo(notaCollegata.getTipo().getCodice());
            }

            // Imposta la fonte
            if (notaCollegata.getFonte() != null) {
                legameDto.setFonte(notaCollegata.getFonte().getCodice());
            }

            // Imposta l'evento
            if (notaCollegata.getCausale() != null && notaCollegata.getCausale().getEvento() != null) {
                legameDto.setEvento(notaCollegata.getCausale().getEvento().getCodice());
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

            // Imposta le note del legame
            if (legame.getNote() != null) {
                legameDto.setNoteLegame(legame.getNote());
            }

            // Imposta la data dell'operazione (potrebbe essere la data della prima nota collegata)
            if (notaCollegata.getData() != null) {
                legameDto.setDataOperazione(java.sql.Timestamp.valueOf(notaCollegata.getData()));
            }

            // Imposta un importo fittizio poiché non abbiamo un importo specifico per il legame
            legameDto.setImporto(0.0);
            legameDto.setValuta("EUR"); // Valuta di default
        }

        return legameDto;
    }
}
