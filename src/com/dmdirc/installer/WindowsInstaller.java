/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import java.util.ArrayList;

/**
 * Installs DMDirc on windows
 *
 * @author Shane Mc Cormack
 */
public class WindowsInstaller extends Installer {

    /** {@inheritDoc} */
    @Override
    public String defaultInstallLocation() {
        String result = "";
        if (CLIParser.getCLIParser().getParamNumber("-directory") > 0) {
            result = CLIParser.getCLIParser().getParam("-directory").
                    getStringValue();
        }
        if (result.isEmpty()) {
            String filename = System.getenv("PROGRAMFILES");
            if (filename == null) {
                if (isVista()) {
                    filename = System.getProperty("user.home") +
                               "\\Desktop\\DMDirc";
                } else {
                    filename = "C:\\Program Files";
                }
            }
            result = filename + "\\DMDirc";
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean validFile(final String filename) {
        return (!filename.equalsIgnoreCase("setup.exe") &&
                !filename.equalsIgnoreCase("jre.exe") &&
                !filename.equalsIgnoreCase("wget.exe") &&
                !filename.equalsIgnoreCase("wgetoutput") &&
                !filename.equalsIgnoreCase("shortcut.exe"));
    }

    /**
     * Are we running vista? -_-
     *
     * @return True if this is vista.
     */
    public boolean isVista() {
        return System.getProperty("os.name").indexOf("Vista") >= 0;
    }

    /**
     * Are we running NT?
     *
     * @return True if this is NT.
     */
    public boolean isNT() {
        final String osName = System.getProperty("os.name");
        return (osName.indexOf("NT") >= 0 || osName.indexOf("2000") >= 0 ||
                osName.indexOf("2003") >= 0 || osName.indexOf("XP") >= 0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsShortcut(final ShortcutType shortcutType) {
        switch (shortcutType) {
            case QUICKLAUNCH:
                // Only windows 95 doesn't have quick launch
                return !(System.getProperty("os.name").indexOf("95") >= 0);
            case DESKTOP:
            case MENU:
            case UNINSTALLER:
            case PROTOCOL:
                // All versions of windows have desktop, menu, uninstaller and protocol
                return true;
            default:
                // Anything else that gets added should be false until the relevent
                // code is added
                return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getMenuName() {
        return "Start menu";
    }

    /**
     * Add a registry key.
     *
     * @param key Key to add.
     */
    public void addRegistryKey(final String key) {
        step.addText(" - Adding Key: " + key);
        final String[] addKey = new String[]{"reg.exe", "add", key, "/f"};
        execAndWait(addKey);
    }

    /**
     * Modify a registry value.
     *
     * @param key Key to use.
     * @param value Value to modify.
     * @param data Data for key.
     */
    public void editRegistryValue(final String key, final String value,
                                  final String data) {
        editRegistryValue(key, value, "REG_SZ", data);
    }

    /**
     * Modify a registry value.
     *
     * @param key Key to use.
     * @param value Value to modify.
     * @param type Type of data.
     * @param data Data for key.
     */
    public void editRegistryValue(final String key, final String value,
                                  final String type, final String data) {
        final ArrayList<String> params = new ArrayList<String>();
        step.addText(" - Editing value: " + key + "\\" + value);
        params.add("reg.exe");
        params.add("add");
        params.add(key);
        params.add("/f");
        if (value.isEmpty()) {
            params.add("/ve");
        } else {
            params.add("/v");
            params.add(value);
        }
        params.add("/t");
        params.add(type);
        if (!data.isEmpty()) {
            params.add("/d");
            params.add(data);
        }

        execAndWait(params.toArray(new String[params.size()]));
    }

    /**
     * Execute and wait for the requested command
     *
     * @param cmd Command array to execute/
     * @return return value from command, or -1 if there was an error.
     */
    private int execAndWait(final String cmd[]) {
        try {
            final Process myProcess = Runtime.getRuntime().exec(cmd);
            new StreamReader(myProcess.getInputStream()).start();
            new StreamReader(myProcess.getErrorStream()).start();
            try {
                myProcess.waitFor();
            } catch (InterruptedException e) {
            }
            if (myProcess.exitValue() != 0) {
                step.addText("\t - Error: Unknown Reason");
            }
            return myProcess.exitValue();
        } catch (SecurityException e) {
            step.addText("\t - Error: " + e.getMessage());
        } catch (IOException e) {
            step.addText("\t - Error: " + e.getMessage());
        }

        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public void setupShortcut(final String location,
                              final ShortcutType shortcutType) {
        // Shortcut.exe is from http://www.optimumx.com/download/#Shortcut

        if (!supportsShortcut(shortcutType)) {
            step.addText(
                    " - Error creating shortcut. Not applicable to this Operating System");
            return;
        }

        if (new File("Shortcut.exe").exists()) {
            String filename = "";
            File dir;

            switch (shortcutType) {
                case DESKTOP:
                    if (isNT() || isVista()) {
                        filename = System.getProperty("user.home") + "\\Desktop";
                    } else {
                        filename = System.getenv("WINDIR") + "\\Desktop";
                    }
                    break;

                case MENU:
                    if (isVista()) {
                        filename = System.getenv("APPDATA") +
                                   "\\Microsoft\\Windows";
                    } else {
                        filename = System.getProperty("user.home");
                    }
                    filename = filename + "\\Start Menu\\Programs\\DMDirc";
                    break;

                case QUICKLAUNCH:
                    if (isVista()) {
                        filename =
                        System.getProperty("user.home") +
                        "\\AppData\\Roaming\\Microsoft\\Internet Explorer\\Quick Launch";
                    } else {
                        filename =
                        System.getProperty("user.home") +
                        "\\Application Data\\Microsoft\\Internet Explorer\\Quick Launch";
                    }
                    break;

                case UNINSTALLER:
                    // Registry hax!
                    final String key =
                                 "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\DMDirc";
                    addRegistryKey(key);
                    editRegistryValue(key, "Comments", "DMDirc IRC Client");
                    editRegistryValue(key, "DisplayName", "DMDirc IRC Client");
                    editRegistryValue(key, "DisplayIcon", location +
                                                          "\\icon.ico");
                    editRegistryValue(key, "UninstallString",
                                      location + "\\Uninstaller.exe");
                    editRegistryValue(key, "Publisher", "DMDirc.com");
                    editRegistryValue(key, "URLInfoAbout",
                                      "http://www.DMDirc.com/");
                    editRegistryValue(key, "URLUpdateInfo",
                                      "http://www.DMDirc.com/");
                    editRegistryValue(key, "InstallDir", location);
                    editRegistryValue(key, "InstalledTime", String.valueOf(
                            System.currentTimeMillis()));
                    return;

                case PROTOCOL:
                    // Add needed keys.
                    addRegistryKey("HKCR\\irc");
                    addRegistryKey("HKCR\\irc\\DefaultIcon");
                    addRegistryKey("HKCR\\irc\\Shell");
                    addRegistryKey("HKCR\\irc\\Shell\\open");
                    addRegistryKey("HKCR\\irc\\Shell\\open\\command");
                    // Now the values
                    editRegistryValue("HKCR\\irc", "", "URL:IRC Protocol");
                    editRegistryValue("HKCR\\irc", "URL Protocol", "");
                    editRegistryValue("HKCR\\irc", "EditFlags", "REG_BINARY",
                                      "02000000");
                    editRegistryValue("HKCR\\irc\\DefaultIcon", "", location +
                                                                    "\\icon.ico");
                    editRegistryValue("HKCR\\irc\\Shell\\open\\command", "",
                                      "\\\"" + location +
                                      "\\DMDirc.exe\\\" -e -c %1");
                    return;

                default:
                    step.addText(
                            " - Error creating shortcut. Not applicable to this Operating System");
                    return;
            }

            if (filename.length() == 0) {
                step.addText(
                        " - Error creating shortcut. Not applicable to this System");
                return;
            }

            // Check the dir exists
            dir = new File(filename);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // Delete an older shortcut
            final File oldFile = new File(filename + "\\DMDirc.lnk");
            if (oldFile.exists()) {
                oldFile.delete();
            }

//			final String thisDirName = new File("").getAbsolutePath();
            final String[] command = new String[]{
                //			                      thisDirName+"/Shortcut.exe",
                "Shortcut.exe",
                "/F:" + filename + "\\DMDirc.lnk",
                "/A:C",
                //			                      "/T:"+location+"\\DMDirc.bat",
                //			                      "/T:javaw.exe",
                //			                      "/P:-jar DMDirc.jar",
                "/T:" + location + "\\DMDirc.exe",
                "/W:" + location,
                "/I:" + location + "\\icon.ico",
                "/D:DMDirc IRC Client"
            };
            execAndWait(command);
        } else {
            step.addText(
                    " - Error creating shortcut: Unable to find Shortcut.exe");
        }
    }
}