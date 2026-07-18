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
package ste.ai.qq.cookies;

import java.io.File;
import java.net.CookieHandler;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link WebChatCookieManager}.
 */
class WebChatCookieManagerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void stores_cookies_in_the_configured_directory() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("manager-cookies").toFile();
        Files.createDirectories(storageDirectory.toPath());

        WebChatCookieManager manager = new WebChatCookieManager(storageDirectory);
        URI uri = new URI("https://example.com");
        manager.getCookieStore().add(uri, createCookie("token", "secret"));

        File cookiesFile = new File(storageDirectory, "cookies.json");
        then(cookiesFile).exists();
        String content = Files.readString(cookiesFile.toPath());
        then(content).contains("\"name\": \"token\"");
        then(content).contains("\"value\": \"secret\"");
    }

    @Test
    void can_be_installed_as_the_default_cookie_handler() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("default-handler").toFile();
        Files.createDirectories(storageDirectory.toPath());

        WebChatCookieManager manager = new WebChatCookieManager(storageDirectory);
        CookieHandler.setDefault(manager);

        then(CookieHandler.getDefault()).isSameAs(manager);
    }

    private java.net.HttpCookie createCookie(String name, String value) {
        java.net.HttpCookie cookie = new java.net.HttpCookie(name, value);
        cookie.setDomain("example.com");
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        return cookie;
    }
}
