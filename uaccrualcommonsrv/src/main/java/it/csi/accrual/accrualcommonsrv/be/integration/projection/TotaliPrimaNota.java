/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.projection;

import java.math.BigDecimal;

public interface TotaliPrimaNota {
    Long getPrimaNotaId();
    BigDecimal getTotaleDare();
    BigDecimal getTotaleAvere();
}
