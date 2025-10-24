package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.Payment;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.enums.PaymentStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Step 1: Get cart items from Cart Service
        Map<String, Object> cartData = getCartFromService(request.getUserId());
        
        if (cartData == null) {
            throw new RuntimeException("Cart not found for user: " + request.getUserId());
        }

        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cartData.get("items");
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        Double totalAmount = (Double) cartData.get("totalAmount");

        // Step 2: Create Order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.getUserId());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerMobile(request.getCustomerMobile());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setEstimatedDeliveryDate(calculateDeliveryDate());

        // Step 3: Add Order Items
        for (Map<String, Object> cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(((Number) cartItem.get("productId")).longValue());
            orderItem.setProductName((String) cartItem.get("productName"));
            orderItem.setProductPrice((Double) cartItem.get("productPrice"));
            orderItem.setQuantity((Integer) cartItem.get("quantity"));
            orderItem.setSubtotal((Double) cartItem.get("subtotal"));
            order.getOrderItems().add(orderItem);
        }

        // Save order first (to get order ID)
        order = orderRepository.save(order);

        // Step 4: Process Payment
        Payment payment = paymentService.processPayment(order, request);
        order.setPayment(payment);

        // Step 5: Update order status based on payment
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED); // COD is also confirmed
        }

        order = orderRepository.save(order);

        // Step 6: Clear Cart
        clearCartFromService(request.getUserId());

        // Step 7: Send Email Notification
        notificationService.sendOrderConfirmationEmail(order);

        // Step 8: Build Response
        return buildOrderResponse(order, "Order placed successfully!");
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return buildOrderResponse(order, null);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
        return buildOrderResponse(order, null);
    }

    // Helper: Generate unique order number
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // Helper: Calculate delivery date (4-9 days from now)
    private LocalDate calculateDeliveryDate() {
        Random random = new Random();
        int daysToAdd = 4 + random.nextInt(6); // Random between 4-9
        return LocalDate.now().plusDays(daysToAdd);
    }

    // Helper: Get cart from Cart Service
    private Map<String, Object> getCartFromService(Long userId) {
        try {
            String url = cartServiceUrl + "/" + userId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching cart from Cart Service: " + e.getMessage());
        }
    }

    // Helper: Clear cart from Cart Service
    private void clearCartFromService(Long userId) {
        try {
            String url = cartServiceUrl + "/" + userId + "/clear";
            restTemplate.delete(url);
        } catch (Exception e) {
            System.err.println("Warning: Failed to clear cart: " + e.getMessage());
        }
    }

    // Helper: Build order response
    private OrderResponse buildOrderResponse(Order order, String message) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerMobile(order.getCustomerMobile());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setOrderDate(order.getOrderDate());
        response.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        response.setOrderItems(order.getOrderItems());
        response.setPaymentMethod(order.getPayment().getPaymentMethod());
        response.setMessage(message);
        return response;
    }
}