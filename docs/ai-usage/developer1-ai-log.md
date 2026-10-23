# AI Usage Log - Developer 1

## Phase 1 - Audit
- **Prompt:** "I need to audit my code for GRASP violations. Can you explain how `instanceof` in my `CategoryDiscount` class violates polymorphism?"
- **Result:** The AI explained that using `instanceof` forces the class to know all subclasses. I verified this in my code and documented it as violation V-01 and V-03.
## Phase 2 - Correction
- **Prompt:** "How can I remove the instanceof checks in CategoryDiscount and replace them with a polymorphic approach using @Override?"
- **Result:** The AI suggested adding a getCategory() method to Product and overriding it in all subclasses. I applied this pattern to resolve violations V-02 and V-04.

## Phase 1 - SOLID Audit
- **Prompt:** "How can I explain the difference between SRP and DIP in my ProductService class?"
- **Result:** The AI explained that SRP is about the class having one reason to change, while DIP is about depending on abstractions. I used this to write my audit findings for V-01 and V-03.