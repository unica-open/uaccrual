/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.type;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoFase;
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
 * UserType specifico per la gestione dell'ENUM TipoFase di PostgreSQL con Hibernate 6.
 * Utilizza Types.OTHER per gestire il tipo custom 'tipo_fase' di PostgreSQL.
 */
public class TipoFaseUserType implements UserType<TipoFase> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<TipoFase> returnedClass() {
        return TipoFase.class;
    }

    @Override
    public TipoFase nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (value == null || rs.wasNull()) {
            return null;
        }
        return TipoFase.valueOf(value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, TipoFase value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Usa setObject con Types.OTHER per PostgreSQL ENUM
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public boolean equals(TipoFase x, TipoFase y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(TipoFase x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public TipoFase deepCopy(TipoFase value) throws HibernateException {
        return value; // gli enum sono immutabili
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(TipoFase value) throws HibernateException {
        return value;
    }

    @Override
    public TipoFase assemble(Serializable cached, Object owner) throws HibernateException {
        return (TipoFase) cached;
    }

    @Override
    public TipoFase replace(TipoFase detached, TipoFase managed, Object owner) throws HibernateException {
        return detached;
    }
}
