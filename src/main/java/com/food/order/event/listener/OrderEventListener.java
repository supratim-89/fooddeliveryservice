package com.food.order.event.listener;

import com.food.order.event.OrderCreatedEvent;
import com.food.order.kafka.OrderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderProducer orderProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Publishing Kafka event for order {}", event.order().getId());

        orderProducer.sendOrderCreatedEvent(event.order());
    }
}
