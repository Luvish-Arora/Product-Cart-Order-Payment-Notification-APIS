package com.ecommerce.order.service;

import com.ecommerce.order.dto.PlaceOrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.Payment;
import com.ecommerce.order.enums.PaymentMethod;
import com.ecommerce.order.enums.PaymentStatus;
import com.ecommerce.order.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Order order, PlaceOrderRequest request) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(order.getTotalAmount());

        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            // Cash on Delivery - Mark as PENDING
            payment.setPaymentStatus(PaymentStatus.PENDING);
        } else if (request.getPaymentMethod() == PaymentMethod.CARD) {
            // Simulate card payment processing
            boolean paymentSuccess = processCardPayment(request);
            
            if (paymentSuccess) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                // Store last 4 digits of card
                String cardNumber = request.getCardNumber();
                payment.setCardLastFour(cardNumber.substring(cardNumber.length() - 4));
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                throw new RuntimeException("Card payment failed. Please check your card details.");
            }
        }

        return paymentRepository.save(payment);
    }

    // Simulate card payment (In real scenario, integrate with payment gateway)
    private boolean processCardPayment(PlaceOrderRequest request) {
        // Basic validation
        if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
            return false;
        }
        if (request.getCvv() == null || request.getCvv().length() != 3) {
            return false;
        }
        
        // Simulate payment processing delay
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // In real scenario: Call payment gateway API
        // For now, always return true (success)
        return true;
    }
}