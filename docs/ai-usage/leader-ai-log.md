# AI Usage Log — Technical Lead (Esteban Vergara)

This document tracks all interactions with Artificial Intelligence tools by the Technical Lead throughout the development and integration of the GameZone Unicesar system, in compliance with the course integration requirements (Requerimiento 5).

---

## 1. General Workflow Summary

### Git and GitHub
Used AI mainly for basic Git commands throughout the project setup: cloning the repository, creating and pushing branches (`develop`, `refactor/unified-sale-registration`, `docs/integration-documentation`), branch protection configuration, and the PR merge workflow. Also used it to diagnose collaborator permission issues and review PR requirements.

### Design Decisions
Architectural rules and core business domain designs were formulated by the team. AI was consulted as a technical sounding board to evaluate edge cases (such as avoiding circular constructor injection between services, preventing in-memory stock overwrites on multi-unit sales, and ensuring financial accuracy when combining promotions with warranties).

### Java Implementation
AI explained and validated structural solutions for the sales and integration orchestration (`SaleService`, `Sale`, `ConsoleUI`, `Main`), ensuring strict adherence to project constraints: pure Java 17, no custom interfaces, no `Optional`, no `Stream`, private fields with getters/setters, English JavaDoc, and Spanish user-facing console text.

---

## 2. Interaction Log (Requerimiento 5 — Integración)

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decisión | Commit relacionado |
|:---|:---|:---|:---|:---|:---|:---|:---|
| **18/10/2026** | Antigravity AI | Fase 3<br>`refactor/unified-sale-registration` | Definir el orden óptimo de los pasos de `SaleService.registerSale` para evitar inconsistencias entre promociones y garantías. | "¿En qué orden deben ejecutarse las promociones y el cálculo de garantías extendidas en `registerSale` para no aplicar descuentos comerciales sobre el costo de la garantía extendida?" | Explicó que el subtotal debe calcularse únicamente sumando los precios de los productos y accesorios. El descuento de `PromotionService` debe calcularse y restarse exclusivamente sobre ese subtotal. El costo de la garantía extendida (+10% por consola) debe sumarse después del descuento: `total = subtotal - descuento + garantiaExtendida`. | Se aprobó e implementó exactamente ese orden secuencial (pasos 3 a 6) en `SaleService`, asegurando que la garantía nunca sufra descuentos comerciales y el total final cuadre con el recibo. | `754614c`<br>(`feat(model): add extendedWarrantyCost and receipt breakdown to Sale`) |
| **18/10/2026** | Antigravity AI | Fase 3<br>`refactor/unified-sale-registration` | Resolver el defecto de stock multi-unidad cuando una venta incluye varias unidades del mismo ítem. | "Al registrar una venta con varias unidades del mismo ID, hacer `item.getAvailableQuantity() - 1` en un bucle sobreescribe el stock o genera cantidades erróneas en memoria. ¿Cómo evitar este defecto sin agregar interfaces ni colecciones complejas?" | Sugirió agregar un contador de unidades por ID (`countUnitsInSale`), validar la cantidad acumulada contra el stock real disponible, calcular `newStock = currentStock - totalSolicitado`, invocar `updateStock` una sola vez por ID único y actualizar todas las instancias en memoria. | Se descartó decrementar de uno en uno en el bucle principal. Se adoptó agrupar por ID único en una lista de validados/actualizados y llamar a `productService.updateStock` o `accessoryService.updateStock` con el decremento total consolidado. | `24a0fd1`<br>(`Merge pull request #15 from EsrauAnonimo/refactor/unified-sale-registration`) |
| **18/10/2026** | Antigravity AI | Fase 3<br>`refactor/unified-sale-registration` | Eliminar el descarte de garantías por clave foránea y evitar guardado doble en la persistencia (Paso 8). | "Al llamar a `warrantyService.assignBasicWarranty`, `WarrantyRepository` busca la venta en `data/sales.dat`. Si no la encuentra, la garantía se descarta. ¿Cómo ordenar la persistencia sin hacer un guardado doble?" | La venta debe persistirse primero en `persistence.save(sales)` con su total y desglose ya calculados. Luego, se itera sobre las consolas llamando directamente a `warrantyService.assignBasicWarranty` o `assignExtendedWarranty`. Como `WarrantyService` ya guarda internamente en su propio repositorio CSV, encontrará la venta en disco y no se requiere volver a guardar la venta. | Se unificó el paso 8 en un solo bloque continuo: primero `persistence.save(sales)` y de inmediato la asignación directa de garantías por consola, evitando métodos intermedios y eliminando el guardado doble. | `24a0fd1`<br>(`refactor(service): update inventory per item type and persist sale`) |
| **18/10/2026** | Antigravity AI | Fase 5<br>`docs/integration-documentation` | Estructurar el diagrama de clases integrado y el análisis de integración de los ajustes A1 a A7. | "¿Cómo representar la orquestación de `SaleService` y `ReturnService` en el diagrama Mermaid integrado sin violar la dirección de dependencias UI -> Service -> Persistence -> Model?" | Presentó la estructura por capas en Mermaid organizando las clases por capa, destacando a `SaleService` como orquestador de ventas y a `ReturnService` como orquestador de devoluciones, y mostrando a `SalePersistence` como hoja para documentar la resolución de dependencias circulares. | Se adoptó la estructura en `docs/integrated-class-diagram.md` y la documentación técnica en `docs/integration-analysis.md`, verificando el cumplimiento estricto de Java puro. | `docs/integration-documentation` |

---

## 3. Interaction Log (Requerimiento 6 — Validaciones y Excepciones)

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decisión | Commit relacionado |
|:---|:---|:---|:---|:---|:---|:---|:---|
| **19/10/2026** | Antigravity AI | Fase 1<br>`feature/exception-handling` | Determinar si `GameZoneException` debe ser checked o unchecked, y analizar impacto en la arquitectura. | "¿`GameZoneException` debe extender `Exception` o `RuntimeException`? ¿Qué consecuencias arquitectónicas tiene sobre las firmas en servicios y repositorios?" | Explicó que extender `RuntimeException` evita ensuciar las firmas de métodos con `throws` a lo largo de las capas intermedias, desacoplando los servicios y permitiendo que los errores de validación fluyan de forma limpia hasta la frontera de presentación (`ConsoleUI`). | Se decidió extender `RuntimeException`, garantizando compatibilidad con las firmas existentes, código simple (KISS) y captura centralizada en la UI. | `bc55081`<br>(`docs: add exception handling and validation architectural analysis`) |
| **19/10/2026** | Antigravity AI | Fase 1<br>`feature/exception-handling` | Diseñar la estructura de captura de excepciones diferenciadas en `ConsoleUI` sin violar el principio KISS. | "¿Cómo estructurar la captura de cuatro excepciones personalizadas en `ConsoleUI` sin caer en antipatrones como `instanceof` anidados ni sobre-ingeniería?" | Recomendó implementar bloques `catch` múltiples específicos en el orden adecuado de especificidad. Esto permite asignar mensajes en español claros para cada categoría sin condicionales internos complejos ni reflexión. | Se adoptaron bloques `try/catch` específicos en `ConsoleUI`, garantizando mensajes diferenciados para no encontrado, regla de negocio, datos inválidos y persistencia. | `3f46be2`<br>(`docs: add Mermaid class diagram for exception and validation layers`) |
| **19/10/2026** | Antigravity AI | Fase 2<br>`feature/exception-handling` | Refactorizar `ConsoleUI` aplicando captura diferenciada de excepciones en todos los flujos interactivos. | "¿Cómo aplicar de forma consistente los cuatro bloques `catch` de `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException` y `PersistenceException` en los menús de productos, personas, ventas, accesorios, promociones, devoluciones y garantías de `ConsoleUI`?" | Proporcionó el desglose de los puntos de interacción en `ConsoleUI` donde se invocan servicios de dominio, estructurando los bloques `try/catch` con los mensajes exactos especificados en la rúbrica y preservando el control de flujo en consola. | Se integraron bloques `try/catch` específicos en cada flujo de registro y consulta de `ConsoleUI`, garantizando mensajes exactos para el usuario y manteniendo el código simple sin frameworks ni abstracciones complejas. | `refactor(ui)` |

---

## 4. What I did NOT ask AI to do
- Did not ask AI to generate arbitrary architecture or bypass established course rules.
- Did not ask AI to generate patterns that violate the rubric (no factories, no builders, no interfaces, no streams, no lambdas).
- Did not ask AI to perform direct commits or bypass peer review pull requests.

