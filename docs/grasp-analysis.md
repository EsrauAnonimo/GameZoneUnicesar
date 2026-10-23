# GRASP Architectural Analysis — Team Evaluation & Synthesis

**Project:** GameZone Unicesar  
**Audited Target:** `develop` branch (commit `916d492` / working branch `feature/grasp-audit`)  
**Auditing Team:**
* Esteban Raúl Vergara Jaime (Technical Leader)
* Luis Eduardo Gómez Rosales (Developer 1)
* Cristian Andrés Pérez González (Developer 2)

---

## 1. Criterion for Identifying GRASP Violations

### 1.1 Architectural Definition of a Violation
Our team established a rigorous, objective criterion grounded in responsibility-driven design (RDD) to determine whether a given software construct represents an authentic GRASP violation or an acceptable pragmatic design decision:

1. **Misplaced Responsibility (Information Expert & High Cohesion):** A violation occurs when a class executes business computations or manipulates data for which it does not possess the primary information, leaving the true information owner as an anemic data container. Similarly, when a single class takes on disparate concerns (such as domain modeling combined with terminal layout formatting or monolithic multi-domain user interface routing), it is flagged as a High Cohesion violation.
2. **Type Inspection over Polymorphic Dispatch (Polymorphism & Protected Variations):** Code that utilizes runtime type inspection (`instanceof` conditional branches or type-based cascades) to vary behavior based on object categories is classified as a severe violation of Polymorphism and Protected Variations, directly breaching the Open/Closed Principle.
3. **Inappropriate Layering and Coupling (Low Coupling & Indirection):** When high-level or cross-cutting classes unnecessarily depend on concrete leaf subclasses, when a service is burdened with an excessive number of disparate domain services, or when lower-layer persistence components invert architecture by depending upwards on domain services, a Low Coupling violation is recorded.
4. **Legitimate Pragmatic Patterns vs. Antipatterns:** We distinguish deliberate Pure Fabrications (such as file I/O repositories `SalePersistence`, `ProductPersistence` and dedicated syntactic validators `SaleValidator`, `PersonValidator`) from design smells. Artificial classes created explicitly to maintain domain purity and cohesively isolate infrastructure concerns are recognized as compliant GRASP Pure Fabrications.

### 1.2 Mandatory Evidence Standard
Every violation documented in our audit reports was required to satisfy the following evidentiary standard:
* **Target Class and Method:** Complete naming of the affected class and method.
* **Exact Line References:** Specific line numbers verified against commit `916d492` on the `develop` branch.
* **Direct Code Snippet:** Extraction of the offending code construct.
* **GRASP Principle and Justification:** Theoretical explanation detailing exactly why the code violates the specified pattern and the architectural risk it introduces.
* **Proposed Refactoring:** Concrete, actionable solution for Phase 2 that eliminates the smell without introducing prohibited language features (such as lambdas, streams, or unauthorized reflection).

---

## 2. Boundary Findings: Cross-Module Interactions

### 2.1 The Sales-Inventory Boundary (`SaleService`, `ProductService`, and `AccessoryService`)
The most prominent cross-module architectural friction discovered during the audit resides at the boundary between the **Sales module** (Technical Leader scope), the **Product module** (Developer 1 scope), and the **Accessory module** (Developer 2 scope).

#### The Problematic Construct:
In `SaleService.java` (lines 143–149), during the completion of a sale transaction (`registerSale`), the service updates item stock:
```java
int newQuantity = product.getAvailableQuantity() - count;
if (product instanceof Accessory) {
    accessoryService.updateStock(id, newQuantity);
} else {
    productService.updateStock(id, newQuantity);
}
```

Furthermore, `SaleService`'s constructor (lines 49–57) requires direct dependencies on both inventory services:
```java
public SaleService(SalePersistence persistence, ProductService productService,
                   AccessoryService accessoryService, PromotionService promotionService,
                   WarrantyService warrantyService) {
    this.persistence = persistence;
    this.productService = productService;
    this.accessoryService = accessoryService;
    this.promotionService = promotionService;
    this.warrantyService = warrantyService;
}
```

#### Cross-Module Root Cause Analysis:
1. **Conceptual Polymorphism vs. Physical Fragmentation:** In the domain model, `Accessory` is a specialization of `Product` (`public class Accessory extends Product`). Consequently, a sale treats items uniformly as `List<Product>`. However, at the persistence and service level, Developer 1 manages regular products (`VideoGame`, `Console`) through `ProductService` / `ProductPersistence`, while Developer 2 manages accessories through `AccessoryService` / `AccessoryRepository`.
2. **Leaking Subtype Knowledge Across Module Boundaries:** Because inventory persistence was separated across two services, `SaleService` is forced to inspect the runtime type (`instanceof Accessory`) to determine which service must execute the update. This breaks module encapsulation: `SaleService` should not need to know that accessories and regular products reside in disparate storage backends.
3. **Violations Generated:**
   * **Low Coupling (V-L01):** `SaleService` is tightly coupled to concrete subclass `Accessory` and two separate inventory services.
   * **Polymorphism (V-L04):** Replaces polymorphic dispatch with manual type branching.
   * **Protected Variations (V-L06):** Adding a new product category with specialized storage (e.g., `DigitalGame` or `Merchandise`) forces modifications to `SaleService`.

### 2.2 Stock Restoration and Returns Boundary
A secondary boundary friction occurs between `ReturnService` / `ReturnRepository` (Developer 2) and `SaleService` / `ProductService` (Tech Lead & Developer 1):
* In `ReturnRepository.java` (lines 37–56), the CSV persistence class explicitly injects `SaleService`, `ProductService`, and `AccessoryService` to resolve referenced entities when loading returns from disk. This creates an inverted boundary dependency where a storage class depends on high-level business services.
* In `ProductService.java` (lines 104–118), `restoreStock` directly mutates `product.setAvailableQuantity(...)` rather than delegating stock adjustment to an encapsulated method within `Product`.

#### Proposed Architectural Boundary Resolution:
Introduce a unified inventory coordination interface or delegate method (`StockManager` or unified catalog dispatch) so that `SaleService` and `ReturnService` communicate with a single entry point for inventory updates without inspecting concrete item subtypes.

---

## 3. Overlapping Patterns and Root-Cause Clustering

During our analysis, we identified that individual code smells frequently triggered simultaneous violations across multiple GRASP patterns. Rather than representing isolated defects, these violations stem from common architectural root causes.

### Cluster A: Type Inspection Conditionals (`instanceof`)
* **Affected Sites:**
  * `CategoryDiscount.java` (lines 106–110): `if (product instanceof VideoGame) ... else if (product instanceof Console) ... else if (product instanceof Accessory)`.
  * `SaleService.java` (lines 144–148, 175–177, 196–198): `if (product instanceof Accessory)` for stock dispatching, and `if (product instanceof Console)` for warranty eligibility.
* **Overlapping Violations:**
  1. **Polymorphism (V-04, V-L04):** The code relies on conditional statements to handle variations in object types instead of assigning polymorphic operations to the class hierarchy.
  2. **Low Coupling (V-02, V-L01):** The inspecting classes become tightly coupled to every concrete leaf subclass in the hierarchy.
  3. **Protected Variations (V-05, V-L06):** The design fails to protect callers from future variations; adding a new product subtype inevitably breaks existing calculation and dispatching methods.
* **Resolution Impact:** Guiding the refactoring through **Polymorphism** (adding abstract methods like `getCategory()` and `isWarrantyEligible()` to `Product`) immediately cures Low Coupling and Protected Variations at all affected sites.

### Cluster B: Console Presentation in Domain Entities
* **Affected Sites:**
  * `Sale.java` (lines 158–195): `generateReceipt()`.
  * `Warranty.java` (line 103): `generateWarrantyCertificate()`.
  * `Return.java` (line 164): `generateReturnReceipt()`.
* **Overlapping Violations:**
  1. **High Cohesion (V-03, V-L02):** Core business entities take on UI formatting responsibilities, mixing persistence-bound domain data with terminal escape sequences, decorative banners, and string column widths.
  2. **Pure Fabrication:** The system lacks dedicated presentation abstractions responsible for string rendering.
* **Resolution Impact:** Applying **Pure Fabrication** by creating specialized formatter classes (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`) extracts presentation concerns, immediately restoring **High Cohesion** to the domain models.

### Cluster C: Anemic Domain Logic and Setter-Based State Mutation
* **Affected Sites:**
  * `SaleService.java` (lines 122–126, 175–177) calculating discounts and warranties and modifying `Sale` via setters (`setTotal`, `setDiscountAmount`).
  * `ProductService.java` (line 112) altering stock via `setAvailableQuantity`.
* **Overlapping Violations:**
  1. **Information Expert (V-01, V-L05):** The class possessing the private state is bypassed; services perform business calculations externally.
  2. **High Cohesion:** Services are burdened with domain arithmetic that naturally belongs inside entities.
* **Resolution Impact:** Applying **Information Expert** by moving calculation methods into `Sale` and `Product` encapsulates mutable state and simplifies service orchestration.

---

## 4. Strategy for Non-Regression

Refactoring core patterns across an integrated codebase carries significant risk of regression. To guarantee that Phase 2 refactoring maintains functional parity and zero regression in console behavior, the team established a strict multi-tier verification strategy.

### 4.1 Baseline Test Scenarios
Prior to making any code modifications, the team executed and recorded terminal output logs for the five fundamental end-to-end flows:

1. **Scenario 1 — Catalog and Inventory Management:**
   * Adding VideoGames, Consoles, and Accessories.
   * Querying lists and validating stock levels.
2. **Scenario 2 — Complex Sale Registration:**
   * Registering a multi-unit sale containing mixed items (Consoles, VideoGames, and Accessories).
   * Verifying automatic promotion selection (`CategoryDiscount`, `PercentageDiscount`, or `BulkPurchaseDiscount`).
   * Applying extended warranty (+10% on console price) and confirming accurate subtotal, discount, warranty, and final total math.
3. **Scenario 3 — Receipt Verification:**
   * Validating that the printed receipt matches expected terminal layout, itemized lines, and financial summaries.
4. **Scenario 4 — Return and Warranty Lifecycle:**
   * Processing a return within the 30-day limit (`Sale.canBeReturned`).
   * Verifying that returned items correctly increment inventory in both `ProductService` and `AccessoryService`.
   * Verifying return rejection when the 30-day threshold is exceeded.
5. **Scenario 5 — Differentiated Exception Handling:**
   * Triggering and verifying exact Spanish error messages for `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, and `PersistenceException` in `ConsoleUI`.

### 4.2 Non-Regression Execution Workflow
* **Deterministic Input Scripts:** Using pre-recorded CLI input scripts to feed identical inputs to `ConsoleUI` before and after each refactoring step.
* **Diff Verification:** Performing text diffs between the pre-refactor console output logs and post-refactor console output logs to guarantee identical user-facing behavior.
* **Continuous Compilation Validation:** Every intermediate change must compile cleanly under Java 17 (`javac`) with zero warnings and no external library dependencies.

---

## 5. Refactoring Prioritization & Correction Order

To avoid merge conflicts, circular dependencies, and broken intermediate states, the team will execute the refactoring in an inside-out, bottom-up dependency sequence across four distinct stages:

```
[Phase 2.1: Domain Models] ---> [Phase 2.2: Pure Fabrications] ---> [Phase 2.3: Services & Repos] ---> [Phase 2.4: Presentation UI]
(Product, Sale, Warranty)       (Receipt & View Formatters)          (SaleService, Inventory)            (ConsoleUI Sub-handlers)
```

### 5.1 Phase 2.1: Domain Layer (Leaf Entities & Abstractions)
* **Target Classes:** `Product`, `VideoGame`, `Console`, `Accessory`, `Sale`, `Warranty`, `Return`.
* **Actions:**
  1. Add abstract polymorphic methods in `Product`: `getCategory()` returning the product domain category, and `isWarrantyEligible()`. Override them cleanly in `VideoGame`, `Console`, and `Accessory`.
  2. Implement `Product.increaseStock(int quantity)` and `Product.decreaseStock(int quantity)` to encapsulate stock mutations and eliminate public raw setters.
  3. Enrich `Sale` with domain calculation methods: `applyDiscount(double amount, String promoName)` and `addExtendedWarranty(double cost)`. Remove public setters for `total` and `discountAmount`.
  4. Refactor `Sale.canBeReturned(LocalDate currentDate)` to accept the evaluation date as a parameter.
* **Justification:** Domain models are leaf dependencies. Enhancing their interface first allows upstream services and formatters to utilize them immediately without altering existing caller signatures.

### 5.2 Phase 2.2: Pure Fabrications & Formatter Layer
* **Target Classes:** New formatters in presentation/service packages (`SaleReceiptFormatter`, `WarrantyCertificateFormatter`, `ReturnReceiptFormatter`).
* **Actions:**
  1. Extract console rendering logic from `Sale.generateReceipt()`, `Warranty.generateWarrantyCertificate()`, and `Return.generateReturnReceipt()` into these dedicated formatters.
  2. Update domain entities to strictly expose business getters.
* **Justification:** Decouples presentation formatting from domain entities, enabling cleaner service and UI orchestration.

### 5.3 Phase 2.3: Service Layer and Cross-Module Coordination
* **Target Classes:** `CategoryDiscount`, `SaleService`, `ProductService`, `ReturnService`, `ReturnRepository`.
* **Actions:**
  1. Replace `instanceof` cascades in `CategoryDiscount.calculateDiscount` with polymorphic calls to `product.getCategory()`.
  2. Refactor `SaleService.registerSale` to eliminate `instanceof Accessory` by coordinating inventory updates through a unified stock delegation contract or method.
  3. Eliminate `instanceof Console` checks in `SaleService` by calling `product.isWarrantyEligible()`.
  4. Decouple `ReturnRepository` by removing direct dependencies on domain services (`SaleService`, `ProductService`, `AccessoryService`), passing pre-loaded domain data or lookup delegates.
* **Justification:** With domain polymorphism and formatters in place, services can be cleanly refactored to achieve low coupling and high cohesion.

### 5.4 Phase 2.4: Presentation Layer (UI Decomposition)
* **Target Classes:** `ConsoleUI.java` and new menu handlers (`SaleMenuHandler`, `ProductMenuHandler`, `PersonMenuHandler`, `ReturnMenuHandler`).
* **Actions:**
  1. Split the 1100-line `ConsoleUI` God Class into dedicated sub-controllers that handle Scanner input and error handling for their specific subsystem.
  2. Maintain `ConsoleUI` as a clean routing facade that displays the main menu and delegates to sub-handlers.
* **Justification:** Completed last because UI handlers depend on the finalized signatures of services and formatters.

---

## 6. Summary of Team Alignment

The team has reached full consensus on all audited findings:
1. Technical Leader findings (V-L01 to V-L06) and Developer 1 findings (V-01 to V-05) are thoroughly integrated.
2. Developer 2's completed modules (`Person`, `Accessory`, `Promotion`, and `ReturnRepository`) have been merged, and dedicated consolidation placeholders have been reserved for his final batch.
3. The boundary friction between `SaleService`, `ProductService`, and `AccessoryService` is clearly diagnosed with an agreed-upon polymorphic resolution plan.
4. The sequential correction order guarantees independent, conflict-free refactoring during Phase 2.