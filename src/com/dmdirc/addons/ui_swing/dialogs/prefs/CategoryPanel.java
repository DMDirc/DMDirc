/*
 * 
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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.TitlePanel;
import com.dmdirc.addons.ui_swing.components.ToolTipPanel;
import com.dmdirc.config.prefs.PreferencesCategory;

import java.awt.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Panel representing a preferences category.
 */
public class CategoryPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -3268284364607758509L;
    /** Panel gap. */
    private final int padding = (int) PlatformDefaults.getUnitValueX("related").
            getValue();
    /** Panel left padding. */
    private final int leftPadding = (int) PlatformDefaults.getPanelInsets(1).
            getValue();
    /** Panel right padding. */
    private final int rightPadding = (int) PlatformDefaults.getPanelInsets(3).
            getValue();
    /** Active preferences category. */
    private PreferencesCategory category;
    /** Parent window. */
    private Window parent;
    /** Title label. */
    private TitlePanel title;
    /** Tooltip display area. */
    private ToolTipPanel tooltip;
    /** Contents Panel. */
    private JScrollPane scrollPane;
    /** Loading panel. */
    private JPanel loading;
    /** Loading panel. */
    private JPanel nullCategory;
    /** Loading panel. */
    private JPanel waitingCategory;
    /** Category panel map. */
    private Map<PreferencesCategory, JPanel> panels;
    /** Category loading swing worker. */
    private SwingWorker worker;
    /** Waiting. */
    private boolean waiting;

    /**
     * Instantiates a new category panel.
     *
     * @param parent Parent window
     */
    public CategoryPanel(final Window parent) {
        this(parent, null);
    }

    /**
     * Instantiates a new category panel.
     *
     * @param parent Parent window
     * @param category Initial category
     */
    public CategoryPanel(final Window parent,
            final PreferencesCategory category) {
        super(new MigLayout("fillx, wrap, ins 0"));
        this.parent = parent;

        panels = Collections.synchronizedMap(
                new HashMap<PreferencesCategory, JPanel>());

        loading = new JPanel(new MigLayout("fillx"));
        loading.add(new TextLabel("Loading..."));

        nullCategory = new JPanel(new MigLayout("fillx"));
        nullCategory.add(new TextLabel("Please select a category."));

        waitingCategory = new JPanel(new MigLayout("fillx"));
        waitingCategory.add(new TextLabel("Please wait, loading..."));

        scrollPane = new JScrollPane(loading);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        title = new TitlePanel(BorderFactory.createEtchedBorder(),
                "Preferences");
        tooltip = new ToolTipPanel("Hover over a setting to see a " +
                "description, if available.");

        add(title, "pushx, growx, h 45!");
        add(scrollPane, "grow, push, h 425!");
        add(tooltip, "pushx, growx, h 65!");

        panels.put(null, loading);
        setCategory(category);
    }

    /**
     * Returns this categrory panel's parent window.
     *
     * @return Parent window
     */
    protected Window getParentWindow() {
        return parent;
    }

    /**
     * Returns the tooltip panel for this category panel.
     *
     * @return Tooltip panel
     */
    protected ToolTipPanel getToolTipPanel() {
        return tooltip;
    }

    /**
     * Informs the category panel a category has been loaded.
     *
     * @param loader Category loader
     * @param category Loaded category
     */
    protected void categoryLoaded(final PrefsCategoryLoader loader,
            final PreferencesCategory category) {
        panels.put(category, loader.getPanel());
        categoryLoaded(category);
    }

    private void categoryLoaded(final PreferencesCategory category) {
        if (this.category == category) {
            UIUtilities.invokeAndWait(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    scrollPane.setViewportView(panels.get(category));
                    if (category == null) {
                        title.setText("Preferences");
                    } else {
                        title.setText(category.getPath());
                    }
                }
            });
        }        
    }

    /**
     * Sets the new active category for this panel and relays out.
     *
     * @param category New Category
     */
    public void setCategory(final PreferencesCategory category) {
        this.category = category;

        if (!panels.containsKey(category)) {
            UIUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    scrollPane.setViewportView(loading);
                }
            });

            worker = new PrefsCategoryLoader(this, category);
            worker.execute();
        } else {
            categoryLoaded(category);
        }
    }

    /** 
     * Sets this panel to a waiting to load state.
     * 
     * @param b
     */
    public void setWaiting(boolean b) {
        waiting = b;
        scrollPane.setViewportView(waitingCategory);
    }
}
