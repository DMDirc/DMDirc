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

package com.dmdirc.ui.swing.components;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.components.ExecutableFileFilter;
import com.dmdirc.util.URLHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/** 
 * URL Protocol configuration panel. 
 */
public class URLProtocolPanel extends JPanel implements ActionListener,
        DocumentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** File chooser. */
    private JFileChooser fileChooser;
    /** Show file chooser. */
    private JButton showFileChooser;
    /** Command text field. */
    private JTextField commandPath;
    /** Option selection. */
    private ButtonGroup optionType;
    /** DMDirc choice. */
    private JRadioButton dmdirc;
    /** Browser choice. */
    private JRadioButton browser;
    /** Mail choice. */
    private JRadioButton mail;
    /** Custom command choice. */
    private JRadioButton custom;
    /** Blurb label. */
    private JLabel blurbLabel;
    /** Substitutions label */
    private JLabel subsLabel;
    /** example label. */
    private JLabel exampleLabel;
    /** URL. */
    private final URI url;

    /**
     * Instantiates the URLDialog.
     *
     * @param url URL to open once added
     */
    public URLProtocolPanel(final URI url) {
        super();

        this.url = url;

        initComponents();
        layoutComponents();
        addListeners();
    }

    /** Initialises the components. */
    private void initComponents() {
        fileChooser = new JFileChooser();
        showFileChooser = new JButton();
        blurbLabel = new JLabel();
        commandPath = new JTextField();
        optionType = new ButtonGroup();
        dmdirc = new JRadioButton("Handle internally (irc links only)");
        browser = new JRadioButton("Use browser (or registered handler)");
        mail = new JRadioButton("Use mail client");
        custom = new JRadioButton("Custom command");
        subsLabel = new JLabel();
        exampleLabel = new JLabel();

        commandPath.setEnabled(false);
        showFileChooser.setEnabled(false);
        subsLabel.setEnabled(false);
        exampleLabel.setEnabled(false);

        optionType.add(dmdirc);
        optionType.add(browser);
        optionType.add(mail);
        optionType.add(custom);

        fileChooser.addChoosableFileFilter(new ExecutableFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        showFileChooser.setText("Browse");
        blurbLabel.setText("Use this dialog to add support for a " +
                "new protocol.");
        subsLabel.setText("$url, $fragment, $path, $port, $query, " +
                "$protocol, $username and $password all substituted.");
        updateExample();
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, hidemode 3"));

        add(blurbLabel, "grow");
        add(dmdirc, "growx");
        add(browser, "growx");
        add(mail, "growx");
        add(custom, "growx");
        add(commandPath, "split 2, growx, pushx");
        add(showFileChooser, "");
        add(subsLabel, "growx");
        add(exampleLabel, "growx");
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        showFileChooser.addActionListener(this);
        dmdirc.addActionListener(this);
        browser.addActionListener(this);
        mail.addActionListener(this);
        custom.addActionListener(this);
        commandPath.getDocument().addDocumentListener(this);
    }

    /** Saves the settings. */
    public void save() {
        final String value;
        if (optionType.getSelection() == dmdirc) {
            value = "DMDIRC";
        } else if (optionType.getSelection() == browser) {
            value = "BROWSER";
        } else if (optionType.getSelection() == mail) {
            value = "MAIL";
        } else if (optionType.getSelection() == custom) {
            value = commandPath.getText();
        } else {
            value = "";
        }

        IdentityManager.getConfigIdentity().setOption("protocol",
                url.getScheme(), value);
    }

    /**
     * Updates the example label.
     */
    private void updateExample() {
        exampleLabel.setText("Example: " + URLHandler.getURLHander().
                substituteParams(url, commandPath.getText()));
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == showFileChooser) {
            if (fileChooser.showDialog(this, "Select") ==
                    JFileChooser.APPROVE_OPTION) {
                commandPath.setText(fileChooser.getSelectedFile().toString());
            }
        } else {
            if (optionType.getSelection() == custom.getModel()) {
                commandPath.setEnabled(true);
                showFileChooser.setEnabled(true);
                subsLabel.setEnabled(true);
                exampleLabel.setEnabled(true);
            } else {
                commandPath.setEnabled(false);
                showFileChooser.setEnabled(false);
                subsLabel.setEnabled(false);
                exampleLabel.setEnabled(false);
            }
            validate();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(DocumentEvent e) {
        updateExample();
    }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(DocumentEvent e) {
        updateExample();
    }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(DocumentEvent e) {
    //Ignore
    }
}
