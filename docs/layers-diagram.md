# Layers Diagram - GameZone Unicesar

```mermaid
flowchart TD
    subgraph UI["UI Layer"]
        ConsoleUI
    end

    subgraph Service["Service Layer"]
        PersonService
        ProductService
        SaleService
    end

    subgraph Persistence["Persistence Layer"]
        PersonPersistence
        ProductPersistence
        SalePersistence
    end

    subgraph Model["Model Layer"]
        Person
        Customer
        Seller
        Product
        VideoGame
        Console
        Sale
    end

    UI --> Service
    Service --> Persistence
    Service --> Model
    Persistence --> Model
```

## Notes
- Dependencies follow the mandatory direction: UI → Service → Persistence → Model.
- The Model layer has no outgoing dependencies to any other layer, keeping the domain independent.
- UI never accesses Persistence directly; all persistence operations must go through Service.
