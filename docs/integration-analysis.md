# System Integration Analysis — Adjustments A1 through A7

This document details the architectural analysis, technical decisions, and design rationale behind the integration adjustments applied during **Phase 1 through Phase 4** of the GameZone Unicesar system integration (Requerimiento 5).

The integration combines four distinct system extensions—**Accessories**, **Promotions**, **Warranties**, and **Returns**—into a single unified system where transactions, inventory, warranties, and financial reporting operate coherently without regressions.

---

## 1. Adjustment A1 — Category Discount for Accessories

- **Type:** New Feature
- **Branch:** `feature/accessory-category-discount`
- **Phase:** Phase 2 (Promotions integration)

### Problem & Cause
In the standalone Promotion module (Requirement 2), `CategoryDiscount` was restricted to validate and apply discounts exclusively to products matching `"VIDEOGAME"` or `"CONSOLE"`. When the Accessories module (Requirement 1) was merged into the catalog, accessories inherited from `Product` as legitimate saleable items. However, the store had no mechanism to launch category-specific promotions on accessories. If a customer attempted to purchase a controller, cable, or memory card, `CategoryDiscount` could not recognize `"ACCESSORY"` as a valid target category.

### Solution Applied
1. **Model (`CategoryDiscount`):** Expanded the target category validation to recognize `"ACCESSORY"` (case-insensitive). When evaluating items in `calculateDiscount(Sale sale)`, any product instance of `Accessory` is classified under the `"ACCESSORY"` category and has the discount applied to its price.
2. **Service (`PromotionService`):** Updated `registerCategoryDiscount(...)` to validate that the category parameter belongs to the allowed set: `"VIDEOGAME"`, `"CONSOLE"`, or `"ACCESSORY"`.
3. **UI (`ConsoleUI`):** Updated the console menu prompts when registering a category discount to allow selecting `"ACCESSORY"`.
4. **Data (`data/promotions.csv`):** Added an active promotion targeting accessories to demonstrate compliance during system evaluation.

---

## 2. Adjustment A2 — Circular Dependency Resolution in Warranty Module

- **Type:** Defect Fix
- **Branch:** `fix/warranty-circular-dependency`
- **Phase:** Phase 3 (Warranties integration)

### Problem & Cause
In Requirement 4, warranties needed to store references to the covered `Product` and the associated `Sale`. During deserialization from `data/warranties.csv`, `WarrantyRepository` needed to rehydrate full `Sale` objects by sale ID. If `WarrantyRepository` were injected with `SaleService`, it produced a circular dependency graph:
```
SaleService -> WarrantyService -> WarrantyRepository -> SaleService
```
This cycle prevents pure constructor injection in `Main.java` at startup without relying on mutable setters or cyclic references.

### Solution Applied
1. **Decoupling `WarrantyRepository`:** `WarrantyRepository` was refactored so it does not depend on `SaleService`. Instead, to resolve existing `Sale` entities, it queries `SalePersistence` directly (which is a leaf persistence class reading `data/sales.dat`).
2. **Dependency Direction in `WarrantyService`:** `WarrantyService` receives `WarrantyRepository`, which relies on leaf persistence and product services (`ProductService` and `AccessoryService`). Neither service depends cyclicly on another.
3. **Clean Graph Construction in `Main`:** The instantiation order in `Main.java` was structured linearly:
   ```
   SalePersistence -> ProductService -> AccessoryService -> WarrantyRepository -> WarrantyService -> SaleService
   ```
4. **Documentation:** Updated `docs/warranty-class-diagram.md` to document the resolved dependencies and prevent regression.

---

## 3. Adjustment A3 — Unified Sale Registration Flow

- **Type:** Architectural Refactoring
- **Branch:** `refactor/unified-sale-registration`
- **Phase:** Phase 3 (Warranties integration)

### Problem & Cause
Requirements 1 (Accessories), 2 (Promotions), and 4 (Warranties) each modified `SaleService.registerSale` independently. When combined naively:
- Discounts could mistakenly apply to extended warranty fees rather than merchandise alone.
- Iterating through `products` with naive decrements (`product.getAvailableQuantity() - 1`) caused stale-data overwrites when multiple units of the same product or accessory were purchased in a single sale.
- If warranties were persisted before the sale was saved to disk, `WarrantyRepository` could discard warranty records due to missing sale IDs in `data/sales.dat`.
- Conversely, saving the sale twice led to file corruption and inconsistent receipts.

### Solution Applied
Reorganized `SaleService.registerSale(Sale sale, List<String> productIdsWithExtendedWarranty)` into a strict 8-step pipeline:
1. **Validate Items:** Verify `sale != null` and that `sale.getProducts()` contains at least one non-null item.
2. **Stock Validation:** Aggregate requested unit counts per distinct product ID (`countUnitsInSale`) and compare against live inventory from `ProductService` or `AccessoryService`. Abort if any item lacks sufficient stock.
3. **Calculate Subtotal:** Compute the raw merchandise subtotal (`roundToCents(sum(item.getPrice()))`).
4. **Apply Promotion:** Query `PromotionService.findBestPromotionFor(sale)` and compute the discount strictly against the merchandise subtotal, capping at `subtotal`.
5. **Compute Extended Warranty Cost:** Determine which consoles requested 12-month extended coverage (+10% of console price) and record `extendedWarrantyCost` on `Sale`.
6. **Calculate Final Total:** Execute `total = subtotal - discount + extendedWarrantyCost`.
7. **Update Inventory:** For each unique item ID, subtract total units purchased and delegate to `accessoryService.updateStock(id, newStock)` or `productService.updateStock(id, newStock)`, updating in-memory quantities consistently.
8. **Persist Sale & Assign Warranties:** First persist `sale` via `SalePersistence.save(sales)`. Immediately afterward, invoke `warrantyService.assignBasicWarranty` or `warrantyService.assignExtendedWarranty` for each console, avoiding double-saves while ensuring disk references resolve properly.

Also enhanced `Sale.generateReceipt()` to display `Subtotal`, `Descuento (nombre)`, `Garantias extendidas`, and `Total`.

---

## 4. Adjustment A4 — Accessory Stock Restoration on Return

- **Type:** Defect Fix
- **Branch:** `fix/return-accessory-stock`
- **Phase:** Phase 4 (Returns integration)

### Problem & Cause
In Requirement 3, `ReturnService.registerReturn` restored inventory by exclusively invoking `ProductService.restoreStock(productId, qty)`. Because `ProductService` only manages `VideoGame` and `Console` objects stored in `data/products.dat`, returning an accessory (controller, cable, memory) did not increment the accessory's stock in `data/accessories.csv`.

### Solution Applied
1. **`AccessoryService` Extension:** Implemented `restoreStock(String accessoryId, int quantity)` in `AccessoryService`, which mirrors the validation and persistence behavior of `ProductService.restoreStock`.
2. **Delegation in `ReturnService`:** Injected `AccessoryService` into `ReturnService`. When processing each returned item, `ReturnService` checks if `product instanceof Accessory`:
   - If true, delegates to `accessoryService.restoreStock(productId, quantity)`.
   - Otherwise, delegates to `productService.restoreStock(productId, quantity)`.
3. **Resolution in `ReturnRepository`:** Updated `ReturnRepository` during CSV deserialization to resolve product identifiers against both `ProductService` and `AccessoryService`.

---

## 5. Adjustment A5 — Proportional Refund on Discounted Sales

- **Type:** Defect Fix
- **Branch:** `fix/return-discounted-refund`
- **Phase:** Phase 4 (Returns integration)

### Problem & Cause
`Return.calculateRefundAmount()` originally iterated over `returnedProducts` and summed their full catalog list prices (`product.getPrice()`). If the customer purchased those items under a promotion (e.g., 20% off total or category discount), refunding full list prices meant the store paid back more money than the customer actually tendered, creating financial losses.

### Solution Applied
1. **Proportional Refund Formula in `Return`:** Refactored refund calculation so that each returned item is refunded proportional to the effective discount ratio of the original transaction:
   $$\text{refundRate} = 1.0 - \left(\frac{\text{discountAmount}}{\text{subtotal}}\right)$$
   $$\text{itemRefund} = \text{itemPrice} \times \text{refundRate}$$
   If no promotion was applied or `subtotal == 0`, `refundRate` defaults to `1.0`.
2. **Receipt Transparency:** Updated `Return.generateReturnReceipt()` to display the item list price, the proportional discount deducted, and the net refund per product.

---

## 6. Adjustment A6 — Comprehensive Monthly Balance Report

- **Type:** Defect Fix
- **Branch:** `fix/monthly-balance-report`
- **Phase:** Phase 4 (Returns integration)

### Problem & Cause
Requirement 3 specified that the monthly financial report must present total sales, total returns, and net balance for a given month and year. However, `ReturnService.generateMonthlyBalance` only returned a single scalar value representing the net difference (`sales - returns`). The UI had no programmatic access to the individual sales and returns components without re-implementing persistence queries. Furthermore, total sales needed to be calculated using the finalized sale total (`sale.getTotal()`, which includes promotional discounts and warranty fees) rather than raw product prices.

### Solution Applied
1. **Service Granularity in `ReturnService`:** Introduced two dedicated query methods:
   - `public double calculateMonthlySales(int month, int year)`: Sums `sale.getTotal()` for all sales completed within the target month and year.
   - `public double calculateMonthlyReturns(int month, int year)`: Sums `returnRecord.getRefundAmount()` for all returns processed within that month and year.
2. **Preserved Method Contract:** Kept `public double generateMonthlyBalance(int month, int year)` to return `calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year)`, ensuring full backward compatibility.
3. **Console UI Display:** Updated option in `ConsoleUI` (`Consultar balance mensual`) to invoke these methods and display:
   - Total Ventas del mes
   - Total Devoluciones del mes
   - Balance Neto

---

## 7. Adjustment A7 — Warranty Revocation on Console Return

- **Type:** New Feature
- **Branch:** `feature/return-warranty-cancellation`
- **Phase:** Phase 4 (Returns integration)

### Problem & Cause
When a customer purchases a console, basic warranty (and optionally extended warranty) is created and saved to `data/warranties.csv`. Prior to this adjustment, returning a console did not alter warranty records. This introduced two critical business flaws:
1. The customer could retain an active warranty certificate for a physical product they no longer owned.
2. If the customer purchased an extended warranty (+10%), that fee was not reimbursed upon returning the console.

### Solution Applied
1. **`WarrantyService` Revocation:** Added `public double cancelWarranties(String productId, String saleId)`, which finds and removes warranty records matching the given product and sale ID from `data/warranties.csv`. If an `ExtendedWarranty` is cancelled, it returns the additional cost paid; for `BasicWarranty`, it returns `0.0`.
2. **Coordination in `ReturnService`:** Injected `WarrantyService` into `ReturnService`. During `registerReturn`, for every returned item that is an instance of `Console`, `ReturnService` calls `warrantyService.cancelWarranties(...)` and adds the returned warranty amount to the total refund.
3. **Domain Model (`Return`):** Added a field `refundedWarrantyAmount` to track warranty reimbursements and included it in the return receipt breakdown.

---

## Summary Matrix

| ID | Adjustment Name | Type | Key Layer / Classes Modified | Primary Impact |
|:---|:---|:---|:---|:---|
| **A1** | Accessory Category Discount | Feature | `model.CategoryDiscount`, `service.PromotionService` | Allows promotions to target accessories. |
| **A2** | Warranty Circular Dependency | Fix | `persistence.WarrantyRepository`, `service.WarrantyService`, `Main` | Eliminates circular dependency in constructor injection. |
| **A3** | Unified Sale Registration | Refactor | `service.SaleService`, `model.Sale`, `ui.ConsoleUI` | 8-step transaction flow, multi-unit stock, warranty order. |
| **A4** | Accessory Stock on Return | Fix | `service.ReturnService`, `service.AccessoryService` | Restores accessory stock upon product return. |
| **A5** | Proportional Return Refund | Fix | `model.Return` | Deducts proportional promotional discount from refund. |
| **A6** | Monthly Balance Report | Fix | `service.ReturnService`, `ui.ConsoleUI` | Exposes monthly sales, returns, and net balance cleanly. |
| **A7** | Warranty Cancellation on Return | Feature | `service.WarrantyService`, `service.ReturnService`, `model.Return` | Revokes warranty and refunds extended fee on console return. |
