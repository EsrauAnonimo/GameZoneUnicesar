
# GameZone Unicesar - Team

## Team Members


| Esteban Vergara -Technical Leader  -Sales 
| Luis Gomes -Developer 1 -Products 
| Cristian Perez -Developer 2 -People 

## Module Responsibilities

### Esteban Vergara - Technical Leader

**Module:** Sales

**Assigned classes:**
- Sale
- SalePersistence
- SaleService

**Activities:**
- Implement the Sales module.
- Implement the sale persistence.
- Implement the business rules related to sales.
- Coordinate the integration of the three modules.
- Review Pull Requests.
- Coordinate the Git and GitHub workflow.
- Ensure that the project follows the layered architecture.

**Feature branch:**
`feature/sale-module`

---

### Luis Gomes - Developer 1

**Module:** Products

**Assigned classes:**
- Product
- VideoGame
- Console
- ProductPersistence
- ProductService

**Activities:**
- Implement the Product hierarchy.
- Implement product persistence.
- Implement the business rules related to products.
- Ensure that the Products module follows the layered architecture.

**Feature branch:**
`feature/product-module`

---

### Cristian Perez - Developer 2

**Module:** People

**Assigned classes:**
- Person
- Customer
- Seller
- PersonPersistence
- PersonService

**Activities:**
- Implement the Person hierarchy.
- Implement Customer and Seller.
- Implement people persistence.
- Implement the business rules related to people.
- Ensure that the People module follows the layered architecture.

**Feature branch:**
`feature/person-module`

## Git Workflow

The project will use the following branch structure:

- `main`: stable versions of the project.
- `develop`: integration branch.
- `feature/*`: branches used for individual development.

Each team member will work on their assigned feature branch. Changes will be integrated into `develop` through Pull Requests after review by another team member.

Direct commits to `main` and `develop` will not be allowed during the development workflow.

## Collaboration and Integration

Each developer is responsible for implementing their assigned module according to the four-layer architecture.

The Technical Leader is responsible for the Sales module, project integration, and coordination of the Git and GitHub workflow.

Pull Requests must be reviewed and approved by another team member before being merged into `develop`.
