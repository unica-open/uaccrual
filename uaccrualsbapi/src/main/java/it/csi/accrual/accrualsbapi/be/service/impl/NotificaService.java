package it.csi.accrual.accrualsbapi.be.service.impl;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTNotifica;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTNotificaRepository;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualsbapi.dto.Notifica;
import it.csi.accrual.accrualsbapi.util.ElaborazioneUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestisce le notifiche del centro avvisi.
 *
 * <p>Le notifiche sono persistite su {@code accrual_t_notifica}: sopravvivono quindi al cambio di
 * sessione dell'utente, a differenza dello stato che il frontend teneva solo in memoria.</p>
 */
@Service
public class NotificaService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificaService.class);

    public static final String TIPO_INFO = "INFO";
    public static final String TIPO_WARN = "WARN";
    public static final String TIPO_ERROR = "ERROR";
    public static final String TIPO_CONFIRM = "CONFIRM";

    @Autowired
    private AccrualTNotificaRepository notificaRepository;

    @Autowired
    private AccrualTEnteRepository accrualTEnteRepository;

    /**
     * Crea una notifica.
     *
     * <p>Gira in una transazione propria: una notifica registrata durante un'elaborazione non deve
     * sparire se l'elaborazione poi fallisce e viene annullata. Vale soprattutto per le notifiche
     * di errore, che sono proprio l'esito da conservare.</p>
     *
     * @param ente Ente a cui la notifica appartiene
     * @param utente Destinatario; {@code null} per una notifica valida per tutto l'ente
     * @param tipo INFO, WARN, ERROR oppure CONFIRM
     * @param codice Codice messaggio applicativo, può essere {@code null}
     * @param titolo Titolo mostrato nel pannello
     * @param messaggio Testo della notifica
     * @param file Flusso a cui la notifica si riferisce, può essere {@code null}
     * @param resumeToken Token di ripresa, solo per le notifiche CONFIRM
     * @return la notifica creata
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccrualTNotifica crea(AccrualTEnte ente, String utente, String tipo, String codice,
                                 String titolo, String messaggio, AccrualTFile file, String resumeToken) {
        AccrualTNotifica notifica = new AccrualTNotifica();
        notifica.setEnte(ente);
        notifica.setUtente(utente);
        notifica.setTipo(tipo);
        notifica.setCodice(codice);
        notifica.setTitolo(titolo);
        notifica.setMessaggio(troncaMessaggio(messaggio));
        notifica.setFile(file);
        notifica.setResumeToken(resumeToken);
        AccrualTNotifica salvata = notificaRepository.save(notifica);
        LOG.info("Creata notifica {} di tipo {} per l'ente {}", salvata.getId(), tipo, ente.getCodiceUnivoco());
        return salvata;
    }

    /**
     * Chiude le notifiche aperte di un certo codice legate a un flusso.
     *
     * <p>Serve a non lasciare avvisi superati nel pannello: quando l'utente risponde a una richiesta
     * di conferma, la richiesta stessa non ha più motivo di essere mostrata.</p>
     *
     * @param file Flusso di riferimento
     * @param codice Codice messaggio delle notifiche da chiudere
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void chiudiPerFlussoECodice(AccrualTFile file, String codice) {
        if (file == null) {
            return;
        }
        List<AccrualTNotifica> aperte = notificaRepository.findAperteByFileIdAndCodice(file.getId(), codice);
        if (aperte.isEmpty()) {
            return;
        }
        String utente = ElaborazioneUtil.getUtenteCorrente();
        LocalDateTime ora = LocalDateTime.now();
        aperte.forEach(n -> {
            n.setDataCancellazione(ora);
            n.setUtenteCancellazione(utente);
        });
        notificaRepository.saveAll(aperte);
        LOG.info("Chiuse {} notifiche '{}' per il flusso {}", aperte.size(), codice, file.getId());
    }

    /**
     * Notifiche aperte visibili all'utente corrente, dalla più recente.
     *
     * @return elenco dei DTO da restituire al frontend
     * @throws NotFoundException se l'ente corrente non è censito
     */
    @Transactional(readOnly = true)
    public List<Notifica> getNotificheUtenteCorrente() throws NotFoundException {
        AccrualTEnte ente = enteCorrente();
        return notificaRepository.findAperteByEnteAndUtente(ente.getId(), ElaborazioneUtil.getUtenteCorrente())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Segna una notifica come letta.
     *
     * @param id ID della notifica
     * @throws NotFoundException se la notifica non esiste per l'ente corrente
     */
    @Transactional
    public void segnaLetta(String id) throws NotFoundException {
        AccrualTNotifica notifica = caricaPerEnteCorrente(id);
        if (notifica.getDataLettura() == null) {
            notifica.setDataLettura(LocalDateTime.now());
            notificaRepository.save(notifica);
        }
    }

    /**
     * Chiude una notifica: non sarà più restituita al frontend.
     *
     * @param id ID della notifica
     * @throws NotFoundException se la notifica non esiste per l'ente corrente
     */
    @Transactional
    public void chiudi(String id) throws NotFoundException {
        AccrualTNotifica notifica = caricaPerEnteCorrente(id);
        notifica.setDataCancellazione(LocalDateTime.now());
        notifica.setUtenteCancellazione(ElaborazioneUtil.getUtenteCorrente());
        notificaRepository.save(notifica);
    }

    private AccrualTNotifica caricaPerEnteCorrente(String id) throws NotFoundException {
        AccrualTEnte ente = enteCorrente();
        long notificaId;
        try {
            notificaId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new NotFoundException("Notifica con ID " + id + " non trovata.");
        }
        return notificaRepository.findByIdAndEnteId(notificaId, ente.getId())
                .orElseThrow(() -> new NotFoundException("Notifica con ID " + id + " non trovata."));
    }

    private AccrualTEnte enteCorrente() throws NotFoundException {
        String codiceEnte = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        return accrualTEnteRepository.findByCodiceUnivoco(codiceEnte)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + codiceEnte + " non trovato."));
    }

    private Notifica toDto(AccrualTNotifica entity) {
        Notifica dto = new Notifica();
        dto.setId(entity.getId());
        dto.setTipo(entity.getTipo());
        dto.setCodice(entity.getCodice());
        dto.setTitolo(entity.getTitolo());
        dto.setMessaggio(entity.getMessaggio());
        dto.setResumeToken(entity.getResumeToken());
        dto.setLetta(entity.getDataLettura() != null);
        if (entity.getFile() != null) {
            dto.setIdFlusso(String.valueOf(entity.getFile().getId()));
        }
        if (entity.getDataInserimento() != null) {
            dto.setData(Date.from(entity.getDataInserimento().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }

    /** La colonna del messaggio è larga 2500 caratteri: oltre, si tronca invece di fallire. */
    private String troncaMessaggio(String messaggio) {
        if (messaggio == null) {
            return "";
        }
        return messaggio.length() > 2500 ? messaggio.substring(0, 2497) + "..." : messaggio;
    }
}
