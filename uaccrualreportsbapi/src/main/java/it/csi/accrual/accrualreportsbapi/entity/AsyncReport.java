/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.entity;

import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accrual_t_async_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsyncReport {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(nullable = false)
    private String format;

    @Column(nullable = false)
    private String rptdesignFileName;

    @Column(nullable = true)
    private String rptconfigFileName;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String parameters;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = true)
    private byte[] fileContent;

    @Column
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    @Version
    private Long version;

    @Column(name = "ente_id", nullable = false)
    private Long enteId;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
        if (this.version == null) {
            this.version = 0L;
        }
        if (this.enteId == null) {
            this.enteId = EnteContextHolder.getCurrentEnte();
        }
        if (this.progressPercentage == null) {
            this.progressPercentage = 0;
        }
    }

    public enum ReportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        ERROR
    }
}
