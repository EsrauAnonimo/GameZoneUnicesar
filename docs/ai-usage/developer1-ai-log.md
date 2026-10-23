# AI Usage Log - Developer 1

## Phase 1 - Audit
- **Prompt:** "I need to audit my code for GRASP violations. Can you explain how `instanceof` in my `CategoryDiscount` class violates polymorphism?"
- **Result:** The AI explained that using `instanceof` forces the class to know all subclasses. I verified this in my code and documented it as violation V-01 and V-03.
## Phase 2 - Correction
- **Prompt:** "How can I remove the instanceof checks in CategoryDiscount and replace them with a polymorphic approach using @Override?"
- **Result:** The AI suggested adding a getCategory() method to Product and overriding it in all subclasses. I applied this pattern to resolve violations V-02 and V-04.