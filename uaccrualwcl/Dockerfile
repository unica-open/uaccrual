# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Usa un'immagine base di Node.js LTS
FROM node:20-alpine AS builder

# Imposta la directory di lavoro
WORKDIR /usr/src/app

# Copia i file di configurazione e le dipendenze
COPY package*.json ./

# Installa tutte le dipendenze, incluse quelle di sviluppo per la build
RUN npm ci

# Copia i file sorgenti
COPY . .

# Esegue la build dell'applicazione Angular con configurazione docker
RUN npm run build -- --configuration=docker

# Passa a un'immagine nginx per servire l'applicazione
FROM nginx:alpine

# Rimuove la configurazione predefinita di nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia il file di configurazione personalizzato per nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copia i file della build dall'immagine precedente
COPY --from=builder /usr/src/app/dist/accrualwcl-ng/browser /usr/share/nginx/html

# Espone la porta 60080
EXPOSE 60080

# Comando di avvio (opzionale, poiché nginx parte automaticamente)
CMD ["nginx", "-g", "daemon off;"]