/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@NoRepositoryBean
public interface EnteAwareRepository<T> extends CrudRepository<T, Long>, JpaSpecificationExecutor<T> {
    
    default Optional<T> findById(Long id) {
        Specification<T> enteFilter = createEnteFilter();
        return findOne(enteFilter.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("id"), id)));
    }
    
    default List<T> findAll() {
        Specification<T> enteFilter = createEnteFilter();
        return findAll(enteFilter);
    }
    
    default List<T> findAll(Sort sort) {
        Specification<T> enteFilter = createEnteFilter();
        return findAll(enteFilter, sort);
    }
    
    default Page<T> findAll(Pageable pageable) {
        Specification<T> enteFilter = createEnteFilter();
        return findAll(enteFilter, pageable);
    }
    
    default long count() {
        Specification<T> enteFilter = createEnteFilter();
        return count(enteFilter);
    }
    
    default boolean existsById(Long id) {
        Specification<T> enteFilter = createEnteFilter();
        return exists(enteFilter.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("id"), id)));
    }
    
    private Specification<T> createEnteFilter() {
        return (root, query, criteriaBuilder) -> {
            Long currentEnte = EnteContextHolder.getCurrentEnte();
            if (currentEnte != null) {
                // Verifica se l'entità ha un campo ente
                try {
                    root.get("ente");
                    return criteriaBuilder.equal(root.get("ente").get("id"), currentEnte);
                } catch (Exception e) {
                    // Se l'entità non ha un campo ente, non applicare il filtro
                    return criteriaBuilder.conjunction();
                }
            }
            return criteriaBuilder.conjunction(); // Nessun filtro se non c'è un ente attivo
        };
    }
}