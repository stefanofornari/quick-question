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

class WebChatTest {

    @Test
    void to_string_returns_only_the_name() {
        WebChat webChat = new WebChat("ChatGPT", "https://chatgpt.com", true);
        
        then(webChat.toString())
            .as("The toString() method must return only the name so it renders correctly in JavaFX ComboBoxes")
            .isEqualTo("ChatGPT");
    }
}
