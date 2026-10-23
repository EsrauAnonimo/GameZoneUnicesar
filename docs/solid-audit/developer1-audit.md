# SOLID Audit - Developer 1

## Scope
- **Products:** Product, VideoGame, Console, ProductPersistence, ProductService.
- **Accessories:** Accessory, Controller, Cable, Memory.
- **Promotions:** Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount.
- **Returns:** Return, Sale.canBeReturned.
- **Warranties:** Warranty, BasicWarranty, ExtendedWarranty.

## 1. Single Responsibility Principle (SRP)
*   **Verdict:** Violation (V-01)
*   **Module:** Products - `ProductService`.
*   **Evidence:** `ProductService.java` (Lines 20-22). The constructor instantiates `ProductPersistence` directly.
*   **Explanation:** The `ProductService` class handles two responsibilities: business logic (validations, registrations) and object creation (instantiating the persistence layer). 
*   **Consequence:** If the persistence layer changes its constructor, `ProductService` breaks.
*   **Proposed Solution:** Use dependency injection so `ProductService` receives an already instantiated persistence object.