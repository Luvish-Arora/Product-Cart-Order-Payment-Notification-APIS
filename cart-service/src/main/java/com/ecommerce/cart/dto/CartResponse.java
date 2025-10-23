package com.ecommerce.cart.dto;

import com.ecommerce.cart.entity.CartItem;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long userId;
    private List<CartItem> items;
    private int totalItems;
    private double totalAmount;
}