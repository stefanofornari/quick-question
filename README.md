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
