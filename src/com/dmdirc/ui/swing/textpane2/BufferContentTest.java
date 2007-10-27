package com.dmdirc.ui.swing.textpane2;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

public class BufferContentTest {

    public static void main(String[] args) throws BadLocationException {

        final JFrame frame = new JFrame();
        final JTextPane tp = new JTextPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        tp.setEditable(false);
        tp.setDocument(new IRCDocument(new StyleContext()));

        frame.add(tp);

        frame.pack();

        frame.setVisible(true);
        
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR\n", null);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //Ignore
        }
        
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR1\n", null);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //Ignore
        }
        
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR2\n", null);
    }
}