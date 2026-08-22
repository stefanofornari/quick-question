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

import dev.dirs.BaseDirectories;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Launches, redirects, and tracks the system default browser on the VNC
 * shared display.
 *
 * <p>The service delegates the platform-specific work to scripts bundled as
 * classpath resources under {@code bin/}. At runtime, the scripts are
 * extracted to the user data directory at {@code <dataDir>/quickquestion/bin}
 * and executed from there.</p>
 */
public class WebChatService {

    private static final String SCRIPTS_RESOURCE_PREFIX = "bin/";
    private static final String LAUNCH_SCRIPT = "launch-browser.sh";
    private static final String STOP_SCRIPT = "stop-browser.sh";

    private static final Logger log = Logger.getLogger(WebChatService.class.getName());

    private static volatile Path resourceBinDir;

    private final Path binDir;
    private final Path pidFile;
    private final String display;
    private final Path profileDir;
    private final Path forcedBrowserBin;
    private final ProcessAliveChecker aliveChecker;
    private String geometry = "600x800";

    /**
     * Creates a service using scripts extracted from classpath resources,
     * the default PID file location, the default VNC display {@code :5},
     * and a dedicated browser profile under the user data directory.
     */
    public WebChatService() {
        this(extractResourceScripts(), defaultPidFile(), ":5", defaultProfileDir(), ProcessAliveChecker.defaultChecker(), null);
    }

    protected WebChatService(final Path binDir, final Path pidFile, final String display) {
        this(binDir, pidFile, display, defaultProfileDir(), ProcessAliveChecker.defaultChecker(), null);
    }

    protected WebChatService(final Path binDir, final Path pidFile, final String display, final ProcessAliveChecker aliveChecker) {
        this(binDir, pidFile, display, defaultProfileDir(), aliveChecker, null);
    }

    protected WebChatService(final Path binDir, final Path pidFile, final String display, final Path profileDir, final ProcessAliveChecker aliveChecker) {
        this(binDir, pidFile, display, profileDir, aliveChecker, null);
    }

    protected WebChatService(final Path binDir, final Path pidFile, final String display, final Path profileDir, final ProcessAliveChecker aliveChecker, final Path forcedBrowserBin) {
        this.binDir = binDir;
        this.pidFile = pidFile;
        this.display = display;
        this.profileDir = profileDir;
        this.aliveChecker = aliveChecker;
        this.forcedBrowserBin = forcedBrowserBin;
    }

    /**
     * Sets the initial browser window geometry.
     *
     * <p>The value must be in {@code WxH} form, e.g. {@code 600x800}.
     * If not set, the service defaults to {@code 600x800}.</p>
     *
     * @param geometry the geometry string, or {@code null}/blank to omit
     *     geometry and let the browser use its own default size
     */
    public void setGeometry(final String geometry) {
        this.geometry = geometry;
    }

    /**
     * Navigates the browser to the given URL.
     *
     * <p>If no browser is running, a new browser is launched in kiosk mode on
     * the shared VNC display. If a browser is already running, it is stopped
     * and a new session is launched for the given URL.</p>
     *
     * @param url the target URL
     * @throws WebChatException if the underlying script fails
     */
    public void navigateTo(final URL url) throws WebChatException {
        log.finest(() -> "navigateTo: " + url);
        if (isRunning()) {
            stop();
        }
        launch(url);
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
        log.finest("stop: stopping browser");
        try {
            killExisting().get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebChatException("Interrupted while waiting for browser to stop", e);
        } catch (final ExecutionException e) {
            throw new WebChatException("Failed to stop browser", e.getCause());
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new WebChatException("Timed out waiting for browser to stop", e);
        } finally {
            try {
                Files.deleteIfExists(pidFile);
            } catch (final IOException e) {
                throw new WebChatException("Failed to remove pid file: " + pidFile, e);
            }
        }
    }

    private CompletableFuture<Void> killExisting() {
        if (!isRunning()) {
            return CompletableFuture.completedFuture(null);
        }
        log.finest("killExisting: stopping browser");
        try {
            final String content = Files.readString(pidFile).trim();
            final long pid = Long.parseLong(content);
            final ProcessHandle handle = ProcessHandle.of(pid).orElse(null);
            if (handle == null) {
                return CompletableFuture.completedFuture(null);
            }
            return stopProcessTreeGracefullyAsync(handle, 5);
        } catch (final IOException | NumberFormatException e) {
            log.fine(() -> "killExisting: failed to read pid for kill: " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private static CompletableFuture<Void> stopProcessTreeGracefullyAsync(ProcessHandle rootHandle, long timeoutSeconds) {
        List<ProcessHandle> allHandles = Stream.concat(
                rootHandle.descendants(),
                Stream.of(rootHandle)
        ).toList();

        allHandles.forEach(ProcessHandle::destroy);

        CompletableFuture<?>[] exitFutures = allHandles.stream()
                .map(ProcessHandle::onExit)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(exitFutures)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .exceptionallyCompose(ex -> {
                    allHandles.stream()
                              .filter(ProcessHandle::isAlive)
                              .forEach(process -> log.finest("not closed: " + process));

                    return CompletableFuture.completedFuture(null);
                })
                ;
    }

    private void launch(final URL url) throws WebChatException {
        log.finest(() -> "launch: " + url);
        final List<String> args = new ArrayList<>();
        args.add(url.toExternalForm());
        args.add("--display");
        args.add(display);
        args.add("--pid-file");
        args.add(pidFile.toString());
        args.add("--profile-dir");
        args.add(profileDir.toString());
        if (forcedBrowserBin != null) {
            args.add("--browser-bin");
            args.add(forcedBrowserBin.toString());
        }
        if (geometry != null && !geometry.isBlank()) {
            args.add("--geometry");
            args.add(geometry);
        }
        execute(LAUNCH_SCRIPT, args.toArray(new String[0]));
        if (!Files.isRegularFile(pidFile)) {
            throw new WebChatException("Launch script did not create pid file: " + pidFile);
        }
    }

    private void execute(final String scriptName, final String... args) throws WebChatException {
        final Path script = binDir.resolve(scriptName);
        final List<String> command = command(script, args);
        log.finest(() -> "command: " + command);
        try {
            final ProcessBuilder pb = new ProcessBuilder(command);
            final Process process = pb.start();
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new WebChatException(scriptName + " failed (exit " + exitCode + ")");
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
            final Path targetDir = Path.of(BaseDirectories.get().dataDir).resolve("quickquestion").resolve("bin");
            Files.createDirectories(targetDir);
            final String[] scripts = {LAUNCH_SCRIPT, STOP_SCRIPT};
            final ClassLoader cl = WebChatService.class.getClassLoader();
            for (final String script : scripts) {
                final Path target = targetDir.resolve(script);
                if (Files.notExists(target)) {
                    final String resourcePath = SCRIPTS_RESOURCE_PREFIX + script;
                    try (final InputStream is = cl.getResourceAsStream(resourcePath)) {
                        if (is == null) {
                            throw new WebChatException("Missing script resource: " + resourcePath);
                        }
                        Files.copy(is, target);
                    }
                }
                target.toFile().setExecutable(true);
            }
            return targetDir;
        } catch (final IOException e) {
            throw new WebChatException("Failed to extract scripts from resources", e);
        }
    }

    private static Path defaultPidFile() {
        return Path.of(System.getProperty("java.io.tmpdir"), "quickquestion", "browser.pid");
    }

    private static Path defaultProfileDir() {
        final Path dir = Path.of(BaseDirectories.get().dataDir).resolve("quickquestion");
        try {
            Files.createDirectories(dir);
        } catch (final IOException e) {
            throw new WebChatException("Failed to create browser profile directory: " + dir, e);
        }
        return dir;
    }

    @FunctionalInterface
    interface ProcessAliveChecker {
        boolean isAlive(long pid);

        static ProcessAliveChecker defaultChecker() {
            return pid -> ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
        }
    }
}
