package com.ecommerce.cart.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Long userId;
    private Long productId;
    private Integer quantity;
}