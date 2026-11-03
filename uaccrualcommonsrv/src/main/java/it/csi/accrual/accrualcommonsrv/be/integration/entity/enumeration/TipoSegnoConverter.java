/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoSegnoConverter implements AttributeConverter<TipoSegno, String> {

    @Override
    public String convertToDatabaseColumn(TipoSegno tipoSegno) {
        if (tipoSegno == null) {
            return null;
        }
        return tipoSegno.getCode();
    }

    @Override
    public TipoSegno convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TipoSegno.getByCode(dbData);
    }
}
