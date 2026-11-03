/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSpecifications {

    public static Specification<AccrualTFile> byEnteId(Long enteId) {
        return (root, query, cb) -> cb.equal(root.get("ente").get("id"), enteId);
    }

    public static Specification<AccrualTFile> byCodiceFile(String codiceFile) {
        return (root, query, cb) -> {
            if (codiceFile == null || codiceFile.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true)); // Nessun filtro se il codice è vuoto
            }
            return cb.like(cb.lower(root.get("codice")), "%" + codiceFile.toLowerCase() + "%");
        };
    }

    public static Specification<AccrualTFile> byFonte(String fonte) {
        return (root, query, cb) -> {
            if (fonte == null || fonte.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            // CDU-011: Ricerca file associati a un tipo file collegato alla fonte indicata
            Join<AccrualTFile, AccrualDFileTipo> fileTipoJoin = root.join("fileTipo");
            Join<AccrualDFileTipo, AccrualDFonte> fonteJoin = fileTipoJoin.join("fonte");
            return cb.like(cb.lower(fonteJoin.get("codice")), "%" + fonte.toLowerCase() + "%");
        };
    }

    public static Specification<AccrualTFile> byTipoFile(String tipoFile) {
        return (root, query, cb) -> {
            if (tipoFile == null || tipoFile.trim().isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }
            Join<AccrualTFile, AccrualTEnte> tipoFileJoin = root.join("fileTipo");
            return cb.equal(tipoFileJoin.get("codice"), tipoFile);
        };
    }

    public static Specification<AccrualTFile> byDataCaricamento(Date dataCaricamento) {
        return (root, query, cb) -> {
            if (dataCaricamento == null) {
                return cb.isTrue(cb.literal(true));
            }
            // Converti java.util.Date in LocalDateTime per il confronto
            LocalDateTime localDateTime = dataCaricamento.toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime();

            // Per cercare solo la data, ignorando l'ora, confrontiamo il giorno
            // Assumendo che il campo dataInserimento in AccrualTFile sia un LocalDateTime
            return cb.equal(cb.function("DATE", java.sql.Date.class, root.get("dataInserimento")),
                            cb.function("DATE", java.sql.Date.class, cb.literal(localDateTime)));
        };
    }

    /**
     * Specifica per filtrare i file non eliminati (dataCancellazione = null)
     */
    public static Specification<AccrualTFile> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("dataCancellazione"));
    }

    // Metodo combinato per tutti i filtri
    public static Specification<AccrualTFile> withFilters(Long enteId, String codiceFile, String fonte, String tipoFile, Date dataCaricamento) {
        List<Specification<AccrualTFile>> specs = new ArrayList<>();

        // Aggiunge sempre il filtro per escludere i file eliminati
        specs.add(notDeleted());

        if (enteId != null) {
            specs.add(byEnteId(enteId));
        }
        if (codiceFile != null && !codiceFile.trim().isEmpty()) {
            specs.add(byCodiceFile(codiceFile));
        }
        if (fonte != null && !fonte.trim().isEmpty()) {
            specs.add(byFonte(fonte));
        }
        if (tipoFile != null && !tipoFile.trim().isEmpty()) {
            specs.add(byTipoFile(tipoFile));
        }
        if (dataCaricamento != null) {
            specs.add(byDataCaricamento(dataCaricamento));
        }

        return specs.stream().reduce(Specification.where(null), Specification::and);
    }
}
