package com.food.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public CompletableFuture<SendResult<String, Object>> sendOrderCreatedEvent(Object orderEvent) {
        return kafkaTemplate.send("order-created", orderEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order created event sent successfully. Topic: {}, Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send order created event", ex);
                    }
                });
    }

    @Async
    public CompletableFuture<SendResult<String, Object>> sendOrderStatusChangedEvent(Object orderStatusEvent) {
        return kafkaTemplate.send("order-status-changed", orderStatusEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order status changed event sent successfully. Topic: {}, Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send order status changed event", ex);
                    }
                });
    }

    @Async
    public CompletableFuture<SendResult<String, Object>> sendOrderCancelledEvent(Object orderCancelledEvent) {
        return kafkaTemplate.send("order-cancelled", orderCancelledEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order cancelled event sent successfully. Topic: {}, Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send order cancelled event", ex);
                    }
                });
    }
}


