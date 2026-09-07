/*
 * Copyright 2026 QuickQuestion contributors
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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class WebChatServiceSpec {

    private Path binDir;
    private Path pidFile;
    private WebChatService service;

    private void writeScript(final Path dir, final String name, final String body) throws IOException {
        final Path script = dir.resolve(name);
        Files.writeString(script, "#!/usr/bin/env bash\n" + body);
        final Set<java.nio.file.attribute.PosixFilePermission> perms =
            new HashSet<>(Files.getPosixFilePermissions(script));
        perms.add(java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE);
        perms.add(java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE);
        perms.add(java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(script, perms);
    }

    private void setupService(final Path scratch) throws Exception {
        binDir = scratch.resolve("bin");
        Files.createDirectories(binDir);
        pidFile = scratch.resolve("browser.pid");
        writeScript(binDir, "launch-browser.sh",
            "PID_FILE=\"\"\n" +
            "while [[ $# -gt 0 ]]; do\n" +
            "  case \"$1\" in\n" +
            "    --pid-file) PID_FILE=\"$2\"; shift 2 ;;\n" +
            "    *) shift ;;\n" +
            "  esac\n" +
            "done\n" +
            "echo launched > \"${PID_FILE}.launched\"\n" +
            "echo 999999999 > \"$PID_FILE\"\n");
        writeScript(binDir, "stop-browser.sh",
            "PID_FILE=\"\"\n" +
            "while [[ $# -gt 0 ]]; do\n" +
            "  case \"$1\" in\n" +
            "    --pid-file) PID_FILE=\"$2\"; shift 2 ;;\n" +
            "    *) shift ;;\n" +
            "  esac\n" +
            "done\n" +
            "rm -f \"$PID_FILE\"\n");
        service = new WebChatService(binDir, pidFile, ":5", pid -> true);
    }

    @Test
    void navigate_to_when_no_browser_running_launches_it(@TempDir final Path scratch) throws Exception {
        setupService(scratch);
        service.navigateTo(new URL("https://chatgpt.com"));
        then(pidFile).exists().isRegularFile();
        then(service.isRunning()).isTrue();
        then(pidFile.resolveSibling("browser.pid.launched")).exists();
    }

    @Test
    void navigate_to_when_browser_running_stops_and_launches_new_session(@TempDir final Path scratch) throws Exception {
        setupService(scratch);
        service.navigateTo(new URL("https://chatgpt.com"));
        Files.deleteIfExists(pidFile.resolveSibling("browser.pid.launched"));
        final long firstPid = Long.parseLong(Files.readString(pidFile).trim());
        service.navigateTo(new URL("https://claude.ai"));
        then(service.isRunning()).isTrue();
        then(Long.parseLong(Files.readString(pidFile).trim())).isEqualTo(firstPid);
        then(pidFile.resolveSibling("browser.pid.launched")).exists();
    }

    @Test
    void is_browser_running_returns_false_when_no_pid_file(@TempDir final Path scratch) throws Exception {
        setupService(scratch);
        then(service.isRunning()).isFalse();
    }

    @Test
    void is_browser_running_returns_false_when_pid_is_stale(@TempDir final Path scratch) throws Exception {
        setupService(scratch);
        service = new WebChatService(binDir, pidFile, ":5", pid -> false);
        Files.writeString(pidFile, "999999\n");
        then(service.isRunning()).isFalse();
    }

    @Test
    void stop_terminates_running_browser_and_clears_state(@TempDir final Path scratch) throws Exception {
        setupService(scratch);
        service.navigateTo(new URL("https://chatgpt.com"));
        then(service.isRunning()).isTrue();
        service.stop();
        then(service.isRunning()).isFalse();
        then(pidFile).doesNotExist();
    }

    @Test
    void launch_script_failure_raises_exception(@TempDir final Path scratch) throws Exception {
        binDir = scratch.resolve("bin");
        Files.createDirectories(binDir);
        pidFile = scratch.resolve("browser.pid");
        writeScript(binDir, "launch-browser.sh", "echo 'out' && echo 'err' >&2 && exit 1\n");
        writeScript(binDir, "stop-browser.sh", "exit 0\n");
        service = new WebChatService(binDir, pidFile, ":5", pid -> true);
        thenThrownBy(() -> service.navigateTo(new URL("https://chatgpt.com")))
            .isInstanceOf(WebChatException.class)
            .hasMessageContaining("stdout: out")
            .hasMessageContaining("stderr: err");
    }
}
