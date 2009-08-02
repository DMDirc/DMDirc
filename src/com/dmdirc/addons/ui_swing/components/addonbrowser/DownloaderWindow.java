/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.ui.CoreUIUtils;
import com.dmdirc.util.DownloadListener;
import com.dmdirc.util.Downloader;

import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

/**
 * Simple window to show download progress on addon feed.
 */
public class DownloaderWindow extends JDialog implements Runnable, DownloadListener {
   
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Downloader progress bar. */
    private final JProgressBar jpb = new JProgressBar(0, 100);
    /** Parent window. */
    private Window parentWindow;

    /**
     * Instantiates a new downloader window.
     *
     * @param parentWindow Parent window
     */
    public DownloaderWindow(final Window parentWindow) {
        super(parentWindow, "DMDirc Addon Browser", ModalityType.MODELESS);
        this.parentWindow = parentWindow;
        setTitle("Downloading addon information...");
        setLayout(new MigLayout("fill"));
        add(jpb, "grow");
        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        CoreUIUtils.centreWindow(this);
        setIconImage(Main.getUI().getMainWindow().getIcon().getImage());
        setVisible(true);
        
        new Thread(this, "Addon downloader thread").start();
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            Downloader.downloadPage("http://addons.dmdirc.com/feed", 
                    Main.getConfigDir() + File.separator + "addons.feed", this);
            new BrowserWindow(parentWindow);
        } catch (IOException ex) {
            removeAll();
            add(new JLabel("Unable to download feed."));
        }
        
        dispose();
    }
    
    /** {@inheritDoc} */
    @Override
    public void downloadProgress(float percent) {
        jpb.setValue((int) percent);
    }

    /** {@inheritDoc} */
    @Override
    public void setIndeterminate(final boolean indeterminate) {
        jpb.setIndeterminate(indeterminate);
    }

}
