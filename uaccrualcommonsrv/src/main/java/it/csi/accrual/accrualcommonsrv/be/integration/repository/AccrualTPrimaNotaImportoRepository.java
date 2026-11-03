/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNotaImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.projection.TotaliPrimaNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualTPrimaNotaImportoRepository extends JpaRepository<AccrualTPrimaNotaImporto, Long> {

    // ============================================================================
    // QUERY OTTIMIZZATE PER API CONTI (con FETCH espliciti)
    // ============================================================================
    
    /**
     * Query ottimizzata per recupero importi/conti della prima nota
     * Utilizzata da: getContiPrimaNota
     * Carica esplicitamente pdc per evitare N+1 queries
     * Filtra solo i record non cancellati logicamente
     */
    @Query("SELECT i FROM AccrualTPrimaNotaImporto i " +
           "JOIN FETCH i.primaNota pn " +
           "JOIN FETCH pn.ente e " +
           "JOIN FETCH i.pdc " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND e.codiceUnivoco = :codiceEnte " +
           "AND i.dataCancellazione IS NULL " +
           "AND (:codiceConto IS NULL OR i.pdc.codiceUfficiale LIKE %:codiceConto%) " +
           "ORDER BY i.pdc.codiceUfficiale")
    List<AccrualTPrimaNotaImporto> findImportiForConti(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte,
        @Param("codiceConto") String codiceConto
    );

    /**
     * Query di conteggio per API conti - usata per paginazione
     * Utilizzata da: getContiPrimaNota
     * Filtra solo i record non cancellati logicamente
     */
    @Query("SELECT COUNT(i) FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.primaNota pn " +
           "JOIN pn.ente e " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND e.codiceUnivoco = :codiceEnte " +
           "AND i.dataCancellazione IS NULL " +
           "AND (:codiceConto IS NULL OR i.pdc.codiceUfficiale LIKE %:codiceConto%)")
    Long countImportiForConti(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte,
        @Param("codiceConto") String codiceConto
    );

    // ============================================================================
    // QUERY ESISTENTI (mantenute per backward compatibility)
    // ============================================================================

    @Query("SELECT i FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.primaNota pn " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND pn.ente.codiceUnivoco = :codiceEnte " +
           "AND (:codiceConto IS NULL OR i.pdc.codiceUfficiale LIKE %:codiceConto%)")
    List<AccrualTPrimaNotaImporto> findByAnnoAndNumeroAndEnteWithFiltroConto(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte,
        @Param("codiceConto") String codiceConto
    );

    @Query("SELECT COUNT(i) FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.primaNota pn " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND pn.ente.codiceUnivoco = :codiceEnte " +
           "AND (:codiceConto IS NULL OR i.pdc.codiceUfficiale LIKE %:codiceConto%)")
    Long countByAnnoAndNumeroAndEnteWithFiltroConto(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte,
        @Param("codiceConto") String codiceConto
    );

    @Query("SELECT i.primaNota.id as primaNotaId, " +
           "COALESCE(SUM(CASE WHEN i.segno = 'DARE' THEN i.importo ELSE 0 END), 0) as totaleDare, " +
           "COALESCE(SUM(CASE WHEN i.segno = 'AVERE' THEN i.importo ELSE 0 END), 0) as totaleAvere " +
           "FROM AccrualTPrimaNotaImporto i " +
           "WHERE i.primaNota.id IN :primaNotaIds " +
           "AND i.dataCancellazione IS NULL " +
           "GROUP BY i.primaNota.id")
    List<TotaliPrimaNota> findTotaliByPrimaNotaIds(@Param("primaNotaIds") List<Long> primaNotaIds);

    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN i.segno = 'DARE' THEN i.importo ELSE 0 END), 0) as totaleDare, " +
           "COALESCE(SUM(CASE WHEN i.segno = 'AVERE' THEN i.importo ELSE 0 END), 0) as totaleAvere " +
           "FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.primaNota pn " +
           "WHERE pn.anno = :anno " +
           "AND pn.numero = :numero " +
           "AND pn.ente.codiceUnivoco = :codiceEnte " +
           "AND i.dataCancellazione IS NULL")
    TotaliPrimaNota findTotaliByAnnoAndNumeroAndEnte(
        @Param("anno") Integer anno,
        @Param("numero") Integer numero,
        @Param("codiceEnte") String codiceEnte
    );

    @Query("SELECT i.pdc.codice, SUM(CASE WHEN i.segno = 'DARE' THEN i.importo ELSE -i.importo END) " +
           "FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.primaNota pn " +
           "JOIN pn.stati rs " +
           "WHERE pn.anno = :anno " +
           "AND pn.ente.codiceUnivoco = :codiceEnte " +
           "AND pn.causale.codice IN :causaliCodici " +
           "AND rs.primaNotaStato.codice = 'D' " +
           "AND rs.fineValidita IS NULL " +
           "AND i.dataCancellazione IS NULL " +
           "GROUP BY i.pdc.codice")
    List<Object[]> findRettificheAggregateByCausaleCodici(@Param("anno") Integer anno, @Param("codiceEnte") String codiceEnte, @Param("causaliCodici") List<String> causaliCodici);
    @Query("SELECT i FROM AccrualTPrimaNotaImporto i " +
           "WHERE i.primaNota.id = :primaNotaId " +
           "AND i.dataCancellazione IS NULL")
    List<AccrualTPrimaNotaImporto> findByPrimaNota_Id(Long primaNotaId);

    List<AccrualTPrimaNotaImporto> findByPrimaNotaIdOrderByIdDesc(Long primaNotaId);

    /**
     * Trova l'importo attivo per un conto specifico della prima nota.
     * Filtro su dataCancellazione IS NULL necessario per escludere i record
     * chiusi logicamente durante l'approccio ibrido di aggiornamento.
     */
    @Query("SELECT i FROM AccrualTPrimaNotaImporto i " +
           "JOIN i.pdc p " +
           "WHERE i.primaNota.id = :primaNotaId " +
           "AND p.codiceUfficiale = :pdcCodiceUfficiale " +
           "AND i.dataCancellazione IS NULL")
    List<AccrualTPrimaNotaImporto> findByPrimaNota_IdAndPdc_CodiceUfficiale(
        @Param("primaNotaId") Long primaNotaId,
        @Param("pdcCodiceUfficiale") String pdcCodiceUfficiale);
}
