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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Launches, redirects, and tracks the system default browser on the VNC
 * shared display.
 *
 * <p>The service delegates the platform-specific work to scripts bundled as
 * classpath resources under {@code ste/ai/qq/bin/}. At runtime, the scripts
 * are extracted once to a temporary directory and executed from there.</p>
 */
public class WebChatService {

    private static final String SCRIPTS_RESOURCE_PREFIX = "bin/";
    private static final String LAUNCH_SCRIPT = "launch-browser.sh";
    private static final String NAVIGATE_SCRIPT = "navigate-browser.sh";
    private static final String STOP_SCRIPT = "stop-browser.sh";

    private static final Logger log = Logger.getLogger(WebChatService.class.getName());

    private static volatile Path resourceBinDir;

    private final Path binDir;
    private final Path pidFile;
    private final String display;
    private final ProcessAliveChecker aliveChecker;

    /**
     * Creates a service using scripts extracted from classpath resources,
     * the default PID file location, and the default VNC display {@code :5}.
     */
    public WebChatService() {
        this(extractResourceScripts(), defaultPidFile(), ":5", ProcessAliveChecker.defaultChecker());
    }

    WebChatService(final Path binDir, final Path pidFile, final String display) {
        this(binDir, pidFile, display, ProcessAliveChecker.defaultChecker());
    }

    WebChatService(final Path binDir, final Path pidFile, final String display, final ProcessAliveChecker aliveChecker) {
        this.binDir = binDir;
        this.pidFile = pidFile;
        this.display = display;
        this.aliveChecker = aliveChecker;
    }

    /**
     * Navigates the browser to the given URL.
     *
     * <p>If no browser is running, a new browser is launched in kiosk mode on
     * the shared VNC display. If a browser is already running, it is
     * redirected to the given URL.</p>
     *
     * @param url the target URL
     * @throws WebChatException if the underlying script fails
     */
    public void navigateTo(final URL url) throws WebChatException {
        log.finest(() -> "navigateTo: " + url);
        if (isRunning()) {
            redirect(url);
        } else {
            launch(url);
        }
    }

    /**
     * Returns {@code true} if a browser process is currently tracked and
     * alive.
     */
    public boolean isRunning() {
        if (!Files.isRegularFile(pidFile)) {
            log.finest("isRunning: no pid file");
            return false;
        }
        try {
            final String content = Files.readString(pidFile).trim();
            if (content.isEmpty()) {
                log.finest("isRunning: empty pid file");
                return false;
            }
            final long pid = Long.parseLong(content);
            final boolean alive = aliveChecker.isAlive(pid);
            log.finest(() -> "isRunning: pid=" + pid + " alive=" + alive);
            return alive;
        } catch (final IOException | NumberFormatException e) {
            log.finest(() -> "isRunning: " + e);
            return false;
        }
    }

    /**
     * Stops the running browser, if any, and clears the tracked PID.
     *
     * @throws WebChatException if the pid file cannot be removed
     */
    public void stop() throws WebChatException {
        if (!isRunning()) {
            log.finest("stop: browser not running");
            return;
        }
        log.finest("stop: stopping browser");
        try {
            final String content = Files.readString(pidFile).trim();
            final long pid = Long.parseLong(content);
            ProcessHandle.of(pid).ifPresent(handle -> {
                if (!handle.destroy()) {
                    handle.destroyForcibly();
                }
            });
        } catch (final IOException | NumberFormatException e) {
            log.fine(() -> "stop: failed to read pid for kill: " + e.getMessage());
        }
        try {
            Files.deleteIfExists(pidFile);
        } catch (final IOException e) {
            throw new WebChatException("Failed to remove pid file: " + pidFile, e);
        }
    }

    private void launch(final URL url) throws WebChatException {
        log.finest(() -> "launch: " + url);
        execute(LAUNCH_SCRIPT, display, pidFile.toString(), url.toExternalForm());
        if (!Files.isRegularFile(pidFile)) {
            throw new WebChatException("Launch script did not create pid file: " + pidFile);
        }
    }

    private void redirect(final URL url) throws WebChatException {
        log.finest(() -> "redirect: " + url);
        execute(NAVIGATE_SCRIPT, display, pidFile.toString(), url.toExternalForm());
    }

    private void execute(final String scriptName, final String... args) throws WebChatException {
        final Path script = binDir.resolve(scriptName);
        final List<String> command = command(script, args);
        try {
            final ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            final Process process = pb.start();
            final String output = new String(process.getInputStream().readAllBytes()).trim();
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new WebChatException(scriptName + " failed (exit " + exitCode + "): " + output);
            }
        } catch (final IOException e) {
            throw new WebChatException("Failed to execute " + scriptName + ": " + e.getMessage(), e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebChatException("Interrupted while executing " + scriptName, e);
        }
    }

    private List<String> command(final Path script, final String... args) {
        final List<String> cmd = new ArrayList<>();
        if (Files.isExecutable(script)) {
            cmd.add(script.toString());
        } else {
            cmd.add("bash");
            cmd.add(script.toString());
        }
        for (final String arg : args) {
            cmd.add(arg);
        }
        return cmd;
    }

    private static Path extractResourceScripts() {
        Path cached = resourceBinDir;
        if (cached != null) {
            return cached;
        }
        synchronized (WebChatService.class) {
            if (resourceBinDir == null) {
                resourceBinDir = doExtractResourceScripts();
            }
            return resourceBinDir;
        }
    }

    private static Path doExtractResourceScripts() {
        try {
            final Path tempDir = Files.createTempDirectory("quickquestion-scripts-");
            final String[] scripts = {LAUNCH_SCRIPT, NAVIGATE_SCRIPT, STOP_SCRIPT};
            final ClassLoader cl = WebChatService.class.getClassLoader();
            for (final String script : scripts) {
                final String resourcePath = SCRIPTS_RESOURCE_PREFIX + script;
                try (final InputStream is = cl.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        throw new WebChatException("Missing script resource: " + resourcePath);
                    }
                    final Path target = tempDir.resolve(script);
                    Files.copy(is, target);
                    target.toFile().setExecutable(true);
                }
            }
            return tempDir;
        } catch (final IOException e) {
            throw new WebChatException("Failed to extract scripts from resources", e);
        }
    }

    private static Path defaultPidFile() {
        return Path.of(System.getProperty("java.io.tmpdir"), "quickquestion", "browser.pid");
    }

    @FunctionalInterface
    interface ProcessAliveChecker {
        boolean isAlive(long pid);

        static ProcessAliveChecker defaultChecker() {
            return pid -> ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
        }
    }
}
