/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualDAutenticazioneTipo;
import it.csi.accrual.accrualsbsrv.dto.AutenticazioneTipo;

@Mapper(componentModel = "spring")
public interface AutenticazioneTipoMapper {

    AutenticazioneTipo toDto(AccrualDAutenticazioneTipo entity);

    AccrualDAutenticazioneTipo toEntity(AutenticazioneTipo dto);
}
