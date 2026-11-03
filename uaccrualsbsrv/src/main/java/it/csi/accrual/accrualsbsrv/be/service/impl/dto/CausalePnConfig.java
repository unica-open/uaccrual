/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl.dto;

// Helper class
public class CausalePnConfig {
    final String codiceCausale;
    final String descrizioneBase;
    final PdcClasse pdcClasse; // Corrisponde alla classe del conto 118
    final SensoConto sensoConto;

    public CausalePnConfig(String codiceCausale, String descrizioneBase, PdcClasse pdcClasse, SensoConto sensoConto) {
        this.codiceCausale = codiceCausale;
        this.descrizioneBase = descrizioneBase;
        this.pdcClasse = pdcClasse;
        this.sensoConto = sensoConto;
    }

    public String getCodiceCausale() {
        return codiceCausale;
    }

    public String getDescrizioneBase() {
        return descrizioneBase;
    }

    public PdcClasse getPdcClasse() {
        return pdcClasse;
    }

    public SensoConto getSensoConto() {
        return sensoConto;
    }

    // Helper enum
    public enum PdcClasse {
        ATTIVO_PATRIMONIALE("ATTIVO_PATRIMONIALE"),
        PASSIVO_PATRIMONIALE("PASSIVO_PATRIMONIALE"),
        COSTI_E_ONERI("COSTI_E_ONERI"),
        RICAVI_E_PROVENTI("RICAVI_E_PROVENTI");

        private final String codice;

        PdcClasse(String codice) {
            this.codice = codice;
        }
        
        public String getCodice() { return codice; }
    }
    // Helper enum
    public enum SensoConto {
        DARE,
        AVERE
    }
}
