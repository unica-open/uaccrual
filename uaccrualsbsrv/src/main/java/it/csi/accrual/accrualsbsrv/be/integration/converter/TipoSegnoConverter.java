/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.converter;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoSegnoConverter implements AttributeConverter<TipoSegno, String> {

    @Override
    public String convertToDatabaseColumn(TipoSegno attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public TipoSegno convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoSegno.getByCode(dbData);
    }
}
