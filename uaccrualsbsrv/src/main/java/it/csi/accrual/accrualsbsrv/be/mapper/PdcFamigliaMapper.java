/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import it.csi.accrual.accrualsbsrv.dto.PdcFamiglia;

@Mapper(componentModel = "spring")
public interface PdcFamigliaMapper {

    @Mapping(source = "modificabile", target = "modificabile")
    @Mapping(source = "ricercabile", target = "ricercabile")
    PdcFamiglia toDto(AccrualTPdcFamiglia entity);

    @Mapping(source = "modificabile", target = "modificabile")
    @Mapping(source = "ricercabile", target = "ricercabile")
    AccrualTPdcFamiglia toEntity(PdcFamiglia dto);
}
