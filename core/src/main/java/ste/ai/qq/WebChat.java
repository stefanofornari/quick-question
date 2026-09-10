/*
 * Copyright 2026 QuickQuestion contributors
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class WebChat extends StackPane {

    final Logger log = Logger.getLogger(getClass().getName());

    final WebChatService service;

    public WebChat() {
        this(new WebChatService());
    }

    protected WebChat(final WebChatService service) {
        log.finest(() -> "creating a new component");
        if (service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        final URL url = getClass().getResource("WebChat.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load " + url, exception);
        }

        this.service = service;

        showProvider(Provider.CHATGPT);

        log.finest(() -> "component created");
    }

    public void showProvider(final Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider cannot be null");
        }
        try {
            service.navigateTo(URI.create(provider.url).toURL());
        } catch (final Exception e) {
            throw new RuntimeException("Failed to navigate to " + provider.displayName, e);
        }
    }

    @FXML
    private void onSwitch(final ActionEvent action) {
        showProvider(Provider.valueOf(((Node)action.getSource()).getId()));
    }
}
