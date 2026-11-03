/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import it.csi.accrual.accrualsbsrv.dto.EntitaContabile;
import it.csi.accrual.accrualsbsrv.dto.ModelloMifData;

import java.util.List;

public interface ModelloMifService {
    ModelloMifData getDatiModelloMif(Integer anno);
    List<EntitaContabile> getAnniDisponibili();
}
