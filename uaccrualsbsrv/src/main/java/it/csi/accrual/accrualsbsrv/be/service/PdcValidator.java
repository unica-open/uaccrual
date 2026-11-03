/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.exception.ValidationException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTPdc;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTCausaleRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTPdcSaldoRepository;
import it.csi.accrual.accrualsbsrv.be.integration.repository.AccrualTSoggettoRepository;

/**
 * Classe per la validazione della logica di business per l'entitÃ  Pdc.
 */
@Component
public class PdcValidator {

    @Autowired
    private AccrualTPdcRepository accrualTPdcRepository;

    @Autowired
    private AccrualTCausaleRepository accrualTCausaleRepository;

    @Autowired
    private AccrualTSoggettoRepository accrualTSoggettoRepository;

    @Autowired
    private AccrualTPdcSaldoRepository accrualTPdcSaldoRepository;

    /**
     * Implementa l'Algoritmo 5.1 - Verifica conto Accrual da salvare.
     * Controlla obbligatorietÃ , unicitÃ , consistenza delle date e legami con le prime note.
     * @param conto l'entitÃ  Pdc da validare
     * @param isInsert true se l'operazione Ã¨ un inserimento, false se Ã¨ un aggiornamento
     */
    public void validateForSave(AccrualTPdc conto, boolean isInsert) {
        // 1. Check if parent is a leaf (for insert operations)
        // NOTA: Secondo le specifiche CDU-003, se il padre è una foglia, 
        //       l'inserimento è consentito e il padre deve essere aggiornato a non-foglia.
        //       Questo controllo è stato rimosso per conformità alle specifiche.
        //       La logica di aggiornamento del padre da foglia a non-foglia è gestita nel service.

        // 2. Mandatory Check - CDU-003 5.1: Tutti i campi obbligatori devono essere valorizzati (WEB_ERR_0002)
        if (conto.getCodice() == null || conto.getCodice().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice del conto è obbligatorio");
        }
        if (conto.getDescrizione() == null || conto.getDescrizione().trim().isEmpty()) {
            throw new IllegalArgumentException("La descrizione del conto è obbligatoria");
        }
        if (conto.getPdcClasse() == null || conto.getPdcClasse().getId() == null) {
            throw new IllegalArgumentException("La classe del conto è obbligatoria");
        }
        if (conto.getPdcTipo() == null || conto.getPdcTipo().getId() == null) {
            throw new IllegalArgumentException("Il tipo del conto è obbligatorio");
        }
        if (conto.getInizioValidita() == null) {
            throw new IllegalArgumentException("La data di inizio validità è obbligatoria");
        }

        // 3. Uniqueness Check - CDU-003 5.1: La combinazione (ente, codice ufficiale, classe, famiglia) deve essere univoca (WEB_ERR_0005)
        if (conto.getCodiceUfficiale() != null && conto.getPdcClasse() != null && conto.getEnte() != null && conto.getPdcFamiglia() != null) {
            if (isInsert) {
                // Per inserimento, verifico se esiste già un conto con la stessa combinazione
                accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_Id(
                    conto.getCodiceUfficiale(),
                    conto.getPdcClasse().getId(),
                    conto.getPdcFamiglia().getId(),
                    conto.getEnte().getId())
                .ifPresent(existingPdc -> {
                    throw new IllegalArgumentException("Esiste già un conto con codice ufficiale, classe e famiglia: " + conto.getCodiceUfficiale() + ", " + conto.getPdcClasse().getDescrizione() + ", " + conto.getPdcFamiglia().getDescrizione());
                });
            } else {
                // Per aggiornamento, escludo l'ID corrente dalla verifica
                accrualTPdcRepository.findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                    conto.getCodiceUfficiale(),
                    conto.getPdcClasse().getId(),
                    conto.getPdcFamiglia().getId(),
                    conto.getEnte().getId(),
                    conto.getId())
                .ifPresent(existingPdc -> {
                    throw new IllegalArgumentException("Esiste già un conto con codice ufficiale, classe e famiglia: " + conto.getCodiceUfficiale() + ", " + conto.getPdcClasse().getDescrizione() + ", " + conto.getPdcFamiglia().getDescrizione());
                });
            }
        }

        // 4. Date Consistency Check - CDU-003 5.1: La data di fine validità deve essere maggiore o uguale alla data di inizio validità (WEB_ERR_0007)
        if (conto.getInizioValidita() != null && conto.getFineValidita() != null) {
            if (conto.getInizioValidita().isAfter(conto.getFineValidita())) {
                throw new IllegalArgumentException("La data di inizio validità deve essere minore o uguale alla data di fine validità");
            }
        }

        // 4.1 Sovrapposizione Validità Check - CDU-003 5.1: Nessun conto con stesso codice ufficiale può avere periodi di validità sovrapposti (WEB_ERR_0030)
        if (conto.getCodiceUfficiale() != null && conto.getPdcClasse() != null && 
            conto.getPdcFamiglia() != null && conto.getEnte() != null && 
            conto.getInizioValidita() != null) {
            
            boolean sovrapposizione = accrualTPdcRepository.existsSovrapposizioneValidita(
                conto.getCodiceUfficiale(),
                conto.getPdcClasse().getId(),
                conto.getPdcFamiglia().getId(),
                conto.getEnte().getId(),
                conto.getInizioValidita(),
                conto.getFineValidita(),
                isInsert ? null : conto.getId()
            );
            
            if (sovrapposizione) {
                // Recupero il conto esistente per il messaggio di errore
                accrualTPdcRepository
                    .findByCodiceUfficialeAndPdcClasse_IdAndPdcFamiglia_IdAndEnte_IdAndIdNot(
                        conto.getCodiceUfficiale(),
                        conto.getPdcClasse().getId(),
                        conto.getPdcFamiglia().getId(),
                        conto.getEnte().getId(),
                        conto.getId())
                    .ifPresent(contoEsistente -> {
                        String fineValiditaStr = contoEsistente.getFineValidita() != null 
                            ? contoEsistente.getFineValidita().toString() 
                            : "illimitata";
                        throw new IllegalArgumentException(
                            String.format("Il conto %s è già presente con validità %s / %s",
                                contoEsistente.getCodiceUfficiale(),
                                contoEsistente.getInizioValidita(),
                                fineValiditaStr)
                        );
                    });
                
                // Se non trova il conto (caso raro), lancia comunque eccezione generica
                throw new IllegalArgumentException(
                    String.format("Il conto %s è già presente con un periodo di validità sovrapposto",
                        conto.getCodiceUfficiale())
                );
            }
        }

        // 5. Prime Note Check - CDU-003 5.1: Se fineValidità è valorizzata, verifica che non esistano prime note in stato diverso da 'Annullato' collegate al conto oppure ai suoi conti figli, con data registrazione successiva a fineValidità (WEB_ERR_0027)
        if (conto.getFineValidita() != null) {
            // Ottenere tutti i figli ricorsivi del conto
            List<Long> idContiDaVerificare = getIdFigliRicorsivi(conto.getId());
            idContiDaVerificare.add(conto.getId()); // Includo anche il conto stesso

            if (accrualTPdcRepository.esistonoPrimeNoteSuccessive(idContiDaVerificare, conto.getFineValidita().toLocalDate())) {
                throw new IllegalArgumentException("Impossibile impostare la fine validità perché esistono prime note successive");
            }
        }
    }

    /**
     * Implementa l'Algoritmo 5.2 - Verifica conto Accrual da eliminare.
     * Controlla la presenza di legami (figli, causali, prime note, soggetti, saldi).
     * @param contoId l'ID del conto da verificare
     */
    public void validateForDelete(Long contoId) {
        // Controllo esistenza figli - CDU-003 5.2: Impedire eliminazione se esistono conti figli (WEB_ERR_0012)
        if (accrualTPdcRepository.existsByPdcPadre_IdAndDataCancellazioneIsNull(contoId)) {
            throw new IllegalArgumentException("Impossibile eliminare il conto perché ha dei figli associati");
        }

        // TODO: Implementare Algoritmo 5.2
        // Altri controlli su legami: 2. Causali, 3. Prime Note, 4. Soggetti, 5. Saldi
        // Lancia WEB_ERR_0012 se esistono legami.

        // Controllo per prime note associate al conto - CDU-003 5.2: Impedire eliminazione se esistono prime note (WEB_ERR_0012)
        if (accrualTPdcRepository.existsPrimeNoteAssociate(contoId)) {
            throw new IllegalArgumentException("Impossibile eliminare il conto perché ha delle prime note associate");
        }

        // Controllo per causali associate al conto - CDU-003 5.2: Impedire eliminazione se esistono causali (WEB_ERR_0012)
        if (accrualTCausaleRepository.existsByPdc_Id(contoId)) {
            throw new IllegalArgumentException("Impossibile eliminare il conto perché ha delle causali associate");
        }

        // Controllo per soggetti associati al conto - CDU-003 5.2: Impedire eliminazione se esistono soggetti (WEB_ERR_0012)
        if (accrualTSoggettoRepository.existsByPdc_Id(contoId)) {
            throw new IllegalArgumentException("Impossibile eliminare il conto perché ha dei soggetti associati");
        }

        // Controllo per saldi associati al conto - CDU-003 5.2: Impedire eliminazione se esistono saldi (WEB_ERR_0012)
        if (accrualTPdcSaldoRepository.existsByPdc_IdAndDataCancellazioneIsNull(contoId)) {
            throw new IllegalArgumentException("Impossibile eliminare il conto perché ha dei saldi associati");
        }
    }

    /**
     * Ottiene ricorsivamente tutti gli ID dei conti figli di un conto specificato
     * @param idContoPadre l'ID del conto padre
     * @return lista di ID dei conti figli (anche nidificati)
     */
    private List<Long> getIdFigliRicorsivi(Long idContoPadre) {
        List<Long> risultato = new ArrayList<>();

        // Ottieni i figli diretti
        List<AccrualTPdc> figliDiretti = accrualTPdcRepository.findByPdcPadre_IdAndDataCancellazioneIsNull(idContoPadre);

        for (AccrualTPdc figlio : figliDiretti) {
            risultato.add(figlio.getId());

            // Aggiungi ricorsivamente i figli di questo figlio
            risultato.addAll(getIdFigliRicorsivi(figlio.getId()));
        }

        return risultato;
    }
}
