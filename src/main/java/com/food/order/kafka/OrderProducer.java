package com.food.order.kafka;

import com.food.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_STATUS_CHANGED_TOPIC = "order.status.changed";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // -------------------------------
    // Order Created
    // -------------------------------
    public void sendOrderCreatedEvent(Order order) {

        kafkaTemplate.send(
                ORDER_CREATED_TOPIC,
                order.getId().toString(),
                order
        ).whenComplete((result, ex) -> {

            if (ex == null) {
                log.info(
                    "Kafka order-created event sent | orderId={} | topic={} | partition={} | offset={}",
                    order.getId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.error(
                    "Failed to send order-created event | orderId={}",
                    order.getId(),
                    ex
                );
            }
        });
    }

    // -------------------------------
    // Order Status Changed
    // -------------------------------
    public void sendOrderStatusChangedEvent(Object event, String orderId) {

        kafkaTemplate.send(
                ORDER_STATUS_CHANGED_TOPIC,
                orderId,
                event
        ).whenComplete((result, ex) -> {

            if (ex == null) {
                log.info(
                    "Kafka order-status-changed event sent | orderId={} | topic={} | partition={} | offset={}",
                    orderId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.error(
                    "Failed to send order-status-changed event | orderId={}",
                    orderId,
                    ex
                );
            }
        });
    }

    // -------------------------------
    // Order Cancelled
    // -------------------------------
    public void sendOrderCancelledEvent(Object event, String orderId) {

        kafkaTemplate.send(
                ORDER_CANCELLED_TOPIC,
                orderId,
                event
        ).whenComplete((result, ex) -> {

            if (ex == null) {
                log.info(
                    "Kafka order-cancelled event sent | orderId={} | topic={} | partition={} | offset={}",
                    orderId,
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.error(
                    "Failed to send order-cancelled event | orderId={}",
                    orderId,
                    ex
                );
            }
        });
    }
}
