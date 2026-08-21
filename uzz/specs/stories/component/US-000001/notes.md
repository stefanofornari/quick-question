# Notes for US-000001

## Technical Decisions
- Implement the component (WebChat) as a reusable JavaFX control that owns a
  VNCViewer connecting to a local VNC server running the browser
- This solution is the result of exploring many alternatives, none able to fulfil
  all requirements or constraints. In particular:
  - JavaFX Web view: bundled with JavaFX 23 (but even 25) embeds a not enough
    recent version of WebKit, resulting in not being able to properly render and
    handle modern AI WebChat
  - About other options:
    - JxBrowser - not free
    - [JCEF](https://github.com/chromiumembedded/cef): this is fundamentally an
      extension to the Chromium Embedded Framework with some native gluing to be
      used within Java. However it downloads an additional chrominium engine
      even if Chrominium is already installed, there is no reliable documentation
      on how to use it and embed it in a java application, and the test done was
      not successful.
    - [avaje-webview](https://github.com/avaje/avaje-webview): interesting project
      that implement a modern WebView toolkit in Java; however it is a standalone
      application and the author does not seem interested in making a JavaFX or
      swing component out of it.
    - We
- The component embeds a VNC viewer (`VNCViewerFX`) that connects to a local
  VNC server (e.g. `:5`). The actual browser runs on that display, managed
  by `WebChatService` via shell scripts.
- `WebChatService` handles browser lifecycle: launch, redirect, stop. It
  delegates platform-specific work to scripts but keeps process management
  (PID tracking, graceful kill) in Java for cross-platform portability.
- Scripts use named options (`--display`, `--pid-file`, `--profile-dir`,
  `--browser-bin`, `--geometry`, `--dry-run`) for clarity and robustness.
- Navigation failure is represented with a lightweight in-component label
  instead of throwing exceptions.
- Keep navigation logic separate from configuration loading where practical.
- Use a small API surface so the component can be embedded in host JavaFX applications.

## Trade-offs
- The first iteration focuses on navigation and failure feedback only.

