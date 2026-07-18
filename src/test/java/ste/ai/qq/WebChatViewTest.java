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


import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static ste.ai.qq.WebChatView.DEFAULT_STORAGE_DIR_NAME;
import static ste.ai.qq.WebChatView.DEFAULT_STORAGE_PATH_NAME;
import static ste.lloop.Loop.on;

/**
 * Tests for {@link WebChatView} session storage directory configuration.
 */
class WebChatViewTest extends ApplicationTest {

    public final static String WINDOWS = "Windows 10";
    public final static String LINUX = "Linux";
    public final static String MACOS = "Mac OS X";

    @TempDir
    Path temporaryDirectory;

    @Test
    void default_session_storage_directory_is_used_when_none_is_supplied() throws Exception {
        final File firstDefaultDirectory = createWebChatView(null).webView().getEngine().getUserDataDirectory();

        on(LINUX, WINDOWS, MACOS).loop((os) -> {
            try {
                SystemLambda.restoreSystemProperties(() -> {
                    System.setProperty("os.name", os);
                    then(firstDefaultDirectory.getAbsolutePath())
                        .as("default storage directory should be under the user home")
                        .isEqualTo(Path.of(DEFAULT_STORAGE_PATH_NAME).resolve(DEFAULT_STORAGE_DIR_NAME).toAbsolutePath().toString());
                });
            } catch (Throwable t) {
                fail(os + ": " + t.getMessage());
            }
        });
    }

    @Test
    void custom_session_storage_directory_is_used_when_supplied() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("custom-session").toFile();
        Files.createDirectories(storageDirectory.toPath());

        WebChatView view = createWebChatView(storageDirectory);

        then(view.webView().getEngine().getUserDataDirectory()).isEqualTo(storageDirectory);
    }

    @Test
    void different_custom_storage_directories_are_isolated() throws Exception {
        File firstStorage = temporaryDirectory.resolve("first-session").toFile();
        File secondStorage = temporaryDirectory.resolve("second-session").toFile();
        Files.createDirectories(firstStorage.toPath());
        Files.createDirectories(secondStorage.toPath());

        WebChatView firstView = createWebChatView(firstStorage);
        WebChatView secondView = createWebChatView(secondStorage);

        then(firstView.webView().getEngine().getUserDataDirectory()).isEqualTo(firstStorage);
        then(secondView.webView().getEngine().getUserDataDirectory()).isEqualTo(secondStorage);
        then(firstView.webView().getEngine().getUserDataDirectory())
            .isNotEqualTo(secondView.webView().getEngine().getUserDataDirectory());
    }

    @Test
    void rejects_invalid_custom_storage_directory() throws Exception {
        File nonWritableDirectory = temporaryDirectory.resolve("readonly").toFile();
        nonWritableDirectory.mkdirs();
        nonWritableDirectory.setWritable(false);

        try {
            Throwable error = WaitForAsyncUtils.asyncFx(() -> {
                try {
                    new WebChatView(nonWritableDirectory);
                    return null;
                } catch (Throwable throwable) {
                    return throwable;
                }
            }).get();
            then(error)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("session storage directory");
        } finally {
            nonWritableDirectory.setWritable(true);
        }
    }

    private WebChatView createWebChatView(File sessionStorageDirectory) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> new WebChatView(sessionStorageDirectory)).get();
    }
}
