/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoDatoConverter implements AttributeConverter<TipoDato, String> {

    @Override
    public String convertToDatabaseColumn(TipoDato tipoDato) {
        if (tipoDato == null) {
            return null;
        }
        return tipoDato.getCode();
    }

    @Override
    public TipoDato convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoDato.getByCode(dbData);
    }
}
