/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualsbsrv.dto.Pdc;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PdcMapper {
    
    PdcMapper INSTANCE = Mappers.getMapper(PdcMapper.class);
    
    @Mapping(source = "ente.id", target = "ente.id")
    @Mapping(source = "pdcFamiglia.id", target = "pdcFamiglia.id")
    @Mapping(source = "pdcClasse.id", target = "pdcClasse.id")
    @Mapping(source = "pdcTipo.id", target = "pdcTipo.id")
    @Mapping(source = "pdcStato.id", target = "pdcStato.id")
    @Mapping(source = "pdcPadre.id", target = "pdcPadre.id")
    @Mapping(source = "version", target = "version")
    Pdc toDto(AccrualTPdc entity);

    @Mapping(source = "ente.id", target = "ente.id")
    @Mapping(source = "pdcFamiglia.id", target = "pdcFamiglia.id")
    @Mapping(source = "pdcClasse.id", target = "pdcClasse.id")
    @Mapping(source = "pdcTipo.id", target = "pdcTipo.id")
    @Mapping(source = "pdcStato.id", target = "pdcStato.id")
    @Mapping(source = "pdcPadre.id", target = "pdcPadre.id")
    AccrualTPdc toEntity(Pdc dto);

    @Mapping(source = "ente.id", target = "ente.id")
    @Mapping(source = "pdcFamiglia.id", target = "pdcFamiglia.id")
    @Mapping(source = "pdcClasse.id", target = "pdcClasse.id")
    @Mapping(source = "pdcTipo.id", target = "pdcTipo.id")
    @Mapping(source = "pdcStato.id", target = "pdcStato.id")
    @Mapping(source = "pdcPadre.id", target = "pdcPadre.id")
    void toEntity(Pdc dto, @MappingTarget AccrualTPdc entity);
}