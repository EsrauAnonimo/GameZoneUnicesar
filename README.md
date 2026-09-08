# GameZone Unicesar

A management system tailored for the GameZone Unicesar video game store.

## Overview

Built with Java using a classic layered architecture, this system takes care of:
- Products (video games and consoles)
- People (managing customers and sales staff)
- Sales processing (with automatic real-time inventory updates)

## Architecture

- **Model**: Core domain models and entities
- **Persistence**: File-based data storage
- **Service**: Core business logic and rules
- **UI**: Interactive command-line interface (CLI)

## 👥 Team

| Role | Team Member |
|------|-------------|
| Tech Lead | [Esteban Vergara] 
| Developer 1 | [Luis gomes] 
| Developer 2 | [Cristian Perez] 

## 🚀 Getting Started

```bash
# Build & compile
mvn clean compile

# Launch the application
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
