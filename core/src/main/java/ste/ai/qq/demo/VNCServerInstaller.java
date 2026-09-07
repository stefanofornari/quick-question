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
package ste.ai.qq.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Ensures that the bundled Tiger VNC server binaries are installed and
 * up-to-date in the user data directory.
 *
 * <p>The installer checks whether the directory
 * {@code tigervnc-<version>.x86_64} already exists under the destination.
 * If not, the bundled archive is extracted as-is using zip4j, preserving
 * file permissions.</p>
 */
public class VNCServerInstaller {

    public static final String VNC_SERVER_VERSION = "1.16.2"; // hardcoded for now...

    private static final Logger log = Logger.getLogger(VNCServerInstaller.class.getName());

    private final Path sourceArchive;

    /**
     * Creates an installer that reads binaries from the given archive.
     *
     * @param sourceArchive path to the archive containing the VNC binaries
     */
    public VNCServerInstaller(final Path sourceArchive) {
        this.sourceArchive = sourceArchive;
    }

    /**
     * Installs or updates the VNC server binaries into the destination directory.
     *
     * <p>If the directory {@code tigervnc-<version>.x86_64} already exists
     * under the destination, the installer returns without doing anything.
     * Otherwise, the bundled archive is extracted into the destination.</p>
     *
     * @param destination the target directory for the VNC binaries
     * @throws IOException if the archive cannot be read or files cannot be written
     */
    public void installInto(final Path destination) throws IOException {
        final Path installDir = destination.resolve("tigervnc-" + VNC_SERVER_VERSION + ".x86_64");

        if (Files.isDirectory(installDir)) {
            log.log(Level.INFO, "VNC server binaries already installed and up-to-date at {0}", destination);
            return;
        }

        try {
            new ZipFile(sourceArchive.toFile()).extractAll(destination.toString());
        } catch (final ZipException e) {
            throw new IOException("Failed to extract VNC server binaries: " + e.getMessage(), e);
        }

        log.log(Level.INFO, "VNC server binaries installed at {0}", destination);
    }
}
