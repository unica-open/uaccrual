/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { buildFilterFromCriteria } from '../src/app/core/utils/filter-converter.util';

// Simple test to verify the filter conversion works correctly
console.log('Testing filter conversion...');

// Test case 1: Single filter
const filters1 = { codice: '1.1.1.01.17' };
const result1 = buildFilterFromCriteria(filters1);
console.log('Test 1 - Input:', filters1);
console.log('Test 1 - Output:', result1);
console.log('Test 1 - Expected: {"codice":{"eq":"1.1.1.01.17"}}');
console.log('Test 1 - Pass:', result1 === '{"codice":{"eq":"1.1.1.01.17"}}');
console.log('');

// Test case 2: Multiple filters
const filters2 = { 
  codice: '1.1.1.01.17',
  descrizione: 'test description'
};
const result2 = buildFilterFromCriteria(filters2);
console.log('Test 2 - Input:', filters2);
console.log('Test 2 - Output:', result2);
console.log('Test 2 - Expected: {"codice":{"eq":"1.1.1.01.17"},"descrizione":{"eq":"test description"}}');
// Note: Object property order might vary, so we'll do a more flexible check
const parsedResult2 = JSON.parse(result2);
const expected2 = {"codice":{"eq":"1.1.1.01.17"},"descrizione":{"eq":"test description"}};
const test2Pass = parsedResult2.codice?.eq === expected2.codice.eq && 
                 parsedResult2.descrizione?.eq === expected2.descrizione.eq &&
                 Object.keys(parsedResult2).length === 2;
console.log('Test 2 - Pass:', test2Pass);
console.log('');

// Test case 3: Empty filters
const filters3 = {};
const result3 = buildFilterFromCriteria(filters3);
console.log('Test 3 - Input:', filters3);
console.log('Test 3 - Output:', result3);
console.log('Test 3 - Expected: {}');
console.log('Test 3 - Pass:', result3 === '{}');
console.log('');

// Test case 4: Filter with special characters
const filters4 = { 
  collegamento_bil_itas: 'some_value',
  'pdc_tipo.codice': 'TIPO001'
};
const result4 = buildFilterFromCriteria(filters4);
console.log('Test 4 - Input:', filters4);
console.log('Test 4 - Output:', result4);
// Parse and check
const parsedResult4 = JSON.parse(result4);
const test4Pass = parsedResult4['collegamento_bil_itas']?.eq === 'some_value' && 
                 parsedResult4['pdc_tipo.codice']?.eq === 'TIPO001';
console.log('Test 4 - Pass:', test4Pass);
console.log('');

console.log('All tests completed.');