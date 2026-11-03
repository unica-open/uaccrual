/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualsbsrv.be.integration.entity.AccrualREventoFonte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEventoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualREventoFonteRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.EventoMapper;
import it.csi.accrual.accrualsbsrv.be.service.EventoService;
import it.csi.accrual.accrualsbsrv.dto.Evento;

@Service
@Transactional
public class EventoServiceImpl implements EventoService {

    @Autowired
    private AccrualTEventoRepository eventoRepository;

    @Autowired
    private AccrualREventoFonteRepository eventoFonteRepository;

    @Autowired
    private EventoMapper eventoMapper;

    @Override
    public List<Evento> getEventi() {
        List<AccrualTEvento> entities = eventoRepository.findAllByOrderByCodiceAsc();
        return entities.stream()
                .map(eventoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Evento> getEventiByEnte(String enteCodice) {
        List<AccrualTEvento> entities = eventoRepository.findAllByEnte_CodiceUnivocoOrderByCodiceAsc(enteCodice);
        return entities.stream()
                .map(eventoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Evento> getEventiByFonteAndEnte(String codiceFonte, String enteCodice) {
        List<AccrualREventoFonte> eventoFonteEntities = eventoFonteRepository.findByFonteCodiceAndEnteCodiceUnivoco(codiceFonte, enteCodice);
        return eventoFonteEntities.stream()
                .map(erf -> eventoMapper.toDto(erf.getEvento()))
                .collect(Collectors.toList());
    }
}