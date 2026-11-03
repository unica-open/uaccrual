/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualsbsrv.dto.Ente;

@Mapper(componentModel = "spring")
public interface EnteMapper {

    Ente toDto(AccrualTEnte entity);

    AccrualTEnte toEntity(Ente dto);
}
