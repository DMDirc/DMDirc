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

package com.dmdirc.addons.ui_swing.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Adjusted Card layout.
 */
public class StepLayout implements LayoutManager2, Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Parent container. */
    private Container parent;
    /** Cards vector. */
    private final List<Step> steps;
    /** Current step. */
    private int currentStep;
    /** Vertical gap. */
    private int vGap;
    /** Horiontal gap. */
    private int hGap;

    /**
     * Instantiates a new step layout.
     */
    public StepLayout() {
        this(0, 0);
    }
    
    /**
     * Instantiates a new step layout.
     * 
     * @param parent Parent component
     */
    public StepLayout(final Container parent) {
        this(0, 0, parent);
    }
    
    /**
     * Instantiates a new step layout with the specified gaps.
     * 
     * @param hGap Horizontal gap
     * @param vGap Vertical gap
     */
    public StepLayout(final int hGap, final int vGap) {
        steps = new ArrayList<Step>();
        currentStep = -1;
        this.hGap = hGap;
        this.vGap = vGap;
    }

    /**
     * Instantiates a new step layout with the specified gaps.
     * 
     * @param hGap Horizontal gap
     * @param vGap Vertical gap
     * @param parent Parent component
     */
    public StepLayout(final int hGap, final int vGap, final Container parent) {
        steps = new ArrayList<Step>();
        currentStep = -1;
        this.hGap = hGap;
        this.vGap = vGap;
        this.parent = parent;
    }

    /**
     * Returns the number of steps in the layout.
     * 
     * @return number of steps >= 0
     */
    public int size() {
        return steps.size();
    }

    /**
     * Checks if the layout is empty
     * 
     * @return true iif the layout has no steps
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * Returns the specified step from the layout.
     * 
     * @param index Step to retrieve
     * 
     * @return Step
     */
    public Step getStep(final int index) {
        return steps.get(index);
    }

    /**
     * Returns the step list.
     * 
     * @return List of steps
     */
    public List getSteps() {
        return steps;
    }

    /**
     * Show the first step.
     * 
     * @param parent Parent container
     */
    public void first(final Container parent) {
        show(0, parent);
    }

    /**
     * Show the last step.
     * 
     * @param parent Parent container
     */
    public void last(final Container parent) {
        show(parent.getComponentCount() - 1, parent);
    }

    /**
     * Show the next step.
     * 
     * @param parent Parent container
     */
    public void next(final Container parent) {
        show(currentStep + 1, parent);
    }

    /** 
     * Show the previous step.
     * 
     * @param parent Parent container
     */
    public void previous(final Container parent) {
        show(currentStep - 1, parent);
    }

    /**
     * Show the specified step.
     * 
     * @param step Step to show
     * @param parent Parent container
     */
    public void show(final Step step, final Container parent) {
        show(steps.indexOf(step), parent);
    }

    /**
     * Show the step at the specified index.
     * 
     * @param step Step to show
     * @param parent Parent container
     */
    public void show(final int step, final Container parent) {
        int stepNumber = step;
        if (stepNumber == -1) {
            if (stepNumber >= steps.size()) {
                stepNumber = steps.size() - 1;
            } else {
                stepNumber = 0;
            }
        }
        synchronized (parent.getTreeLock()) {
            int componentCount = parent.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setVisible(false);
                    break;
                }
            }
            if (componentCount > 0) {
                currentStep = stepNumber;
                parent.getComponent(currentStep).setVisible(true);
                parent.validate();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addLayoutComponent(final Component comp,
            final Object constraints) {
        if (!(comp instanceof Step)) {
            throw new IllegalArgumentException("Component must be an instance of Step");
        }
        addLayoutComponent((Step) comp);
    }

    /** 
     * {@inheritDoc}
     * 
     * @deprecated Use addLayoutComponent(Component, Object) or 
     * addLayoutComponent(Component)
     * 
     * @see addLayoutComponent(Component)
     * @see addLayoutComponent(Component, Object)
     */
    @Override
    @Deprecated
    public void addLayoutComponent(final String name, final Component comp) {
        if (!(comp instanceof Step)) {
            throw new IllegalArgumentException("Component must be an instance of Step");
        }
        addLayoutComponent((Step) comp);
    }

    /**
     * Adds a component to the layout.
     * 
     * @param step Component to add
     */
    public void addLayoutComponent(final Step step) {
        synchronized (step.getTreeLock()) {
            if (!steps.isEmpty()) {
                step.setVisible(false);
            }
            steps.add(step);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeLayoutComponent(final Component comp) {
        synchronized (comp.getTreeLock()) {
            if (comp.isVisible()) {
                comp.setVisible(false);
            }
            next(comp.getParent());
            steps.remove(comp);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @return Returns the preferred size of the container
     */
    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();
            int width = 0;
            int height = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                Dimension preferredDimension = comp.getPreferredSize();
                if (preferredDimension.width > width) {
                    width = preferredDimension.width;
                }
                if (preferredDimension.height > height) {
                    height = preferredDimension.height;
                }
            }
            return new Dimension(insets.left + insets.right + width + hGap * 2,
                    insets.top + insets.bottom + height + vGap * 2);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @return Returns the minimum size of the container
     */
    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();
            int width = 0;
            int height = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                Dimension minimumDimension = comp.getMinimumSize();
                if (minimumDimension.width > width) {
                    width = minimumDimension.width;
                }
                if (minimumDimension.height > height) {
                    height = minimumDimension.height;
                }
            }
            return new Dimension(insets.left + insets.right + width + hGap * 2,
                    insets.top + insets.bottom + height + vGap * 2);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param parent Container to get the size for
     * 
     * @return Returns the maximum size of the container
     */
    @Override
    public Dimension maximumLayoutSize(final Container parent) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param target Container to get the alignment from
     * 
     * @return Alignment
     */
    @Override
    public float getLayoutAlignmentX(final Container target) {
        return 0.5f;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param target Container to get the alignment from
     * 
     * @return Alignment
     */
    @Override
    public float getLayoutAlignmentY(final Container target) {
        return 0.5f;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param target  Container to invalidate
     */
    @Override
    public void invalidateLayout(final Container target) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void layoutContainer(final Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();
            Component comp = null;
            boolean currentFound = false;

            for (int i = 0; i < componentCount; i++) {
                comp = parent.getComponent(i);
                comp.setBounds(hGap + insets.left, vGap + insets.top,
                        parent.getWidth() - (hGap * 2 + insets.left +
                        insets.right), parent.getHeight() - (vGap * 2 +
                        insets.top + insets.bottom));
                if (comp.isVisible()) {
                    currentFound = true;
                }
            }

            if (!currentFound && componentCount > 0) {
                parent.getComponent(0).setVisible(true);
            }
        }
    }
}
