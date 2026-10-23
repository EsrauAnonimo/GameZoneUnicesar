# Diagrama de Clases — Estado 'Antes' de la Refactorización SOLID

**Proyecto:** GameZone Unicesar  
**Fase:** Fase 2 — Corrección SOLID (Requerimiento 8)  
**Rama:** `feature/solid-refactor`  
**Objetivo:** Modelar la estructura arquitectónica del sistema antes de aplicar las correcciones SOLID, documentando visualmente las violaciones a SRP, OCP, LSP, ISP y DIP identificadas durante la auditoría.

---

## 1. Diagrama de Clases Mermaid ('Antes')

```mermaid
classDiagram
    direction TB

    %% PRESENTATION LAYER
    class ConsoleUI {
        -PersonService personService
        -ProductService productService
        -SaleService saleService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -ReturnService returnService
        -WarrantyService warrantyService
        -Scanner scanner
        +ConsoleUI(PersonService, ProductService, SaleService, AccessoryService, PromotionService, ReturnService, WarrantyService)
        +showMainMenu() void
        +showProductMenu() void
        +showSaleMenu() void
        +showReturnMenu() void
        %% V-03: DIP - Hardcodes new Scanner(System.in) and tightly couples to 7 concrete services
    }

    class ReceiptFormatter {
        +formatReceipt(Sale) String
        %% Note: Formats Sale receipt, but Return receipt remained in domain (V-04)
    }

    %% SERVICE LAYER
    class SaleService {
        -SalePersistence persistence
        -ProductService productService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -WarrantyService warrantyService
        +registerSale(Sale, List~String~) void
        -calculateWarrantyCost(Sale, List~String~) double
        -assignWarranties(Sale, List~String~) void
        -applyBestPromotion(Sale) void
        %% V-01: SRP - Handles stock validation, warranties, discount fallback, currency rounding, and System.out logs
    }

    class ReturnService {
        -ReturnRepository returnRepository
        -SaleService saleService
        -ProductService productService
        -AccessoryService accessoryService
        -WarrantyService warrantyService
        +registerReturn(String, List~String~, String) Return
        +generateMonthlyBalance(int, int) double
        +calculateMonthlySales(int, int) double
        -restoreStock(Product) void
        %% V-06: OCP - instanceof Console & instanceof Accessory
        %% V-08: SRP - Calculates general store sales and monthly balance
    }

    class ProductService {
        -ProductPersistence persistence
        +ProductService()
        +registerVideoGame(VideoGame) void
        +registerConsole(Console) void
        +restoreStock(String, int) void
        %% V-07: LSP - Directly sets quantity, bypassing product.increaseStock
        %% V-09: DIP - Hardcodes new ProductPersistence() in constructor
        %% V-10: OCP - Separate methods per concrete product subtype
    }

    %% PERSISTENCE LAYER
    class ReturnRepository {
        -SaleService saleService
        -ProductService productService
        -AccessoryService accessoryService
        +ReturnRepository(SaleService, ProductService, AccessoryService)
        +saveAll(List~Return~) void
        +loadAll() List~Return~
        -parseLine(String) Return
        %% V-02: DIP - Persistence layer directly injects 3 high-level domain services (Layer Inversion)
    }

    class SalePersistence {
        +save(List~Sale~) void
        +loadAll() List~Sale~
    }

    class ProductPersistence {
        +save(List~Product~) void
        +loadAll() List~Product~
    }

    %% MODEL LAYER
    class Sale {
        -String id
        -LocalDate date
        -Customer customer
        -Seller seller
        -List~Product~ products
        -double total
        -double discountAmount
        -double extendedWarrantyCost
        +canBeReturned() boolean
        %% V-11: SRP - Embeds 30-day return policy and LocalDate.now() in transaction record
    }

    class Return {
        -String id
        -LocalDate date
        -Sale originalSale
        -List~Product~ returnedProducts
        -double refundAmount
        +calculateRefundAmount() double
        +generateReturnReceipt() String
        %% V-04: SRP - Builds terminal formatted receipt inside domain entity
    }

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int availableQuantity
        +increaseStock(int) void
        +isWarrantyEligible() boolean
        +isAccessory() boolean
        +updateStock(ProductService, AccessoryService, int) void
        %% V-05: OCP - Signature hardcodes concrete inventory services
    }

    class VideoGame {
        -String platform
        -String genre
    }

    class Console {
        -String brand
        -String generation
        +isWarrantyEligible() boolean
    }

    class Accessory {
        <<abstract>>
        +isAccessory() boolean
    }

    %% RELATIONSHIPS & INJECTIONS
    ConsoleUI ..> SaleService : invokes
    ConsoleUI ..> ReturnService : invokes
    ConsoleUI ..> ProductService : invokes
    ConsoleUI ..> ReceiptFormatter : uses

    SaleService --> SalePersistence : persists
    SaleService --> ProductService : updates stock
    SaleService --> ReturnService : none
    SaleService ..> Sale : manages

    ReturnService --> ReturnRepository : persists
    ReturnService --> SaleService : looks up sale
    ReturnService --> ProductService : restores stock

    ReturnRepository ..> SaleService : INVERTED DEPENDENCY (V-02)
    ReturnRepository ..> ProductService : INVERTED DEPENDENCY (V-02)

    Sale "1" *-- "*" Product : contains
    Return "1" --> "1" Sale : references
    Return "1" *-- "*" Product : returns

    Product <|-- VideoGame
    Product <|-- Console
    Product <|-- Accessory
```

---

## 2. Catálogo de Violaciones Representadas en el Diagrama

| ID Violación | Principio SOLID | Elemento en el Diagrama | Descripción del Defecto de Diseño |
|---|---|---|---|
| **V-01** | Single Responsibility (SRP) | `SaleService` | Orquesta registro, cálculo de garantías, fallback de promociones, redondeo y salida directa a consola (`System.out.println`). |
| **V-02** | Dependency Inversion (DIP) | `ReturnRepository ..> SaleService` | Inversión de capas arquitectónicas: un repositorio de persistencia inyecta y llama servicios de lógica de negocio. |
| **V-03** | Dependency Inversion (DIP) | `ConsoleUI` | Acoplamiento rígido a 7 servicios concretos e instanciación directa no inyectable de `new Scanner(System.in)`. |
| **V-04** | Single Responsibility (SRP) | `Return.generateReturnReceipt()` | Construcción de texto de presentación con marcos decorativos dentro de la entidad de dominio `Return`. |
| **V-05** | Open/Closed (OCP) | `Product.updateStock(...)` | Firma que recibe servicios concretos (`ProductService`, `AccessoryService`), requiriendo modificación si surgen nuevos tipos de almacenamiento. |
| **V-06** | Open/Closed (OCP) | `ReturnService` | Evaluación con `instanceof Console` para cancelar garantías y `instanceof Accessory` para restaurar existencias. |
| **V-07** | Liskov Substitution (LSP) | `ProductService.restoreStock` | Mutación directa con `setAvailableQuantity` en lugar de llamar a `increaseStock`, ignorando invariantes de subtipos. |
| **V-08** | Single Responsibility (SRP) | `ReturnService` | Cálculo del total de ventas del mes (`calculateMonthlySales`), una responsabilidad contable ajena al módulo de devoluciones. |
| **V-09** | Dependency Inversion (DIP) | `ProductService` | Instanciación con `new ProductPersistence()` en el constructor, impidiendo mockeo o inyección de dependencias. |
| **V-10** | Open/Closed (OCP) | `ProductService` | Métodos separados `registerVideoGame` y `registerConsole` en lugar de un contrato extensible `registerProduct(Product)`. |
| **V-11** | Single Responsibility (SRP) | `Sale.canBeReturned()` | Regla de ventana de 30 días y reloj del sistema acoplados directamente dentro del registro inmutable de la venta. |
