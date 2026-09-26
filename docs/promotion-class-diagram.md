# Promotion Module — Class Diagram

This diagram shows the promotion module integrated with the existing GameZone
Unicesar system. It covers the promotion hierarchy, the persistence and service
classes, and the integration with sales.

```mermaid
classDiagram
    direction LR

    class Promotion {
        <<abstract>>
        -String id
        -String name
        -LocalDate startDate
        -LocalDate endDate
        +isActive(LocalDate) boolean
        +calculateDiscount(Sale) double
    }

    class PercentageDiscount {
        -double percentage
        +calculateDiscount(Sale) double
    }

    class CategoryDiscount {
        -double percentage
        -String targetCategory
        +calculateDiscount(Sale) double
    }

    class BulkPurchaseDiscount {
        -int minimumQuantity
        -double percentage
        +calculateDiscount(Sale) double
    }

    class Sale {
        -String id
        -LocalDate date
        -Customer customer
        -Seller seller
        -List~Product~ products
        -double total
        -String appliedPromotionName
        -double discountAmount
        +getProducts() List~Product~
        +calculateTotal() double
        +generateReceipt() String
    }

    class PromotionRepository {
        +saveAll(List~Promotion~) void
        +loadAll() List~Promotion~
    }

    class PromotionService {
        -PromotionRepository repository
        +registerPercentageDiscount(...) void
        +registerCategoryDiscount(...) void
        +registerBulkPurchaseDiscount(...) void
        +listAllPromotions() List~Promotion~
        +listActivePromotions() List~Promotion~
        +findBestPromotionFor(Sale) Promotion
        +findById(String) Promotion
    }

    class SaleService {
        -ProductService productService
        -AccessoryService accessoryService
        -PromotionService promotionService
        +registerSale(Sale) void
    }

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int availableQuantity
    }

    Promotion <|-- PercentageDiscount
    Promotion <|-- CategoryDiscount
    Promotion <|-- BulkPurchaseDiscount

    Sale "1" o-- "1..*" Product : contains
    CategoryDiscount ..> Product : filters by category

    SaleService --> PromotionService : uses
    SaleService --> ProductService : uses
    SaleService --> AccessoryService : uses
    PromotionService --> PromotionRepository : uses
```

## Notes

- `Promotion` is abstract and declares `calculateDiscount(Sale)` as an abstract
  method. Each subclass provides its own calculation rule.
- `PercentageDiscount` applies a percentage to the whole sale total.
- `CategoryDiscount` applies a percentage only to the products that match the
  target category (`VIDEOGAME` or `CONSOLE`).
- `BulkPurchaseDiscount` applies a percentage to the whole sale only when the
  sale contains at least `minimumQuantity` products.
- `Sale` is extended additively with `appliedPromotionName` and
  `discountAmount`, so the receipt can show the applied discount.
- `PromotionService.findBestPromotionFor(Sale)` selects the active promotion
  that yields the highest monetary discount. Only one promotion is applied per
  sale, and promotions are not cumulative.
- `PromotionRepository` persists promotions in `data/promotions.csv` using a
  type discriminator (`PERCENTAGE`, `CATEGORY`, `BULK`).
