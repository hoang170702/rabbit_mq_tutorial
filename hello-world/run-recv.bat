@echo off
REM Script to run Recv (Consumer) class

cd /d "%~dp0"

echo Starting RabbitMQ Consumer (Recv)...
call mvnw.cmd compile exec:java -Dexec.mainClass="learn.helloworld.consumer.Recv"
