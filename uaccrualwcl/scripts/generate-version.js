/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const packageJson = require('../package.json');
const version = packageJson.version;

let gitCommit = 'unknown';
try {
  gitCommit = execSync('git rev-parse --short HEAD').toString().trim();
} catch (e) {
  console.warn('Git not available, using default commit hash');
}

const buildDate = new Date().toISOString();

const content = `// AUTO-GENERATED FILE - DO NOT EDIT MANUALLY
// Generated on: ${buildDate}
export const APP_VERSION = '${version}';
export const GIT_COMMIT = '${gitCommit}';
export const BUILD_DATE = '${buildDate}';
export const VERSION_STRING = \`v\${APP_VERSION}-\${GIT_COMMIT}\`;
`;

const outputPath = path.join(__dirname, '..', 'src', 'version.ts');
fs.writeFileSync(outputPath, content, 'utf8');
console.log(`✓ Version file generated: v${version}-${gitCommit}`);
