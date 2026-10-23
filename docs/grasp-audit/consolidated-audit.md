# Auditoría Consolidada GRASP — GameZone Unicesar

**Rama auditada:** `develop` (commit `916d492` / rama de trabajo `feature/grasp-audit`)  
**Fecha:** Octubre 2026  
**Equipo de Desarrollo:**
* **Líder Técnico:** Esteban Raúl Vergara Jaime (`leader-audit.md`)
* **Desarrollador 1:** Luis Eduardo Gómez Rosales (`developer1-audit.md`)
* **Desarrollador 2:** Cristian Andrés Pérez González (`developer2-audit.md`)

---

## 1. Tabla de Cobertura de la Auditoría

A continuación se detalla la matriz de cobertura de las clases, módulos y responsabilidades auditadas por cada miembro del equipo de desarrollo, abarcando la totalidad del sistema GameZone Unicesar.

| Módulo / Subsistema | Responsable | Clases y Métodos Auditados | Estado de Auditoría |
|---|---|---|---|
| **Ventas y Persistencia Central** | Líder Técnico | `Sale.java` (métodos de cálculo, `generateReceipt`, `canBeReturned`), `SalePersistence.java` (`save`, `loadAll`), `SaleValidator.java` | Completada |
| **Orquestación de Venta e Integración** | Líder Técnico | `SaleService.java` (`registerSale`, `calculateSubtotal`, `applyPromotions`, `calculateExtendedWarrantyCost`, `assignWarranties`, `restoreStock`) | Completada |
| **Capa de Entrada y Presentación** | Líder Técnico | `ConsoleUI.java` (menús interactivos, captura de excepciones, ruteo de flujos), `Main.java` (inyección de dependencias) | Completada |
| **Productos y Catálogo** | Desarrollador 1 | `Product.java`, `VideoGame.java`, `Console.java`, `ProductPersistence.java`, `ProductService.java` (`restoreStock`, `updateStock`, `listAllProducts`) | Completada |
| **Jerarquía de Accesorios (Req. 1)** | Desarrollador 1 | `Accessory.java`, `Controller.java`, `Cable.java`, `Memory.java` | Completada |
| **Estrategias de Promoción (Req. 2)** | Desarrollador 1 | `Promotion.java`, `PercentageDiscount.java`, `CategoryDiscount.java` (`calculateDiscount`), `BulkPurchaseDiscount.java` | Completada |
| **Entidades de Devolución y Garantía (Req. 3 y 4)** | Desarrollador 1 | `Warranty.java`, `BasicWarranty.java`, `ExtendedWarranty.java`, `Return.java` (`generateReturnReceipt`), `Sale.canBeReturned` | Completada |
| **Personas y Roles (Taller 1)** | Desarrollador 2 | `Person.java`, `Customer.java`, `Seller.java`, `PersonPersistence.java`, `PersonService.java`, `PersonValidator.java` | Completada |
| **Servicio de Accesorios (Req. 1)** | Desarrollador 2 | `AccessoryRepository.java`, `AccessoryService.java` | Completada |
| **Servicio de Promociones (Req. 2)** | Desarrollador 2 | `PromotionRepository.java`, `PromotionService.java` | Completada |
| **Servicio y Repositorio de Devoluciones (Req. 3)** | Desarrollador 2 | `ReturnRepository.java` (acoplamiento a servicios), `ReturnService.java` (gestión de balances y stock) | En consolidación |
| **Servicio y Repositorio de Garantías (Req. 4)** | Desarrollador 2 | `WarrantyRepository.java`, `WarrantyService.java` | En consolidación |

---

## 2. Veredicto Consolidado de los 9 Patrones GRASP

### 2.1 Matriz Resumen de Veredictos

| Patrón GRASP | Veredicto Global del Equipo | Áreas con Cumplimiento Correcto | Áreas con Violación Identificada |
|---|---|---|---|
| **1. Information Expert** | **Violación Parcial** | `Sale.calculateTotal`, `Accessory.compatibleConsoles`, `PersonService.registerCustomer` | `SaleService` calculando descuentos/garantías en lugar de `Sale` (V-L05); `Sale.canBeReturned` usando `LocalDate.now()` (V-D1-01); `ProductService.restoreStock` mutando stock directamente. |
| **2. Creator** | **Correcto** | `Main.java` (raíz de composición), `PersonService.preloadVendorsIfEmpty`, delegación de `WarrantyService.grantWarranty`, `Accessory` creando lista interna. | Ninguna violación crítica detectada. |
| **3. Controller** | **Aplicable / Smell** | `PersonService` como controlador de dominio limpio sin consola; `ConsoleUI` canaliza entradas a servicios. | `ConsoleUI.java` actúa como God Controller (+1100 líneas) acumulando parsing, ruteo y orquestación de 7 módulos (V-L03). |
| **4. Low Coupling** | **Violación** | `PersonService` acoplado limpiamente solo a modelo, validador y persistencia. | `SaleService` acoplado a 5 servicios y subclase `Accessory` (V-L01); `CategoryDiscount` acoplado a subclases de `Product` (V-D1-02); `ReturnRepository` acoplado a servicios de negocio (V-D2-01). |
| **5. High Cohesion** | **Violación** | Servicios enfocados en casos de uso de negocio; persistencias enfocadas en I/O. | `Sale.generateReceipt` (V-L02), `Warranty.generateWarrantyCertificate` y `Return.generateReturnReceipt` (V-D1-03) mezclan dominio con presentación; `ConsoleUI` multipropósito (V-L03). |
| **6. Polymorphism** | **Violación** | Jerarquías polimórficas de `Promotion`, `Warranty` y `Accessory`. | Uso de `instanceof` en `SaleService` para discriminar `Accessory` vs `Product` y `Console` (V-L04); `instanceof` en `CategoryDiscount.calculateDiscount` (V-D1-04). |
| **7. Pure Fabrication** | **Correcto** | Clases de persistencia (`SalePersistence`, `ProductPersistence`, `PersonPersistence`), validadores (`SaleValidator`, `PersonValidator`). | Se evidencia necesidad de crear nuevas fabricaciones puras para formateo de texto (`ReceiptFormatter`). |
| **8. Indirection** | **Correcto** | Servicios (`SaleService`, `ProductService`, `PersonService`) actúan como intermediarios entre la UI (`ConsoleUI`) y el almacenamiento. | Cumplimiento consistente en todos los flujos de negocio. |
| **9. Protected Variations** | **Violación** | Persistencia oculta tras servicios; validaciones aisladas tras validadores. | `SaleService` expuesto a variaciones de tipos de producto y reglas de garantía fijas (V-L06); `CategoryDiscount` expuesto a nuevas categorías mediante branches rígidos (V-D1-05). |

---

### 2.2 Análisis Detallado por Patrón

#### 1. Information Expert (Experto en Información)
* **Consenso:** El patrón se respeta cuando la clase que contiene los atributos requeridos realiza la operación (ej. `Accessory` administrando compatibilidad de consolas). Sin embargo, existe una violación recurrente donde los servicios asumen responsabilidades de cálculo mutando entidades anémicas.
* **Hallazgos:**
  - `SaleService.java:122-126`, `175-177`: El servicio calcula los descuentos comerciales y el costo de garantía extendida (`0.10 * price`) e inyecta los resultados vía setters en `Sale`. `Sale` debe ser el experto que aplique sus propios descuentos y adicione garantías.
  - `Sale.java:200`: `canBeReturned()` evalúa el límite de 30 días invocando internamente `LocalDate.now()`. `Sale` no es experto en el "tiempo actual"; debe recibir la fecha de evaluación como parámetro para permitir pruebas unitarias deterministas.
  - `ProductService.java:112`: En `restoreStock`, el servicio extrae y sobreescribe directamente la cantidad (`setAvailableQuantity(getAvailableQuantity() + qty)`). `Product` debe poseer el método `increaseStock(qty)`.

#### 2. Creator (Creador)
* **Consenso:** Cumplimiento adecuado. La instanciación de objetos de dominio e infraestructura sigue reglas lógicas de agregación y composición.
* **Hallazgos:**
  - `Main.java:26-62` actúa como Composition Root centralizado, instanciando repositorios y servicios con inyección vía constructor.
  - `PersonService.preloadVendorsIfEmpty` crea instancias de `Seller` porque gestiona directamente la colección de vendedores.
  - `SaleService` no crea directamente las garantías ni las ventas; delega la creación a `WarrantyService` y recibe la venta construida desde el flujo de entrada.

#### 3. Controller (Controlador)
* **Consenso:** Aplicable en la capa de presentación y servicios, pero con síntomas severos de "Overburdened / God Controller" en la interfaz de consola.
* **Hallazgos:**
  - `ConsoleUI.java` absorbe el control de eventos de usuario para 7 módulos diferentes en más de 1100 líneas de código, manejando parsing numérico, validación de entrada por Scanner, renderizado de menús y coordinación con 6 servicios. Debe modularizarse en controladores especializados (`SaleMenuHandler`, `ProductMenuHandler`, etc.).
  - En la capa de servicios, `PersonService` cumple un rol de controlador de aplicación adecuado al coordinar casos de uso sin depender de APIs de consola (`System.out`, `Scanner`).

#### 4. Low Coupling (Bajo Acoplamiento)
* **Consenso:** Violación crítica identificada en múltiples capas (servicios, persistencia y promociones).
* **Hallazgos:**
  - `SaleService.java:49-57, 144-148`: El servicio de ventas depende directamente de 5 servicios (`SalePersistence`, `ProductService`, `AccessoryService`, `PromotionService`, `WarrantyService`) y conoce explícitamente a la subclase `Accessory`.
  - `CategoryDiscount.java:106-110`: La promoción se acopla directamente a cada subclase concreta (`VideoGame`, `Console`, `Accessory`).
  - `ReturnRepository.java:7-9, 37-56`: La persistencia de devoluciones depende e inyecta tres servicios de dominio (`SaleService`, `ProductService`, `AccessoryService`) para deserializar registros, invirtiendo la dirección arquitectónica recomendada.

#### 5. High Cohesion (Alta Cohesión)
* **Consenso:** Violación en entidades del modelo de dominio que asumen responsabilidades de presentación gráfica o textual.
* **Hallazgos:**
  - `Sale.java:158-195` (`generateReceipt`): La entidad de venta construye el recibo formateado para terminal de consola con encabezados, separadores y columnas.
  - `Warranty.java:103` (`generateWarrantyCertificate`) y `Return.java:164` (`generateReturnReceipt`): Mismo defecto en garantías y devoluciones.
  - Un cambio en la representación visual (idioma, columnas, salida HTML o GUI) fuerza la modificación innecesaria de entidades centrales del negocio.

#### 6. Polymorphism (Polimorfismo)
* **Consenso:** Violación severa por el uso recurrente de condicionales `instanceof` para bifurcar lógica según el tipo dinámico de objeto.
* **Hallazgos:**
  - `SaleService.java:144-148`: Condicional `product instanceof Accessory` para decidir si llamar a `accessoryService.updateStock` o a `productService.updateStock`.
  - `SaleService.java:175-177, 196-198`: Condicional `product instanceof Console` para habilitar y calcular garantías extendidas.
  - `CategoryDiscount.java:106-110`: `if/else if` con `instanceof` sobre los tipos de producto en lugar de invocar un método polimórfico en `Product`.

#### 7. Pure Fabrication (Fabricación Pura)
* **Consenso:** Correcto y altamente valorado en la arquitectura existente.
* **Hallazgos:**
  - `SalePersistence.java`, `ProductPersistence.java`, `PersonPersistence.java`: Clases fabricadas artificialmente para aislar el I/O en disco (archivos `.dat` y `.csv`), preservando la cohesión del modelo.
  - `SaleValidator.java`, `PersonValidator.java`: Fabricaciones puras que aíslan las reglas de validación sintáctica de datos.
  - La solución a las violaciones de presentación en `Sale`, `Warranty` y `Return` consistirá en introducir nuevas Fabricaciones Puras (`SaleReceiptFormatter`, `ReturnReceiptFormatter`).

#### 8. Indirection (Indirección)
* **Consenso:** Correcto. La arquitectura desacopla consistentemente los extremos mediante capas intermedias.
* **Hallazgos:**
  - Los servicios de dominio (`SaleService`, `ProductService`, `PersonService`, `ReturnService`) actúan como mediadores entre la presentación (`ConsoleUI`) y el almacenamiento en disco, garantizando que un cambio en la tecnología de almacenamiento no impacte a la interfaz.

#### 9. Protected Variations (Variaciones Protegidas)
* **Consenso:** Violación derivada de la falta de polimorfismo y puntos de variación inestables.
* **Hallazgos:**
  - En `SaleService.java`, si se agrega una nueva categoría de productos (ej. tarjetas digitales o coleccionables) o cambian las tarifas de garantía, `SaleService` debe ser modificado directamente.
  - En `CategoryDiscount.java`, el soporte para nuevas familias de productos rompe la clase al carecer de un contrato polimórfico estable que encapsule la variación de categoría.

---

## 3. Hallazgo de Frontera (Cross-Module Boundary Finding)

### 3.1 Descripción de la Frontera Arquitectónica
Durante la auditoría se identificó un punto de fricción crítico en la frontera de integración entre el módulo de **Ventas** (responsabilidad del Líder Técnico, `SaleService`), el módulo de **Productos** (responsabilidad del Dev 1, `ProductService`), y el módulo de **Accesorios** (responsabilidad del Dev 2, `AccessoryService`).

### 3.2 Evidencia Concreta de Código
En `SaleService.java`, método `registerSale` (líneas 143-148):
```java
int newQuantity = product.getAvailableQuantity() - count;
if (product instanceof Accessory) {
    accessoryService.updateStock(id, newQuantity);
} else {
    productService.updateStock(id, newQuantity);
}
```

Asimismo, en el constructor de `SaleService.java` (líneas 49-57):
```java
public SaleService(SalePersistence persistence, ProductService productService,
                   AccessoryService accessoryService, PromotionService promotionService,
                   WarrantyService warrantyService) {
    this.persistence = persistence;
    this.productService = productService;
    this.accessoryService = accessoryService;
    this.promotionService = promotionService;
    this.warrantyService = warrantyService;
}
```

Y en `ReturnRepository.java` (líneas 37-56) y `ReturnService.java` (líneas 26-30):
```java
private final SaleService saleService;
private final ProductService productService;
private final AccessoryService accessoryService;
```

### 3.3 Diagnóstico y Violaciones GRASP Asociadas
1. **Ruptura de Polimorfismo y Acoplamiento Cruzado:** `Accessory` hereda de `Product`, por lo que conceptualmente una venta maneja una lista homogénea `List<Product>`. Sin embargo, debido a que el stock de videojuegos/consolas se almacena en `ProductPersistence` (vía `ProductService`) y el stock de accesorios se almacena en `AccessoryPersistence` (vía `AccessoryService`), `SaleService` se ve obligado a inspeccionar el tipo en tiempo de ejecución (`instanceof Accessory`).
2. **Violación de Bajo Acoplamiento (Low Coupling):** `SaleService` no puede limitarse a interactuar con un subsistema general de catálogo/inventario; debe conocer e inyectar ambos servicios concretos.
3. **Violación de Variaciones Protegidas (Protected Variations):** La introducción futura de un nuevo tipo de producto con persistencia propia (por ejemplo, productos digitales o suscripciones) requerirá modificar `SaleService`, violando el principio Open/Closed.
4. **Acoplamiento Inverso en Devoluciones:** En la frontera de devoluciones, `ReturnRepository` (clase de persistencia) requiere acceso directo a `SaleService`, `ProductService` y `AccessoryService` para resolver entidades referenciadas, generando un acoplamiento indebido de la capa inferior hacia capas superiores.

### 3.4 Solución Propuesta para la Refactorización (Fase 2)
* **Unificación del Manejo de Inventario:** Diseñar un coordinador o fachada de inventario (`InventoryCoordinator` o método unificado en el catálogo) que reciba el identificador y la cantidad a actualizar, resolviendo internamente el despacho según corresponda, sin que `SaleService` necesite inspeccionar subclases con `instanceof`.
* **Polimorfismo en Producto:** Permitir que cada producto conozca la forma de registrar o descontar su stock, o que el servicio de inventario trate de forma polimórfica a cualquier ítem comercializable.
* **Desacoplar Persistencia de Devoluciones:** Hacer que `ReturnRepository` reciba únicamente datos planos o repositorios de lectura, evitando inyectar servicios completos de negocio dentro de una clase de persistencia CSV.

---

## 4. Lista Consolidada de Violaciones

A continuación se listan las violaciones detectadas por el equipo, agrupadas por responsable e identificador único:

### 4.1 Violaciones Identificadas por el Líder Técnico

| ID | Patrón GRASP | Clase y Método Afectado | Líneas Exactas | Severidad | Resumen |
|---|---|---|---|---|---|
| **V-L01** | Low Coupling | `SaleService.java` | `SaleService.java:49-57`, `144-148` | Alta | Acoplado directamente a 5 servicios y a la subclase concreta `Accessory`. |
| **V-L02** | High Cohesion | `Sale.java` | `Sale.java:158-195` | Media | `generateReceipt()` genera texto y diseño visual de terminal dentro del modelo de dominio. |
| **V-L03** | High Cohesion / Controller Smell | `ConsoleUI.java` | `ConsoleUI.java:35-46`, `48-1107` | Alta | Clase monolítica que centraliza parsing, menús y ruteo de 7 módulos del sistema. |
| **V-L04** | Polymorphism | `SaleService.java` | `SaleService.java:144-148`, `175-177`, `196-198` | Alta | Uso de `instanceof` para discriminar `Accessory` vs `Product` y `Console` para garantías. |
| **V-L05** | Information Expert | `SaleService.java` & `Sale.java` | `SaleService.java:122-126`, `175-177`, `291-294` | Media | El servicio calcula descuentos y costos de garantías y los asigna con setters en `Sale`. |
| **V-L06** | Protected Variations | `SaleService.java` | `SaleService.java:144-148`, `175-177` | Media | Código no protegido ante nuevas subclases de productos ni cambios en tarifas de garantía. |

---

### 4.2 Violaciones Identificadas por el Desarrollador 1 (Luis Gómez)

| ID | Patrón GRASP | Clase y Método Afectado | Líneas Exactas | Severidad | Resumen |
|---|---|---|---|---|---|
| **V-D1-01** | Information Expert | `Sale.java` | `Sale.java:200` (`canBeReturned`) | Media | Invoca `LocalDate.now()` internamente asumiendo conocimiento del tiempo actual. |
| **V-D1-02** | Low Coupling | `CategoryDiscount.java` | `CategoryDiscount.java:106, 108, 110` | Alta | Acoplado a cada subclase concreta de `Product` (`VideoGame`, `Console`, `Accessory`). |
| **V-D1-03** | High Cohesion | `Warranty.java` & `Return.java` | `Warranty.java:103`, `Return.java:164` | Media | Entidades formatean texto de certificados y recibos de devolución para consola. |
| **V-D1-04** | Polymorphism | `CategoryDiscount.java` | `CategoryDiscount.java:106-110` | Alta | Uso de `if/else if` con `instanceof` para calcular descuentos por categoría. |
| **V-D1-05** | Protected Variations | `CategoryDiscount.java` | `CategoryDiscount.java:106-110` | Alta | Categorías cableadas mediante condicionales de tipo; no tolera nuevas categorías. |

---

### 4.3 Violaciones Identificadas por el Desarrollador 2 (Cristian Pérez)

| ID | Patrón GRASP | Clase y Método Afectado | Líneas Exactas | Severidad | Resumen |
|---|---|---|---|---|---|
| **V-D2-01** | Low Coupling | `ReturnRepository.java` | `ReturnRepository.java:7-9, 37-39, 51` | Alta | El repositorio depende e inyecta tres servicios de dominio (`SaleService`, `ProductService`, `AccessoryService`). |

> **Nota:** El Desarrollador 2 se encuentra en proceso de carga final de sus hallazgos detallados sobre los módulos restantes. Se reservan los siguientes espacios para la consolidación total:

```markdown
<!-- ESPACIO RESERVADO PARA VIOLACIONES ADICIONALES DE DEV 2 (Cristian Pérez) -->
<!-- Se incorporarán las tarjetas y filas de violaciones de ReturnService, WarrantyService y validadores una vez finalizada su entrega en feature/grasp-audit -->
```

---

## 5. Estrategia y Priorización de Refactorización (Fase 2)

Para garantizar un proceso de refactorización ordenado, sin conflictos de fusión (*merge conflicts*) y preservando la estabilidad funcional, el equipo adoptará el siguiente orden secuencial de intervención:

1. **Paso 1 — Modelo de Dominio (Model Layer):**
   - Incorporar método polimórfico en `Product` (`getCategory()` o `isWarrantyEligible()`) para eliminar los `instanceof` en `CategoryDiscount` y `SaleService`.
   - Mudar la lógica de aplicación de descuentos y cálculo de totales dentro de `Sale` (`applyDiscount`, `addExtendedWarranty`).
   - Parametrizar fecha en `Sale.canBeReturned(LocalDate currentDate)`.
   - Agregar método `increaseStock(int qty)` en `Product` para encapsular la mutación.
2. **Paso 2 — Fabricaciones Puras de Presentación (Pure Fabrications / Formatters):**
   - Extraer métodos de formateo de consola (`generateReceipt`, `generateWarrantyCertificate`, `generateReturnReceipt`) hacia formateadores dedicados (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`).
3. **Paso 3 — Desacoplamiento de Servicios y Persistencia (Service & Persistence Layer):**
   - Unificar o coordinar la actualización de inventario para que `SaleService` no bifurque entre `ProductService` y `AccessoryService`.
   - Desacoplar `ReturnRepository` eliminando la inyección de servicios de negocio en la capa de persistencia.
4. **Paso 4 — Descomposición del Controlador de Presentación (Presentation / ConsoleUI):**
   - Dividir `ConsoleUI` en submanejadores especializados por flujo de menú para restablecer la alta cohesión.
