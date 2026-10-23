package com.gamezone.ui;

import com.gamezone.model.Person;
import com.gamezone.model.Sale;

/**
 * Pure Fabrication responsible for formatting a Sale into a human-readable
 * receipt string for console presentation.
 * <p>
 * This class extracts presentation formatting out of the {@link Sale} domain
 * entity, restoring High Cohesion in the domain layer (V-L02).
 */
public class ReceiptFormatter {

    private static final String NO_PROMOTION_LABEL = "(sin promocion)";

    /**
     * Formats the textual receipt of the sale, showing the subtotal, the
     * applied discount (including the name of the promotion), the extended
     * warranty cost and the final total.
     *
     * @param sale the sale to format into a receipt
     * @return the receipt as a multi-line string
     */
    public static String formatReceipt(Sale sale) {
        if (sale == null) {
            return "";
        }
        StringBuilder receipt = new StringBuilder();
        receipt.append("\n=== Recibo GameZone Unicesar ===\n");
        receipt.append("Venta: ").append(sale.getId()).append('\n');
        receipt.append("Fecha: ").append(sale.getDate()).append('\n');
        receipt.append("Cliente: ").append(describe(sale.getCustomer())).append('\n');
        receipt.append("Vendedor: ").append(describe(sale.getSeller())).append('\n');
        receipt.append("Productos: ").append(sale.getProducts() != null ? sale.getProducts().size() : 0).append('\n');
        receipt.append(String.format("Subtotal: %.2f%n", sale.getSubtotal()));
        if (sale.getAppliedPromotionName() != null) {
            receipt.append(String.format("Descuento (%s): -%.2f%n", sale.getAppliedPromotionName(), sale.getDiscountAmount()));
        } else {
            receipt.append(String.format("Descuento %s: -%.2f%n", NO_PROMOTION_LABEL, sale.getDiscountAmount()));
        }
        receipt.append(String.format("Garantias extendidas: +%.2f%n", sale.getExtendedWarrantyCost()));
        receipt.append(String.format("Total: %.2f", sale.getTotal()));
        receipt.append("\n=== Fin del recibo ===");
        return receipt.toString();
    }

    /**
     * Returns a safe textual identifier for a person, so that building a
     * receipt never fails because of an incomplete sale.
     *
     * @param person the customer or seller of the sale
     * @return the person id, or a placeholder when the person is null
     */
    private static String describe(Person person) {
        return person == null ? "(desconocido)" : person.getId();
    }

    /**
     * Formats the textual receipt of a product return, showing the return ID,
     * the date, original sale ID, reason, returned products, and refund amount.
     *
     * @param productReturn the return to format into a receipt
     * @return the receipt as a multi-line string
     */
    public static String formatReturnReceipt(com.gamezone.model.Return productReturn) {
        if (productReturn == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("===== COMPROBANTE DE DEVOLUCION =====\n");
        sb.append("ID: ").append(productReturn.getId() != null ? productReturn.getId() : "N/A").append("\n");
        sb.append("Fecha: ").append(productReturn.getDate() != null ? productReturn.getDate() : "N/A").append("\n");
        sb.append("Venta original: ").append(productReturn.getOriginalSale() != null ? productReturn.getOriginalSale().getId() : "N/A").append("\n");
        sb.append("Motivo: ").append(productReturn.getReason() != null ? productReturn.getReason() : "No especificado").append("\n");
        sb.append("Productos devueltos:\n");
        
        java.util.List<com.gamezone.model.Product> products = productReturn.getReturnedProducts();
        if (products == null || products.isEmpty()) {
            sb.append("  (No hay productos devueltos)\n");
        } else {
            for (com.gamezone.model.Product product : products) {
                if (product != null) {
                    sb.append("  - ").append(product.getDescription()).append("\n");
                }
            }
        }
        
        sb.append("Monto reembolsado: $").append(String.format("%.2f", productReturn.getRefundAmount())).append("\n");
        sb.append("=====================================");
        return sb.toString();
    }
}
