/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualsbsrv.dto.PdcClasse;

@Mapper(componentModel = "spring")
public interface PdcClasseMapper {

    PdcClasse toDto(AccrualDPdcClasse entity);

    AccrualDPdcClasse toEntity(PdcClasse dto);
}