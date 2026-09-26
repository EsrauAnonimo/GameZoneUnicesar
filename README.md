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


## Team

See [`TEAM.md`](./TEAM.md) for roles, class distribution, and responsibilities.

## Design documentation

See the [`docs/`](./docs) folder for the system analysis and diagrams (hierarchy, class, layers).
