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

import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.security.SecurityProperties;
import it.csi.accrual.accrualcommonsrv.service.EnteAccessService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro per la gestione del contesto multi-ente.
 *
 * Legge l'ente dal token JWT (claim 'codice_ente') tramite SecurityContextHelper,
 * cerca l'ente nel database e imposta il contesto in EnteContextHolder.
 *
 * L'header X-Ente-Codice NON è più utilizzato: l'ente deve essere nel JWT.
 */
@Component
@Provider
public class EnteContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EnteContextFilter.class);

    @Autowired
    private EnteAccessService enteAccessService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("EnteContextFilter: Ricevuta richiesta {} {}", requestContext.getMethod(), requestContext.getUriInfo().getPath());

        // Se l'endpoint è pubblico, salto la validazione ente
        if (isPublicEndpoint(requestContext)) {
            LOG.info("EnteContextFilter: Endpoint pubblico {} - salto validazione ente", requestContext.getUriInfo().getPath());
            return;
        }

        // Logga gli header della richiesta
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        LOG.debug("EnteContextFilter: Headers ricevuti: {}", headers);

        // Logga i parametri della query
        MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
        LOG.debug("EnteContextFilter: Parametri query: {}", queryParams);

        // Estrae l'idEnte dal JWT (claim codice_ente)
        Long idEnte = extractIdEnteFromJwt(requestContext);

        if (idEnte != null) {
            LOG.info("EnteContextFilter: Contesto ente impostato correttamente con id: {}", idEnte);
            // Imposta il contesto multi-ente
            EnteContextHolder.setCurrentEnte(idEnte);
        } else {
            LOG.warn("EnteContextFilter: Nessun ente trovato nel JWT. Headers: {}, QueryParams: {}",
                     requestContext.getHeaders(), queryParams);
            // Restituisci errore di autorizzazione
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Utente non autorizzato. Ente non presente nel token JWT.").build());
        }
    }

    /**
     * Verifica se il percorso corrente è un endpoint pubblico
     */
    private boolean isPublicEndpoint(ContainerRequestContext requestContext) {
        if (securityProperties.getPublicEndpoints() == null) {
            return false;
        }

        // Il path da JAX-RS UriInfo è già relativo al context path (es: "/ping")
        String jaxRsPath = requestContext.getUriInfo().getPath();
        if (!jaxRsPath.startsWith("/")) {
            jaxRsPath = "/" + jaxRsPath;
        }

        for (String publicEndpoint : securityProperties.getPublicEndpoints()) {
            // Matching esatto o con pattern (** per wildcard)
            if (jaxRsPath.equals(publicEndpoint) || jaxRsPath.matches(publicEndpoint.replace("**", ".*"))) {
                return true;
            }
            // Prova anche il suffisso (es: /api/v1/ping -> /ping)
            String endpointSuffix = "/" + publicEndpoint.substring(publicEndpoint.lastIndexOf("/") + 1);
            if (jaxRsPath.equals(endpointSuffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOG.info("EnteContextFilter: Invio risposta {} per richiesta {}", responseContext.getStatus(), requestContext.getUriInfo().getPath());
        // Pulisce il contesto multi-ente dopo ogni risposta
        EnteContextHolder.clear();
    }

    /**
     * Estrae l'id dell'ente dal claim JWT.
     * Legge il claim 'codice_ente' dal JWT.
     *
     * @param requestContext Contesto della richiesta
     * @return L'id dell'ente, o null se non trovato
     */
    private Long extractIdEnteFromJwt(ContainerRequestContext requestContext) {
        LOG.debug("EnteContextFilter: Estrazione ente dal JWT");

        // Legge l'ente dal JWT tramite SecurityContextHelper
        String codiceEnteSiac = securityContextHelper.getEnteCodiceUnivoco();
        LOG.debug("EnteContextFilter: Codice ente dal JWT: {}", codiceEnteSiac);

        if (codiceEnteSiac != null) {
            LOG.debug("EnteContextFilter: Ricerca ente con codice fiscale: {}", codiceEnteSiac);
            // Cerca l'ente in ACCRUAL usando il codice ente di SIAC
            var enteOpt = enteAccessService.findEnteByCodiceUnivoco(codiceEnteSiac);

            if (enteOpt.isPresent()) {
                LOG.debug("EnteContextFilter: Ente trovato con id: {}", enteOpt.get().getId());
                return enteOpt.get().getId();
            } else {
                LOG.warn("EnteContextFilter: Nessun ente trovato per il codice univoco: {}", codiceEnteSiac);
            }
        } else {
            LOG.warn("EnteContextFilter: Claim ente (codice_ente) non presente nel JWT");
        }

        return null;
    }
}
