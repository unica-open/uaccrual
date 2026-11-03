/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEtichetta;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualTEtichettaRepository extends JpaRepository<AccrualTEtichetta, Long> {

    @Query("SELECT e FROM AccrualTEtichetta e WHERE e.codice = :codice AND e.ente = :ente AND e.dataCancellazione IS NULL ORDER BY e.dataInserimento DESC")
    List<AccrualTEtichetta> findByCodiceAndEnte(@Param("codice") String codice, @Param("ente") AccrualTEnte ente);

    /**
     * Etichette attive per i codici indicati, dalla piu' recente alla piu' vecchia.
     *
     * <p>Lo stesso numero di etichetta puo' comparire piu' volte, perche' ogni elaborazione ne
     * inserisce una riga nuova. L'ordinamento fa trovare per prima quella appena inserita, cosi'
     * i legami con la prima nota non finiscono su un'etichetta di un'elaborazione precedente.
     * L'id discrimina le righe inserite nello stesso istante, come quelle di uno stesso file.</p>
     */
    @Query("SELECT e FROM AccrualTEtichetta e WHERE e.codice IN :codici AND e.ente = :ente AND e.dataCancellazione IS NULL "
         + "ORDER BY e.dataInserimento DESC, e.id DESC")
    List<AccrualTEtichetta> findByCodiceInAndEnte(@Param("codici") List<String> codici, @Param("ente") AccrualTEnte ente);

    @Query("SELECT e FROM AccrualTEtichetta e WHERE e.ente = :ente AND e.anno = :anno AND e.dataCancellazione IS NULL")
    List<AccrualTEtichetta> findByEnteAndAnno(@Param("ente") AccrualTEnte ente, @Param("anno") Integer anno);
}
