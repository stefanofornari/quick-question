# QuickQuestion

`QuickQuestion` is:

- a JavaFX component
- a demo application
- a NetBeans plug-in

for using web-based LLM chat interfaces inside a JavaFX or Swing application.

## `WebChat` Control

`ste.ai.qq.WebChat` is a self-contained JavaFX component that displays a native
browser (i.e. the reall browser used by default by the user) as a JavaFX node.
It additionally provides a `ButtonBar` selector to pick which one to use amongst
the most known IA WEbChat service.

When the user clicks a service button, e.g. ChatGPT, the component loads its web
interface so the user can interact with it.

The component can be embedded directly in FXML by importing its class and using
`WebChat` descriptor:

```xml
<?import ste.ai.qq.WebChat?>
...
<WebChat fx:id="webChat" />
```

## How It Works
Using modern web chat web applications in Java is technically quite challenging
because they use the most advanced web protocols and applications that franckly
only native modern browsers provide. `QuickQuestion' runs a real browser (the
default browser in your system) completely hidden to the user and in a virtual
desktop in a VNC server. `WebChat` connects to the VNC server and show the content
of the screen inside the component area. The component is fully wired to the
real browser so the interaction is smooth like you would interact directly with
Firefox or Chrome.

## Supported browsers and Systems
`QuickQuestion' currently supports **Linux x86_64** systems and **Firefox* and
**Chrome/Chrominium** as browsers.

## For developers

### Architecture overview

'QuickQuestion' does not embed a browser directly inside the JavaFX scene graph.
Instead, it runs the browser inside a **local VNC server** and embeds a **VNC client**
(`VNCViewerFX`) that streams the remote desktop into the application UI.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Host JavaFX Application                      │
│                                                                     │
│  ┌─────────────────┐    ┌───────────────────────────────────────┐   │
│  │   Provider Bar  │    │          VNCViewerFX (client)         │   │
│  │  [Anthropic]    │    │                                       │   │
│  │  [ChatGPT]      │    │   (streams display :5 from localhost) │   │
│  │  [Perplexity]   │    │                                       │   │
│  └─────────────────┘    └───────────────────────────────────────┘   │
│           │                          │                              │
│           │ navigateTo(url)          │ RFB protocol                 │
│           ▼                          ▼                              │
│  ┌────────────────────────────────────────────────────────┐         │
│  │                  WebChatService                        │         │
│  │  - launches / stops the browser session                │         │
│  │  - tracks the VNC server PID                           │         │
│  │  - delegates to launch-browser.sh / stop-browser.sh    │         │
│  └────────────────────────────────────────────────────────┘         │
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
                │   display :5, port 5905  │
                └──────────────────────────┘
                              │
                              ▼
                ┌──────────────────────────┐
                │   Browser (kiosk mode)   │
                │   navigates to LLM URL   │
                └──────────────────────────┘
```

### VNC server

The VNC server used is **TigerVNC** (`Xvnc`). It is responsible for creating a
virtual X display (by default `:5`, TCP port `5905`) on which the browser runs.
The `VNCViewerFX` component connects to `localhost:5905` and renders the remote
desktop inside the JavaFX application.

`launch-browser.sh` looks for the `Xvnc` binary in a bundled TigerVNC
distribution next to the extracted scripts:
   `<dataDir>/quickquestion/tigervnc-<version>/usr/bin/Xvnc`

If Xvnc is not there already, `QuickQuestion` installs it from a stripped down
version of TigerVNC v1.16.2.x86_64 bundled in the dependency `com.github.stefanofornari:mini-tigervnc`.

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

Embedding a modern browser directly in JavaFX would require a toolkit that keeps
pace with Chrome/Firefox/Edge releases, which is difficult in practice. JavaFX's
built-in `WebView` uses an older WebKit version that cannot reliably render and
interact with modern LLM web chat applications. Other embedded-browser options
(e.g. JCEF) pull in large native dependencies and still lag behind the system
browser.

By running the **system default browser** inside a local VNC session, `QuickQuestion`
benefits from:

- A **real, up-to-date browser** with full JavaScript, cookie, and security support.
- **No WebKit/Chromium Embedded Framework version mismatch** — the host's browser is used as-is.
- **Same behavior as the user's normal browser**, including extensions, saved passwords, and login state.
