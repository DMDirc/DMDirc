/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import uk.org.ownage.dmdirc.identities.IdentityManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.plugins.PluginManager;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * Main class, handles initialisation.
 * @author chris
 */
public final class Main {
    
    /**
     * Stores the current program version.
     */
    public static final String VERSION = "0.3";
    
    /**
     * Prevents creation of main.
     */
    private Main() {
    }
    
    /**
     * Entry procedure.
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final StringBuilder classNameBuilder = new StringBuilder();
        if (Config.hasOption("ui", "antialias")) {
            final String aaSetting = Config.getOption("ui", "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            final FontUIResource font = new FontUIResource("Dialog", Font.PLAIN , 12);
            
            UIManager.put("Label.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("PasswordField.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("List.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("Panel.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("Tree.font", font);
            UIManager.put("InternalFrame.titleFont", font);
            UIManager.put("swing.boldMetal", false);
            UIManager.put("InternalFrame.useTaskBar", false);
            UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
            
            if (Config.hasOption("ui", "lookandfeel")) {
                for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                    if (laf.getName().equals(Config.getOption("ui", "lookandfeel"))) {
                        classNameBuilder.setLength(0);
                        classNameBuilder.append(laf.getClassName());
                        break;
                    }
                }
                
                if (classNameBuilder.length() != 0) {
                    UIManager.setLookAndFeel(classNameBuilder.toString());
                }
            }
        } catch (InstantiationException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel: " + classNameBuilder.toString(), ex);
        } catch (ClassNotFoundException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available: " + classNameBuilder.toString(), ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available: " + classNameBuilder.toString(), ex);
        } catch (IllegalAccessException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel: " + classNameBuilder.toString(), ex);
        }
        
        IdentityManager.load();
        
        PluginManager.getPluginManager();
        
        MainFrame.getMainFrame();
    }
    
}
