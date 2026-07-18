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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A persistent {@link CookieStore} implementation that saves cookies to disk
 * as JSON and restores them on creation.
 * <p>
 * Write operations are mirrored to the configured storage file immediately.
 * A shutdown hook is registered to persist the final state when the JVM exits
 * normally.
 */
public class WebChatCookieStore implements CookieStore, Runnable {

    private static final String COOKIES_FILE_NAME = "cookies.json";

    private final CookieStore backingStore;
    private final File storageFile;

    /**
     * Creates a cookie store that persists cookies in the given directory.
     *
     * @param storageDirectory the directory where the cookie file is stored
     */
    public WebChatCookieStore(File storageDirectory) {
        this.backingStore = new CookieManager().getCookieStore();
        this.storageFile = new File(storageDirectory, COOKIES_FILE_NAME);

        if (!storageFile.exists()) {
            try {
                Files.createDirectories(storageFile.getParentFile().toPath());
            } catch (IOException exception) {
                System.err.println("WebChatCookieStore failed to create storage directory: " + exception.getMessage());
            }
        }

        loadFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    private synchronized void saveToDisk() {
        JSONArray jsonArray = new JSONArray();

        for (HttpCookie cookie : backingStore.getCookies()) {
            if (!cookie.hasExpired()) {
                JSONObject jsonCookie = new JSONObject();
                jsonCookie.put("name", cookie.getName());
                jsonCookie.put("value", cookie.getValue());
                jsonCookie.put("domain", cookie.getDomain() != null ? cookie.getDomain() : "");
                jsonCookie.put("path", cookie.getPath() != null ? cookie.getPath() : "");
                jsonCookie.put("maxAge", cookie.getMaxAge());
                jsonCookie.put("secure", cookie.getSecure());
                jsonCookie.put("httpOnly", cookie.isHttpOnly());

                jsonArray.put(jsonCookie);
            }
        }

        try {
            Files.createDirectories(storageFile.getParentFile().toPath());
        } catch (IOException exception) {
            System.err.println("WebChatCookieStore failed to create storage directory: " + exception.getMessage());
            return;
        }

        try (FileWriter writer = new FileWriter(storageFile)) {
            writer.write(jsonArray.toString(2));
        } catch (IOException exception) {
            System.err.println("WebChatCookieStore failed to save JSON cookies: " + exception.getMessage());
        }
    }

    private void loadFromDisk() {
        if (!storageFile.exists() || storageFile.length() == 0) {
            return;
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonCookie = jsonArray.getJSONObject(i);

                String name = jsonCookie.getString("name");
                String value = jsonCookie.getString("value");

                HttpCookie cookie = new HttpCookie(name, value);
                cookie.setDomain(jsonCookie.optString("domain", ""));
                cookie.setPath(jsonCookie.optString("path", ""));
                cookie.setMaxAge(jsonCookie.getLong("maxAge"));
                cookie.setSecure(jsonCookie.getBoolean("secure"));
                cookie.setHttpOnly(jsonCookie.getBoolean("httpOnly"));

                if (!cookie.hasExpired()) {
                    URI uri = null;
                    String domain = cookie.getDomain();
                    if (domain != null && !domain.isEmpty()) {
                        String cleanDomain = domain.startsWith(".") ? domain.substring(1) : domain;
                        uri = new URI("https://" + cleanDomain);
                    }
                    backingStore.add(uri, cookie);
                }
            }
        } catch (Exception exception) {
            System.err.println("WebChatCookieStore failed to parse JSON file: " + exception.getMessage());
        }
    }

    @Override
    public synchronized void add(URI uri, HttpCookie cookie) {
        backingStore.add(uri, cookie);
        saveToDisk();
    }

    @Override
    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        boolean removed = backingStore.remove(uri, cookie);
        if (removed) {
            saveToDisk();
        }
        return removed;
    }

    @Override
    public synchronized boolean removeAll() {
        boolean changed = backingStore.removeAll();
        if (changed) {
            saveToDisk();
        }
        return changed;
    }

    @Override
    public synchronized List<HttpCookie> get(URI uri) {
        return backingStore.get(uri);
    }

    @Override
    public synchronized List<HttpCookie> getCookies() {
        return backingStore.getCookies();
    }

    @Override
    public synchronized List<URI> getURIs() {
        return backingStore.getURIs();
    }

    @Override
    public void run() {
        saveToDisk();
    }
}
