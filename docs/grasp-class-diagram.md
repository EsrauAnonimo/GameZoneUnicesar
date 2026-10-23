# Diagrama de Clases — Estado 'Antes' de la Refactorización GRASP

**Proyecto:** GameZone Unicesar  
**Fase:** Fase 2 — Corrección GRASP (Requerimiento 7)  
**Rama:** `feature/grasp-refactor`  
**Objetivo:** Modelar la estructura arquitectónica inicial previa a las correcciones, destacando los puntos de acoplamiento indebido, violaciones de polimorfismo, falta de cohesión y responsabilidades mal ubicadas identificadas durante la auditoría.

---

## 1. Diagrama de Clases Mermaid ('Antes')

```mermaid
classDiagram
    direction TB

    class ConsoleUI {
        -PersonService personService
        -ProductService productService
        -SaleService saleService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -ReturnService returnService
        -WarrantyService warrantyService
        +start() void
        +showMainMenu() void
        +handleSalesMenu() void
        +handleProductMenu() void
        +handlePersonMenu() void
        +handleAccessoryMenu() void
        +handlePromotionMenu() void
        +handleReturnMenu() void
        +handleWarrantyMenu() void
    }

    class SaleService {
        -SalePersistence persistence
        -ProductService productService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -WarrantyService warrantyService
        +SaleService(SalePersistence, ProductService, AccessoryService, PromotionService, WarrantyService)
        +registerSale(Sale, List~String~) void
        -calculateWarrantyCost(Sale, List~String~) double
        -assignWarranties(Sale, List~String~) void
        -applyBestPromotion(Sale) void
    }

    class Sale {
        -String id
        -LocalDate date
        -Person customer
        -Person seller
        -List~Product~ products
        -double total
        -double discountAmount
        -String appliedPromotionName
        -double extendedWarrantyCost
        +calculateTotal() double
        +getSubtotal() double
        +generateReceipt() String
        +canBeReturned() boolean
        +setTotal(double) void
        +setDiscountAmount(double) void
        +setExtendedWarrantyCost(double) void
    }

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int availableQuantity
        +getDescription()* String
        +getId() String
        +getPrice() double
        +getAvailableQuantity() int
        +setAvailableQuantity(int) void
    }

    class Console {
        -String brand
        -String model
        -String generation
        +getDescription() String
    }

    class VideoGame {
        -String platform
        -String genre
        -String ageRating
        +getDescription() String
    }

    class Accessory {
        <<abstract>>
        -List~Console~ compatibleConsoles
        +addCompatibleConsole(Console) void
    }

    class CategoryDiscount {
        -String targetCategory
        -double percentage
        +calculateDiscount(Sale) double
    }

    class ReturnRepository {
        -SaleService saleService
        -ProductService productService
        -AccessoryService accessoryService
        +ReturnRepository(SaleService, ProductService, AccessoryService)
        +saveAll(List~Return~) void
        +loadAll() List~Return~
        -parseLine(String) Return
    }

    class ReturnService {
        -ReturnRepository returnRepository
        -SaleService saleService
        -ProductService productService
        -AccessoryService accessoryService
        -WarrantyService warrantyService
        +processReturn(String, List~String~, String) Return
        -restoreStock(Product) void
    }

    class Return {
        -String id
        -LocalDate returnDate
        -Sale originalSale
        -List~Product~ returnedProducts
        -double refundAmount
        -String reason
        +generateReturnReceipt() String
        +calculateRefundAmount() double
    }

    class Warranty {
        <<abstract>>
        -String id
        -Product product
        -Sale sale
        -LocalDate startDate
        -LocalDate endDate
        +generateWarrantyCertificate() String
        +calculateCoverageEndDate()* LocalDate
    }

    class SalePersistence {
        +save(List~Sale~) void
        +loadAll() List~Sale~
    }

    %% Relaciones de Herencia
    Product <|-- Console
    Product <|-- VideoGame
    Product <|-- Accessory

    %% Relaciones de Presentación y Controlador
    ConsoleUI ..> SaleService : invoca
    ConsoleUI ..> Sale : muestra recibo via generateReceipt

    %% Relaciones de Servicios y Dominio
    SaleService --> SalePersistence : usa
    SaleService --> ProductService : actualiza stock
    SaleService --> AccessoryService : actualiza stock
    SaleService --> PromotionService : busca mejor promo
    SaleService --> WarrantyService : asigna garantias
    SaleService ..> Sale : orquesta y muta
    SaleService ..> Product : itera
    SaleService ..> Accessory : instanceof check (V-L04)
    SaleService ..> Console : instanceof check (V-L04)

    %% Violaciones de Promociones
    CategoryDiscount ..> VideoGame : instanceof check (V-D1-04)
    CategoryDiscount ..> Console : instanceof check (V-D1-04)
    CategoryDiscount ..> Accessory : instanceof check (V-D1-04)

    %% Violaciones de Persistencia y Devoluciones
    ReturnRepository --> SaleService : inyecta para resolver ventas (V-D2-01)
    ReturnRepository --> ProductService : inyecta para resolver productos (V-D2-01)
    ReturnRepository --> AccessoryService : inyecta para resolver accesorios (V-D2-01)
    ReturnService --> ReturnRepository : usa
    ReturnService ..> Console : instanceof check en cancelacion (Frontera)
    ReturnService ..> Accessory : instanceof check en restoreStock (Frontera)

    %% Modelo de Devoluciones y Garantías
    ReturnService ..> Return : crea y guarda
    Return ..> Sale : referencia
    Return ..> Product : contiene lista
    Warranty ..> Product : cubre
    Warranty ..> Sale : referencia
```

---

## 2. Convenciones y Mapeo de Violaciones en el Diagrama

| Elemento en el Diagrama | Violación GRASP Identificada | Justificación Arquitectónica |
|---|---|---|
| `Sale.generateReceipt()` | **V-02 (V-L02) — High Cohesion** | La entidad de dominio `Sale` contiene lógica de formato textual y dibujo de tablas de consola, mezclando negocio con presentación. |
| `SaleService` $\rightarrow$ 5 Servicios + `Accessory` | **V-01 (V-L01) — Low Coupling** | Constructor sobrecargado con 5 dependencias e inspección directa de la subclase concreta `Accessory`. |
| `SaleService` (`instanceof Console`, `instanceof Accessory`) | **V-04 (V-L04) — Polymorphism** | Bifurcación manual de tipos para despachar stock y cobrar garantías extendidas en vez de invocar métodos polimórficos en `Product`. |
| `SaleService` mutando totales de `Sale` con setters | **V-05 (V-L05) — Information Expert** | El servicio asume la responsabilidad de calcular subtotales, garantías y descuentos, tratando a `Sale` como un contenedor anémico. |
| `ConsoleUI` centralizando 7 módulos | **V-03 (V-L03) — High Cohesion / God Controller** | Monolito de más de 1100 líneas con parsing de Scanner, validación y coordinación directa de todos los flujos. |
| `CategoryDiscount` (`instanceof` en cascada) | **V-08 / V-10 (V-D1-02, V-D1-04) — Low Coupling & Polymorphism** | Acoplamiento rígido hacia cada subclase de `Product`, violando el principio Open/Closed. |
| `ReturnRepository` inyectando 3 servicios de negocio | **V-12 (V-D2-01) — Low Coupling** | Inversión de capas donde una clase de persistencia CSV depende de servicios de alto nivel para hidratar referencias. |
| `Warranty` y `Return` con `generate*Certificate/Receipt()` | **V-09 (V-D1-03) — High Cohesion** | Entidades de dominio asumiendo responsabilidades de presentación de consola. |
| `Sale.canBeReturned()` usando `LocalDate.now()` | **V-07 (V-D1-01) — Information Expert** | La entidad `Sale` no tiene información de la fecha actual de evaluación; debe recibirla por parámetro. |
