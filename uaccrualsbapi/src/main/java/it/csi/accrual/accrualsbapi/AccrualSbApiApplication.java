/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"it.csi.accrual.accrualsbapi", "it.csi.accrual.accrualsbapi.be", "it.csi.accrual.accrualcommonsrv"})
@EntityScan(basePackages = {"it.csi.accrual.accrualsbapi.be.integration.entity", "it.csi.accrual.accrualcommonsrv"})
@EnableJpaRepositories(basePackages = {"it.csi.accrual.accrualsbapi.be.integration.repository", "it.csi.accrual.accrualcommonsrv"})
@EnableJpaAuditing
@EnableAsync
public class AccrualSbApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccrualSbApiApplication.class, args);
    }

}
