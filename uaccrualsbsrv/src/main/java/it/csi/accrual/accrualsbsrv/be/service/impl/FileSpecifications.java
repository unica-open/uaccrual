/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileTipo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFonte;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("codice")), "%" + codiceFile.toLowerCase() + "%");
        };
    }

    public static Specification<AccrualTFile> byFonte(String fonte) {
        return (root, query, cb) -> {
            if (fonte == null || fonte.trim().isEmpty()) {
                return cb.conjunction();
            }
            Join<AccrualTFile, AccrualDFileTipo> tipoFileJoin = root.join("fileTipo");
            Join<AccrualDFileTipo, AccrualDFonte> fonteJoin = tipoFileJoin.join("fonte");
            return cb.equal(fonteJoin.get("codice"), fonte);
        };
    }

    public static Specification<AccrualTFile> byTipoFile(String tipoFile) {
        return (root, query, cb) -> {
            if (tipoFile == null || tipoFile.trim().isEmpty()) {
                return cb.conjunction();
            }
            Join<AccrualTFile, AccrualDFileTipo> tipoFileJoin = root.join("fileTipo");
            return cb.equal(tipoFileJoin.get("codice"), tipoFile);
        };
    }

    public static Specification<AccrualTFile> byDataCaricamento(Date dataCaricamento) {
        return (root, query, cb) -> {
            if (dataCaricamento == null) {
                return cb.conjunction();
            }
            // Gestione della data ignorando l'ora: cerchiamo in tutto l'intervallo del giorno
            LocalDateTime startOfDay = dataCaricamento.toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.with(LocalTime.MAX);

            return cb.between(root.get("dataInserimento"), startOfDay, endOfDay);
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
