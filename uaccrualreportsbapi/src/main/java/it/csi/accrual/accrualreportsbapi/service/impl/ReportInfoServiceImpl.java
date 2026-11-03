/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service.impl;

import it.csi.accrual.accrualreportsbapi.dto.ReportInfo;
import it.csi.accrual.accrualreportsbapi.dto.ReportParameter;
import it.csi.accrual.accrualreportsbapi.dto.ParameterOption;
import it.csi.accrual.accrualreportsbapi.service.ReportInfoService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;
import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementazione service per estrazione metadata report dai template BIRT.
 */
@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

    @Value("${birt.reports.directory:./reports}")
    private String reportsDirectory;

    private final ReportEngine reportEngine;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private static final Pattern DATASET_ROW_EXPR = Pattern.compile("(?:dataSetRow|row)\\[\"([^\"]+)\"\\]");

    // Cache semplice per le etichette dei parametri (nome_file -> mappa nome -> etichetta)
    private final Map<String, Map<String, String>> parameterLabelsCache = new ConcurrentHashMap<>();

    public ReportInfoServiceImpl(ReportEngine reportEngine, DataSource dataSource) {
        this.reportEngine = reportEngine;
        this.dataSource = dataSource;
        this.jdbcTemplate = (dataSource != null) ? new JdbcTemplate(dataSource) : null;
    }

    @Override
    public ReportInfo getReportInfo(String rptdesign) {
        log.info("========== INIZIO ESTRAZIONE METADATA REPORT ==========");
        log.info("Template richiesto: {}", rptdesign);
        log.info("Reports directory configurata: {}", reportsDirectory);

        // Costruisci percorso completo
        String templatePath = Paths.get(reportsDirectory, rptdesign).toString();
        log.info("Percorso completo template: {}", templatePath);

        // Verifica esistenza file
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            log.error("Template non trovato: {}", templatePath);
            log.error("File richiesto: {}", rptdesign);
            throw new IllegalArgumentException("Template non trovato: " + rptdesign + ". Verificare che il file esista nella directory dei report.");
        }

        try {
            log.info("Apertura template BIRT: {}", templatePath);

            // Apri il design del report
            IReportRunnable design = reportEngine.openReportDesign(templatePath);

            if (design == null) {
                log.error("Impossibile aprire il template: {}", rptdesign);
                log.error("Il file esiste ma non può essere aperto come design BIRT valido");
                throw new RuntimeException("Impossibile aprire il template: " + rptdesign + ". Il file potrebbe essere corrotto o non essere un valido design BIRT.");
            }

            log.info("Template aperto con successo");

            // Estrai parametri
            List<ReportParameter> parametri = estraiParametri(design);

            // Costruisci response
            ReportInfo info = new ReportInfo();
            info.setTemplate(rptdesign);
            info.setParametri(parametri);

            log.info("Estratti {} parametri dal template {}", parametri.size(), rptdesign);
            log.info("========== FINE ESTRAZIONE METADATA REPORT ==========");

            return info;

        } catch (IllegalArgumentException e) {
            // Propaga eccezioni per template non trovato
            log.error("Errore: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Errore imprevisto nell'estrazione dei parametri: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nell'estrazione dei parametri: " + e.getMessage() + ". Contattare l'amministratore di sistema.");
        }
    }

    @Override
    public Map<String, String> getParameterLabels(String rptdesign) {
        // Controlla cache
        Map<String, String> cached = parameterLabelsCache.get(rptdesign);
        if (cached != null) {
            log.debug("Etichette parametri per {} restituite da cache", rptdesign);
            return cached;
        }

        log.info("Caricamento etichette parametri da template: {}", rptdesign);

        String templatePath = Paths.get(reportsDirectory, rptdesign).toString();
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            log.warn("Template non trovato per estrazione etichette: {}", templatePath);
            return Collections.emptyMap();
        }

        try {
            IReportRunnable design = reportEngine.openReportDesign(templatePath);
            if (design == null) {
                log.warn("Impossibile aprire template per estrazione etichette: {}", rptdesign);
                return Collections.emptyMap();
            }

            ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();
            if (designHandle == null) {
                return Collections.emptyMap();
            }

            Map<String, String> labels = new LinkedHashMap<>();
            estraiEtichetteParametri(designHandle, labels);
            parameterLabelsCache.put(rptdesign, labels);
            log.info("Estratte {} etichette da {}", labels.size(), rptdesign);

            return labels;

        } catch (Exception e) {
            log.error("Errore estrazione etichette parametri da {}: {}", rptdesign, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Estrae ricorsivamente nome → etichetta per tutti i parametri VISIBILI.
     */
    private void estraiEtichetteParametri(ReportDesignHandle designHandle, Map<String, String> labels) {
        SlotHandle parametersSlot = designHandle.getParameters();
        if (parametersSlot == null) return;

        int count = parametersSlot.getCount();
        for (int i = 0; i < count; i++) {
            try {
                Object element = parametersSlot.get(i);
                if (element == null) continue;

                String elementClassName = element.getClass().getName();
                if (elementClassName != null && elementClassName.contains("ParameterGroupHandle")) {
                    // Recupera nome gruppo
                    String groupName = (String) invokeMethod(element, "getStringProperty", "promptText");
                    if (groupName == null || groupName.trim().isEmpty()) {
                        groupName = (String) invokeMethod(element, "getName");
                        if (groupName == null || groupName.trim().isEmpty()) {
                            groupName = null;
                        }
                    }
                    // Gruppo: estrae i parametri annidati con contesto gruppo
                    Object groupSlot = invokeMethod(element, "getParameters");
                    if (groupSlot instanceof SlotHandle) {
                        SlotHandle slotHandle = (SlotHandle) groupSlot;
                        int subCount = slotHandle.getCount();
                        for (int j = 0; j < subCount; j++) {
                            Object subElement = slotHandle.get(j);
                            if (subElement instanceof ScalarParameterHandle) {
                                aggiungiEtichettaSeVisibile((ScalarParameterHandle) subElement, labels, groupName);
                            }
                        }
                    }
                } else if (element instanceof ScalarParameterHandle) {
                    aggiungiEtichettaSeVisibile((ScalarParameterHandle) element, labels, null);
                }
            } catch (Exception e) {
                log.debug("Errore estrazione etichetta parametro {}: {}", i, e.getMessage());
            }
        }
    }

    /**
     * Aggiunge nome → etichetta solo se il parametro non è hidden.
     */
    private void aggiungiEtichettaSeVisibile(ScalarParameterHandle handle, Map<String, String> labels, String groupName) {
        try {
            if (Boolean.TRUE.equals(handle.isHidden())) return;

            String name = handle.getName();
            if (name == null || name.trim().isEmpty()) return;

            String promptText = handle.getPromptText();
            String label = (promptText != null && !promptText.trim().isEmpty()) ? promptText : name;
            if (groupName != null) {
                label = groupName + " - " + label;
            }
            labels.put(name, label);
        } catch (Exception e) {
            log.debug("Errore in aggiungiEtichettaSeVisibile: {}", e.getMessage());
        }
    }

    /**
     * Estrae i parametri da un design BIRT.
     */
    private List<ReportParameter> estraiParametri(IReportRunnable design) {
        List<ReportParameter> parametri = new ArrayList<>();

        try {
            // Ottieni il design handle per accedere ai parametri
            ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();

            if (designHandle == null) {
                log.warn("Impossibile ottenere il DesignHandle dal design - il template potrebbe non avere parametri");
                return parametri;
            }

            log.debug("DesignHandle ottenuto con successo");

            // Estrai i parametri definiti nel report
            SlotHandle parametersSlot = designHandle.getParameters();

            if (parametersSlot == null) {
                log.info("Nessuno slot parametri trovato nel template - il report non ha parametri definiti");
                return parametri;
            }

            int count = parametersSlot.getCount();
            log.info("Trovati {} parametri BIRT nel template", count);

            for (int i = 0; i < count; i++) {
                try {
                    Object element = parametersSlot.get(i);

                    // Gestisci i ParameterGroupHandle (contenitori di parametri)
                    String elementClassName = element != null ? element.getClass().getName() : null;
                    if (elementClassName != null && elementClassName.contains("ParameterGroupHandle")) {
                        log.info("Elemento alla posizione {} è un ParameterGroupHandle - estraggo i parametri annidiati", i);
                        // Recupera il nome del gruppo (prima promptText, fallback getName)
                        String groupName = (String) invokeMethod(element, "getStringProperty", "promptText");
                        if (groupName == null || groupName.trim().isEmpty()) {
                            groupName = (String) invokeMethod(element, "getName");
                            if (groupName == null || groupName.trim().isEmpty()) {
                                groupName = "Gruppo " + i;
                            }
                        }
                        // Estrai i parametri dal gruppo
                        estraiParametriDaGruppo(element, parametri, i, groupName, designHandle);
                        continue;
                    }

                    // Processa solo gli ScalarParameterHandle (parametri effettivi)
                    if (element instanceof ScalarParameterHandle) {
                        ScalarParameterHandle scalarHandle = (ScalarParameterHandle) element;
                        ReportParameter parametro = new ReportParameter();

                        // Estrai nome usando il metodo diretto invece di reflection
                        String paramName = scalarHandle.getName();
                        if (paramName == null || paramName.trim().isEmpty()) {
                            log.warn("Parametro alla posizione {} ha nome nullo o vuoto - saltato", i);
                            continue;
                        }

                        parametro.setNome(paramName);
                        log.info("  Parametro {}: {}", i, paramName);

                        // Estrai etichetta (prompt text)
                        String promptText = scalarHandle.getPromptText();
                        parametro.setEtichetta(promptText != null && !promptText.trim().isEmpty() ? promptText : paramName);

                        // Estrai tipo usando il metodo diretto
                        // Nota: getDataType() può restituire sia String che Integer a seconda della versione BIRT
                        Object dataTypeObj = scalarHandle.getDataType();
                        Integer dataType = 2; // Default a STRING

                        if (dataTypeObj instanceof Integer) {
                            dataType = (Integer) dataTypeObj;
                        } else if (dataTypeObj instanceof String) {
                            dataType = mapBirtTypeFromString((String) dataTypeObj);
                        }

                        String tipo = mapBirtTypeToParamType(dataType);

                        // Sovrascrivi tipo in base al controlType BIRT
                        String controlType = scalarHandle.getControlType();
                        if (controlType != null) {
                            switch (controlType) {
                                case "list-box":
                                    tipo = "COMBO";
                                    break;
                                case "radio-button":
                                    tipo = "RADIO";
                                    break;
                                case "check-box":
                                    tipo = "CHECKBOX";
                                    break;
                            }
                        }

                        parametro.setTipo(ReportParameter.TipoEnum.valueOf(tipo));
                        log.info("    Tipo: {} (dataType={})", tipo, dataType);

                        // Estrai obbligatorio
                        Boolean required = scalarHandle.isRequired();
                        parametro.setObbligatorio(required);
                        log.info("    Obbligatorio: {}", required);

                        // Estrai valore di default
                        Object defaultValue = scalarHandle.getDefaultValue();
                        if (defaultValue != null) {
                            parametro.setValoreDefault(defaultValue);
                            log.info("    Default: {}", defaultValue);
                        }

                        // Estrai HIDDEN da BIRT
                        Boolean hidden = scalarHandle.isHidden();
                        parametro.setHidden(hidden);
                        log.info("    Hidden: {}", hidden);

                        // Estrai valori possibili per parametri COMBO/RADIO (dinamici da dataSet)
                        try {
                            List<ParameterOption> options = extractOptions(designHandle, scalarHandle, null);
                            if (options != null && !options.isEmpty()) {
                                parametro.setValoriPossibili(options);
                                log.info("    Caricate {} opzioni per il parametro {}", options.size(), paramName);
                            }
                        } catch (Exception e) {
                            log.warn("Impossibile estrarre le opzioni per il parametro {}: {}", paramName, e.getMessage());
                        }

                        parametri.add(parametro);
                        log.info("  Parametro {} aggiunto con successo", paramName);
                    } else {
                        log.warn("Elemento alla posizione {} non è uno ScalarParameterHandle (tipo: {}) - saltato", i, elementClassName);
                    }
                } catch (Exception e) {
                    log.error("Errore nell'estrazione del parametro alla posizione {}: {}", i, e.getMessage(), e);
                    // Continua con il prossimo parametro invece di fallire completamente
                }
            }

            log.info("Estrazione completata: {} parametri estratti con successo", parametri.size());

        } catch (Exception e) {
            log.error("Errore generale nell'estrazione dei parametri BIRT: {}", e.getMessage(), e);
            // Non lanciare eccezione, restituisci lista vuota o parzialmente popolata
        }

        return parametri;
    }

    /**
     * Estrae i parametri annidati da un ParameterGroupHandle.
     */
    private void estraiParametriDaGruppo(Object groupHandle, List<ReportParameter> parametri, int groupIndex, String groupName, ReportDesignHandle designHandle) {
        try {
            log.info("  [DEBUG] Inizio estrazione parametri dal gruppo {}", groupIndex);
            log.info("  [DEBUG] Tipo groupHandle: {}", groupHandle.getClass().getName());
            
            // Usa reflection per ottenere il metodo getParameters() dal gruppo
            Object groupParametersSlot = invokeMethod(groupHandle, "getParameters");
            
            log.info("  [DEBUG] groupParametersSlot: {}", groupParametersSlot);

            if (groupParametersSlot == null) {
                log.warn("Il ParameterGroupHandle alla posizione {} non ha parametri annidati", groupIndex);
                return;
            }
            
            log.info("  [DEBUG] Tipo groupParametersSlot: {}", groupParametersSlot.getClass().getName());

            // Casta a SlotHandle per usare direttamente il metodo get(int)
            if (!(groupParametersSlot instanceof SlotHandle)) {
                log.error("  [DEBUG] groupParametersSlot non è uno SlotHandle!");
                return;
            }
            
            SlotHandle slotHandle = (SlotHandle) groupParametersSlot;
            int count = slotHandle.getCount();
            
            log.info("  Trovati {} parametri nel gruppo alla posizione {}", count, groupIndex);

            String previousDataSetName = null;
            String previousValueExpr = null;

            for (int j = 0; j < count; j++) {
                try {
                    log.info("  [DEBUG] Estrazione parametro {} dal gruppo", j);
                    Object paramElement = slotHandle.get(j);
                    log.info("  [DEBUG] paramElement: {}", paramElement);
                    log.info("  [DEBUG] Tipo paramElement: {}", paramElement != null ? paramElement.getClass().getName() : "null");
                    
                    if (paramElement instanceof ScalarParameterHandle) {
                        log.info("  [DEBUG] paramElement è ScalarParameterHandle");
                        ScalarParameterHandle scalarHandle = (ScalarParameterHandle) paramElement;
                        ReportParameter parametro = new ReportParameter();

                        // Estrai nome
                        String paramName = scalarHandle.getName();
                        log.info("  [DEBUG] paramName: {}", paramName);
                        
                        if (paramName == null || paramName.trim().isEmpty()) {
                            log.warn("  Parametro nel gruppo alla posizione {} ha nome nullo - saltato", j);
                            continue;
                        }

                        parametro.setNome(paramName);
                        log.info("    Parametro {}.{}: {}", groupIndex, j, paramName);

                        // Estrai etichetta
                        String promptText = scalarHandle.getPromptText();
                        parametro.setEtichetta(promptText != null && !promptText.trim().isEmpty() ? promptText : paramName);

                        // Estrai tipo
                        Object dataTypeObj = scalarHandle.getDataType();
                        Integer dataType = 2;
                        
                        if (dataTypeObj instanceof Integer) {
                            dataType = (Integer) dataTypeObj;
                        } else if (dataTypeObj instanceof String) {
                            dataType = mapBirtTypeFromString((String) dataTypeObj);
                        }

                        String tipo = mapBirtTypeToParamType(dataType);

                        // Sovrascrivi tipo in base al controlType BIRT
                        String controlType = scalarHandle.getControlType();
                        if (controlType != null) {
                            switch (controlType) {
                                case "list-box":
                                    tipo = "COMBO";
                                    break;
                                case "radio-button":
                                    tipo = "RADIO";
                                    break;
                                case "check-box":
                                    tipo = "CHECKBOX";
                                    break;
                            }
                        }

                        parametro.setTipo(ReportParameter.TipoEnum.valueOf(tipo));
                        log.info("      Tipo: {} (dataType={})", tipo, dataType);

                        // Estrai obbligatorio
                        Boolean required = scalarHandle.isRequired();
                        parametro.setObbligatorio(required);
                        log.info("      Obbligatorio: {}", required);

                        // Estrai valore di default
                        Object defaultValue = scalarHandle.getDefaultValue();
                        if (defaultValue != null) {
                            parametro.setValoreDefault(defaultValue);
                            log.info("      Default: {}", defaultValue);
                        }

                        // Estrai HIDDEN
                        Boolean hidden = scalarHandle.isHidden();
                        parametro.setHidden(hidden);
                        log.info("      Hidden: {}", hidden);

                        // Determine parent lineage for cascading parameters
                        String currentDataSetName = scalarHandle.getDataSetName();
                        String parentValueExpr = null;
                        if (currentDataSetName != null && previousDataSetName != null
                                && currentDataSetName.equals(previousDataSetName)) {
                            parentValueExpr = previousValueExpr;
                        }

                        // Estrai valori possibili per parametri COMBO/RADIO (dinamici da dataSet)
                        try {
                            List<ParameterOption> options = extractOptions(designHandle, scalarHandle, parentValueExpr);
                            if (options != null && !options.isEmpty()) {
                                parametro.setValoriPossibili(options);
                                log.info("      Caricate {} opzioni per il parametro {}", options.size(), paramName);
                            }
                        } catch (Exception e) {
                            log.warn("Impossibile estrarre le opzioni per il parametro {}: {}", paramName, e.getMessage());
                        }

                        // Track lineage for next parameter in group
                        if (currentDataSetName != null) {
                            previousDataSetName = currentDataSetName;
                            previousValueExpr = scalarHandle.getValueExpr();
                        }

                        // Imposta il gruppo di appartenenza
                        parametro.setGruppo(groupName);
                        log.info("      Gruppo: {}", groupName);

                        parametri.add(parametro);
                        log.info("    Parametro {}.{} aggiunto con successo", groupIndex, j);
                    } else {
                        log.warn("  [DEBUG] paramElement NON è ScalarParameterHandle, salto");
                    }
                } catch (Exception e) {
                    log.error("Errore nell'estrazione del parametro annidato alla posizione {}: {}", j, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Errore nell'estrazione dei parametri dal gruppo alla posizione {}: {}", groupIndex, e.getMessage(), e);
        }
    }

    /**
     * Invoca un metodo senza argomenti usando reflection.
     * Restituisce null se il metodo non esiste o non può essere invocato.
     */
    private Object invokeMethod(Object obj, String methodName) {
        try {
            return obj.getClass().getMethod(methodName).invoke(obj);
        } catch (NoSuchMethodException e) {
            // Metodo non trovato sull'oggetto - normale per alcuni metodi BIRT opzionali
            log.trace("Metodo {} non trovato su {}: {}", methodName, obj.getClass().getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.debug("Errore nell'invocazione del metodo {}: {}", methodName, e.getMessage());
            return null;
        }
    }

    /**
     * Invoca un metodo con un argomento usando reflection.
     * Restituisce null se il metodo non esiste o non può essere invocato.
     */
    private Object invokeMethod(Object obj, String methodName, Object arg) {
        try {
            return obj.getClass().getMethod(methodName, arg.getClass()).invoke(obj, arg);
        } catch (NoSuchMethodException e) {
            log.trace("Metodo {} non trovato su {}: {}", methodName, obj.getClass().getName(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.debug("Errore nell'invocazione del metodo {}: {}", methodName, e.getMessage());
            return null;
        }
    }

    /**
     * Estrae le opzioni per un parametro BIRT (dinamico da dataSet o statico da selectionList).
     */
    private List<ParameterOption> extractOptions(ReportDesignHandle designHandle, ScalarParameterHandle scalarHandle, String parentValueExpr) {
        String dataSetName = scalarHandle.getDataSetName();
        if (dataSetName != null && !dataSetName.isEmpty()) {
            String valueExpr = scalarHandle.getValueExpr();
            String labelExpr = scalarHandle.getLabelExpr();
            return extractDataSetOptions(designHandle, dataSetName, valueExpr, labelExpr, parentValueExpr);
        }

        // Prova lista di selezione statica (inline nel rptdesign)
        List<ParameterOption> staticOptions = extractStaticOptions(scalarHandle);
        if (!staticOptions.isEmpty()) {
            return staticOptions;
        }

        return Collections.emptyList();
    }

    /**
     * Estrae le opzioni da una selectionList statica (valori definiti inline nel rptdesign).
     */
    private List<ParameterOption> extractStaticOptions(ScalarParameterHandle scalarHandle) {
        List<ParameterOption> options = new ArrayList<>();
        try {
            Object choicesObj = invokeMethod(scalarHandle, "choiceIterator");
            if (choicesObj instanceof Iterator) {
                Iterator<?> choices = (Iterator<?>) choicesObj;
                while (choices.hasNext()) {
                    Object choice = choices.next();
                    if (choice != null) {
                        Object value = invokeMethod(choice, "getValue");
                        Object label = invokeMethod(choice, "getLabel");
                        if (value != null) {
                            ParameterOption option = new ParameterOption();
                            option.setValore(value);
                            option.setEtichetta(label != null ? String.valueOf(label) : String.valueOf(value));
                            options.add(option);
                        }
                    }
                }
            }
            if (!options.isEmpty()) {
                log.info("    Caricate {} opzioni statiche per il parametro {}", options.size(), scalarHandle.getName());
            }
        } catch (Exception e) {
            log.warn("Impossibile estrarre opzioni statiche per il parametro {}: {}", scalarHandle.getName(), e.getMessage());
        }
        return options;
    }

    /**
     * Esegue la query del dataSet BIRT e mappa i risultati in ParameterOption.
     */
    private List<ParameterOption> extractDataSetOptions(ReportDesignHandle designHandle, String dataSetName, String valueExpr, String labelExpr, String parentValueExpr) {
        if (jdbcTemplate == null) {
            log.warn("DataSource non disponibile - impossibile estrarre opzioni per il dataSet {}", dataSetName);
            return Collections.emptyList();
        }
        if (valueExpr == null || labelExpr == null) {
            log.warn("valueExpr o labelExpr nulli per il dataSet {} - impossibile estrarre opzioni", dataSetName);
            return Collections.emptyList();
        }

        String valueColumn = extractColumnName(valueExpr);
        String labelColumn = extractColumnName(labelExpr);
        if (valueColumn == null || labelColumn == null) {
            log.warn("Impossibile estrarre i nomi colonna dalle espressioni per il dataSet {}: valueExpr={}, labelExpr={}", dataSetName, valueExpr, labelExpr);
            return Collections.emptyList();
        }

        String parentColumn = null;
        if (parentValueExpr != null) {
            parentColumn = extractColumnName(parentValueExpr);
        }

        log.info("Estrazione opzioni per dataSet {}: valueColumn={}, labelColumn={}, parentColumn={}", dataSetName, valueColumn, labelColumn, parentColumn);

        try {
            OdaDataSetHandle dataSetHandle = (OdaDataSetHandle) designHandle.findDataSet(dataSetName);
            if (dataSetHandle == null) {
                log.warn("DataSet {} non trovato nel design", dataSetName);
                return Collections.emptyList();
            }

            String queryText = dataSetHandle.getQueryText();
            if (queryText == null || queryText.trim().isEmpty()) {
                log.warn("Query vuota per il dataSet {}", dataSetName);
                return Collections.emptyList();
            }

            log.info("Esecuzione query per dataSet {}: {}", dataSetName, queryText.replace("\n", " ").replaceAll("\\s+", " ").trim());

            Long enteId = EnteContextHolder.getCurrentEnte();
            if (enteId == null) {
                log.warn("Ente ID non presente nel contesto - impossibile eseguire query per dataSet {}", dataSetName);
                return Collections.emptyList();
            }

            int paramCount = queryText.split("\\?", -1).length - 1;
            Object[] params = Collections.nCopies(paramCount, enteId).toArray();

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(queryText, params);
            List<ParameterOption> options = new ArrayList<>();
            Set<String> seenKeys = new HashSet<>();

            for (Map<String, Object> row : rows) {
                Object valore = row.get(valueColumn);
                if (valore == null) continue;
                Object dipendeDa = parentColumn != null ? row.get(parentColumn) : null;
                String key = String.valueOf(valore) + "|" + (dipendeDa != null ? String.valueOf(dipendeDa) : "null");
                if (!seenKeys.add(key)) continue;
                ParameterOption option = new ParameterOption();
                option.setValore(valore);
                Object labelObj = row.get(labelColumn);
                option.setEtichetta(labelObj != null ? String.valueOf(labelObj) : null);
                if (parentColumn != null) {
                    option.setDipendeDa(dipendeDa);
                }
                options.add(option);
            }

            log.info("Trovate {} opzioni per dataSet {}", options.size(), dataSetName);
            return options;

        } catch (Exception e) {
            log.warn("Errore nell'esecuzione della query per dataSet {}: {}", dataSetName, e.getMessage());
            log.debug("Stack trace:", e);
            return Collections.emptyList();
        }
    }

    /**
     * Estrae il nome colonna da un'espressione BIRT del tipo dataSetRow["columnName"].
     */
    private String extractColumnName(String expression) {
        if (expression == null) return null;
        Matcher matcher = DATASET_ROW_EXPR.matcher(expression);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("Espressione BIRT non riconosciuta: {} - usata come nome colonna diretto", expression);
        return expression;
    }

    /**
     * Mappa il tipo di dato BIRT al tipo parametro frontend.
     */
    private String mapBirtTypeToParamType(int birtDataType) {
        // Costanti DataType di BIRT
        final int ANY = 1;
        final int STRING = 2;
        final int BOOLEAN = 3;
        final int INTEGER = 4;
        final int DECIMAL = 5;
        final int FLOAT = 6;
        final int DATE = 7;
        final int TIME = 8;
        final int DATE_TIME = 9;

        switch (birtDataType) {
            case ANY:
            case STRING:
                return "TEXT";
            case INTEGER:
            case DECIMAL:
            case FLOAT:
                return "NUMBER";
            case DATE:
            case TIME:
            case DATE_TIME:
                return "DATE";
            case BOOLEAN:
                return "CHECKBOX";
            default:
                log.warn("Tipo BIRT non mappato: {}, uso TEXT come default", birtDataType);
                return "TEXT";
        }
    }

    /**
     * Mappa il tipo di dato BIRT da stringa al tipo parametro frontend.
     * Usato quando getDataType() restituisce una stringa invece di un integer.
     */
    private int mapBirtTypeFromString(String dataTypeStr) {
        if (dataTypeStr == null) {
            return 2; // STRING
        }
        
        // Mappatura delle costanti di tipo BIRT come stringhe
        switch (dataTypeStr.toUpperCase()) {
            case "ANY":
            case "STRING":
            case "JAVA_LANG_STRING":
                return 2; // STRING
            case "INTEGER":
            case "JAVA_LANG_INTEGER":
            case "INT":
            case "JAVA_LANG_BIGINTEGER":
                return 4; // INTEGER
            case "DECIMAL":
            case "JAVA_MATH_BIGDECIMAL":
            case "DOUBLE":
            case "JAVA_LANG_DOUBLE":
            case "FLOAT":
            case "JAVA_LANG_FLOAT":
                return 5; // DECIMAL
            case "DATE":
            case "JAVA_SQL_DATE":
            case "JAVA_UTIL_DATE":
                return 7; // DATE
            case "TIME":
            case "JAVA_SQL_TIME":
                return 8; // TIME
            case "DATETIME":
            case "DATE_TIME":
            case "JAVA_SQL_TIMESTAMP":
                return 9; // DATE_TIME
            case "BOOLEAN":
            case "JAVA_LANG_BOOLEAN":
                return 3; // BOOLEAN
            default:
                log.warn("Tipo BIRT string non riconosciuto: {}, uso STRING come default", dataTypeStr);
                return 2; // STRING come default
        }
    }
}
