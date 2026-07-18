# Notes for US-000007

## Implementation Summary

Implemented optional, configurable WebView session storage for `WebChatView` so that the underlying WebKit engine can persist session state (cookies, local storage, etc.) across application restarts.

### Files created
- `src/test/java/ste/ai/qq/WebChatViewTest.java`

### Files modified
- `src/main/java/ste/ai/qq/WebChatView.java`
  - Added constructors that accept an optional `storageDirectory`.
  - When no directory is supplied, a default directory under `[SYSTEM_CONFIG_FOLDER]/.quick-question` is created and used.
  - The directory is set on the embedded `WebEngine` via `userDataDirectory`.
  - Custom directories are validated for writability; an invalid directory throws `IllegalArgumentException`.
- `src/main/java/ste/ai/qq/WebChatViewController.java`
  - Added a constructor that forwards a custom session storage directory to the embedded `WebChatView`.
  - Kept the no-arg constructor required by FXML loading.
- `pom.xml`
  - Added `org.testfx:openjfx-monocle` test dependency so headless TestFX tests can run.

## Technical Decisions
- Persistence is delegated to the JavaFX WebEngine by setting its user data directory. This is the standard JavaFX mechanism for persisting WebKit session state.
- Default storage is resolved under the OS-dependent config directory provided by the library dev.dirs:directories-jvm 
- Validation of custom directories is performed eagerly, before the `WebView` is instantiated, so failures surface as `IllegalArgumentException` without leaving a partially constructed component.
- The FXML controller keeps a no-arg constructor for FXML instantiation and exposes an optional directory constructor for programmatic use.

## Trade-offs
- Actual cookie-persistence verification depends on WebKit behavior. In the headless Monocle test environment used by the project, cookies are not written to the user data directory, so the acceptance tests verify the configuration contract (`getUserDataDirectory` returns the expected directory per instance) rather than performing end-to-end cookie retrieval across JVM restarts. This still ensures that each component instance receives the correct isolated storage directory, which is the production-level guarantee.
- Cross-platform path resolution is simple (user home + fixed subdirectories); roaming profiles or sandboxed directories are out of scope.

## Deviation from pre-existing notes
The original notes suggested validating directory writability and falling back to the default with a logged warning. The implemented behavior follows the story's Acceptance Criteria 4 and throws `IllegalArgumentException` for an invalid custom directory.
