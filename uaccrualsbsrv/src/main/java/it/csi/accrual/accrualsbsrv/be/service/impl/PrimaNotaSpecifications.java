/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;


import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class PrimaNotaSpecifications {

    private static final Logger log = LoggerFactory.getLogger(PrimaNotaSpecifications.class);

    /**
     * Filtro fisso per anno di bilancio dal token JWT.
     * Analogo a quanto già fatto per PDC in PdcSpecifications.isValidInYear().
     */
    public static Specification<AccrualTPrimaNota> byAnnoBilancio() {
        return (root, query, cb) -> {
            Integer annoBilancio = SecurityContextHelper.getAnnoBilancioStatic();
            if (annoBilancio == null) {
                throw new IllegalStateException("Anno di bilancio non disponibile nel contesto JWT. Verificare che il token contenga il claim 'anno_bilancio'.");
            }
            log.debug("Filtro ricerca prime note per anno di bilancio: {}", annoBilancio);
            return cb.equal(root.get("anno"), annoBilancio);
        };
    }

    public static Specification<AccrualTPrimaNota> byEnteCodiceUnivoco() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        return (root, query, cb) -> {
            Join<AccrualTPrimaNota, AccrualTEnte> enteJoin = root.join("ente", JoinType.INNER);
            return cb.equal(enteJoin.get("codiceUnivoco"), enteCodice);
        };
    }

    public static Specification<AccrualTPrimaNota> byTipo(String codiceTipo) {
        return (root, query, cb) -> {
            Join<AccrualTPrimaNota, AccrualDPrimaNotaTipo> tipoJoin = root.join("tipo", JoinType.INNER);
            return cb.equal(tipoJoin.get("codice"), codiceTipo);
        };
    }

    public static Specification<AccrualTPrimaNota> byFonte(String codiceFonte) {
        return (root, query, cb) -> {
            Join<AccrualTPrimaNota, AccrualDFonte> fonteJoin = root.join("fonte", JoinType.INNER);
            return cb.equal(fonteJoin.get("codice"), codiceFonte);
        };
    }

    public static Specification<AccrualTPrimaNota> byEvento(String codiceEvento) {
        return (root, query, cb) -> {
            Join<AccrualTPrimaNota, AccrualTCausale> causaleJoin = root.join("causale", JoinType.INNER);
            Join<AccrualTCausale, AccrualTEvento> eventoJoin = causaleJoin.join("evento", JoinType.INNER);
            return cb.equal(eventoJoin.get("codice"), codiceEvento);
        };
    }

    public static Specification<AccrualTPrimaNota> byCausale(String codiceCausale) {
        return (root, query, cb) -> {
            Join<AccrualTPrimaNota, AccrualTCausale> causaleJoin = root.join("causale", JoinType.INNER);
            return cb.equal(causaleJoin.get("codice"), codiceCausale);
        };
    }

    public static Specification<AccrualTPrimaNota> byNumeroProvvisorio(Integer numeroProvvisorio) {
        return (root, query, cb) -> cb.equal(root.get("numero"), numeroProvvisorio);
    }

    public static Specification<AccrualTPrimaNota> byNumeroDefinitivo(Integer numeroDefinitivo) {
        return (root, query, cb) -> cb.equal(root.get("progressivoGiornale"), numeroDefinitivo);
    }

    public static Specification<AccrualTPrimaNota> byStato(String codiceStato) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<AccrualRPrimaNotaStato> statoRoot = subquery.from(AccrualRPrimaNotaStato.class);
            Join<AccrualRPrimaNotaStato, AccrualDPrimaNotaStato> dStatoJoin = statoRoot.join("primaNotaStato", JoinType.INNER);
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                        cb.equal(statoRoot.get("primaNota"), root),
                        cb.equal(dStatoJoin.get("codice"), codiceStato),
                        cb.isNull(statoRoot.get("fineValidita"))
                    ));
            return cb.exists(subquery);
        };
    }

    public static Specification<AccrualTPrimaNota> byDescrizione(String descrizione) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("descrizione")), "%" + descrizione.toLowerCase() + "%");
    }

    public static Specification<AccrualTPrimaNota> byCodiceConto(String codiceConto) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<AccrualTPrimaNotaImporto> importoRoot = subquery.from(AccrualTPrimaNotaImporto.class);
            Join<AccrualTPrimaNotaImporto, AccrualTPdc> pdcJoin = importoRoot.join("pdc", JoinType.INNER);
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                        cb.equal(importoRoot.get("primaNota"), root),
                        cb.equal(pdcJoin.get("codiceUfficiale"), codiceConto),
                        cb.isNull(importoRoot.get("dataCancellazione"))
                    ));
            return cb.exists(subquery);
        };
    }

    public static Specification<AccrualTPrimaNota> byDataRegProvvisoriaDa(String dataRegProvvisoriaDa) {
        return (root, query, cb) -> {
            LocalDateTime startOfDay = LocalDate.parse(dataRegProvvisoriaDa).atStartOfDay();
            return cb.greaterThanOrEqualTo(root.get("data"), startOfDay);
        };
    }

    public static Specification<AccrualTPrimaNota> byDataRegProvvisoriaA(String dataRegProvvisoriaA) {
        return (root, query, cb) -> {
            // Per includere tutto il giorno, impostiamo l'orario a 23:59:59
            LocalDateTime endOfDay = LocalDate.parse(dataRegProvvisoriaA).atTime(23, 59, 59);
            return cb.lessThanOrEqualTo(root.get("data"), endOfDay);
        };
    }

    public static Specification<AccrualTPrimaNota> byDataRegDefinitivaDa(String dataRegDefinitivaDa) {
        return (root, query, cb) -> {
            LocalDateTime startOfDay = LocalDate.parse(dataRegDefinitivaDa).atStartOfDay();
            return cb.greaterThanOrEqualTo(root.get("dataRegistrazioneGiornale"), startOfDay);
        };
    }

    public static Specification<AccrualTPrimaNota> byDataRegDefinitivaA(String dataRegDefinitivaA) {
        return (root, query, cb) -> {
            // Per includere tutto il giorno, impostiamo l'orario a 23:59:59
            LocalDateTime endOfDay = LocalDate.parse(dataRegDefinitivaA).atTime(23, 59, 59);
            return cb.lessThanOrEqualTo(root.get("dataRegistrazioneGiornale"), endOfDay);
        };
    }

    public static Specification<AccrualTPrimaNota> byImportoDa(Number importoDa) {
        return (root, query, cb) -> {
            // Creiamo una subquery per trovare prime note con somma importi DARE >= valore minimo
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<AccrualTPrimaNotaImporto> importoRoot = subquery.from(AccrualTPrimaNotaImporto.class);

            Join<AccrualTPrimaNotaImporto, AccrualTPrimaNota> primaNotaJoin = importoRoot.join("primaNota", JoinType.INNER);

            // Filtriamo solo gli importi DARE
            Predicate darePredicate = cb.equal(importoRoot.get("segno").as(String.class), TipoSegno.DARE.getCode());

            // Raggruppiamo per prima nota e confrontiamo la somma
            subquery.select(cb.count(importoRoot.get("id"))) // Solo per il count
                    .where(cb.and(
                        cb.equal(primaNotaJoin.get("id"), root.get("id")),
                        darePredicate
                    ))
                    .groupBy(primaNotaJoin.get("id"))
                    .having(cb.greaterThanOrEqualTo(
                        cb.coalesce(cb.sum(importoRoot.get("importo")), BigDecimal.ZERO),
                        new BigDecimal(importoDa.toString())
                    ));

            // La condizione è che esista tale gruppo
            return cb.exists(subquery);
        };
    }

    public static Specification<AccrualTPrimaNota> byImportoA(Number importoA) {
        return (root, query, cb) -> {
            // Creiamo una subquery per trovare prime note con somma importi DARE <= valore massimo
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<AccrualTPrimaNotaImporto> importoRoot = subquery.from(AccrualTPrimaNotaImporto.class);

            Join<AccrualTPrimaNotaImporto, AccrualTPrimaNota> primaNotaJoin = importoRoot.join("primaNota", JoinType.INNER);

            // Filtriamo solo gli importi DARE
            Predicate darePredicate = cb.equal(importoRoot.get("segno").as(String.class), TipoSegno.DARE.getCode());

            // Raggruppiamo per prima nota e confrontiamo la somma
            subquery.select(cb.count(importoRoot.get("id"))) // Solo per il count
                    .where(cb.and(
                        cb.equal(primaNotaJoin.get("id"), root.get("id")),
                        darePredicate
                    ))
                    .groupBy(primaNotaJoin.get("id"))
                    .having(cb.lessThanOrEqualTo(
                        cb.coalesce(cb.sum(importoRoot.get("importo")), BigDecimal.ZERO),
                        new BigDecimal(importoA.toString())
                    ));

            // La condizione è che esista tale gruppo
            return cb.exists(subquery);
        };
    }

    public static Specification<AccrualTPrimaNota> nonQuadrata() {
        return (root, query, cb) -> {
            // Subquery somma DARE (non cancellati)
            Subquery<BigDecimal> dareQuery = query.subquery(BigDecimal.class);
            Root<AccrualTPrimaNotaImporto> dareRoot = dareQuery.from(AccrualTPrimaNotaImporto.class);
            Expression<BigDecimal> dareSum = cb.coalesce(cb.sum(dareRoot.<BigDecimal>get("importo")), BigDecimal.ZERO);
            dareQuery.select(dareSum)
                     .where(cb.and(
                         cb.equal(dareRoot.get("primaNota"), root),
                         cb.equal(dareRoot.get("segno").as(String.class), TipoSegno.DARE.getCode()),
                         cb.isNull(dareRoot.get("dataCancellazione"))
                     ));

            // Subquery somma AVERE (non cancellati)
            Subquery<BigDecimal> avereQuery = query.subquery(BigDecimal.class);
            Root<AccrualTPrimaNotaImporto> avereRoot = avereQuery.from(AccrualTPrimaNotaImporto.class);
            Expression<BigDecimal> avereSum = cb.coalesce(cb.sum(avereRoot.<BigDecimal>get("importo")), BigDecimal.ZERO);
            avereQuery.select(avereSum)
                      .where(cb.and(
                          cb.equal(avereRoot.get("primaNota"), root),
                          cb.equal(avereRoot.get("segno").as(String.class), TipoSegno.AVERE.getCode()),
                          cb.isNull(avereRoot.get("dataCancellazione"))
                      ));

            // Subquery count importi non cancellati
            Subquery<Long> countQuery = query.subquery(Long.class);
            Root<AccrualTPrimaNotaImporto> countRoot = countQuery.from(AccrualTPrimaNotaImporto.class);
            countQuery.select(cb.count(countRoot.get("id")))
                      .where(cb.and(
                          cb.equal(countRoot.get("primaNota"), root),
                          cb.isNull(countRoot.get("dataCancellazione"))
                      ));

            // Non quadrata: nessun importo OPPURE SUM(DARE) != SUM(AVERE)
            return cb.or(
                cb.equal(countQuery, 0L),
                cb.notEqual(dareQuery, avereQuery)
            );
        };
    }
}
