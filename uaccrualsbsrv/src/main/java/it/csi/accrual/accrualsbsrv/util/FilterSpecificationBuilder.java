/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.util;

import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.csi.accrual.accrualsbsrv.be.exception.BadRequestException;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@Component
public class FilterSpecificationBuilder<T> {

    private final ObjectMapper objectMapper;

    public FilterSpecificationBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Specification<T> build(String filter) {
        if (filter == null || filter.isEmpty()) {
            return Specification.where(null);
        }

        try {
            Map<String, Map<String, Object>> filterMap = objectMapper.readValue(filter, new TypeReference<Map<String, Map<String, Object>>>() {});
            
            Specification<T> spec = Specification.where(null);

            for (Map.Entry<String, Map<String, Object>> entry : filterMap.entrySet()) {
                String field = entry.getKey();
                Map<String, Object> operators = entry.getValue();

                for (Map.Entry<String, Object> operatorEntry : operators.entrySet()) {
                    String operator = operatorEntry.getKey();
                    Object value = operatorEntry.getValue();

                    spec = spec.and(createSpecification(field, operator, value));
                }
            }
            return spec;
        } catch (Exception e) {
            // Handle parsing exception, maybe log it and return an empty spec
            // For now, we'll just print the stack trace
            e.printStackTrace();
            return Specification.where(null);
        }
    }

    private Path<Object> getPath(Root<T> root, String field) {
        String[] parts = field.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }

    private Specification<T> createSpecification(String field, String operator, Object value) {
        return (root, query, criteriaBuilder) -> {
            Path<Object> path = getPath(root, field);
            switch (operator) {
                case "eq":
                    return criteriaBuilder.equal(path, value);
                case "ne":
                    return criteriaBuilder.notEqual(path, value);
                case "gt":
                    return criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) value);
                case "gte":
                    return criteriaBuilder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                case "lt":
                    return criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) value);
                case "lte":
                    return criteriaBuilder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                case "c": // contains (case-sensitive)
                    return criteriaBuilder.like(path.as(String.class), "%" + value.toString() + "%");
                case "ci": // contains (case-insensitive)
                    return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
                case "s": // starts with (case-sensitive)
                    return criteriaBuilder.like(path.as(String.class), value.toString() + "%");
                case "si": // starts with (case-insensitive)
                    return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
                case "e": // ends with (case-sensitive)
                    return criteriaBuilder.like(path.as(String.class), "%" + value.toString());
                case "ei": // ends with (case-insensitive)
                    return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
                case "in":
                    return path.in((java.util.Collection) value);
                case "nin":
                    return criteriaBuilder.not(path.in((java.util.Collection) value));
                default:
                    throw new BadRequestException("Unsupported operator: " + operator);
            }
        };
    }
}
