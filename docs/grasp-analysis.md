# GRASP Analysis - Team Answers

1. **Criterion:** We consider a violation when a class assumes responsibilities that don't belong to it (e.g., UI logic in the model), or when it uses conditionals based on type instead of polymorphism. Evidence must include the exact class, method, and a snippet of the current code in develop.
2. **Boundary findings:** We checked dependencies between our modules. We found that `SaleService` (Leader) depends on `ProductService` (Developer 1) to restore stock, which is correct.
3. **Overlapping patterns:** We recorded V-01 and V-03 together because they stem from the same `instanceof` code. The correction will be guided by Polymorphism.
4. **Strategy for non-regression:** We will run the main menu operations (adding products, making a sale, returning a product) before and after the refactor, ensuring the console output remains identical.
5. **Correction order:** We will fix model violations first (Polymorphism), then UI/Service violations, coordinating via chat to avoid merge conflicts.