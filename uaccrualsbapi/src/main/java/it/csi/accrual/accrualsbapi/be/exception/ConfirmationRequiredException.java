/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.exception;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPrimaNota;
import java.util.List;

public class ConfirmationRequiredException extends Exception {

    private String confirmationCode;
    private String confirmationMessage;
    private List<AccrualTPrimaNota> primaNoteDaAnnullare;

    public ConfirmationRequiredException(String confirmationCode, String confirmationMessage) {
        super(confirmationMessage);
        this.confirmationCode = confirmationCode;
        this.confirmationMessage = confirmationMessage;
    }

    public ConfirmationRequiredException(String confirmationCode, String confirmationMessage, List<AccrualTPrimaNota> primaNoteDaAnnullare) {
        super(confirmationMessage);
        this.confirmationCode = confirmationCode;
        this.confirmationMessage = confirmationMessage;
        this.primaNoteDaAnnullare = primaNoteDaAnnullare;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public List<AccrualTPrimaNota> getPrimaNoteDaAnnullare() {
        return primaNoteDaAnnullare;
    }
}
