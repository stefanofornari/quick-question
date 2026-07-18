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

import java.util.Objects;

/**
 * Represents a selectable LLM endpoint entry.
 */
public record WebChat(String name, String url, boolean googleLogin) {

    /**
     * Creates a new LLM entry.
     *
     * @param name the display name
     * @param url the navigation URL
     * @param googleLogin whether Google login is supported
     */
    public WebChat {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(url, "url must not be null");
    }

    /**
     * Creates a new LLM entry with Google login supported by default.
     *
     * @param name the display name
     * @param url the navigation URL
     */
    public WebChat(String name, String url) {
        this(name, url, true);
    }

    @Override
    public String toString() {
        return name;
    }
}
