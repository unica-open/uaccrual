/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import it.csi.accrual.accrualauthsbapi.be.impl.*;
import it.csi.accrual.accrualauthsbapi.config.GenericExceptionMapper;
import it.csi.accrual.accrualauthsbapi.config.ValidationExceptionMapper;
import it.csi.accrual.accrualauthsbapi.config.ResteasyJacksonConfig;
import java.util.Set;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("api/v1")
public class AuthRestApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(AuthRestApplication.class);

   @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(CredentialsLoginApiImpl.class);
        classes.add(LogoutApiImpl.class);
        classes.add(PasswordResetApiImpl.class);
        classes.add(ServiceLoginApiImpl.class);
        classes.add(TokenLoginApiImpl.class);
        classes.add(ResteasyJacksonConfig.class);
        classes.add(GenericExceptionMapper.class);
        classes.add(ValidationExceptionMapper.class);
        log.info(">>> AuthRestApplication.getClasses() chiamato, classi: " + classes.size());
        return classes;
    }
}
