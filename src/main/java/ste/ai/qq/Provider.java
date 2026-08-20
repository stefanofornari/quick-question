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

import java.util.ArrayList;
import java.util.List;

import static ste.lloop.Loop.on;

/**
 * Provides a curated, out-of-the-box list of supported LLM web chat providers.
 * <p>
 * Host applications can obtain the predefined providers and pass them to the
 * Quick Question component through its entries property.
 */

public enum Provider {
    ANTHROPIC_CLAUDE(new WebChat("Anthropic Claude", "https://claude.ai", false)),
    CHAT_GPT(new WebChat("ChatGPT", "https://chatgpt.com", true)),
    PERPLEXITY(new WebChat("Perplexity", "https://www.perplexity.ai", true));

    private final WebChat webChat;

    Provider(WebChat webChat) {
        this.webChat = webChat;
    }

    public WebChat webChat() {
        return webChat;
    }

    public static List<WebChat> asList() {
        final List<WebChat> list = new ArrayList();

        on(values()).loop((provider) -> list.add(provider.webChat()));

        return List.copyOf(list);
    }
}
