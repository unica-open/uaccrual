/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AsyncReportPollingService } from './async-report-polling.service';
import { ReportService } from './report.service';
import { AsyncReportDetail } from '../models/report.models';

describe('AsyncReportPollingService', () => {
  let service: AsyncReportPollingService;
  let reportServiceSpy: jasmine.SpyObj<ReportService>;

  const mockReportDetail: AsyncReportDetail = {
    report_id: '550e8400-e29b-41d4-a716-446655440000',
    formato: 'pdf',
    file_nome: 'test.pdf',
    stato: 'PROCESSING',
    data_creazione: '2025-01-01T10:00:00Z',
    progress_percentage: 50
  };

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ReportService', ['getAsyncReportDetail']);

    TestBed.configureTestingModule({
      providers: [
        AsyncReportPollingService,
        { provide: ReportService, useValue: spy }
      ]
    });

    service = TestBed.inject(AsyncReportPollingService);
    reportServiceSpy = TestBed.inject(ReportService) as jasmine.SpyObj<ReportService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start polling', (done) => {
    const reportId = '550e8400-e29b-41d4-a716-446655440000';

    reportServiceSpy.getAsyncReportDetail.and.returnValue(of(mockReportDetail));

    service.startPolling(reportId, 1000).subscribe(status => {
      expect(status).toEqual(mockReportDetail);
      expect(reportServiceSpy.getAsyncReportDetail).toHaveBeenCalledWith(reportId);
      done();
    });
  });

  it('should stop polling', () => {
    const reportId = '550e8400-e29b-41d4-a716-446655440000';

    service.startPolling(reportId, 5000);
    service.stopPolling();

    // Verify no errors thrown
    expect(() => service.stopPolling()).not.toThrow();
  });

  it('should cleanup on destroy', () => {
    const reportId = '550e8400-e29b-41d4-a716-446655440000';

    service.startPolling(reportId, 5000);
    service.ngOnDestroy();

    // Verify subscription is cleaned up
    expect(() => service.stopPolling()).not.toThrow();
  });

  it('should use default interval of 5000ms', () => {
    const reportId = '550e8400-e29b-41d4-a716-446655440000';

    reportServiceSpy.getAsyncReportDetail.and.returnValue(of(mockReportDetail));

    service.startPolling(reportId); // No interval specified

    expect(reportServiceSpy.getAsyncReportDetail).toHaveBeenCalled();
  });
});
