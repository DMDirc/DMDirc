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

package uk.org.ownage.dmdirc.ui.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import uk.org.ownage.dmdirc.Config;

/**
 *
 * @author chris
 */
public class InputHandler implements KeyListener, ActionListener {
    
    private int bufferPosition;
    private int bufferSize;
    private int bufferMaximum;
    private String[] buffer;
    private JTextField target;
    
    /** Creates a new instance of InputHandler */
    public InputHandler(JTextField target) {
        bufferSize = Integer.parseInt(Config.getOption("ui","inputbuffersize"));
        
        this.target = target;
        this.buffer = new String[bufferSize];
        bufferPosition = 0;
        bufferMaximum = 0;
        
        target.addKeyListener(this);
        target.addActionListener(this);
        target.setFocusTraversalKeysEnabled(false);
    }
    
    public void keyTyped(KeyEvent keyEvent) {

    }
    
    public void keyPressed(KeyEvent keyEvent) {
        // Formatting codes
        if ((keyEvent.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            if (keyEvent.getKeyCode() == keyEvent.VK_B) {
                append("" + (char)2);
            }
            if (keyEvent.getKeyCode() == keyEvent.VK_U) {
                append("" + (char)31);
            }
            if (keyEvent.getKeyCode() == keyEvent.VK_O) {
                append("" + (char)15);
            }
            if (keyEvent.getKeyCode() == keyEvent.VK_K) {
                append("" + (char)3);
            }
        }        
    }
    
    public void keyReleased(KeyEvent keyEvent) {
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        buffer[bufferMaximum] = actionEvent.getActionCommand();
        bufferMaximum = (bufferMaximum + 1) % bufferSize;
        bufferPosition = bufferMaximum;
    }

    private void append(String string) {
        target.setText(target.getText()+string);
    }
    
}
