/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import it.csi.accrual.accrualsbsrv.dto.AttributoInfoDTO;
import java.util.List;

/**
 * Service per la gestione degli attributi
 */
public interface AttributoService {

    /**
     * Recupera la lista degli attributi disponibili per un ente
     *
     * @return lista di AttributoInfoDTO
     */
    List<AttributoInfoDTO> getAttributiByEnte();
}
