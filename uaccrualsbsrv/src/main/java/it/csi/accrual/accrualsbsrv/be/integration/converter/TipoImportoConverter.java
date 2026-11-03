/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.converter;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoImportoConverter implements AttributeConverter<TipoImporto, String> {

    @Override
    public String convertToDatabaseColumn(TipoImporto attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public TipoImporto convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoImporto.getByCode(dbData);
    }
}
