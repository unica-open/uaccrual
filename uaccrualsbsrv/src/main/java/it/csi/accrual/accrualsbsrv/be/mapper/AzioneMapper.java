/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTAzione;
import it.csi.accrual.accrualsbsrv.dto.Azione;

@Mapper(componentModel = "spring", uses = { GruppoAzioneMapper.class })
public interface AzioneMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    @Mapping(source = "ordine", target = "ordine")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "gruppoAzione", target = "gruppoAzione")
    Azione toDto(AccrualTAzione entity);
}
