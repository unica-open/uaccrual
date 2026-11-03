/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Aggiunto per getCurrentStato/setCurrentStato
import java.util.ArrayList; // Aggiunto per inizializzazione lista
import java.util.List;
import java.util.Comparator; // Aggiunto per ordinamento stato

import jakarta.persistence.CascadeType; // Aggiunto per CascadeType
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany; // Aggiunto per OneToMany
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_t_file", schema = "accrual")
public class AccrualTFile extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_file_file_id_seq")
    @SequenceGenerator(name = "accrual_t_file_file_id_seq", sequenceName = "accrual.accrual_t_file_file_id_seq", allocationSize = 1)
    @Column(name = "file_id")
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String nome;

    @Column(name = "file_size", nullable = false)
    private BigDecimal dimensione;

    @Column(name = "file_type", nullable = false, length = 500)
    private String tipo;

    @Column(name = "file_file")
    private byte[] file;

    @Column(name = "file_codice", nullable = false)
    private String codice;

    @Column(name = "file_note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_tipo_id")
    private AccrualDFileTipo fileTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccrualRFileStato> stati = new ArrayList<>(); // Inizializzazione

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getDimensione() {
        return dimensione;
    }

    public void setDimensione(BigDecimal dimensione) {
        this.dimensione = dimensione;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AccrualDFileTipo getFileTipo() {
        return fileTipo;
    }

    public void setFileTipo(AccrualDFileTipo fileTipo) {
        this.fileTipo = fileTipo;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public List<AccrualRFileStato> getStati() {
        return stati;
    }

    public void setStati(List<AccrualRFileStato> stati) {
        this.stati = stati;
    }

    // Metodo per ottenere lo stato corrente (quello con fineValidita a null o futura più recente)
    public AccrualDFileStato getCurrentStato() {
        return this.stati.stream()
                .filter(s -> s.getFineValidita() == null || s.getFineValidita().isAfter(LocalDateTime.now()))
                .max(Comparator.comparing(AccrualRFileStato::getInizioValidita))
                .map(AccrualRFileStato::getFileStato)
                .orElse(null); // O lancia un'eccezione se uno stato è sempre atteso
    }

    // Metodo per impostare un nuovo stato, invalidando il precedente
    public void setCurrentStato(AccrualDFileStato newStato, String utente, AccrualTEnte ente) {
        LocalDateTime now = LocalDateTime.now();

        // Invalida lo stato corrente (se esiste)
        this.stati.stream()
                .filter(s -> s.getFineValidita() == null || s.getFineValidita().isAfter(now))
                .forEach(s -> {
                    s.setFineValidita(now);
                    s.setDataAggiornamento(now);
                    s.setUtenteAggiornamento(utente);
                });

        // Crea un nuovo stato
        AccrualRFileStato nuovoStatoEntry = new AccrualRFileStato();
        nuovoStatoEntry.setFile(this);
        nuovoStatoEntry.setFileStato(newStato);
        nuovoStatoEntry.setEnte(ente); // Imposta l'ente passato come parametro
        nuovoStatoEntry.setInizioValidita(now);
        nuovoStatoEntry.setDataInserimento(now);
        nuovoStatoEntry.setUtenteInserimento(utente);
        nuovoStatoEntry.setUtenteAggiornamento(utente); // Aggiunto per coerenza con base entity
        this.stati.add(nuovoStatoEntry);
    }
    
    /**
     * Metodo aggiunto per soddisfare le dipendenze del codice
     * L'anno potrebbe essere ottenuto dal nome del file o da altri dati
     */
    public Integer getAnno() {
        // Per ora, restituiamo null o possiamo cercare di estrarlo dal nome del file
        // Ad esempio, se il nome del file contiene l'anno
        if (nome != null) {
            // Cerca un pattern di anno (4 cifre) nel nome del file
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
            java.util.regex.Matcher matcher = pattern.matcher(nome);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        }
        return null; // Se non troviamo l'anno, restituiamo null
    }
    
    /**
     * Metodo per ottenere la fonte del file
     * La fonte è ottenuta attraverso la relazione con AccrualDFileTipo
     */
    public String getFonte() {
        if (fileTipo != null && fileTipo.getFonte() != null) {
            return fileTipo.getFonte().getCodice();
        }
        return null;
    }
}


