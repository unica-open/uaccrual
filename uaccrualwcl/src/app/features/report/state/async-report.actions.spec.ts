/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import {
  SearchAsyncReports,
  ChangeAsyncReportPage,
  LoadAsyncReportDetail,
  DownloadAsyncReport,
  CancelAsyncReport,
  DeleteAsyncReport,
  ClearAsyncReportState,
  SetAsyncReportMessage,
  ClearAsyncReportMessage
} from './async-report.actions';
import { AsyncReportSearchRequest } from '../models/report.models';

describe('AsyncReport Actions', () => {
  describe('SearchAsyncReports', () => {
    it('should create the action with request', () => {
      const request: AsyncReportSearchRequest = {
        data_elaborazione_da: '2025-01-01',
        data_elaborazione_a: '2025-01-31',
        stato: 'COMPLETED'
      };

      const action = new SearchAsyncReports(request);

      expect(action).toBeDefined();
      expect(action.request).toEqual(request);
    });

    it('should create the action with default values', () => {
      const request: AsyncReportSearchRequest = {};
      const action = new SearchAsyncReports(request);

      expect(action).toBeDefined();
      expect(action.request).toEqual(request);
    });
  });

  describe('ChangeAsyncReportPage', () => {
    it('should create the action with page and size', () => {
      const action = new ChangeAsyncReportPage(2, 50);

      expect(action.page).toBe(2);
      expect(action.size).toBe(50);
    });
  });

  describe('LoadAsyncReportDetail', () => {
    it('should create the action with reportId', () => {
      const reportId = '550e8400-e29b-41d4-a716-446655440000';
      const action = new LoadAsyncReportDetail(reportId);

      expect(action.reportId).toBe(reportId);
    });
  });

  describe('DownloadAsyncReport', () => {
    it('should create the action with reportId and formato', () => {
      const reportId = '550e8400-e29b-41d4-a716-446655440000';
      const formato = 'xlsx';
      const action = new DownloadAsyncReport(reportId, formato);

      expect(action.reportId).toBe(reportId);
      expect(action.formato).toBe(formato);
    });

    it('should use default formato "pdf" when not specified', () => {
      const reportId = '550e8400-e29b-41d4-a716-446655440001';
      const action = new DownloadAsyncReport(reportId);

      expect(action.reportId).toBe(reportId);
      expect(action.formato).toBe('pdf');
    });
  });

  describe('CancelAsyncReport', () => {
    it('should create the action with reportId', () => {
      const reportId = '550e8400-e29b-41d4-a716-446655440000';
      const action = new CancelAsyncReport(reportId);

      expect(action.reportId).toBe(reportId);
    });
  });

  describe('DeleteAsyncReport', () => {
    it('should create the action with reportId', () => {
      const reportId = '550e8400-e29b-41d4-a716-446655440000';
      const action = new DeleteAsyncReport(reportId);

      expect(action.reportId).toBe(reportId);
    });
  });

  describe('ClearAsyncReportState', () => {
    it('should create the action', () => {
      const action = new ClearAsyncReportState();
      expect(action).toBeDefined();
    });
  });

  describe('SetAsyncReportMessage', () => {
    it('should create the action with message', () => {
      const message = {
        type: 'success' as const,
        title: 'Operazione completata',
        text: 'Report scaricato con successo'
      };
      const action = new SetAsyncReportMessage(message);

      expect(action.message).toEqual(message);
      expect(action.message.type).toBe('success');
    });
  });

  describe('ClearAsyncReportMessage', () => {
    it('should create the action', () => {
      const action = new ClearAsyncReportMessage();
      expect(action).toBeDefined();
    });
  });
});
