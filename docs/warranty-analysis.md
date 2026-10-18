# Warranty Module Analysis (Taller 4)

Technical decisions taken while integrating the warranty module into
GameZoneUnicesar. This document answers the design questions that came up while
wiring `SaleService`, `ConsoleUI` and `Main` together.

---

## 1. Why is the end date not stored in `data/warranties.csv`?

Because it is fully derivable. `Warranty` computes `endDate` in its constructor
as `startDate.plusMonths(getDurationInMonths())`, and every concrete warranty
returns a constant duration (6 or 12 months).

Storing it would create two sources of truth: a hand-edited or corrupted CSV
could claim a 12-month coverage on a `BASIC` line, and the model would disagree
with the file. Since the discriminator already determines the duration,
persisting only `startDate` guarantees the file and the model can never
disagree.

The same reasoning applies to `getAdditionalCost()`: it is recomputed from the
current product price rather than frozen at purchase time.

## 2. How is the circular dependency between `SaleService` and `WarrantyService` avoided?

The obvious wiring is impossible to construct:

```
SaleService -> WarrantyService -> WarrantyRepository -> SaleService
```

`WarrantyRepository` needs to resolve the `Sale` referenced by each stored
warranty, and the obvious choice is to ask `SaleService` for it. But
`SaleService` already depends on `WarrantyService`, which depends on the
repository, which would depend back on `SaleService`. Constructor injection
cannot build that graph.

The cycle is broken by having `WarrantyRepository` resolve sales through
`SalePersistence` instead. `SalePersistence` is a leaf: it reads
`data/sales.dat` and knows nothing about warranties. Both services therefore
depend inward on persistence and never on each other.

`WarrantyRepository` still uses `ProductService` and `AccessoryService` to
resolve the covered product, because both of those are leaves of the object
graph as well and accessories are not part of the product list.

## 3. Why does a console get one warranty and not two?

The requirement says a `Console` receives the basic warranty, and that a
console listed as "extended" receives the extended warranty. Taken literally, a
console that was paid for the extended coverage would end up with both a
`BasicWarranty` and an `ExtendedWarranty` for the same purchase.

That is ambiguous, and it breaks a required query. `findWarrantyByProduct`
returns a single `Warranty`; with two records the answer would depend on file
order, and `listActiveWarranties` would double-count the same console.

The rule implemented instead is: every console gets the basic warranty, and
when the customer buys the extended coverage the basic warranty is **replaced**
by the extended one through `WarrantyService.revokeWarrantyFor`. A purchase
therefore always has exactly one warranty per product, and the more expensive
coverage wins. This matches the commercial reading of an "extended" warranty: an
upgrade of the same product, not an additional product.

## 4. Why is the warranty cost added after the promotion instead of before?

Discounts exist to reward the customer on merchandise. If the warranty were
priced into the subtotal before `applyBestPromotion` ran, a percentage
discount would also discount the warranty, and a customer buying only a cheap
console with an expensive extended warranty could end up paying less than zero
for it after the discount is clamped to the subtotal.

Adding the cost after the promotion guarantees:
- a discount never reduces the price of a warranty;
- the discount is still clamped against the merchandise subtotal only;
- the arithmetic stays easy to audit: `total = subtotal - discount + warranties`.

The included basic warranty adds `0.0`, so sales without extended coverage are
numerically identical to what they were before this module existed.

## 5. Why do only consoles get a warranty?

`assignWarranties` in `SaleService` skips anything that is not an instance of
`Console`. Video games and accessories are registered through exactly the same
path as in Taller 3: stock validation, promotion, stock update and persistence.

This keeps the change additive. A sale of video games only produces no
warranty rows, so `data/warranties.csv` stays empty and no behaviour of the
previous workshops changes.

The check is on the runtime type rather than on a "is a console" flag, so any
future `Product` subclass is excluded by default instead of silently gaining a
warranty it should not have.

---

## Coordination notes for the other developers

The leader's part only calls methods that exist in the agreed warranty service
API. No extra method was introduced on top of it, so the pieces can be merged
in any order once the warranty model and persistence land:

| Called from | Method used |
| --- | --- |
| `SaleService` | `assignBasicWarranty(Product, Sale, LocalDate)` |
| `SaleService` | `assignExtendedWarranty(Product, Sale, LocalDate)` |
| `SaleService` | `findWarrantyByProduct(String, String)` |
| `SaleService` | `Warranty.getAdditionalCost()` |
| `ConsoleUI` | `findWarrantyByProduct(String, String)` |
| `ConsoleUI` | `listAllWarranties()` |
| `ConsoleUI` | `listActiveWarranties()` |
| `ConsoleUI` | `listWarrantiesExpiringSoon(int)` |
| `ConsoleUI` | `Warranty.generateWarrantyCertificate()` |
| `ConsoleUI` | `Warranty.getProduct()`, `getSale()`, `getEndDate()`, `getWarrantyType()` |
| `Main` | `new WarrantyRepository(SalePersistence, ProductService, AccessoryService)` |

Two coordination points that do need agreement:

1. **`WarrantyRepository`'s constructor signature.** This part builds it as
   `(SalePersistence, ProductService, AccessoryService)`. Dev 2 should either
   use that signature or announce a different one, because `Main` has to match.
2. **`assignBasicWarranty` / `assignExtendedWarranty` return values.** This
   part treats a `null` return as "the warranty was not created" and simply
   skips the cost, so the sale is still registered. Returning `null` for an
   invalid call is therefore safe; throwing is not.

---

## Known limitations

- This part does not compile on its own: `SaleService` and `ConsoleUI` reference
  `Warranty` and `WarrantyService`, which are contributed by the other two
  developers. Until those land, `javac` and `mvn` both report missing symbols,
  and that is expected rather than a defect.
- Behaviour was verified against a temporary stub of the warranty classes
  matching the agreed API, covering: console with extended warranty adds 10%,
  console without it gets the basic warranty at no cost, video games get no
  warranty, one warranty per product and sale, the file round-trips through a
  restart, and the four submenu queries. Once the real classes arrive those
  checks should be re-run, because a different constructor or return contract
  would change the outcome.
- `mvn clean package` has not been run in the environment where this module was
  written; verification was done with `javac --release 17`, matching the
  `maven.compiler.release` value in `pom.xml`, and through the real console
  application.
- The console main menu still calls `Integer.parseInt` without a `try/catch`, so
  a non-numeric answer kills the application. This is pre-existing behaviour
  from Taller 1 and was left untouched on purpose.
- `Product.getDescription()` and `Person.getFullSummary()` still print field
  names in English, so warranty listings mix Spanish labels with English model
  `toString()` output.
- Warranty identifiers are random (`GAR-XXXXXXXX`). They are unique but not
  sequential, so they cannot be used to infer creation order.