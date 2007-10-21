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

package com.dmdirc.updater;

import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.Downloader;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * The update checker contacts the DMDirc website to check to see if there
 * are any updates available.
 *
 * @author chris
 */
public final class UpdateChecker extends MouseAdapter implements Runnable {
    
    /** The label used to indicate that there's an update available. */
    private static JLabel label;
    
    /** The list of updates that are available. */
    private static final List<Update> updates = new ArrayList<Update>();
    
    /**
     * Instantiates an Updatechecker.
     */
    public UpdateChecker() {
        //Ignore
    }
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        Main.getUI().getStatusBar().setMessage("Checking for updates...");
        
        updates.clear();
        
        try {
            final String content = "component=client&channel="
                    + Main.UPDATE_CHANNEL + "&date=" + Main.RELEASE_DATE;
            
            final List<String> response
                    = Downloader.getPage("http://www.dmdirc.com/update.php", content);
            
            for (String line : response) {
                checkLine(line);
            }
            
            IdentityManager.getConfigIdentity().setOption("updater",
                    "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
            UpdateChecker.init();
        } catch (MalformedURLException ex) {
            Logger.appError(ErrorLevel.LOW, "Error when checking for updates", ex);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, 
                    "I/O error when checking for updates: " + ex.getMessage());
        }
    }
    
    /**
     * Checks the specified line to determine the message from the update server.
     *
     * @param line The line to be checked
     */
    private void checkLine(final String line) {
        if (line.startsWith("uptodate")) {
            Main.getUI().getStatusBar().setMessage("No updates available");
        } else if (line.startsWith("outofdate")) {
            doUpdateAvailable(line);
        } else {
            Logger.userError(ErrorLevel.LOW, "Unknown update line received from server: "
                    + line);
        }
    }
    
    /**
     * Informs the user that there's an update available.
     *
     * @param line The line that was received from the update server
     */
    public void doUpdateAvailable(final String line) {
        updates.add(new Update(line));
        
        if (label == null) {
            label = new JLabel();
            label.addMouseListener(this);
            label.setBorder(BorderFactory.createEtchedBorder());
            label.setIcon(IconManager.getIconManager().getIcon("update"));
            Main.getUI().getStatusBar().addComponent(label);
        }
    }
    
    /**
     * Initialises the update checker. Sets a timer to check based on the
     * frequency specified in the config.
     */
    public static void init() {
        final int last
                = IdentityManager.getGlobalConfig().getOptionInt("updater", "lastcheck", 0);
        final int freq
                = IdentityManager.getGlobalConfig().getOptionInt("updater", "frequency", 86400);
        final int timestamp = (int) (new Date().getTime() / 1000);
        int time = 0;
        
        if (last + freq > timestamp) {
            time = last + freq - timestamp;
        }
        
        if (time > freq || time < 0) {
            Logger.userError(ErrorLevel.LOW, "Attempted to schedule update check "
                    + (time < 0 ? "in the past" : "too far in the future")
                    + ", rescheduling.");
            time = 1;
        }
                
        new Timer("Update Checker Timer").schedule(new TimerTask() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                new Thread(new UpdateChecker(), "UpdateChecker thread").start();
            }
        }, time * 1000);
    }
    
    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Main.getUI().getUpdaterDialog(updates).display();
        }
    }
    
}
