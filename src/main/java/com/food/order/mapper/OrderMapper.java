package com.food.order.mapper;

import com.food.order.dto.OrderRequest;
import com.food.order.model.Order;
import com.food.order.model.OrderItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(
	    componentModel = "spring",
	    builder = @org.mapstruct.Builder(disableBuilder = true) // 🔑 IMPORTANT
	)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", expression = "java(generateOrderNumber())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toOrder(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toOrderItem(OrderRequest.OrderItemRequest request);

    @AfterMapping
    default void linkItems(@MappingTarget Order order, OrderRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = toOrderItem(itemReq);
            item.setUnitPrice(BigDecimal.valueOf(10));
            item.setTotalPrice(
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            order.addItem(item);
            total = total.add(item.getTotalPrice());
        }

        order.setTotalAmount(total);
    }

    default String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
