package com.dmdirc.addons.ui_swing.textpane2;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext;

public class DocumentContentTest {

    public static void main(String[] args) throws BadLocationException {

        final JFrame frame = new JFrame();
        final JTextPane tp = new JTextPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        tp.setEditable(false);
        tp.setDocument(new IRCDocument(new DocumentContent(), new StyleContext()));

        frame.add(new JScrollPane(tp));

        frame.pack();
        
        frame.setVisible(true);
        
        for (int i = 0; i <= 1000; i++) {
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR.... RAR!!!! RARRRR\n", null);
        Thread.yield();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            //Ignore
        }
        
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR1\n", null);
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            //Ignore
        }
        
        tp.getDocument().insertString(tp.getDocument().getLength(), "RAR2\n", null);
    }
}