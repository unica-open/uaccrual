/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualsbsrv.dto.PdcTipo;

@Mapper(componentModel = "spring")
public interface PdcTipoMapper {

    PdcTipo toDto(AccrualDPdcTipo entity);

    AccrualDPdcTipo toEntity(PdcTipo dto);
}