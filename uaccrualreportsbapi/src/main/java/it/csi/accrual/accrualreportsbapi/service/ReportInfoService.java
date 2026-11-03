/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import it.csi.accrual.accrualreportsbapi.dto.ReportInfo;
import java.util.Map;

/**
 * Service per l'estrazione dei metadata report dai template BIRT.
 */
public interface ReportInfoService {

    /**
     * Estrae i metadata da un template BIRT.
     * 
     * @param rptdesign Percorso del template .rptdesign relativo alla directory dei report
     * @return Metadata del report con parametri estratti
     * @throws IllegalArgumentException se il template non esiste
     * @throws RuntimeException se si verifica un errore nella lettura
     */
    ReportInfo getReportInfo(String rptdesign);

    /**
     * Estrae solo la mappa nome → etichetta per i parametri VISIBILI del template BIRT.
     *
     * @param rptdesign Percorso del template .rptdesign relativo alla directory dei report
     * @return Mappa nome_parametro → etichetta (promptText), esclude parametri hidden
     */
    Map<String, String> getParameterLabels(String rptdesign);
}
