
package com.dmdirc.addons.ui_swing.components;

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

import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;

/**
 * Panel to display toolstips of a component.
 */
public class ToolTipPanel extends JPanel implements MouseListener {

    /**
     * A version number for this class. It should be changed whenever the
     * class structure is changed (or anything else that would prevent
     * serialized objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -8929794537312606692L;
    /** Default tooltip. */
    private final String defaultHelp;
    /** Tooltip display. */
    private TextLabel tooltip;
    /** Map of registered components to their tooltips. */
    private final Map<JComponent, String> tooltips;

    /**
     * Instantiates a new tooltip panel.
     *
     * @param defaultHelp Default help message when idle
     */
    public ToolTipPanel(final String defaultHelp) {
        super(new MigLayout());

        this.defaultHelp = defaultHelp;
        this.tooltips = new HashMap<JComponent, String>();

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder());

        tooltip = new TextLabel();
        reset();

        add(tooltip, "grow, push");
    }

    /**
     * Resets the content of the tooltip.
     */
    protected void reset() {
        tooltip.setText(defaultHelp);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, true);
        tooltip.getDocument().setParagraphAttributes(0, defaultHelp.length(),
                                                     sas, true);
    }

    /**
     * Sets the content of the tooltip area to the specified text.
     *
     * @param text The text to be displayed
     */
    protected void setText(final String text) {
        if (tooltip == null) {
            return;
        }
        tooltip.setText(text);
        if (tooltip.getDocument() == null || text == null) {
            return;
        }
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setItalic(sas, false);
        tooltip.getDocument().setParagraphAttributes(0, text.length(), sas, true);
    }

    /**
     * Registers a component with this tooltip handler.
     *
     * @param component Component to register
     */
    public void registerTooltipHandler(final JComponent component) {
        registerTooltipHandler(component, component.getToolTipText());
        component.setToolTipText(null);
    }

    /**
     * Registers a component with this tooltip handler.
     *
     * @param component Component to register
     * @param tooltipText Tooltip text for the component
     */
    public void registerTooltipHandler(final JComponent component,
            final String tooltipText) {
        if (component instanceof JXLayer) {
            tooltips.put(((JXLayer) component).getGlassPane(), tooltipText);
            ((JXLayer) component).getGlassPane().addMouseListener(this);
        } else {
            tooltips.put(component, tooltipText);
            component.addMouseListener(this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Not used
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        if (e.getSource() instanceof JComponent) {
            setText(tooltips.get(e.getSource()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        reset();
    }
}
