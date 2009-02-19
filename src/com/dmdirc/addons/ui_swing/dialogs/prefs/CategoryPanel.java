/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.components.ColourChooser;
import com.dmdirc.addons.ui_swing.components.OptionalColourChooser;
import com.dmdirc.addons.ui_swing.components.TextLabel;
import com.dmdirc.addons.ui_swing.components.TitlePanel;
import com.dmdirc.addons.ui_swing.components.ToolTipPanel;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationDisplay;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
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
    /** Category panel map. */
    private Map<PreferencesCategory, JPanel> panels;

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

        panels = new HashMap<PreferencesCategory, JPanel>();

        loading = new JPanel(new MigLayout("fillx"));
        loading.add(new TextLabel("Loading..."));

        nullCategory = new JPanel(new MigLayout("fillx"));
        nullCategory.add(new TextLabel("Please select a category."));

        scrollPane = new JScrollPane(loading);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        title = new TitlePanel(BorderFactory.createEtchedBorder(),
                "Preferences");
        tooltip = new ToolTipPanel("Hover over a setting to see a " +
                "description, if available.");

        add(title, "pushx, growx, h 45!");
        add(scrollPane, "grow, push, h 425!");
        add(tooltip, "pushx, growx, h 65!");

        setCategory(category);
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

            new SwingWorker<JPanel, Object>() {

                /** {@inheritDoc} */
                @Override
                protected JPanel doInBackground() throws Exception {
                    return initCategory();
                }

                /** {@inheritDoc} */
                @Override
                protected void done() {
                    try {
                        panels.put(category, get());
                        scrollPane.setViewportView(panels.get(category));
                        if (category == null) {
                            title.setText("Preferences");
                        } else {
                            title.setText(category.getPath());
                        }
                    } catch (InterruptedException ex) {
                        //Ignore
                    } catch (ExecutionException ex) {
                        Logger.appError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
                    }
                }
            }.execute();
        } else {
            scrollPane.setViewportView(panels.get(category));
            if (category == null) {
                title.setText("Preferences");
            } else {
                title.setText(category.getPath());
            }
        }
    }

    private JPanel initCategory() {
        final JPanel panel;
        if (category == null) {
            panel = nullCategory;
        } else {
            panel = addCategory(category);
        }
        return panel;
    }

    /**
     * Initialises the specified category.
     *
     * @since 0.6.3
     * @param category The category that is being initialised
     * @param panel The panel to which we're adding its contents
     * @param path The textual path of this category
     */
    private void initCategory(final PreferencesCategory category,
            final JPanel panel, final String path) {

        if (!category.getDescription().isEmpty()) {
            panel.add(new TextLabel(category.getDescription()), "span, " +
                    "growx, wrap 2*unrel");
        }

        for (PreferencesCategory child : category.getSubcats()) {
            if (child.isInline() && category.isInlineBefore()) {
                addInlineCategory(child, panel);
            } else if (!child.isInline()) {
                addCategory(child);
            }
        }

        if (category.hasObject()) {
            if (!(category.getObject() instanceof JPanel)) {
                throw new IllegalArgumentException(
                        "Custom preferences objects" +
                        " for this UI must extend JPanel.");
            }

            panel.add((JPanel) category.getObject(), "growx");

            return;
        }

        for (PreferencesSetting setting : category.getSettings()) {
            addComponent(category, setting, panel);
        }

        if (!category.isInlineBefore()) {
            for (PreferencesCategory child : category.getSubcats()) {
                if (child.isInline()) {
                    addInlineCategory(child, panel);
                }
            }
        }
    }

    /**panel.
     * Initialises and adds a component to a panel.
     *
     * @param category The category the setting is being added to
     * @param setting The setting to be used
     * @param panel The panel to add the component to
     */
    private void addComponent(final PreferencesCategory category,
            final PreferencesSetting setting,
            final JPanel panel) {

        final TextLabel label = getLabel(setting);

        JComponent option = PrefsComponentFactory.getComponent(setting);
        option.setToolTipText(setting.getHelptext());

        if (option instanceof DurationDisplay) {
            ((DurationDisplay) option).setWindow(parent);
        } else if (option instanceof ColourChooser) {
            ((ColourChooser) option).setWindow(parent);
        } else if (option instanceof OptionalColourChooser) {
            ((OptionalColourChooser) option).setWindow(parent);
        }

        panel.add(label, "align label, wmax 40%");
        panel.add(option, "growx, w 60%");

        tooltip.registerTooltipHandler(label);
        tooltip.registerTooltipHandler(option);
    }

    /**
     * Retrieves the title label for the specified setting.
     *
     * @param setting The setting whose label is being requested
     * @return A JLabel with the appropriate text and tooltip
     */
    private TextLabel getLabel(final PreferencesSetting setting) {
        final TextLabel label = new TextLabel(setting.getTitle() + ": ", false);

        if (setting.getHelptext().isEmpty()) {
            label.setToolTipText("No help available.");
        } else {
            label.setToolTipText(setting.getHelptext());
        }

        return label;
    }

    /**
     * Adds a new inline category.
     *
     * @param category The category to be added
     * @param parent The panel to add the category to
     */
    private void addInlineCategory(final PreferencesCategory category,
            final JPanel parent) {
        final JPanel panel =
                new JPanel(new MigLayout("fillx, gap unrel, wrap 2, hidemode 3, pack, " +
                "wmax 470-" + leftPadding + "-" +
                rightPadding + "-2*" + padding));
        panel.setBorder(BorderFactory.createTitledBorder(category.getTitle()));

        parent.add(panel, "span, growx, wrap");

        initCategory(category, panel, "");
    }

    /**
     * Adds the specified category to the preferences dialog.
     *
     * @since 0.6.3
     * @param category The category to be added
     * @param namePrefix Category name prefix
     */
    private JPanel addCategory(final PreferencesCategory category) {
        final JPanel panel =
                new JPanel(new MigLayout("fillx, gap unrel, " +
                "wrap 2, hidemode 3, wmax 470-" + leftPadding + "-" +
                rightPadding + "-2*" + padding));
        final String path = category.getPath();

        initCategory(category, panel, path);

        return panel;
    }
}
