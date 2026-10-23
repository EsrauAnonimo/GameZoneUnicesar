# SOLID Audit — Technical Leader Report

**Project:** GameZone Unicesar  
**Auditor (Technical Leader):** Esteban Raúl Vergara Jaime  
**Role Scope:** Sales Module (`Sale.java`, `SalePersistence.java`, `SaleService.java`), UI & Bootstrap (`ConsoleUI.java`, `Main.java`), Product Stock Restoration (`ProductService.restoreStock`), and GRASP Refactored Components (`ReceiptFormatter.java`).  
**Commit Audited:** `develop` branch post GRASP refactoring  

---

## 1. Executive Summary & Violation Matrix

As Technical Leader, I audited the classes under my direct responsibility along with the cross-cutting components I touched during the GRASP refactoring. While the GRASP refactor solved critical high cohesion and polymorphism problems (such as eliminating receipt printing from `Sale` and removing `instanceof` cascades in `SaleService`), a thorough SOLID audit reveals **5 real, verifiable violations** across SRP, OCP, LSP, ISP, and DIP.

| ID | SOLID Principle | Affected Class / Component | Exact Lines | Severity | Summary |
|---|---|---|---|---|---|
| **V-S01** | **SRP** (Single Responsibility) | `SaleService.java` | `SaleService.java:99-165`, `281-312` | **High** | Multi-responsibility service orchestrating promotions, warranties, stock updates, currency rounding, and console warning logs. |
| **V-S02** | **DIP** (Dependency Inversion) | `ConsoleUI.java` | `ConsoleUI.java:35-46` | **High** | Monolithic UI tightly coupled to 7 concrete services and hardcoding `new Scanner(System.in)` inside its constructor. |
| **V-S03** | **OCP** (Open/Closed) | `SaleService.java` & `Product.java` | `SaleService.java:155`, `Product.java:136-142` | **Medium** | Stock update method signature hardcodes concrete services (`ProductService`, `AccessoryService`), requiring modification if new product types are added. |
| **V-S04** | **LSP / OCP** | `ProductService.java` (`restoreStock`) | `ProductService.java:105-119` | **Medium** | Directly mutates `product.setAvailableQuantity(...)` rather than delegating to encapsulated domain method `product.increaseStock(quantity)`. |
| **V-S05** | **SRP** (Single Responsibility) | `Sale.java` | `Sale.java:159-166` | **Low** | Domain transaction entity embeds the 30-day return eligibility rule (`canBeReturned`), coupling the sale record to return policy changes. |

---

## 2. Detailed Findings by Principle

### 2.1 Single Responsibility Principle (SRP)

#### Violation V-S01: Overburdened `SaleService`
* **Affected Class:** `com.gamezone.service.SaleService`
* **Evidence:** Lines 99–165 (`registerSale`) and Lines 281–312 (`applyBestPromotion`):
  ```java
  // In SaleService.java
  applyBestPromotion(sale);
  double warrantyTotal = calculateWarrantyCost(sale, productIdsWithExtendedWarranty);
  ...
  product.updateStock(productService, accessoryService, newQuantity);
  sales.add(sale);
  persistence.save(sales);
  assignWarranties(sale, productIdsWithExtendedWarranty);
  ```
  And inside `applyBestPromotion` (lines 305–311):
  ```java
  catch (RuntimeException e) {
      ...
      System.out.println("Advertencia: no se pudo aplicar la promocion a la venta "
              + sale.getId() + ". Se registra sin descuento. Detalle: " + e.getMessage());
  }
  ```
* **Explanation:** `SaleService` has far too many reasons to change:
  1. If promotional discount selection, threshold capping, or error-logging changes.
  2. If the warranty pricing rule (10% extra for extended warranty) or warranty assignment policy changes.
  3. If multi-unit inventory validation or deduction logic changes.
  4. If sales query, indexing, or persistence logic changes.
  5. It directly outputs terminal text (`System.out.println`), mixing console I/O into a backend business service.
* **Architectural Risk:** Any tweak to warranties or promotions risks introducing regressions into core sale recording and inventory deductions.
* **Proposed Solution for Phase 2:**
  - Delegate warranty coordination to `WarrantyService` completely rather than computing costs inside `SaleService`.
  - Remove direct console printing (`System.out.println`) from `SaleService`; return a status or throw handled domain exceptions.

---

#### Violation V-S05: Return Business Rule Embedded in `Sale` Entity
* **Affected Class:** `com.gamezone.model.Sale`
* **Evidence:** Lines 159–166:
  ```java
  public boolean canBeReturned() {
      if (date == null) {
          return false;
      }
      LocalDate today = LocalDate.now();
      LocalDate limitDate = date.plusDays(30);
      return !today.isAfter(limitDate);
  }
  ```
* **Explanation:** A `Sale` object should be an immutable record of a past commercial transaction. Deciding whether that sale is currently eligible for return involves business policies (the 30-day limit) and non-deterministic environment state (`LocalDate.now()`).
* **Consequence:** If GameZone decides to adjust return windows (e.g., 15 days for opened electronics, 60 days during holiday seasons, or 0 days for digital goods), the core `Sale` domain class must be modified and recompiled.
* **Proposed Solution:** Move the return validity check into `ReturnService` or a dedicated `ReturnPolicy` validator where it belongs.

---

### 2.2 Open/Closed Principle (OCP)

#### Violation V-S03: Rigid Stock Delegation Signature
* **Affected Class:** `com.gamezone.service.SaleService` and `com.gamezone.model.Product`
* **Evidence:** `SaleService.java:155` and `Product.java:136-142`:
  ```java
  // Product.java:136
  public void updateStock(com.gamezone.service.ProductService productService,
                          com.gamezone.service.AccessoryService accessoryService,
                          int newQuantity) {
      if (productService != null) {
          productService.updateStock(getId(), newQuantity);
      }
  }
  ```
* **Explanation:** In the GRASP refactoring, we eliminated `if (product instanceof Accessory)` by delegating to `product.updateStock(...)`. While this achieved polymorphism, the method signature is tightly coupled to exactly two concrete service types: `ProductService` and `AccessoryService`.
* **Consequence:** If we add a new product category that requires its own inventory storage (such as digital licenses managed by a `DigitalService` or store merchandise), the method signature in `Product` must be changed to receive a third service, breaking `Product`, all subclasses, and `SaleService`. It is not closed for modification.
* **Proposed Solution:** Introduce an inventory dispatch abstraction or pass a unified stock handler so that `Product` and `SaleService` do not couple to every concrete inventory service in the system.

---

### 2.3 Liskov Substitution Principle (LSP)

#### Violation V-S04: Bypassing Subtype Invariants in `ProductService.restoreStock`
* **Affected Class:** `com.gamezone.service.ProductService`
* **Evidence:** Lines 105–119:
  ```java
  public void restoreStock(String productId, int quantity) {
      if (quantity <= 0) {
          return;
      }
      List<Product> products = persistence.loadAll();
      for (Product product : products) {
          if (product.getId().equals(productId)) {
              product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
              break;
          }
      }
      persistence.save(products);
  }
  ```
* **Explanation:** In `Product.java:148`, the class provides an encapsulated domain method:
  ```java
  public void increaseStock(int quantity) {
      if (quantity > 0) {
          this.availableQuantity += quantity;
      }
  }
  ```
  However, `ProductService.restoreStock` ignores `increaseStock` and directly invokes `product.setAvailableQuantity(...)`.
* **Consequence:** If a subtype of `Product` (such as `ExampleDigitalGame`) overrides `increaseStock()` to reject inventory restoration or trigger license reactivation, `ProductService` completely bypasses that logic, breaking the behavioral contract of the subtype and violating LSP.
* **Proposed Solution:** Refactor line 113 to invoke `product.increaseStock(quantity);`.

---

### 2.4 Interface Segregation Principle (ISP)

#### Observation: Fat Interface of `SaleService` for Ancillary Services
* **Evidence:** `SaleService.java` public API.
* **Context:** `ReturnService` and `WarrantyService` only require a read-only lookup: `findSaleById(String saleId)` or `getSaleById(String saleId)`. However, because `SaleService` combines sale creation, multi-unit stock decrements, warranty assignment, and sales queries into one class, caller modules are forced to depend on a fat class with capabilities they never use.
* **Proposed Solution:** Keep read operations distinct from transaction execution, or ensure callers only interact with focused query methods.

---

### 2.5 Dependency Inversion Principle (DIP)

#### Violation V-S02: Concrete Coupling and Hardcoded Scanner in `ConsoleUI`
* **Affected Class:** `com.gamezone.ui.ConsoleUI`
* **Evidence:** Lines 35–46:
  ```java
  public ConsoleUI(PersonService personService, ProductService productService, SaleService saleService,
                    AccessoryService accessoryService, PromotionService promotionService,
                    ReturnService returnService, WarrantyService warrantyService) {
      this.personService = personService;
      this.productService = productService;
      this.saleService = saleService;
      this.accessoryService = accessoryService;
      this.promotionService = promotionService;
      this.returnService = returnService;
      this.warrantyService = warrantyService;
      this.scanner = new Scanner(System.in);
  }
  ```
* **Explanation:**
  1. The presentation layer depends directly on 7 concrete service implementations rather than abstractions.
  2. The constructor directly instantiates `new Scanner(System.in)`. Because standard input is hardcoded, it is impossible to supply an alternate input stream (e.g., automated test scripts or simulation files) without modifying the source code.
* **Proposed Solution:**
  - Overload the constructor to allow injecting `Scanner` or an `InputStream`.
  - Decompose `ConsoleUI` into dedicated sub-menu controllers to reduce direct service coupling.

---

## 3. Reflection on GRASP Refactoring Components

### `ReceiptFormatter.java`
* **Evaluation:** Extracting `ReceiptFormatter` was a major architectural win for GRASP High Cohesion and SRP. `Sale.java` is now purely a transaction domain model.
* **SOLID Perspective:**
  - **SRP:** Compliant. It only formats sales into strings.
  - **OCP:** Minor limitation. The formatting template is hardcoded to Spanish console output. If alternative export formats are required (e.g., ticket printer ESC/POS or HTML receipt), a new method or strategy is needed. Given our console-only project scope, this pragmatic design is fully justified.

---

## 4. Leader's Action Plan for Phase 2 (Refactoring)
1. **Fix V-S04:** Update `ProductService.restoreStock` to use `product.increaseStock(quantity)`.
2. **Fix V-S01:** Remove `System.out.println` from `SaleService` and decouple warranty pricing math.
3. **Fix V-S05:** Delegate return window evaluation from `Sale` to the returns module.
4. **Fix V-S02:** Allow injecting `Scanner` in `ConsoleUI` to respect Dependency Inversion.
