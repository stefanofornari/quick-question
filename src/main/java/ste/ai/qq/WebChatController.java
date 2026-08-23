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

import java.net.URI;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class WebChatController {

    final Logger log = Logger.getLogger(getClass().getName());

    final WebChatService service = new WebChatService();

    @FXML
    private TopHidingToolBar providerToolBar;

    @FXML
    public void initialize() {
        log.finest(() -> "initializing the controller");

        Platform.runLater(() -> {
            if (providerToolBar == null) {
                log.fine("providerPane not available for wiring");
                return;
            }
        });

        log.finest(() -> "controller initialized");
    }

    @FXML
    private void onSwitch(final ActionEvent action) {
        Provider provider = Provider.valueOf(((Node)action.getSource()).getId());
        try {
            service.navigateTo(URI.create(provider.url).toURL());
        } catch (final Exception e) {
            throw new RuntimeException("Failed to navigate to " + provider.displayName, e);
        }
    }
}