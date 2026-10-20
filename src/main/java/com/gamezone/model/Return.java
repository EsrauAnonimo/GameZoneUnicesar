package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product return in the GameZone system.
 * A return is associated with an original sale and contains the list
 * of returned products, the reason, and the calculated refund amount.
 * 
 * @author Dev 1
 */
public class Return {

    private String id;
    private LocalDate date;
    private Sale originalSale;
    private List<Product> returnedProducts;
    private String reason;
    private double refundAmount;

    /**
     * Constructor for a Return.
     * 
     * @param id Unique code that identifies the return
     * @param date Date when the return was made
     * @param originalSale The sale that is being returned
     * @param reason Reason for the return
     */
    public Return(String id, LocalDate date, Sale originalSale, String reason) {
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = new ArrayList<>();
        this.reason = reason;
        this.refundAmount = 0.0;
    }

    /**
     * Gets the return ID.
     * 
     * @return The return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the return date.
     * 
     * @return The return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Gets the original sale associated with this return.
     * 
     * @return The original sale
     */
    public Sale getOriginalSale() {
        return originalSale;
    }

    /**
     * Gets the list of returned products.
     * 
     * @return The list of returned products
     */
    public List<Product> getReturnedProducts() {
        return returnedProducts;
    }

    /**
     * Adds a product to the list of returned products.
     * 
     * @param product The product to add
     */
    public void addReturnedProduct(Product product) {
        this.returnedProducts.add(product);
    }

    /**
     * Gets the reason for the return.
     * 
     * @return The reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason for the return.
     * 
     * @param reason The new reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the refund amount.
     * 
     * @return The refund amount
     */
    public double getRefundAmount() {
        return refundAmount;
    }
    /**
     * Sets the refund amount.
     * 
     * @param refundAmount The new refund amount
     */
    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    /**
     * Calculates the refund amount proportionally according to the discount of the original sale.
     * Assigns the calculated value to the refundAmount attribute.
     * If the list of returned products is empty, the refund amount will be 0.0.
     * 
     * @return The calculated refund amount
     */
    public double calculateRefundAmount() {
        if (returnedProducts == null || returnedProducts.isEmpty()) {
            this.refundAmount = 0.0;
            return 0.0;
        }

        double subtotal = 0.0;
        double discount = 0.0;
        if (originalSale != null) {
            subtotal = originalSale.getSubtotal();
            discount = originalSale.getDiscountAmount();
        }

        double refundRatio = 1.0;
        if (subtotal > 0 && discount > 0) {
            refundRatio = 1.0 - (discount / subtotal);
            if (refundRatio < 0.0) {
                refundRatio = 0.0;
            }
        }

        double total = 0.0;
        for (Product product : returnedProducts) {
            if (product != null) {
                total += product.getPrice() * refundRatio;
            }
        }
        total = Math.round(total * 100.0) / 100.0;
        this.refundAmount = total;
        return total;
    }
        /**
     * Generates a formatted receipt for the return.
     * The receipt is written in Spanish and includes all return details.
     * 
     * @return A formatted string with the return receipt
     */
    public String generateReturnReceipt() {
    StringBuilder sb = new StringBuilder();
    sb.append("===== COMPROBANTE DE DEVOLUCION =====\n");
    sb.append("ID: ").append(id != null ? id : "N/A").append("\n");
    sb.append("Fecha: ").append(date != null ? date : "N/A").append("\n");
    sb.append("Venta original: ").append(originalSale != null ? originalSale.getId() : "N/A").append("\n");
    sb.append("Motivo: ").append(reason != null ? reason : "No especificado").append("\n");
    sb.append("Productos devueltos:\n");
    
    if (returnedProducts == null || returnedProducts.isEmpty()) {
        sb.append("  (No hay productos devueltos)\n");
    } else {
        for (Product product : returnedProducts) {
            if (product != null) {
                sb.append("  - ").append(product.getDescription()).append("\n");
            }
        }
    }
    
    sb.append("Monto reembolsado: $").append(String.format("%.2f", refundAmount)).append("\n");
    sb.append("=====================================");
    return sb.toString();
    }
}
