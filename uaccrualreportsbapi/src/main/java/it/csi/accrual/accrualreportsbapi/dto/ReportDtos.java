/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.dto;

import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

// Wrapper per mantenere la compatibilità con il codice esistente
public class ReportDtos {

    public static class AsyncReportResponse {
        public static AsyncReportResponseBuilder builder() {
            return new AsyncReportResponseBuilder();
        }

        public static class AsyncReportResponseBuilder {
            private java.util.UUID reportId;
            private AsyncReport.ReportStatus status;
            private java.util.Date createdAt;

            public AsyncReportResponseBuilder reportId(java.util.UUID reportId) {
                this.reportId = reportId;
                return this;
            }

            public AsyncReportResponseBuilder status(AsyncReport.ReportStatus status) {
                this.status = status;
                return this;
            }

            public AsyncReportResponseBuilder createdAt(java.util.Date createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse build() {
                it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse response =
                    new it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse();
                response.setReportId(reportId);
                // Convertire lo status dell'entity in StatusEnum del DTO
                if (status != null) {
                    response.setStatus(it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse.StatusEnum.valueOf(status.name()));
                }
                response.setCreatedAt(createdAt);
                return response;
            }
        }
    }

    public static class AsyncReportStatus {
        public static AsyncReportStatusBuilder builder() {
            return new AsyncReportStatusBuilder();
        }

        public static class AsyncReportStatusBuilder {
            private java.util.UUID reportId;
            private AsyncReport.ReportStatus status;
            private Integer progress;
            private String downloadUrl;
            private String error;

            public AsyncReportStatusBuilder reportId(java.util.UUID reportId) {
                this.reportId = reportId;
                return this;
            }

            public AsyncReportStatusBuilder status(AsyncReport.ReportStatus status) {
                this.status = status;
                return this;
            }

            public AsyncReportStatusBuilder progress(Integer progress) {
                this.progress = progress;
                return this;
            }

            public AsyncReportStatusBuilder downloadUrl(String downloadUrl) {
                this.downloadUrl = downloadUrl;
                return this;
            }

            public AsyncReportStatusBuilder error(String error) {
                this.error = error;
                return this;
            }

            public it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus build() {
                it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus response =
                    new it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus();
                response.setReportId(reportId);
                // Convertire lo status dell'entity in StatusEnum del DTO
                if (status != null) {
                    response.setStatus(it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus.StatusEnum.valueOf(status.name()));
                }
                response.setProgress(progress);
                response.setDownloadUrl(downloadUrl);
                response.setError(error);
                return response;
            }
        }
    }
}
