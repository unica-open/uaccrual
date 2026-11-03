/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDAttributo;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDAttributoRepository;
import it.csi.accrual.accrualsbsrv.dto.AttributoInfoDTO;
import it.csi.accrual.accrualsbsrv.be.service.AttributoService;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio per la gestione degli attributi
 */
@Service
@Transactional
public class AttributoServiceImpl implements AttributoService {

    private static final Logger log = LoggerFactory.getLogger(AttributoServiceImpl.class);

    private final AccrualDAttributoRepository attributoRepository;

    public AttributoServiceImpl(AccrualDAttributoRepository attributoRepository) {
        this.attributoRepository = attributoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributoInfoDTO> getAttributiByEnte() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Richiesta getAttributiByEnte per ente: {}", enteCodice);
        List<AccrualDAttributo> attributi = attributoRepository.findByEnteCodiceUnivocoAndDataCancellazioneIsNull(enteCodice);

        return attributi.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Converte entity in DTO
     */
    private AttributoInfoDTO toDTO(AccrualDAttributo entity) {
        AttributoInfoDTO dto = new AttributoInfoDTO();
        dto.setCodice(entity.getCodice());
        dto.setDescrizione(entity.getDescrizione());
        return dto;
    }
}
