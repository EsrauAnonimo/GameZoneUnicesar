Return Module — Analysis and Design
This document covers the design decisions taken while integrating the return module into the GameZone Unicesar system, together with the layered-architecture justification for each one.

1. The business rule states that a sale can only be returned within 30 days after its date. Where is this rule implemented, and why does it belong to Sale instead of ReturnService?
The rule is implemented in Sale through the canBeReturned() method, which compares LocalDate.now() against the sale date plus 30 days and treats the limit day as still valid.

It belongs to Sale because the sale is the entity that owns the date the rule depends on, and because the rule is a property of the sale itself: asking a sale whether it may be returned is a question about that sale, not about a return that has not been created yet. ReturnService then reuses that answer as a precondition before registering anything.

Placing the check in ReturnService instead would duplicate the 30-day constant in a second place and would spread a rule about sales into the return module, where it would be harder to find and easier to change inconsistently.

2. A Return keeps a reference to the original Sale instead of copying the customer, the seller, the date and the total. What does this decision buy us, and how does it protect the consistency of the data?
Return declares a single attribute originalSale of type Sale and exposes it through getOriginalSale() with no setter, so a return can never be re-pointed to a different sale once it has been created.

Because the sale is referenced rather than duplicated, there is only one source of truth for the customer, the date and the total. If the customer renames their phone number, every return of that sale shows the updated value, and it is structurally impossible for a return to claim a different customer than the one who actually bought the products. The lists of returned products and the refund amount are the only data the return owns on its own.

3. The refund amount is the sum of the prices of the returned products. Which class computes it, and what stops the console from computing it instead?
The amount is computed by Return.calculateRefundAmount(), which walks returnedProducts, sums getPrice() for each element, stores the result in the refundAmount attribute and returns it. An empty list yields 0.0 rather than an error.

The console only asks the return for the value and formats it. If the menu recalculated the sum, the same figure would be produced in two different places, and the receipt printed by the user interface could disagree with the figure stored in the file whenever one of them changed. Because the calculation lives in the model, the receipt, the persisted CSV row and any later report all read the same value.

4. Returning a product has to put the units back into the inventory, but the sale already decreased the stock when it was registered. Why was a new restoreStock method added to ProductService instead of reusing updateStock?
The sale flow uses updateStock(String, int), which sets the available quantity to an absolute value, so it fits a scenario where the new quantity is already known.

restoreStock(String, int) was added instead, and it increases the current quantity by the given amount and persists the result. The difference matters: an absolute setter would need the caller to read the current stock, add the returned units and write the total back, which puts inventory arithmetic in the return module. The additive method keeps the arithmetic inside ProductService, and it ignores non positive quantities so that a bad return can never reduce the stock by accident.

This change is purely additive: updateStock keeps its original behaviour and signature, so the product, sale, accessory and promotion modules are untouched.

5. The repository stores only identifiers in data/returns.csv, and it receives SaleService and ProductService to rebuild the objects when loading. What does that choice imply for the layering, and what is its cost?
Because the file keeps the sale id and the product ids rather than whole objects, the repository needs the sale service and the product service to resolve them into real Sale and Product instances while loading. This is an inversion of the usual dependency direction: the persistence layer ends up depending on the service layer, since the rest of the system flows ui -> service -> persistence -> model.

The cost is real and should be stated plainly. It couples the repository to two services, it makes the repository impossible to test in isolation without those services, and it opens a risk of circular construction if those services ever need the repository back. It was implemented this way because the return record has to survive restarts of the program, and the sale and product data is the only place where that information lives. If the design were reopened, the cleaner alternative would be to keep the repository depending on nothing but the model, and let ReturnService perform the resolution after calling loadAll().

6. The same stock restoration problem appears again for accessories. How is it handled, and what is the pending gap?
A sale can contain accessories as well as videogames and consoles, and accessories are stored in a different file and managed by AccessoryService, not by ProductService. restoreStock looks the product up in the product list, so a returned accessory is not found there and the inventory is left untouched.

The return service therefore restores the stock correctly for videogames and consoles, and silently does nothing for accessories. Closing the gap is a one-line change on the persistence and service constructors: ReturnRepository and ReturnService would also need an AccessoryService, and the restoration branch would check whether the returned item is an Accessory before delegating to restoreStock. Until that is agreed with Dev 2, the accessory case is a known limitation of the module and not a bug in the videogame and console flows.
