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
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link WebChatCookieStore} persistence behaviour.
 */
class WebChatCookieStoreTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void cookies_are_saved_to_disk_when_added() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("cookies-session").toFile();
        Files.createDirectories(storageDirectory.toPath());
        WebChatCookieStore store = new WebChatCookieStore(storageDirectory);

        HttpCookie cookie = new HttpCookie("session", "abc123");
        cookie.setDomain(".example.com");
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);

        store.add(new URI("https://example.com"), cookie);

        File cookiesFile = new File(storageDirectory, "cookies.json");
        then(cookiesFile).exists();
        String content = Files.readString(cookiesFile.toPath());
        then(content).contains("\"name\": \"session\"");
        then(content).contains("\"value\": \"abc123\"");
    }

    @Test
    void cookies_are_reloaded_from_disk_on_startup() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("cookies-reload").toFile();
        Files.createDirectories(storageDirectory.toPath());

        WebChatCookieStore firstStore = new WebChatCookieStore(storageDirectory);
        HttpCookie cookie = new HttpCookie("auth", "token-value");
        cookie.setDomain("chat.example.com");
        cookie.setPath("/chat");
        cookie.setMaxAge(7200);
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        firstStore.add(new URI("https://chat.example.com"), cookie);

        WebChatCookieStore secondStore = new WebChatCookieStore(storageDirectory);

        then(secondStore.getCookies()).hasSize(1);
        HttpCookie loaded = secondStore.getCookies().getFirst();
        then(loaded.getName()).isEqualTo("auth");
        then(loaded.getValue()).isEqualTo("token-value");
        then(loaded.getDomain()).isEqualTo("chat.example.com");
        then(loaded.getPath()).isEqualTo("/chat");
        then(loaded.getMaxAge()).isEqualTo(7200);
        then(loaded.getSecure()).isFalse();
        then(loaded.isHttpOnly()).isFalse();
    }

    @Test
    void removing_a_cookie_updates_the_storage_file() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("cookies-remove").toFile();
        Files.createDirectories(storageDirectory.toPath());
        WebChatCookieStore store = new WebChatCookieStore(storageDirectory);

        HttpCookie cookie = new HttpCookie("to-remove", "value");
        URI uri = new URI("https://example.com");
        store.add(uri, cookie);

        store.remove(uri, cookie);

        File cookiesFile = new File(storageDirectory, "cookies.json");
        then(cookiesFile).exists();
        String content = Files.readString(cookiesFile.toPath());
        then(content).doesNotContain("to-remove");
    }

    @Test
    void expired_cookies_are_not_persisted() throws Exception {
        File storageDirectory = temporaryDirectory.resolve("cookies-expired").toFile();
        Files.createDirectories(storageDirectory.toPath());
        WebChatCookieStore store = new WebChatCookieStore(storageDirectory);

        HttpCookie expired = new HttpCookie("expired", "old");
        expired.setMaxAge(0);

        store.add(new URI("https://example.com"), expired);

        File cookiesFile = new File(storageDirectory, "cookies.json");
        then(cookiesFile).exists();
        String content = Files.readString(cookiesFile.toPath());
        then(content).doesNotContain("expired");
    }
}
