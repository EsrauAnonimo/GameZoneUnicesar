# SOLID Architectural Analysis — Technical Leader Report

**Project:** GameZone Unicesar  
**Audited Target:** `develop` branch (with Requirement 7 GRASP corrections applied)  
**Author:** Esteban Raúl Vergara Jaime (Technical Leader)  
**Scope:** Architecture, Sales Module, Presentation Layer, and Cross-Cutting Boundaries  

---

## Overview

As Technical Leader of GameZone Unicesar, this document answers the five guiding architectural questions regarding the application of the SOLID principles (SRP, OCP, LSP, ISP, DIP) to our codebase.

Our context is practical:
- We are building a console-based video game store management system in Java 17.
- The GRASP refactoring (Requirement 7) has already been merged into `develop`.
- We work under strict academic constraints: **no external frameworks**, **no new interfaces**, **no Streams**, and **no Optional**. All design patterns and decoupling must be achieved through clean object-oriented inheritance, abstract classes, constructor injection, and single-purpose classes.

---

## Question 1: Single Responsibility Principle (SRP)

> *How does the Single Responsibility Principle define a "reason to change"? How does SRP apply to our core classes (`SaleService`, `ConsoleUI`, `ReceiptFormatter`, and `Sale`), and why is separating business rules, persistence, and presentation vital for long-term maintainability?*

### 1.1 The Definition of a "Reason to Change"
In practical terms, a "reason to change" is not just about a class doing more than one thing; it is about who or what drives modifications to that class. As Robert C. Martin explains, a responsibility is tied to an actor or stakeholder (e.g., the accounting team, the inventory manager, the terminal UI designer). If a class has to change when the inventory rules change AND ALSO when the receipt printing format changes, it has two completely different actors and therefore multiple responsibilities.

### 1.2 Application in GameZone Unicesar

1. **`Sale.java` (Domain Entity):**
   - *Previous state:* Prior to GRASP refactoring, `Sale` contained `generateReceipt()`, which built terminal strings with dashed lines and currency formatting.
   - *Current state:* We extracted that logic into `ReceiptFormatter`. Now, `Sale` only holds transaction data (`customer`, `seller`, `products`, `total`, `discountAmount`).
   - *Remaining SRP Smell:* `Sale` still contains `canBeReturned()` (lines 159–166). Deciding whether a transaction is returnable based on a 30-day window belongs to the Returns domain / policy, not to the immutable sale record. If the return policy changes to 15 days, `Sale` must be modified.

2. **`ReceiptFormatter.java` (Pure Fabrication / Presentation):**
   - *SRP Compliance:* Excellent. It has one single reason to change: when the visual presentation, headers, or formatting of receipts on the console changes. Domain logic does not touch this class.

3. **`SaleService.java` (Business Service):**
   - *SRP Violation:* `SaleService` currently has too many reasons to change:
     - When multi-unit stock validation rules change (`ProductValidator.validateStockAvailability`).
     - When promotion resolution, discount capping, or promotion warning logging changes (`applyBestPromotion`, line 309).
     - When console warranty rules (basic vs. extended) change (`calculateWarrantyCost` and `assignWarranties`).
     - When sale retrieval and customer/seller query logic changes.
     - When terminal output is triggered directly inside the service (`System.out.println` on line 309).
   - *Takeaway:* A service should orchestrate, but here `SaleService` acts as an all-knowing coordinator that also formats money, logs warnings to standard output, and executes warranty policies.

4. **`ConsoleUI.java` (Presentation Controller):**
   - *SRP Violation (God Class):* At over 1100 lines, `ConsoleUI` manages user prompts, parses numbers from `Scanner`, catches exceptions across 7 distinct business domains, and coordinates business workflows. A change to any single domain's console prompts forces edits on this monolithic class.

### 1.3 Why Separation Matters
Separating presentation, domain, and persistence ensures that changes remain localized. Modifying a receipt layout will never break total calculations, and updating how items are serialized to `sales.dat` will never affect the console menu.

---

## Question 2: Open/Closed Principle (OCP)

> *What does it mean for a software entity to be open for extension but closed for modification in an environment where creating new interfaces is prohibited? How did our GRASP polymorphic adjustments help, and where does OCP still break?*

### 2.1 OCP Without New Java Interfaces
The Open/Closed Principle states that software entities (classes, modules, functions) should be open for extension, but closed for modification. Many developers mistakenly believe that OCP can only be achieved with Java `interface`. That is incorrect.

In our system, where new interfaces are prohibited, OCP is achieved through:
1. **Abstract Base Classes:** `Product` and `Promotion` define abstract contracts (`getDescription()`, `calculateDiscount(Sale)`). When we added `BulkPurchaseDiscount` or `ExampleDigitalGame`, we did not touch `Promotion.java` or `Product.java`. We simply extended them.
2. **Polymorphic Hooks (Default Methods in Abstract Classes):** In Requirement 7, we added `isWarrantyEligible()`, `getWarrantyCost()`, and `getCategory()` to `Product`. Subclasses override them when needed without altering existing classes.

### 2.2 Where GRASP Solved OCP
Before Requirement 7, `CategoryDiscount.java` inspected object types using `instanceof VideoGame` and `instanceof Console`. Adding accessories broke `CategoryDiscount`. By introducing `getCategory()` polymorphically on `Product`, `CategoryDiscount` is now closed for modification: any new product type just returns its category string.

### 2.3 Where OCP is Still Violated

1. **`SaleService.registerSale` Stock Delegation (Line 155):**
   ```java
   product.updateStock(productService, accessoryService, newQuantity);
   ```
   Even though `updateStock` is polymorphic, its method signature hardcodes two concrete services: `ProductService` and `AccessoryService`. If tomorrow we add a `DigitalGameService` or `MerchandiseService` with separate inventory storage, we have to reopen and modify `Product.java`, all its subclasses, and `SaleService.java`.

2. **`SaleService.assignWarranties` (Lines 211–215):**
   ```java
   if (wantsExtendedWarranty(product, productIdsWithExtendedWarranty)) {
       warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
   } else {
       warrantyService.assignBasicWarranty(product, sale, sale.getDate());
   }
   ```
   If a third warranty tier is introduced (e.g., "Accidental Damage Warranty"), `SaleService` must be modified with more conditional branches.

3. **`ReceiptFormatter.java` (Line 25):**
   `ReceiptFormatter` is a static utility that outputs hardcoded console text. If we need to print HTML receipts or export to JSON, we cannot extend it; we must rewrite it or add new methods directly.

---

## Question 3: Liskov Substitution Principle (LSP)

> *How does the Liskov Substitution Principle guarantee behavioral subtyping beyond signature matching? How do our inheritance hierarchies (`Product`, `Promotion`, `Person`) hold up, and what subtle LSP hazards exist?*

### 3.1 Definition of LSP
Liskov Substitution states that if $S$ is a subtype of $T$, then objects of type $T$ may be replaced with objects of type $S$ without altering any of the desirable properties of the program (correctness, task performed, etc.). In simple words: a subclass must not only match method names and types; it must honor the expectations and behavioral contracts set by the parent.

### 3.2 Evaluation of GameZone Hierarchies

1. **The `Product` Hierarchy (`Product` $\rightarrow$ `Console`, `VideoGame`, `Accessory` $\rightarrow$ `Controller`, `Cable`, `Memory`):**
   - *Good Substitution:* `Sale` handles items uniformly as `List<Product>`. Calculating the subtotal (`product.getPrice()`) works seamlessly for every subtype.
   - *Subtle LSP Hazard (Warranty and Stock):* In the base `Product` class, `isWarrantyEligible()` defaults to `false` and `getWarrantyCost()` defaults to `0.0`. `Console` overrides them to return `true` and `price * 0.10`. This is clean because clients calling `product.isWarrantyEligible()` get a safe, non-breaking response.
   - *Stock Invariant Hazard in `ProductService.restoreStock`:*
     In `ProductService.java` (line 113):
     ```java
     product.setAvailableQuantity(product.getAvailableQuantity() + quantity);
     ```
     `Product` defines a domain method `increaseStock(int quantity)` (added in GRASP refactor). However, `ProductService.restoreStock` bypasses this method and mutates raw quantity via `setAvailableQuantity`. If a subtype (like `ExampleDigitalGame`) overrides `increaseStock` to enforce that digital goods cannot have returned physical stock, `ProductService` breaks the subtype's behavior by directly mutating the field.

2. **The `Promotion` Hierarchy (`Promotion` $\rightarrow$ `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`):**
   - *Contract:* `public abstract double calculateDiscount(Sale sale)`.
   - *Preconditions and Postconditions:* All subclasses accept any valid `Sale` and return a non-negative `double`. None throw unexpected checked exceptions or corrupt the sale. This is a model implementation of LSP.

3. **The `Person` Hierarchy (`Person` $\rightarrow$ `Customer`, `Seller`):**
   - Both subclasses fulfill the base contract `getRoleDescription()`. No method throws `UnsupportedOperationException`.

---

## Question 4: Interface Segregation Principle (ISP)

> *Given that the project strictly prohibits creating new Java interfaces, how can the Interface Segregation Principle be applied to abstract classes, class APIs, and client dependencies?*

### 4.1 Understanding ISP Without `interface`
The Interface Segregation Principle states: *"Clients should not be forced to depend upon interfaces that they do not use."*

While ISP is traditionally demonstrated with Java `interface` constructs (`public interface Readable`), the architectural concept applies to **any exposed contract**:
- The public methods of a class form its "interface" to the rest of the system.
- An abstract class is a contract.
- A service with 20 public methods exposes a "fat interface."

When clients depend on a class that exposes far more than what those clients need, they become unnecessarily coupled to changes in unrelated parts of that class.

### 4.2 ISP Violations in GameZone

1. **`SaleService` as a Fat Interface for Other Services:**
   - Both `ReturnService` and `WarrantyService` need access to sales data. Specifically, they only need a lookup operation: `findSaleById(String saleId)`.
   - However, because `SaleService` does not segregate read operations from write/transaction operations, `ReturnService` depends on a class that exposes:
     - `registerSale(...)`
     - Stock update delegations
     - Warranty granting
     - Customer and seller query filters
   - If `SaleService` changes how it registers sales or updates stock, callers that only wanted to read a sale are exposed to the risk of recompilation and regression.

2. **Monolithic `ConsoleUI`:**
   - `ConsoleUI` has no segregation. It mixes 7 business domains into one 1100-line concrete class. A menu handler that only needs to display products is bundled together with the return balance math and seller registration.

3. **Anemic Setters on `Sale.java`:**
   - `Sale.java` exposes public setters: `setTotal(double)`, `setDiscountAmount(double)`, `setExtendedWarrantyCost(double)`, and `setAppliedPromotionName(String)`.
   - Any external class can arbitrarily rewrite the financial results of a sale without recomputing products. Clients of `Sale` are given write access to fields they should only read.

---

## Question 5: Dependency Inversion Principle (DIP)

> *What is Dependency Inversion, and where is it violated in GameZone? How does manual constructor injection help, and what are the architectural boundaries between high-level policy and low-level details?*

### 5.1 Definition of DIP
The Dependency Inversion Principle states:
1. *High-level modules should not depend on low-level modules. Both should depend on abstractions.*
2. *Abstractions should not depend on details. Details should depend on abstractions.*

- **High-level modules:** The business logic (e.g., `SaleService`, `ProductService`, `ReturnService`). They define what the store actually does (rules, discounts, transactions).
- **Low-level modules:** The infrastructure and technical mechanisms (e.g., `SalePersistence` writing bytes to `data/sales.dat`, `Scanner` reading characters from `System.in`).

### 5.2 DIP Violations in GameZone

1. **`ProductService` Instantiates its Own Persistence (Line 23):**
   ```java
   public class ProductService {
       private ProductPersistence persistence;
       public ProductService() {
           this.persistence = new ProductPersistence(); // Hardcoded DIP violation!
       }
   ```
   High-level business service `ProductService` is tightly coupled to concrete class `ProductPersistence`. It is impossible to test `ProductService` in memory without touching `data/products.dat`.

2. **`SalePersistence` and File Path Hardcoding (Line 11):**
   ```java
   private static final String FILE_PATH = "data/sales.dat";
   ```
   `SalePersistence` does not allow configuring where files are written. The detail is hardcoded.

3. **`ConsoleUI` Depends on 7 Concrete Services and `System.in`:**
   ```java
   public ConsoleUI(PersonService personService, ProductService productService, SaleService saleService,
                    AccessoryService accessoryService, PromotionService promotionService,
                    ReturnService returnService, WarrantyService warrantyService) {
       ...
       this.scanner = new Scanner(System.in); // Hardcoded console stream!
   }
   ```
   `ConsoleUI` cannot be unit-tested because `new Scanner(System.in)` cannot be injected or redirected to a mock input string without modifying the class.

4. **`SaleService` Depends on 5 Concrete Service Implementations:**
   `SaleService` depends directly on concrete `ProductService`, `AccessoryService`, `PromotionService`, and `WarrantyService`.

### 5.3 Pragmatic DIP Under Project Constraints
While we cannot introduce `public interface ProductRepository` due to the rule prohibiting new interfaces, we can apply DIP principles pragmatically:
- **Consistent Constructor Injection:** Every class must receive its dependencies via constructor (as `PersonService` and `SaleService` do), completely eliminating calls to `new ConcreteService()` inside constructors.
- **Abstract Classes as Abstractions:** In Java, an `abstract class` provides the same architectural inversion as an `interface`. High-level services can depend on abstract repositories or abstract providers.
- **Composition Root (`Main.java`):** `Main` already acts as a manual Dependency Injection container. It instantiates the leaf repositories and wires them into services and UI, preserving the unidirectional architectural flow: `UI -> Service -> Persistence -> Model`.
