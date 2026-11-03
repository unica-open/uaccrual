/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.eclipse.birt.report.model.metadata;

import org.eclipse.birt.report.model.api.extension.IEncryptionHelper;

/**
 * Override no-op di SimpleEncryptionHelper di BIRT.
 *
 * Il classloader carica le classi del jar applicativo prima di quelle
 * delle dipendenze, quindi questa classe vince su quella originale in
 * org.eclipse.birt.runtime*.jar senza alcuna configurazione aggiuntiva.
 *
 * Problema risolto: l'implementazione originale fa Base64.decode() sulla
 * password memorizzata nel .rptdesign, causando "Illegal base64 character"
 * se la password non e' stata cifrata con il meccanismo BIRT.
 * Questa versione restituisce i valori invariati: le credenziali reali
 * vengono iniettate a runtime da RenderService.configureOdaDataSourceOnDesign().
 */
public class SimpleEncryptionHelper implements IEncryptionHelper {

    private static final SimpleEncryptionHelper instance = new SimpleEncryptionHelper();

    public static SimpleEncryptionHelper getInstance() {
        return instance;
    }

    @Override
    public String encrypt(String value) {
        return value;
    }

    @Override
    public String decrypt(String value) {
        return value;
    }
}
