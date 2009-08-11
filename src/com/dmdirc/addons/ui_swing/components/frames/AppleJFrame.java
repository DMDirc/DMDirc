package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.addons.ui_swing.*;
import com.dmdirc.addons.ui_swing.components.MenuBar;
import javax.swing.JFrame;

/**
 * Haxy JFrame to allow Apple to show the MenuBar in otherwise parent-less
 * dialogs.
 *
 * @author shane
 */
public class AppleJFrame extends JFrame {
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Create a new Apple JFrame
     *
     * @param parentWindow Main Window
     */
    public AppleJFrame(final MainFrame parentWindow, final SwingController controller) {
        super();
        final MenuBar menu = new MenuBar(controller, parentWindow);
        Apple.getApple().setMenuBar(menu);
        setJMenuBar(menu);
    }
}
