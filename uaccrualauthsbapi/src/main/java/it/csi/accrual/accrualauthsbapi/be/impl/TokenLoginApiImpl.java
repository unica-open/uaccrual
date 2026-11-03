/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import it.csi.accrual.accrualauthsbapi.be.TokenLoginApi;
import it.csi.accrual.accrualauthsbapi.dto.TokenLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TODO: Implementare validazione token OIDC esterno - EXT_TOKEN
 */
@Component
@Slf4j
public class TokenLoginApiImpl implements TokenLoginApi {

	@Override
	public Response loginWithExternalToken(@Valid TokenLoginRequest body,
			@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {

		log.info("Tentativo di login con token OIDC esterno - TODO: implementare");

		// TODO: Implementare logica EXT_TOKEN
		// 1. Validare token OIDC con OpenIdValidator
		// 2. Estrarre CF/informazioni utente dal token
		// 3. Cercare utente nel DB
		// 4. Generare JWT

		return Response.status(Response.Status.NOT_IMPLEMENTED)
			.entity("Endpoint non ancora implementato - EXT_TOKEN")
			.build();
	}
}
