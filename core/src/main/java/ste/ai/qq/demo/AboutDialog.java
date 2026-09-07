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
package ste.ai.qq.demo;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class AboutDialog extends VBox {

    private final Logger log = Logger.getLogger(getClass().getName());

    public final AboutDialogController controller;

    public AboutDialog() {
        final URL url = getClass().getResource("AboutDialog.fxml");
        final FXMLLoader fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
            this.controller = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load " + url, exception);
        }

        log.finest(() -> "about dialog created");
    }

    public void setOnClose(Runnable onClose) {
        controller.setOnClose(onClose);
    }
}
