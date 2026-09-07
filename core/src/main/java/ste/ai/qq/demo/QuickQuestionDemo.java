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

import atlantafx.base.theme.NordLight;
import dev.dirs.ProjectDirectories;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;

/**
 * Demo application entry point.
 */
public class QuickQuestionDemo extends Application {

    private static final String QUALIFIER = "com.github.stefanofornari";
    private static final String ORGANIZATION = "ste";
    private static final String APPLICATION = "quickquestion";

    /**
     * Starts the demo application.
     *
     * @param stage the primary stage
     * @throws IOException if the FXML cannot be loaded
     */
    @Override
    public void start(final Stage stage) throws IOException {

        //
        // Let's make sure Tiger VNC bundled with QuickQuestion is installed
        // locally (under ~/.local/share/quickquestion). The intaller checks if
        // the bundled version is already installed and updates it if needed.
        //
        final VNCServerInstaller installer = new VNCServerInstaller(Path.of("tigervnc", "tiger-vnc-mini-%s.jar".formatted(VNCServerInstaller.VNC_SERVER_VERSION)));
        try {
            installer.installInto(
                Path.of(ProjectDirectories.from(QUALIFIER, ORGANIZATION, APPLICATION).dataDir)
            );
        } catch (final IOException e) {
            System.err.println("QuickQuestion: unable to install VNC server binaries: " + e.getMessage());
            Platform.exit();
            return;
        }

        //
        // Start the application
        //
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

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ste/ai/qq/logo-qq-256x256.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ste/ai/qq/logo-qq-128x128.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ste/ai/qq/logo-qq-32x32.png")));

        stage.setResizable(true);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
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
