/*
 * Copyright 2026 Quick Question contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.ai.qq;

import java.net.URL;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * Controller for the {@link WebChat} component.
 * <p>
 * Wires the provider buttons to {@link WebChatService} so that selecting a
 * provider launches or redirects the kiosk browser to the corresponding
 * web chat URL.
 * </p>
 */
public class WebChatController {

    final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    private Node providerPane;

    @FXML
    public void initialize() {
        log.finest(() -> "initializing the controller");

        Platform.runLater(() -> {
            final WebChatService service = new WebChatService();
            if (providerPane == null) {
                log.fine("providerPane not available for wiring");
                return;
            }

            wireButton(providerPane, "gptButton", Provider.CHAT_GPT, service);
            wireButton(providerPane, "claudeButton", Provider.ANTHROPIC_CLAUDE, service);
            wireButton(providerPane, "geminiButton", Provider.GEMINI, service);
            wireButton(providerPane, "mistralButton", Provider.MISTRAL, service);
            wireButton(providerPane, "perplexityButton", Provider.PERPLEXITY, service);
        });

        log.finest(() -> "controller initialized");
    }

    private static void wireButton(final Node pane, final String buttonId, final Provider provider, final WebChatService service) {
        final Button button = (Button) pane.lookup("#" + buttonId);
        if (button == null) {
            return;
        }
        button.setOnAction(event -> {
            try {
                service.navigateTo(new URL(provider.url));
            } catch (final Exception e) {
                throw new RuntimeException("Failed to navigate to " + provider.displayName, e);
            }
        });
    }
}
