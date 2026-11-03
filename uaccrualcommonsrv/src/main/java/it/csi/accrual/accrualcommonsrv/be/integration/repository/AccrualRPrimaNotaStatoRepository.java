/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRPrimaNotaStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccrualRPrimaNotaStatoRepository extends JpaRepository<AccrualRPrimaNotaStato, Long> {


    @Query("SELECT s FROM AccrualRPrimaNotaStato s WHERE s.primaNota = :primaNota AND s.fineValidita IS NULL")
    List<AccrualRPrimaNotaStato> findByPrimaNotaAndFineValiditaIsNull(@Param("primaNota") AccrualTPrimaNota primaNota);

    @Query("SELECT s FROM AccrualRPrimaNotaStato s WHERE s.primaNota.id = :primaNotaId AND s.fineValidita IS NULL")
    List<AccrualRPrimaNotaStato> findByPrimaNotaIdAndFineValiditaIsNull(@Param("primaNotaId") Long primaNotaId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE AccrualRPrimaNotaStato s SET s.fineValidita = :now, s.utenteAggiornamento = :utente WHERE s.primaNota.id IN :ids AND s.fineValidita IS NULL")
    int chiudiStatiByPrimaNotaIds(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now, @Param("utente") String utente);
}