# Quick Question

Quick Question is a JavaFX component and demo application for embedding web-based LLM chat interfaces.

## `WebChatView` component

`ste.ai.qq.WebChatView` is a self-contained JavaFX component that displays a `ButtonBar` service selector and an embedded `WebView`. Add it to a scene like any other node:

```java
WebChatView webChatView = new WebChatView();
webChatView.webChatSetProperty().set(defaultProviders);
```

The list of available services is exposed through the `webChatSetProperty` property:

```java
webChatView.webChatSetProperty(myListProperty);
```

When the user clicks a service button, the component loads the associated URL and shows a warning label if Google login is not supported for that provider.

The component can also be embedded directly in FXML by importing its class:

```xml
<?import ste.ai.qq.WebChatView?>
...
<WebChatView fx:id="webChatView" />
```

Optional constructors allow a custom default URL and/or a custom WebView session storage directory:

```java
new WebChatView()                                 // default storage, no default URL
new WebChatView(storageDirectory)                 // custom storage
new WebChatView(defaultUrl, storageDirectory)     // default URL + custom storage
```

## Cookie Management

The `WebChatView` component persists WebView local storage across JVM restarts, but it does **not** persist HTTP cookies by itself. If the hosting application needs cookies to survive a restart (for example, to keep a user logged in), the container must install a suitable `java.net.CookieHandler`.

For convenience, Quick Question provides a simple persistent cookie manager/storage implementation under `ste.ai.qq.cookies`:

- `WebChatCookieManager` – a `java.net.CookieManager` that delegates to `WebChatCookieStore`.
- `WebChatCookieStore` – a `java.net.CookieStore` that saves cookies as JSON and reloads them when created.

Capabilities of the provided cookie manager/storage:

- Reads previously saved cookies from disk when the store is instantiated.
- Writes cookies to disk whenever the store is modified (add/remove/removeAll).
- Skips expired cookies when saving and loading.
- Stores cookies in a file named `cookies.json` inside the configured directory.
- Registers a JVM shutdown hook to flush the current cookie state on normal exit.

The demo application (`ste.ai.qq.demo.DemoApplication`) shows how to use this implementation by installing `WebChatCookieManager` at startup, using the same user data directory as the WebView local storage.

> **Note:** The cookie manager is installed at JVM level through `CookieHandler.setDefault(...)`. Once installed, all HTTP requests made through the `java.net` API will use it.

## For developers

### Architecture overview

Quick Question does not embed a browser directly inside the JavaFX scene graph. Instead, it runs the browser inside a **local VNC server** and embeds a **VNC client** (`VNCViewerFX`) that streams the remote desktop into the application UI.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Host JavaFX Application                       │
│                                                                       │
│  ┌─────────────────┐    ┌───────────────────────────────────────┐   │
│  │   Provider Bar  │    │          VNCViewerFX (client)         │   │
│  │  [Anthropic]    │    │                                       │   │
│  │  [ChatGPT]      │    │   (streams display :5 from localhost) │   │
│  │  [Perplexity]   │    │                                       │   │
│  └─────────────────┘    └───────────────────────────────────────┘   │
│           │                          │                                │
│           │ navigateTo(url)         │ RFB protocol                   │
│           ▼                          ▼                                │
│  ┌───────────────────────────────────────────────────────┐          │
│  │                  WebChatService                        │          │
│  │  - launches / stops the browser session                │          │
│  │  - tracks the VNC server PID                           │          │
│  │  - delegates to launch-browser.sh / stop-browser.sh    │          │
│  └───────────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              │ executes
                              ▼
                ┌──────────────────────────┐
                │   launch-browser.sh      │
                │                          │
                │  1. starts Xvnc on :5    │
                │  2. waits for VNC ready  │
                │  3. launches browser     │
                │  4. writes Xvnc PID      │
                └──────────────────────────┘
                              │
                              ▼
                ┌──────────────────────────┐
                │    Xvnc (VNC server)     │
                │    display :5, port 5905  │
                └──────────────────────────┘
                              │
                              ▼
                ┌──────────────────────────┐
                │   Browser (kiosk mode)   │
                │   navigates to LLM URL   │
                └──────────────────────────┘
```

### VNC server

The VNC server used is **TigerVNC** (`Xvnc`). It is responsible for creating a virtual X display (by default `:5`, TCP port `5905`) on which the browser runs. The `VNCViewerFX` component connects to `localhost:5905` and renders the remote desktop inside the JavaFX application.

`launch-browser.sh` looks for the `Xvnc` binary in the following order:

1. A bundled TigerVNC distribution next to the extracted scripts:
   `<dataDir>/quickquestion/tigervnc-1.16.2.x86_64/usr/bin/Xvnc`
2. The system `PATH` (`Xvnc`).

If neither is available, the script exits with an error.

### Scripts and data locations

At runtime, `WebChatService` extracts the bundled shell scripts from the classpath (`src/main/resources/bin/`) into the user data directory:

```
<dataDir>/quickquestion/
├── bin/
│   ├── launch-browser.sh
│   └── stop-browser.sh
└── tigervnc-1.16.2.x86_64/   (optional bundled VNC distribution)
    └── usr/bin/Xvnc
```

- `<dataDir>` is provided by `dev.dirs` (`BaseDirectories.get().dataDir`), typically `~/.local/share` on Linux.
- The PID file is written to the system temp directory: `<tmpDir>/quickquestion/browser.pid`.

### Browser lifecycle

`WebChatService` owns the browser session entirely from Java:

- **`navigateTo(URL)`**: if a session is already running, it is stopped first; then `launch-browser.sh` is invoked to start a fresh session.
- **`launch(url)`**: runs `launch-browser.sh`, which starts the VNC server, waits until the display is ready (`xset q`), and launches the system default browser in kiosk mode on that display. The VNC server PID is written to the PID file.
- **`stop()`**: reads the VNC server PID, sends `SIGTERM` to the entire process tree (VNC server + browser descendants), waits up to 10 seconds, and removes the PID file. This ensures the browser window is gracefully closed before a new session begins.
- **`isRunning()`**: checks whether the PID file exists and whether the tracked PID is still alive.

Because every navigation stops the previous session first, there is always at most one VNC server and one browser window running. This avoids profile-lock issues and stale browser state.

### Why a real browser via VNC?

Embedding a modern browser directly in JavaFX would require a toolkit that keeps pace with Chrome/Firefox/Edge releases, which is difficult in practice. JavaFX's built-in `WebView` uses an older WebKit version that cannot reliably render and interact with modern LLM web chat applications. Other embedded-browser options (e.g. JCEF) pull in large native dependencies and still lag behind the system browser.

By running the **system default browser** inside a local VNC session, Quick Question benefits from:

- A **real, up-to-date browser** with full JavaScript, cookie, and security support.
- **No WebKit/Chromium Embedded Framework version mismatch** — the host's browser is used as-is.
- **Same behavior as the user's normal browser**, including extensions, saved passwords, and login state.
