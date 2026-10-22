# GRASP Audit - Developer 1

## Scope
- **Products:** Product, VideoGame, Console, ProductRepository, ProductService.
- **Accessories:** Accessory, Controller, Cable, Memory.
- **Promotions:** Promotion, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount.
- **Returns:** Return, Sale.canBeReturned.
- **Warranties:** Warranty, BasicWarranty, ExtendedWarranty.

## 1. Information Expert
*   **Verdict:** Correct
*   **Evidence:** `Return.calculateRefundAmount()` has the list of `returnedProducts` and calculates the total. The information is where it belongs.

## 2. Creator
*   **Verdict:** Correct
*   **Evidence:** `ProductService` creates the `ProductPersistence` instance because it needs to save the products.

## 3. Controller
*   **Verdict:** Not applicable
*   **Justification:** My model classes do not receive UI events; the `ConsoleUI` handles that.

## 4. Low Coupling
*   **Verdict:** Violation (V-01)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** `CategoryDiscount.java`, method `calculateDiscount()` uses `instanceof VideoGame`, `instanceof Console`, and `instanceof Accessory`.
*   **Explanation:** The class is tightly coupled to every subclass of Product.
*   **Consequence:** If a new type of product is added, this class must be modified.
*   **Proposed Solution:** Apply Polymorphism. Add an abstract method `getCategory()` to `Product` and let each subclass return its category.
*   **Code of another member:** No.

## 5. High Cohesion
*   **Verdict:** Violation (V-02)
*   **Module:** Warranties & Returns.
*   **Evidence:** `Warranty.java` in `generateWarrantyCertificate()` and `Return.java` in `generateReturnReceipt()`.
*   **Explanation:** The model classes are formatting text for the console (presentation logic). Model classes should only handle business logic.
*   **Consequence:** Changes in UI formatting require modifying domain classes.
*   **Proposed Solution:** Apply Pure Fabrication. Create a formatter class in the service or UI layer to generate the receipt strings.
*   **Code of another member:** No.

## 6. Polymorphism
*   **Verdict:** Violation (V-03)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** The `if/else if` block with `instanceof` in `calculateDiscount`.
*   **Explanation:** It uses conditional logic to handle variations in type, instead of letting objects handle their own behavior.
*   **Consequence:** Violates the Open/Closed principle.
*   **Proposed Solution:** Same as V-01, use polymorphism.
*   **Code of another member:** No.

## 7. Pure Fabrication
*   **Verdict:** Correct
*   **Evidence:** `ProductPersistence` is a pure fabrication because it handles file I/O, which is not a domain responsibility.

## 8. Indirection
*   **Verdict:** Correct
*   **Evidence:** `ProductService` acts as an indirection between the UI and `ProductPersistence`.

## 9. Protected Variations
*   **Verdict:** Violation (V-04)
*   **Module:** Promotions - `CategoryDiscount`.
*   **Evidence:** Hardcoded categories and `instanceof` checks.
*   **Explanation:** If a new category is introduced, this class breaks.
*   **Proposed Solution:** Use Polymorphism.
*   **Code of another member:** No.