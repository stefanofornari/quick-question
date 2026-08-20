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


import static org.assertj.core.api.BDDAssertions.then;

/**
 * Tests for {@link Provider}.
 */
public class DefaultProvidersSpec {

    @Test
    void predefined_providers_contains_all_expected_entries() {
        then(Provider.asList())
            .hasSize(3)
            .extracting(WebChat::name)
            .containsExactly("Anthropic Claude", "ChatGPT", "Perplexity");
    }

    @Test
    void anthropic_claude_has_expected_url_and_no_google_login() {
        // Then
        then(Provider.ANTHROPIC_CLAUDE.webChat().name()).isEqualTo("Anthropic Claude");
        then(Provider.ANTHROPIC_CLAUDE.webChat().url()).isEqualTo("https://claude.ai");
        then(Provider.ANTHROPIC_CLAUDE.webChat().googleLogin()).isFalse();
    }

    @Test
    void chatgpt_has_expected_url_and_google_login() {
        then(Provider.CHAT_GPT.webChat().name()).isEqualTo("ChatGPT");
        then(Provider.CHAT_GPT.webChat().url()).isEqualTo("https://chatgpt.com");
        then(Provider.CHAT_GPT.webChat().googleLogin()).isTrue();
    }

    @Test
    void perplexity_has_expected_url_and_google_login() {
        then(Provider.PERPLEXITY.webChat().name()).isEqualTo("Perplexity");
        then(Provider.PERPLEXITY.webChat().url()).isEqualTo("https://www.perplexity.ai");
        then(Provider.PERPLEXITY.webChat().googleLogin()).isTrue();
    }

    @Test
    void predefined_providers_list_is_unmodifiable() {
        then(Provider.asList()).isUnmodifiable();
    }
}
