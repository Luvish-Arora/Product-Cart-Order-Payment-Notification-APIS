package com.ecommerce.cart.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {
    private Integer quantity;
}