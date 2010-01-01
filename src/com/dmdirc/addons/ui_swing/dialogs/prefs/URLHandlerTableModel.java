/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.config.IdentityManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    /** Data list. */
    private List<URI> uris;
    /** Handlers list. */
    private List<String> handlers;

    /**
     * Instantiates a new table model.
     */
    public URLHandlerTableModel() {
        this(new ArrayList<URI>(), new ArrayList<String>());
    }

    /**
     * Instantiates a new table model.
     * 
     * @param uris URIs to show
     * @param handlers Handlers to show
     */
    public URLHandlerTableModel(final List<URI> uris,
            final List<String> handlers) {
        this.uris = uris;
        this.handlers = handlers;
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
                return handlers.get(rowIndex);
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (uris.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    uris.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                if (!(aValue instanceof URI)) {
                    throw new IllegalArgumentException("Value must be a URI");
                }
                uris.set(rowIndex, (URI) aValue);
                break;
            case 1:
                if (!(aValue instanceof String)) {
                    throw new IllegalArgumentException("Value must be a String");
                }
                handlers.set(rowIndex, (String) aValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Adds a URI to the model.
     * 
     * @param uri URI to add
     */
    public void addURI(final URI uri) {
        final String handler;
        if (IdentityManager.getGlobalConfig().hasOptionString("protocol", uri.getScheme())) {
            handler = IdentityManager.getGlobalConfig().getOption("protocol", uri.getScheme());
        } else {
            handler = "";
        }
        uris.add(uri);
        handlers.add(handler);
        fireTableRowsInserted(uris.size() - 1, uris.size() - 1);
    }

    /**
     * Removes a URI to the model.
     * 
     * @param uri URI to remove
     */
    public void removeURI(final URI uri) {
        removeURI(uris.indexOf(uri));
    }

    /**
     * Removes a URI to the model.
     * 
     * @param index Index of the URI to remove
     */
    public void removeURI(final int index) {
        if (index != -1) {
            uris.remove(index);
            handlers.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    /**
     * Returns a map of the URL handlers in this model.
     * 
     * @return URL Handler map
     */
    public Map<URI, String> getURLHandlers() {
        final Map<URI, String> urlHandlers = new HashMap<URI, String>();
        
        for (int i = 0; i < uris.size(); i++) {
            urlHandlers.put(uris.get(i), handlers.get(i));
        }
        
        return urlHandlers;
    }
}
