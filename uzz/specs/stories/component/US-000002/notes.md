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
  - if no browser has been already launched, a new browser is launched in the screen
    shared by the VNCServer (e.g. :5) and the VNC server's pid is saved in the system tmp folder
  - if a browser has already been launched - either for the same or another provider,
    the existing session is stopped and a new browser session is launched for the provider Web Chat site.

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
  - `navigateTo(URL)` always launches a fresh browser session: if a session is already running, it is stopped first, then a new one is launched.
  - `isRunning()` checks the PID file and delegates liveness to a `ProcessAliveChecker` (defaults to `ProcessHandle`), making the service fully testable without real browser processes.
  - `stop()` performs a graceful tree kill of the tracked process and clears the PID file.
- **Rationale:** Tests mock the scripts by providing a temporary `binDir` with stub scripts; no real browser or VNC session is needed for unit tests.

### Script Contracts
- `launch-browser.sh <url> [--display <display>] [--pid-file <pid-file>] [--profile-dir <profile-dir>] [--browser-bin <browser-bin>] [--geometry <WxH>] [--dry-run]`: starts the VNC server on the given display, waits for it to be ready, then launches the first available browser in kiosk mode. Writes the VNC server PID to `pid-file`. `--geometry` is translated per browser family (Firefox: `--width`/`--height`; Chromium: `--window-position=0,0 --window-size=W+1,H+1`). Default geometry is `600x800`.
- `stop-browser.sh <pid-file>`: cleanup hook; removes the PID file. Actual process termination is handled by `WebChatService.stop()` via `ProcessHandle.destroy()` / `destroyForcibly()`.

### Browser Process Management (updated)
- `WebChatService` owns the browser lifecycle in Java.
- `navigateTo()` stops any existing session before launching a new one. This ensures the VNC server and browser are gracefully terminated before a new session starts.
- `stop()` performs a graceful tree kill: snapshot descendants, send SIGTERM to all, wait up to 10s, escalate to SIGKILL if needed.

### Testability
- The `ProcessAliveChecker` abstraction allows tests to stub liveness checks without relying on real OS processes, avoiding sandbox/container restrictions on backgrounded child processes.
- Mock scripts write marker files (`*.launched`) alongside the pid file so tests can assert which script was invoked.

### Trade-offs
- Navigation is implemented by stopping the existing session and launching a fresh one via `launch-browser.sh`, rather than using browser-specific remote-control protocols or re-invoking the browser binary. This is pragmatic for Linux kiosk deployments where the VNC server lifecycle is managed alongside the browser.
- `WebChatException` is an unchecked exception to keep the service API simple; callers may catch it if they need to surface launch/navigation failures.