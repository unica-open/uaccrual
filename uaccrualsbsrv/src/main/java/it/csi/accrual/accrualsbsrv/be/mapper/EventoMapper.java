/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualsbsrv.dto.Evento;

@Mapper(componentModel = "spring")
public interface EventoMapper {
    
    EventoMapper INSTANCE = Mappers.getMapper(EventoMapper.class);
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    @Mapping(source = "primaNotaStato.codice", target = "statoDefaultPrimaNota")
    Evento toDto(AccrualTEvento entity);
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    AccrualTEvento toEntity(Evento dto);
}