# SOLID Refactor Log

| Class | Change Made | Violation ID | Principle Applied | Commit / PR | Responsible |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ProductService | Applied constructor injection to remove direct instantiation of ProductPersistence. | V-01, V-03 | DIP, SRP | Hash de tu commit | Developer 1 |
| ProductService | Created a single generic `registerProduct(Product)` method to avoid modifying the class for new product types. | V-02 | OCP | Hash de tu commit | Developer 1 |