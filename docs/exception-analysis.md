# Exception Handling & Validation Architecture Analysis (Requirement 6)

Technical decisions and architectural rationale for introducing the validation and custom exception layer into GameZone Unicesar. This document answers the five core design questions defined in the project specification.

---

## 1. Should `GameZoneException` extend `Exception` (checked) or `RuntimeException` (unchecked)?

`GameZoneException` extends **`RuntimeException` (unchecked)**.

### Architectural Rationale:
1. **Separation of Concerns and Clean Contracts:**
   Checked exceptions force every intermediate method signature to declare `throws GameZoneException`. In our layered architecture (`ui -> service -> validation / persistence -> model`), services orchestrate operations across multiple repositories and validators. Polluting every service and persistence interface with checked exception declarations introduces high coupling and boilerplate throws clauses without adding safety.
2. **KISS Principle & Failure Propagation:**
   Validation errors (e.g., negative prices, empty titles), business rule violations (e.g., stock shortage), and not-found entities represent operational failures caused by user input or runtime conditions. These conditions must propagate cleanly across architectural layers without requiring intermediate methods to catch or re-declare them.
3. **Single Boundary Handling:**
   The presentation layer (`ConsoleUI`) acts as the single boundary responsible for catching domain exceptions and translating them into user-friendly Spanish messages. Using unchecked exceptions allows domain errors to bubble directly to `ConsoleUI` without intermediate clutter.
4. **Consequences on Method Signatures:**
   Method signatures across services and repositories remain clean and concise (`public void registerSale(...)` instead of `public void registerSale(...) throws GameZoneException`). This preserves backward compatibility, keeps the public API readable, and adheres strictly to the course constraint of avoiding unnecessary complexity.

---

## 2. Why is a hierarchy with a common root (`GameZoneException`) preferable to generic `IllegalArgumentException`?

Relying exclusively on `IllegalArgumentException` treats all failures identically: a missing database record, a negative sale price, an invalid email format, and a disk corruption error all look identical to caller code.

### Design Principles Applied:
- **Semantic Expressiveness:** Distinct exception types (`ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException`) explicitly state *what kind* of problem occurred.
- **Single Responsibility Principle (SRP):** Each exception class represents one single, well-defined category of failure:
  - `ResourceNotFoundException`: entity lookup failure by ID.
  - `BusinessRuleException`: domain constraint violation (e.g., attempting to sell more units than currently in stock).
  - `InvalidDataException`: input data fails format, range, or nullity rules.
  - `PersistenceException`: technical I/O error during file read/write operations.
- **Open/Closed Principle (OCP) & Polymorphism:** Because all specific exceptions inherit from `GameZoneException`, new exception types can be introduced without breaking high-level catching policies. The UI layer can catch specific subclasses for differentiated messages, while knowing that every domain exception adheres to the `GameZoneException` root.
- **Separation of Concerns:** Clearly distinguishes client-side data entry errors from domain business logic violations and low-level infrastructure failures.

---

## 3. Where are validators invoked, why is this coherent with layered architecture, and can they access persistence?

### Invocation Point:
Validators are invoked **at the very beginning of service methods**, before any business computation, state mutation, or persistence call occurs.

### Architectural Coherence:
In our layered architecture (`UI -> Service -> Persistence -> Model`), the new `com.gamezone.validation` package is a dedicated cross-cutting support layer positioned logically between `Service` and `Model`:
- The UI layer gathers user input and calls service methods.
- The service delegates raw validation to stateless validators (e.g., `ProductValidator.validateProductData(...)`).
- If validation passes, the service proceeds to update domain models and invoke persistence. This ensures that invalid data or illegal business operations never corrupt the domain model or reach the storage files (`products.dat`, `sales.dat`, etc.).

### Can Validators Access Persistence?
**NO, under no circumstances.**
- Validators must remain pure, stateless components that only inspect in-memory arguments passed to their static methods.
- Allowing validators to access `persistence` would violate the unidirectional dependency rule, create circular dependencies, and introduce hidden side-effects.
- When a validation rule requires checking against existing data (for instance, checking if a person ID is already registered), the **Service** queries the persistence layer, retrieves the list of existing entities, and passes that list into the validator (e.g., `PersonValidator.validateUniquePerson(existingList, id)`).

---

## 4. How does the team guarantee existing behavior is preserved across the 10 menu operations?

### Preservation Strategy:
1. **Equivalence of Validation Thresholds:**
   Every validation rule previously handled inline with `IllegalArgumentException` or manual `if (...)` guards is moved to the corresponding validator class with the exact same threshold conditions. No business rule is tightened or loosened; only the resulting exception type is upgraded to a typed domain exception.
2. **Stable Method Signatures:**
   All public service methods retain their existing parameters and return types. Because `GameZoneException` is an unchecked exception, caller signatures remain completely backwards-compatible.
3. **Differentiated Presentation Guard:**
   In `ConsoleUI`, the previous generic error checks are replaced with targeted catch blocks. For valid inputs, the execution flow and console output remain 100% identical.

### Demonstration Evidence:
- **Unit & Regression Verification:** Successful build execution via `mvn clean test` and `WarrantyReferenceCheck`.
- **End-to-End Console Walkthrough:** Running all 10 menu options with valid data to confirm identical outputs.
- **Negative Boundary Testing:** Exercising intentional failure scenarios (e.g., negative product price, non-existent customer ID, insufficient stock) to verify that clear, differentiated Spanish messages appear without application crashes or unhandled stack traces.

---

## 5. Why use multiple specific catch blocks instead of a single generic `catch (GameZoneException)` in the UI?

`ConsoleUI` implements **multiple specific catch blocks** (`catch (ResourceNotFoundException e)`, `catch (BusinessRuleException e)`, `catch (InvalidDataException e)`, `catch (PersistenceException e)`).

### Justification:
1. **Differentiated User Communication:**
   A single generic `catch (GameZoneException e)` would force either a generic message or error-prone `instanceof` checks inside the catch block. Using dedicated catch clauses allows the presentation layer to assign distinct Spanish prefixes to each failure category:
   - Resource not found: `"No se encontró el recurso solicitado: " + e.getMessage()`
   - Business rule: `"No se puede completar la operación: " + e.getMessage()`
   - Invalid data: `"Los datos ingresados no son válidos: " + e.getMessage()`
   - Persistence error: `"Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador."`
2. **KISS & Idiomatic Java:**
   Multiple catch blocks are the standard, clean, object-oriented way in Java to handle heterogeneous exception types. It provides explicit control flow without resorting to reflective inspections, casting, or nested conditionals.

---

## Coordination notes for the developers

- **Developer 1 (Exceptions & Validators):** Implements `GameZoneException` and the 4 subclasses in `com.gamezone.exceptions`, plus `ProductValidator`, `PersonValidator`, and `SaleValidator` in `com.gamezone.validation`.
- **Developer 2 (Services & Repositories):** Refactors `ProductService`, `PersonService`, and `SaleService` to invoke the validators, and wraps `IOException` in repositories with `PersistenceException`.
- **Technical Lead (UI & Docs):** Updates `ConsoleUI` with the 4 differentiated catch blocks, documents the architecture, updates `README.md`, and reviews all integration Pull Requests.
