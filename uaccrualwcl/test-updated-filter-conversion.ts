/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { buildFilterFromCriteria } from '../src/app/core/utils/filter-converter.util';

console.log('Testing updated filter conversion...');

// Test case 1: Codice filter should be converted to pdc_codice_ufficiale with 'si' operator
const filters1 = { codice: '1.1.1.01.17' };
const result1 = buildFilterFromCriteria(filters1);
console.log('Test 1 - Input:', filters1);
console.log('Test 1 - Output:', result1);
console.log('Test 1 - Expected: {"pdc_codice_ufficiale":{"si":"1.1.1.01.17"}}');
const parsedResult1 = JSON.parse(result1);
const test1Pass = parsedResult1['pdc_codice_ufficiale']?.si === '1.1.1.01.17' && 
                 Object.keys(parsedResult1).length === 1;
console.log('Test 1 - Pass:', test1Pass);
console.log('');

// Test case 2: Multiple filters including codice
const filters2 = { 
  codice: '1.1.1.01.17',
  descrizione: 'test description'
};
const result2 = buildFilterFromCriteria(filters2);
console.log('Test 2 - Input:', filters2);
console.log('Test 2 - Output:', result2);
// Parse and check
const parsedResult2 = JSON.parse(result2);
const test2Pass = parsedResult2['pdc_codice_ufficiale']?.si === '1.1.1.01.17' && 
                 parsedResult2.descrizione?.eq === 'test description' &&
                 Object.keys(parsedResult2).length === 2;
console.log('Test 2 - Pass:', test2Pass);
console.log('');

// Test case 3: Different field should still use 'eq' operator
const filters3 = { descrizione: 'test' };
const result3 = buildFilterFromCriteria(filters3);
console.log('Test 3 - Input:', filters3);
console.log('Test 3 - Output:', result3);
const parsedResult3 = JSON.parse(result3);
const test3Pass = parsedResult3.descrizione?.eq === 'test' && 
                 !parsedResult3.codice &&
                 Object.keys(parsedResult3).length === 1;
console.log('Test 3 - Pass:', test3Pass);
console.log('');

// Test case 4: Empty filters
const filters4 = {};
const result4 = buildFilterFromCriteria(filters4);
console.log('Test 4 - Input:', filters4);
console.log('Test 4 - Output:', result4);
console.log('Test 4 - Expected: {}');
console.log('Test 4 - Pass:', result4 === '{}');
console.log('');

console.log('All tests completed.');