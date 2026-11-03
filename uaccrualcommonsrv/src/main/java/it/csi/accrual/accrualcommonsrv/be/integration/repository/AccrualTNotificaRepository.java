package it.csi.accrual.accrualcommonsrv.be.integration.repository;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTNotifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccrualTNotificaRepository extends JpaRepository<AccrualTNotifica, Long> {

    /**
     * Notifiche aperte visibili a un utente: le proprie più quelle rivolte a tutto l'ente.
     * La cancellazione logica è già filtrata dalla {@code @SQLRestriction} dell'entità base.
     */
    @Query("SELECT n FROM AccrualTNotifica n WHERE n.ente.id = :enteId "
         + "AND (n.utente IS NULL OR n.utente = :utente) ORDER BY n.dataInserimento DESC")
    List<AccrualTNotifica> findAperteByEnteAndUtente(@Param("enteId") Long enteId, @Param("utente") String utente);

    @Query("SELECT n FROM AccrualTNotifica n WHERE n.id = :id AND n.ente.id = :enteId")
    Optional<AccrualTNotifica> findByIdAndEnteId(@Param("id") Long id, @Param("enteId") Long enteId);

    /**
     * Notifiche aperte legate a un flusso e a un codice messaggio: servono a chiudere quelle
     * superate quando il flusso cambia stato, per non lasciarne di orfane.
     */
    @Query("SELECT n FROM AccrualTNotifica n WHERE n.file.id = :fileId AND n.codice = :codice")
    List<AccrualTNotifica> findAperteByFileIdAndCodice(@Param("fileId") Long fileId, @Param("codice") String codice);
}
