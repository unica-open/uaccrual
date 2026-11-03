/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcClasse;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDPdcTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRCausalePdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEvento;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcFamiglia;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;

public class PdcSpecifications {

    // Filtro fisso per ricercabile (come era prima) + esclusione record cancellati
    public static Specification<AccrualTPdc> isRicercabile() {
        return (root, query, cb) -> {
            Join<AccrualTPdc, AccrualTPdcFamiglia> famigliaJoin = root.join("pdcFamiglia", JoinType.INNER);
            return cb.and(
                cb.isTrue(famigliaJoin.get("ricercabile")),
                cb.isNull(root.get("dataCancellazione"))
            );
        };
    }

    // Filtro fisso per anno di validità (anno corrente) (come era prima) + esclusione record cancellati
    /**
     * @deprecated Usare {@link #isValidInYear(Integer)} per specificare l'anno di bilancio.
     * Questo metodo usa Year.now() come fallback per retrocompatibilità.
     */
    @Deprecated
    public static Specification<AccrualTPdc> isValidInCurrentYear() {
        return (root, query, cb) -> {
            // Leggi anno bilancio da BilancioContextHolder
            Integer annoBilancio = it.csi.accrual.accrualcommonsrv.be.util.BilancioContextHolder.getCurrentAnnoBilancio();
            if (annoBilancio == null) {
                throw new IllegalStateException("Anno di bilancio non disponibile nel contesto JWT. Verificare che il token contenga il claim 'anno_bilancio'.");
            }
            
            LocalDateTime startOfYear = LocalDateTime.of(annoBilancio, 1, 1, 0, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(annoBilancio, 12, 31, 23, 59, 59);

            return cb.and(
                cb.isNull(root.get("dataCancellazione")), // Esclude i record cancellati
                cb.lessThanOrEqualTo(root.get("inizioValidita"), endOfYear),
                cb.or(
                    cb.isNull(root.get("fineValidita")),
                    cb.greaterThanOrEqualTo(root.get("fineValidita"), startOfYear)
                )
            );
        };
    }

    /**
     * Filtro per anno di validità dinamico.
     * Filtra i conti la cui validità comprende l'anno specificato.
     * Algoritmo: inizioValidita <= 31/12/anno AND (fineValidita IS NULL OR fineValidita >= 01/01/anno)
     *
     * @param anno anno di bilancio (obbligatorio)
     * @return Specification per filtro validità
     * @throws IllegalArgumentException se anno è null
     */
    public static Specification<AccrualTPdc> isValidInYear(Integer anno) {
        if (anno == null) {
            throw new IllegalArgumentException("L'anno di bilancio è obbligatorio per il filtro di validità");
        }
        
        return (root, query, cb) -> {
            LocalDateTime startOfYear = LocalDateTime.of(anno, 1, 1, 0, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(anno, 12, 31, 23, 59, 59);

            return cb.and(
                cb.isNull(root.get("dataCancellazione")), // Esclude i record cancellati
                cb.lessThanOrEqualTo(root.get("inizioValidita"), endOfYear),
                cb.or(
                    cb.isNull(root.get("fineValidita")),
                    cb.greaterThanOrEqualTo(root.get("fineValidita"), startOfYear)
                )
            );
        };
    }

    // Filtro per ente
    public static Specification<AccrualTPdc> byEnteCodiceUnivoco(String codiceEnte) {
        return (root, query, cb) -> cb.equal(root.get("ente").get("codiceUnivoco"), codiceEnte);
    }

    // Filtro per famiglia
    public static Specification<AccrualTPdc> byFamiglia(String famiglia) {
        return (root, query, cb) -> {
            Join<AccrualTPdc, AccrualTPdcFamiglia> famigliaJoin = root.join("pdcFamiglia", JoinType.INNER);
            return cb.equal(famigliaJoin.get("codice"), famiglia);
        };
    }

    // Filtro per codiceEvento (con subquery, come era prima)
    public static Specification<AccrualTPdc> byCodiceEvento(String codiceEvento) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            jakarta.persistence.criteria.Root<AccrualRCausalePdc> rcpSubRoot = subquery.from(AccrualRCausalePdc.class);
            Join<AccrualRCausalePdc, AccrualTCausale> causaleSubJoin = rcpSubRoot.join("causale", JoinType.INNER);
            Join<AccrualTCausale, AccrualTEvento> eventoSubJoin = causaleSubJoin.join("evento", JoinType.INNER);

            subquery.select(rcpSubRoot.get("pdc").get("id"))
                    .where(cb.equal(eventoSubJoin.get("codice"), codiceEvento));

            return cb.in(root.get("id")).value(subquery);
        };
    }

    // Filtro per pdcClasseCodice
    public static Specification<AccrualTPdc> byPdcClasseCodice(String pdcClasseCodice) {
        return (root, query, cb) -> {
            Join<AccrualTPdc, AccrualDPdcClasse> classeJoin = root.join("pdcClasse", JoinType.INNER);
            return cb.equal(classeJoin.get("codice"), pdcClasseCodice);
        };
    }

    // Filtro per codiceUfficiale (ricerca "inizia con")
    public static Specification<AccrualTPdc> byCodiceUfficiale(String codiceUfficiale) {
        return (root, query, cb) -> cb.like(root.get("codiceUfficiale"), codiceUfficiale + "%");
    }

    // Filtro per pdcStatoCodice
    public static Specification<AccrualTPdc> byPdcStatoCodice(String pdcStatoCodice) {
        return (root, query, cb) -> {
            Join<AccrualTPdc, AccrualDPdcStato> statoJoin = root.join("pdcStato", JoinType.INNER);
            return cb.equal(statoJoin.get("codice"), pdcStatoCodice);
        };
    }

    // Filtro per descrizione (case-insensitive contains)
    public static Specification<AccrualTPdc> byDescrizione(String descrizione) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("descrizione")), "%" + descrizione.toLowerCase() + "%");
    }

    // Filtro per livello
    public static Specification<AccrualTPdc> byLivello(Integer livello) {
        return (root, query, cb) -> cb.equal(root.get("livello"), livello);
    }

    // Filtro per modificabile
    public static Specification<AccrualTPdc> byModificabile(Boolean modificabile) {
        return (root, query, cb) -> cb.equal(root.get("modificabile"), modificabile);
    }

    // Filtro per enteDenominazione
    public static Specification<AccrualTPdc> byEnteDenominazione(String enteDenominazione) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("ente").get("denominazione")), "%" + enteDenominazione.toLowerCase() + "%");
    }

    // Filtro per pdcTipoCodice
    public static Specification<AccrualTPdc> byPdcTipoCodice(String pdcTipoCodice) {
        return (root, query, cb) -> {
            Join<AccrualTPdc, AccrualDPdcTipo> tipoJoin = root.join("pdcTipo", JoinType.INNER);
            return cb.equal(tipoJoin.get("codice"), pdcTipoCodice);
        };
    }

    public static Specification<AccrualTPdc> byCodice(String codice) {
        return (root, query, builder) -> {
            if (!org.springframework.util.StringUtils.hasText(codice)) {
                return builder.conjunction(); // Nessun filtro se il codice è vuoto
            }
            String lowerCaseCodice = codice.toLowerCase();
            if (lowerCaseCodice.contains("%") || lowerCaseCodice.contains("_")) {
                // Se contiene wildcard, usa LIKE con la stringa fornita
                return builder.like(builder.lower(root.get("codice")), lowerCaseCodice);
            } else {
                // Altrimenti, usa EQUAL per una corrispondenza esatta
                return builder.equal(builder.lower(root.get("codice")), lowerCaseCodice);
            }
        };
    }

    /**
     * Filtro per collegamento_bil_itas (codice di bilancio ITAS).
     * Ricerca case-insensitive con LIKE.
     */
    public static Specification<AccrualTPdc> byCollegamentoBilItas(String collegamentoBilItas) {
        return (root, query, cb) -> {
            if (!org.springframework.util.StringUtils.hasText(collegamentoBilItas)) {
                return cb.conjunction(); // Nessun filtro se il campo è vuoto
            }
            return cb.like(cb.lower(root.get("collegamentoBilItas")), "%" + collegamentoBilItas.toLowerCase() + "%");
        };
    }
}