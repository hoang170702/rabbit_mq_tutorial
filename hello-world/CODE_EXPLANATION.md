# Giải thích Code RabbitMQ

## 1. RabbitMQConfig.java - Cấu hình kết nối

```java
public class RabbitMQConfig {
    public static Connection getConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");      // RabbitMQ server address
        factory.setPort(5672);             // AMQP port
        factory.setUsername("guest");      // Username
        factory.setPassword("guest");      // Password

        return factory.newConnection();    // Tạo connection mới
    }
}
```

**Giải thích:**
- `ConnectionFactory`: Factory pattern để tạo connection đến RabbitMQ
- `setHost("localhost")`: Kết nối đến RabbitMQ server trên máy local
- `setPort(5672)`: Port mặc định của AMQP protocol
- `newConnection()`: Tạo TCP connection đến RabbitMQ server

---

## 2. QueueConstant.java - Định nghĩa tên queue

```java
public class QueueConstant {
    public static final String QUEUE_NAME = "hello";
}
```

**Giải thích:**
- Tập trung tên queue ở 1 nơi để dễ quản lý
- Producer và Consumer đều dùng chung constant này
- Tránh typo khi hardcode string

---

## 3. Send.java - Producer (Gửi message)

### 3.1. Khởi tạo Connection và Channel

```java
try (Connection connection = RabbitMQConfig.getConnection();
     Channel channel = connection.createChannel()) {
```

**Giải thích:**
- `try-with-resources`: Tự động đóng connection và channel sau khi xong
- `Connection`: TCP connection đến RabbitMQ server
- `Channel`: Virtual connection trong 1 connection (nhẹ hơn, dùng để gửi/nhận message)

### 3.2. Khai báo Queue

```java
Map<String, Object> queueArgs = Map.of("x-queue-type", "quorum");

channel.queueDeclare(
    QueueConstant.QUEUE_NAME,  // Tên queue
    true,                      // durable: lưu queue vào disk
    false,                     // exclusive: cho phép nhiều consumer
    false,                     // autoDelete: giữ queue khi không có consumer
    queueArgs                  // arguments: quorum queue type
);
```

**Giải thích:**
- `queueDeclare()`: Tạo queue nếu chưa tồn tại, hoặc verify nếu đã có
- **durable = true**: Queue sẽ tồn tại sau khi RabbitMQ restart
- **exclusive = false**: Nhiều consumer có thể connect vào queue này
- **autoDelete = false**: Queue không tự xóa khi consumer disconnect
- **x-queue-type = quorum**: Sử dụng quorum queue (replicated, high availability)

**Quorum Queue vs Classic Queue:**
- Quorum: Replicated across multiple nodes, data safety cao hơn
- Classic: Single node, performance cao hơn nhưng ít reliable

### 3.3. Gửi Message

```java
String message = "Hello RabbitMQ 🚀";

channel.basicPublish(
    "",                        // exchange: "" = default exchange
    QueueConstant.QUEUE_NAME,  // routing key = queue name
    null,                      // properties: null = default
    message.getBytes()         // message body dạng byte array
);

System.out.println(" [x] Sent: " + message);
```

**Giải thích:**
- `basicPublish()`: Gửi message vào RabbitMQ
- **exchange = ""**: Dùng default exchange (direct exchange)
- **routing key**: Với default exchange, routing key = tên queue
- **message.getBytes()**: Convert string thành byte array

**Flow:**
```
Producer → Default Exchange → Routing Key "hello" → Queue "hello"
```

---

## 4. Recv.java - Consumer (Nhận message)

### 4.1. Khởi tạo Connection và Channel

```java
Connection connection = RabbitMQConfig.getConnection();
Channel channel = connection.createChannel();
```

**Lưu ý:** 
- Consumer KHÔNG dùng try-with-resources vì cần giữ connection mở liên tục
- Connection sẽ đóng khi user tắt chương trình (Ctrl+C)

### 4.2. Khai báo Queue (giống Producer)

```java
Map<String, Object> queueArgs = Map.of("x-queue-type", "quorum");

channel.queueDeclare(
    QueueConstant.QUEUE_NAME,
    true,
    false,
    false,
    queueArgs
);
```

**Tại sao Consumer cũng phải declare queue?**
- Đảm bảo queue tồn tại trước khi consume
- Nếu Consumer chạy trước Producer, queue vẫn được tạo
- Idempotent operation: Declare nhiều lần không gây lỗi

### 4.3. Định nghĩa Callback xử lý message

```java
DeliverCallback deliverCallback = (consumerTag, delivery) -> {
    String message = new String(delivery.getBody());
    System.out.println(" [x] Received: " + message);

    // ACK message sau khi xử lý xong
    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
};
```

**Giải thích:**
- `DeliverCallback`: Functional interface, được gọi khi có message mới
- `consumerTag`: ID của consumer (do RabbitMQ tự generate)
- `delivery`: Object chứa message và metadata
- `delivery.getBody()`: Lấy message body dạng byte array
- `basicAck()`: Xác nhận đã xử lý xong message

**Tại sao cần ACK?**
- Đảm bảo message không bị mất nếu consumer crash giữa chừng
- RabbitMQ chỉ xóa message khỏi queue sau khi nhận ACK
- Nếu consumer crash trước khi ACK, message sẽ được gửi lại cho consumer khác

**basicAck() parameters:**
```java
channel.basicAck(
    delivery.getEnvelope().getDeliveryTag(),  // ID của message
    false                                      // multiple: false = chỉ ACK 1 message này
);
```

### 4.4. Bắt đầu consume

```java
channel.basicConsume(
    QueueConstant.QUEUE_NAME,  // Queue name
    false,                     // autoAck: false = manual ACK
    deliverCallback,           // Callback khi nhận message
    consumerTag -> {}          // Callback khi consumer bị cancel (empty)
);
```

**Giải thích:**
- `basicConsume()`: Đăng ký consumer để nhận message liên tục
- **autoAck = false**: Manual ACK (best practice)
- **deliverCallback**: Function xử lý message
- **cancelCallback**: Function xử lý khi consumer bị cancel (ở đây để trống)

**Auto ACK vs Manual ACK:**

| Auto ACK | Manual ACK |
|----------|------------|
| RabbitMQ ACK ngay khi gửi message | Developer phải gọi `basicAck()` |
| Message có thể mất nếu consumer crash | Message an toàn hơn |
| Performance cao hơn | Reliable hơn |
| Không nên dùng trong production | Best practice ✅ |

---

## 5. Message Flow

### Producer Flow:
```
1. Producer tạo connection → RabbitMQ
2. Producer tạo channel
3. Producer declare queue "hello"
4. Producer gửi message "Hello RabbitMQ 🚀"
5. Message được lưu vào queue "hello"
6. Producer đóng connection và thoát
```

### Consumer Flow:
```
1. Consumer tạo connection → RabbitMQ
2. Consumer tạo channel
3. Consumer declare queue "hello"
4. Consumer đăng ký callback để nhận message
5. Consumer chờ message (blocking)
6. Khi có message:
   - Callback được gọi
   - In ra message
   - Gửi ACK về RabbitMQ
   - RabbitMQ xóa message khỏi queue
7. Consumer tiếp tục chờ message tiếp theo
```

### Sequence Diagram:
```
Producer                RabbitMQ                Consumer
   |                       |                       |
   |--[1] Connect--------->|                       |
   |--[2] Declare Queue--->|                       |
   |--[3] Publish Msg----->|                       |
   |                       |<--[4] Connect---------|
   |                       |<--[5] Declare Queue---|
   |                       |<--[6] Subscribe-------|
   |                       |                       |
   |                       |--[7] Deliver Msg----->|
   |                       |                       |--[8] Process
   |                       |<--[9] ACK-------------|
   |                       |--[10] Delete Msg      |
```

---

## 6. Best Practices được áp dụng

### ✅ Manual ACK
```java
channel.basicConsume(QUEUE_NAME, false, deliverCallback, ...);
channel.basicAck(deliveryTag, false);
```
- Đảm bảo message không bị mất

### ✅ Durable Queue
```java
channel.queueDeclare(QUEUE_NAME, true, ...);
```
- Queue tồn tại sau khi RabbitMQ restart

### ✅ Quorum Queue
```java
Map.of("x-queue-type", "quorum")
```
- High availability, replicated data

### ✅ Try-with-resources (Producer)
```java
try (Connection conn = ...; Channel ch = ...) { }
```
- Tự động đóng resources

### ✅ Connection pooling
- Trong production nên dùng connection pool thay vì tạo connection mới mỗi lần

---

## 7. Các khái niệm quan trọng

### Connection vs Channel
- **Connection**: TCP connection đến RabbitMQ (nặng, tốn tài nguyên)
- **Channel**: Virtual connection trong 1 connection (nhẹ, nên dùng nhiều channel thay vì nhiều connection)

### Exchange Types
- **Default Exchange ("")**: Direct routing theo queue name
- **Direct Exchange**: Routing theo routing key chính xác
- **Topic Exchange**: Routing theo pattern (e.g., "log.*")
- **Fanout Exchange**: Broadcast đến tất cả queue

### Message Acknowledgment
- **Auto ACK**: Không an toàn, message có thể mất
- **Manual ACK**: An toàn, developer kiểm soát
- **NACK**: Từ chối message, có thể requeue

### Queue Types
- **Classic**: Single node, fast
- **Quorum**: Replicated, safe, recommended
- **Stream**: Append-only log, for high throughput

---

## 8. Troubleshooting

### Message không được consume?
- Kiểm tra Consumer có chạy không
- Kiểm tra queue name có đúng không
- Check RabbitMQ Management UI: http://localhost:15672

### Connection refused?
- RabbitMQ server chưa chạy
- Port 5672 bị block bởi firewall

### Message bị mất?
- Kiểm tra durable = true
- Kiểm tra manual ACK
- Xem log RabbitMQ

### Performance chậm?
- Dùng connection pool
- Batch publish nhiều message
- Tăng prefetch count cho consumer
