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
package ste.ai.qq.bin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.BDDAssertions.then;

class LaunchBrowserScriptSpec {

    private Path extractLaunchScript(final Path scratch) throws IOException {
        final Path script = scratch.resolve("launch-browser.sh");
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream("bin/launch-browser.sh")) {
            if (is == null) {
                throw new IOException("Missing classpath resource: bin/launch-browser.sh");
            }
            Files.copy(is, script);
        }
        script.toFile().setExecutable(true);
        return script;
    }

    private void copyHarnessResource(final Path scratch, final String resourcePath, final Path target) throws IOException {
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream("bin/" + resourcePath)) {
            if (is == null) {
                throw new IOException("Missing harness resource: bin/" + resourcePath);
            }
            Files.createDirectories(target.getParent());
            Files.copy(is, target);
        }
        target.toFile().setExecutable(true);
    }

    private ProcessResult runScript(final Path scratch, final String url, final String... extraArgs) throws Exception {
        final Path script = extractLaunchScript(scratch);

        final Path fakeBin = scratch.resolve("bin");
        Files.createDirectories(fakeBin);
        copyHarnessResource(scratch, "fake-xdg-settings", fakeBin.resolve("xdg-settings"));
        copyHarnessResource(scratch, "fake-xdg-mime", fakeBin.resolve("xdg-mime"));
        copyHarnessResource(scratch, "fake-google-chrome", fakeBin.resolve("google-chrome"));

        final Path appsDir = scratch.resolve("share").resolve("applications");
        Files.createDirectories(appsDir);
        copyHarnessResource(scratch, "share/applications/google-chrome.desktop", appsDir.resolve("google-chrome.desktop"));

        final Path profileDir = scratch.resolve("profile");

        final List<String> command = new ArrayList<>();
        command.add(script.toString());
        command.add(url);
        command.add("--display");
        command.add(":5");
        command.add("--pid-file");
        command.add(scratch.resolve("browser.pid").toString());
        command.add("--profile-dir");
        command.add(profileDir.toString());
        for (final String arg : extraArgs) {
            command.add(arg);
        }

        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("PATH", fakeBin.toString() + ":" + System.getenv("PATH"));
        pb.environment().put("XDG_DATA_DIRS", scratch.resolve("share").toString());
        pb.environment().put("HOME", scratch.toString());
        pb.redirectErrorStream(true);

        final Process process = pb.start();
        final String output = new String(process.getInputStream().readAllBytes()).trim();
        final int exitCode = process.waitFor();
        return new ProcessResult(exitCode, output);
    }

    @Test
    void dry_run_with_chrome_desktop_produces_kiosk_command(@TempDir final Path scratch) throws Exception {
        final ProcessResult result = runScript(scratch, "https://example.com", "--dry-run");
        then(result.exitCode).isZero();
        then(result.output).contains("google-chrome").contains("--kiosk").contains("--user-data-dir").contains("google-chrome-profile").contains("https://example.com");
    }

    @Test
    void missing_url_returns_non_zero_exit(@TempDir final Path scratch) throws Exception {
        final ProcessResult result = runScript(scratch, "", "--dry-run");
        then(result.exitCode).isNotZero();
        then(result.output).contains("url required");
    }

    @Test
    void help_prints_usage_and_exits_zero(@TempDir final Path scratch) throws Exception {
        final Path script = extractLaunchScript(scratch);
        final ProcessBuilder pb = new ProcessBuilder(script.toString(), "--help");
        pb.environment().put("PATH", System.getenv("PATH"));
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        final String output = new String(process.getInputStream().readAllBytes()).trim();
        final int exitCode = process.waitFor();
        then(exitCode).isZero();
        then(output).contains("Usage:");
    }

    @Test
    void geometry_with_chrome_dry_run_adds_window_size_and_position(@TempDir final Path scratch) throws Exception {
        final ProcessResult result = runScript(scratch, "https://example.com", "--geometry", "600x800", "--dry-run");
        then(result.exitCode).isZero();
        then(result.output).contains("google-chrome").contains("--window-size=601").contains("--window-position").contains("https://example.com");
    }

    @Test
    void invalid_geometry_returns_non_zero_exit(@TempDir final Path scratch) throws Exception {
        final ProcessResult result = runScript(scratch, "https://example.com", "--geometry", "600", "--dry-run");
        then(result.exitCode).isNotZero();
        then(result.output).contains("invalid --geometry");
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
