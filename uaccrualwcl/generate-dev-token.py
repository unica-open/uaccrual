#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

"""
Script per generare il token JWT statico per lo sviluppo ACCRUAL.

Questo script legge dev-info.json e genera un token JWT firmato
con la chiave segreta di sviluppo. Il token viene salvato in dev-info.json.

Utilizzo:
    python generate-dev-token.py

Requisiti:
    pip install PyJWT
"""

import json
import jwt
import datetime
import os

# Configurazione
DEV_JWT_SECRET = 'accrual-dev-secret-key-for-jwt-token-generation'
DEV_INFO_PATH = os.path.join('src', 'assets', 'config', 'dev-info.json')
TOKEN_EXPIRATION_HOURS = 24  # Scadenza token (ore)


def load_dev_info():
    """Carica i dati da dev-info.json"""
    print(f"Lettura di {DEV_INFO_PATH}...")
    
    with open(DEV_INFO_PATH, 'r', encoding='utf-8') as f:
        return json.load(f)


def create_jwt_payload(dev_info):
    """Crea il payload per il token JWT dai dati di dev-info.json"""
    now = datetime.datetime.utcnow()
    expiration = now + datetime.timedelta(hours=TOKEN_EXPIRATION_HOURS)
    
    payload = {
        # Standard claims
        'iss': 'accrualauth',
        'sub': dev_info.get('user', {}).get('username', 'DEVUSER'),
        'aud': 'accrual-services',
        'iat': int(now.timestamp()),
        'exp': int(expiration.timestamp()),
        'jti': str(__import__('uuid').uuid4()),
        
        # Claim personalizzati (coerenti con JwtUtils.java)
        'nonce': str(__import__('uuid').uuid4()),
        'utente_codice': dev_info.get('user', {}).get('username', 'DEVUSER'),
        'nome': dev_info.get('user', {}).get('displayName', 'Utente di Sviluppo'),
        'cognome': 'Sviluppo',
        'codiceFiscale': dev_info.get('user', {}).get('username', 'DEVUSER'),
        'codiceFiscaleEnte': dev_info.get('enteCodice', '80087670016'),
        'codice_ente': dev_info.get('enteCodice', '80087670016'),
        'denominazione_ente': dev_info.get('enteDenominazione', 'Regione Piemonte'),
        'anno_bilancio': dev_info.get('annoBilancio', 2025),
        'fase_bilancio': dev_info.get('faseBilancio', 'APERTO'),
        'ruolo': 'DEVELOPER',
        'source': 'DEV'
    }
    
    return payload


def generate_token(payload, secret):
    """Genera il token JWT firmato"""
    token = jwt.encode(
        payload,
        secret,
        algorithm='HS256',
        headers={'alg': 'HS256', 'typ': 'JWT'}
    )
    return token


def save_dev_info_with_token(dev_info, token):
    """Salva dev-info.json con il token aggiunto"""
    dev_info['token'] = token
    
    print(f"Scrittura di {DEV_INFO_PATH} con token...")
    with open(DEV_INFO_PATH, 'w', encoding='utf-8') as f:
        json.dump(dev_info, f, indent=2, ensure_ascii=False)


def verify_token(token, secret):
    """Verifica che il token sia valido"""
    try:
        decoded = jwt.decode(token, secret, algorithms=['HS256'])
        print(f"✓ Token verificato con successo!")
        print(f"  Subject: {decoded.get('sub')}")
        print(f"  Scadenza: {datetime.datetime.fromtimestamp(decoded['exp']).strftime('%Y-%m-%d %H:%M:%S')}")
        return True
    except jwt.ExpiredSignatureError:
        print("✗ Token scaduto!")
        return False
    except jwt.InvalidTokenError as e:
        print(f"✗ Token non valido: {e}")
        return False


def main():
    print("=" * 60)
    print("Generazione Token JWT per Sviluppo ACCRUAL")
    print("=" * 60)
    print()
    
    # Carica dev-info.json
    try:
        dev_info = load_dev_info()
    except FileNotFoundError:
        print(f"Errore: File {DEV_INFO_PATH} non trovato!")
        return 1
    except json.JSONDecodeError as e:
        print(f"Errore: JSON non valido in {DEV_INFO_PATH}: {e}")
        return 1
    
    # Crea payload
    print("Creazione payload JWT...")
    payload = create_jwt_payload(dev_info)
    
    # Genera token
    print(f"Firma token con algoritmo HS256...")
    token = generate_token(payload, DEV_JWT_SECRET)
    
    # Salva token in dev-info.json
    save_dev_info_with_token(dev_info, token)
    
    # Verifica token
    print()
    verify_token(token, DEV_JWT_SECRET)
    
    print()
    print("=" * 60)
    print("Token generato con successo!")
    print(f"Scadenza: {TOKEN_EXPIRATION_HOURS} ore")
    print("=" * 60)
    print()
    print("Nota: Il token è stato salvato in dev-info.json")
    print("Quando scade, esegui nuovamente questo script.")
    print()
    
    return 0


if __name__ == '__main__':
    exit(main())
