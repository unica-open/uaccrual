/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;

import org.mapstruct.Mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTGruppoAzione;
import it.csi.accrual.accrualsbsrv.dto.GruppoAzione;

@Mapper(componentModel = "spring")
public interface GruppoAzioneMapper {

    GruppoAzione toDto(AccrualTGruppoAzione entity);

    AccrualTGruppoAzione toEntity(GruppoAzione dto);
}
