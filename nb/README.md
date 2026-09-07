# QuickQuestion NetBeans Module

NetBeans IDE module that embeds the QuickQuestion LLM chat selector inside the
IDE via `JFXPanel`.

## What's Inside

- `ste.netbeans.ai.qq.nb.QuickQuestionTopComponent` — NetBeans `TopComponent`
  that embeds the `WebChat` component using `JFXPanel`

## Building

```bash
mvn clean install
```

## Installing in NetBeans

After building, the module is packaged as an NBM file under `nb/target/`.

Install it through **Tools → Plugins → Downloaded → Add Plugins** and select the
generated NBM.

## Usage

Once installed, open the QuickQuestion window from the **Window** menu:

- **Window → QuickQuestion**

The window embeds the same `WebChat` component used by the standalone demo
application. Selecting a provider launches the remote browser session via Tiger
VNC, and the viewer is streamed into the NetBeans window.

The window state is persisted by NetBeans (`PERSISTENCE_ALWAYS`).

## How It Works

The module bridges Swing and JavaFX using `JFXPanel`:

1. NetBeans opens `QuickQuestionTopComponent` as a regular Swing `TopComponent`.
2. On `componentOpened()`, a `JFXPanel` is created and the JavaFX `WebChat`
   scene is attached to it.
3. `Platform.setImplicitExit(false)` keeps the JavaFX runtime alive across
   NetBeans window open/close cycles.
4. The VNC viewer cursor is hidden because the remote desktop renders its own cursor.

## Requirements

- NetBeans IDE 12+ / Apache NetBeans 14+
- Java 21+
- Linux x86_64
- Tiger VNC server binaries (auto-installed by the core module on first run)
