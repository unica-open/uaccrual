/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.type;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * UserType specifico per la gestione dell'ENUM TipoImporto di PostgreSQL con Hibernate 6.
 * Utilizza Types.OTHER per gestire il tipo custom 'tipo_importo' di PostgreSQL.
 */
public class TipoImportoUserType implements UserType<TipoImporto> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<TipoImporto> returnedClass() {
        return TipoImporto.class;
    }

    @Override
    public TipoImporto nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (value == null || rs.wasNull()) {
            return null;
        }
        return TipoImporto.valueOf(value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, TipoImporto value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Usa setObject con Types.OTHER per PostgreSQL ENUM
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public boolean equals(TipoImporto x, TipoImporto y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(TipoImporto x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public TipoImporto deepCopy(TipoImporto value) throws HibernateException {
        return value; // gli enum sono immutabili
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(TipoImporto value) throws HibernateException {
        return value;
    }

    @Override
    public TipoImporto assemble(Serializable cached, Object owner) throws HibernateException {
        return (TipoImporto) cached;
    }

    @Override
    public TipoImporto replace(TipoImporto detached, TipoImporto managed, Object owner) throws HibernateException {
        return detached;
    }
}
