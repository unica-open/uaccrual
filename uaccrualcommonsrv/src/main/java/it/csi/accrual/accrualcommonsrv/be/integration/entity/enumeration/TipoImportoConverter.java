/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoImportoConverter implements AttributeConverter<TipoImporto, String> {

    @Override
    public String convertToDatabaseColumn(TipoImporto tipoImporto) {
        if (tipoImporto == null) {
            return null;
        }
        return tipoImporto.getCode();
    }

    @Override
    public TipoImporto convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoImporto.getByCode(dbData);
    }
}
