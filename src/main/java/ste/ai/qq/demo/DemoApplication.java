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
package ste.ai.qq.demo;

import atlantafx.base.theme.NordLight;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ste.ai.qq.WebChatView;
import ste.ai.qq.cookies.WebChatCookieManager;

/**
 * Demo application entry point.
 */
public class DemoApplication extends Application {

    /**
     * Installs the persistent cookie manager backed by the given storage directory.
     *
     * @param storageDirectory directory where the cookie file is persisted
     */
    public static void installCookieManager(File storageDirectory) {
        CookieHandler.setDefault(new WebChatCookieManager(storageDirectory));
    }

    /**
     * Starts the demo application.
     *
     * @param stage the primary stage
     * @throws IOException if the FXML cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        installCookieManager(WebChatView.defaultStorageDirectory().toFile());

        URL fxml = DemoApplication.class.getResource("/ste/ai/qq/demo-view.fxml");
        if (fxml == null) {
            throw new IOException("Unable to locate demo FXML at /ste/ai/qq/demo-view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Quick Question Demo");
        stage.show();
    }
}
