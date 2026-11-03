/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaAttributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualRPrimaNotaAttributoRepository extends JpaRepository<AccrualRPrimaNotaAttributo, Long> {
    
    // ============================================================================
    // QUERY OTTIMIZZATE PER API ATTRIBUTI (con FETCH espliciti)
    // ============================================================================
    
    /**
     * Query ottimizzata per recupero attributi della prima nota
     * Utilizzata da: getAttributiPrimaNota
     * Carica esplicitamente attributo e primaNotaImporto+pdc per evitare N+1 queries
     * Filtra solo i record con validità temporale attiva (non chiusi logicamente)
     */
    @Query("SELECT a FROM AccrualRPrimaNotaAttributo a " +
           "JOIN FETCH a.primaNota pn " +
           "JOIN FETCH pn.ente e " +
           "JOIN FETCH a.attributo " +
           "LEFT JOIN FETCH a.primaNotaImporto pni " +
           "LEFT JOIN FETCH pni.pdc " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND e.codiceUnivoco = :codiceEnte " +
           "AND a.fineValidita IS NULL " +
           "ORDER BY a.attributo.codice")
    List<AccrualRPrimaNotaAttributo> findAttributiForPrimaNota(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte
    );
    
    // ============================================================================
    // QUERY ESISTENTI (mantenute per backward compatibility)
    // ============================================================================

    @Query("SELECT a FROM AccrualRPrimaNotaAttributo a " +
           "WHERE a.primaNota.id = :primaNotaId " +
           "AND a.fineValidita IS NULL")
    List<AccrualRPrimaNotaAttributo> findByPrimaNota_Id(Long primaNotaId);
}