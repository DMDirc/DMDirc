/*
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

package com.dmdirc.ui.swing.components;

import java.awt.Component;

import java.util.Arrays;
import javax.swing.JSplitPane;

import javax.swing.plaf.basic.BasicSplitPaneUI;
import net.miginfocom.layout.PlatformDefaults;

/**
 * JSplit pane that snaps around its components preferred size.
 */
public class SnappingJSplitPane extends JSplitPane {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Snap distance. */
    private final int snapDistance;
    /** Component to snap to? */
    private boolean leftComponentSnap = true;

    /** Orientation type . */
    public enum Orientation {

        /** Horizontal orientation. */
        HORIZONTAL,
        /** Vertical orientation. */
        VERTICAL
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal 
     * split, two null components and snapping to the left component.
     */
    public SnappingJSplitPane() {
        this(Orientation.HORIZONTAL);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal 
     * split, two null components and snapping to the left component.
     * 
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final int snapDistance) {
        this(Orientation.HORIZONTAL, snapDistance);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal 
     * split and two null components.
     * 
     * @param leftComponentSnap Snap to the preferred size of the left component?
     */
    public SnappingJSplitPane(final boolean leftComponentSnap) {
        this(Orientation.HORIZONTAL, leftComponentSnap, 10);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using a horizontal 
     * split and two null components.
     * 
     * @param leftComponentSnap Snap to the preferred size of the left component?
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final boolean leftComponentSnap,
            final int snapDistance) {
        this(Orientation.HORIZONTAL, leftComponentSnap, snapDistance);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components and snapping to the left component.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     */
    public SnappingJSplitPane(final Orientation orientation) {
        this(orientation, null, null, 10);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components and snapping to the left component.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final Orientation orientation,
            final int snapDistance) {
        this(orientation, null, null, snapDistance);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponentSnap Snap to the preferred size of the left component?
     */
    public SnappingJSplitPane(final Orientation orientation,
            final boolean leftComponentSnap) {
        this(orientation, null, null, leftComponentSnap, 10);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponentSnap Snap to the preferred size of the left component?
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final Orientation orientation,
            final boolean leftComponentSnap, final int snapDistance) {
        this(orientation, null, null, leftComponentSnap, snapDistance);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components and snapping to the left component.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponent left component
     * @param rightComponent right component
     */
    public SnappingJSplitPane(final Orientation orientation,
            final Component leftComponent, final Component rightComponent) {
        this(orientation, leftComponent, rightComponent, true, 10);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components and snapping to the left component.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponent left component
     * @param rightComponent right component
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final Orientation orientation,
            final Component leftComponent, final Component rightComponent,
            final int snapDistance) {
        this(orientation, leftComponent, rightComponent, true, snapDistance);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponent left component
     * @param rightComponent right component
     * @param leftComponentSnap Snap to the preferred size of the left component?
     */
    public SnappingJSplitPane(final Orientation orientation,
            final Component leftComponent, final Component rightComponent,
            final boolean leftComponentSnap) {
        this(orientation, leftComponent, rightComponent, leftComponentSnap, 10);
    }

    /**
     * Instantiates a new snapping split pane. Defaults to using two null 
     * components.
     * 
     * @param orientation Split pane orientation 
     * <code>JSplitPane.HORIZONTAL_SPLIT</code> or 
     * <code>JSplitPane.VERTICAL_SPLIT</code>
     * @param leftComponent left component
     * @param rightComponent right component
     * @param leftComponentSnap Snap to the preferred size of the left component?
     * @param snapDistance Distance to around the preferred size
     */
    public SnappingJSplitPane(final Orientation orientation,
            final Component leftComponent, final Component rightComponent,
            final boolean leftComponentSnap, final int snapDistance) {
        super((orientation.equals(Orientation.HORIZONTAL)) ? HORIZONTAL_SPLIT : VERTICAL_SPLIT,
                true, leftComponent, rightComponent);

        this.leftComponentSnap = leftComponentSnap;
        this.snapDistance = snapDistance;

        setBorder(null);
        setDividerSize((int) PlatformDefaults.getPanelInsets(0).getValue());
        setOneTouchExpandable(true);
        setContinuousLayout(true);
        
        getActionMap().setParent(null);
        getActionMap().clear();
    }

    /** {@inheritDoc} */
    @Override
    public void setDividerLocation(final int location) {
        if (getOrientation() == HORIZONTAL_SPLIT) {
            setDividerLocationHorizontal(location);
        } else {
            setDividerLocationVertical(location);
        }
    }

    /**
     * Sets the divider location snapping to the preferred component size.
     * 
     * @param location new location
     */
    private void setDividerLocationHorizontal(final int location) {
        if (leftComponentSnap) {
            final int pref = getLeftComponent().getPreferredSize().width;
            super.setDividerLocation(Math.abs(pref - location) <= snapDistance ? pref
                    : location);
        } else {
            final int pref = getRightComponent().getPreferredSize().width;
            final int result = Math.abs(getWidth() - location - pref);
            if (result >= -snapDistance && result <= snapDistance) {
                super.setDividerLocation(Math.abs(getWidth() - pref));
            } else {
                super.setDividerLocation(location);
            }
        }
    }

    /**
     * Sets the divider location snapping to the preferred component size.
     * 
     * @param location new location
     */
    private void setDividerLocationVertical(final int location) {
        if (leftComponentSnap) {
            final int pref = getLeftComponent().getPreferredSize().height;
            super.setDividerLocation(Math.abs(pref - location) <= snapDistance ? pref
                    : location);
        } else {
            final int pref = getRightComponent().getPreferredSize().height;
            final int result = Math.abs(getWidth() - location - pref);
            if (result >= -snapDistance && result <= snapDistance) {
                super.setDividerLocation(Math.abs(getWidth() - pref));
            } else {
                super.setDividerLocation(location);
            }
        }
    }
}
    
