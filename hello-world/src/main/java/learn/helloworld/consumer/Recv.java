package learn.helloworld.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import learn.helloworld.config.RabbitMQConfig;
import learn.helloworld.constant.QueueConstant;

import java.util.Map;

public class Recv {
    public static void main(String[] args) {
        try {
            Connection connection = RabbitMQConfig.getConnection();
            Channel channel = connection.createChannel();

            Map<String, Object> queueArgs = Map.of("x-queue-type", "quorum");

            channel.queueDeclare(
                    QueueConstant.QUEUE_NAME,
                    true,
                    false,
                    false,
                    queueArgs
            );

            System.out.println(" [*] Waiting for messages...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody());
                System.out.println(" [x] Received: " + message);

                // ACK message
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            // autoAck = false (best practice)
            channel.basicConsume(
                    QueueConstant.QUEUE_NAME,
                    false,
                    deliverCallback,
                    consumerTag -> {
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
