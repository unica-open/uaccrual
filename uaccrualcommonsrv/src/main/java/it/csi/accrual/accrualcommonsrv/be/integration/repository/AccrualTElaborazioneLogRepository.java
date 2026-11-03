/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTElaborazioneLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccrualTElaborazioneLogRepository extends JpaRepository<AccrualTElaborazioneLog, Long> {
    
    @Query("SELECT l FROM AccrualTElaborazioneLog l WHERE l.elaborazione.file.id = :flussoId ORDER BY l.dataInserimento DESC")
    List<AccrualTElaborazioneLog> findByElaborazioneFileIdOrderByDataInserimentoDesc(@Param("flussoId") Long flussoId);
    

}
