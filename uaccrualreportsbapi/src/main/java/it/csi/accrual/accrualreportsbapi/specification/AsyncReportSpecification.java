/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.specification;

import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifiche JPA per la ricerca di report asincroni con filtri dinamici.
 * 
 * Utilizza CriteriaBuilder per evitare problemi di casting con PostgreSQL
 * quando i parametri sono NULL.
 */
public class AsyncReportSpecification {

    /**
     * Crea una specifica per la ricerca di report con filtri multipli.
     *
     * @param enteId ID dell'ente (obbligatorio)
     * @param completedAtDa Data di completamento da (opzionale, >=)
     * @param completedAtA Data di completamento a (opzionale, <=)
     * @param status Stato del report (opzionale)
     * @param tipoReport Tipo di report (opzionale, filtrato tramite rptdesignFileName)
     * @param formato Formato del report (opzionale)
     * @return Specifica JPA per la ricerca
     */
    public static Specification<AsyncReport> withFilters(
            Long enteId,
            LocalDateTime completedAtDa,
            LocalDateTime completedAtA,
            AsyncReport.ReportStatus status,
            String tipoReport,
            String formato) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // enteId — obbligatorio
            predicates.add(cb.equal(root.get("enteId"), enteId));

            // completedAt >= da (opzionale)
            if (completedAtDa != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("completedAt"), completedAtDa));
            }

            // completedAt <= a (opzionale)
            if (completedAtA != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("completedAt"), completedAtA));
            }

            // status (opzionale)
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // tipo_report (opzionale) - filtro su rptdesignFileName con LIKE 'ACCRUAL-XXX%'
            if (tipoReport != null && !tipoReport.isBlank()) {
                // Il tipo_report arriva già come codice report (es. ACCRUAL-001)
                predicates.add(cb.like(root.get("rptdesignFileName"), tipoReport + "%"));
            }

            // formato (opzionale) - converto a lowercase per match con DB
            if (formato != null && !formato.isBlank()) {
                predicates.add(cb.equal(root.get("format"), formato.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
