# Layers Diagram — GameZone Unicesar (Integrated System)

```mermaid
flowchart TD
    subgraph UI["UI Layer (com.gamezone.ui)"]
        ConsoleUI
    end

    subgraph Service["Service Layer (com.gamezone.service)"]
        PersonService
        ProductService
        AccessoryService
        SaleService
        PromotionService
        WarrantyService
        ReturnService
    end

    subgraph Persistence["Persistence Layer (com.gamezone.persistence)"]
        PersonPersistence["PersonPersistence (CSV)"]
        ProductPersistence["ProductPersistence (.dat)"]
        AccessoryPersistence["AccessoryPersistence (CSV)"]
        SalePersistence["SalePersistence (.dat)"]
        PromotionRepository["PromotionRepository (CSV)"]
        WarrantyRepository["WarrantyRepository (CSV)"]
        ReturnRepository["ReturnRepository (CSV)"]
    end

    subgraph Model["Model Layer (com.gamezone.model)"]
        PersonModel["Person, Customer, Seller"]
        ProductModel["Product, VideoGame, Console"]
        AccessoryModel["Accessory, Controller, Cable, Memory"]
        SaleModel["Sale"]
        PromotionModel["Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount"]
        WarrantyModel["Warranty, BasicWarranty, ExtendedWarranty"]
        ReturnModel["Return"]
    end

    UI --> Service
    Service --> Persistence
    Service --> Model
    Persistence --> Model
```

## Architectural Notes
- **Strict Layering:** Dependencies flow strictly downward: `UI → Service → Persistence → Model`.
- **Domain Purity:** The `Model` layer contains pure business entities with zero knowledge of file access, databases, or user interfaces.
- **Service Orchestration:** `SaleService` orchestrates items from both `ProductService` and `AccessoryService`, evaluates discounts via `PromotionService`, and delegates console coverage to `WarrantyService`.
- **Return Orchestration:** `ReturnService` validates 30-day return eligibility via `Sale.canBeReturned()`, restores stock through `ProductService` and `AccessoryService`, revokes warranties via `WarrantyService`, and calculates proportional refunds.
- **Persistence Decoupling:** `WarrantyRepository` resolves sales directly through `SalePersistence`, avoiding circular dependencies with `SaleService`.
