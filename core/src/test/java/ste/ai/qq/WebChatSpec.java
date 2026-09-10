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

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class WebChatSpec extends ApplicationTest {

    private Stage stage;
    private WebChat chat;

    private AtomicReference<URL> providerURL = new AtomicReference();

    @Override
    public void start(final Stage stage) throws Exception {

        this.stage = stage;

        this.chat = new WebChat(new WebChatService() {
            @Override
            public void navigateTo(final URL url) {
                providerURL.set(url);
            }
        });

        Scene scene = new Scene(new StackPane(chat), 768, 1024);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void show_ChatGPT_start_startup() {
        WaitForAsyncUtils.waitForFxEvents();
        then(providerURL.get().toExternalForm()).isEqualTo(Provider.CHATGPT.url);
    }

    @Test
    void constructor_sanity_check() {
        thenThrownBy(() -> {
            new WebChat(null);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("service cannot be null");
    }

    @Test
    void showProvider_sanity_check() {
        thenThrownBy(() -> {
            chat.showProvider(null);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("provider cannot be null");
    }
}
