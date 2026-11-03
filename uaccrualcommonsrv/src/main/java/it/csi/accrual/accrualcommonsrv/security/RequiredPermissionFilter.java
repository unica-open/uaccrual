/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RequiredPermissionFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequiredPermissionFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Autowired
    private PermissionHelper permissionHelper;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String permission = null;

        // 1. Check for @RequiredPermission annotation on the method
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            RequiredPermission annotation = method.getAnnotation(RequiredPermission.class);
            if (annotation != null) {
                permission = annotation.value();
            }
        }

        // 2. Fallback: look up in PermissionRegistry by HTTP method + path
        if (permission == null) {
            String httpMethod = requestContext.getMethod();
            String path = requestContext.getUriInfo().getPath();
            // Remove context path prefix: /accrualsbsrv/api/v1/... -> strip to /...
            String apiPath = path.replaceAll("^/[^/]+/api/v1", "");
            permission = PermissionRegistry.getPermission(httpMethod, apiPath);
        }

        if (permission == null) return;

        if (!permissionHelper.hasAzione(permission)) {
            log.warn("Accesso negato a {} - permesso {} non disponibile",
                     requestContext.getUriInfo().getPath(), permission);
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity("Permesso \"" + permission + "\" non disponibile")
                        .build()
            );
        }
    }
}
