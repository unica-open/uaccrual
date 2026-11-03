/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private static final Logger log = LoggerFactory.getLogger(SecurityProperties.class);
    private List<String> publicEndpoints = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("SecurityProperties initialized");
        if (publicEndpoints != null) {
            for(String s : publicEndpoints) {
                log.info("path: " + s);
            }
        }
    }

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }
}