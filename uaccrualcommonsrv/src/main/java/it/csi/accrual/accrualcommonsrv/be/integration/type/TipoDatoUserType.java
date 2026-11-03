/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.type;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoDato;
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
 * UserType specifico per la gestione dell'ENUM TipoDato di PostgreSQL con Hibernate 6.
 * Utilizza Types.OTHER per gestire il tipo custom 'tipo_dato' di PostgreSQL.
 */
public class TipoDatoUserType implements UserType<TipoDato> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<TipoDato> returnedClass() {
        return TipoDato.class;
    }

    @Override
    public TipoDato nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (value == null || rs.wasNull()) {
            return null;
        }
        return TipoDato.getByCode(value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, TipoDato value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Usa setObject con Types.OTHER per PostgreSQL ENUM
            // Per TipoDato usiamo il code invece di name() perché l'enum usa codici lowercase
            st.setObject(index, value.getCode(), Types.OTHER);
        }
    }

    @Override
    public boolean equals(TipoDato x, TipoDato y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(TipoDato x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public TipoDato deepCopy(TipoDato value) throws HibernateException {
        return value; // gli enum sono immutabili
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(TipoDato value) throws HibernateException {
        return value;
    }

    @Override
    public TipoDato assemble(Serializable cached, Object owner) throws HibernateException {
        return (TipoDato) cached;
    }

    @Override
    public TipoDato replace(TipoDato detached, TipoDato managed, Object owner) throws HibernateException {
        return detached;
    }
}
