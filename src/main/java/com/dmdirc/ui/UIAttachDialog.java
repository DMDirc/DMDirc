/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.ui;

import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Simple dialog to pick and load a UI in an existing client.
 */
public class UIAttachDialog extends JDialog implements ActionListener, ListSelectionListener {

    /** Java Serialisation version ID. */
    private static final long serialVersionUID = 1;
    /** Sensible component gap. */
    private static final int GAP = 5;
    /** Services list. */
    private final JList<Service> list;
    /** Service manager to use to find UI services. */
    private final ServiceManager serviceManager;

    /**
     * Creates a new dialog allowing the user to select and load a UI.
     *
     * @param serviceManager Service manager to use to find UI services.
     */
    public UIAttachDialog(final ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        list = initList();
        setLayout(new BorderLayout());
        add(list, BorderLayout.CENTER);
        add(initButtonPanel(), BorderLayout.PAGE_END);
    }

    /** Initialises the model with a list of UIs. */
    private JList<Service> initList() {
        final DefaultListModel<Service> model = new DefaultListModel<>();
        final JList<Service> newList = new JList<>(model);
        newList.setCellRenderer(new ServiceRenderer());
        newList.addListSelectionListener(this);
        final List<Service> services = serviceManager.getServicesByType("ui");
        services.forEach(model::addElement);
        newList.setSelectedIndex(0);
        return newList;
    }

    /** Initialises the OK and and cancel button panel. */
    private JPanel initButtonPanel() {
        final JPanel panel = new JPanel();
        final BoxLayout box = new BoxLayout(panel, BoxLayout.LINE_AXIS);
        final JButton ok = new JButton("OK");
        final JButton cancel = new JButton("Cancel");

        ok.setActionCommand("OK");
        ok.addActionListener(this);
        cancel.setActionCommand("Cancel");
        cancel.addActionListener(this);

        panel.setLayout(box);
        panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        panel.add(Box.createHorizontalGlue());
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            panel.add(ok);
            panel.add(Box.createHorizontalStrut(GAP));
            panel.add(cancel);
        } else {
            panel.add(cancel);
            panel.add(Box.createHorizontalStrut(GAP));
            panel.add(ok);
        }

        return panel;
    }

    /** Displays this dialog. */
    public void display() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("OK".equals(e.getActionCommand())) {
            if (list.getSelectedValue() == null) {
                return;
            }
            list.getSelectedValue().activate();
        }
        dispose();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            list.setSelectedIndex(e.getLastIndex());
        }
    }

    /**
     * Renders plugin services properly.
     */
    private static class ServiceRenderer extends DefaultListCellRenderer {

        /** Java Serialisation version ID. */
        private static final long serialVersionUID = 1;

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                final Object value, final int index, final boolean isSelected,
                final boolean cellHasFocus) {
            final Component label = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (value instanceof Service) {
                setText(((Service) value).getName());
            }

            return label;
        }

    }

}
