/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class StaticOpenApiResource {

    @GetMapping(value = "/openapi.yaml", produces = "application/x-yaml")
    public ResponseEntity<String> getOpenApiSpec() throws IOException {
        Resource resource = new ClassPathResource("META-INF/openapi.yaml");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String spec = resource.getContentAsString(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .body(spec);
    }
}
