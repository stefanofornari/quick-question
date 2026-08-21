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

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

/**
 * .
 */
public class WebChat extends HBox {

    final Logger log = Logger.getLogger(getClass().getName());

    public final WebChatController controller;

    public WebChat() {
        log.finest(() -> "creating a new component");
        final URL url = getClass().getResource("WebChat.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            // Retrieve the controller instance created by FXMLLoader
            this.controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load " + url, exception);
        }
        log.finest(() -> "component created");
    }

}
