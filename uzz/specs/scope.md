# QuickQuestion Scope Overview

## Project Name
- QuickQuestion

## Version
- 0.0.0-SNAPSHOT

## Package
- ste.ai.qq

## Overall Description
QuickQuestion is a JavaFX component that can be embedded into JavaFX applications
to provide quick access to LLM web chat interfaces. The component displays a VNC
viewer and a hard-coded list of LLM endpoints at the top of the view. Selecting
an LLM navigates the viewer to that provider's web chat page by launching a
remote browser session via a bundled Tiger VNC server.

A `Provider` enum is provided so host applications can easily reference the
out-of-the-box supported LLM providers.

Session persistence will be handled through the browser profile directory used
by the remote browser (a new provile directory is created and used so not to
interfere with the user's real profile).

A demo application showcases the component and a NetBeans plug-in is provider to
use QuickQuestion inside the IDE.

## License
Given that the project uses and distributes GPL components (e.g. TiverVNC) the
while project is distributed under GPL v2 license.

## In-Scope Capabilities
- JavaFX component for embedding a VNC viewer into host JavaFX applications
- Remote browser session launched via bundled Tiger VNC server
- Top-level LLM selector listing hard-coded LLM names and URLs
- Navigation to the selected LLM URL when a user chooses an entry
- Browser session cookie persistence across application restarts via browser profile directory
- Demo application showcasing the component with About dialog showing vesioning
  and licensing information
- NetBeans pluig-in to use QuickQuestion in the IDE
- Tiger VNC server auto-installation into the user data directory at startup

### Out-of-the-box Providers
| Provider | URL |
|---|---|
| ChatGPT | https://chatgpt.com |
| Claude | https://claude.ai |
| Gemini | https://gemini.google.com |
| Mistral | https://chat.mistral.ai/chat |
| Perplexity | https://www.perplexity.ai |

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
- AssertJ BDD (BDDAssertions)
- Headless test execution support
- TDD-oriented workflow
- Tiger VNC server bundled as a dependency

## Target Systems/Platforms
- JavaFX desktop applications
- Demo application for local desktop execution
- Host applications embedding the component
- NetBeans IDE module (via JFXPanel embedding)

## Path/Link to `coding-standard.md`
- `uzz/specs/coding-standard.md`

## Path/Link to `development-framework.md`
- `uzz/specs/development-framework.md`
