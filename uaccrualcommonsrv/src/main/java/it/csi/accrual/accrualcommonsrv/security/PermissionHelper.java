/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRRuoloAzione;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualRRuoloAzioneRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PermissionHelper {

    private static final Logger log = LoggerFactory.getLogger(PermissionHelper.class);

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Autowired
    private AccrualTEnteRepository enteRepository;

    @Autowired
    private AccrualRRuoloAzioneRepository ruoloAzioneRepository;

    @Transactional(readOnly = true)
    public Set<String> getUserAzioniCodici() {
        try {
            String ruolo = securityContextHelper.getRuolo();
            String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
            if (ruolo == null || enteCodice == null) {
                return new HashSet<>();
            }
            Optional<AccrualTEnte> enteOpt = enteRepository.findByCodiceUnivoco(enteCodice);
            if (!enteOpt.isPresent()) {
                return new HashSet<>();
            }
            List<AccrualRRuoloAzione> ruoloAzioni = ruoloAzioneRepository
                    .findAllAzioniByRuoloCodiceAndEnte(ruolo, enteOpt.get());
            Set<String> codici = new HashSet<>();
            for (AccrualRRuoloAzione ra : ruoloAzioni) {
                if (ra.getAzione() != null && ra.getAzione().getCodice() != null) {
                    codici.add(ra.getAzione().getCodice());
                }
            }
            return codici;
        } catch (Exception e) {
            log.warn("Impossibile caricare le azioni utente: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    public boolean hasAzione(String codiceAzione) {
        return getUserAzioniCodici().contains(codiceAzione);
    }
}
