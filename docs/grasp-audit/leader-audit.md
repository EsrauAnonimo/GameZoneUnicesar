# GRASP Audit - Technical Leader

## Scope
- **Sale & Persistence:** `Sale.java`, `SalePersistence.java` (SaleRepository).
- **Sale Service:** `SaleService.java` (Integration of accessories, promotions, warranties, and stock management).
- **Presentation & Entrypoint:** `ConsoleUI.java` (Menu navigation, exception handling, and module workflows), `Main.java`.
- **Product Stock Restoration:** `ProductService.restoreStock(String, int)`.

## Summary Table of Identified Violaciones

| ID | GRASP Pattern | Affected Class / Method | Exact Lines | Severity | Brief Summary |
|---|---|---|---|---|---|
| V-L01 | Low Coupling | `SaleService.java` | `SaleService.java:49-57`, `144-148` | High | Coupled to 5 separate services and concrete subclass `Accessory`. |
| V-L02 | High Cohesion | `Sale.java` | `Sale.java:158-195` | Medium | Domain entity builds formatted console receipt string (`generateReceipt`). |
| V-L03 | High Cohesion | `ConsoleUI.java` | `ConsoleUI.java:35-46`, `48-1107` | High | God UI class mixing routing, scanner parsing, and logic across 7 modules. |
| V-L04 | Polymorphism | `SaleService.java` | `SaleService.java:144-148`, `175-177`, `196-198` | High | Uses `instanceof` checks on `Accessory` and `Console` instead of polymorphic behavior. |
| V-L05 | Information Expert | `SaleService.java` & `Sale.java` | `SaleService.java:122-126`, `175-177`, `291-294` | Medium | Service calculates discounts and warranty math and pushes values into `Sale` via setters. |
| V-L06 | Protected Variations | `SaleService.java` | `SaleService.java:144-148` | Medium | Adding a new Product category requires modifying stock update logic in SaleService. |

---

## 1. Information Expert

* **Verdict:** Partially Correct / Violation (V-L05)
* **Compliant Areas:**
  - `Sale.java:40-50` (`calculateTotal()`): `Sale` holds the list of products (`List<Product> products`) and computes the subtotal by summing each product's price. This is a clean example of Information Expert because `Sale` has the information needed for this calculation.
* **Violation Details:**
  - **Module:** `SaleService.java` and `Sale.java`.
  - **Evidence:** `SaleService.java:122-126`, `SaleService.java:175-177`, and `SaleService.java:291-294`.
  - **Explanation:** Even though `Sale` stores fields like `total`, `discountAmount`, and `extendedWarrantyCost`, it does not manage its own calculations. Instead, `SaleService` calculates the discount and warranty amounts and mutates `Sale` using setters (`sale.setTotal()`, `sale.setDiscountAmount()`, `sale.setExtendedWarrantyCost()`). Furthermore, `SaleService.java:175-177` hardcodes the warranty cost rule (`product.getPrice() * 0.10`), which is business knowledge belonging to the warranty/product domain.
  - **Secondary Observation:** In `ProductService.java:112` (`restoreStock`), the service calls `product.setAvailableQuantity(product.getAvailableQuantity() + quantity)`. `Product` should be the expert responsible for altering its own stock rather than exposing raw setters.
* **Consequence:** `Sale` acts mostly as a data holder (anemic domain model) for totals and discounts, while `SaleService` takes on the calculation responsibilities.
* **Proposed Solution:** Move total calculation and discount application methods inside `Sale` (e.g., `sale.applyDiscount(amount, promotionName)` and `sale.applyWarrantyCost(cost)`). Remove public setters for `total` and `discountAmount`.

---

## 2. Creator

* **Verdict:** Correct
* **Evidence:** `Main.java:26-62`.
* **Explanation:** `Main.java` acts as the composition root of the application. It creates instances of repositories (`SalePersistence`, `ProductPersistence`, `WarrantyRepository`, etc.), services (`SaleService`, `ProductService`, etc.), and `ConsoleUI`, wiring them together through constructor injection.
* **In Sale Scope:** `SaleService` does not directly instantiate `Sale` objects; it receives them already constructed from `ConsoleUI` (where user input is gathered). For warranties, `SaleService.java:191-218` delegates the creation/granting of warranties to `warrantyService.grantWarranty(...)`, respecting the Creator pattern by letting the dedicated service handle warranty records.

---

## 3. Controller

* **Verdict:** Applicable / Design Smell (Overburdened Controller)
* **Evidence:** `ConsoleUI.java:48-75` and `ConsoleUI.java:63-73`.
* **Explanation:** `ConsoleUI` serves as the system controller for console input. It receives the user's menu choice and coordinates calls to the domain services (`saleService.registerSale()`, `productService.listAllProducts()`, etc.).
* **Observation:** While it correctly prevents UI code from directly manipulating persistence, `ConsoleUI` is a monolithic controller of over 1100 lines. It controls 7 different business domains at once and directly does input parsing, validation display, and service coordination.
* **Proposed Solution:** Decompose `ConsoleUI` into sub-controllers or dedicated menu handlers (e.g., `SaleMenuHandler`, `ProductMenuHandler`).

---

## 4. Low Coupling

* **Verdict:** Violation (V-L01)
* **Module:** `SaleService.java`.
* **Evidence:**
  - `SaleService.java:49-57`: The constructor requires 5 dependencies: `SalePersistence`, `ProductService`, `AccessoryService`, `PromotionService`, and `WarrantyService`.
  - `SaleService.java:144-148`:
    ```java
    if (product instanceof Accessory) {
        accessoryService.updateStock(id, newQuantity);
    } else {
        productService.updateStock(id, newQuantity);
    }
    ```
* **Explanation:** `SaleService` is directly tied to two separate inventory services (`ProductService` and `AccessoryService`) plus promotion and warranty services. It has high coupling because it explicitly knows about concrete sub-types of products to figure out which service updates stock.
* **Consequence:** If a new inventory service is added or if the way accessory stock is stored changes, `SaleService` breaks and must be updated. Testing `SaleService` requires mocking or instantiating 5 different services.
* **Proposed Solution:** Create a unified inventory interface/service (e.g., `StockService` or `InventoryManager`) that hides whether an item is an Accessory, Console, or VideoGame. `SaleService` would only depend on that single inventory coordinator.

---

## 5. High Cohesion

* **Verdict:** Violation (V-L02, V-L03)
* **Evidence 1 (V-L02):** `Sale.java:158-195` (`generateReceipt()`).
  - **Explanation:** `Sale` is a domain entity meant to hold the business data of a transaction. However, `generateReceipt()` constructs formatted text for console display (`=== Recibo GameZone Unicesar ===`, column labels, dashed separators).
  - **Consequence:** If the presentation format changes (e.g., translating labels, changing layout, or formatting for a GUI), the domain class `Sale` has to be changed. This lowers the cohesion of `Sale`.
  - **Proposed Solution:** Move receipt formatting to a dedicated presentation class (e.g., `ReceiptFormatter` or `SaleView`).
* **Evidence 2 (V-L03):** `ConsoleUI.java:48-1107`.
  - **Explanation:** `ConsoleUI` contains 1107 lines of code doing scanner management, numeric parsing, string validation, business service calls, and error handling for all 7 modules of the store.
  - **Consequence:** The class has too many responsibilities, making it hard to maintain and modify without introducing side effects.
* **Evidence 3:** `SaleService.java:299-301`.
  - **Explanation:** `SaleService` prints warning messages directly to `System.out` when a promotion fails to apply. Business services should not handle console output.

---

## 6. Polymorphism

* **Verdict:** Violation (V-L04)
* **Module:** `SaleService.java`.
* **Evidence:**
  - `SaleService.java:144-148`:
    ```java
    if (product instanceof Accessory) {
        accessoryService.updateStock(id, newQuantity);
    } else {
        productService.updateStock(id, newQuantity);
    }
    ```
  - `SaleService.java:175-177`:
    ```java
    if (product instanceof Console && wantsExtendedWarranty(product, productIdsWithExtendedWarranty)) {
        total += product.getPrice() * 0.10;
    }
    ```
  - `SaleService.java:196-198`:
    ```java
    if (!(product instanceof Console)) {
        continue;
    }
    ```
* **Explanation:** `SaleService` uses type inspection (`instanceof`) to handle differences in behavior. It branches depending on whether a product is an `Accessory` or a regular `Product` for stock updates, and checks if a product is a `Console` to calculate and assign warranties.
* **Consequence:** Violates Polymorphism and the Open/Closed Principle. If a new type of product is introduced (such as digital codes, merchandise, or accessories with warranties), `SaleService` must be modified with more conditional branches.
* **Proposed Solution:** Delegate stock update through a unified polymorphic method (e.g., in a unified stock catalog) and let `Product` provide a polymorphic method `boolean isWarrantyEligible()` or let the warranty module evaluate eligibility polymorphically.

---

## 7. Pure Fabrication

* **Verdict:** Correct
* **Evidence:** `SalePersistence.java:9-49` (and `SaleValidator.java:1-50`).
* **Explanation:** `SalePersistence` is a clear example of Pure Fabrication. Saving and loading sales to a binary file (`data/sales.dat`) is not a problem that naturally belongs to the domain concept of a "Sale". Creating an artificial class `SalePersistence` prevents low cohesion in `Sale` and separates serialization logic from domain logic.
* **In Validation:** `SaleValidator` is also a pure fabrication that isolates input validation rules so they don't pollute the domain entity or the service.

---

## 8. Indirection

* **Verdict:** Correct
* **Evidence:** `SaleService.java:32-314` between `ConsoleUI.java` and `SalePersistence.java`.
* **Explanation:** `SaleService` acts as an intermediary (indirection layer) between the user interface (`ConsoleUI`) and the persistence layer (`SalePersistence`).
* **Advantage:** `ConsoleUI` has no knowledge of how sales are stored on disk. If the persistence mechanism changes from Java serialization to JSON, XML, or SQL, `ConsoleUI` remains unaffected because `SaleService` absorbs the interface.

---

## 9. Protected Variations

* **Verdict:** Violation (V-L06)
* **Module:** `SaleService.java`.
* **Evidence:** `SaleService.java:144-148` (inventory dispatching) and `SaleService.java:175-177` (warranty price hardcoding).
* **Explanation:** The design fails to protect `SaleService` against anticipated variations in the product hierarchy. Because product types and stock repositories vary, the code uses hardcoded `instanceof` checks and calculates warranty percentages directly (`0.10`).
* **Consequence:** Changes to warranty pricing policies or new product classes directly impact `SaleService`.
* **Proposed Solution:** Introduce stable interfaces for inventory updates and delegate warranty price calculations to `WarrantyService` or `Warranty` sub-classes so that variations in rules are encapsulated behind those interfaces.

---

## Summary of Proposed Solutions for Refactoring (Phase 2)

1. **Unify Inventory Management (fixes V-L01, V-L04, V-L06):**
   - Create a common interface or facade for inventory (`StockManager` or `InventoryService`) that handles both `Product` and `Accessory` items uniformly. `SaleService` will only depend on this single service instead of depending on both `ProductService` and `AccessoryService`.
2. **Move Receipt Generation out of Domain (fixes V-L02):**
   - Extract `Sale.generateReceipt()` into a Pure Fabrication formatter class (e.g., `SaleReceiptPrinter` or `SaleView`). `Sale` should only contain transaction data and domain logic.
3. **Encapsulate Total & Discount Logic in Sale (fixes V-L05):**
   - Add methods to `Sale` like `applyPromotionDiscount(double amount, String promoName)` and `addExtendedWarranty(double cost)` that update the total internally, removing public setters for `total` and `discountAmount`.
4. **Decompose ConsoleUI (fixes V-L03):**
   - Split `ConsoleUI` into dedicated sub-menu handlers (`SaleMenuHandler`, `ProductMenuHandler`, etc.) so no single presentation class handles all 7 modules.
5. **Clean up Product Stock Restoration in ProductService:**
   - Add a method `product.increaseStock(quantity)` in `Product` so that `ProductService.restoreStock()` does not directly call getters and setters on available quantity.


