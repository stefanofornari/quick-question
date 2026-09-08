/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package ste.netbeans.ai.qq.nb;

import dev.dirs.ProjectDirectories;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import ste.ai.qq.VNCServerInstaller;

/**
 *
 */
@OnStart
public class Install extends ModuleInstall implements Runnable {

    private static final String QUALIFIER = "com.github.stefanofornari";
    private static final String ORGANIZATION = "ste";
    private static final String APPLICATION = "quickquestion";

    private final Logger LOG = Logger.getLogger(Install.class.getName());


    private final File vncJar = InstalledFileLocator.getDefault().locate(
        "modules/mini-tigervnc.jar",                   // Path inside the cluster
        "com.github.stefanofornari.nb.quick.question", // Module's Code Name Base
        false                                          // No localization needed
    );
    final Path userDir = Path.of(ProjectDirectories.from(QUALIFIER, ORGANIZATION, APPLICATION).dataDir);

    @Override
    public void run() {
        LOG.info(() -> "installing QuickQuestion");
        //
        // Let's make sure Tiger VNC bundled with QuickQuestion is installed
        // locally (under ~/.local/share/quickquestion). The installer checks if
        // the bundled version is already installed and updates it if needed.
        //
        LOG.info(() -> "installing mini-tigernvc from %s into %s".formatted(vncJar, userDir));
        if (vncJar != null) {
            final VNCServerInstaller installer = new VNCServerInstaller(vncJar.toPath());
            try {
                installer.installInto(userDir);
            } catch (final IOException e) {
                LOG.severe(() -> "unable to install VNC server binaries: " + e.getMessage());
                Exceptions.printStackTrace(e);
            }
        } else {
            LOG.severe("unable to install VNC server: mini-tigervnc.jar not found");
        }
    }
}

