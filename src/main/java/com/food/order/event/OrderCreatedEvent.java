package com.food.order.event;



import com.food.order.model.Order;

public record OrderCreatedEvent(Order order) {}

