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

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DemoViewController}.
 */
class DemoViewControllerTest {

    /**
     * Initializes JavaFX for headless tests.
     */
    @BeforeAll
    static void initFx() {
        new JFXPanel();
    }

    /**
     * Verifies that the demo shows the configured entries on startup.
     */
    @Test
    void demo_application_starts_with_configuration_controls() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                Parent root = loader.load();
                DemoViewController controller = loader.getController();
                then(root).isNotNull();
                then(controller).isNotNull();
                then(controller.getWebChatView()).isNotNull();
                then(controller.getWebChatView().selector().getButtons()).hasSize(3);
                then(controller.getWebChatView().webChatSet().getFirst().name())
                        .isEqualTo("Anthropic Claude");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    /**
     * Verifies that applying changes updates the embedded component configuration.
     */
    @Test
    void demo_application_updates_the_component_configuration() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                loader.load();
                DemoViewController controller = loader.getController();
                setTextField(controller, "nameField", "Claude");
                setTextField(controller, "urlField", "https://claude.ai");
                controller.handleAddEntry();
                controller.handleApplyChanges();
                ste.ai.qq.WebChatView webChatView = controller.getWebChatView();
                then(webChatView.webChatSet()).hasSize(4);
                then(webChatView.webChatSet().get(3).name()).isEqualTo("Claude");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    /**
     * Verifies that multiple additions are reflected in the configured order.
     */
    @Test
    void demo_application_reflects_multiple_additions_in_order() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                loader.load();
                DemoViewController controller = loader.getController();
                setTextField(controller, "nameField", "Claude");
                setTextField(controller, "urlField", "https://claude.ai");
                controller.handleAddEntry();
                setTextField(controller, "nameField", "ChatGPT");
                setTextField(controller, "urlField", "https://chatgpt.com");
                controller.handleAddEntry();
                controller.handleApplyChanges();
                ste.ai.qq.WebChatView webChatView = controller.getWebChatView();
                then(webChatView.webChatSet()).hasSize(5);
                then(webChatView.webChatSet().get(3).name()).isEqualTo("Claude");
                then(webChatView.webChatSet().get(4).name()).isEqualTo("ChatGPT");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    /**
     * Verifies that adding an entry updates the selector without requiring an explicit apply.
     */
    @Test
    void demo_application_updates_selector_dynamically_on_add() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                loader.load();
                DemoViewController controller = loader.getController();
                setTextField(controller, "nameField", "Claude");
                setTextField(controller, "urlField", "https://claude.ai");
                controller.handleAddEntry();
                ste.ai.qq.WebChatView webChatView = controller.getWebChatView();
                then(webChatView.selector().getButtons()).hasSize(4);
                then(webChatView.webChatSet().get(3).name()).isEqualTo("Claude");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void demo_application_shows_warning_for_unsupported_google_login() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                loader.load();
                DemoViewController controller = loader.getController();
                ste.ai.qq.WebChatView webChatView = controller.getWebChatView();

                selectService(webChatView, "Anthropic Claude");

                then(webChatView.warningLabel().isVisible()).isTrue();
                then(webChatView.warningLabel().getText()).contains("Google login", "email login");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void demo_application_hides_warning_for_supported_google_login() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(DemoViewControllerTest.class.getResource("/ste/ai/qq/demo-view.fxml"));
                loader.load();
                DemoViewController controller = loader.getController();
                ste.ai.qq.WebChatView webChatView = controller.getWebChatView();

                selectService(webChatView, "Perplexity");

                then(webChatView.warningLabel().isVisible()).isFalse();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                latch.countDown();
            }
        });
        then(latch.await(10, TimeUnit.SECONDS)).isTrue();
    }

    private static void setTextField(DemoViewController controller, String fieldName, String value) {
        TextField field = switch (fieldName) {
            case "nameField" -> controller.nameField;
            case "urlField" -> controller.urlField;
            default -> throw new IllegalArgumentException("Unsupported field: " + fieldName);
        };
        field.setText(value);
    }

    private static void selectService(ste.ai.qq.WebChatView webChatView, String name) {
        for (javafx.scene.Node node : webChatView.selector().getButtons()) {
            if (node instanceof Button button && button.getText().equals(name)) {
                button.fire();
                return;
            }
        }
        throw new IllegalStateException("Service not found: " + name);
    }
}
