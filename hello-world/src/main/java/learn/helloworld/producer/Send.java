package learn.helloworld.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import learn.helloworld.config.RabbitMQConfig;
import learn.helloworld.constant.QueueConstant;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Send {
    public static void main(String[] args) throws IOException {
        try (Connection connection = RabbitMQConfig.getConnection();
             Channel channel = connection.createChannel()) {
            Map<String, Object> queueArgs = Map.of("x-queue-type", "quorum");
            // declare queue
            channel.queueDeclare(
                    QueueConstant.QUEUE_NAME,
                    true,   // save disk
                    false, // many consumer
                    false, // retain queue
                    queueArgs
            );

            String message = "Hello RabbitMQ 🚀";

            channel.basicPublish(
                    "",
                    QueueConstant.QUEUE_NAME,
                    null,
                    message.getBytes()
            );
            System.out.println(" [x] Sent: " + message);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
