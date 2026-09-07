# Hierarchy Diagram

This diagram represents the inheritance relationships identified in the domain model.
It shows only generalization/specialization relationships, without attributes, methods, or associations.

```mermaid
classDiagram
    class Person {
        <<abstract>>
    }
    class Customer
    class Seller

    class Product {
        <<abstract>>
    }
    class VideoGame
    class Console

    Person <|-- Customer
    Person <|-- Seller

    Product <|-- VideoGame
    Product <|-- Console
```

## Notes
- `Person` and `Product` are abstract classes: they cannot be instantiated directly, since they only exist to group common characteristics of their subclasses.
- `Customer`, `Seller`, `VideoGame`, and `Console` are concrete classes.
- Both hierarchies belong exclusively to the `model` layer.
