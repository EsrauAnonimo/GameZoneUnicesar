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

## Team

See [`TEAM.md`](./TEAM.md) for roles, class distribution, and responsibilities.

## Design documentation

See the [`docs/`](./docs) folder for the system analysis and diagrams (hierarchy, class, layers).