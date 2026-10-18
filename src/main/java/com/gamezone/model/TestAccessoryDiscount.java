package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class TestAccessoryDiscount {

    public static void main(String[] args) {
        // 1. Creamos un producto de tipo Accessory (usando una subclase como Controller)
        Controller control = new Controller("ACC1", "Control PS5", 100.0, 10, "Inalámbrico");
        
        // 2. Creamos una venta de prueba y le agregamos el producto
        // (Ajusta esto según cómo esté creado el constructor de tu clase Sale)
        Sale venta = new Sale("SALE1", LocalDate.now(), null, null, new ArrayList<>());
        venta.getProducts().add(control);

        // 3. Creamos el descuento del 20% para ACCESSORY
        CategoryDiscount descuento = new CategoryDiscount(
                "PROMO1", "Descuento Accesorios", LocalDate.now(), LocalDate.now().plusDays(5), 
                20.0, CategoryDiscount.CATEGORY_ACCESSORY
        );

        // 4. Calculamos el descuento
        double descuentoCalculado = descuento.calculateDiscount(venta);

        // 5. Mostramos el resultado en consola
        System.out.println("Precio del accesorio: $" + control.getPrice());
        System.out.println("Descuento calculado (20%): $" + descuentoCalculado);
        
        if (descuentoCalculado == 20.0) {
            System.out.println("✅ ¡PRUEBA EXITOSA! El cálculo es correcto.");
        } else {
            System.out.println("❌ ERROR: El cálculo esperado era 20.0 pero dio " + descuentoCalculado);
        }
    }
}
