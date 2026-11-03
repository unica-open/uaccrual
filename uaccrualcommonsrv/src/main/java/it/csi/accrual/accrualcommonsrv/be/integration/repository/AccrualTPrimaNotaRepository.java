/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTCausale;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccrualTPrimaNotaRepository extends JpaRepository<AccrualTPrimaNota, Long>, JpaSpecificationExecutor<AccrualTPrimaNota> {

    @Query("SELECT DISTINCT p FROM AccrualTPrimaNota p " +
           "JOIN FETCH p.causale c " +
           "LEFT JOIN FETCH c.evento " +
           "LEFT JOIN FETCH p.stati s " +
           "LEFT JOIN FETCH s.primaNotaStato " +
           "WHERE p.ente = :ente AND p.anno = :anno AND c.evento.codice = :eventoCodice")
    List<AccrualTPrimaNota> findByEnteAndAnnoAndEventoCodice(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno, @Param("eventoCodice") String eventoCodice);

    @Query("SELECT DISTINCT p FROM AccrualTPrimaNota p " +
           "JOIN FETCH p.causale c " +
           "LEFT JOIN FETCH p.stati s " +
           "LEFT JOIN FETCH s.primaNotaStato " +
           "WHERE p.ente = :ente AND p.anno = :anno AND c.codice IN :causaliCodici")
    List<AccrualTPrimaNota> findByEnteAndAnnoAndCausaleCodiceIn(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno, @Param("causaliCodici") List<String> causaliCodici);

    @Query("SELECT p FROM AccrualTPrimaNota p JOIN p.ente e WHERE p.id = :id AND e.codiceUnivoco = :enteCodiceUnivoco")
    AccrualTPrimaNota findByIdAndEnteCodiceUnivoco(@Param("id") Long id, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    // ============================================================================
    // QUERY PER CONSULTAZIONE (senza importi/attributi - solo intestazione)
    // ============================================================================
    
    /**
     * Query ottimizzata per consultazione prima nota - carica solo intestazione e stati
     * NON carica: importi, attributi, legami
     * Utilizzata da: getPrimaNotaById, getPrimaNotaByAnnoAndNumero, annullaPrimaNota, riapriPrimaNota
     */
    @Query("SELECT p FROM AccrualTPrimaNota p " +
           "JOIN FETCH p.ente e " +
           "LEFT JOIN FETCH p.tipo " +
           "LEFT JOIN FETCH p.fonte " +
           "LEFT JOIN FETCH p.causale c " +
           "LEFT JOIN FETCH c.evento " +
           "LEFT JOIN FETCH p.stati s " +
           "JOIN FETCH s.primaNotaStato " +
           "WHERE p.anno = :anno AND p.numero = :numero AND e.codiceUnivoco = :enteCodiceUnivoco")
    Optional<AccrualTPrimaNota> findForConsultazione(@Param("anno") Integer anno, @Param("numero") Integer numero, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    /**
     * Query ottimizzata per operazioni di aggiornamento - carica tutti i dati correlati
     * Utilizzata da: aggiornaPrimaNota (quando serve modificare conti/attributi)
     * MANTIENE la query esistente per backward compatibility
     */
    @Query("SELECT p FROM AccrualTPrimaNota p JOIN FETCH p.ente e LEFT JOIN FETCH p.tipo LEFT JOIN FETCH p.fonte LEFT JOIN FETCH p.causale LEFT JOIN FETCH p.stati LEFT JOIN FETCH p.importi LEFT JOIN FETCH p.attributi LEFT JOIN FETCH p.legamiComeDestinazione pd LEFT JOIN FETCH pd.primaNotaDa LEFT JOIN FETCH pd.primaNotaA LEFT JOIN FETCH pd.primaNotaTipoRelazione LEFT JOIN FETCH p.legamiComeOrigine po LEFT JOIN FETCH po.primaNotaDa LEFT JOIN FETCH po.primaNotaA LEFT JOIN FETCH po.primaNotaTipoRelazione WHERE p.anno = :anno AND p.numero = :numero AND e.codiceUnivoco = :enteCodiceUnivoco")
    Optional<AccrualTPrimaNota> findByAnnoAndNumeroAndEnteCodiceUnivoco(@Param("anno") Integer anno, @Param("numero") Integer numero, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);
    
    // ============================================================================
    // QUERY PER PRIME NOTE COLLEGATE (solo legami, senza importi/attributi)
    // ============================================================================
    
    /**
     * Query ottimizzata per recupero prime note collegate - carica solo legami
     * NON carica: importi, attributi
     * Utilizzata da: getPrimeNoteCollegate
     */
    @Query("SELECT p FROM AccrualTPrimaNota p " +
           "JOIN FETCH p.ente e " +
           "LEFT JOIN FETCH p.tipo " +
           "LEFT JOIN FETCH p.fonte " +
           "LEFT JOIN FETCH p.causale c " +
           "LEFT JOIN FETCH c.evento " +
           "LEFT JOIN FETCH p.stati s " +
           "JOIN FETCH s.primaNotaStato " +
           "LEFT JOIN FETCH p.legamiComeDestinazione ld " +
           "LEFT JOIN FETCH ld.primaNotaDa " +
           "LEFT JOIN FETCH ld.primaNotaA " +
           "LEFT JOIN FETCH ld.primaNotaTipoRelazione " +
           "LEFT JOIN FETCH p.legamiComeOrigine lo " +
           "LEFT JOIN FETCH lo.primaNotaDa " +
           "LEFT JOIN FETCH lo.primaNotaA " +
           "LEFT JOIN FETCH lo.primaNotaTipoRelazione " +
           "WHERE p.anno = :anno AND p.numero = :numero AND e.codiceUnivoco = :enteCodiceUnivoco")
    Optional<AccrualTPrimaNota> findForCollegate(@Param("anno") Integer anno, @Param("numero") Integer numero, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    @Query("SELECT p FROM AccrualTPrimaNota p WHERE p.causale IN :causali")
    List<AccrualTPrimaNota> findByCausaleIn(@Param("causali") List<AccrualTCausale> causali);
    
    @Query("SELECT pn FROM AccrualTPrimaNota pn JOIN pn.ente e JOIN pn.causale c JOIN pn.stati s " +
           "WHERE e.id = :enteId AND pn.anno = :anno AND c.codice IN :causaliCodici AND s.id != :statoId")
    List<AccrualTPrimaNota> findByEnteIdAndAnnoAndCausaleCodiceInAndStatoIdNot(@Param("enteId") Long enteId, @Param("anno") Integer anno, @Param("causaliCodici") List<String> causaliCodici, @Param("statoId") Long statoId);

    @Query("SELECT DISTINCT p.id FROM AccrualTPrimaNota p JOIN p.causale c JOIN p.stati s JOIN s.primaNotaStato ps " +
           "WHERE p.ente = :ente AND p.anno = :anno AND c.codice IN :codici " +
           "AND s.fineValidita IS NULL AND ps.codice != :statoAnnullato")
    List<Long> findIdsDiNonAnnullateByEnteAndAnnoAndCausaleCodiceIn(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno, @Param("codici") List<String> codici, @Param("statoAnnullato") String statoAnnullato);

    @Query("SELECT COUNT(DISTINCT p.id) FROM AccrualTPrimaNota p JOIN p.stati s JOIN s.primaNotaStato ps " +
           "WHERE p.causale.id = :causaleId AND p.dataCancellazione IS NULL " +
           "AND s.fineValidita IS NULL AND ps.codice <> :statoAnnullato")
    long countNonAnnullateByCausaleId(@Param("causaleId") Long causaleId, @Param("statoAnnullato") String statoAnnullato);

    @Query("SELECT COUNT(DISTINCT p.id) FROM AccrualTPrimaNota p JOIN p.stati s JOIN s.primaNotaStato ps " +
           "WHERE p.causale.id = :causaleId AND p.dataCancellazione IS NULL " +
           "AND s.fineValidita IS NULL AND ps.codice <> :statoAnnullato " +
           "AND p.data > :data")
    long countNonAnnullateByCausaleIdAndDataAfter(@Param("causaleId") Long causaleId, @Param("statoAnnullato") String statoAnnullato, @Param("data") LocalDateTime data);

    @Query("SELECT p FROM AccrualTPrimaNota p " +
           "JOIN p.ente e " +
           "WHERE e.codiceUnivoco = :enteCodiceUnivoco " +
           "AND p.anno = :anno " +
           "AND NOT EXISTS (SELECT 1 FROM AccrualRPrimaNotaStato s JOIN s.primaNotaStato ps WHERE s.primaNota = p AND ps.codice = 'A' AND s.fineValidita IS NULL) " +
           "AND (p.id NOT IN (SELECT DISTINCT pni.primaNota.id FROM AccrualTPrimaNotaImporto pni where pni.dataCancellazione IS NULL) " +
           "OR (SELECT COALESCE(SUM(CASE WHEN pni.segno = 'DARE' THEN pni.importo ELSE 0 END), 0) FROM AccrualTPrimaNotaImporto pni WHERE pni.primaNota.id = p.id and pni.dataCancellazione IS NULL) <> " +
           "(SELECT COALESCE(SUM(CASE WHEN pni.segno = 'AVERE' THEN pni.importo ELSE 0 END), 0) FROM AccrualTPrimaNotaImporto pni WHERE pni.primaNota.id = p.id and pni.dataCancellazione IS NULL))")
    List<AccrualTPrimaNota> findIncompleteByEnteCodiceUnivocoAndAnno(@Param("enteCodiceUnivoco") String enteCodiceUnivoco, @Param("anno") Integer anno);
}
