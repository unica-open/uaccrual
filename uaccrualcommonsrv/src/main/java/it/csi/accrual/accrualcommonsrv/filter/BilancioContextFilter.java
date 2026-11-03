/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualcommonsrv.be.util.BilancioContextHolder;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro per la gestione del contesto anno di bilancio.
 *
 * Legge l'anno di bilancio dal token JWT (claim 'anno_bilancio')
 * tramite SecurityContextHelper e imposta il contesto in BilancioContextHolder.
 *
 * L'header X-Anno-Bilancio è DEPRECATO: l'anno deve essere nel JWT.
 */
@Component
@Provider
public class BilancioContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BilancioContextFilter.class);

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.debug("BilancioContextFilter: Ricevuta richiesta {} {}",
                 requestContext.getMethod(), requestContext.getUriInfo().getPath());

        // Estrae l'annoBilancio dal JWT
        Integer annoBilancio = extractAnnoBilancioFromJwt();

        if (annoBilancio != null) {
            LOG.info("BilancioContextFilter: Anno bilancio impostato a: {}", annoBilancio);
            BilancioContextHolder.setCurrentAnnoBilancio(annoBilancio);
        } else {
            LOG.warn("BilancioContextFilter: Claim 'anno_bilancio' non presente nel JWT. Richiesta: {} {}",
                     requestContext.getMethod(), requestContext.getUriInfo().getPath());
            // Anno bilancio obbligatorio: restituisce errore
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Anno di bilancio non presente nel token JWT. Effettuare il login con un bilancio valido.").build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOG.debug("BilancioContextFilter: Pulizia contesto anno bilancio");
        BilancioContextHolder.clear();
    }

    /**
     * Estrae l'anno di bilancio dal claim JWT tramite SecurityContextHelper.
     */
    private Integer extractAnnoBilancioFromJwt() {
        LOG.debug("BilancioContextFilter: Estrazione anno bilancio dal JWT");

        Integer annoBilancio = securityContextHelper.getAnnoBilancio();
        LOG.debug("BilancioContextFilter: Anno bilancio dal JWT: {}", annoBilancio);

        return annoBilancio;
    }
}
