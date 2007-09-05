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

package com.dmdirc.ui.swing;


import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandParser;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.UpdaterDialog;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.components.SwingPreferencesPanel;
import com.dmdirc.ui.swing.components.SwingStatusBar;
import com.dmdirc.ui.swing.dialogs.SwingUpdaterDialog;
import com.dmdirc.ui.swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.ui.swing.dialogs.firstrunwizard.SwingFirstRunWizard;
import com.dmdirc.ui.swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.updater.Update;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

/**
 * Controls the main swing UI.
 *
 * @author Chris
 */
public final class SwingController implements UIController {
    
    /**
     * Singleton instance of MainFrame.
     */
    private static MainFrame me;
    
    private static SwingStatusBar statusBar;
    
    /** Instantiates a new SwingController. */
    public SwingController() {
        //Do nothing
    }
    
    /** {@inheritDoc} */
    public synchronized MainFrame getMainWindow() {
        if (me == null) {
            statusBar = new SwingStatusBar();
            me = new MainFrame(statusBar);
        }
        return me;
    }
    
    /** {@inheritDoc} */
    public synchronized StatusBar getStatusBar() {
        if (statusBar == null)  {
            getMainWindow();
        }
        
        return statusBar;
    }
    
    /** {@inheritDoc} */
    public FrameManager getFrameManager() {
        return getMainWindow().getFrameManager();
    }
    
    /** {@inheritDoc} */
    public ChannelWindow getChannel(final Channel channel) {
        return new ChannelFrame(channel);
    }
    
    /** {@inheritDoc} */
    public ServerWindow getServer(final Server server) {
        return new ServerFrame(server);
    }
    
    /** {@inheritDoc} */
    public QueryWindow getQuery(final Query query) {
        return new QueryFrame(query);
    }
    
    /** {@inheritDoc} */
    public Window getWindow(final FrameContainer owner) {
        return new CustomFrame(owner);
    }
    
    /** {@inheritDoc} */
    public InputWindow getInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        return new CustomInputFrame(owner, commandParser);
    }
    
    /** {@inheritDoc} */
    public PreferencesPanel getPreferencesPanel(
            final PreferencesInterface parent, final String title) {
        return new SwingPreferencesPanel(parent, title);
    }
    
    /** {@inheritDoc} */
    public UpdaterDialog getUpdaterDialog(final List<Update> updates) {
        return new SwingUpdaterDialog(updates);
    }
    
    /** {@inheritDoc} */
    public void showFirstRunWizard() {
        new SwingFirstRunWizard().display();
    }
    
    /** {@inheritDoc} */
    public void showChannelSettingsDialog(final Channel channel) {
        ChannelSettingsDialog.getChannelSettingDialog(channel).setVisible(true);
    }
    
    /** {@inheritDoc} */
    public void showServerSettingsDialog(final Server server) {
        new ServerSettingsDialog(server).setVisible(true);
    }
    
    /** {@inheritDoc} */
    public void initUISettings() {
        // For this to work it *HAS* to be before anything else UI related.
        if (Config.hasOption("ui", "antialias")) {
            final String aaSetting = Config.getOption("ui", "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        }
        final String lnfName = getLookAndFeel(Config.getOption("ui", "lookandfeel"));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            final FontUIResource font = new FontUIResource("Dialog", Font.PLAIN , 12);
            
            UIManager.put("TableHeader.font", font);
            UIManager.put("ToggleButton.font", font);
            UIManager.put("ScrollPane.font", font);
            UIManager.put("Spinner.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("Slider.font", font);
            UIManager.put("OptionPane.font", font);
            UIManager.put("ToolBar.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("MenuBar.font", font);
            UIManager.put("PopupMenu.font", font);
            UIManager.put("DesktopIcon.font", font);
            UIManager.put("TextPane.font", font);
            UIManager.put("ProgressBar.font", font);
            UIManager.put("FormattedTextField.font", font);
            UIManager.put("ColorChooser.font", font);
            UIManager.put("Viewport.font", font);
            UIManager.put("ToolTip.font", font);
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
            UIManager.put("EditorPane.font", font);
            UIManager.put("TextArea.font", font);
            
            UIManager.put("CheckBoxMenuItem.acceleratorFont",
                    font.deriveFont((float) font.getSize() - 2));
            UIManager.put("Menu.acceleratorFont",
                    font.deriveFont((float) font.getSize() - 2));
            UIManager.put("MenuItem.acceleratorFont",
                    font.deriveFont((float) font.getSize() - 2));
            
            UIManager.put("swing.boldMetal", false);
            UIManager.put("InternalFrame.useTaskBar", false);
            UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
            UIManager.put("Tree.scrollsOnExpand", true);
            UIManager.put("Tree.scrollsHorizontallyAndVertically", true);
            
            UIManager.put("Tree.dropCellBackground", Color.WHITE);
            UIManager.put("Tree.selectionBackground", Color.WHITE);
            UIManager.put("Tree.textBackground", Color.WHITE);
            UIManager.put("Tree.selectionBorderColor", Color.WHITE);
            UIManager.put("Tree.drawsFocusBorder", false);
            UIManager.put("Tree.drawHorizontalLines", true);
            UIManager.put("Tree.drawVerticalLines", true);
            UIManager.put("Tree.background", Color.WHITE);
            
            //These are likely to change lots, and i cant test them - Greboid
            UIManager.put("apple.awt.showGrowBox", true);
            UIManager.put("apple.laf.useScreenMenuBar", true);
            UIManager.put("com.apple.mrj.application.apple.menu.about.name", "DMDirc: " + Main.VERSION);
            UIManager.put("com.apple.mrj.application.growbox.intrudes", false);
            UIManager.put("com.apple.mrj.application.live-resize", true);
            
            if (Config.hasOption("ui", "lookandfeel") && !lnfName.isEmpty()) {
                UIManager.setLookAndFeel(lnfName);
            }
        } catch (InstantiationException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set look and feel: " + lnfName);
        } catch (ClassNotFoundException ex) {
            Logger.userError(ErrorLevel.LOW, "Look and feel not available: " + lnfName);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.userError(ErrorLevel.LOW, "Look and feel not available: " + lnfName);
        } catch (IllegalAccessException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set look and feel: " + lnfName);
        }
        
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new DMDircEventQueue());
    }
    
    /**
     * Returns the class name of the look and feel from its display name.
     *
     * @param displayName Look and feel display name
     *
     * @return Look and feel class name or a zero length string
     */
    private String getLookAndFeel(final String displayName) {
        final StringBuilder classNameBuilder = new StringBuilder();
        
        if (displayName != null && !displayName.isEmpty()) {
            for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if (laf.getName().equals(displayName)) {
                    classNameBuilder.setLength(0);
                    classNameBuilder.append(laf.getClassName());
                    break;
                }
            }
        }
        return classNameBuilder.toString();
    }
    
}
