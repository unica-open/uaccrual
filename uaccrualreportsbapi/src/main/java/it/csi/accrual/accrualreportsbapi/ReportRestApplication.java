/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi;

import java.util.Set;

import it.csi.accrual.accrualcommonsrv.filter.EnteContextFilter;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("api/v1")
public class ReportRestApplication extends Application {
//
//    @Override
//    public Set<Class<?>> getClasses() {
//        Set<Class<?>> resources = new java.util.HashSet<>();
//        resources.add(EnteContextFilter.class);
//        return resources;
//    }
}