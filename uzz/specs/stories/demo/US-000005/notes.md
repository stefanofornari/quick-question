# US-000005 Notes

## Technical decisions
- The demo UI uses FXML for both the main view and the embedded web chat component, following the project development framework.
- The reusable `WebChatView` component now exposes a `ListProperty<WebChat> entriesProperty` and binds the selector items to it, following the JavaFX property pattern.
- The demo controller uses a single `ObservableList<WebChat>` as the model, shares it with the embedded component via `entriesProperty().set(entries)`, and uses it as the `TableView` items list.
- Because the component and the demo share the same observable list, additions to the model are immediately reflected in the dropdown without requiring an explicit Apply.

## Design choices
- Kept `handleApplyChanges()` as an explicit synchronization point to satisfy the acceptance criteria wording, even though the shared observable list already keeps the dropdown up to date.
- Removed `setEntries(List<WebChat>)` from `WebChatView` and `WebChatViewController`; callers should use the `entriesProperty` directly.
- Removed the manual `entriesTable.getItems().setAll(entries)` calls in favor of `entriesTable.setItems(entries)`.

## Trade-offs
- The Apply button is now technically redundant for the dropdown update but is retained for explicit user confirmation and to satisfy the story's `When the user applies the changes` step.
- Sharing the same observable list between the demo model and the embedded component simplifies synchronization but means the component's entries property must be treated as a view into the caller's model.

## Verification
- Target acceptance criteria: existing entries appear in the listbox; newly added entries appear in the dropdown after Apply; multiple additions are reflected in order.
- Added `demo_application_updates_dropdown_dynamically_on_add` to verify the dropdown updates immediately when an entry is added, demonstrating the property-based dynamic link.
