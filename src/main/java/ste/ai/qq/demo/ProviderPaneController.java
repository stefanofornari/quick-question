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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

/**
 * Controller for the provider selection pane.
 */
public class ProviderPaneController {

    @FXML
    private Button gptButton;

    @FXML
    private Button claudeButton;

    @FXML
    private Button geminiButton;

    @FXML
    private Button mistralButton;

    @FXML
    private Button perplexityButton;

    @FXML
    private void onSwitch(ActionEvent event) {
        Button source = (Button) event.getSource();
        // Placeholder for provider switching logic
        System.out.println("Selected provider: " + source.getId());
    }

    /**
     * Returns the stage that hosts this controller's scene.
     *
     * @return the stage
     */
    public Stage getStage() {
        if (gptButton != null && gptButton.getScene() != null) {
            return (Stage) gptButton.getScene().getWindow();
        }
        return null;
    }
}
