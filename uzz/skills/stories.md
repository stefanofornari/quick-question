# Skill: Generate User Stories from Scope

## Goal
Analyze a `scope.md` file and break down its functional domains into actionable, INVEST-compliant user stories with BDD (Given/When/Then) acceptance criteria.

## Input Requirements
- A `scope.md` file containing high-level features, functional domains, target systems, and out-of-scope items.

## Rules & Standards
1. **INVEST Principle:** Every user story must be Independent, Negotiable, Valuable, Estimable, Small (1-3 days of work), and Testable.
2. **Format:**
   - Story: `AS A [role], I WANT [capability], SO THAT [value/benefit]`
   - Acceptance Criteria: BDD / Gherkin format (`Given / When / Then`).
3. **Respect Scope Boundaries:** Do NOT generate stories for items explicitly listed under "Out-of-Scope" or "Future Enhancements."

## Execution Steps

### Step 1: Parse & Map Domains
- Read `scope.md`.
- Extract the list of functional domains and core capabilities.
- Identify the target user roles (e.g., Admin, Customer, System).

### Step 2: Story Decomposition
- For each domain, decompose large features into small, vertical slices (thin end-to-end functionality, not horizontal layers like "build DB").
- Assign a clear title and unique identifier to each story (e.g., `[DOMAIN]/US-[NUMBER]`) - where number must
  be zero padded to 6 digits
- Use `user` in AS A [user] as defined users.md accordingly to the intended actors involved in the functionality

### Step 3: Generate Output
Output the user stories using the following template for each domain, each US
in its own file located under [DOMAIN]/[US-ID]/[US-ID].md:

---
# User Stories for Domain: [Domain Name]

## [US-00001] Title
**As a** [User Role],
**I want** [Specific Action/Capability],
**So that** [Business Value / Reason].

### Technical Context & Constraints
- **Target System:** [e.g., Spring Boot / Web / Mobile]
- **Toolchain / Rules:** [Refers to coding standards/frameworks defined in scope.md]

### Acceptance Criteria
```gherkin
Scenario: [Happy path scenario title]
  Given [initial context]
  When [action taken]
  Then [expected outcome]

Scenario: [Edge case or error handling title]
  Given [initial context]
  When [invalid or edge case action taken]
  Then [expected error or rollback]
