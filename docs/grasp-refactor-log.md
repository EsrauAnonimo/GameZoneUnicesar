# Bitácora de Refactorización GRASP y Estrategia de No Regresión

**Proyecto:** GameZone Unicesar  
**Fase:** Fase 2 — Corrección GRASP (Requerimiento 7)  
**Rama:** `feature/grasp-refactor`  
**Responsable (Líder Técnico):** Esteban Raúl Vergara Jaime  

---

## 1. Estrategia de Verificación de No Regresión

Para asegurar que las modificaciones de diseño y desacoplamiento no alteren el comportamiento funcional del sistema ni rompan los flujos interactivos de `ConsoleUI`, el equipo sigue una estrategia rigurosa de pruebas manuales y validación de compilación continua.

### 1.1 Reglas Fundamentales de Implementación
1. **Simplicidad ante todo (KISS):** Código directo, legible y funcional.
2. **Restricción de APIs avanzadas:** Cero interfaces nuevas, cero `Optional`, cero `Stream` API.
3. **Mecanismos permitidos:** Herencia estándar con `@Override`, clases abstractas, polimorfismo clásico y métodos privados auxiliares.
4. **Validación de Compilación:** Toda intervención debe compilar limpiamente bajo Java 17 sin errores ni dependencias externas.

---

### 1.2 Casos de Prueba en el Menú (`ConsoleUI`)

A continuación se definen los escenarios específicos que se ejecutan en la interfaz de consola para garantizar la no regresión:

| ID Caso | Flujo en Menú | Acción y Datos de Entrada | Resultado Esperado (Criterio de Aceptación) |
|---|---|---|---|
| **NR-01** | Menú Productos / Accesorios | Opción de listar todos los productos y accesorios. | Muestra el catálogo completo con títulos, precios y existencias disponibles intactas. |
| **NR-02** | Menú Ventas $\rightarrow$ Registrar Venta (Estándar) | Venta de 1 videojuego y 1 consola sin solicitar garantía extendida. | Subtotal = suma de precios. Descuento según promociones aplicables. Sin costo de garantía extendida. Total correcto. Stock de ambos productos disminuye exactamente en 1 unidad. |
| **NR-03** | Menú Ventas $\rightarrow$ Registrar Venta (Garantía Extendida) | Venta de 1 consola indicando que sí desea garantía extendida (+10%). | El subtotal refleja el precio de la consola, se adiciona el 10% en el renglón "Garantías extendidas", el total incluye dicho valor. Se registra póliza extendida en `WarrantyService`. |
| **NR-04** | Menú Ventas $\rightarrow$ Registrar Venta (Accesorios Mixtos) | Venta que incluye 1 consola y 2 controles/accesorios. | `SaleService` procesa la venta polimórficamente sin fallar. El stock de la consola se actualiza en `ProductPersistence` y el stock de los controles en `AccessoryPersistence`. |
| **NR-05** | Menú Ventas $\rightarrow$ Impresión de Recibo | Visualización en pantalla tras registrar la venta. | El recibo formateado por `ReceiptFormatter` mantiene exactamente el diseño, encabezados, labels y decimales que producía `Sale.generateReceipt()`. |
| **NR-06** | Menú Devoluciones $\rightarrow$ Procesar Devolución | Devolución de producto dentro de los 30 días posteriores a la venta. | Reembolso calculado correctamente, reversión de existencias en el inventario correspondiente y cancelación de garantías asociadas en caso de consolas. |
| **NR-07** | Menú Principal $\rightarrow$ Manejo de Errores | Intentar vender con stock insuficiente o ID duplicado. | Se lanzan `BusinessRuleException` o `InvalidDataException` capturadas por `ConsoleUI` con mensajes claros en español sin terminar abruptamente el programa. |

---

## 2. Registro Histórico de Refactorizaciones (Bitácora)

A continuación se registra cada intervención realizada por el Líder Técnico, detallando el ID de la violación resuelta, el patrón GRASP aplicado, las clases modificadas y el veredicto de no regresión:

| ID Violación | Patrón GRASP | Clases Modificadas | Descripción del Cambio | Estado |
|---|---|---|---|---|
| **V-L02** | High Cohesion | `Sale.java`, `ReceiptFormatter.java`, `ConsoleUI.java` | Extracción de la lógica de presentación `generateReceipt()` desde la entidad `Sale` hacia una Fabricación Pura (`ReceiptFormatter`). `Sale` queda como entidad pura de negocio. | En ejecución |
| **V-L04** | Polymorphism | `Product.java`, `Console.java`, `Accessory.java`, `SaleService.java` | Adición de métodos polimórficos `getExtendedWarrantyCost()`, `isWarrantyEligible()` y `updateStock(...)` en `Product` y subclases. Eliminación de todos los `if (instanceof Console)` e `if (instanceof Accessory)` en `SaleService`. | En ejecución |
| **V-L01** | Low Coupling | `SaleService.java`, `Main.java` | Desacoplamiento de `SaleService` respecto a subclases concretas (`Accessory`, `Console`) y adición de constructor sobrecargado simplificado para operaciones núcleo de venta. | En ejecución |

---

## 3. Verificación de Compilación y Paridad
* **Compilador:** `javac` (Java 17).
* **Paridad de Salida:** Verificada contra las trazas previas de terminal.
