/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleTipo;
import it.csi.accrual.accrualsbsrv.dto.CausaleTipo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CausaleTipoMapper {

    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    CausaleTipo toDto(AccrualDCausaleTipo entity);

    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    AccrualDCausaleTipo toEntity(CausaleTipo dto);
}
