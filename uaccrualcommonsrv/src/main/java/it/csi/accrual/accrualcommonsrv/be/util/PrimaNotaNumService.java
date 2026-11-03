/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.util;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNotaNum;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPrimaNotaNumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestione centralizzata dei contatori di numerazione per le prime note
 * (provvisori e definitivi). Usato da accrualsbsrv e accrualsbapi.
 */
@Service
public class PrimaNotaNumService {

    @Autowired
    private AccrualTPrimaNotaNumRepository primaNotaNumRepository;

    /**
     * Riserva {@code count} numeri consecutivi provvisori per l'ente e l'anno.
     * Restituisce il primo numero del blocco riservato.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int reserveNumeriProvvisori(AccrualTEnte ente, Integer anno, int count) {
        return reserveNumeri(ente, anno, count, false);
    }

    /**
     * Riserva {@code count} numeri consecutivi definitivi per l'ente e l'anno.
     * Restituisce il primo numero del blocco riservato.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int reserveNumeriDefinitivi(AccrualTEnte ente, Integer anno, int count) {
        return reserveNumeri(ente, anno, count, true);
    }

    /**
     * Riserva un singolo numero provvisorio. Equivalente a reserveNumeriProvvisori(ente, anno, 1).
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int nextNumeroProvvisorio(AccrualTEnte ente, Integer anno) {
        return reserveNumeri(ente, anno, 1, false);
    }

    /**
     * Riserva un singolo numero definitivo. Equivalente a reserveNumeriDefinitivi(ente, anno, 1).
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public int nextNumeroDefinitivo(AccrualTEnte ente, Integer anno) {
        return reserveNumeri(ente, anno, 1, true);
    }

    private int reserveNumeri(AccrualTEnte ente, Integer anno, int count, boolean definitiva) {
        if (count <= 0) {
            throw new IllegalArgumentException("count deve essere > 0");
        }
        AccrualTPrimaNotaNum contatore = primaNotaNumRepository
                .findByEnteAndAnnoAndIsDefinitiva(ente, String.valueOf(anno), definitiva)
                .orElseGet(() -> {
                    AccrualTPrimaNotaNum n = new AccrualTPrimaNotaNum();
                    n.setEnte(ente);
                    n.setAnno(String.valueOf(anno));
                    n.setNumero(0);
                    n.setIsDefinitiva(definitiva);
                    return n;
                });
        int primo = contatore.getNumero() + 1;
        contatore.setNumero(contatore.getNumero() + count);
        primaNotaNumRepository.save(contatore);
        return primo;
    }
}
