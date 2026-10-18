Promotion Module — Analysis and Design
This document covers the five guiding questions for integrating the promotion module into the GameZone Unicesar system.

1. The three promotions have different calculation rules but share common attributes. How is this reflected in the class hierarchy? Which OOP mechanism allows each promotion type to calculate its discount without the rest of the system knowing the concrete types?
The class hierarchy is structured around an abstract class called Promotion, which brings together common attributes (id, name, startDate, endDate) and general behavior (isActive). Each concrete subclass (PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount) then implements its own version of calculateDiscount.

The mechanism that makes this work is polymorphism, supported by method overriding. This allows SaleService (or any other module client) to interact strictly with the abstract type Promotion and call calculateDiscount(sale) without caring which concrete class is executing under the hood. This keeps the system decoupled and makes it effortless to introduce new promotion types down the road without altering existing code.

2. The base class Promotion cannot implement the discount calculation method because each type has a different logic. How is this method declared in the base class and what does this declaration guarantee regarding the subclasses?
The method is declared in the abstract class Promotion as:
public abstract double calculateDiscount(Sale sale)

This signature establishes a contract that forces every concrete subclass to provide its own calculation logic. If a subclass fails to implement it, the code simply won't compile. This guarantees that any object derived from Promotion will reliably respond when asked to calculate its discount, resolving the call at runtime.

3. The business rule states that only the promotion with the highest discount is applied. In which class is this selection logic located and why is this location consistent with the layered architecture? Why should this logic NOT be in Sale or in the console menu?
The selection logic lives in PromotionService, specifically inside the findBestPromotionFor(Sale sale) method. This fits right into a layered architecture, as the service layer is responsible for coordinating the module's business rules and has direct access to the repository that fetches stored promotions.

It shouldn't be in Sale because Sale is a domain entity whose sole job is to model sale data, not to manage or evaluate available promotions.

It shouldn't be in the console menu because the user interface should only handle presentation and user interactions, never core business rules.

Keeping the evaluation logic in the service ensures a clean domain model and a lightweight UI.

4. What modifications are required in the Sale class and in the generateReceipt method so that the receipt shows the applied discount? Do these modifications break any existing behavior?
The Sale class requires two new private attributes along with their getters and setters:

appliedPromotionName (String)

discountAmount (double)

Meanwhile, generateReceipt needs to be updated to print the subtotal, the applied discount (including the promotion name), and the final total.

These modifications do not break any existing behavior. They are purely additive, meaning existing constructors, methods, and usages will continue to work seamlessly. When no promotion is applied, these attributes default to null and 0.0, preserving full backward compatibility.

5. Active promotions are determined by comparing the current date with the start and end dates of each promotion. Where is this validation performed (in Promotion, in PromotionService, or in both)? Justify.
The validation logic itself belongs in Promotion via the isActive(LocalDate date) method, since the promotion itself holds the date attributes and knows whether it is currently active.

The PromotionService then invokes this method when filtering active promotions in listActivePromotions() and when picking the best deal in findBestPromotionFor(Sale).

This division of responsibilities is clean and intentional: Promotion encapsulates the business rule, while PromotionService orchestrates its execution. This avoids duplicating date-comparison logic and keeps the rule centralized in one place.
