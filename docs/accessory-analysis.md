
ACCESSORIES MODULE - ANALYSIS AND DESIGN


This document addresses the five key questions regarding the integration of the accessories module into the GameZone Unicesar system.

Should accessories be integrated into the existing hierarchy (inheriting from `Product`) or should an independent hierarchy be created?

Accessories should extend `Product` directly. An accessory shares the exact same commercial identity as any other item for sale: it has an ID, a title, a price, and available stock. By inheriting from `Product`, these attributes are reused, the `getDescription()` contract is leveraged, and the polymorphic behavior relied upon by `Sale` and `SaleService` is maintained.

If an independent hierarchy were created, the `Sale` class would have to manage two completely disconnected types of items. This would break the current design, where `sale.getProducts()` returns a list of `Product` objects. Keeping them within the same hierarchy allows sales to naturally accept accessories without modifying existing contracts, thereby reducing the risk of regressions and maintaining system consistency.

Which attributes are common to the three accessory types, and which are specific? How is this reflected in the class hierarchy?

Common attributes (defined in `Accessory`, inherited from `Product` plus the compatibility list):

`id`, `title`, `price`, and `availableQuantity` (inherited from `Product`).

`compatibleConsoles`: a list of consoles with which the accessory is compatible.

Specific attributes:

`Controller`: `connectionType` (wireless or wired).

`Cable`: `lengthMeters` (double) and `connectorType` (String).

`Memory`: `capacityGb` (int) and `memoryType` (String).

This distinction is implemented via an abstract `Accessory` class that encapsulates shared behavior and requires each concrete subclass to define its own implementation of `getDescription()` using `@Override`. This centralizes common logic in one place while allowing each subclass to retain its own identity.

Compatibility between an accessory and a console represents a relationship between two entities. How is this represented in the design and persistence layers? Is it an attribute of the accessory, the console, or both? Compatibility is a many-to-many (N:M) relationship between accessories and consoles. It is not merely a simple, one-sided attribute; the system must allow querying which consoles an accessory supports and, indirectly, which accessories are compatible with a specific console.

In the model: The relationship is represented as a `List<Console>` within the `Accessory` class, along with methods to add, remove, and query compatible consoles. It is placed in this class because the accessory is the element that declares its compatibility, and the console's perspective can be obtained by filtering the accessory inventory.

In persistence: The relationship is stored in `data/accessories.csv` within the `compatibleConsoleIds` field, which contains console identifiers separated by the `|` character. When loading data into memory, these IDs are resolved against the console inventory. This keeps the CSV file flat while allowing the relationship to be reconstructed at runtime.

Conclusion: Compatibility is a bidirectional relationship conceptually, but it is stored on the accessory side for simplicity and query performance.

What modifications are required in `SaleService` to include accessories without breaking current functionality regarding video games and consoles?

The `SaleService.registerSale` method receives a `Sale` object, which exposes its items via `sale.getProducts()` (returning a `List<Product>`). Since `Accessory` inherits from `Product`, the method already accepts accessories transparently without needing to change its signature.

The only required modification is in the inventory update step:

When iterating through each item in the sale, if it is an instance of `Accessory`, stock updating is delegated to `AccessoryService.updateStock(...)`.

Otherwise, stock updating continues to be delegated to `ProductService.updateStock(...)`.

Validation of available stock and total calculation remain unified thanks to the base `Product` contract. The changes are strictly additive and do not alter existing behavior for video games or consoles.

In which layer of the system architecture should each new class be placed, and why? The new classes are distributed according to the four-layer architecture established in the project:

Model layer (model): Accessory, Controller, Cable, and Memory. These are domain entities and must not include file access logic.

Persistence layer (persistence): AccessoryPersistence. It handles reading from and writing to data/accessories.csv, following the same convention as ProductPersistence and PersonPersistence.

Service layer (service): AccessoryService. It manages the business rules for registering, listing, and updating accessories.
