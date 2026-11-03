/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDSoggettoStato;
import it.csi.accrual.accrualsbsrv.dto.SoggettoStato;

@Mapper(componentModel = "spring")
public interface SoggettoStatoMapper {

    SoggettoStato toDto(AccrualDSoggettoStato entity);

    AccrualDSoggettoStato toEntity(SoggettoStato dto);
}
