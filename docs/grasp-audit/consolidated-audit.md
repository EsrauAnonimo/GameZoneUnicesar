# Auditoría Consolidada GRASP — GameZone Unicesar

**Rama auditada:** `develop` (commit `916d492` / rama de trabajo `feature/grasp-audit`)  
**Fecha:** Octubre 2026  
**Equipo de Desarrollo:**
* **Líder Técnico:** Esteban Raúl Vergara Jaime (`leader-audit.md`)
* **Desarrollador 1:** Luis Eduardo Gómez Rosales (`developer1-audit.md`)
* **Desarrollador 2:** Cristian Andrés Pérez González (`developer2-audit.md`)

---

## 1. Tabla de Cobertura de la Auditoría

A continuación se detalla la matriz de cobertura total de las clases, módulos y responsabilidades auditadas por cada miembro del equipo de desarrollo, abarcando el 100% del sistema GameZone Unicesar:

| Módulo / Subsistema | Responsable | Clases y Métodos Auditados | Estado de Auditoría |
|---|---|---|---|
| **Ventas y Persistencia Central** | Líder Técnico | `Sale.java` (métodos de cálculo, `generateReceipt`, `canBeReturned`), `SalePersistence.java` (`save`, `loadAll`), `SaleValidator.java` | Completada |
| **Orquestación de Venta e Integración** | Líder Técnico | `SaleService.java` (`registerSale`, `calculateSubtotal`, `applyPromotions`, `calculateExtendedWarrantyCost`, `assignWarranties`, `restoreStock`) | Completada |
| **Capa de Entrada y Presentación** | Líder Técnico | `ConsoleUI.java` (menús interactivos, captura de excepciones, ruteo de flujos), `Main.java` (raíz de composición e inyección de dependencias) | Completada |
| **Productos y Catálogo** | Desarrollador 1 | `Product.java`, `VideoGame.java`, `Console.java`, `ProductPersistence.java`, `ProductService.java` (`restoreStock`, `updateStock`, `listAllProducts`), `ProductValidator.java` | Completada |
| **Jerarquía de Accesorios (Req. 1)** | Desarrollador 1 | `Accessory.java`, `Controller.java`, `Cable.java`, `Memory.java` | Completada |
| **Estrategias de Promoción (Req. 2)** | Desarrollador 1 | `Promotion.java`, `PercentageDiscount.java`, `CategoryDiscount.java` (`calculateDiscount`), `BulkPurchaseDiscount.java` | Completada |
| **Entidades de Devolución y Garantía (Req. 3 y 4)** | Desarrollador 1 | `Warranty.java`, `BasicWarranty.java`, `ExtendedWarranty.java`, `Return.java` (`generateReturnReceipt`), `Sale.canBeReturned` | Completada |
| **Personas y Roles (Taller 1)** | Desarrollador 2 | `Person.java`, `Customer.java`, `Seller.java`, `PersonPersistence.java`, `PersonService.java` (`registerCustomer`, `preloadVendorsIfEmpty`), `PersonValidator.java` | Completada |
| **Servicio y Persistencia de Accesorios (Req. 1)** | Desarrollador 2 | `AccessoryPersistence.java` (o `AccessoryRepository`), `AccessoryService.java` | Completada |
| **Servicio y Repositorio de Promociones (Req. 2)** | Desarrollador 2 | `PromotionRepository.java`, `PromotionService.java` | Completada |
| **Servicio y Repositorio de Devoluciones (Req. 3)** | Desarrollador 2 | `ReturnRepository.java` (acoplamiento a servicios de dominio), `ReturnService.java` (gestión de balances, validaciones y restauración de stock) | Completada |
| **Servicio y Repositorio de Garantías (Req. 4)** | Desarrollador 2 | `WarrantyRepository.java`, `WarrantyService.java` (registro, consulta y cancelación de pólizas de garantía) | Completada |
| **Manejo Transversal de Excepciones (Req. 6)** | Equipo Completo (Transversal) | `GameZoneException.java`, `BusinessRuleException.java`, `InvalidDataException.java`, `PersistenceException.java`, `ResourceNotFoundException.java` (auditoría en capas de persistencia, servicios y presentación) | Completada |

---

## 2. Veredicto Consolidado de los 9 Patrones GRASP

### 2.1 Matriz Resumen de Veredictos del Equipo

| Patrón GRASP | Veredicto Global del Equipo | Áreas con Cumplimiento Correcto | Áreas con Violación Identificada |
|---|---|---|---|
| **1. Information Expert** | **Violación Parcial** | `Sale.calculateTotal`, `Accessory.compatibleConsoles`, `PersonService.registerCustomer` | `SaleService` calculando descuentos comerciales y garantías en lugar de `Sale` (V-05); `Sale.canBeReturned` usando `LocalDate.now()` internamente (V-07); `ProductService.restoreStock` mutando stock directamente vía setters. |
| **2. Creator** | **Correcto** | `Main.java` (raíz de composición central), `PersonService.preloadVendorsIfEmpty` (instancia `Seller`), delegación de `WarrantyService.grantWarranty`, `Accessory` creando lista interna de consolas compatibles. | Ninguna violación crítica detectada. |
| **3. Controller** | **Aplicable / Smell** | `PersonService` como controlador de dominio limpio sin consola; `ConsoleUI` canaliza entradas hacia servicios sin acceso directo a persistencia. | `ConsoleUI.java` actúa como God Controller (+1100 líneas) acumulando parsing por Scanner, ruteo y orquestación de 7 módulos (V-03). |
| **4. Low Coupling** | **Violación** | `PersonService` acoplado limpiamente solo a modelo, validador y persistencia; validadores desacoplados de la UI. | `SaleService` acoplado a 5 servicios y a la subclase `Accessory` (V-01); `CategoryDiscount` acoplado a cada subclase de `Product` (V-08); `ReturnRepository` acoplado e inyectando `SaleService`, `ProductService` y `AccessoryService` (V-12). |
| **5. High Cohesion** | **Violación** | Servicios enfocados en casos de uso de negocio; persistencias enfocadas en operaciones de I/O en disco. | `Sale.generateReceipt` (V-02), `Warranty.generateWarrantyCertificate` y `Return.generateReturnReceipt` (V-09) mezclan dominio con diseño de terminal de consola; `ConsoleUI` con excesivas responsabilidades (V-03). |
| **6. Polymorphism** | **Violación** | Jerarquías polimórficas de `Promotion`, `Warranty` y `Accessory`. | Uso de `instanceof` en `SaleService` para discriminar `Accessory` vs `Product` y `Console` (V-04); `instanceof` en `CategoryDiscount.calculateDiscount` (V-10); `instanceof` en `ReturnService` para `Accessory` y `Console` (V-12). |
| **7. Pure Fabrication** | **Correcto** | Clases de persistencia (`SalePersistence`, `ProductPersistence`, `PersonPersistence`, `AccessoryPersistence`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`) y validadores (`SaleValidator`, `ProductValidator`, `PersonValidator`). | Se evidencia necesidad de crear nuevas fabricaciones puras para formateo de texto (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`). |
| **8. Indirection** | **Correcto** | Servicios (`SaleService`, `ProductService`, `PersonService`, `ReturnService`, `WarrantyService`) median entre la UI (`ConsoleUI`) y el almacenamiento. | Cumplimiento consistente y homogéneo en todos los subsistemas. |
| **9. Protected Variations** | **Violación** | Persistencia oculta tras servicios; validaciones sintácticas aisladas tras validadores especializados. | `SaleService` expuesto a variaciones de tipos de producto y reglas de garantía fijas (V-06); `CategoryDiscount` expuesto a nuevas categorías mediante branches rígidos (V-11). |

---

### 2.2 Análisis Detallado por Patrón

#### 1. Information Expert (Experto en Información)
* **Consenso del Equipo:** El patrón se cumple cuando la clase que encapsula los atributos necesarios realiza el cómputo de negocio. No obstante, se consolidaron violaciones donde los servicios asumen responsabilidades aritméticas de dominio y mutan entidades anémicas mediante setters.
* **Hallazgos:**
  - `SaleService.java:122-126`, `175-177`: El servicio calcula los descuentos comerciales y el costo de garantía extendida (`0.10 * price`) e inyecta los resultados vía setters en `Sale`. `Sale` debe ser el experto que aplique sus propios descuentos y adicione garantías.
  - `Sale.java:200`: `canBeReturned()` evalúa el límite de 30 días invocando internamente `LocalDate.now()`. `Sale` no es experto en el tiempo actual; debe recibir la fecha de evaluación como parámetro para permitir pruebas unitarias deterministas.
  - `ProductService.java:112`: En `restoreStock`, el servicio extrae y sobreescribe directamente la cantidad (`setAvailableQuantity(getAvailableQuantity() + qty)`). `Product` debe poseer el método `increaseStock(qty)`.

#### 2. Creator (Creador)
* **Consenso del Equipo:** Cumplimiento adecuado en todo el proyecto. La creación de instancias respeta relaciones de agregación y composición.
* **Hallazgos:**
  - `Main.java:26-62` actúa como Composition Root centralizado, instanciando repositorios y servicios con inyección de dependencias vía constructor.
  - `PersonService.preloadVendorsIfEmpty` crea instancias de `Seller` porque gestiona directamente la colección interna de vendedores.
  - `SaleService` no crea directamente las garantías ni las ventas; delega la creación a `WarrantyService` y recibe la venta construida desde el flujo de entrada.

#### 3. Controller (Controlador)
* **Consenso del Equipo:** Aplicable en la capa de presentación y servicios, pero con síntomas severos de "Overburdened / God Controller" en la interfaz de consola.
* **Hallazgos:**
  - `ConsoleUI.java` absorbe el control de eventos de usuario para 7 módulos diferentes en más de 1100 líneas de código, manejando parsing numérico, validación de entrada por Scanner, renderizado de menús y coordinación con 6 servicios. Debe modularizarse en controladores especializados (`SaleMenuHandler`, `ProductMenuHandler`, etc.).
  - En la capa de servicios, `PersonService` cumple un rol de controlador de aplicación adecuado al coordinar casos de uso sin depender de APIs de consola (`System.out`, `Scanner`).

#### 4. Low Coupling (Bajo Acoplamiento)
* **Consenso del Equipo:** Violación crítica identificada en múltiples capas (servicios, persistencia y promociones).
* **Hallazgos:**
  - `SaleService.java:49-57, 144-148`: El servicio de ventas depende directamente de 5 servicios (`SalePersistence`, `ProductService`, `AccessoryService`, `PromotionService`, `WarrantyService`) y conoce explícitamente a la subclase `Accessory`.
  - `CategoryDiscount.java:106-110`: La promoción se acopla directamente a cada subclase concreta (`VideoGame`, `Console`, `Accessory`).
  - `ReturnRepository.java:7-9, 37-56`: La persistencia de devoluciones depende e inyecta tres servicios de dominio (`SaleService`, `ProductService`, `AccessoryService`) para deserializar registros, invirtiendo la dirección arquitectónica recomendada.

#### 5. High Cohesion (Alta Cohesión)
* **Consenso del Equipo:** Violación en entidades del modelo de dominio que asumen responsabilidades de presentación gráfica o textual.
* **Hallazgos:**
  - `Sale.java:158-195` (`generateReceipt`): La entidad de venta construye el recibo formateado para terminal de consola con encabezados, separadores y columnas.
  - `Warranty.java:103` (`generateWarrantyCertificate`) y `Return.java:164` (`generateReturnReceipt`): Mismo defecto en garantías y devoluciones.
  - Un cambio en la representación visual (idioma, columnas, salida HTML o GUI) fuerza la modificación innecesaria de entidades centrales del negocio.

#### 6. Polymorphism (Polimorfismo)
* **Consenso del Equipo:** Violación severa por el uso recurrente de condicionales `instanceof` para bifurcar lógica según el tipo dinámico de objeto.
* **Hallazgos:**
  - `SaleService.java:144-148`: Condicional `product instanceof Accessory` para decidir si llamar a `accessoryService.updateStock` o a `productService.updateStock`.
  - `SaleService.java:175-177, 196-198`: Condicional `product instanceof Console` para habilitar y calcular garantías extendidas.
  - `CategoryDiscount.java:106-110`: `if/else if` con `instanceof` sobre los tipos de producto en lugar de invocar un método polimórfico en `Product`.
  - `ReturnService.java:114, 324`: Chequeos `instanceof Console` para cancelación de garantías e `instanceof Accessory` para restauración de stock.

#### 7. Pure Fabrication (Fabricación Pura)
* **Consenso del Equipo:** Correcto y altamente valorado en la arquitectura existente.
* **Hallazgos:**
  - `SalePersistence.java`, `ProductPersistence.java`, `PersonPersistence.java`, `AccessoryPersistence.java`, `PromotionRepository.java`, `ReturnRepository.java`, `WarrantyRepository.java`: Clases fabricadas artificialmente para aislar el I/O en disco (archivos `.dat` y `.csv`), preservando la cohesión del modelo.
  - `SaleValidator.java`, `PersonValidator.java`, `ProductValidator.java`: Fabricaciones puras que aíslan las reglas de validación sintáctica de datos.
  - La solución a las violaciones de presentación en `Sale`, `Warranty` y `Return` consistirá en introducir nuevas Fabricaciones Puras (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`).

#### 8. Indirection (Indirección)
* **Consenso del Equipo:** Correcto. La arquitectura desacopla consistentemente los extremos mediante capas intermedias.
* **Hallazgos:**
  - Los servicios de dominio (`SaleService`, `ProductService`, `PersonService`, `ReturnService`, `WarrantyService`) actúan como mediadores entre la presentación (`ConsoleUI`) y el almacenamiento en disco, garantizando que un cambio en la tecnología de almacenamiento no impacte a la interfaz.

#### 9. Protected Variations (Variaciones Protegidas)
* **Consenso del Equipo:** Violación derivada de la falta de polimorfismo y puntos de variación inestables.
* **Hallazgos:**
  - En `SaleService.java`, si se agrega una nueva categoría de productos (ej. tarjetas digitales o coleccionables) o cambian las tarifas de garantía, `SaleService` debe ser modificado directamente.
  - En `CategoryDiscount.java`, el soporte para nuevas familias de productos rompe la clase al carecer de un contrato polimórfico estable que encapsule la variación de categoría.

---

## 3. Hallazgos de Frontera (Cross-Module Boundary Findings)

De acuerdo con las reglas de auditoría del proyecto, cuando un desarrollador identifica violaciones que involucran componentes implementados por otro miembro del equipo, dicho conflicto constituye un **Hallazgo de Frontera** que debe ser diagnosticado y resuelto en este documento de consolidación, definiendo la interacción entre los componentes y asignando con precisión la responsabilidad de cada desarrollador.

---

### 3.1 Frontera 1: Persistencia de Devoluciones y Catálogo de Productos (`ReturnRepository` -> `ProductService` / `Product`)
* **Módulos involucrados:** Devoluciones (Dev 2, Cristian) vs. Productos (Dev 1, Luis) y Ventas (Líder Técnico, Esteban).
* **Auditor que reportó el hallazgo:** Cristian Pérez (Dev 2) en `developer2-audit.md` (Sección 2.4 y V-01).

#### Interacción entre Componentes:
- **Luis (Dev 1)** implementó la entidad base `Product.java` y el servicio `ProductService.java` para consultar y administrar el catálogo de videojuegos y consolas.
- **Esteban (Líder)** implementó `SaleService.java` para el registro y consulta de ventas.
- **Cristian (Dev 2)** implementó `ReturnRepository.java` para serializar y deserializar registros de devoluciones en el archivo `data/returns.csv`.
- **Punto de fricción:** Al deserializar una línea del archivo CSV en el método `parseLine` (líneas 180-222), `ReturnRepository` necesita reconstruir los objetos de dominio asociados a la devolución: la venta original (`Sale`) y la lista de productos devueltos (`List<Product>`). Para lograrlo, Cristian inyectó directamente `ProductService`, `SaleService` y `AccessoryService` en el constructor de `ReturnRepository` (líneas 37-56), invocando `productService.listAllProducts()` y `saleService.listAllSales()` para buscar las instancias correspondientes por ID.
- **Diagnóstico GRASP:**
  1. **Violación de Bajo Acoplamiento (Low Coupling):** Una clase de la capa de persistencia inferior (`ReturnRepository`) no debe depender de servicios de lógica de negocio de la capa superior (`ProductService`, `SaleService`), invirtiendo el flujo arquitectónico de dependencias.
  2. **Dificultad de Pruebas:** Para instanciar y probar unitariamente `ReturnRepository`, es obligatorio inicializar y precargar de datos tres servicios de negocio independientes.

#### Reparto de la Corrección (Phase 2):
* **Desarrollador 1 (Luis Gómez):** 
  - En `ProductService` (o en la capa de persistencia/catálogo de productos), provee un método de búsqueda eficiente por ID (`findProductById(String id)`) o expone la colección de productos precargada para evitar que la persistencia deba iterar manualmente sobre listas completas de negocio.
* **Desarrollador 2 (Cristian Pérez):** 
  - Refactoriza `ReturnRepository` eliminando la inyección directa de `ProductService`, `SaleService` y `AccessoryService` de su constructor y atributos.
  - En su lugar, modifica la firma de `loadAll` o delega la hidratación/resolución de referencias a `ReturnService` (el servicio de devoluciones), pasando las listas de entidades ya cargadas o un proveedor de datos desacoplado, preservando a `ReturnRepository` como una Fabricación Pura centrada exclusivamente en la lectura/escritura de texto plano CSV.

---

### 3.2 Frontera 2: Restauración de Stock y Cancelación de Garantías en Devoluciones (`ReturnService` -> `Product`, `Accessory` y `Console`)
* **Módulos involucrados:** Devoluciones (Dev 2, Cristian) vs. Jerarquía de Productos y Garantías (Dev 1, Luis).
* **Auditor que reportó el hallazgo:** Identificado a partir del análisis de dependencias de Cristian (Dev 2) y la integración con el modelo de Luis (Dev 1).

#### Interacción entre Componentes:
- **Luis (Dev 1)** diseñó la jerarquía de herencia de productos donde `Console` y `Accessory` heredan de la clase base abstracta `Product`. Sin embargo, no dotó a `Product` de métodos polimórficos para consultar la elegibilidad de garantía ni un mecanismo polimórfico de alteración de stock.
- **Cristian (Dev 2)** implementó `ReturnService.java` para procesar devoluciones (`processReturn`). Para cumplir con las reglas de negocio de Taller 1:
  1. En las líneas 113-117, Cristian debió verificar si los productos devueltos eran consolas para reembolsar y cancelar garantías:
     ```java
     if (product instanceof Console) {
         warrantyRefund += warrantyService.cancelWarranties(product.getId(), sale.getId());
     }
     ```
  2. En las líneas 323-329 (`restoreStock`), debió verificar si el producto era un accesorio o un producto regular para saber a cuál servicio devolverle la unidad:
     ```java
     if (product instanceof Accessory) {
         accessoryService.restoreStock(product.getId(), 1);
         return;
     }
     productService.restoreStock(product.getId(), 1);
     ```
- **Diagnóstico GRASP:**
  1. **Violación de Polimorfismo:** `ReturnService` inspecciona con `instanceof` los tipos de la jerarquía creados por Dev 1 en vez de apoyarse en comportamiento polimórfico.
  2. **Violación de Variaciones Protegidas:** La incorporación de nuevos tipos de productos elegibles para garantía o con inventario propio obligaría a modificar `ReturnService`.

#### Reparto de la Corrección (Phase 2):
* **Desarrollador 1 (Luis Gómez - Clase Base y Jerarquía):**
  - Dev 1 corrige la clase base `Product` agregando el método polimórfico `isWarrantyEligible()`, sobrescribiéndolo en `Console` retornando `true` y en las demás subclases retornando `false`.
  - Agrega en `Product` métodos encapsulados para alteración de inventario (`increaseStock(int qty)`).
* **Desarrollador 2 (Cristian Pérez - Servicio de Devoluciones):**
  - Dev 2 corrige la llamada en `ReturnService`, sustituyendo la verificación `if (product instanceof Console)` por la invocación polimórfica `if (product.isWarrantyEligible())`.
  - Canaliza la restauración de inventario a través de la interfaz unificada de catálogo/inventario, eliminando el condicional `if (product instanceof Accessory)`.

---

### 3.3 Frontera 3: Integración de Venta e Inventario Multitipo (`SaleService` -> `ProductService` y `AccessoryService`)
* **Módulos involucrados:** Ventas (Líder Técnico, Esteban) vs. Productos (Dev 1, Luis) y Accesorios (Dev 2, Cristian).
* **Auditor que reportó el hallazgo:** Esteban Vergara (Líder) en `leader-audit.md` (V-L01, V-L04, V-L06).

#### Interacción entre Componentes:
- En `SaleService.java:143-149`, el Líder Técnico ejecuta la reducción de existencias mediante una bifurcación de tipos:
  ```java
  int newQuantity = product.getAvailableQuantity() - count;
  if (product instanceof Accessory) {
      accessoryService.updateStock(id, newQuantity);
  } else {
      productService.updateStock(id, newQuantity);
  }
  ```
- `SaleService` debe inyectar forzosamente tanto `ProductService` (creado por Dev 1) como `AccessoryService` (creado por Dev 2), rompiendo el bajo acoplamiento.

#### Reparto de la Corrección (Phase 2):
* **Líder Técnico (Esteban Vergara):**
  - Refactoriza `SaleService` para que interactúe contra una única abstracción o coordinador de inventario (`InventoryCoordinator` / método de despacho polimórfico), eliminando la inyección dual de `ProductService` y `AccessoryService` y los bloques `instanceof`.
* **Desarrollador 1 (Luis Gómez) y Desarrollador 2 (Cristian Pérez):**
  - Homogenizan las firmas de actualización y consulta de stock en `ProductService` y `AccessoryService` para soportar la interfaz común de inventario.

---

## 4. Lista Consolidada de Violaciones del Proyecto

A continuación se presenta la lista unificada y definitiva de las 12 violaciones identificadas en la auditoría GRASP, consolidando los informes del Líder Técnico, Desarrollador 1 y Desarrollador 2, con la asignación formal del **Responsable de Corrección**:

### 4.1 Tabla Maestra Consolidada (V-01 a V-12)

| ID | ID Origen | Patrón GRASP | Clase y Método Afectado | Líneas Exactas | Severidad | Resumen del Hallazgo | Responsable de Corrección |
|---|---|---|---|---|---|---|---|
| **V-01** | V-L01 | Low Coupling | `SaleService.java` | `SaleService.java:49-57`, `144-148` | Alta | `SaleService` está acoplado directamente a 5 servicios y a la subclase concreta `Accessory` para actualizar stock. | **Líder Técnico** (con apoyo de Dev 1 y Dev 2 en unificación de inventario) |
| **V-02** | V-L02 | High Cohesion | `Sale.java` | `Sale.java:158-195` (`generateReceipt`) | Media | La entidad de dominio `Sale` construye el recibo formateado para terminal de consola con encabezados y columnas. | **Líder Técnico** (extracción a Pure Fabrication `SaleReceiptFormatter`) |
| **V-03** | V-L03 | High Cohesion / Controller Smell | `ConsoleUI.java` | `ConsoleUI.java:35-46`, `48-1107` | Alta | Clase monolítica de +1100 líneas que absorbe parsing, Scanner, menús y ruteo de 7 módulos del sistema. | **Líder Técnico** (modularización en submanejadores especializados por flujo) |
| **V-04** | V-L04 | Polymorphism | `SaleService.java` | `SaleService.java:144-148`, `175-177`, `196-198` | Alta | Uso de `instanceof` para bifurcar lógica según `Accessory` vs `Product` y `Console` para garantías. | **Desarrollador 1** (corrige clase base `Product`) y **Líder Técnico** (corrige llamadas en servicio) |
| **V-05** | V-L05 | Information Expert | `SaleService.java` & `Sale.java` | `SaleService.java:122-126`, `175-177`, `291-294` | Media | `SaleService` calcula descuentos comerciales y costo de garantías y muta `Sale` vía setters en vez de delegarlo en la entidad experta. | **Líder Técnico** (encapsula métodos de cálculo y mutación controlada en `Sale`) |
| **V-06** | V-L06 | Protected Variations | `SaleService.java` | `SaleService.java:144-148`, `175-177` | Media | Lógica de despacho de inventario y regla de cálculo de garantía (+10%) expuestas a variaciones en la jerarquía de productos y tarifas. | **Líder Técnico** (desacopla tarifas y delega en interfaz de inventario) |
| **V-07** | V-D1-01 | Information Expert | `Sale.java` | `Sale.java:200` (`canBeReturned`) | Media | Invoca `LocalDate.now()` internamente asumiendo conocimiento del tiempo actual en lugar de recibir la fecha de evaluación como parámetro. | **Desarrollador 1** (ajusta firma y lógica a `canBeReturned(LocalDate currentDate)`) |
| **V-08** | V-D1-02 | Low Coupling | `CategoryDiscount.java` | `CategoryDiscount.java:106, 108, 110` | Alta | Acoplamiento directo a cada subclase concreta de `Product` (`VideoGame`, `Console`, `Accessory`). | **Desarrollador 1** (elimina dependencias concretas usando polimorfismo) |
| **V-09** | V-D1-03 | High Cohesion | `Warranty.java` & `Return.java` | `Warranty.java:103`, `Return.java:164` | Media | Entidades de dominio construyen texto formateado para consola (`generateWarrantyCertificate` y `generateReturnReceipt`). | **Desarrollador 1** (extracción a Pure Fabrications `WarrantyCertificateFormatter` y `ReturnReceiptFormatter`) |
| **V-10** | V-D1-04 | Polymorphism | `CategoryDiscount.java` | `CategoryDiscount.java:106-110` | Alta | `if/else if` con `instanceof` para calcular descuentos por categoría en vez de apoyarse en despacho polimórfico. | **Desarrollador 1** (implementa método polimórfico `getCategory()` en `Product`) |
| **V-11** | V-D1-05 | Protected Variations | `CategoryDiscount.java` | `CategoryDiscount.java:106-110` | Alta | Categorías cableadas mediante condicionales de tipo; la adición de una nueva categoría rompe la clase. | **Desarrollador 1** (encapsula variación de categorías tras contrato polimórfico) |
| **V-12** | V-D2-01 | Low Coupling / Polymorphism | `ReturnRepository.java` & `ReturnService.java` | `ReturnRepository.java:7-9, 37-39, 51`; `ReturnService.java:114, 324` | Alta | `ReturnRepository` inyecta 3 servicios de dominio (`SaleService`, `ProductService`, `AccessoryService`); `ReturnService` evalúa `instanceof Console` e `instanceof Accessory`. | **Desarrollador 1** (provee métodos de consulta/polimorfismo en `Product`) y **Desarrollador 2** (desacopla `ReturnRepository` y llamadas en `ReturnService`) |

---

### 4.2 Desglose de Violaciones por Informe de Origen

#### A. Informe del Líder Técnico (Esteban Vergara — `leader-audit.md`)
* **V-L01 (V-01):** Low Coupling en `SaleService.java:49-57, 144-148`. Dependencia directa de 5 servicios e inspección explícita de `Accessory`.
* **V-L02 (V-02):** High Cohesion en `Sale.java:158-195`. Generación de recibo de texto en consola dentro de la entidad de dominio.
* **V-L03 (V-03):** High Cohesion / Controller Smell en `ConsoleUI.java:35-46, 48-1107`. Controlador monolítico multipropósito.
* **V-L04 (V-04):** Polymorphism en `SaleService.java:144-148, 175-177, 196-198`. Uso de `instanceof` para stock (`Accessory`) y garantías (`Console`).
* **V-L05 (V-05):** Information Expert en `SaleService.java:122-126, 175-177` y `Sale.java`. Mutación externa de totales y descuentos.
* **V-L06 (V-06):** Protected Variations en `SaleService.java:144-148, 175-177`. Fragilidad ante nuevas categorías y cambios de tarifas.

#### B. Informe del Desarrollador 1 (Luis Gómez — `developer1-audit.md`)
* **V-D1-01 (V-07):** Information Expert en `Sale.java:200` (`canBeReturned`). Uso de `LocalDate.now()` impide pruebas deterministas.
* **V-D1-02 (V-08):** Low Coupling en `CategoryDiscount.java:106, 108, 110`. Acoplado a cada subclase concreta de `Product`.
* **V-D1-03 (V-09):** High Cohesion en `Warranty.java:103` y `Return.java:164`. Formateo de certificados y recibos en entidades de negocio.
* **V-D1-04 (V-10):** Polymorphism en `CategoryDiscount.java:106-110`. Bifurcación manual de tipos en lugar de método polimórfico.
* **V-D1-05 (V-11):** Protected Variations en `CategoryDiscount.java:106-110`. Reglas rígidas no protegidas ante nuevas familias de producto.

#### C. Informe del Desarrollador 2 (Cristian Pérez — `developer2-audit.md`)
* **V-D2-01 (V-12):** Low Coupling en `ReturnRepository.java:7-9, 37-39, 51`. Inyección y dependencia directa de `SaleService`, `ProductService` y `AccessoryService` en la capa de persistencia CSV, y ramificaciones `instanceof` en `ReturnService.java:114, 324`.

---

## 5. Estrategia y Priorización de Refactorización (Fase 2)

Para garantizar un proceso de refactorización ordenado, sin conflictos de fusión (*merge conflicts*) y preservando la estabilidad funcional, el equipo adoptará el siguiente orden secuencial de intervención:

```
[Paso 1: Modelo de Dominio] ---> [Paso 2: Fabricaciones Puras] ---> [Paso 3: Servicios y Persistencia] ---> [Paso 4: Presentación UI]
  (Product, Sale, Warranty)         (Formatters de Recibos)            (Inventario, Sale/ReturnService)         (Submanejadores ConsoleUI)
```

1. **Paso 1 — Modelo de Dominio (Model Layer):**
   - Incorporar métodos polimórficos en `Product`: `getCategory()` e `isWarrantyEligible()`. Sobrescribir en `VideoGame`, `Console` y `Accessory`. *(Responsable: Dev 1)*.
   - Encapsular la lógica de cálculo y asignación de descuentos y garantías dentro de `Sale` (`applyDiscount`, `addExtendedWarranty`), eliminando setters públicos para totales y descuentos. *(Responsable: Líder Técnico)*.
   - Parametrizar fecha en `Sale.canBeReturned(LocalDate currentDate)`. *(Responsable: Dev 1)*.
   - Agregar método `increaseStock(int qty)` en `Product` para encapsular la mutación. *(Responsable: Dev 1)*.

2. **Paso 2 — Fabricaciones Puras de Presentación (Pure Fabrications / Formatters):**
   - Extraer métodos de formateo de consola (`generateReceipt`, `generateWarrantyCertificate`, `generateReturnReceipt`) hacia formateadores dedicados (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`). *(Responsables: Líder Técnico y Dev 1)*.

3. **Paso 3 — Desacoplamiento de Servicios y Persistencia (Service & Persistence Layer):**
   - Unificar o coordinar la actualización de inventario para que `SaleService` no bifurque entre `ProductService` y `AccessoryService`. *(Responsable: Líder Técnico con apoyo de Dev 1 y Dev 2)*.
   - Desacoplar `ReturnRepository` eliminando la inyección de servicios de negocio en la capa de persistencia CSV, trasladando la hidratación a `ReturnService`. *(Responsable: Dev 2)*.
   - Eliminar los chequeos `instanceof` en `ReturnService` usando los métodos polimórficos de `Product`. *(Responsable: Dev 2)*.
   - Eliminar los `instanceof` en `CategoryDiscount` llamando polimórficamente a `product.getCategory()`. *(Responsable: Dev 1)*.

4. **Paso 4 — Descomposición del Controlador de Presentación (Presentation / ConsoleUI):**
   - Dividir `ConsoleUI` en submanejadores especializados por flujo de menú (`SaleMenuHandler`, `ProductMenuHandler`, `PersonMenuHandler`, `ReturnMenuHandler`) para restablecer la alta cohesión. *(Responsable: Líder Técnico)*.
