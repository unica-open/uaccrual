/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualsbsrv.dto.Causale;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CausaleMapper {

    @Mapping(source = "causaleTipo.id", target = "tipo.id")
    @Mapping(source = "evento.id", target = "evento.id")
    Causale toDto(AccrualTCausale entity);

    @Mapping(source = "tipo.id", target = "causaleTipo.id")
    @Mapping(source = "evento.id", target = "evento.id")
    AccrualTCausale toEntity(Causale dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "tipo.id", target = "causaleTipo.id")
    @Mapping(source = "evento.id", target = "evento.id")
    AccrualTCausale updateEntityFromDto(Causale dto, @MappingTarget AccrualTCausale entity);
}
