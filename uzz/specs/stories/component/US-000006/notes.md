# Notes for US-000006

## Implementation Summary
Implemented a reusable `DefaultProviders` class plus an immutable `WebChatProvider` record.

### Files created
- `src/main/java/ste/ai/qq/WebChatProvider.java`
- `src/main/java/ste/ai/qq/DefaultProviders.java`
- `src/test/java/ste/ai/qq/DefaultProvidersTest.java`

### Technical Decisions
- `WebChatProvider` is a Java record with `name`, `url`, and `googleLogin` fields. Records provide immutability and concise, value-based semantics.
- `DefaultProviders` keeps the predefined provider list in a `private static final List` created with `List.of(...)`, which is naturally unmodifiable.
- `providers()` simply returns the predefined list. No JavaFX property is used at this level because the story only requires exposing the data; binding to a component property is the consumer's responsibility.
- The component itself remains agnostic of the predefined list, preserving its reusability.

### Trade-offs
- Only a single flag (`googleLogin`) is tracked per provider, matching the scope.
- The out-of-the-box list is intentionally limited to three providers as specified in the story.
- The list is shared across all instances. Because it is immutable, this is safe and memory-efficient.

### Test Approach
- TDD workflow: tests were written before production code.
- Tests verify provider count, names, URLs, `googleLogin` values, and list unmodifiability.
- All assertions use AssertJ BDD `then()` and snake_case test method names per project conventions.
