# US-000002 Implementation Notes

## Overview
Implementation of the user story [US-000002] Select an LLM from a preconfigured list

## Technical Decisions

### Native Browser
- **Problem:** How to we navigate to the provider url and show the chat web site?
- **Decision:** We use VNCViewerFX, which means a FX component that shows and
  interacts with a remote desktop. This means that there is a VNCServer runnign
  on the local machine (for now) and the viewer embedded in teh QuickQuestion
  UI. When a provider is selected:
  - if no browser has been already launched, a new browser is launched in the screeen
    shared by the VNCServer (e.g. :5) and its pid is saved in the system tmp folder
  - if a browser as already launched - either for the same or another provider,
    the browser is redirected to the provider Web Chat site.

### Browser Process Management
- **Problem:** How to we launch and manage browser's process?
- **Decision:** WebChatService
  - Create a WebChatService class that run systemprocesses as needed
  - WebChatService shall also be able to track and detect the current status (e.g.
    save/retrieve PID, retrieve if the process is running, etc...)
  - Build as much as possible the logic in the service for the sake of platform
    independency, but use system scripts whenever appropriate
  - WebChatService shall spawn the default browser configured on the system
  - The browser shall be launched in full-screen kiosk mode

### Target Systems:
- **Problem:** Which operating systems shall be supported
- **Solution** Linux only for now

### WebChatService Implementation
- **Problem:** How to keep browser process management platform-independent while still leveraging native capabilities?
- **Decision:** WebChatService owns the browser lifecycle entirely in Java but delegates actual browser launching, navigation, and termination to bash scripts in a configurable `binDir`.
  - Default `binDir` is resolved via `dev.dirs` (`BaseDirectories.get().configDir`) as `<configDir>/quickquestion/bin`.
  - Default PID file lives under the system tmp dir: `<tmpDir>/quickquestion/browser.pid`.
  - Default VNC display is `:5`, configurable via constructor.
  - `navigateTo(URL)` launches the browser if `isRunning()` is false, otherwise redirects.
  - `isRunning()` checks the PID file and delegates liveness to a `ProcessAliveChecker` (defaults to `ProcessHandle`), making the service fully testable without real browser processes.
  - `stop()` invokes the stop script and clears the PID file.
- **Rationale:** Tests mock the scripts by providing a temporary `binDir` with stub scripts; no real browser or VNC session is needed for unit tests.

### Script Contracts
- `launch-browser.sh <display> <pid-file> <url>`: launches the first available browser (`firefox`, `chromium`, `google-chrome`, `google-chrome-stable`, `chromium-browser`) in kiosk mode on the given display, writes the browser PID to `pid-file`.
- `navigate-browser.sh <display> <pid-file> <url>`: re-invokes the detected browser binary on the same display with the new URL (singleton browsers open the URL in the existing kiosk instance).
- `stop-browser.sh <pid-file>`: terminates the browser process recorded in the pid file and removes it.

### Testability
- The `ProcessAliveChecker` abstraction allows tests to stub liveness checks without relying on real OS processes, avoiding sandbox/container restrictions on backgrounded child processes.
- Mock scripts write marker files (`*.launched`, `*.navigated`) alongside the pid file so tests can assert which script was invoked.

### Trade-offs
- Navigation is implemented by re-invoking the browser binary rather than using browser-specific remote-control protocols; this is pragmatic for Linux kiosk deployments where browsers typically handle subsequent invocations as singleton navigation requests.
- `WebChatException` is an unchecked exception to keep the service API simple; callers may catch it if they need to surface launch/navigation failures.