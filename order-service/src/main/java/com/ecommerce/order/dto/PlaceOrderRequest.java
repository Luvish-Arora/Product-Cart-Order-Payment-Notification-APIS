package com.ecommerce.order.dto;

import com.ecommerce.order.enums.PaymentMethod;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerMobile;
    private PaymentMethod paymentMethod;
    
    // Card details (only for CARD payment)
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
}