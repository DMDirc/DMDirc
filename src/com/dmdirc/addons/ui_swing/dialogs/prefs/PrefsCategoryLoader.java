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

import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ColourChooser;
import com.dmdirc.addons.ui_swing.components.OptionalColourChooser;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.durationeditor.DurationDisplay;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.ReturnableThread;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import javax.swing.UIManager;
import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Loads a preferences panel for a specified preferences category in the
 * background.
 */
public class PrefsCategoryLoader extends SwingWorker<JPanel, Object> {

    /** Panel gap. */
    private final int padding = (int) PlatformDefaults.getUnitValueX("related").
            getValue();
    /** Panel left padding. */
    private final int leftPadding = (int) PlatformDefaults.getPanelInsets(1).
            getValue();
    /** Panel right padding. */
    private final int rightPadding = (int) PlatformDefaults.getPanelInsets(3).
            getValue();
    /** Error panel. */
    private JPanel errorCategory;
    private CategoryPanel categoryPanel;
    private PreferencesCategory category;

    /**
     * Instantiates a new preferences category loader.
     *
     * @param categoryPanel Parent Category panel
     * @param category Preferences Category to load
     */
    public PrefsCategoryLoader(final CategoryPanel categoryPanel,
            final PreferencesCategory category) {
        this.categoryPanel = categoryPanel;
        this.category = category;

        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                errorCategory = new JPanel(new MigLayout("fillx"));
                errorCategory.add(new TextLabel(
                        "There was an error loading this category."));
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception if unable to compute a result
     */
    @Override
    protected JPanel doInBackground() throws Exception {
        return addCategory(category);
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        if (isCancelled()) {
            return;
        }
        categoryPanel.categoryLoaded(this, category);
    }

    /**
     * Returns the panel for this loader.
     *
     * @return Loaded panel
     */
    public JPanel getPanel() {
        JPanel panel;
        try {
            panel = super.get();
        } catch (InterruptedException ex) {
            panel = errorCategory;
        } catch (ExecutionException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Error loading prefs panel", ex);
            panel = errorCategory;
        }
        return panel;
    }

    /**
     * Initialises the specified category.
     *
     * @since 0.6.3m1
     * @param category The category that is being initialised
     * @param panel The panel to which we're adding its contents
     * @param path The textual path of this category
     */
    private void initCategory(final PreferencesCategory category,
            final JPanel panel, final String path) {

        if (!category.getDescription().isEmpty()) {
            panel.add(new TextLabel(category.getDescription()), "span, " +
                    "growx, pushx, wrap 2*unrel");
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

            panel.add((JPanel) category.getObject(), "growx, pushx");

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

    /**
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


        JComponent option = UIUtilities.invokeAndWait(
                new ReturnableThread<JComponent>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(PrefsComponentFactory.getComponent(setting));
            }
        });
        
        option.setToolTipText(null);
        categoryPanel.getToolTipPanel().registerTooltipHandler(label);
        categoryPanel.getToolTipPanel().registerTooltipHandler(option,
                setting.getHelptext());

        if (option instanceof DurationDisplay) {
            ((DurationDisplay) option).setWindow(categoryPanel.getParentWindow());
        } else if (option instanceof ColourChooser) {
            ((ColourChooser) option).setWindow(categoryPanel.getParentWindow());
        } else if (option instanceof OptionalColourChooser) {
            ((OptionalColourChooser) option).setWindow(categoryPanel.
                    getParentWindow());
        }

        if (Apple.isAppleUI()) {
            panel.add(label, "align right, wmax 40%");
        } else {
            panel.add(label, "align left, wmax 40%");
        }
        panel.add(option, "growx, pushx, w 60%");
    }

    /**
     * Retrieves the title label for the specified setting.
     *
     * @param setting The setting whose label is being requested
     * @return A TextLabel with the appropriate text and tooltip
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
        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), category.getTitle()));

        parent.add(panel, "span, growx, pushx, wrap");

        initCategory(category, panel, "");
    }

    /**
     * Adds the specified category to the preferences dialog.
     *
     * @since 0.6.3m1
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
