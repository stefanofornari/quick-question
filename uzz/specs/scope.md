# Quick Question Scope Overview

## Project Name
- Quick Question

## Version
- 0.0.0-SNAPSHOT

## Package
- ste.ai.qq

## Overall Description
Quick Question is a JavaFX component that can be embedded into JavaFX applications to provide quick access to LLM web chat interfaces. The component displays a WebView and a selectable list of LLM endpoints at the top of the view. Selecting an LLM navigates the WebView to that provider’s web chat page.

The component accepts a list of LLM providers through a configurable property; it does not own or display a hard-coded list itself. A reusable `DefaultProviders` class is provided so host applications can easily obtain a curated, out-of-the-box set of supported LLM providers.

Session cookie persistence will be handled by the underlying WebView engine. The host application may optionally supply a custom directory for WebView cookie/session storage; if no directory is supplied, a sensible default will be used.

A demo application will be provided to showcase the component and to allow simple configuration of the available LLM entries. The demo application is responsible for presenting the default providers in the selector and for surfacing any provider-specific warnings to the end user.

## In-Scope Capabilities
- JavaFX component for embedding a WebView into host JavaFX applications
- Default WebView navigation to an LLM home/chat page
- Top-level LLM selector listing configured LLM names and URLs
- Navigation to the selected LLM URL when a user chooses an entry
- Reading the list of LLMs and URLs from a configuration object or configuration function
- Reusable `DefaultProviders` class exposing the out-of-the-box supported LLM providers
- Provider data model including `googleLogin` flag (`true`/`false`)
- WebView session cookie persistence across application restarts
- Optional custom directory for WebView session/cookie storage supplied by the host application
- Default WebView session storage directory used when none is supplied
- Demo application showcasing the component
- Simple configuration UI in the demo application
- Demo application uses `DefaultProviders` to populate the selector
- Demo application shows a warning when a selected provider does not support Google login
- Support for a separate additional configuration panel component as a related but excluded module

## Out-of-the-box Providers
| Provider | URL | googleLogin |
|---|---|---|
| Anthropic Claude | https://claude.ai | false |
| ChatGPT | https://chatgpt.com | true |
| Perplexity | https://www.perplexity.ai | true |

## Out-of-Scope / Future Enhancements
- Authentication / API key management
- Prompt engineering helpers
- Chat memory
- Streaming token rendering
- Provider-specific integrations
- Browser automation beyond simple navigation
- Theming / advanced UI customization
- Memory features

## Development Toolchain
- JavaFX
- Java
- Maven
- JUnit 5
- TestFX
- AssertJ
- Headless test execution support
- TDD-oriented workflow

## Target Systems/Platforms
- JavaFX desktop applications
- Demo application for local desktop execution
- Host applications embedding the component

## Path/Link to `coding-standard.md`
- `uzz/specs/coding-standard.md`

## Path/Link to `development-framework.md`
- `uzz/specs/development-framework.md`
