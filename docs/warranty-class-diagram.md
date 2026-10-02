# Warranty Class Diagram (Taller 4)

```mermaid
classDiagram
    class Warranty {
        <<abstract>>
        -String id
        -Product product
        -Sale sale
        -LocalDate startDate
        -LocalDate endDate
        +Warranty(String, Product, Sale, LocalDate)
        +int getDurationInMonths()*
        +String getWarrantyType()*
        +double getAdditionalCost()*
        +boolean isActive(LocalDate)
        +String generateWarrantyCertificate()
        +String getId()
        +Product getProduct()
        +Sale getSale()
        +LocalDate getStartDate()
        +LocalDate getEndDate()
    }

    class BasicWarranty {
        -int DURATION_IN_MONTHS = 6
        -String WARRANTY_TYPE = "Garantia Basica"
        +BasicWarranty(String, Product, Sale, LocalDate)
        +int getDurationInMonths()
        +String getWarrantyType()
        +double getAdditionalCost()
    }

    class ExtendedWarranty {
        -int DURATION_IN_MONTHS = 12
        -String WARRANTY_TYPE = "Garantia Extendida"
        -double COST_PERCENTAGE = 0.10
        +ExtendedWarranty(String, Product, Sale, LocalDate)
        +int getDurationInMonths()
        +String getWarrantyType()
        +double getAdditionalCost()
    }

    class WarrantyService {
        -WarrantyRepository repository
        +WarrantyService(WarrantyRepository)
        +BasicWarranty assignBasicWarranty(Product, Sale, LocalDate)
        +ExtendedWarranty assignExtendedWarranty(Product, Sale, LocalDate)
        +Warranty findWarrantyByProduct(String, String)
        +List~Warranty~ listAllWarranties()
        +List~Warranty~ listActiveWarranties()
        +List~Warranty~ listWarrantiesExpiringSoon(int)
    }

    class WarrantyRepository {
        -SalePersistence salePersistence
        -ProductService productService
        -AccessoryService accessoryService
        +WarrantyRepository(SalePersistence, ProductService, AccessoryService)
        +void saveAll(List~Warranty~)
        +List~Warranty~ loadAll()
        -String buildLine(Warranty)
        -Warranty parseLine(String)
        -Sale findSaleById(String)
        -Product findProductById(String)
        -void ensureDataFolderExists()
    }

    class SaleService {
        -SalePersistence persistence
        -ProductService productService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -WarrantyService warrantyService
        +SaleService(SalePersistence, ProductService, AccessoryService, PromotionService, WarrantyService)
        +void registerSale(Sale)
        +void registerSale(Sale, List~String~)
        +List~Sale~ listAllSales()
        +List~Sale~ getSalesByCustomer(String)
        +List~Sale~ getSalesBySeller(String)
        -void applyBestPromotion(Sale)
        -void assignWarranties(Sale, List~String~)
        -boolean wantsExtendedWarranty(Product, List~String~)
        -double roundToCents(double)
    }

    class ConsoleUI {
        -WarrantyService warrantyService
        +void showMainMenu()
        +void showSaleMenu()
        +void showWarrantyMenu()
        -boolean wantsExtendedWarranty(Product)
        -void showWarranties(List~Warranty~)
        -String describe(Warranty)
    }

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int availableQuantity
        +String getId()
        +String getPrice()
    }

    class Console {
        -String brand
        -String model
        -String generation
    }

    class Sale {
        -String id
        -LocalDate date
        -double total
    }

    class SalePersistence {
        +List~Sale~ loadAll()
    }

    class ProductService {
        +List~Product~ listAllProducts()
    }

    class AccessoryService {
        +Product findById(String)
    }

    Warranty <|-- BasicWarranty
    Warranty <|-- ExtendedWarranty

    Warranty o-- Product : product
    Warranty o-- Sale : sale

    ConsoleUI --> WarrantyService : queries
    ConsoleUI --> SaleService : registerSale(sale, extendedIds)

    SaleService --> WarrantyService : grants warranties
    WarrantyService --> WarrantyRepository : load/save

WarrantyRepository --> SalePersistence : resolves Sale
WarrantyRepository --> ProductService : resolves Product
WarrantyRepository --> AccessoryService : resolves Accessory

SaleService --> SalePersistence
SaleService --> ProductService
SaleService --> AccessoryService
SaleService --> PromotionService

    classDef model fill:#e8f4ea,stroke:#4a7
    classDef service fill:#e6eef8,stroke:#47a
    classDef persistence fill:#fdf0e0,stroke:#a74
    classDef ui fill:#f3e8f8,stroke:#74a

    class Warranty,BasicWarranty,ExtendedWarranty,Product,Console,Sale model
    class WarrantyService,SaleService,ProductService,AccessoryService service
    class WarrantyRepository,SalePersistence persistence
    class ConsoleUI ui
```

## Dependency direction

```
ConsoleUI  ->  SaleService  ->  WarrantyService  ->  WarrantyRepository
                  |                                     |
                  +------------> SalePersistence <-------+
```

`WarrantyRepository` resolves sales through `SalePersistence` rather than
`SaleService`. Injecting `SaleService` there would produce the cycle
`SaleService -> WarrantyService -> WarrantyRepository -> SaleService`, which
constructor injection cannot build. See `warranty-analysis.md`, question 2.

## Division of labour

The diagram shows the whole warranty module, but only these classes belong to
the technical leader's part of Taller 4:

| Class | Author |
| --- | --- |
| `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Dev 1 (model) |
| `WarrantyRepository` | Dev 2 (persistence) |
| `WarrantyService` | Dev 2 (service) |
| `SaleService`, `ConsoleUI`, `Main` | Technical leader |

`SaleService` and `ConsoleUI` are shown because they are the classes this part
modifies; the warranty model and persistence above them are contributed
separately.