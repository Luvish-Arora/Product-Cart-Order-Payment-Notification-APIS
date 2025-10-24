package com.ecommerce.order.dto;

import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.enums.PaymentMethod;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerMobile;
    private Double totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private LocalDate estimatedDeliveryDate;
    private List<OrderItem> orderItems;
    private PaymentMethod paymentMethod;
    private String message;
}