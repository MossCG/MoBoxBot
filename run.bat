@echo off
chcp 65001 >nul
cd /d "%~dp0"
java -Dfile.encoding=UTF-8 -Djava.awt.headless=true -jar MoBoxBot.jar
