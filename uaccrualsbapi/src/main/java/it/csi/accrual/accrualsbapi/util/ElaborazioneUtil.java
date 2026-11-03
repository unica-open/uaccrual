/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.util;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFonteRepository;
import it.csi.accrual.accrualcommonsrv.be.util.AccrualBusinessConstants;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbapi.be.service.impl.PrimaNotaServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class ElaborazioneUtil {

    private ElaborazioneUtil() {}

    public static void validaContestoElaborazione(
            String codiceFiscaleEnte,
            String codiceUnivocoEnte,
            String fonte,
            Integer anno,
            AccrualTEnte ente,
            AccrualDFonteRepository fonteRepository) {

        if (!ente.getCodiceUnivoco().equals(codiceFiscaleEnte) ||
            !ente.getCodiceUnivoco().equals(codiceUnivocoEnte)) {
            throw new IllegalArgumentException(
                "Codice fiscale Ente, Codice univoco Ente non corrispondenti all'Ente con il quale si sta operando");
        }

        if (fonteRepository.findByCodiceAndEnte_CodiceUnivoco(fonte, ente.getCodiceUnivoco()).isEmpty()) {
            throw new IllegalArgumentException(
                "Fonte non presente nell'Ente con il quale si sta operando");
        }

        Integer annoBilancio = SecurityContextHelper.getAnnoBilancioStatic();
        if (annoBilancio == null || !annoBilancio.equals(anno)) {
            throw new IllegalArgumentException(
                "Anno non corrispondente all'anno con il quale si sta operando");
        }
    }

    public static String getUtenteCorrente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }

    public static List<AccrualTPrimaNota> filtraPrimeNoteNonAnnullate(List<AccrualTPrimaNota> pns) {
        return pns.stream()
            .filter(pn -> pn.getStati().stream()
                .filter(s -> s.getFineValidita() == null)
                .noneMatch(s -> AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO.equals(s.getPrimaNotaStato().getCodice())))
            .collect(Collectors.toList());
    }

    public static void annullaPrimeNote(
            List<AccrualTPrimaNota> pnsDaAnnullare,
            String utente,
            AccrualTEnte ente,
            PrimaNotaServiceImpl primaNotaService) {
        AccrualDPrimaNotaStato statoAnnullato = primaNotaService.getStatoPrimaNotaByCodice(AccrualBusinessConstants.STATO_PRIMA_NOTA_ANNULLATO);
        pnsDaAnnullare.forEach(pn -> primaNotaService.storicizzaEImpostaNuovoStato(pn, statoAnnullato, utente, ente));
    }
}
