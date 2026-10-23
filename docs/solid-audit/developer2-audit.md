# SOLID Audit Report — Developer 2

**Student / Developer:** Cristian Andrés Pérez González (Developer 2)  
**Audited branch:** `feature/solid-audit` (based on `develop` post GRASP refactoring)  
**Assigned Scope:**
- **People Module:** `Person.java`, `Customer.java`, `Seller.java`, `PersonPersistence.java`, `PersonService.java`.
- **Ancillary Modules & Boundaries:** `Return.java`, `ReturnRepository.java`, `ReturnService.java`, `WarrantyRepository.java`, `WarrantyService.java`, `AccessoryPersistence.java`, `AccessoryService.java`, `PromotionRepository.java`, `PromotionService.java`.

---

## 1. Summary of SOLID Violations

After reviewing my assigned classes and how they connect with the rest of the project, I found **4 real SOLID violations**. Even though our code works in console, some classes have too many jobs, break the layer direction, or still use type checks instead of polymorphism.

| ID | SOLID Principle | Affected Class / Method | Exact Line Numbers | Severity | Brief Summary |
|---|---|---|---|---|---|
| **V-D2-01** | **DIP** (Dependency Inversion) | `ReturnRepository.java` | `ReturnRepository.java:7-9`, `37-39`, `51-56` | **High** | A storage/persistence repository directly injects 3 high-level business services (`SaleService`, `ProductService`, `AccessoryService`), inverting our 4-layer architecture. |
| **V-D2-02** | **SRP** (Single Responsibility) | `Return.java` | `Return.java:164-186` | **Medium** | The domain entity `Return` builds formatted console receipts with Spanish text and decorative borders (`generateReturnReceipt`). |
| **V-D2-03** | **SRP** (Single Responsibility) | `ReturnService.java` | `ReturnService.java:182-260` | **Medium** | `ReturnService` calculates general store sales (`calculateMonthlySales`) and monthly balance reports, taking on accounting and sales responsibilities. |
| **V-D2-04** | **OCP** (Open/Closed) | `ReturnService.java` | `ReturnService.java:113-117`, `324-329` | **Medium** | Uses `instanceof Console` to cancel warranties and `instanceof Accessory` to restore stock, so adding a new product type forces editing this service. |

---

## 2. Evaluation by Principle

### 2.1 Single Responsibility Principle (SRP)

#### Violation V-D2-02: Receipt formatting inside `Return.java`
* **Where:** `Return.java`, lines 164 to 186 (`generateReturnReceipt`).
* **Offending Code:**
  ```java
  public String generateReturnReceipt() {
      StringBuilder sb = new StringBuilder();
      sb.append("===== COMPROBANTE DE DEVOLUCION =====\n");
      sb.append("ID: ").append(id != null ? id : "N/A").append("\n");
      ...
      sb.append("Monto reembolsado: $").append(String.format("%.2f", refundAmount)).append("\n");
      sb.append("=====================================");
      return sb.toString();
  }
  ```
* **Why it's a violation:** `Return` is a domain class. Its only job should be holding the data of a return (id, date, original sale, returned products, and refund amount). When it builds a pretty console receipt with `=====`, it mixes presentation logic with business data. The leader already fixed this for `Sale` by creating `ReceiptFormatter`, but I forgot to do the same for `Return`.
* **Consequence:** If we want to change the receipt language, change the border, or print to a graphical window or file, we have to change the `Return` domain entity.
* **Proposed Solution:** Move `generateReturnReceipt()` out of `Return.java` and put it into `ReceiptFormatter.java` (e.g. `ReceiptFormatter.formatReturnReceipt(Return ret)`), leaving `Return` as a clean entity.

---

#### Violation V-D2-03: `ReturnService` doing store sales accounting
* **Where:** `ReturnService.java`, lines 182 to 260 (`generateMonthlyBalance` and `calculateMonthlySales`).
* **Offending Code:**
  ```java
  public double generateMonthlyBalance(int month, int year) {
      double monthlySales = calculateMonthlySales(month, year);
      double monthlyReturns = calculateMonthlyReturns(month, year);
      return monthlySales - monthlyReturns;
  }

  public double calculateMonthlySales(int month, int year) {
      double total = 0.0;
      for (Sale sale : saleService.listAllSales()) {
          ...
          total += sale.getTotal();
      }
      return total;
  }
  ```
* **Why it's a violation:** `ReturnService` is supposed to manage product returns (validating return dates, restoring stock, and canceling warranties). But here it also queries all sales from `saleService` and calculates total monthly sales income. Calculating overall store revenue is a sales or reporting responsibility, not a returns responsibility.
* **Consequence:** If the way monthly sales revenue is calculated changes, or if we add discounts or taxes to sales reports, `ReturnService` has to be modified. It has two reasons to change.
* **Proposed Solution:** Move `calculateMonthlySales` to `SaleService` or a reporting helper, and make `ReturnService` only compute return totals, or pass both totals to a dedicated balance calculation.

---

### 2.2 Open/Closed Principle (OCP)

#### Violation V-D2-04: Type checking with `instanceof` in `ReturnService`
* **Where:** `ReturnService.java`, lines 113 to 117 and lines 324 to 329.
* **Offending Code:**
  ```java
  // Lines 113-117
  for (Product product : productsToReturn) {
      if (product instanceof Console) {
          warrantyRefund += warrantyService.cancelWarranties(product.getId(), sale.getId());
      }
  }

  // Lines 324-329
  private void restoreStock(Product product) {
      if (product instanceof Accessory) {
          accessoryService.restoreStock(product.getId(), 1);
          return;
      }
      productService.restoreStock(product.getId(), 1);
  }
  ```
* **Why it's a violation:** The class is not closed for modification. If we create a new product subclass tomorrow (like a wearable, a collector item, or a headset with a warranty), `ReturnService` will not know what to do unless we open it and add more `if (product instanceof ...)` checks.
* **Consequence:** Every new item type risks breaking the return process.
* **Proposed Solution:** In `Product.java`, we already have `product.isWarrantyEligible()`. We should check `if (product.isWarrantyEligible())` instead of `instanceof Console`. For restoring stock, we should use a polymorphic inventory method or have `Product` know how to increase its own stock.

---

### 2.3 Liskov Substitution Principle (LSP)

* **Status:** Mostly correct in the People module.
* **Analysis:**
  - `Person` has two subclasses: `Customer` and `Seller`.
  - Both subclasses properly override `getRoleDescription()`. If a list of `Person` is processed anywhere, replacing `Person` with a `Customer` or `Seller` does not throw unexpected exceptions or violate expectations.
  - In `Return`, substituting different subtypes of `Product` in `returnedProducts` works cleanly for computing `product.getPrice()`.

---

### 2.4 Interface Segregation Principle (ISP)

* **Status:** Acceptable within project constraints (no interfaces).
* **Analysis:**
  - `PersonService` only exposes methods that customers and sellers really need (`registerCustomer`, `registerSeller`, `listCustomers`, `listSellers`, `findCustomerById`). It doesn't force callers into unrelated operations.
  - However, in `ReturnRepository`, because it injects full services (`SaleService`, `ProductService`, `AccessoryService`), it is exposed to massive APIs when it only needs single lookup methods (`findSaleById`, `findProductById`).

---

### 2.5 Dependency Inversion Principle (DIP)

#### Violation V-D1-01 / V-D2-01: Inverted layer dependency in `ReturnRepository`
* **Where:** `ReturnRepository.java`, lines 7 to 9, lines 37 to 39, and constructor at lines 51 to 56.
* **Offending Code:**
  ```java
  package com.gamezone.persistence;

  import com.gamezone.service.AccessoryService;
  import com.gamezone.service.ProductService;
  import com.gamezone.service.SaleService;

  public class ReturnRepository {
      private final SaleService saleService;
      private final ProductService productService;
      private final AccessoryService accessoryService;

      public ReturnRepository(SaleService saleService, ProductService productService,
                              AccessoryService accessoryService) {
          this.saleService = saleService;
          this.productService = productService;
          this.accessoryService = accessoryService;
      }
  ```
* **Why it's a violation:** In our 4-layer architecture, dependencies should only point down: `UI -> Service -> Persistence -> Model`. Here, `ReturnRepository` is a **persistence** class, but it imports and depends on 3 **services** from the layer above! High-level modules and low-level modules are completely tangled.
* **Consequence:**
  - It creates an architectural cycle (`ReturnService -> ReturnRepository -> SaleService / ProductService / AccessoryService`).
  - You cannot test `ReturnRepository` reading from disk without instantiating and wiring 3 complete business services first.
* **Proposed Solution:** `ReturnRepository` should only read lines and return raw data (IDs, dates, reasons). The job of looking up the actual `Sale` and `Product` objects from those IDs should be done in `ReturnService`, which is the business layer. This keeps `ReturnRepository` as a pure file reader/writer.

---

## 3. Conclusion & Next Steps for Refactoring
For the refactoring phase, I should:
1. Extract `generateReturnReceipt()` from `Return.java` to `ReceiptFormatter.java` (Fixes SRP).
2. Remove `SaleService`, `ProductService`, and `AccessoryService` from `ReturnRepository` and move entity resolution to `ReturnService` (Fixes DIP).
3. Replace `instanceof Console` with `product.isWarrantyEligible()` in `ReturnService` (Fixes OCP).
4. Move monthly sales total calculation out of `ReturnService` (Fixes SRP).
