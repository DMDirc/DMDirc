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

import com.dmdirc.Config;
import com.dmdirc.Main;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.dialogs.UpdaterDialog;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * The update checker contacts the DMDirc website to check to see if there
 * are any updates available.
 * @author chris
 */
public final class UpdateChecker implements Runnable, MouseListener {
    
    /** The label used to indicate that there's an update available. */
    private JLabel label;
    
    /** The list of updates that are available. */
    private final List<Update> updates = new ArrayList<Update>();
    
    /** Instantiates an Updatechecker. */
    public UpdateChecker() {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void run() {
        MainFrame.getMainFrame().getStatusBar().setMessage("Checking for updates...");
        
        updates.clear();
        
        URL url;
        URLConnection urlConn;
        DataOutputStream printout;
        BufferedReader printin;
        try {
            url = new URL("http://www.dmdirc.com/update.php");
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            printout = new DataOutputStream(urlConn.getOutputStream());
            final String content = "component=client&channel="
                    + Main.UPDATE_CHANNEL + "&date=" + Main.RELEASE_DATE;
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            printin = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            
            String line = null;
            do {
                if (line != null && line.length() > 0) {
                    checkLine(line);
                }
                
                line = printin.readLine();
            } while (line != null);
            printin.close();
            Config.setOption("updater", "lastcheck", String.valueOf((int) (new Date().getTime() / 1000)));
            UpdateChecker.init();
        } catch (MalformedURLException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to check for updates", ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to check for updates", ex);
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to check for updates", ex);
        }
    }
    
    /**
     * Checks the specified line to determine the message from the update server.
     * @param line The line to be checked
     */
    private void checkLine(final String line) {
        if (line.startsWith("uptodate")) {
            MainFrame.getMainFrame().getStatusBar().setMessage("No updates available");
        } else if (line.startsWith("outofdate")) {
            doUpdateAvailable(line);
        } else {
            Logger.error(ErrorLevel.WARNING, "Unknown response from update server: " + line);
        }
    }
    
    /**
     * Informs the user that there's an update available.
     * @param line The line that was received from the update server
     */
    private void doUpdateAvailable(final String line) {
        updates.add(new Update(line));
        
        if (label == null) {
            final ClassLoader classLoader = getClass().getClassLoader();
            final ImageIcon icon = new ImageIcon(classLoader.getResource("com/dmdirc/res/update.png"));
            
            label.addMouseListener(this);
            label = new JLabel();
            label.setBorder(BorderFactory.createEtchedBorder());
            label.setIcon(icon);
            MainFrame.getMainFrame().getStatusBar().addComponent(label);
        }
    }
    
    /**
     * Initialises the update checker. Sets a timer to check based on the
     * frequency specified in the config.
     */
    public static void init() {
        final int last = Config.getOptionInt("updater", "lastcheck", 0);
        final int freq = Config.getOptionInt("updater", "frequency", 86400);
        final int timestamp = (int) (new Date().getTime() / 1000);
        int time = 0;
        
        if (last + freq > timestamp) {
            time = last + freq - timestamp;
        }
        
        /*
        final List<Update> temp = new ArrayList<Update>();
        temp.add(new Update("outofdate teststuff 20073005 20060101 http://www.example.com/"));
        new UpdaterDialog(temp);
        */
        
        new Timer().schedule(new TimerTask() {
            public void run() {
                new Thread(new UpdateChecker()).start();
            }           
        }, time * 1000);
    }

    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent e) {
        new UpdaterDialog(updates);
    }

    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
        // Do nothing
    }
    
}
