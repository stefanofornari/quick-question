# Development Framework

## Purpose
This document defines the development framework and workflow for the QuickQuestion project.

## Technology Stack
- Java
- JavaFX
- CommonsFX (`org.commonsfx`)
- AtlantaFX
- Maven
- JUnit 5
- TestFX
- AssertJ

## JavaFX
- use fxml descriptors whenever possible

## Testing Strategy
- Follow a TDD workflow.
  1. Write failing tests before implementation and check they fail
  2. Write the minimal production code to pass the test
  3. Run the test and refactor the code until it passes
  4. Check the scope is satisfied and if not reiterate from 1
- Use JUnit 5 for unit and integration tests.
- Use TestFX for JavaFX UI tests.
- Support headless execution for automated test runs.
- Use AssertJ BDD assertions with `then()`.
- Use WireMock to stub external HTTP endpoints; tests must not depend on real network services or live sites.

## Build and Execution
- Use Maven as the build tool.
- Keep the project build reproducible and automation-friendly.
- Ensure tests can run in CI without a graphical desktop session.

## Dependencies

### JavaFX
- `javafx-controls` — core UI controls
- `javafx-base` — base runtime classes
- `javafx-graphics` — graphics and windowing toolkit
- `javafx-media` — audio/video playback support
- `javafx-fxml` — FXML loading and declarative UI
- `javafx-web` — WebView engine (referenced but primarily VNC-backed in current implementation)
- `javafx-swing` — Swing interoperability (required for NetBeans JFXPanel embedding)

### UI/Theming
- `atlantafx-base` — modern, clean theme for JavaFX applications

### VNC / Remote Browser
- `vncviewerfx` — JavaFX VNC client component used to stream the remote desktop
- `tiger-vnc-mini` — bundled stripped-down Tiger VNC server binaries (`Xvnc`) auto-extracted at runtime

### Utilities
- `directories` (`dev.dirs`) — cross-platform user data directory discovery (`~/.local/share` on Linux)
- `zip4j` — archive extraction used by `VNCServerInstaller` to unpack the bundled VNC distribution

### Testing
- `junit-jupiter` — JUnit 5 test engine
- `assertj-core` — fluent assertions, including BDD-style `then()` assertions
- `testfx-junit5` / `testfx-core` — JavaFX UI testing framework
- `openjfx-monocle` — headless Glass platform for CI/automated TestFX runs
- `xtest` — internal test utilities

### NetBeans Module
- `org-openide-windows` — NetBeans window system and TopComponent API
- `org-openide-awt` — NetBeans Swing integration utilities
- `org-netbeans-api-annotations-common` — NetBeans annotation processors
- `org-netbeans-libs-javafx` — NetBeans-bundled JavaFX integration
- `org-openide-util` — NetBeans utility and lookup infrastructure
- `org-netbeans-modules-settings` — NetBeans settings persistence
