/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDCausaleStato;
import it.csi.accrual.accrualsbsrv.dto.CausaleStato;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CausaleStatoMapper {

    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    CausaleStato toDto(AccrualDCausaleStato entity);

    @Mapping(source = "codice", target = "codice")
    @Mapping(source = "descrizione", target = "descrizione")
    AccrualDCausaleStato toEntity(CausaleStato dto);
}
