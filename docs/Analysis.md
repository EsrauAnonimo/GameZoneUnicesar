# GameZone Unicesar - System Analysis

## Team members

##esteban vergara (technical leader)- code : 1065595117
##luis gomes (developer 1 - products)- code: 1118821800
##cristian perez (developer 2 - people). code: 1067599993

### 1. Common and specific attributes

The idea is to separate what it means to be a "person" from what it means to be a "customer" or a "seller." All people who interact with the store, regardless of their role, have a name, an identification document, and a contact phone number. These attributes identify them as people, regardless of what they do in the system.

On the other hand, the email address is only relevant to a customer because it is used to contact them regarding their purchases. The employee code and work shift are only relevant to a seller because they are administrative data related to their employment relationship with the store.

This distinction is reflected in an inheritance hierarchy: common attributes are placed in the base class (Person), while the attributes specific to each role are placed in the corresponding subclass (Customer, Seller). In this way, each subclass inherits the common attributes and only adds the attributes that are exclusive to its role, avoiding code duplication.

### 2. Generic Person class

We can consider whether, in the real GameZone business, there could be someone who interacts with the store without being either a customer or a seller. The answer is no, because everyone who interacts with the system necessarily has one of these two roles.

Therefore, Person exists only as an abstraction to group the common characteristics of customers and sellers, rather than as an entity that should exist independently in the business domain.

For this reason, the class should not be instantiable. If someone were able to write `new Person("123", "Juan", ...)`, they would be creating an entity that does not represent anything real in the system, since that person would neither buy nor sell products.

The Java mechanism that prevents this is declaring the class as `abstract`. The compiler does not allow an instance to be created directly from an abstract class; instances can only be created from its concrete subclasses.


### 3. Common and specific characteristics

All products have some basic information in common. These are:
identifier: the code used to identify the product.
title: the name of the product.
price: the price of the product.
availableQuantity: shows how many units are available.
Besides this information, each type of product has its own characteristics.
For video games, we have:
platform: shows where the game can be played, such as PlayStation, Xbox, or PC.
genre: shows the type of game, for example, action, sports, or adventure.
ageRating: shows the recommended age for the game.
For consoles, we have:
brand: shows the brand of the console.
model: shows the specific model.
generation: shows which generation the console belongs to.
To organize this information, we have the Product class, which contains the information that all products have in common. Then, VideoGame and Console inherit this information and add their own characteristics.
In simple words, all products have some basic information, but each type also has its own specific characteristics

### 4. Product description behavior

The Product class has a method called getDescription(), but this method is abstract. This means that the Product class says that every product must have a way to show its description, but it does not define exactly how it will look.
Each class creates its own version of the method. This is related to polymorphism, because different classes can use the same method, but each one can work in a different way.
For example, a video game could show:
"FIFA 24 | PS5 | Sports"
And a console could show:
"PlayStation 5 | Sony | Digital"
So, even though both products use getDescription(), each one shows the information that belongs to its own type.

## Sales and relationships


### 5. Relationships between Sale and other classes

The Sale class has an association relationship with Customer, Seller, and Product.
A sale is related to one customer and one seller, and it contains one or more products.
These relationships are associations because these classes represent different entities
that participate in the sale, and none of them is a specialization of Sale.

### 6. Responsibility for calculating the sale total

The Sale class should be responsible for calculating its own total because the total
is directly related to the products included in the sale. The Sale class represents
the transaction and has access to the products purchased, so calculating the total
is a responsibility that belongs to the sale itself.

## Business constraints

### 7. Minimum number of products in a sale

A sale must contain at least one product before it can be registered. This rule should
be validated before completing the sale registration. The SaleService should verify
that the sale contains at least one product and reject the operation if the product
list is empty.

### 8. Inventory update after a sale

The SaleService should be responsible for handling the inventory update when a sale
is registered. First, it must verify that each product has enough available stock.
If the stock is sufficient, the quantity sold is deducted from the product inventory.
The persistence layer is then used to save
the updated information.

## Layered architecture

### 9. Responsibilities of each layer

The criterion is not simply what a class does in general, but what the class is responsible for.

If a class represents a business concept with its own data and behavior, without knowing anything about files or the console, it belongs in the `model` layer (Person, Customer, Seller).

If a class is only responsible for reading and writing data to files, it belongs in the `persistence` layer (PersonPersistence).

If a class contains business rules, such as validations, deciding when something can be registered, or coordinating persistence operations, it belongs in the `service` layer (PersonService).

If a class interacts directly with the user, such as displaying menus, reading keyboard input, or showing results, it belongs in the `ui` layer.

In short, the criterion is that each class should be placed in the layer that corresponds to its single responsibility, rather than placing in one class everything it could potentially do.


### 10. Why Persistence Logic Should Not Be in Domain Classes

It is not a good idea to put the logic for saving and reading files inside classes like Product, Sale, or Customer, because these classes already have other responsibilities.
The domain classes should mainly manage the information and business rules. On the other hand, saving and reading files belongs to the Persistence layer.
Separating these functions makes the program easier to organize and modify. For example, if we later want to stop using files and use a database instead, we would not have to change the main classes of the program.
That is why the project is divided into different layers:
Model: contains the information and business rules.
Persistence: is responsible for saving and reading data.
Service: coordinates the operations of the program.
UI: is the part that the user interacts with.
This way, each part of the program has a specific function. Classes like Product should focus on representing the product and not on knowing where or how the information is stored.

### 11. Dependencies between layers

The dependencies between the layers follow the direction UI → Service → Persistence → Model.
The user interface depends on the service layer because it uses the services to execute
the operations requested by the user. The service layer depends on the model and persistence
layers because it contains the business rules and needs to access stored information.
The persistence layer depends on the model to store and recover domain objects. The model
does not depend on any other layer, which keeps the domain independent from the rest of
