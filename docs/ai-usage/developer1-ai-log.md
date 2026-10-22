# AI Usage Log - Developer 1

## Phase 1 - Audit
- **Prompt:** "I need to audit my code for GRASP violations. Can you explain how `instanceof` in my `CategoryDiscount` class violates polymorphism?"
- **Result:** The AI explained that using `instanceof` forces the class to know all subclasses. I verified this in my code and documented it as violation V-01 and V-03.