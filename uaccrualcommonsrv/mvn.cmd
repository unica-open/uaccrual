REM SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
REM SPDX-License-Identifier: EUPL-1.2

@echo off

:: Legge il percorso di JAVA_HOME dal file di configurazione
for /f "tokens=2 delims==" %%a in ('findstr /b java.home buildfiles\local.properties') do set "JAVA_HOME_UNIX=%%a"

if "%JAVA_HOME_UNIX%"=="" (
  echo Errore: Impossibile trovare java.home nel file local.properties
  echo Creare questo file e specificare le configurazioni necessarie.
  exit /b 1
)

:: Converte il percorso da formato Unix a formato Windows
for /f "delims=" %%b in ('call %~dp0unix_to_win_path.bat "%JAVA_HOME_UNIX%"') do set "JAVA_HOME=%%b"

call %~dp0mvnw.cmd %* --settings .mvn\conf\settings.xml
