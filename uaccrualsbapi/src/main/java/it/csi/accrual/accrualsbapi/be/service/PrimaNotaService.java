/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;

public interface PrimaNotaService {
    AccrualTPrimaNota creaPrimaNota(AccrualTEnte ente, AccrualTCausale causale, AccrualTFile file, String numero, String anno, java.util.Date data, String note);
    void aggiungiAttributo(AccrualTPrimaNota primaNota, String codiceAttributo, String valoreAttributo);
    void aggiungiAttributo(AccrualTPrimaNota primaNota, String codiceAttributo, String valoreAttributo, AccrualTPrimaNotaImporto primaNotaImporto);
    AccrualTPrimaNotaImporto getUltimoImporto(AccrualTPrimaNota primaNota);
}