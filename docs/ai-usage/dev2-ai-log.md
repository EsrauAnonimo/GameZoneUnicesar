# AI Usage Log — Developer 2 (Persistence and Service layers)

This document tracks the interactions with Artificial Intelligence tools that I, Developer 2, had while working on the persistence and service layers of the GameZone Unicesar system.

> **Note on when this log was written:** this log was completed after the Technical Lead asked for it, so it was reconstructed from the history of my conversations with the AI and from the Git history. It was not kept day by day.

---

## 1. General Workflow Summary

### Tool used
Claude (Anthropic), through its chat interface (web and mobile). Some early work was done in a different Claude account from the one used later. Claude never had access to the repository: it could only read the code and command output that I pasted or photographed, and it could not run Java or Git for me.

### How the AI was used
- **Code drafts.** Claude wrote drafts of the classes assigned to me (`AccessoryPersistence` and `AccessoryService`, `ReturnRepository` and `ReturnService`, `WarrantyRepository` and `WarrantyService`) and the changes of the later adjustments, based on the Technical Lead's instructions and on the existing code that I showed it.
- **Applying changes.** Because the team requires atomic commits, Claude prepared patches and small `.bat` scripts that apply each change, run `git add`, `git commit` with the agreed message and `git push`. I ran every script myself.
- **Git and environment help.** Setting up Git and NetBeans on a new computer, cloning the repository, creating branches, understanding push and pull errors, and reading build output.
- **Documentation.** A draft of the updated `docs/warranty-class-diagram.md`.

### What I did myself
- Ran every command and script, and compiled with *Clean and Build* in NetBeans after each change (`BUILD SUCCESS`).
- Ran the manual verification class `WarrantyReferenceCheck` and read its results.
- Opened the Pull Requests; the Technical Lead reviews and merges them.
- Edited `Main.java` by hand to adjust the constructor injection for A2, as the instructions asked.
- Checked the existing code before each change (for example, that `ConsoleUI` already catches the four custom exceptions before replacing `IllegalArgumentException`).

### Design decisions
Architecture and requirements come from the Technical Lead and from the course documents. The AI proposed implementations inside those rules; the decisions listed below are the ones I accepted.

---

## 2. Interaction Log (Talleres 1 to 4)

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decisión | Commit relacionado |
|:---|:---|:---|:---|:---|:---|:---|:---|
| No recordada | Claude | Taller 1<br>`feature/accessory-module` | Crear `AccessoryPersistence` y `AccessoryService` (Dev 2). | Pasé las instrucciones del líder para Dev 2 y pedí ayuda para implementar la persistencia y el servicio de accesorios. | Claude escribió un borrador con CSV y discriminador por tipo (`CONTROLLER`, `CABLE`, `MEMORY`), y los métodos de registro, listado, búsqueda por compatibilidad y actualización de stock. Después ajustó los nombres a `AccessoryPersistence`, que es el que usa el equipo. | Usé el borrador de Claude con cambios míos. | `c588efb` (`feat: add AccessoryPersistence with CSV save/load`)<br>`dde054e` (`feat: add AccessoryService with registration methods`) |
| No recordada | Claude (otra cuenta) | Taller 3<br>`feature/return-module` | Subir mis commits de `ReturnRepository` y `ReturnService`, y resolver un error de compilación causado por clases de promociones sin subir. | Pregunté cómo continuar con los commits y por qué el proyecto no compilaba. | Claude me ayudó con los comandos de Git para subir los commits y a entender el error de compilación. No conservo el detalle de esa conversación. | Usé el borrador de Claude con cambios míos. | `c3fb77e` (`feat: add ReturnRepository with CSV persistence`)<br>`6833970` (`feat: add ReturnService with registration and validations`) |
| 30/09/2026 | Claude | Taller 4<br>`feature/warranty-module` | Crear `WarrantyRepository` y `WarrantyService` a partir de las clases `Warranty`, `BasicWarranty` y `ExtendedWarranty` de Dev 1. | Mostré `Warranty`, `BasicWarranty`, `ExtendedWarranty`, `Sale`, `Product`, `SaleService` y `ProductService`, y pedí el código de mi parte. | Claude escribió ambas clases: CSV con `;` y discriminador `BASIC`/`EXTENDED`, resolución de venta y producto por id, y las consultas por producto y venta, activas y por vencer. Cuando mostré `Main.java`, ajustó el constructor para recibir `SalePersistence`, `ProductService` y `AccessoryService`, que es como el líder lo había cableado. | Adopté el código tal como compiló en NetBeans (`BUILD SUCCESS`). Lo repartí en 6 commits con un script que armó Claude, copiando versiones por etapas. | `56fd391`, `a965db3`, `d63e501`, `30e688f`, `778e120`, `625dbb2` |

---

## 3. Interaction Log (Requerimiento 5 — Integración, ajuste A2)

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decisión | Commit relacionado |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 18/10/2026 | Claude | A2<br>`fix/warranty-circular-dependency` | Eliminar la dependencia circular en garantías: el repositorio solo debe guardar IDs y el servicio debe resolver las referencias. | Mostré cómo `Main` construía `WarrantyRepository` y `WarrantyService`, cómo `SaleService` usaba `WarrantyService`, y qué métodos del servicio se usaban desde otros archivos. | Claude propuso que `WarrantyRepository` trabaje con una clase interna `WarrantyRecord` (tipo, id, productId, saleId, fecha) sin depender de `Sale`, `Product` ni de ningún servicio, y que `WarrantyService` reciba `SalePersistence` y `ProductService` para reconstruir las garantías. Las firmas públicas del servicio no cambian, así que `SaleService` y `ConsoleUI` no se tocan. | Adopté el diseño. Ajusté `Main.java` a mano con la nueva construcción. Los registros cuya venta o producto ya no existen se omiten al cargar en lugar de romper la carga. | `66498d9`, `d2cd8c0`, `040a404` |
| 19/10/2026 | Claude | A2<br>`fix/warranty-circular-dependency` | Actualizar el diagrama de clases de garantías y refactorizar la lógica repetida de asignación. | Pedí el diagrama actualizado y la versión pulida del servicio. | Claude actualizó `docs/warranty-class-diagram.md` (agregó `WarrantyRecord`, `SalePersistence` y las nuevas relaciones) y extrajo un método privado común para guardar el registro en las dos asignaciones. | Adopté ambos cambios. | `2934d19` (`docs: update warranty-class-diagram.md`)<br>`1a985ba` (`refactor: polish warranty reference resolution`) |
| 19/10/2026 | Claude | A2<br>`fix/warranty-circular-dependency` | Verificar el cambio sin librerías de prueba, porque el `pom.xml` no tiene JUnit. | Mostré que `pom.xml` no contiene JUnit y que `src/test/java` estaba vacía. | Claude escribió `WarrantyReferenceCheck`, una clase con `main` que usa un archivo temporal y datos simulados, y comprueba ids, resolución desde IDs, costo de la garantía extendida, registros huérfanos y continuidad de ids. | La ejecuté en mi computador y pasaron las 12 comprobaciones (`ALL CHECKS PASSED`). | `c24001c` (`test: verify warranty loading without circular dependency`) |

---

## 4. Interaction Log (Requerimiento 6 — Validaciones y Excepciones)

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decisión | Commit relacionado |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 20/10/2026 | Claude | Req. 6<br>`feature/exception-handling-dev2` | Entender qué había que cambiar en mis clases. | Mostré las instrucciones del líder y los resultados de buscar `IllegalArgumentException` e `IOException` en el proyecto. | Claude señaló que `ProductService`, `PersonService` y `SaleService` ya no tenían `IllegalArgumentException`, y que los que quedaban estaban en `ReturnService` y `WarrantyService`, que son clases mías. | Le pregunté al líder si cambiaba esas dos clases. Los cambios en esas dos clases se incluyeron en el PR #21, que el líder fusionó. | — |
| 20/10/2026 | Claude | Req. 6<br>`feature/exception-handling-dev2` | Que la persistencia deje de tragar los errores de archivo y lance `PersistenceException` con contexto. | Mostré el código de `ProductPersistence`, `SalePersistence` y `PersonPersistence`, y la clase `PersistenceException`. | Claude cambió los `catch (IOException)` que solo imprimían mensajes o devolvían una lista vacía, para lanzar `PersistenceException` con el archivo y la operación. Como la excepción solo acepta el mensaje, el detalle del error va dentro del texto. Cerró los flujos con `try-with-resources` y agregó JavaDoc. | Adopté los cambios. Un archivo dañado ahora produce una excepción en vez de una lista vacía. | `6595e7f`, `70111db`, `cb5a456` |
| 20/10/2026 | Claude | Req. 6<br>`feature/exception-handling-dev2` | Cambiar `IllegalArgumentException` por excepciones tipadas en `ReturnService` y `WarrantyService`. | Mostré cómo `ConsoleUI` captura las excepciones y las líneas de esas dos clases que lanzaban `IllegalArgumentException`. | Claude comprobó que `ConsoleUI` captura solo las cuatro excepciones nuevas, y propuso: venta inexistente o producto que no pertenece → `ResourceNotFoundException`; plazo de 30 días vencido o producto ya devuelto → `BusinessRuleException`; sin productos o argumentos nulos → `InvalidDataException`. Los mensajes siguen en español. | Adopté la correspondencia y actualicé el JavaDoc. | `7450c22`, `0c62e0e` |
| 20/10/2026 | Claude | Req. 6<br>`feature/exception-handling-dev2` | Aplicar lo mismo a la persistencia de garantías. | Mostré los `catch` de `WarrantyRepository`. | Claude cambió `UncheckedIOException` por `PersistenceException` con archivo y operación, y quitó el `import` que ya no se usaba. | Adopté el cambio. Compilé con `BUILD SUCCESS`. | `913c8bc` |

---

## 5. Code generated by the AI and how I checked it

- Claude wrote the code and the patches for the commits listed above. I did not write those classes from scratch.
- I verified each change by compiling in NetBeans, by running the verification class for A2, and by reading the Git log and the Pull Request before asking for review.
- Claude could not compile Java in its own environment, so every claim that "it compiles" was confirmed only by my own build.
- All code follows the project rules: pure Java 17, no `Optional`, no `Stream`, private fields, English JavaDoc and comments, and Spanish text for the user.

## 6. What I did NOT ask AI to do
- I did not ask the AI to commit, push or merge anything: it has no access to the repository. All commits were created by me, running the scripts and commands.
- I did not ask the AI to bypass peer review: every change goes through a Pull Request reviewed and merged by the Technical Lead.
- I did not ask the AI to change files that belong to the Technical Lead (`ConsoleUI`, `README.md`), except for adjusting `Main.java` for A2 by hand, as the instructions for that adjustment ask me to coordinate with the Lead.

## 7. Pending items in this log
- Add entries for the next requirement (Requerimiento 7) when it starts.
