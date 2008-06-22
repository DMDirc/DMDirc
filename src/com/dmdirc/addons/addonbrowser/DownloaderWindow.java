/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.util.DownloadListener;
import com.dmdirc.util.Downloader;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

/**
 *
 * @author chris
 */
public class DownloaderWindow extends JDialog implements Runnable, DownloadListener {
   
    private final JProgressBar jpb = new JProgressBar(0, 100);

    public DownloaderWindow() {
        setTitle("Downloading addon information...");
        add(jpb);
        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo((Component) Main.getUI().getMainWindow());
        setIconImage(Main.getUI().getMainWindow().getIcon().getImage());
        setVisible(true);
        
        new Thread(this, "Addon downloader thread").start();
    }

    @Override
    public void run() {
        try {
            Downloader.downloadPage("http://addons.dmdirc.com/feed", 
                    Main.getConfigDir() + File.separator + "addons.feed", this);
            new BrowserWindow();
        } catch (IOException ex) {
            // Do nothing
        }
        
        dispose();
    }
    
    @Override
    public void downloadProgress(float percent) {
        jpb.setValue((int) percent);
    }

}
