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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Tests for {@link DefaultProviders}.
 */
class DefaultProvidersTest {

    @Test
    void predefined_providers_contains_all_expected_entries() {
        // Given
        final DefaultProviders defaultProviders = new DefaultProviders();

        // When
        final List<WebChat> providers = defaultProviders.providers();

        // Then
        then(providers)
            .hasSize(3)
            .extracting(WebChat::name)
            .containsExactly("Anthropic Claude", "ChatGPT", "Perplexity");
    }

    @Test
    void anthropic_claude_has_expected_url_and_no_google_login() {
        // Given
        final DefaultProviders defaultProviders = new DefaultProviders();

        // When
        final WebChat claude = defaultProviders.providers().get(0);

        // Then
        then(claude.name()).isEqualTo("Anthropic Claude");
        then(claude.url()).isEqualTo("https://claude.ai");
        then(claude.googleLogin()).isFalse();
    }

    @Test
    void chatgpt_has_expected_url_and_google_login() {
        // Given
        final DefaultProviders defaultProviders = new DefaultProviders();

        // When
        final WebChat chatGpt = defaultProviders.providers().get(1);

        // Then
        then(chatGpt.name()).isEqualTo("ChatGPT");
        then(chatGpt.url()).isEqualTo("https://chatgpt.com");
        then(chatGpt.googleLogin()).isTrue();
    }

    @Test
    void perplexity_has_expected_url_and_google_login() {
        // Given
        final DefaultProviders defaultProviders = new DefaultProviders();

        // When
        final WebChat perplexity = defaultProviders.providers().get(2);

        // Then
        then(perplexity.name()).isEqualTo("Perplexity");
        then(perplexity.url()).isEqualTo("https://www.perplexity.ai");
        then(perplexity.googleLogin()).isTrue();
    }

    @Test
    void predefined_providers_list_is_unmodifiable() {
        // Given
        final DefaultProviders defaultProviders = new DefaultProviders();
        final List<WebChat> providers = defaultProviders.providers();

        // When / Then
        then(providers).isUnmodifiable();
    }
}
