@echo off
title Aero DevTools - Beta Energistics
color 0A

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_181
set PATH=%JAVA_HOME%\bin;%PATH%

"%JAVA_HOME%\bin\bash" "%~dp0debug.sh"
