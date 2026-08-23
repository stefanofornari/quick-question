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
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Demo application entry point.
 */
public class QuickQuestionDemo extends Application {

    /**
     * Starts the demo application.
     *
     * @param stage the primary stage
     * @throws IOException if the FXML cannot be loaded
     */
    @Override
    public void start(final Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        final URL fxml = QuickQuestionDemo.class.getResource("QuickQuestionDemo.fxml");
        if (fxml == null) {
            throw new IOException("Unable to locate demo FXML at " + fxml);
        }
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
            getClass().getResource("QuickQuestionDemo.css").toExternalForm()
        );

        stage.setScene(scene);

        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    private void showProviderDialog(Stage owner) throws IOException {
        final URL paneFxml = QuickQuestionDemo.class.getResource("/ste/ai/qq/ProviderPane.fxml");
        if (paneFxml == null) {
            throw new IOException("Unable to locate ProviderPane FXML at " + paneFxml);
        }
        FXMLLoader loader = new FXMLLoader(paneFxml);
        Parent dialogRoot = loader.load();

        Scene dialogScene = new Scene(dialogRoot);
        dialogScene.getStylesheets().add(
            getClass().getResource("QuickQuestionDemo.css").toExternalForm()
        );

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setScene(dialogScene);
        dialog.setTitle("Select Provider");
        dialog.setResizable(false);
        dialog.show();
    }

    @Override
    public void stop() {
        // Ensure the network threads disconnect cleanly on app exit
        //if (vncViewer != null && vncViewer.isConnected()) {
        //    vncViewer.disconnect();
        //}
        // TO DO
    }
}
