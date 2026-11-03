/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// This file is used as default for development
export const environment = {
  production: false,
  environmentName: 'development',

  services: {
    accrualLegacyApiUrl: '/accrualsbsrv/api/v1',
    accrualNewApiUrl: '/accrualsbapi/api/v1',
    accrualReportsApiUrl: '/accrualreportsbapi/api/v1',
    accrualAuthApiUrl: '/accrualauthsbapi/api/v1',
  },

  authentication: {
    devFileUrl: '/assets/config/dev-info.json'
  },

  features: {
    enableDebugTools: true,
    enableMockData: true,
    enableAdvancedFeatures: true
  },

  network: {
    timeout: 30000,
    retryAttempts: 3,
    retryDelay: 1000
  }
};
