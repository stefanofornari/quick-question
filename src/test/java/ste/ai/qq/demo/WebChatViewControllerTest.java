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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ste.ai.qq.WebChatView;

/**
 * Tests for {@link WebChatView} self-contained creation.
 */
class WebChatViewControllerTest {

    /**
     * Initializes JavaFX for headless tests.
     */
    @BeforeAll
    static void initFx() {
        new JFXPanel();
    }

    /**
     * Verifies that the component can be instantiated directly and loads its own FXML.
     */
    @Test
    void component_creates_self_contained_view() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                WebChatView view = new WebChatView();
                assertInstanceOf(WebChatView.class, view);
                assertTrue(view.getChildren().size() > 0);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
