/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccrualTPdcRepository extends JpaRepository<AccrualTPdc, Long>, JpaSpecificationExecutor<AccrualTPdc> {

    @Query("SELECT p FROM AccrualTPdc p WHERE p.pdcClasse.codice = :classeCodice AND p.ente = :ente")
    List<AccrualTPdc> findByPdcClasseCodiceAndEnte(@Param("classeCodice") String classeCodice, @Param("ente") AccrualTEnte ente);

    /**
     * Trova un PDC per codice
     */
    AccrualTPdc findByCodice(String codice);

    /**
     * Trova un PDC non cancellato per codice
     */
    AccrualTPdc findByCodiceAndDataCancellazioneIsNull(String codice);

    /**
     * Trova PDC per codice e id ente
     */
    @Query("SELECT p FROM AccrualTPdc p WHERE p.codice = :codice AND p.ente.id = :enteId")
    AccrualTPdc findByCodiceAndEnte_Id(@Param("codice") String codice, @Param("enteId") Long enteId);

    /**
     * Trova un PDC non cancellato per codice e id ente
     */
    @Query("SELECT p FROM AccrualTPdc p WHERE p.codice = :codice AND p.ente.id = :enteId AND p.dataCancellazione IS NULL")
    AccrualTPdc findByCodiceAndEnte_IdAndDataCancellazioneIsNull(@Param("codice") String codice, @Param("enteId") Long enteId);

    /**
     * Trova il primo PDC non cancellato per codice e id ente (resiliente a duplicati)
     */
    Optional<AccrualTPdc> findFirstByCodiceAndEnte_IdAndDataCancellazioneIsNull(String codice, Long enteId);

    /**
     * Trova PDC per codice e codice fiscale ente
     */
    @Query("SELECT p FROM AccrualTPdc p WHERE p.codice = :codice AND p.ente.codiceUnivoco = :enteCodiceUnivoco")
    AccrualTPdc findByCodiceAndEnte_CodiceUnivoco(@Param("codice") String codice, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    /**
     * Trova PDC per codice e codice fiscale ente, con fetch eager delle relazioni necessarie.
     */
    @Query("SELECT pdc FROM AccrualTPdc pdc " +
           "LEFT JOIN FETCH pdc.pdcClasse " +
           "LEFT JOIN FETCH pdc.pdcTipo " +
           "LEFT JOIN FETCH pdc.pdcStato " +
           "WHERE pdc.codice = :codice AND pdc.ente.codiceUnivoco = :enteCodiceUnivoco")
    AccrualTPdc findByCodiceAndEnteCodiceUnivocoWithRelations(@Param("codice") String codice, @Param("enteCodiceUnivoco") String enteCodiceUnivoco);

    /**
     * Controlla se esiste un PDC con un determinato codice
     */
    boolean existsByCodice(String codice);

    /**
     * Controlla se esiste un PDC con un determinato codice e id diverso
     */
    boolean existsByCodiceAndIdNot(String codice, Long id);

    /**
     * Trova PDC per famiglia
     */
    Page<AccrualTPdc> findByPdcFamiglia_Id(Long famigliaId, Pageable pageable);

    /**
     * Discovery per CDU-008: Trova il conto di apertura patrimoniale (Ref. 2.6.1.2)
     * Deve iniziare per '99', essere foglia e avere descrizione specifica o codice specifico.
     * Se trova più risultati, restituisce il primo ordinato per codice.
     */
    @Query("SELECT p FROM AccrualTPdc p WHERE p.ente = :ente " +
           "AND (p.codice = '99.01.01.01' OR p.codice = '99.1.0.0.0' OR p.descrizione = 'APERTURA PATRIMONIALE') " +
           "AND (p.foglia = true OR p.fogliaNonFoglia = true) " +
           "AND p.dataCancellazione IS NULL")
    List<AccrualTPdc> findContoAperturaPatrimoniale(@Param("ente") AccrualTEnte ente);

    // CDU-003: Verifica unicità codice ufficiale
    Optional<AccrualTPdc> findByCodiceUfficialeAndEnte_IdAndDataCancellazioneIsNull(String codiceUfficiale, Long idEnte);

    // CDU-003: Trova figli di un conto
    List<AccrualTPdc> findByPdcPadre_IdAndDataCancellazioneIsNull(Long idPdcPadre);

    // CDU-003: Verifica esistenza legami (esempio per conti figli)
    boolean existsByPdcPadre_IdAndDataCancellazioneIsNull(Long idPdc);

    /**
     * Verifica se esistono conti figli con famiglia incompatibile
     * (diversa da quella del conto padre)
     *
     * @param pdcPadreId ID del conto padre
     * @param pdcFamigliaId ID della famiglia del conto padre
     * @return true se esistono figli con famiglia diversa, false altrimenti
     */
    @Query("SELECT COUNT(p) > 0 FROM AccrualTPdc p " +
           "WHERE p.pdcPadre.id = :pdcPadreId " +
           "AND p.dataCancellazione IS NULL " +
           "AND p.pdcFamiglia.id != :pdcFamigliaId")
    boolean existsFigliConFamigliaIncompatibile(
        @Param("pdcPadreId") Long pdcPadreId,
        @Param("pdcFamigliaId") Long pdcFamigliaId);

    // CDU-003 V01: Verifica esistenza prime note associate a un conto
    @Query("SELECT COUNT(pni) > 0 FROM AccrualTPrimaNotaImporto pni " +
           "JOIN AccrualTPrimaNota pn ON pni.primaNota.id = pn.id " +
           "JOIN AccrualRPrimaNotaStato stato ON pn.id = stato.primaNota.id " +
           "JOIN AccrualDPrimaNotaStato statoPrimaNota ON stato.primaNotaStato.id = statoPrimaNota.id " +
           "WHERE pni.pdc.id = :idConto " +
           "AND statoPrimaNota.codice != 'ANNULLATO'")
    boolean existsPrimeNoteAssociate(
        @Param("idConto") Long idConto
    );

    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndEnte_IdAndIdNot(String codiceUfficiale, Long classeId, Long enteId, Long id);
    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(String codiceUfficiale, Long classeId, Long famigliaId, Long enteId, Long id);
    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndEnte_Id(String codiceUfficiale, Long classeId, Long enteId);
    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndEnte_IdAndDataCancellazioneIsNull(String codiceUfficiale, Long classeId, Long enteId);
    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_Id(String codiceUfficiale, Long classeId, Long famigliaId, Long enteId);
    Optional<AccrualTPdc> findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndDataCancellazioneIsNull(String codiceUfficiale, Long classeId, Long famigliaId, Long enteId);
    
    @Query("SELECT p FROM AccrualTPdc p WHERE p.codiceUfficiale = :codice AND p.ente.id = :enteId")
    Optional<AccrualTPdc> findByCodiceUfficialeAndEnte_Id(@Param("codice") String codice, @Param("enteId") Long enteId);

    /**
     * Elimina i conti per codice ufficiale e ente (usato per cleanup test)
     */
    void deleteByCodiceUfficialeAndEnte_Id(String codiceUfficiale, Long enteId);

    @Query("SELECT COUNT(pni) > 0 FROM AccrualTPrimaNotaImporto pni " +
           "JOIN AccrualTPrimaNota pn ON pni.primaNota.id = pn.id " +
           "JOIN AccrualRPrimaNotaStato stato ON pn.id = stato.primaNota.id " +
           "JOIN AccrualDPrimaNotaStato statoPrimaNota ON stato.primaNotaStato.id = statoPrimaNota.id " +
           "WHERE pni.pdc.id IN :idConti " +
           "AND statoPrimaNota.codice != 'ANNULLATO' " +
           "AND pn.dataRegistrazioneGiornale > :fineValidita")
    boolean esistonoPrimeNoteSuccessive(@Param("idConti") List<Long> idConti, @Param("fineValidita") LocalDate fineValidita);
    
    @Query("SELECT p FROM AccrualTPdc p WHERE p.pdcFamiglia.codice = :famigliaCodice")
    Optional<AccrualTPdc> findByCodiceAndPdcFamigliaCod(@Param("codice") String codice, @Param("famigliaCodice") String famigliaCodice);
    
    @Query("SELECT p FROM AccrualTPdc p WHERE p.ente = :ente AND p.dataCancellazione IS NULL")
    List<AccrualTPdc> findAllByEnteAndNotDeleted(@Param("ente") AccrualTEnte ente);

    @Query("SELECT p FROM AccrualTPdc p WHERE p.fondoDotazione = true AND p.ente = :ente AND p.dataCancellazione IS NULL")
    Optional<AccrualTPdc> findFondoDotazione(@Param("ente") AccrualTEnte ente);

    @Query("SELECT p FROM AccrualTPdc p WHERE p.codice = :codice AND p.descrizione = :descrizione")
    Optional<AccrualTPdc> findByCodiceAndDesc(@Param("codice") String codice, @Param("descrizione") String descrizione);
    
    @Query("SELECT p FROM AccrualTPdc p WHERE (p.codice = :codice OR p.codiceUfficiale = :codice) AND p.ente.id = :enteId AND (p.fineValidita IS NULL OR p.fineValidita >= CURRENT_TIMESTAMP)")
    Optional<AccrualTPdc> findByCodiceAndAnnoAndEnteId(@Param("codice") String codice, @Param("anno") Integer anno, @Param("enteId") Long enteId);

    @Query("SELECT p FROM AccrualTPdc p WHERE (p.codice IN :codici OR p.codiceUfficiale IN :codici) AND p.ente.id = :enteId AND (p.fineValidita IS NULL OR p.fineValidita >= CURRENT_TIMESTAMP)")
    List<AccrualTPdc> findByCodiceInAndEnteId(@Param("codici") List<String> codici, @Param("enteId") Long enteId);

    /**
     * Verifica se esistono conti con stessa combinazione (ente, famiglia, classe, codiceUfficiale)
     * e periodi di validità sovrapposti.
     * Due periodi si sovrappongono se: inizio1 <= fine2 AND inizio2 <= fine1
     * @param codiceUfficiale il codice ufficiale del conto
     * @param classeId ID della classe
     * @param famigliaId ID della famiglia
     * @param enteId ID dell'ente
     * @param inizioValidita data inizio validità del conto da salvare
     * @param fineValidita data fine validità del conto da salvare
     * @param excludeId ID del conto da escludere (per aggiornamenti, null per inserimenti)
     * @return true se esiste sovrapposizione
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM accrual.accrual_t_pdc p " +
           "WHERE p.pdc_codice_ufficiale = :codiceUfficiale " +
           "AND p.pdc_classe_id = :classeId " +
           "AND p.pdc_famiglia_id = :famigliaId " +
           "AND p.ente_id = :enteId " +
           "AND p.pdc_id != :excludeId " +
           "AND p.gest_data_del IS NULL " +
           "AND (p.fine_validita IS NULL OR p.fine_validita >= :inizioValidita) " +
           "AND (p.inizio_validita <= COALESCE(CAST(:fineValidita AS timestamp), CAST('9999-12-31 23:59:59' AS timestamp)))",
           nativeQuery = true)
    boolean existsSovrapposizioneValidita(
        @Param("codiceUfficiale") String codiceUfficiale,
        @Param("classeId") Long classeId,
        @Param("famigliaId") Long famigliaId,
        @Param("enteId") Long enteId,
        @Param("inizioValidita") LocalDateTime inizioValidita,
        @Param("fineValidita") LocalDateTime fineValidita,
        @Param("excludeId") Long excludeId);
}
