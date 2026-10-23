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

## 2. Open/Closed Principle (OCP)
*   **Verdict:** Violation (V-02)
*   **Module:** Products - `ProductService`.
*   **Evidence:** `ProductService.java` (Lines 24 and 44). Methods `registerVideoGame()` and `registerConsole()`.
*   **Explanation:** If the store sells a new type of product (e.g., Merchandise), we would have to modify `ProductService` to add a new register method. The class is not closed for modification.
*   **Consequence:** Adding new product types forces us to modify existing, working code.
*   **Proposed Solution:** Create a single `registerProduct(Product product)` method using polymorphism.

## 3. Liskov Substitution Principle (LSP)
*   **Verdict:** Correct
*   **Module:** Products & Warranties.
*   **Evidence:** `Product.java` (Line 81) has `isWarrantyEligible()`. Subclasses like `Console.java` override it correctly to return `true`. Also, the `getCategory()` fix ensures all subclasses behave correctly.
*   **Explanation:** Any subclass can replace the base `Product` class without breaking the system's behavior.