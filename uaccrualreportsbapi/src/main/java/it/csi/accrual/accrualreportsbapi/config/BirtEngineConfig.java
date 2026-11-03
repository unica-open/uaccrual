/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BirtEngineConfig {

    @Value("${birt.render.temp.dir:${java.io.tmpdir}/birt-processing}")
    private String tempDir;

    @Bean
    public ReportEngine reportEngine() throws EngineException {
        log.info("Initializing BIRT Report Engine v4.18...");

        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setLogConfig(tempDir, java.util.logging.Level.WARNING);
        engineConfig.setBIRTHome(tempDir);

        ReportEngine engine = new ReportEngine(engineConfig);

        log.info("BIRT Report Engine v4.18 initialized successfully");
        return engine;
    }
}
