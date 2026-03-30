@echo off
REM Script to run Send (Producer) class

cd /d "%~dp0"

echo Starting RabbitMQ Producer (Send)...
call mvnw.cmd compile exec:java -Dexec.mainClass="learn.helloworld.producer.Send"
