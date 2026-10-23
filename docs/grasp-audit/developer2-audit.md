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

ReturnService - Verdict: correct.

It creates the Return because it is the class that manages and

registers returns.

Evidence: ReturnService, line 994 (new Return).



WarrantyService - Verdict: correct.

It creates BasicWarranty and ExtendedWarranty because it is the

class that manages the creation of warranties.

Evidence: WarrantyService, lines 263 and 266.



AccessoryService - Verdict: correct.

It receives the accessories already created by the caller and

takes care of registering and storing them.

Evidence: AccessoryService, lines 782 to 795.



PromotionService - Verdict: correct.

It is the class that creates the concrete promotions

(PercentageDiscount, CategoryDiscount and BulkPurchaseDiscount).

Evidence: PromotionService, lines 229 to 241.



WarrantyRepository - Verdict: correct.

It creates WarrantyRecord because that object directly

represents the record the repository reads and stores.

Evidence: WarrantyRepository, line 353.

### 2.3 Controller

Verdict: correct.



PersonService is the first object after the UI that coordinates

the registerCustomer operation, and it does not mix console input

or output. The UI does not contain the business rules, and the

service knows nothing about how the result is displayed.



Evidence: PersonService.registerCustomer, lines 60 to 70. A search

of PersonService for Scanner, System.out and println found

No matches.

### 2.4 Low Coupling

PersonService - Verdict: correct.



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

ReturnRepository - Verdict: violation.

It depends directly on SaleService, ProductService and

AccessoryService, so it is tightly bound to those classes.

Evidence: ReturnRepository imports, lines 7 to 9.

WarrantyService - Verdict: correct.

It receives its dependencies through the constructor and uses

them to resolve the warranties.

Evidence: imports, lines 3 to 11; constructor, line 41.



AccessoryService - Verdict: correct.

It receives AccessoryPersistence and the list of consoles

through the constructor instead of creating them internally.

Evidence: imports, lines 3 to 12; constructor, line 38.



PromotionService - Verdict: correct.

It only depends directly on PromotionRepository for persistence

and receives it through the constructor.

Evidence: imports, lines 3 to 10; constructor, line 31.



WarrantyRepository - Verdict: correct.

It only handles warranty data persistence and does not depend on

Sale, Product or their services.

Evidence: import, line 3.



ReturnService - Verdict: violation.

It depends directly on several services (SaleService,

ProductService, AccessoryService and WarrantyService).

Evidence: ReturnService, lines 8 to 4.

### 2.5 High Cohesion

PersonService - Verdict: correct.

The class concentrates responsibilities related to managing

customers and sellers: registering them, finding them,

validating them and coordinating their storage.

Evidence: PersonService, lines 22 to 184.

ReturnRepository - Verdict: correct.

The class is mainly in charge of saving, loading and rebuilding

returns from the file.

Evidence: ReturnRepository.saveAll (lines 64 to 75), loadAll

(lines 85 to 107) and parseLine (lines 180 to 222).

ReturnService - Verdict: correct.

It concentrates the operations of returns, although it has

several external dependencies to complete them.

Evidence: ReturnService, lines 55 to 997.



WarrantyService - Verdict: correct.

It focuses on creating, finding and managing warranties.

Evidence: lines 25 to 160.



AccessoryService - Verdict: correct.

All its operations relate to administering accessories, their

stock and compatibility.

Evidence: lines 22 to 157.



PromotionService - Verdict: correct.

The whole class is focused on registering, querying and

evaluating promotions.

Evidence: lines 20 to 156.



WarrantyRepository - Verdict: correct.

Its only responsibility is saving and loading warranty records.

Evidence: lines 25 to 144. 

### 2.6 Polymorphism

PersonService - Verdict: correct.

It uses instanceof to identify whether a Person object is a

Customer or a Seller and acts according to its type.

Evidence: PersonService constructor, lines 44 to 46.

ReturnService - Verdict: violation.

It uses instanceof to decide whether the product is an

Accessory or a Console, instead of taking advantage of

polymorphism.

Evidence: ReturnService, lines 991 and 994.



WarrantyService - Verdict: violation.

It decides the warranty type with if statements comparing

TYPE\_BASIC and TYPE\_EXTENDED, instead of delegating the behavior

through polymorphism.

Evidence: WarrantyService, lines 262 to 266.



AccessoryService - Verdict: correct.

It works with Accessory and its subtypes without using

instanceof to decide which behavior to run.

Evidence: AccessoryService, lines 447 to 486.



PromotionService - Verdict: correct.

The concrete promotions work through the Promotion abstraction,

for example when calling calculateDiscount(sale), without

checking the type with instanceof.

Evidence: PromotionService.findBestPromotionFor, lines 1079 to 1084.



WarrantyRepository - Verdict: violation.

It uses if to distinguish between BASIC and EXTENDED; the

repository takes part in the type decision instead of leaving it

entirely to polymorphism.

Evidence: WarrantyRepository.parse, lines 353 to 365.

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
|Pattern|Low Coupling|
|Module|Returns (Req. 3)|
|Evidence|ReturnRepository imports, lines 7 to 9; attributes, lines 37 to 39; constructor, line 51.|
|Explanation|ReturnRepository depends directly on SaleService, ProductService and AccessoryService, so it is tightly bound to those classes.|
|Consequence|If SaleService, ProductService or AccessoryService change tomorrow, ReturnRepository may stop working or need changes; also, to test it on its own it would need those three services prepared with their data.|
|Proposed solution|ReturnRepository could directly receive the information it needs, for example the already loaded lists of Sale, Product and accessories, instead of asking SaleService, ProductService and AccessoryService to look them up.|
|Code of another developer|No.|
|||
|ID|V-02|
| Pattern|Low Coupling|
|Module|Returns (Req. 3)|
|Evidence|ReturnService, lines 80 to 84|
|Explanation |It depends directly on several services (SaleService, ProductService, AccessoryService and WarrantyService).|
| Consequence|If SaleService, ProductService, AccessoryService or WarrantyService change tomorrow, ReturnService could need changes and, to test it in isolation, those services would have to be prepared. |
|Proposed solution|One way to reduce the coupling would be for ReturnService to receive directly the data it needs, such as the already loaded sales, products and accessories, instead of depending directly on so many services to look them up. |
| Code of another developer|NO.|
|||
|ID|V-03|
|<br />Pattern|Polymorphism|
|Module|Returns (Req. 3)|
|Evidence|ReturnService, lines 991 and 992 (instanceof)|
|Explanation|It uses instanceof to decide whether the product is an Accessory or a Console, instead of taking advantage of polymorphism.|
|Consequence|It would have to be modified if new product types appear.|
|Proposed solution|Delegate the behavior of restoring stock to the classes themselves, avoiding instanceof.|
|Code of another developer|No.|
|||
|ID|V-04|
|Pattern|Polymorphism|
|Module|Warranties (Req. 4)|
|Evidence|WarrantyService, lines 262 to 266 (TYPE\_BASIC / TYPE\_EXTENDED)|
|Explanation|It decides the warranty type with if statements comparing TYPE\_BASIC and TYPE\_EXTENDED, instead of delegating the behavior through polymorphism.|
|Consequence|Each new warranty type would force adding more if statements.|
|Proposed solution|Use polymorphism so that each warranty determines its own behavior.|
|Code of another developer|No.|
|||
|ID|V-05|
|Pattern|Polymorphism|
|Module|Warranties (Req. 4)|
|Evidence|WarrantyRepository.parse, lines 353 to 365|
|Explanation|It uses if to distinguish between BASIC and EXTENDED; the repository takes part in the type decision instead of leaving it entirely to polymorphism.|
|Consequence|Each new warranty type would force modifying the repository.|
|Proposed solution|Delegate the creation of the warranty type through a factory or a polymorphic mechanism.|
|Code of another developer|No.|
|||



