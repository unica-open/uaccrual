/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@SpringBootApplication
@ComponentScan(basePackages = {
    "it.csi.accrual.accrualsbsrv",
    "it.csi.accrual.accrualcommonsrv"
})
@EntityScan(basePackages = {"it.csi.accrual.accrualsbsrv", "it.csi.accrual.accrualcommonsrv"})
@EnableJpaRepositories(basePackages = {"it.csi.accrual.accrualsbsrv.be.integration.repository", "it.csi.accrual.accrualcommonsrv"})
@EnableRetry
@EnableJpaAuditing
@EnableAsync
public class AccrualSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccrualSpringBootApplication.class, args);
    }
}
