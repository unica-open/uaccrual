/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.configuration;

import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

/**
 * Configurazioni relative ai client JaxRs necessari per accedere a servizi esterni.
 * 
 */
@Configuration
public class JaxRsClientConfig {
	

  
	@Bean
	public ClientBuilder newClientBuilder() {
		return ClientBuilder.newBuilder()
	    		.connectTimeout(10, TimeUnit.SECONDS);

	}
	
	


}
