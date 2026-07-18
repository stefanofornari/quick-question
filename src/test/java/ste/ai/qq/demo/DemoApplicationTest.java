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

import java.io.File;
import java.net.CookieHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ste.ai.qq.cookies.WebChatCookieManager;

/**
 * Tests for {@link DemoApplication} setup behaviour.
 */
class DemoApplicationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void demo_application_installs_persistent_cookie_manager_in_storage_directory() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("demo-storage").toFile();
        Files.createDirectories(storageDirectory.toPath());

        DemoApplication.installCookieManager(storageDirectory);

        then(CookieHandler.getDefault())
            .isInstanceOf(WebChatCookieManager.class);
    }
}
