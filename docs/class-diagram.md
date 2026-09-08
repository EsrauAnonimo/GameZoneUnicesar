# Class Diagram - GameZone Unicesar

```mermaid
classDiagram
    %% ===== MODEL LAYER =====
    class Person {
        <<abstract>>
        -id : String
        -name : String
        -identification : String
        -phone : String
        +getId() String
        +getName() String
        +getIdentification() String
        +getPhone() String
        +getRoleDescription()* String
        +getFullSummary() String
    }
    class Customer {
        -email : String
        +getEmail() String
        +getRoleDescription() String
    }
    class Seller {
        -employeeCode : String
        -workShift : String
        +getEmployeeCode() String
        +getRoleDescription() String
    }
    class Product {
        <<abstract>>
        -id : String
        -title : String
        -price : double
        -availableQuantity : int
        +getDescription()* String
        +getPrice() double
        +getAvailableQuantity() int
        +setAvailableQuantity(quantity : int) void
    }
    class VideoGame {
        -platform : String
        -genre : String
        -ageRating : String
        +getDescription() String
    }
    class Console {
        -brand : String
        -model : String
        -generation : String
        +getDescription() String
    }
    class Sale {
        -id : String
        -date : LocalDate
        -customer : Customer
        -seller : Seller
        -products : List~Product~
        -total : double
        +calculateTotal() double
        +getProducts() List~Product~
    }
    Person <|-- Customer
    Person <|-- Seller
    Product <|-- VideoGame
    Product <|-- Console
    Sale "1" --> "1" Customer
    Sale "1" --> "1" Seller
    Sale "1" --> "1..*" Product
    %% ===== PERSISTENCE LAYER =====
    class PersonPersistence {
        +save(people : List~Person~) void
        +loadAll() List~Person~
    }
    class ProductPersistence {
        +save(products : List~Product~) void
        +loadAll() List~Product~
    }
    class SalePersistence {
        +save(sales : List~Sale~) void
        +loadAll() List~Sale~
    }
    PersonPersistence ..> Person
    ProductPersistence ..> Product
    SalePersistence ..> Sale
    %% ===== SERVICE LAYER =====
    class PersonService {
        -persistence : PersonPersistence
        +registerCustomer(customer : Customer) void
        +listCustomers() List~Customer~
        +listSellers() List~Seller~
    }
    class ProductService {
        -persistence : ProductPersistence
        +registerVideoGame(videoGame : VideoGame) void
        +registerConsole(console : Console) void
        +listAllProducts() List~Product~
        +updateStock(productId : String, quantity : int) void
    }
    class SaleService {
        -persistence : SalePersistence
        -productService : ProductService
        +registerSale(sale : Sale) void
        +listAllSales() List~Sale~
        +getSalesByCustomer(customerId : String) List~Sale~
        +getSalesBySeller(sellerId : String) List~Sale~
    }
    PersonService ..> PersonPersistence
    ProductService ..> ProductPersistence
    SaleService ..> SalePersistence
    SaleService ..> ProductService
    %% ===== UI LAYER =====
    class ConsoleUI {
        -personService : PersonService
        -productService : ProductService
        -saleService : SaleService
        +showMainMenu() void
        +showProductMenu() void
        +showPersonMenu() void
        +showSaleMenu() void
    }
    ConsoleUI ..> PersonService
    ConsoleUI ..> ProductService
    ConsoleUI ..> SaleService
```

## Notes
In this diagram, dependency relationships between layers are indicated with dashed arrows (..>), following the strict architectural direction: UI → Service → Persistence → Model. Solid arrows (-->) denote associations exclusively within the model layer. Abstract elements—including the Person and Product base classes and the getDescription() method—are marked accordingly, maintaining consistency with the hierarchy diagram
