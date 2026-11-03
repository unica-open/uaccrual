/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualTSoggetto;
import it.csi.accrual.accrualsbsrv.dto.Soggetto;

@Mapper(componentModel = "spring", uses = {TipoSoggettoMapper.class, SoggettoStatoMapper.class, EnteMapper.class})
public interface SoggettoMapper {

    SoggettoMapper INSTANCE = Mappers.getMapper(SoggettoMapper.class);

    @Mapping(source = "stato", target = "stato")
    @Mapping(source = "tipo", target = "tipo")
    @Mapping(source = "ente", target = "ente")
    Soggetto toDto(AccrualTSoggetto soggetto);

    @Mapping(source = "stato", target = "stato")
    @Mapping(source = "tipo", target = "tipo")
    @Mapping(source = "ente", target = "ente")
    AccrualTSoggetto toEntity(Soggetto soggettoDto);
}
