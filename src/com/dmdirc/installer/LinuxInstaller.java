/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Installs DMDirc on linux
 *
 * @author Shane Mc Cormack
 */
public class LinuxInstaller extends Installer {
	/**
	 * Are we running as root?
	 */
	private boolean isRoot() {
		return (CLIParser.getCLIParser().getParamNumber("-isroot") > 0);
	}
	
	/**
	 * Is the given file name vaild to copy to the installation directory?
	 *
	 * @param filename File to check
	 * @return true If the file should be copied, else false.
	 */
	public boolean validFile(final String filename) {
		return (!filename.equalsIgnoreCase("setup.sh") &&
		        !filename.equalsIgnoreCase("getjre.sh") &&
		        !filename.equalsIgnoreCase("progressbar.sh") &&
		        !filename.equalsIgnoreCase("installjre.sh"));
	}

	/**
	 * Get the default install location
	 */
	public String defaultInstallLocation() {
		String result = "";
		if (CLIParser.getCLIParser().getParamNumber("-directory") > 0) {
			return CLIParser.getCLIParser().getParam("-directory").getStringValue();
		}
		if (result.isEmpty()) {
			if (isRoot()) {
				result = "/usr/local/DMDirc";
			} else {
				result = System.getProperty("user.home") + "/DMDirc";
			}
		}
		return result;
	}

	/**
	 * Check if this OS supports a given shortcut Type
	 *
	 * @param shortcutType Type of shortcut to check
	 * @return True if this OS supports a given shortcut Type
	 */
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
				// Anything else that gets added should be false until the relevent
				// code is added
				return false;
		}
	}

	/**
	 * Setup shortcut
	 *
	 * @param location Location where app will be installed to.
	 * @param shortcutType Type of shortcut to add.
	 */
	public void setupShortcut(final String location, final ShortcutType shortcutType) {
		if (!supportsShortcut(shortcutType)) {
			step.addText(" - Error creating shortcut. Not applicable to this Operating System");
			return;
		}

		PrintWriter writer = null;
		try {
			String filename = "";
			String command = "";

			switch (shortcutType) {
				case DESKTOP:
					filename = System.getProperty("user.home")+"/Desktop/DMDirc.desktop";
					break;

				case MENU:
					if (isRoot()) {
						filename = "/usr/share/applications/DMDirc.desktop";
					} else {
						filename = System.getProperty("user.home")+"/.local/share/applications/DMDirc.desktop";
					}
					break;

				case UNINSTALLER:
					writer = new PrintWriter(location+"/uninstall.sh");
					writer.println("#!/bin/sh");
					writer.println("# DMDirc Uninstaller");
					writer.println("ISKDE=`pidof -x -s kdeinit`");
					writer.println("KDIALOG=`which kdialog`");
					writer.println("ISGNOME=`pidof -x -s gnome-panel`");
					writer.println("ZENITY=`which zenity`");
					writer.println("DIALOG=`which dialog`");

					if (isRoot()) {
						writer.println("USER=`whoami`");
						writer.println("if [ \"${USER}\" != \"root\" ]; then");
						writer.println("if [ ${result} -eq 1 ]; then");
						writer.println("\t\tif [ \"\" != \"${ISKDE}\" -a \"\" != \"${KDIALOG}\" -a \"\" != \"${DISPLAY}\" ]; then");
						writer.println("\t\t	${KDIALOG} --title \"DMDirc Uninstaller\" --error \"Uninstall Aborted. Only root can use this script\"");
						writer.println("\t\telif [ \"\" != \"${ISGNOME}\" -a \"\" != \"${ZENITY}\" -a \"\" != \"${DISPLAY}\" ]; then");
						writer.println("\t\t	${ZENITY} --info --title \"DMDirc Uninstaller\" --text \"Uninstall Aborted. Only root can use this script\"");
						writer.println("\t\tfi");
						writer.println("\t\techo \"Uninstall Aborted. Only root can use this script\"");
						writer.println("\tfi");
						writer.println("exit 1;");
						writer.println("fi");
					}

					writer.println("if [ \"\" != \"${ISKDE}\" -a \"\" != \"${KDIALOG}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("	echo \"Dialog Prompt on: ${DISPLAY}\"");
					writer.println("	${KDIALOG} --title \"DMDirc Uninstaller\" --yesno \"Are you sure you want to uninstall DMDirc?\"");
					writer.println("elif [ \"\" != \"${ISGNOME}\" -a \"\" != \"${ZENITY}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("	echo \"Dialog Prompt on: ${DISPLAY}\"");
					writer.println("	${ZENITY} --question --title \"DMDirc Uninstaller\" --text \"Are you sure you want to uninstall DMDirc?\"");
					writer.println("elif [ \"\" != \"${DIALOG}\" ]; then");
					writer.println("	${DIALOG} --title \"DMDirc Uninstaller\" --yesno \"Are you sure you want to uninstall DMDirc?\" 8 40");
					writer.println("fi");

					writer.println("if [ $? -ne 0 ]; then");
					writer.println("\tif [ \"\" != \"${ISKDE}\" -a \"\" != \"${KDIALOG}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("\t	${KDIALOG} --title \"DMDirc Uninstaller\" --msgbox \"Uninstall Aborted\"");
					writer.println("\telif [ \"\" != \"${ISGNOME}\" -a \"\" != \"${ZENITY}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("\t	${ZENITY} --info --title \"DMDirc Uninstaller\" --text \"Uninstall Aborted\"");
					writer.println("\tfi");
					writer.println("\techo \"Uninstall Aborted\"");
					writer.println("\texit 1;");
					writer.println("fi");

					writer.println("echo \"Uninstalling dmdirc\"");
					writer.println("echo \"Removing Shortcuts..\"");
					if (isRoot()) {
						command = "${TOOL} --config-source=`${TOOL} --get-default-source`";
						filename = "/usr/share/services/irc.protocol";
						writer.println("rm -Rfv /usr/share/applications/DMDirc.desktop");
					} else {
						command = "${TOOL}";
						filename = "~/.kde/share/services/irc.protocol";
						writer.println("rm -Rfv "+System.getProperty("user.home")+"/.local/share/applications/DMDirc.desktop");
						writer.println("rm -Rfv "+System.getProperty("user.home")+"/Desktop/DMDirc.desktop");
					}
					writer.println("TOOL=`which gconftool-2`");
					writer.println("if [ \"${TOOL}\" != \"\" ]; then");
					writer.println("\tCURRENT=`"+command+" --get /desktop/gnome/url-handlers/irc/command`");
					writer.println("\tif [ \"${CURRENT}\" = \"\\\""+location+"/DMDirc.sh\\\" -e -c %s\" ]; then");
					writer.println("\t\techo \"Removing Gnome Protocol Handler\"");
					writer.println("\t\t"+command+" --unset /desktop/gnome/url-handlers/irc/enabled");
					writer.println("\t\t"+command+" --unset /desktop/gnome/url-handlers/irc/command");
					writer.println("\telse");
					writer.println("\t\techo \"Not Removing Gnome Protocol Handler\"");
					writer.println("\tfi");
					writer.println("fi");

					writer.println("if [ -e \""+filename+"\" ]; then");
					writer.println("\tCURRENT=`grep DMDirc "+filename+"`");
					writer.println("\tif [ \"\" != \"${CURRENT}\" ]; then");
					writer.println("\t\techo \"Removing KDE Protocol Handler\"");
					writer.println("\t\trm -Rfv "+filename);
					writer.println("\telse");
					writer.println("\t\techo \"Not Removing KDE Protocol Handler\"");
					writer.println("\tfi");
					writer.println("fi");

					writer.println("echo \"Removing Installation Directory\"");
					writer.println("rm -Rfv \""+location+"\"");
					
					
					writer.println("PROFILEDIR=\"${HOME}/.DMDirc\"");
					writer.println("if [ -e ${PROFILEDIR}/dmdirc.config ]; then");
					writer.println("\tif [ \"\" != \"${ISKDE}\" -a \"\" != \"${KDIALOG}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("\t\techo \"Dialog Prompt on: ${DISPLAY}\"");
					writer.println("\t\t${KDIALOG} --title \"DMDirc Uninstaller\" --yesno \"A dmdirc profile has been detected (${PROFILEDIR})\n Do you want to delete it aswell?\"");
					writer.println("\telif [ \"\" != \"${ISGNOME}\" -a \"\" != \"${ZENITY}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("\t\techo \"Dialog Prompt on: ${DISPLAY}\"");
					writer.println("\t\t${ZENITY} --question --title \"DMDirc Uninstaller\" --text \"A dmdirc profile has been detected (${PROFILEDIR})\n Do you want to delete it aswell\"");
					writer.println("\telif [ \"\" != \"${DIALOG}\" ]; then");
					writer.println("\t\t${DIALOG} --title \"DMDirc Uninstaller\" --yesno \"A dmdirc profile has been detected (${PROFILEDIR})\n Do you want to delete it aswell\" 8 40");
					writer.println("\tfi");

					writer.println("\tif [ $? -eq 0 ]; then");
					writer.println("\t\trm -Rfv \"${PROFILEDIR}\"");
					writer.println("\tfi");
					writer.println("fi");

					writer.println("if [ \"\" != \"${ISKDE}\" -a \"\" != \"${KDIALOG}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("	${KDIALOG} --title \"DMDirc Uninstaller\" --msgbox \"DMDirc Uninstalled Successfully\"");
					writer.println("elif [ \"\" != \"${ISGNOME}\" -a \"\" != \"${ZENITY}\" -a \"\" != \"${DISPLAY}\" ]; then");
					writer.println("	${ZENITY} --info --title \"DMDirc Uninstaller\" --text \"DMDirc Uninstalled Successfully\"");
					writer.println("fi");

					writer.println("echo \"Done.\"");

					(new File(location+"/uninstall.sh")).setExecutable(true);
					return;

				case PROTOCOL:
					if (isRoot()) {
						command = "${TOOL} --config-source=`${TOOL} --get-default-source`";
						filename = "/usr/share/services/";
					} else {
						command = "${TOOL}";
						filename = "${HOME}/.kde/share/services/";
					}

					writer = new PrintWriter(location+"/protocolHandlers.sh");
					writer.println("#!/bin/sh");
					writer.println("TOOL=`which gconftool-2`");
					writer.println("if [ \"${TOOL}\" != \"\" ]; then");
					writer.println("\t"+command+" --set --type=bool /desktop/gnome/url-handlers/irc/enabled true");
					writer.println("\t"+command+" --set --type=string /desktop/gnome/url-handlers/irc/command \"\\\""+location+"/DMDirc.sh\\\" -e -c %s\"");
					writer.println("\t"+command+" --set --type=bool /desktop/gnome/url-handlers/irc/need-terminal false");
					writer.println("fi");
					writer.println("if [ -e \""+filename+"\" ]; then");
					writer.println("\techo \"[Protocol]\" > "+filename+"irc.protocol");
					writer.println("\techo \"exec=\""+location+"/DMDirc.sh\" -e -c %u\" >> "+filename+"irc.protocol");
					writer.println("\techo \"protocol=irc\" >> "+filename+"irc.protocol");
					writer.println("\techo \"input=none\" >> "+filename+"irc.protocol");
					writer.println("\techo \"output=none\" >> "+filename+"irc.protocol");
					writer.println("\techo \"helper=true\" >> "+filename+"irc.protocol");
					writer.println("\techo \"listing=false\" >> "+filename+"irc.protocol");
					writer.println("\techo \"reading=false\" >> "+filename+"irc.protocol");
					writer.println("\techo \"writing=false\" >> "+filename+"irc.protocol");
					writer.println("\techo \"makedir=false\" >> "+filename+"irc.protocol");
					writer.println("\techo \"deleting=false\" >> "+filename+"irc.protocol");
					writer.println("fi");
					writer.println("exit 0;");
					writer.close();

					final File protocolFile = new File(location+"/protocolHandlers.sh");
					protocolFile.setExecutable(true);

					try {
						final Process gconfProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", location+"/protocolHandlers.sh"});
						new StreamReader(gconfProcess.getInputStream()).start();
						new StreamReader(gconfProcess.getErrorStream()).start();
						gconfProcess.waitFor();
						protocolFile.delete();
					} catch (Exception e) {
						step.addText(" - Error adding Protocol Handler: "+e.getMessage());
					}
					return;

				default:
					step.addText(" - Error creating shortcut. Not applicable to this Operating System");
					return;
			}
			File temp = new File(filename);
			if (!temp.getParentFile().exists()) {
				temp.getParentFile().mkdir();
			}
			writer = new PrintWriter(filename);
			writeFile(writer, location);
		} catch (Exception e) {
			step.addText(" - Error creating shortcut: "+e.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Write the .desktop file
	 *
	 * @param writer PrintWriter to write to
	 * @param location Location of installed files
	 * @throws IOException if an error occurs when writing
	 */
	private void writeFile(final PrintWriter writer, final String location) throws IOException {
		writer.println("[Desktop Entry]");
		writer.println("Categories=Network;IRCClient;");
		writer.println("Comment=DMDirc IRC Client");
		writer.println("Encoding=UTF-8");
//		writer.println("Exec=java -jar "+location+"/DMDirc.jar");
		writer.println("Exec="+location+"/DMDirc.sh");
		writer.println("GenericName=IRC Client");
		writer.println("Icon="+location+"/icon.svg");
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

	/**
	 * Any post-install tasks should be done here.
	 *
	 * @param location Location where app was installed to.
	 */
	public void postInstall(final String location) {
		(new File(location+"/DMDirc.sh")).setExecutable(true);
	}
}