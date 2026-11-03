/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoTipo;
import it.csi.accrual.accrualsbsrv.dto.TipoSoggetto;

@Mapper(componentModel = "spring")
public interface TipoSoggettoMapper {

    TipoSoggetto toDto(AccrualDSoggettoTipo entity);

    AccrualDSoggettoTipo toEntity(TipoSoggetto dto);
}
