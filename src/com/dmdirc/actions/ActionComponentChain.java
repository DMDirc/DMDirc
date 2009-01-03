/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.actions;

import com.dmdirc.actions.interfaces.ActionComponent;
import com.dmdirc.Precondition;
import com.dmdirc.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * An action component chain supports chaining of multiple action components
 * together.
 *
 * @author chris
 */
public class ActionComponentChain implements ActionComponent {
    
    /**
     * A list of components in this chain.
     */
    private final List<ActionComponent> components = new ArrayList<ActionComponent>();
    
    /**
     * Creates a new component chain from the specified text representation.
     * Chains are separated with full stops (.).
     *
     * @param source The class that this chain needs to start with
     * @param chain The textual representation of the chain
     */
    public ActionComponentChain(final Class source, final String chain) {
        Class current = source;
        
        for (String componentName : chain.split("\\.")) {
            final ActionComponent component = ActionManager.getActionComponent(componentName);
            
            if (component == null) {
                throw new IllegalArgumentException("Component " + componentName
                        + " not found");
            } else if (component.appliesTo() == current) {
                components.add(component);
                current = component.getType();
            } else {
                throw new IllegalArgumentException("Component " + componentName
                        + " cannot be applied to " + current.getName());
            }
        }
        
    }
    
    /** {@inheritDoc} */
    @Override
    public Object get(final Object argument) {
        Object res = argument;
        
        for (ActionComponent component : components) {
            res = component.get(res);
        }
        
        return res;
    }
    
    /** {@inheritDoc} */
    @Precondition("This component chain has one or more components")
    @Override
    public Class appliesTo() {
        Logger.assertTrue(!components.isEmpty());
        
        return components.get(0).appliesTo();
    }
    
    /** {@inheritDoc} */
    @Precondition("This component chain has one or more components")
    @Override
    public Class getType() {
        Logger.assertTrue(!components.isEmpty());
        
        return components.get(components.size() - 1).getType();
    }
    
    /** {@inheritDoc} */
    @Precondition("This component chain has one or more components")
    @Override
    public String getName() {
        Logger.assertTrue(!components.isEmpty());
        
        final StringBuilder name = new StringBuilder();
        
        for (ActionComponent component : components) {
            name.append("'s ");
            name.append(component.getName());
        }
        
        return name.substring(3);
    }
    
    /** {@inheritDoc} */
    @Override
    @Precondition("This component chain has one or more components")
    public String toString() {
        Logger.assertTrue(!components.isEmpty());
        
        final StringBuilder name = new StringBuilder();
        
        for (ActionComponent component : components) {
            name.append('.');
            name.append(component.toString());
        }
        
        return name.substring(1);
    }
    
}