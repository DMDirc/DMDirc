/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.installer;

import com.dmdirc.installer.cliparser.CLIParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Installs DMDirc on linux
 * 
 * @author Shane Mc Cormack
 */
public class LinuxInstaller extends Installer {
    /**
     * Are we running as root?
     * 
     * @return true iif running as root
     */
    private boolean isRoot() {
        return CLIParser.getCLIParser().getParamNumber("-isroot") > 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean validFile(final String filename) {
        return !filename.equalsIgnoreCase("setup.sh")
                && !filename.equalsIgnoreCase("getjre.sh")
                && !filename.equalsIgnoreCase("jre.bin")
                && !filename.equalsIgnoreCase("java-bin")
                && !filename.equalsIgnoreCase("progressbar.sh")
                && !filename.equalsIgnoreCase("installjre.sh")
                && !filename.matches(".*pipe.*");
    }

    /** {@inheritDoc} */
    @Override
    public String defaultInstallLocation() {
        String result = "";
        if (CLIParser.getCLIParser().getParamNumber("-directory") > 0) {
            return CLIParser.getCLIParser().getParam("-directory")
                    .getStringValue();
        }
        if (result.isEmpty()) {
            if (isRoot()) {
                result = "/opt/dmdirc";
            } else {
                result = System.getProperty("user.home") + "/DMDirc";
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsShortcut(final ShortcutType shortcutType) {
        switch (shortcutType) {
            case QUICKLAUNCH:
                return false;
            case DESKTOP:
                // No desktop for root
                return !isRoot();
            case PROTOCOL:
            case UNINSTALLER:
            case MENU:
                // Both root and non-root have a menu, uninstaller, and protocol
                return true;
            default:
                // Anything else that gets added should be false until the
                // relevent
                // code is added
                return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setupShortcut(final String location,
            final ShortcutType shortcutType) {
        if (!supportsShortcut(shortcutType)) {
            step
                    .addText(" - Error creating shortcut. Not applicable to this Operating System");
            return;
        }

        PrintWriter writer = null;
        try {
            String filename = "";
            String command = "";

            switch (shortcutType) {
                case DESKTOP:
                    filename = System.getProperty("user.home")
                            + "/Desktop/DMDirc.desktop";
                    break;

                case MENU:
                    if (isRoot()) {
                        filename = "/usr/share/applications/DMDirc.desktop";
                    } else {
                        filename = System.getProperty("user.home")
                                + "/.local/share/applications/DMDirc.desktop";
                    }
                    break;

                case UNINSTALLER:
                    // Write config for uninstaller
                    writer = new PrintWriter(location + "/.uninstall.conf");
                    writer.println("#!/bin/sh");
                    writer.println("# DMDirc Uninstaller Settings");
                    writer.println("INSTALLED_AS_ROOT="
                            + (isRoot() ? "1" : "0"));
                    writer.println("INSTALL_LOCATION=" + location);

                    // Make sure uninstaller is executeable
                    new File(location + "/uninstall.sh").setExecutable(true);
                    return;

                case PROTOCOL:
                    if (isRoot()) {
                        command = "${TOOL} --config-source=`${TOOL} --get-default-source`";
                        filename = "/usr/share/services/";
                    } else {
                        command = "${TOOL}";
                        filename = "${HOME}/.kde/share/services/";
                    }

                    writer = new PrintWriter(location + "/protocolHandlers.sh");
                    writer.println("#!/bin/sh");
                    writer.println("TOOL=`which gconftool-2`");
                    writer.println("if [ \"${TOOL}\" != \"\" ]; then");
                    writer
                            .println("\t"
                                    + command
                                    + " --set --type=bool /desktop/gnome/url-handlers/irc/enabled true");
                    writer
                            .println("\t"
                                    + command
                                    + " --set --type=string /desktop/gnome/url-handlers/irc/command \"\\\""
                                    + location + "/DMDirc.sh\\\" -e -c %s\"");
                    writer
                            .println("\t"
                                    + command
                                    + " --set --type=bool /desktop/gnome/url-handlers/irc/need-terminal false");
                    writer.println("fi");
                    writer.println("if [ ! -e \"" + filename + "\" ]; then");
                    writer.println("\tmkdir -p \"" + filename + "\"");
                    writer.println("fi");
                    writer.println("echo \"[Protocol]\" > " + filename
                            + "irc.protocol");
                    writer.println("echo \"exec=\"" + location
                            + "/DMDirc.sh\" -e -c %u\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"protocol=irc\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"input=none\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"output=none\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"helper=true\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"listing=false\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"reading=false\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"writing=false\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"makedir=false\" >> " + filename
                            + "irc.protocol");
                    writer.println("echo \"deleting=false\" >> " + filename
                            + "irc.protocol");
                    writer.println("chmod a+x " + filename + "irc.protocol");
                    writer.println("exit 0;");
                    writer.close();

                    final File protocolFile = new File(location
                            + "/protocolHandlers.sh");
                    protocolFile.setExecutable(true);

                    try {
                        final Process gconfProcess = Runtime.getRuntime().exec(
                                new String[] { "/bin/sh",
                                        location + "/protocolHandlers.sh" });
                        new StreamReader(gconfProcess.getInputStream()).start();
                        new StreamReader(gconfProcess.getErrorStream()).start();
                        try {
                            gconfProcess.waitFor();
                        } catch (final InterruptedException e) {
                        }
                        protocolFile.delete();
                    } catch (final SecurityException e) {
                        step.addText(" - Error adding Protocol Handler: "
                                + e.getMessage());
                    } catch (final IOException e) {
                        step.addText(" - Error adding Protocol Handler: "
                                + e.getMessage());
                    }
                    return;

                default:
                    step
                            .addText(" - Error creating shortcut. Not applicable to this Operating System");
                    return;
            }
            final File outFile = new File(filename);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdir();
            }
            writer = new PrintWriter(outFile);
            writeFile(writer, location);

            outFile.setExecutable(true);
        } catch (final IOException e) {
            step.addText(" - Error creating shortcut: " + e.toString());
        } catch (final SecurityException e) {
            step.addText(" - Error creating shortcut: " + e.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Write the .desktop file
     * 
     * @param writer
     *            PrintWriter to write to
     * @param location
     *            Location of installed files
     * @throws IOException
     *             if an error occurs when writing
     */
    private void writeFile(final PrintWriter writer, final String location)
            throws IOException {
        writer.println("[Desktop Entry]");
        writer.println("Categories=Network;IRCClient;");
        writer.println("Comment=DMDirc IRC Client");
        writer.println("Encoding=UTF-8");
        // writer.println("Exec=java -jar "+location+"/DMDirc.jar");
        writer.println("Exec=" + location + "/DMDirc.sh");
        writer.println("GenericName=IRC Client");
        writer.println("Icon=" + location + "/icon.svg");
        if (isRoot()) {
            writer.println("Name=DMDirc (Global)");
        } else {
            writer.println("Name=DMDirc");
        }
        writer.println("StartupNotify=true");
        writer.println("Terminal=false");
        writer.println("TerminalOptions=");
        writer.println("Type=Application");
    }

    /** {@inheritDoc} */
    @Override
    public void postInstall(final String location) {
        new File(location + "/DMDirc.sh").setExecutable(true, !isRoot());
    }
}
