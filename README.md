# GameZone Unicesar

Inventory and sales management system for a video game store, developed in Java with a layered architecture.

## Description

GameZone Unicesar is a console-based system that manages products (video games and consoles), people (customers and sellers), and sales, with data persistence between executions.

## Technologies

- **Language:** Java 17
- **Build tool:** Maven
- **Persistence:** Java serialization (`.dat` files)

## Architecture

The project is organized into 4 layers within the `com.gamezone` package:

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
1. Open the project via `File → Open Project`, selecting the `GameZoneUnicesar` root folder
2. Right-click the project → `Run`

**Option 2 — From terminal, with Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
```

## Preloaded data

On first run, the system automatically creates 3 preloaded sellers in `data/`.
## Accessory module

GameZone Unicesar now supports a new line of products: video game accessories.
The system manages three types of accessories: controllers, cables and
memories, which can be sold together with consoles and video games in the
same transaction.

### Features

- Register controllers, cables and memories.
- List all accessories.
- List accessories by type (`CONTROLLER`, `CABLE`, `MEMORY`).
- Query accessories compatible with a specific console.
- Include accessories in sales together with consoles and video games.
- Update accessory stock automatically after each sale.

### New menu options

The console menu now includes an **Accessory Management** submenu with the
following options:

1. Register a new controller.
2. Register a new cable.
3. Register a new memory.
4. List all accessories.
5. List accessories by type.
6. Query accessories compatible with a console.

The **Sales Management** submenu now allows selecting accessories in addition
to products when registering a new sale.

### Data

Accessories are persisted in `data/accessories.csv` using a semicolon (`;`)
separator. The file uses a type discriminator (`CONTROLLER`, `CABLE`,
`MEMORY`) to reconstruct the correct subclass when loading.

## Warranty module

GameZone Unicesar now supports warranty management for consoles sold in the store.
The system automatically grants an included basic warranty for each console and
allows customers to purchase an optional extended warranty.

### Features

- Automatically assign a basic warranty (6 months, no additional cost) to each console in a sale.
- Optionally upgrade to an extended warranty (12 months, +10% of the console price) during sale registration.
- Consult warranties by product and sale, emitting a formatted warranty certificate in Spanish.
- List all registered warranties.
- List warranties that are currently active today.
- List active warranties expiring soon within a user-defined number of days.
- Ensure only consoles receive warranties while video games and accessories remain unaffected.
- Add extended warranty costs after promotional discounts to preserve discount calculations.

### New menu options

The main menu now includes a **Warranty Management** (`Gestión de garantías`) submenu (option 7) with the following options:

1. Consultar garantía de un producto en una venta (Query warranty by product and sale, displaying the certificate).
2. Listar todas las garantías (List all registered warranties).
3. Listar garantías vigentes (List currently active warranties).
4. Listar garantías próximas a vencer (List warranties expiring soon within N days).
0. Volver al menú principal (Return to main menu).

Additionally, when registering a sale, the system prompts whether extended coverage is desired for each console added.

### Data

Warranties are persisted in `data/warranties.csv` using a semicolon (`;`) separator with the following structure:
```csv
type;id;productId;saleId;startDate
```
The first column uses a type discriminator (`BASIC`, `EXTENDED`) to reconstruct the appropriate subclass upon loading. End dates and additional costs are dynamically computed to maintain a single source of truth.

## Team

See [`TEAM.md`](./TEAM.md) for roles, class distribution, and responsibilities.

## Design documentation

See the [`docs/`](./docs) folder for the system analysis and diagrams (hierarchy, class, layers).
