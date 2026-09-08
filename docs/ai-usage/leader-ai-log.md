# AI Usage Log - Technical Lead (Esteban Vergara)

## Git and GitHub
Used AI mainly for basic Git commands throughout the project setup: cloning the repository, creating and pushing branches (develop, feature/sale-module), branch protection configuration, and the commit/push workflow. Also used it to fix a specific issue: a teammate got a 403 error trying to push their feature branch, which AI helped diagnose as a missing-collaborator permission issue.

## Design decisions
No design decisions were asked directly to the AI. All architectural and business-rule decisions came from the team's own discussion in analysis.md. AI was used to translate those already-made decisions into Mermaid diagram syntax (hierarchy, class, and layers diagrams) and to review a teammate's Person class submission for consistency with the approved class-diagram.md.

## Java implementation
AI explained the reasoning and structure needed for each class in the sales module (Sale, SalePersistence, SaleService, Main) — for example, why serialization was chosen for persistence, why stock validation belongs in the service layer instead of the model (based on our own answer to analysis question 8), and how try/catch works for file I/O. I wrote and adapted the actual code myself based on those explanations, and I'm able to explain the logic behind each class.

## What I did NOT ask AI to do
- Did not ask AI to answer the analysis.md questions — those were discussed and written by the team beforehand.
- Did not ask AI to generate the design diagrams from scratch — they were built from our own analysis answers, and AI only helped with correct Mermaid syntax.
