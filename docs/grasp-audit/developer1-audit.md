# GRASP Audit - Developer 1

## Scope
- **Products:** Product, VideoGame, Console, ProductRepository, ProductService.
- **Accessories:** Accessory, Controller, Cable, Memory.
- **Promotions:** Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount.
- **Returns:** Return, Sale.canBeReturned.
- **Warranties:** Warranty, BasicWarranty, ExtendedWarranty.

## 1. Information Expert
*   **Verdict:** Violation (V-01)
*   **Module:** Returns - `Sale.canBeReturned`
*   **Evidence:** `Sale.java`, method `canBeReturned()` at Line 200. The method uses `LocalDate.now()` internally to calculate the 30-day limit.
*   **Explanation:** The `Sale` class has the sale date, but it does not have the information about the *current date*. By calling `LocalDate.now()`, it assumes a responsibility that doesn't belong to it.
*   **Consequence:** Impossible to unit test returns for different dates without waiting for real time to pass.
*   **Proposed Solution:** Pass the current date as a parameter: `canBeReturned(LocalDate currentDate)`.
*   **Code of another member:** No.

*   **Verdict:** Correct (Accessory Hierarchy)
*   **Evidence:** `Accessory.java` at Line 15. The `Accessory` class holds the `compatibleConsoles` list and its own methods to add/remove consoles. The information is exactly where it belongs.

## 2. Creator
*   **Verdict:** Correct
*   **Evidence:** `ProductService.java` at Line 22 creates the `ProductPersistence` instance. `Accessory.java` creates its own `ArrayList` for `compatibleConsoles` because it aggregates them.

## 3. Controller
*   **Verdict:** Not applicable
*   **Justification:** My model classes (Product, Accessory, Warranty, Return) do not receive UI events; the `ConsoleUI` handles that.

## 4. Low Coupling
*   **Verdict:** Violation (V-02)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** `CategoryDiscount.java`, method `calculateDiscount()` at Lines 106, 108, and 110. Uses `instanceof VideoGame`, `instanceof Console`, and `instanceof Accessory`.
*   **Explanation:** The class is tightly coupled to every subclass of Product.
*   **Consequence:** If a new type of product is added, this class must be modified.
*   **Proposed Solution:** Apply Polymorphism. Add an abstract method `getCategory()` to `Product` and let each subclass return its category.
*   **Code of another member:** No.

## 5. High Cohesion
*   **Verdict:** Violation (V-03)
*   **Module:** Warranties & Returns.
*   **Evidence:** `Warranty.java` in `generateWarrantyCertificate()` at Line 103, and `Return.java` in `generateReturnReceipt()` at Line 164.
*   **Explanation:** The model classes are formatting text for the console (presentation logic). Model classes should only handle business logic.
*   **Consequence:** Changes in UI formatting require modifying domain classes.
*   **Proposed Solution:** Apply Pure Fabrication. Create a formatter class in the service or UI layer.
*   **Code of another member:** No.

## 6. Polymorphism
*   **Verdict:** Violation (V-04)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** The `if/else if` block with `instanceof` in `calculateDiscount()` at Lines 106, 108, and 110.
*   **Explanation:** It uses conditional logic to handle variations in type, instead of letting objects handle their own behavior.
*   **Consequence:** Violates the Open/Closed principle.
*   **Proposed Solution:** Same as V-02, use polymorphism.
*   **Code of another member:** No.

## 7. Pure Fabrication
*   **Verdict:** Correct
*   **Evidence:** `ProductPersistence.java` at Line 16 is a pure fabrication because it handles file I/O, which is not a domain responsibility.

## 8. Indirection
*   **Verdict:** Correct
*   **Evidence:** `ProductService.java` acts as an indirection between the UI and `ProductPersistence`.

## 9. Protected Variations
*   **Verdict:** Violation (V-05)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** Hardcoded categories and `instanceof` checks in `calculateDiscount()` at Lines 106, 108, and 110.
*   **Explanation:** If a new category is introduced, this class breaks.
*   **Proposed Solution:** Use Polymorphism.
*   **Code of another member:** No.