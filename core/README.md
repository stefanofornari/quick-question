# QuickQuestion Core

Core JavaFX component and demo application for QuickQuestion.

## What's Inside

- `ste.ai.qq.WebChat` — embeddable JavaFX component that embeds a VNC viewer and a provider selector toolbar
- `ste.ai.qq.Provider` — enum of supported LLM providers
- `ste.ai.qq.WebChatService` — service that launches and manages a remote browser session via a bundled Tiger VNC server
- `ste.ai.qq.demo.QuickQuestionDemo` — demo application showcasing the component
- `ste.ai.qq.demo.VNCServerInstaller` — installs the bundled Tiger VNC binaries into the user data directory

## Building

```bash
mvn clean install
```

## Running the Demo

```bash
mvn -pl core javafx:run
```

Or run the packaged distribution:

```bash
java -jar target/quick-question-core-0.0.0-SNAPSHOT.jar
```

## Embedding the Component

Add `quick-question-core` as a dependency and import `ste.ai.qq.WebChat` in FXML:

```xml
<?import ste.ai.qq.WebChat?>
...
<WebChat fx:id="webChat" />
```

The component exposes a `WebChatController` that can be used to programmatically navigate to a provider URL.

## How It Works

QuickQuestion does not embed a browser directly. Instead, it runs the system default browser inside a local Tiger VNC session and streams the remote desktop into the JavaFX scene via `VNCViewerFX`.

```
Host JavaFX App
  ├── WebChat (VNC viewer + provider toolbar)
  │     └── WebChatService (launches/stops browser via VNC)
  └── Xvnc (:5) + Browser (kiosk mode)
```

`WebChatService` delegates to `launch-browser.sh` / `stop-browser.sh`, which are extracted from the classpath into `<dataDir>/quickquestion/bin/` at runtime.

## Data Locations

| Path | Purpose |
|---|---|
| `<dataDir>/quickquestion/bin/` | Extracted shell scripts |
| `<dataDir>/quickquestion/tigervnc-1.16.2.x86_64/` | Bundled Tiger VNC binaries |
| `<tmpDir>/quickquestion/browser.pid` | Tracked browser/VNC PID |

`<dataDir>` is provided by `dev.dirs` (typically `~/.local/share` on Linux).

## Supported Providers

- ChatGPT — https://chatgpt.com
- Claude — https://claude.ai
- Gemini — https://gemini.google.com
- Mistral — https://chat.mistral.ai/chat
- Perplexity — https://www.perplexity.ai

## Requirements

- Linux x86_64
- Java 21+
- Tiger VNC server binaries (bundled and auto-installed on first run)
