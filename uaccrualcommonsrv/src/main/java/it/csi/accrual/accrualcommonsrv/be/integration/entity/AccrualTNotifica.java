package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Notifica mostrata all'utente nel centro avvisi del client.
 *
 * <p>Le notifiche sono persistenti: sopravvivono al cambio di sessione dell'utente, a differenza
 * dello stato che il frontend teneva solo in memoria.</p>
 *
 * <p>Il ciclo di vita è: creata → letta ({@link #dataLettura} valorizzata quando l'utente la apre)
 * → chiusa (cancellazione logica su {@code gest_data_del}, gestita da {@link AccrualBaseEntity}).</p>
 */
@Entity
@Table(name = "accrual_t_notifica", schema = "accrual")
public class AccrualTNotifica extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_notifica_notifica_id_seq")
    @SequenceGenerator(name = "accrual_t_notifica_notifica_id_seq", sequenceName = "accrual.accrual_t_notifica_notifica_id_seq", allocationSize = 1)
    @Column(name = "notifica_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    /** Destinatario della notifica; {@code null} indica una notifica valida per tutto l'ente. */
    @Column(name = "notifica_utente", length = 200)
    private String utente;

    /** INFO, WARN, ERROR oppure CONFIRM. */
    @Column(name = "notifica_tipo", nullable = false, length = 20)
    private String tipo;

    /** Codice messaggio applicativo, es. {@code WEB_CON_0024}. */
    @Column(name = "notifica_codice", length = 200)
    private String codice;

    @Column(name = "notifica_titolo", nullable = false, length = 500)
    private String titolo;

    @Column(name = "notifica_messaggio", nullable = false, length = 2500)
    private String messaggio;

    /** Flusso a cui la notifica si riferisce, se ce n'è uno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private AccrualTFile file;

    /** Token di ripresa elaborazione, valorizzato solo per le notifiche di tipo CONFIRM. */
    @Column(name = "notifica_resume_token", length = 2000)
    private String resumeToken;

    @Column(name = "notifica_data_lettura")
    private LocalDateTime dataLettura;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public AccrualTFile getFile() {
        return file;
    }

    public void setFile(AccrualTFile file) {
        this.file = file;
    }

    public String getResumeToken() {
        return resumeToken;
    }

    public void setResumeToken(String resumeToken) {
        this.resumeToken = resumeToken;
    }

    public LocalDateTime getDataLettura() {
        return dataLettura;
    }

    public void setDataLettura(LocalDateTime dataLettura) {
        this.dataLettura = dataLettura;
    }
}
