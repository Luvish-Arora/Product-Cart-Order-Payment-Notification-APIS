package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmationEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomerEmail());
            message.setSubject("Order Confirmation - " + order.getOrderNumber());
            message.setText(buildEmailContent(order));
            
            mailSender.send(message);
            System.out.println("✅ Order confirmation email sent to: " + order.getCustomerEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            // Don't throw exception - order should still be placed even if email fails
        }
    }

    private String buildEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        
        content.append("Dear ").append(order.getCustomerName()).append(",\n\n");
        content.append("Thank you for your order!\n\n");
        content.append("Order Details:\n");
        content.append("═══════════════════════════════════\n");
        content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        content.append("Order Date: ").append(order.getOrderDate()).append("\n");
        content.append("Estimated Delivery: ").append(order.getEstimatedDeliveryDate()).append("\n\n");
        
        content.append("Items Ordered:\n");
        content.append("─────────────────────────────────\n");
        for (OrderItem item : order.getOrderItems()) {
            content.append(String.format("• %s x%d - ₹%.2f\n", 
                item.getProductName(), 
                item.getQuantity(), 
                item.getSubtotal()));
        }
        
        content.append("─────────────────────────────────\n");
        content.append(String.format("Total Amount: ₹%.2f\n\n", order.getTotalAmount()));
        
        content.append("Payment Method: ").append(order.getPayment().getPaymentMethod()).append("\n");
        content.append("Payment Status: ").append(order.getPayment().getPaymentStatus()).append("\n\n");
        
        content.append("We will notify you once your order is shipped.\n\n");
        content.append("Thank you for shopping with us!\n\n");
        content.append("Best regards,\n");
        content.append("E-Commerce Team");
        
        return content.toString();
    }
}