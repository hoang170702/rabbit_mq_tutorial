# RabbitMQ Hello World Tutorial

Dự án demo RabbitMQ với Java 21 và Spring Boot 4.0.5

## Yêu cầu

- Java 21
- Maven
- RabbitMQ Server chạy trên `localhost:5672`
- Credentials: `guest/guest`

## Cấu trúc dự án

```
hello-world/
├── src/main/java/learn/helloworld/
│   ├── producer/Send.java          # Producer - gửi message
│   ├── consumer/Recv.java          # Consumer - nhận message
│   ├── config/RabbitMQConfig.java  # Cấu hình kết nối RabbitMQ
│   └── constant/QueueConstant.java # Tên queue
├── run-send.bat / run-send.sh      # Script chạy Producer
├── run-recv.bat / run-recv.sh      # Script chạy Consumer
└── README.md
```

## Cài đặt RabbitMQ Server

### Windows (với Chocolatey):
```powershell
choco install rabbitmq
```

### macOS (với Homebrew):
```bash
brew install rabbitmq
brew services start rabbitmq
```

### Docker:
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Truy cập RabbitMQ Management UI: http://localhost:15672 (guest/guest)

## Cách chạy

### Bước 1: Compile project
```powershell
.\mvnw.cmd clean compile
```

### Bước 2: Chạy Consumer (Terminal 1)
**PowerShell:**
```powershell
.\run-recv.bat
```

**Bash/Linux/macOS:**
```bash
./run-recv.sh
```

**Hoặc dùng Maven trực tiếp:**
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="learn.helloworld.consumer.Recv"
```

Consumer sẽ chạy liên tục và hiển thị:
```
[*] Waiting for messages...
```

### Bước 3: Chạy Producer (Terminal 2)
**PowerShell:**
```powershell
.\run-send.bat
```

**Bash/Linux/macOS:**
```bash
./run-send.sh
```

**Hoặc dùng Maven trực tiếp:**
```powershell
.\mvnw.cmd exec:java -Dexec.mainClass="learn.helloworld.producer.Send"
```

Producer sẽ gửi message và thoát:
```
[x] Sent: Hello RabbitMQ 🚀
```

Consumer sẽ nhận và hiển thị:
```
[x] Received: Hello RabbitMQ 🚀
```

## Chi tiết kỹ thuật

### Queue Configuration
- **Queue name**: `hello`
- **Queue type**: Quorum (high availability)
- **Durable**: `true` (lưu vào disk)
- **Exclusive**: `false` (nhiều consumer có thể kết nối)
- **Auto-delete**: `false` (giữ queue sau khi consumer disconnect)

### Consumer Configuration
- **Manual ACK**: `true` (best practice - xác nhận thủ công sau khi xử lý xong)
- **Auto ACK**: `false`

### Connection Settings
- **Host**: `localhost`
- **Port**: `5672`
- **Username**: `guest`
- **Password**: `guest`

## Troubleshooting

### Lỗi: Connection refused
- Kiểm tra RabbitMQ server đã chạy chưa
- Verify port 5672 đang mở

### Lỗi: Access refused
- Kiểm tra username/password trong `RabbitMQConfig.java`
- Default credentials: guest/guest

### Lỗi: PowerShell không nhận diện script
- Thêm `.\` trước tên file: `.\run-recv.bat`

## Dependencies

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.29.0</version>
</dependency>
```

## Tài liệu tham khảo

- [RabbitMQ Official Tutorial](https://www.rabbitmq.com/tutorials/tutorial-one-java.html)
- [RabbitMQ Java Client API](https://rabbitmq.github.io/rabbitmq-java-client/api/current/)
