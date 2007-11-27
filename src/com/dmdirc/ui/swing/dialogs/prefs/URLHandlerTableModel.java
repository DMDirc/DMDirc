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

package com.dmdirc.ui.swing.dialogs.prefs;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * URL Handler table model.
 */
public class URLHandlerTableModel extends AbstractTableModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Config. */
    private static final ConfigManager config =
            IdentityManager.getGlobalConfig();
    /** Data list. */
    private List<URI> uris;

    /**
     * Instantiates a new table model.
     */
    public URLHandlerTableModel() {
        this(new ArrayList<URI>());
    }

    /**
     * Instantiates a new table model.
     * 
     * @param uris URIs to show
     */
    public URLHandlerTableModel(final List<URI> uris) {
        this.uris = uris;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return uris.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Protocol";
            case 1:
                return "Handler";
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return URI.class;
            case 1:
                return String.class;
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (uris.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    uris.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                return uris.get(rowIndex);
            case 1:
                String handler;
                if (config.hasOption("protocol", uris.get(rowIndex).getScheme())) {
                    handler = config.getOption("protocol", uris.get(rowIndex).
                            getScheme());
                    if ("DMDIRC".equals(handler)) {
                        handler = "Handle internally (irc links only).";
                    } else if ("BROWSER".equals(handler)) {
                        handler = "Use browser (or system registered handler).";
                    } else if ("MAIL".equals(handler)) {
                        handler = "Use mail client.";
                    } else {
                        handler = "Custom command: " + handler;
                    }
                } else {
                    handler = "No handler.";
                }
                return handler;
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /**
     * Adds a URI to the model.
     * 
     * @param uri URI to add
     */
    public void addURI(final URI uri) {
        uris.add(uri);
        fireTableRowsInserted(uris.size() - 1, uris.size() - 1);
    }

    /**
     * Removes a URI to the model.
     * 
     * @param uri URI to remove
     */
    public void removeURI(final URI uri) {
        uris.remove(uri);
        fireTableRowsDeleted(uris.size(), uris.size());
    }
}
