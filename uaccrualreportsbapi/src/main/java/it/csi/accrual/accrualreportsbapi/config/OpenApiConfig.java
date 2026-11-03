/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BIRT Report Service API")
                        .version("1.0.0")
                        .description(
                                "Microservizio Spring Boot per la generazione di report BIRT in modalità sincrona e asincrona. " +
                                "Supporta formati: PDF, XLSX, HTML, DOCX (fallback a PDF)"
                        )
                        .contact(new Contact()
                                .name("BIRT Report Service Team")
                                .email("support@example.com")
                                .url("https://example.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                )
                .addTagsItem(new Tag()
                        .name("Health")
                        .description("Health check endpoints")
                )
                .addTagsItem(new Tag()
                        .name("Report - Sync")
                        .description("Synchronous report generation (timeout: 1 minute)")
                )
                .addTagsItem(new Tag()
                        .name("Report - Async")
                        .description("Asynchronous report generation with background processing")
                );
    }
}
