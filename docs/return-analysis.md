Return Module — Analysis and Design
This document outlines the core design decisions made while integrating the return module into the GameZone Unicesar system, along with the software architecture principles behind each choice.

1. Business Rule: 30-Day Return Limit
Where is it implemented?

Inside the Sale class via the canBeReturned() method. It evaluates LocalDate.now() against the original sale date plus 30 days, considering the 30th day as still eligible for a return.

Why Sale instead of ReturnService?

The sale owns the transaction date required for this check. Determining whether a sale is eligible for a return is a property of the sale itself—we are asking an existing sale about its state, not evaluating a return that hasn't been created yet. ReturnService simply consumes this result as a precondition before creating a return record.

Architectural Impact:

Placing this rule in ReturnService would leak domain logic across modules and duplicate the 30-day rule. Keeping it in Sale ensures domain rules stay encapsulated where the underlying data lives.

2. Referencing Sale vs. Duplicating Data
Design Choice:

Return holds a single originalSale reference of type Sale, exposed through getOriginalSale() without a setter. Once instantiated, a return cannot be reassigned to a different sale.

Data Consistency:

By referencing the original sale instead of copying customer information, seller details, date, and total amount, we maintain a single source of truth. For instance, if a customer updates their phone number, all associated returns automatically reflect the update. It becomes structurally impossible for a return to reference a different customer than the one who completed the purchase. The return entity only owns data specific to its operation: the list of returned items and the computed refund amount.

3. Refund Calculation Authority
Calculation Ownership:

The refund total is computed by Return.calculateRefundAmount(). It iterates through returnedProducts, sums the output of getPrice() for each item, stores the result in refundAmount, and returns it. If the item list is empty, it evaluates to 0.0 without throwing an exception.

Why UI/Console Shouldn't Calculate It:

The user interface is strictly responsible for rendering data, not calculating financial totals. If the UI performed this calculation independently, any discrepancy in display logic could cause the printed receipt to mismatch the value stored on disk. Keeping the calculation inside the domain model guarantees that receipts, CSV logs, and reports all read from the exact same value.

4. Stock Restoration Design
The Problem:

The standard sale workflow uses ProductService.updateStock(String, int), which accepts an absolute integer to overwrite current inventory levels. Reusing this method for returns would require the return module to fetch the current stock, add the returned quantity, and manually write back the total.

The Solution:

Instead of coupling stock arithmetic to the return module, a dedicated restoreStock(String, int) method was added to ProductService. This method directly increments inventory for a given product ID and persists the result. It also validates inputs to ensure non-positive quantities cannot inadvertently lower stock levels.

Architectural Impact:

This is a purely additive modification. updateStock retains its original signature and behavior, ensuring existing workflows (sales, products, promotions, accessories) remain completely unaffected while keeping inventory logic localized within ProductService.

5. Repository Dependencies and Layering Trade-offs
Persistence Strategy:

To keep records compact, data/returns.csv stores only entity identifiers (sale ID and product IDs). Consequently, ReturnRepository receives instances of SaleService and ProductService to rehydrate complete objects upon loading data from disk.

Layering Trade-offs:

This creates an inverted dependency direction where the persistence layer depends on the service layer, breaking the standard UI -> Service -> Persistence -> Model flow.

Costs & Future Improvements:

Tight Coupling: The repository becomes directly dependent on two external services.

Testability: Mocking or testing ReturnRepository in isolation requires setting up full service instances.

Circular Dependency Risk: Increases the risk of circular references if those services ever require the repository.

Alternative: A cleaner future refactor would strip service dependencies from ReturnRepository, allowing it to depend exclusively on domain models while shifting the entity resolution process into ReturnService after loading raw IDs.

6. Accessory Stock Gap and Resolution Path
Current Limitation:

Sales can include accessories, which are managed separately by AccessoryService and persisted in a different data file. Currently, restoreStock only queries the product registry. When an accessory is returned, the lookup fails silently, leaving accessory stock unadjusted. As a result, stock restoration currently works for video games and consoles, but not accessories.

Resolution Path:

Fixing this requires injecting AccessoryService into ReturnRepository and ReturnService, then adding a type check (e.g., checking if an item is an Accessory) before delegating to the appropriate service. Until this architectural update is coordinated across the team, accessory stock restoration remains a documented system constraint rather than an unexpected runtime bug.
