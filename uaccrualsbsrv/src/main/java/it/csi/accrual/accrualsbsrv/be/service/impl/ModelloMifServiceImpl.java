/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdcSaldo;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTParametro;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcSaldoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPrimaNotaImportoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEntitaContabile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEntitaContabileRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTParametroRepository;
import it.csi.accrual.accrualsbsrv.dto.EntitaContabile;
import it.csi.accrual.accrualsbsrv.be.service.ModelloMifService;
import it.csi.accrual.accrualsbsrv.dto.ModelloMifData;
import it.csi.accrual.accrualsbsrv.dto.MifSaldo;
import it.csi.accrual.accrualsbsrv.dto.MifRettifica;
import it.csi.accrual.accrualsbsrv.dto.MifSaldoItas;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModelloMifServiceImpl implements ModelloMifService {

    private static final Logger log = LoggerFactory.getLogger(ModelloMifServiceImpl.class);

    private static final String PARAM_MEF_RETTIFICHE_CAUSALI = "MEF_RETTIFICHE_CAUSALI";

    @Autowired
    private AccrualTEnteRepository enteRepository;

    @Autowired
    private AccrualTParametroRepository parametroRepository;

    @Autowired
    private AccrualTPdcSaldoRepository pdcSaldoRepository;

    @Autowired
    private AccrualTPrimaNotaImportoRepository primaNotaImportoRepository;

    @Autowired
    private AccrualTPdcRepository pdcRepository;

    @Autowired
    private AccrualTEntitaContabileRepository entitaContabileRepository;

    @Override
    public ModelloMifData getDatiModelloMif(Integer anno) {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Recupero dati modello MIF per anno: {} ed ente: {}", anno, enteCodice);

        AccrualTEnte ente = enteRepository.findByCodiceUnivoco(enteCodice)
                .orElseThrow(() -> new IllegalArgumentException("Ente non trovato con codice fiscale: " + enteCodice));

        ModelloMifData data = new ModelloMifData();

        // 1. Saldi Foglio 1 (Anno corrente)
        List<AccrualTPdcSaldo> saldiEntities = pdcSaldoRepository.findByEnteAndAnno(ente, anno);
        log.info("Trovati {} saldi per anno {}", saldiEntities.size(), anno);

        List<MifSaldo> saldiFoglio1 = saldiEntities.stream().map(s -> {
            MifSaldo dto = new MifSaldo();
            dto.setCodiceConto(s.getCodice());
            dto.setImporto(s.getImporto() != null ? s.getImporto().doubleValue() : 0.0);
            return dto;
        }).collect(Collectors.toList());
        data.setSaldiFoglio1(saldiFoglio1);
        log.info("Saldi Foglio 1: {} conti", saldiFoglio1.size());

        // 2. Rettifiche Foglio 2 - algoritmo conforme a CDU-012 sez. 2.6.2.2
        List<MifRettifica> rettificheFoglio2 = calcolaRettificheFoglio2(anno, enteCodice, ente);
        data.setRettificheFoglio2(rettificheFoglio2);
        log.info("Rettifiche Foglio 2: {} conti", rettificheFoglio2.size());

        // 3. Saldi Bilancio Itas (Anno precedente XXXX-1)
        Integer annoPrecedente = anno - 1;
        List<Object[]> saldiItas = pdcSaldoRepository.findSaldiAggregatiByItas(ente, annoPrecedente);
        log.info("Trovati {} saldi ITAS per anno {}", saldiItas.size(), annoPrecedente);

        List<MifSaldoItas> saldiBilancioItas = saldiItas.stream().map(row -> {
            MifSaldoItas dto = new MifSaldoItas();
            dto.setCodiceBilancioItas((String) row[0]);
            dto.setImporto(row[1] != null ? ((BigDecimal) row[1]).doubleValue() : 0.0);
            return dto;
        }).collect(Collectors.toList());
        data.setSaldiBilancioItas(saldiBilancioItas);
        log.info("Saldi Bilancio ITAS: {} conti", saldiBilancioItas.size());

        return data;
    }

    /**
     * Algoritmo conforme a CDU-012 sez. 2.6.2.2:
     *
     * Per ogni conto con flagFoglia=true O flagFogliaNonFoglia=true:
     *   colonna L = rettifiche proprie + rettifiche di tutti i discendenti
     *
     * Per ogni conto con flagFoglia=false E flagFogliaNonFoglia=false:
     *   colonna L = rettifiche dei soli discendenti (non del conto stesso)
     *
     * La chiave nel DTO e' pdc_codice_ufficiale (quello usato dal template Excel).
     */
    private List<MifRettifica> calcolaRettificheFoglio2(Integer anno, String enteCodice, AccrualTEnte ente) {

        // Step 1: parametro causali rettifiche
        Optional<AccrualTParametro> parametroOpt = parametroRepository
                .findByParametroNomeAndEnteAndParametroAbilitatoTrue(PARAM_MEF_RETTIFICHE_CAUSALI, ente);
        List<String> causaliCodici = parametroOpt
                .map(p -> Arrays.stream(p.getParametroValore().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        if (causaliCodici.isEmpty()) {
            log.warn("Parametro {} non trovato o non abilitato per ente {}: nessuna rettifica calcolata per Foglio 2",
                PARAM_MEF_RETTIFICHE_CAUSALI, enteCodice);
            return Collections.emptyList();
        }
        log.info("Parametro {}: causali = {}", PARAM_MEF_RETTIFICHE_CAUSALI, causaliCodici);

        // Step 2: rettifiche dirette per pdc_codice (sommatoria PN definitivi per causale)
        List<Object[]> rettificheRows = primaNotaImportoRepository
                .findRettificheAggregateByCausaleCodici(anno, enteCodice, causaliCodici);
        Map<String, Double> rettificheDiretteMap = new HashMap<>();
        for (Object[] row : rettificheRows) {
            String codicePdc = (String) row[0];
            Double importo = row[1] != null ? ((BigDecimal) row[1]).doubleValue() : 0.0;
            rettificheDiretteMap.put(codicePdc, importo);
        }
        log.info("Rettifiche dirette (pdc_codice -> importo) per {} conti: {}", rettificheDiretteMap.size(), rettificheDiretteMap);

        // Step 3: carica l'intero albero PDC dell'ente in memoria (2 query in totale)
        List<AccrualTPdc> tuttiConti = pdcRepository.findAllByEnteAndNotDeleted(ente);
        log.info("Caricati {} conti PDC per ente {}", tuttiConti.size(), enteCodice);

        Map<Long, AccrualTPdc> contiById = tuttiConti.stream()
                .collect(Collectors.toMap(AccrualTPdc::getId, c -> c));

        // Mappa padreId -> lista figli diretti
        Map<Long, List<AccrualTPdc>> figliByPadreId = tuttiConti.stream()
                .filter(c -> c.getPdcPadre() != null)
                .collect(Collectors.groupingBy(c -> c.getPdcPadre().getId()));

        // Step 3.5: per i conti di classe AVERE la rettifica corretta è Avere-Dare = -(Dare-Avere)
        // Per lo stesso pdc_codice (es. '5.1') coesistono record di famiglie diverse:
        //   - famiglia ACCRUAL-A (fogliaNonFoglia=true): PP/AVERE — nodo su cui si registrano le PN
        //   - famiglia DLGS118 (foglia=false, fogliaNonFoglia=false): CE/DARE — aggregatore
        // Filtrando solo i nodi foglia o fogliaNonFoglia si ottiene il segno del nodo su cui
        // le prime note vengono effettivamente registrate, senza dipendere dal codice letterale
        // della famiglia (che varia per ambiente: 'ACCRUAL', 'ACCRUAL-A', ecc.).
        Map<String, TipoSegno> segnoByCodicePdc = tuttiConti.stream()
                .filter(c -> c.getPdcClasse() != null && c.getPdcClasse().getSegno() != null)
                .filter(c -> c.isFoglia() || Boolean.TRUE.equals(c.getFogliaNonFoglia()))
                .collect(Collectors.toMap(AccrualTPdc::getCodice, c -> c.getPdcClasse().getSegno(), (a, b) -> a));
        rettificheDiretteMap.replaceAll((codice, importo) ->
                TipoSegno.AVERE.equals(segnoByCodicePdc.get(codice)) ? -importo : importo);
        log.info("Rettifiche dirette dopo correzione segno: {}", rettificheDiretteMap);

        // Step 4: DFS post-order - per ogni nodo calcola la somma delle rettifiche
        // del nodo stesso + tutti i suoi discendenti
        Map<Long, Double> totaleByNodeId = new HashMap<>();
        tuttiConti.stream()
                .filter(c -> c.getPdcPadre() == null)
                .forEach(radice -> calcolaTotaleSubalbero(radice.getId(), figliByPadreId, contiById,
                        rettificheDiretteMap, totaleByNodeId));

        // Step 5: applica la regola flagFoglia / flagFogliaNonFoglia per costruire la risposta
        Map<String, Double> risultatoMap = new HashMap<>();
        for (AccrualTPdc conto : tuttiConti) {
            boolean isFoglia = conto.isFoglia();
            boolean isFogliaNonFoglia = Boolean.TRUE.equals(conto.getFogliaNonFoglia());

            Double totaleConto = totaleByNodeId.getOrDefault(conto.getId(), 0.0);
            Double selfRettifica = rettificheDiretteMap.getOrDefault(conto.getCodice(), 0.0);

            Double importoColonnaL;
            if (isFoglia || isFogliaNonFoglia) {
                // foglia o fogliaNonFoglia: rettifiche proprie + discendenti
                importoColonnaL = totaleConto;
            } else {
                // puro aggregatore: solo discendenti (senza il proprio importo diretto)
                importoColonnaL = totaleConto - selfRettifica;
            }

            if (importoColonnaL != 0.0) {
                String codiceUfficiale = conto.getCodiceUfficiale() != null
                        ? conto.getCodiceUfficiale()
                        : conto.getCodice();
                risultatoMap.put(codiceUfficiale, importoColonnaL);
                log.debug("Rettifica Foglio2: pdc_codice={}, foglia={}, fogliaNonFoglia={}, codiceUfficiale={}, importo={}",
                        conto.getCodice(), isFoglia, isFogliaNonFoglia, codiceUfficiale, importoColonnaL);
            }
        }

        log.info("Rettifiche Foglio2 calcolate (approccio conforme) per {} conti", risultatoMap.size());

        return risultatoMap.entrySet().stream().map(entry -> {
            MifRettifica dto = new MifRettifica();
            dto.setCodiceConto(entry.getKey());
            dto.setImporto(entry.getValue());
            log.info("Rettifica Foglio2 -> codiceUfficiale={}, importo={}", entry.getKey(), entry.getValue());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * DFS post-order: calcola la somma delle rettifiche dirette del nodo
     * piu' quelle di tutti i suoi discendenti, salva il risultato in totaleByNodeId.
     */
    private Double calcolaTotaleSubalbero(Long nodeId,
            Map<Long, List<AccrualTPdc>> figliByPadreId,
            Map<Long, AccrualTPdc> contiById,
            Map<String, Double> rettificheDiretteMap,
            Map<Long, Double> totaleByNodeId) {

        AccrualTPdc conto = contiById.get(nodeId);
        if (conto == null) return 0.0;

        Double selfRettifica = rettificheDiretteMap.getOrDefault(conto.getCodice(), 0.0);

        Double childrenSum = 0.0;
        for (AccrualTPdc figlio : figliByPadreId.getOrDefault(nodeId, Collections.emptyList())) {
            childrenSum += calcolaTotaleSubalbero(figlio.getId(), figliByPadreId, contiById,
                    rettificheDiretteMap, totaleByNodeId);
        }

        Double totale = selfRettifica + childrenSum;
        totaleByNodeId.put(nodeId, totale);
        return totale;
    }

    @Override
    public List<EntitaContabile> getAnniDisponibili() {
        String enteCodice = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        log.info("Recupero anni disponibili per ente: {}", enteCodice);
        List<AccrualTEntitaContabile> entita = entitaContabileRepository.findAnniByEnteCodice(enteCodice);
        return entita.stream().map(ec -> {
            EntitaContabile dto = new EntitaContabile();
            try {
                dto.setAnno(Integer.parseInt(ec.getAnno()));
            } catch (NumberFormatException e) {
                log.warn("Anno non numerico ignorato: {}", ec.getAnno());
                return null;
            }
            dto.setFase(ec.getFase() != null ? ec.getFase().name() : null);
            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }
}
