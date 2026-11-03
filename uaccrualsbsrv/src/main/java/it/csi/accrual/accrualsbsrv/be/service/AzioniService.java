/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTGruppoAzione;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRRuoloAzione;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRRuoloAzioneRepository;
import it.csi.accrual.accrualsbsrv.be.mapper.AzioneMapper;
import it.csi.accrual.accrualsbsrv.be.mapper.GruppoAzioneMapper;
import it.csi.accrual.accrualsbsrv.dto.Azione;
import it.csi.accrual.accrualsbsrv.dto.GruppoAzione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AzioniService {

    private static final Logger log = LoggerFactory.getLogger(AzioniService.class);
    private final AzioneMapper azioneMapper;
    private final GruppoAzioneMapper gruppoAzioneMapper;
    private final SecurityContextHelper securityContextHelper;
    private final AccrualRRuoloAzioneRepository ruoloAzioneRepository;
    private final AccrualTEnteRepository enteRepository;

    public AzioniService(AzioneMapper azioneMapper,
                         GruppoAzioneMapper gruppoAzioneMapper,
                         SecurityContextHelper securityContextHelper,
                         AccrualRRuoloAzioneRepository ruoloAzioneRepository,
                         AccrualTEnteRepository enteRepository) {
        this.azioneMapper = azioneMapper;
        this.gruppoAzioneMapper = gruppoAzioneMapper;
        this.securityContextHelper = securityContextHelper;
        this.ruoloAzioneRepository = ruoloAzioneRepository;
        this.enteRepository = enteRepository;
    }

    @Transactional(readOnly = true)
    public List<Azione> getAzioni(String jwtToken) {
        String ruolo = securityContextHelper.getRuolo();
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();

        AccrualTEnte ente = enteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new RuntimeException("Ente non trovato: " + enteCodice));

        List<AccrualRRuoloAzione> ruoliAzioni = ruoloAzioneRepository.findAllAzioniByRuoloCodiceAndEnte(ruolo, ente);

        return ruoliAzioni.stream()
                .map(r -> azioneMapper.toDto(r.getAzione()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GruppoAzione> getAzioniGrouped(String jwtToken) {
        String ruolo = securityContextHelper.getRuolo();
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();

        AccrualTEnte ente = enteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new RuntimeException("Ente non trovato: " + enteCodice));

        List<AccrualRRuoloAzione> ruoliAzioni = ruoloAzioneRepository
                .findByRuoloCodiceAndEnteOrderByAzioneOrdineAsc(ruolo, ente);

        Map<AccrualTGruppoAzione, List<AccrualRRuoloAzione>> grouped = ruoliAzioni.stream()
                .collect(Collectors.groupingBy(r -> r.getAzione().getGruppoAzione() != null ? r.getAzione().getGruppoAzione() : new AccrualTGruppoAzione()));

        List<GruppoAzione> result = grouped.entrySet().stream()
                .map(entry -> {
                    GruppoAzione gruppo = gruppoAzioneMapper.toDto(entry.getKey());
                    gruppo.setAzioni(entry.getValue().stream()
                            .map(r -> azioneMapper.toDto(r.getAzione()))
                            .collect(Collectors.toList()));
                    return gruppo;
                })
                .collect(Collectors.toList());
        return result;
    }
}
