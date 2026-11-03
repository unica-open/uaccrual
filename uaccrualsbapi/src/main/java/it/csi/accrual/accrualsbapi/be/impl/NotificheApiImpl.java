package it.csi.accrual.accrualsbapi.be.impl;

import it.csi.accrual.accrualsbapi.be.NotificheApi;
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualsbapi.be.service.impl.NotificaService;
import it.csi.accrual.accrualsbapi.dto.Error;
import it.csi.accrual.accrualsbapi.dto.Notifica;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/notifiche")
public class NotificheApiImpl implements NotificheApi {

    private static final Logger LOG = LoggerFactory.getLogger(NotificheApiImpl.class);

    @Autowired
    private NotificaService notificaService;

    @Override
    public Response getNotifiche(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            List<Notifica> notifiche = notificaService.getNotificheUtenteCorrente();
            LOG.debug("NotificheApiImpl.getNotifiche - Notifiche aperte: {}", notifiche.size());
            return Response.ok(notifiche).build();
        } catch (NotFoundException e) {
            LOG.warn("NotificheApiImpl.getNotifiche - {}", e.getMessage());
            return errore(Response.Status.NOT_FOUND, "404", "NOT_FOUND", "Ente non trovato", e.getMessage());
        } catch (Exception e) {
            LOG.error("NotificheApiImpl.getNotifiche - Errore durante il recupero delle notifiche", e);
            return errore(Response.Status.INTERNAL_SERVER_ERROR, "500", "WEB_ERR_0001", "Errore imprevisto",
                    "Si è verificato un errore imprevisto: " + e.getMessage());
        }
    }

    @Override
    public Response setNotificaLetta(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            notificaService.segnaLetta(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("NotificheApiImpl.setNotificaLetta - {}", e.getMessage());
            return errore(Response.Status.NOT_FOUND, "404", "NOT_FOUND", "Notifica non trovata", e.getMessage());
        } catch (Exception e) {
            LOG.error("NotificheApiImpl.setNotificaLetta - Errore sulla notifica {}", id, e);
            return errore(Response.Status.INTERNAL_SERVER_ERROR, "500", "WEB_ERR_0001", "Errore imprevisto",
                    "Si è verificato un errore imprevisto: " + e.getMessage());
        }
    }

    @Override
    public Response deleteNotifica(
            String id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            notificaService.chiudi(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("NotificheApiImpl.deleteNotifica - {}", e.getMessage());
            return errore(Response.Status.NOT_FOUND, "404", "NOT_FOUND", "Notifica non trovata", e.getMessage());
        } catch (Exception e) {
            LOG.error("NotificheApiImpl.deleteNotifica - Errore sulla notifica {}", id, e);
            return errore(Response.Status.INTERNAL_SERVER_ERROR, "500", "WEB_ERR_0001", "Errore imprevisto",
                    "Si è verificato un errore imprevisto: " + e.getMessage());
        }
    }

    private Response errore(Response.Status httpStatus, String status, String code, String title, String detail) {
        Error error = new Error();
        error.setStatus(status);
        error.setCode(code);
        error.setTitle(title);
        error.setDetail(detail);
        return Response.status(httpStatus).entity(error).build();
    }
}
