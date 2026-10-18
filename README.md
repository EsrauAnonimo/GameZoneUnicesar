# GameZone Unicesar

Inventory and sales management system for a video game store, developed in Java with a layered architecture.

## Description

GameZone Unicesar is a console-based system that manages products (video games and consoles), accessories (controllers, cables, memories), people (customers and sellers), sales transactions, promotional discounts, console warranties, and product returns, with complete data persistence between executions.

The system represents the complete integration of four major business modules operating coherently over a unified layered architecture.

## Technologies

- **Language:** Java 17 (Pure Java: strictly no external frameworks, no interfaces, no `Stream`, no `Optional`)
- **Build tool:** Apache Maven
- **Persistence:**
  - Java object serialization (`.dat` binary files) for Products and Sales.
  - CSV text persistence (`.csv` files with semicolon `;` separator) for Sellers, Accessories, Promotions, Warranties, and Returns.

## Architecture

The project strictly follows a four-layer architecture within the `com.gamezone` package:

1. **UI Layer (`com.gamezone.ui`):** `ConsoleUI` manages console menus, user input validation, and result presentation in Spanish. It communicates exclusively with the Service layer.
2. **Service Layer (`com.gamezone.service`):** Implements business rules and orchestrates domain operations (`SaleService`, `ProductService`, `AccessoryService`, `PromotionService`, `WarrantyService`, `ReturnService`, and `PersonService`).
3. **Persistence Layer (`com.gamezone.persistence`):** Encapsulates all file I/O operations (`ProductPersistence`, `AccessoryPersistence`, `SalePersistence`, `PromotionRepository`, `WarrantyRepository`, `ReturnRepository`, and `PersonPersistence`).
4. **Model Layer (`com.gamezone.model`):** Pure domain entities representing products, sales, warranties, promotions, returns, and people. Contains zero file I/O or console logic.

See [`docs/layers-diagram.md`](./docs/layers-diagram.md) and [`docs/integrated-class-diagram.md`](./docs/integrated-class-diagram.md) for detailed structural diagrams.

## Prerequisites

- JDK 17 or higher installed
- Maven installed (or NetBeans's built-in Maven support)

## How to build

From the project root:

```bash
mvn clean install
```

## How to run

**Option 1 — From NetBeans:**
1. Open the project via `File → Open Project`, selecting the `GameZoneUnicesar` root folder.
2. Right-click the project → `Run`.

**Option 2 — From terminal, with Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
```

## Preloaded data

On startup, the system loads data from the `data/` folder:
- `data/sellers.csv`: Preloaded with 3 sellers.
- `data/products.dat`: Preloaded with video games and consoles.
- `data/accessories.csv`: Preloaded with controllers, cables, and memories.
- `data/promotions.csv`: Preloaded with active percentage, category, and bulk purchase promotions.
- `data/sales.dat`, `data/warranties.csv`, and `data/returns.csv`: Created dynamically as transactions occur.

---

## Integrated System Overview

The integrated GameZone Unicesar system orchestrates the four business modules seamlessly:

1. **Unified Sale Registration (Ajuste A3):**
   - A single sale transaction can include video games, consoles, and accessories simultaneously.
   - Live inventory validation checks multi-unit availability across both `ProductService` and `AccessoryService` before accepting the sale.
   - Queries `PromotionService.findBestPromotionFor` to automatically apply the best available discount, calculated strictly on the merchandise subtotal.
   - Assigns an included 6-month basic warranty for every console in the sale and allows upgrading to a 12-month extended warranty (+10% of console price).
   - Generates an itemized receipt displaying Subtotal, Applied Promotion with discount, Extended Warranties, and Final Total.
   - Persists the sale to disk first, then creates and persists console warranties directly in `data/warranties.csv`.

2. **Accessory Support & Category Discounts (Ajuste A1):**
   - Comprehensive hierarchy for controllers, cables, and memory cards extending `Product`.
   - N:M compatibility mapping between accessories and consoles.
   - Category discounts can target video games, consoles, or accessories (`"ACCESSORY"`).

3. **Warranty Lifecycle & Revocation (Ajuste A2, A7):**
   - Warranties cover sold consoles with a single active warranty policy per purchase.
   - Circular dependency between `SaleService` and `WarrantyService` is eliminated by resolving sales through leaf persistence (`SalePersistence`).
   - If a console is returned, `WarrantyService.cancelWarranties` immediately revokes the warranty certificate and refunds the extended warranty fee.

4. **Return Management & Proportional Refunds (Ajuste A4, A5, A6):**
   - Enforces the 30-day return policy via `Sale.canBeReturned()`.
   - Computes proportional refunds matching the effective promotional discount of the original sale: $\text{price} \times (1 - \text{discount} / \text{subtotal})$.
   - Restores stock automatically delegating to `ProductService.restoreStock` or `AccessoryService.restoreStock` based on item type.
   - Generates comprehensive monthly balance reports detailing total sales revenue, total returns refunded, and net balance.

---

## Accessory Module

The system manages three types of accessories: controllers, cables, and memories, which can be sold together with consoles and video games in the same transaction.

### Features
- Register controllers, cables, and memories.
- List all accessories or filter by type (`CONTROLLER`, `CABLE`, `MEMORY`).
- Query accessories compatible with a specific console.
- Automatic stock decrements upon sale and restoration upon return.

---

## Promotion Module

The system supports automatic evaluation and selection of the best promotional discount for any sale transaction.

### Features
- Percentage discount on the total merchandise subtotal.
- Category discount targeting Video Games, Consoles, or Accessories (Ajuste A1).
- Bulk purchase discount applying a percentage off when purchasing at least $N$ items.
- Dynamic evaluation of active dates via `Promotion.isActive(LocalDate)`.
- `PromotionService.findBestPromotionFor(Sale)` automatically selects the single highest discount.

---

## Warranty Module

Console purchases automatically include warranty coverage managed by `WarrantyService`.

### Features
- Automatic basic warranty (6 months, included at no extra cost) for all consoles.
- Optional extended warranty (12 months, +10% of console price) selected during sale registration.
- Query warranties by product and sale, displaying formatted certificates.
- Query currently active warranties and warranties expiring within $N$ days.
- Revocation of warranties and refund of extended fees upon console return (Ajuste A7).

---

## Return Module

Customers can return purchased products within 30 days of the sale date.

### Features
- 30-day validity verification via `Sale.canBeReturned()`.
- Proportional refund calculation for purchases made with promotional discounts (Ajuste A5).
- Automatic inventory restoration for both regular products and accessories (Ajuste A4).
- Automatic warranty revocation and refund for returned consoles (Ajuste A7).
- Monthly balance reports showing monthly sales, monthly returns, and net revenue (Ajuste A6).

---

## Team

See [`TEAM.md`](./TEAM.md) for roles, class distribution, and responsibilities.

## Design Documentation

See the [`docs/`](./docs) folder for detailed system analysis, design documents, and diagrams:
- [`docs/integration-analysis.md`](./docs/integration-analysis.md): Complete analysis of adjustments A1 through A7.
- [`docs/integrated-class-diagram.md`](./docs/integrated-class-diagram.md): Unified Mermaid class diagram for the integrated system.
- [`docs/layers-diagram.md`](./docs/layers-diagram.md): Layered architecture diagram.
- Individual module analysis: [`accessory-analysis.md`](./docs/accessory-analysis.md), [`promotion-analysis.md`](./docs/promotion-analysis.md), [`warranty-analysis.md`](./docs/warranty-analysis.md), [`return-analysis.md`](./docs/return-analysis.md).
- AI usage logs: [`docs/ai-usage/`](./docs/ai-usage/).
