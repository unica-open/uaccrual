/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.security;

import java.util.HashMap;
import java.util.Map;

public class PermissionRegistry {

    private static final Map<String, String> ENDPOINT_PERMISSIONS = new HashMap<>();

    static {
        // Prima Nota
        reg("GET",    "/primanota/check-incomplete", "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/incomplete",        "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/causali",           "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/fonti",             "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/stati",              "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/tipi",               "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/export",             "OP-GEN-ricPrimaNota");
        reg("POST",   "/primanota/search",             "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/.*/conti",           "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/.*/totali",          "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/.*/attributi",       "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/.*/collegate",       "OP-GEN-ricPrimaNota");
        reg("GET",    "/primanota/.*/.*",              "OP-GEN-ricPrimaNota");
        reg("POST",   "/primanota/.*/annulla",         "OP-SPE-annPrimaNota");
        reg("POST",   "/primanota/.*/riapri",          "OP-SPE-riapriPrimaNota");
        reg("POST",   "/primanota/.*/valida",          "OP-SPE-validaPrimaNota");
        reg("POST",   "/primanota/valida-massiva",     "OP-SPE-validaPrimaNota");
        reg("POST",   "/primanota",                    "OP-SPE-addPrimaNota");
        reg("PUT",    "/primanota/.*/.*",              "OP-SPE-updPrimaNota");

        // PDC
        reg("POST",   "/pdc/search",                   "OP-GEN-ricPdc");
        reg("POST",   "/pdc/searchTree",               "OP-GEN-ricPdc");
        reg("GET",    "/pdc/classi",                   "OP-GEN-ricPdc");
        reg("GET",    "/pdc/tipi",                     "OP-GEN-ricPdc");
        reg("GET",    "/pdc/stati",                    "OP-GEN-ricPdc");
        reg("GET",    "/pdc/famiglie",                 "OP-GEN-ricPdc");
        reg("GET",    "/pdc/relazioni",                "OP-GEN-ricPdc");
        reg("GET",    "/pdc/export",                   "OP-GEN-ricPdc");
        reg("GET",    "/pdc/.*/.*/.*",                 "OP-GEN-ricPdc");
        reg("GET",    "/pdc",                          "OP-GEN-ricPdc");
        reg("POST",   "/pdc",                          "OP-SPE-addPdc");
        reg("PUT",    "/pdc/.*/.*/.*",                 "OP-SPE-updPdc");
        reg("DELETE", "/pdc/.*/.*/.*",                 "OP-SPE-delPdc");

        // Causale
        reg("GET",    "/causali/tipi",                 "OP-GEN-ricCausale");
        reg("GET",    "/causali/stati",                "OP-GEN-ricCausale");
        reg("GET",    "/causali/eventi",               "OP-GEN-ricCausale");
        reg("GET",    "/causali/.*/conti",             "OP-GEN-ricCausale");
        reg("GET",    "/causali/.*",                   "OP-GEN-ricCausale");
        reg("GET",    "/causali",                      "OP-GEN-ricCausale");
        reg("POST",   "/causali",                      "OP-SPE-addCausale");
        reg("PUT",    "/causali/.*",                   "OP-SPE-updCausale");
        reg("POST",   "/causali/.*/valida",            "OP-SPE-aggCausale");
        reg("DELETE", "/causali/.*",                   "OP-SPE-delCausale");

        // Modello MIF
        reg("GET",    "/modellomif/anni",              "OP-GEN-updModMF");
        reg("GET",    "/modellomif/dati",              "OP-GEN-updModMF");

        // Flussi (accrualsbsrv)
        reg("POST",   "/flussi/upload",                "OP-GEN-uploadFile");
        reg("GET",    "/flussi/search",                "OP-GEN-ricFile");
        reg("GET",    "/flussi/fonti",                 "OP-GEN-ricFile");
        reg("GET",    "/flussi/.*/elabora",            "OP-SPE-elaboraFile");
        reg("GET",    "/flussi/.*/download",           "OP-GEN-ricFile");
        reg("GET",    "/flussi/.*/export-results",     "OP-GEN-ricFile");
        reg("GET",    "/flussi/.*/process-status",     "OP-GEN-ricFile");
        reg("GET",    "/flussi/.*",                    "OP-GEN-ricFile");
        reg("DELETE", "/flussi/.*",                    "OP-SPE-delFlusso");
    }

    private static void reg(String method, String pathPattern, String permission) {
        ENDPOINT_PERMISSIONS.put(method + ":" + pathPattern, permission);
    }

    public static String getPermission(String httpMethod, String path) {
        String key = httpMethod + ":" + path;
        String direct = ENDPOINT_PERMISSIONS.get(key);
        if (direct != null) return direct;

        for (Map.Entry<String, String> entry : ENDPOINT_PERMISSIONS.entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            if (parts.length != 2) continue;
            if (!parts[0].equals(httpMethod)) continue;
            if (match(parts[1], path)) return entry.getValue();
        }
        return null;
    }

    private static boolean match(String pattern, String path) {
        if (pattern.equals(path)) return true;
        String regex = pattern
            .replace(".", "\\.")
            .replace(".*", "[^/]+");
        return path.matches(regex);
    }
}
