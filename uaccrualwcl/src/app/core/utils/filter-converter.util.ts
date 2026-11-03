/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Utility function to convert filter expressions from string format to JSON format
 * 
 * Converts expressions like:
 * - "codice=='1.1.1.01.17'" to {"codice": {"eq": "1.1.1.01.17"}}
 * - "descrizione!=test" to {"descrizione": {"ne": "test"}}
 * - "codice in (val1,val2)" to {"codice": {"in": ["val1", "val2"]}}
 */

export interface FilterCondition {
  [operator: string]: string | string[];
}

export interface FilterObject {
  [key: string]: FilterCondition;
}

/**
 * Converts a filter string to JSON format
 * @param filterString The filter string in format like "codice=='value'"
 * @returns JSON object in format {"fieldName": {"operator": "value"}}
 */
export function convertFilterToJSON(filterString: string): string {
  // Handle 'and' separated conditions
  const conditions = filterString.split(' and ');
  const result: FilterObject = {};

  for (const condition of conditions) {
    const trimmedCondition = condition.trim();
    
    // Handle different operators
    if (trimmedCondition.includes(' in (')) {
      // Handle 'in' operator: fieldName in (value1,value2,value3)
      const match = trimmedCondition.match(/^([a-zA-Z_][a-zA-Z0-9_.]*)\s+in\s+\((.+)\)$/);
      if (match) {
        const fieldName = match[1];
        const values = match[2].split(',').map(v => v.trim().replace(/['"]/g, ''));
        result[fieldName] = { "in": values } as FilterCondition;
      }
    } else if (trimmedCondition.includes(' nin (')) {
      // Handle 'nin' operator: fieldName nin (value1,value2,value3)
      const match = trimmedCondition.match(/^([a-zA-Z_][a-zA-Z0-9_.]*)\s+nin\s+\((.+)\)$/);
      if (match) {
        const fieldName = match[1];
        const values = match[2].split(',').map(v => v.trim().replace(/['"]/g, ''));
        result[fieldName] = { "nin": values } as FilterCondition;
      }
    } else {
      // Handle comparison operators: ==, !=, >, >=, <, <=
      let operator: string;
      let regex: RegExp;
      
      if (trimmedCondition.includes('==')) {
        operator = 'eq';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*==\s*(.*)/;
      } else if (trimmedCondition.includes('!=')) {
        operator = 'ne';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*!=\s*(.*)/;
      } else if (trimmedCondition.includes('>=')) {
        operator = 'gte';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*>=\s*(.*)/;
      } else if (trimmedCondition.includes('>')) {
        operator = 'gt';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*>\s*(.*)/;
      } else if (trimmedCondition.includes('<=')) {
        operator = 'lte';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*<=\s*(.*)/;
      } else if (trimmedCondition.includes('<')) {
        operator = 'lt';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s*<\s*(.*)/;
      } else if (trimmedCondition.includes(' c ')) {
        operator = 'c';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+c\s+(.*)/;
      } else if (trimmedCondition.includes(' ci ')) {
        operator = 'ci';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+ci\s+(.*)/;
      } else if (trimmedCondition.includes(' s ')) {
        operator = 's';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+s\s+(.*)/;
      } else if (trimmedCondition.includes(' si ')) {
        operator = 'si';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+si\s+(.*)/;
      } else if (trimmedCondition.includes(' e ')) {
        operator = 'e';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+e\s+(.*)/;
      } else if (trimmedCondition.includes(' ei ')) {
        operator = 'ei';
        regex = /([a-zA-Z_][a-zA-Z0-9_.]*)\s+ei\s+(.*)/;
      } else {
        // If no operator found, assume equality
        const parts = trimmedCondition.split(' ');
        if (parts.length === 2) {
          const fieldName = parts[0];
          const value = parts[1].replace(/['"]/g, '');
          result[fieldName] = { "eq": value };
        }
        continue;
      }

      const match = trimmedCondition.match(regex);
      if (match) {
        const fieldName = match[1];
        let value = match[2].trim();
        
        // Remove quotes if present
        if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
          value = value.substring(1, value.length - 1);
        }
        
        result[fieldName] = { [operator]: value } as FilterCondition;
      }
    }
  }

  return JSON.stringify(result);
}

/**
 * Builds a filter string from search criteria object
 * @param filters The search criteria object
 * @returns A JSON string representation of the filters
 */
export function buildFilterFromCriteria(filters: any): string {
  const result: FilterObject = {};

  Object.keys(filters).forEach(key => {
    const value = filters[key];
    if (value !== undefined && value !== null && value !== '') {
      let filterKey = key; // Default to the key from the filters object
      let operator = 'eq'; // Default operator

      switch (key) {
        case 'codice':
          filterKey = 'codice_ufficiale';
          break;
        case 'descrizione':
          filterKey = 'descrizione';
          operator = 'si';
          break;
        case 'collegamento_bil_itas':
          // Il campo 'collegamento_bil_itas' non esiste nel DTO PdcFilter, quindi lo ignoriamo
          break;
        case 'classe':
          filterKey = 'pdc_classe_codice';
          break;
        case 'tipo':
          filterKey = 'pdc_tipo_codice';
          break;
        case 'stato':
          filterKey = 'pdc_stato_codice';
          break;
        case 'codiceEvento':
          filterKey = 'codice_evento';
          break;
        default:
          // For other fields, assume direct mapping and 'eq' operator
          break;
      }

      // Solo se il campo esiste nel DTO PdcFilter, lo aggiungiamo al risultato
      if (filterKey !== key ||
          ['famiglia', 'codice_evento', 'pdc_classe_codice', 'codice_ufficiale',
           'pdc_stato_codice', 'descrizione', 'livello', 'modificabile',
           'ente_denominazione', 'pdc_tipo_codice', 'segmento', 'foglia',
           'fondo_dotazione', 'a_partita', 'collegamento_bil_itas', 'inizio_validita',
           'fine_validita'].includes(filterKey)) {
        result[filterKey] = { [operator]: value } as FilterCondition;
      }
    }
  });

  return JSON.stringify(result);
}