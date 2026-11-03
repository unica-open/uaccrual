/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

// Karma configuration per test di autenticazione
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {},
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/auth-test'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' }
      ]
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['ChromeHeadless'],
    restartOnFileChange: false,
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },
    // File pattern per includere SOLO test di autenticazione
    files: [
      'src/app/core/models/auth-provider.type.spec.ts',
      'src/app/core/services/auth-context.service.spec.ts',
      'src/app/core/services/authentication.service.spec.ts',
      'src/app/core/services/auth.guard.spec.ts',
      'src/app/core/services/auth.interceptor.spec.ts',
      'src/app/core/services/multi-ente.interceptor.spec.ts',
      'src/app/core/services/providers/shibboleth.provider.spec.ts',
      'src/app/core/services/providers/credentials.provider.spec.ts',
      'src/app/core/services/providers/session.provider.spec.ts',
      'src/app/core/services/providers/dev.provider.spec.ts'
    ],
    // Escludi esplicitamente altri test
    exclude: [
      'src/app/features/report/**/*.spec.ts',
      'src/app/features/primanota/**/*.spec.ts',
      'src/app/features/pdc/**/*.spec.ts',
      'src/app/shared/**/*.spec.ts'
    ],
    preprocessors: {
      'src/app/core/**/*.spec.ts': ['coverage']
    }
  });
};
