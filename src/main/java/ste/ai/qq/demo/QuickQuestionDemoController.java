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

import com.tigervnc.rfb.Configuration;
import com.tigervnc.rfb.Security;
import com.tigervnc.rfb.SecurityClient;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import ste.ai.qq.WebChat;
import ste.vnc.viewer.demo.AboutDialog;
import ste.vnc.viewer.demo.CustomTitleBar;

/**
 *
 */
public class QuickQuestionDemoController {

    private final Logger log = Logger.getLogger(getClass().getName());

    @FXML
    public CustomTitleBar titleBar;

    @FXML
    public WebChat webChat;

    @FXML
    public Region resizeN, resizeS, resizeE, resizeW, resizeNE, resizeNW, resizeSE, resizeSW;

    @FXML
    public void initialize() {
        log.finest(() -> "initializing the controller");

        // Enable viewer parameters so Configuration.setParam() can see them
        Configuration.enableViewerParams();

        // Restrict security to None only
        SecurityClient.setDefaults();
        Security.enabledSecTypes.clear();
        Security.EnableSecType(Security.secTypeNone);

        Platform.runLater(() -> {
            titleBar.onFullscreen(this::onToggleFullScreen);
            titleBar.onAbout(this::onAbout);
            titleBar.onExit(this::onExit);

            titleBar.controller.setupResizeHandlers(
                resizeN, resizeS, resizeE, resizeW,
                resizeNE, resizeNW, resizeSE, resizeSW
            );

            Scene scene = titleBar.getScene();
            if (scene != null) {
                KeyCombination quitShortcut = new KeyCodeCombination(
                    KeyCode.Q, KeyCombination.SHORTCUT_DOWN
                );
                scene.getAccelerators().put(quitShortcut, this::onExit);
            }
        });

        log.finest(() -> "controller initialized");
    }

    @FXML
    private void onCloseWindow() {
        stage().close();
    }

    void onExit() {
        System.exit(0);
    }

    @FXML
    private void onToggleFullScreen() {
        final Stage stage = stage();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    private void onAbout() {
        AboutDialog dialog = new AboutDialog();
        dialog.setOnClose(() -> {
            if (dialog.getScene() != null && dialog.getScene().getWindow() != null) {
                dialog.getScene().getWindow().hide();
            }
        });

        final Scene dialogScene = new javafx.scene.Scene(dialog);
        // Make the Scene background transparent so the VBox rounded corners render smoothly
        dialogScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialogScene.getStylesheets().add(getClass().getResource("QuickQuestionDemo.css").toExternalForm());

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.initOwner(stage());
        // Use TRANSPARENT style instead of UNDECORATED
        dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    protected Stage stage() {
        if (webChat != null) {
            return (Stage)webChat.getScene().getWindow();
        }

        throw new IllegalStateException("no Stage available!");
    }

}
