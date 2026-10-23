# Consolidated SOLID Audit Report — GameZone Unicesar

**Project:** GameZone Unicesar  
**Phase:** Requirement 8 — Phase 1 (SOLID Audit & Consolidation)  
**Branch:** `feature/solid-audit` (based on `develop` post GRASP refactoring)  
**Auditing Team:**
- Esteban Raúl Vergara Jaime (Technical Leader)
- Luis Eduardo Gómez Rosales (Developer 1 — Products)
- Cristian Andrés Pérez González (Developer 2 — People & Ancillary Modules, assumed by Technical Leader)

---

## 1. Audit Scope & Team Coverage Matrix

To ensure that no part of the system was overlooked, the team audited every class across all modules against the 5 SOLID principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion).

| Module / Component | Classes Included | Assigned Auditor | Individual Report Status |
|---|---|---|---|
| **Sales Module** | `Sale.java`, `SalePersistence.java`, `SaleService.java` | Esteban Vergara (Leader) | **Completed** (`leader-audit.md`) |
| **Presentation & Bootstrap** | `ConsoleUI.java`, `Main.java`, `ReceiptFormatter.java` | Esteban Vergara (Leader) | **Completed** (`leader-audit.md`) |
| **Products Module** | `Product.java`, `VideoGame.java`, `Console.java`, `ProductPersistence.java`, `ProductService.java` | Luis Gómez (Dev 1) | **Completed** (`developer1-audit.md`) |
| **Accessories Module** | `Accessory.java`, `Controller.java`, `Cable.java`, `Memory.java`, `AccessoryPersistence.java`, `AccessoryService.java` | Luis Gómez (Dev 1) / Cristian Pérez (Dev 2) | **Completed** (`developer1-audit.md` & `developer2-audit.md`) |
| **People Module** | `Person.java`, `Customer.java`, `Seller.java`, `PersonPersistence.java`, `PersonService.java` | Cristian Pérez (Dev 2) | **Completed** (`developer2-audit.md`) |
| **Returns Module** | `Return.java`, `ReturnRepository.java`, `ReturnService.java` | Cristian Pérez (Dev 2) | **Completed** (`developer2-audit.md`) |
| **Warranties Module** | `Warranty.java`, `BasicWarranty.java`, `ExtendedWarranty.java`, `WarrantyRepository.java`, `WarrantyService.java` | Cristian Pérez (Dev 2) | **Completed** (`developer2-audit.md`) |
| **Promotions Module** | `Promotion.java`, `PercentageDiscount.java`, `CategoryDiscount.java`, `BulkPurchaseDiscount.java`, `PromotionRepository.java`, `PromotionService.java` | Cristian Pérez (Dev 2) | **Completed** (`developer2-audit.md`) |

---

## 2. Unified SOLID Violation Matrix

The table below unifies all findings from the Technical Leader, Developer 1, and Developer 2 reports into a single, standardized matrix (V-01 through V-11):

| ID | Principle | Affected Class / Component | Line Numbers | Reporter | Severity | Summary of Problem |
|---|---|---|---|---|---|---|
| **V-01** | **SRP** | `SaleService.java` | `SaleService.java:99-165`, `281-312` | Leader | **High** | Multi-responsibility service: handles stock checks, warranty pricing, discount handling, currency rounding, and terminal console logs (`System.out.println`). |
| **V-02** | **DIP** | `ReturnRepository.java` | `ReturnRepository.java:7-9`, `37-39`, `51-56` | Dev 2 | **High** | Layer inversion: a low-level persistence repository directly injects 3 high-level business services (`SaleService`, `ProductService`, `AccessoryService`). |
| **V-03** | **DIP** | `ConsoleUI.java` | `ConsoleUI.java:35-46` | Leader | **High** | The monolithic UI is coupled to 7 concrete services and hardcodes `new Scanner(System.in)` in its constructor, preventing test automation. |
| **V-04** | **SRP** | `Return.java` | `Return.java:164-186` | Dev 2 | **Medium** | The domain entity `Return` builds a terminal-formatted string in Spanish (`generateReturnReceipt`), coupling presentation formatting to the domain. |
| **V-05** | **OCP** | `SaleService.java` & `Product.java` | `SaleService.java:155`, `Product.java:136-142` | Leader | **Medium** | `product.updateStock(productService, accessoryService, qty)` hardcodes two concrete services in its signature; adding a new category forces rewriting the signature. |
| **V-06** | **OCP** | `ReturnService.java` | `ReturnService.java:113-117`, `324-329` | Dev 2 | **Medium** | Uses `if (product instanceof Console)` to cancel warranties and `if (product instanceof Accessory)` to restore stock, breaking open/closed behavior. |
| **V-07** | **LSP / OCP** | `ProductService.java` (`restoreStock`) | `ProductService.java:105-119` | Leader | **Medium** | Directly alters `product.setAvailableQuantity(...)` rather than delegating to `product.increaseStock(quantity)`, bypassing domain encapsulation and subtype rules. |
| **V-08** | **SRP** | `ReturnService.java` | `ReturnService.java:182-260` | Dev 2 | **Medium** | Calculates monthly store sales revenue (`calculateMonthlySales`) and financial balances, which belongs to sales or reporting, not returns. |
| **V-09** | **SRP / DIP** | `ProductService.java` | `ProductService.java:20-24` | Dev 1 | **Medium** | Constructor directly instantiates `ProductPersistence` with `new`, coupling the service to concrete file storage and violating dependency inversion. |
| **V-10** | **OCP** | `ProductService.java` | `ProductService.java:26`, `40` | Dev 1 | **Medium** | Separate methods `registerVideoGame(VideoGame)` and `registerConsole(Console)`; adding a new product category forces editing `ProductService` instead of using a polymorphic `registerProduct(Product)`. |
| **V-11** | **SRP** | `Sale.java` | `Sale.java:159-166` | Leader | **Low** | Domain transaction entity embeds the 30-day return policy rule (`canBeReturned`), coupling transaction records to return rule changes. |

---

## 3. Boundary Findings (Hallazgos de Frontera)

The most critical architectural insights appear where the code written by the Technical Leader, Developer 1, and Developer 2 crosses module boundaries:

### 3.1 Hallazgo de Frontera 1: The Sales-Inventory-Accessory Boundary (`SaleService` $\times$ `Product` $\times$ `AccessoryService`)
* **Classes involved:** `SaleService.java:155` (Leader), `Product.java:136-142` (Dev 1), and `AccessoryService.java` (Dev 2).
* **Detailed analysis:**
  In Requirement 7 (GRASP), we eliminated `instanceof Accessory` by creating `product.updateStock(...)`. However, evaluating this across modules under SOLID reveals a cross-boundary **OCP violation (V-05)**:
  ```java
  public void updateStock(com.gamezone.service.ProductService productService,
                          com.gamezone.service.AccessoryService accessoryService,
                          int newQuantity)
  ```
  The base class `Product` (Developer 1's domain) is forced to know about `AccessoryService` (Developer 2's service), and `SaleService` (the Leader's module) is forced to pass both services on every stock update call.
  If tomorrow Developer 1 adds `DigitalGame` (with its own `DigitalService`), the method signature breaks across all three developers' files: `Product.java`, every product subclass, and `SaleService.java`.
* **Cross-module solution:** In Phase 2, we will decouple the inventory update contract so that `Product` and `SaleService` communicate through a unified inventory handler without concrete service references.

### 3.2 Hallazgo de Frontera 2: The Returns-to-Sales Inverted Layer Boundary (`ReturnRepository` $\times$ `SaleService` $\times$ `ProductService`)
* **Classes involved:** `ReturnRepository.java:7-9`, `37-39`, `51-56` (Dev 2), `SaleService.java` (Leader), and `ProductService.java` (Dev 1).
* **Detailed analysis:**
  Developer 2's persistence class (`ReturnRepository`) depends directly on the Technical Leader's service (`SaleService`) and Developer 1's services (`ProductService`, `AccessoryService`) to parse text lines from `data/returns.csv`.
  This is a severe boundary inversion (**V-02**): a low-level persistence class in `com.gamezone.persistence` is calling high-level business services in `com.gamezone.service`. It creates an architectural cycle: `ReturnService -> ReturnRepository -> SaleService`. If `SaleService` changes its constructor or error handling, file reading in `ReturnRepository` breaks.
* **Cross-module solution:** `ReturnRepository` must be stripped of all service dependencies. It should only read raw records from CSV (IDs, dates, refund amounts). Resolving those IDs into full `Sale` and `Product` instances must be handled in the service layer (`ReturnService`), completely freeing the persistence layer from service dependencies.

### 3.3 Hallazgo de Frontera 3: The Return-to-Inventory Encapsulation Boundary (`ReturnService` $\times$ `ProductService`)
* **Classes involved:** `ReturnService.java:328` (Dev 2) and `ProductService.java:113` (Dev 1).
* **Detailed analysis:**
  When a return is registered, Developer 2's `ReturnService` calls Developer 1's `ProductService.restoreStock(id, 1)`. Inside `restoreStock`, Developer 1 mutates the product with:
  ```java
  product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
  ```
  This completely bypasses `product.increaseStock(quantity)`, which Developer 1 specifically built in Requirement 7!
  This boundary friction violates the Liskov Substitution Principle (**V-07**). If a subtype of `Product` enforces that stock cannot be restored (for example, non-returnable digital codes or sealed software), `ProductService` silently overrules the subtype's invariant.
* **Cross-module solution:** Developer 1 must update `ProductService.restoreStock` to call `product.increaseStock(quantity)`.

---

## 4. Relationship with Requirement 7 (GRASP Refactoring)

Our GRASP refactoring (Requirement 7) was an essential milestone for the project, but analyzing it under SOLID reveals why this second audit phase is critical:

1. **What GRASP Accomplished:**
   - **High Cohesion:** Solved `V-L02` by extracting receipt formatting from `Sale.java` into `ReceiptFormatter.java`.
   - **Polymorphism:** Solved `V-L04` by removing `instanceof` cascades in `CategoryDiscount.java` and introducing `getCategory()` on `Product`.
   - **Encapsulation:** Added `increaseStock` to `Product` to reduce reliance on external setters.

2. **What Remained Incomplete (The SOLID Gaps):**
   - **Half-applied Solutions:** While `Sale.generateReceipt()` was extracted to `ReceiptFormatter`, `Return.generateReturnReceipt()` was completely forgotten inside `Return.java` (**V-04**).
   - **Remaining `instanceof`:** While `CategoryDiscount` was refactored with polymorphism, `ReturnService` still has `instanceof Console` and `instanceof Accessory` (**V-06**).
   - **Trade-offs that broke OCP:** The GRASP solution for stock update removed `instanceof` inside `SaleService`, but did so by passing concrete services into `product.updateStock(...)`, which violated the Open/Closed Principle (**V-05**).
   - **Unused Encapsulation:** Developer 1 added `Product.increaseStock(quantity)`, but `ProductService.restoreStock` was never updated to call it, leaving an LSP/encapsulation smell (**V-07**).

SOLID provides the precise, uncompromising criteria needed to close every loop left open by the GRASP refactor.

---

## 5. Team Refactoring Strategy for Phase 2

1. **Technical Leader (Esteban):**
   - Clean up `SaleService`: remove console logging (`System.out.println`) and decouple warranty calculation math (**V-01**).
   - Move `canBeReturned()` logic out of `Sale.java` into the returns domain (**V-11**).
   - Overload `ConsoleUI` to allow constructor injection of `Scanner` (**V-03**).

2. **Developer 1 (Luis):**
   - Inject `ProductPersistence` into `ProductService` constructor instead of calling `new` (**V-09**).
   - Replace separate registration methods in `ProductService` with a unified `registerProduct(Product)` (**V-10**).
   - Update `ProductService.restoreStock` to invoke `product.increaseStock(quantity)` (**V-07**).

3. **Developer 2 (Cristian / Leader):**
   - Move `Return.generateReturnReceipt()` into `ReceiptFormatter` (**V-04**).
   - Decouple `ReturnRepository` by removing `SaleService`, `ProductService`, and `AccessoryService` from its constructor (**V-02**).
   - Replace `instanceof Console` in `ReturnService` with `product.isWarrantyEligible()` (**V-06**).
   - Move monthly sales revenue calculation out of `ReturnService` (**V-08**).
