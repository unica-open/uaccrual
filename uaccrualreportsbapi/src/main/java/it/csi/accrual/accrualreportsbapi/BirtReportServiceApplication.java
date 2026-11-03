/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = {"it.csi.accrual.accrualreportsbapi", "it.csi.accrual.accrualcommonsrv", "it.csi.accrual.accrualreportsbapi.be.impl"})
@EnableJpaRepositories(basePackages = {"it.csi.accrual.accrualreportsbapi.repository", "it.csi.accrual.accrualcommonsrv"})
@EntityScan(basePackages = {"it.csi.accrual.accrualreportsbapi.entity", "it.csi.accrual.accrualcommonsrv"})
@OpenAPIDefinition(info = @Info(title = "Accrual Report Service API", version = "1.0.0", description = "API per la generazione di report BIRT sincroni e asincroni"))
public class BirtReportServiceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(BirtReportServiceApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BirtReportServiceApplication.class);
	}
}