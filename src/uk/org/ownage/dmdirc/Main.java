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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import uk.org.ownage.dmdirc.identities.IdentityManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * Main class, handles initialisation.
 * @author chris
 */
public final class Main {
    
    /**
     * Stores the current program version.
     */
    public static final String VERSION = "SVN";
    
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
        if (Config.hasOption("ui", "antialias")) {
            final String aaSetting = Config.getOption("ui", "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            if (Config.hasOption("ui", "lookandfeel")) {
                UIManager.setLookAndFeel(Config.getOption("ui", "lookandfeel"));
            }
        } catch (InstantiationException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel", ex);
        } catch (ClassNotFoundException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available", ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available", ex);
        } catch (IllegalAccessException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel", ex);
        }
        
        IdentityManager.load();
        
        MainFrame.getMainFrame();
    }
    
}
