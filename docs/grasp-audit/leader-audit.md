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

