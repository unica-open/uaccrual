/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableCaching
@EntityScan(basePackages = {
	"it.csi.accrual.accrualauthsbapi.entity",
	"it.csi.accrual.accrualcommonsrv"
})
@EnableJpaRepositories(basePackages = {
	"it.csi.accrual.accrualauthsbapi.repository",
	"it.csi.accrual.accrualcommonsrv"
})
@ComponentScan(
	basePackages = {
		"it.csi.accrual.accrualauthsbapi",
		"it.csi.accrual.accrualcommonsrv.security",
		"it.csi.accrual.accrualcommonsrv.config"
	},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {
			it.csi.accrual.accrualcommonsrv.security.SecurityConfig.class
		}
	)
)
@OpenAPIDefinition(info = @Info(title = "Accrual Authentication API", version = "1.0.0", description = "API per la gestione dell'autenticazione e autorizzazione degli utenti nel sistema Accrual"))
public class AccrualAuthenticationServiceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(AccrualAuthenticationServiceApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AccrualAuthenticationServiceApplication.class);
	}
}