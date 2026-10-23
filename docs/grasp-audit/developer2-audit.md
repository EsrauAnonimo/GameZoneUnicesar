# GRASP Audit Report - Developer 2

Audited against the state of the `develop` branch at commit `916d492`.

## 1\. Scope

Classes and methods audited, by module and requirement.

|Module (requirement)|Classes / methods audited|
|-|-|
|People (Taller 1)|Person, Customer, Seller, PersonRepository, PersonService|
|Accessories (Req. 1)|AccessoryRepository, AccessoryService|
|Promotions (Req. 2)|PromotionRepository, PromotionService|
|Returns (Req. 3)|ReturnRepository, ReturnService (including the monthly balance)|
|Warranties (Req. 4)|WarrantyRepository, WarrantyService|
|Exceptions and validations (Req. 6)|Use of exceptions and validators in my services and repositories|

## 2\. Pattern verdicts

Verdict values: **violation**, **correct**, **not applicable**.

### 2.1 Information Expert

Verdict: correct.



According to the Information Expert pattern, a responsibility

should belong to the class that already has the data needed to

fulfill it. In PersonService.registerCustomer, PersonService

maintains the lists of customers and sellers, so it is the right

class to check that the ID is not duplicated. Saving is delegated

to PersonPersistence, the only class that touches files. Field

validation is done by PersonValidator, a team design decision to

centralize the rules. It could be argued that Customer also has

the data, but we preferred not to spread that logic into the model.



Evidence: PersonService.registerCustomer, lines 60 to 70.

### 2.2 Creator

Verdict: correct.



In PersonService.preloadVendorsIfEmpty, the service creates the

Seller objects with new Seller(...) because it is the class that

contains the sellers list and has the data needed to initialize

them, which is exactly when the Creator pattern says that class

should create them.



Evidence: PersonService.preloadVendorsIfEmpty, lines 160 to 171

(the new Seller(...) calls are on lines 163, 165 and 167).

### 2.3 Controller

Verdict: correct.



PersonService is the first object after the UI that coordinates

the registerCustomer operation, and it does not mix console input

or output. The UI does not contain the business rules, and the

service knows nothing about how the result is displayed.



Evidence: PersonService.registerCustomer, lines 60 to 70. A search

of PersonService for Scanner, System.out and println found

No maches.

### 2.4 Low Coupling

Verdict: correct.



In PersonService, the 8 project dependencies are necessary and

point in the expected direction: the model (Customer, Seller,

Person), the layer below (PersonPersistence) and the validator.

PersonService does not depend on the UI or on other services, and

it receives PersonPersistence through the constructor instead of

creating it inside.



If PersonValidator changed, only three calls would be affected

(validate, validateUniquePerson and validatePersonExists). If

PersonPersistence changed, only loadAll and save would be

affected. The damage stays contained in a few points.



Evidence: PersonService imports, lines 3 to 10; constructor,

lines 36 to 50.

### 2.5 High Cohesion

PersonService - Verdict: correct.

The class concentrates responsibilities related to managing

customers and sellers: registering them, finding them,

validating them and coordinating their storage.

Evidence: PersonService, lines 22 to 184.

### 2.6 Polymorphism

PersonService - Verdict: correct.

It uses instanceof to identify whether a Person object is a

Customer or a Seller and acts according to its type.

Evidence: PersonService constructor, lines 44 to 46.

### 2.7 Pure Fabrication

PersonService - Verdict: correct.

It organizes customers and sellers and delegates storage to

PersonPersistence instead of mixing responsibilities.

Evidence: PersonService.saveAll, lines 178 to 184.

### 2.8 Indirection

PersonService - Verdict: correct.

The user interface does not access PersonPersistence directly;

it goes through PersonService first.

Evidence: PersonService, lines 42 to 182.

### 2.9 Protected Variations

PersonService - Verdict: correct.

Changes in how people are stored stay protected behind

PersonService and PersonPersistence, so other parts do not

depend on those details.

Evidence: PersonService and PersonPersistence, lines 42 to 182.

## 3\. Violation template

Copy this table once for each violation (V-01, V-02, ...).

|Field|Content|
|-|-|
|ID|V-01|
|Pattern||
|Module||
|Evidence|Class, method and code fragment or exact lines in `develop`|
|Explanation||
|Consequence||
|Proposed solution||
|Code of another developer||



