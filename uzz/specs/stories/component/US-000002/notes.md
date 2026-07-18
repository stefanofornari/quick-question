# US-000002 Implementation Notes

## Overview
Implementation of the user story "Select an LLM from a configured list".

## Technical Decisions

### Selection Handling Mechanism
- **Problem:** The original implementation used `ComboBox.setOnAction(...)` to handle selection changes. This event only fires on user interaction and not on programmatic selection changes made via `getSelectionModel().select(...)`.
- **Decision:** Replaced `setOnAction` with a `ChangeListener` on `selector.getSelectionModel().selectedItemProperty()`. This fires for both user and programmatic selection changes, making the component behavior predictable and testable.

### Auto-Select Behavior
- **Problem:** `setEntries(...)` originally auto-selected the first item, which implicitly triggered navigation. This blurred the line between configuration loading (US-000003) and user selection (US-000002).
- **Decision:** Removed the auto-select logic from `setEntries(...)`. Population of the selector is now purely a data operation. Initial navigation is handled by `onDisplayed()`.

### Initial Navigation
- **Decision:** Refactored `onDisplayed()` to:
  - Load the explicit `defaultUrl` when provided (US-000001 behavior).
  - Otherwise, select the first configured entry, which triggers the selection listener and navigates to its URL.
- **Rationale:** This keeps initial page loading and user selection navigation flowing through the same mechanism, keeping the selector and webview in sync.

## Edge Cases Handled
- **Empty configuration:** `onDisplayed()` does nothing; selector remains empty; webview remains on its current (blank) page; failure label stays hidden.
- **Invalid URL:** Navigation failure state is still shown via the existing `navigateTo(...)` logic.
- **No default URL with entries:** The first entry is selected and its URL is loaded.

## Files Modified
- `src/test/java/ste/ai/qq/WebChatViewTest.java` — added acceptance-criteria tests.
- `src/main/java/ste/ai/qq/WebChatView.java` — refactored selection handling and initial navigation.

## Test Results
- `WebChatViewTest`: 4/4 passing.
- `DemoViewControllerTest`: 3/3 passing.
- `WebChatViewControllerTest`: 1/1 passing.
- **Total: 8/8 passing.**

## Network Isolation
- Added WireMock (`org.wiremock:wiremock:3.9.1`) as a test dependency.
- Refactored `WebChatViewTest` to use local WireMock URLs instead of real LLM sites.
- Updated `uzz/specs/development-framework.md` to mandate WireMock for external HTTP stubs in tests.

## Trade-offs
- Programmatic selection now also triggers navigation. This is desirable for testability and consistency but means callers should be aware that `select(...)` on the selector will load the corresponding URL.
- The selector selection is only updated on `onDisplayed()` when no default URL is provided. When a default URL is provided that does not match any entry, the selector shows no selection while the webview loads the default page. This is acceptable within current scope and avoids over-engineering matching logic.
