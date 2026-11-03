/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

/**
 * Configura il thread pool per i metodi @Async e propaga il SecurityContext
 * al thread figlio, necessario per leggere JWT/ente nell'elaborazione asincrona.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "elaborazioneExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Un solo thread attivo per non saturare il DB con elaborazioni parallele
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        // Coda generosa: permette di accodare richieste in attesa senza rifiutarle
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("elaborazione-flusso-");
        // Attendi fino a 60 min prima di interrompere un thread in esecuzione
        executor.setAwaitTerminationSeconds(3600);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        // Propaga il SecurityContext (JWT con codice_ente) al thread figlio
        return new DelegatingSecurityContextExecutor(executor);
    }
}
