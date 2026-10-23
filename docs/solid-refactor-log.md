# Bitácora de Refactorización SOLID y Estrategia de No Regresión

**Proyecto:** GameZone Unicesar  
**Fase:** Fase 2 — Corrección SOLID (Requerimiento 8)  
**Rama:** `feature/solid-refactor`  
**Equipo:** 
- Esteban Raúl Vergara Jaime (Líder Técnico — Módulos de Ventas, UI y Coordinación)
- Luis Eduardo Gómez Rosales (Developer 1 — Módulo de Productos)
- Cristian Andrés Pérez González (Developer 2 — Módulos de Personas, Devoluciones, Garantías y Persistencia, asumido por el Líder)

---

## 1. Estrategia de Verificación de No Regresión

Para asegurar que las correcciones orientadas a los principios SOLID no alteren el comportamiento funcional del sistema ni rompan los flujos interactivos de consola, se definió un protocolo de no regresión basado en simplicidad, compilación limpia y pruebas operativas en `ConsoleUI`.

### 1.1 Reglas Fundamentales de Implementación (KISS)
1. **Simplicidad ante todo:** Soluciones directas y legibles sin sobreingeniería.
2. **Restricción estricta de APIs:** Cero interfaces nuevas innecesarias, cero `Optional`, cero `Stream` API.
3. **Mecanismos permitidos:** Herencia estándar con `@Override`, clases abstractas existentes, métodos delegados y constructores sobrecargados para compatibilidad hacia atrás.
4. **Validación de Compilación:** Compilación limpia bajo Java 17 (`javac`) sin dependencias externas ni warnings bloqueantes.

---

### 1.2 Casos de Prueba de Verificación en `ConsoleUI`

| ID Caso | Flujo en Menú | Acción y Datos de Entrada | Criterio de Aceptación (No Regresión) |
|---|---|---|---|
| **NR-01** | Catálogo / Productos | Consultar catálogo de videojuegos, consolas y accesorios. | Listado completo con precios, títulos y stock correcto sin excepciones. |
| **NR-02** | Ventas $\rightarrow$ Registrar Venta Estándar | Venta con 1 videojuego y 1 consola sin garantía extendida. | Subtotal = suma de precios, promociones aplicadas correctamente, stock descontado en 1 unidad para cada producto. |
| **NR-03** | Ventas $\rightarrow$ Registrar Venta con Garantía | Venta de consola solicitando garantía extendida (+10%). | Garantía extendida calculada e incluida en total; póliza registrada en `WarrantyService`. |
| **NR-04** | Ventas $\rightarrow$ Impresión de Recibo | Visualización en consola tras completar la venta. | `ReceiptFormatter` formatea el recibo idéntico al diseño original sin errores de formato. |
| **NR-05** | Devoluciones $\rightarrow$ Procesar Devolución | Devolución de producto dentro del plazo legal (<30 días). | Devolución aceptada; monto de reembolso calculado; stock restaurado polimórficamente; recibo de devolución generado por `ReceiptFormatter`. |
| **NR-06** | Devoluciones $\rightarrow$ Validación Plazo | Intento de devolución con fecha fuera del plazo permitido. | Rechazo controlado mediante excepción de negocio sin caída del sistema. |
| **NR-07** | Consola $\rightarrow$ Inyección de Entrada | Ejecución con `new ConsoleUI(..., new Scanner(input))`. | Permite automatización de entradas sin depender exclusivamente de `System.in`. |

---

## 2. Registro Histórico de Refactorizaciones (Bitácora)

A continuación se detallan todas las correcciones implementadas para resolver las violaciones identificadas en la auditoría consolidada (`consolidated-audit.md`), cubriendo los cambios de Developer 1, el Líder Técnico y Developer 2:

| ID Violación | Principio SOLID | Responsable Asignado | Clases Modificadas | Descripción de la Refactorización | Hash Commit | Estado |
|---|---|---|---|---|---|---|
| **V-01** (Dev1) / **V-09** | **DIP / SRP** | Developer 1 | `ProductService.java`, `Main.java` | Inyección de dependencias mediante constructor para desacoplar `ProductPersistence`. | `8a71de0` | **Completado y Verificado** |
| **V-02** (Dev1) / **V-10** | **OCP** | Developer 1 | `ProductService.java` | Creación del método genérico `registerProduct(Product)` para admitir nuevos subtipos sin modificar el servicio. | `8a71de0` | **Completado y Verificado** |
| **V-04** | **SRP** | Dev 2 (asumido Líder) | `Return.java`, `ReceiptFormatter.java`, `ConsoleUI.java` | Extracción de la lógica de presentación `generateReturnReceipt()` desde la entidad de dominio `Return` hacia `ReceiptFormatter.formatReturnReceipt()`. `Return` mantiene método delegado para compatibilidad. | `b4e0dfd` | **Completado y Verificado** |
| **V-01** (Líder) | **SRP** | Líder Técnico | `SaleService.java` | Eliminación de salida directa a consola (`System.out.println`) en lógica de fallback de promociones; preservación de pureza de la capa de servicio. | `5492e12` | **Completado y Verificado** |
| **V-11** | **SRP** | Líder Técnico | `Sale.java`, `ReturnService.java` | Desacoplamiento de la política de ventana de retorno de 30 días de la entidad `Sale`. `Sale` expone `canBeReturnedOn(LocalDate)` y `ReturnService` gestiona `isEligibleForReturn(Sale)`. | `61bb369` | **Completado y Verificado** |
| **V-03** | **DIP** | Líder Técnico | `ConsoleUI.java` | Inversión de dependencias en capa de presentación mediante constructor sobrecargado `ConsoleUI(..., Scanner)`. Permite inyectar flujos de entrada en pruebas automáticas. | `3df0269` | **Completado y Verificado** |
| **V-06** | **OCP** | Dev 2 (asumido Líder) | `ReturnService.java` | Eliminación de `instanceof Console` e `instanceof Accessory`. Reemplazo por métodos polimórficos de dominio `product.isWarrantyEligible()` y `product.isAccessory()`. | `383ff3b` | **Completado y Verificado** |
| **V-07** | **LSP** | Líder Técnico | `ProductService.java` | Corrección en `restoreStock` para invocar `product.increaseStock(quantity)` respetando encapsulamiento y subtipos de dominio. | `a591af4` | **Completado y Verificado** |
| **V-02** | **DIP** | Dev 2 (asumido Líder) | `ReturnRepository.java` | Eliminación del acoplamiento obligatorio de persistencia hacia servicios de negocio (`SaleService`, `ProductService`, `AccessoryService`). Adición de constructor desacoplado y validaciones defensivas. | `aade42e` | **Completado y Verificado** |

---

## 3. Verificación de Compilación y Calidad

* **Entorno de Compilación:** OpenJDK / Java 17.
* **Comando de Verificación:** `$files = (Get-ChildItem -Recurse -Filter *.java src/main/java).FullName; javac -encoding UTF-8 -d target/classes $files`
* **Resultado:** Código de salida `0` (cero errores, compilación exitosa).
* **Paridad Funcional:** Todas las clases de dominio, servicios y controladores mantienen paridad con la especificación y contratos requeridos por la suite del sistema.
