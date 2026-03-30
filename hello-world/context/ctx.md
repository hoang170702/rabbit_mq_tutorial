# Project Context Log

------ 15:29:27 30/03/2026 ------

## Project Overview
- **Technology Stack**: Java 21, Spring Boot 4.0.5
- **Purpose**: RabbitMQ Hello World Tutorial
- **Message Broker**: RabbitMQ (localhost:5672)

## Project Structure
- **Producer**: `learn.helloworld.producer.Send` - Gửi message "Hello RabbitMQ 🚀" vào queue
- **Consumer**: `learn.helloworld.consumer.Recv` - Nhận và xử lý message từ queue
- **Queue Config**: Quorum queue type, durable, manual ACK
- **Queue Name**: "hello"

## Dependencies
- Spring Boot Starter
- RabbitMQ AMQP Client 5.29.0

## Run Scripts Created
- `run-send.sh` / `run-send.bat` - Chạy Producer (Send)
- `run-recv.sh` / `run-recv.bat` - Chạy Consumer (Recv)

## Notes
- RabbitMQ server phải chạy trên localhost:5672
- Credentials: guest/guest
- Queue type: quorum (high availability)
- Consumer sử dụng manual ACK (best practice)

