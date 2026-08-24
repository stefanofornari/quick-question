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
package ste.ai.qq.demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ste.xtest.logging.ListLogHandler;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class VNCServerInstallerSpec {

    @Test
    void vnc_server_binaries_already_installed_and_up_to_date_does_not_modify_installation(@TempDir final Path tempDir) throws Exception {
        final Path sourceJar = Path.of(VNCServerInstallerSpec.class.getClassLoader()
            .getResource("tigervnc-1.16.2.x86_64.jar").toURI());
        final Path destination = tempDir.resolve("quickquestion");
        final Path installDir = destination.resolve("tigervnc-1.16.2.x86_64");
        Files.createDirectories(installDir);

        final Logger logger = Logger.getLogger(VNCServerInstaller.class.getName());
        final ListLogHandler handler = new ListLogHandler();
        logger.addHandler(handler);

        try {
            new VNCServerInstaller(sourceJar).installInto(destination);

            then(installDir).exists().isDirectory();
            ste.xtest.logging.LogAssertions.then(handler.getRecords())
                .containsINFO("VNC server binaries already installed and up-to-date at {0}");
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void vnc_server_binaries_missing_installs_them(@TempDir final Path tempDir) throws Exception {
        final Path sourceJar = Path.of(VNCServerInstallerSpec.class.getClassLoader()
            .getResource("tigervnc-1.16.2.x86_64.jar").toURI());
        final Path destination = tempDir.resolve("quickquestion");

        final Logger logger = Logger.getLogger(VNCServerInstaller.class.getName());
        final ListLogHandler handler = new ListLogHandler();
        logger.addHandler(handler);

        try {
            new VNCServerInstaller(sourceJar).installInto(destination);

            final Path installDir = destination.resolve("tigervnc-1.16.2.x86_64");
            final Path vncBin = installDir.resolve("usr").resolve("bin").resolve("Xvnc");
            then(installDir).exists().isDirectory();
            then(vncBin).exists().isRegularFile();
            ste.xtest.logging.LogAssertions.then(handler.getRecords())
                .containsINFO("VNC server binaries installed at {0}");
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void vnc_server_binaries_outdated_updates_them(@TempDir final Path tempDir) throws Exception {
        final Path sourceJar = Path.of(VNCServerInstallerSpec.class.getClassLoader()
            .getResource("tigervnc-1.16.2.x86_64.jar").toURI());
        final Path destination = tempDir.resolve("quickquestion");
        final Path oldInstallDir = destination.resolve("tigervnc-1.16.1.x86_64");
        Files.createDirectories(oldInstallDir);

        final Logger logger = Logger.getLogger(VNCServerInstaller.class.getName());
        final ListLogHandler handler = new ListLogHandler();
        logger.addHandler(handler);

        try {
            new VNCServerInstaller(sourceJar).installInto(destination);

            final Path newInstallDir = destination.resolve("tigervnc-1.16.2.x86_64");
            final Path vncBin = newInstallDir.resolve("usr").resolve("bin").resolve("Xvnc");
            then(newInstallDir).exists().isDirectory();
            then(vncBin).exists().isRegularFile();
            ste.xtest.logging.LogAssertions.then(handler.getRecords())
                .containsINFO("VNC server binaries installed at {0}");
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void vnc_server_package_not_found_raises_error(@TempDir final Path tempDir) {
        final Path missingJar = tempDir.resolve("missing").resolve("tiger-vnc-mini-1.16.2.jar");
        final Path destination = tempDir.resolve("quickquestion");

        thenThrownBy(() -> new VNCServerInstaller(missingJar).installInto(destination))
            .isInstanceOf(java.io.IOException.class);
    }
}
