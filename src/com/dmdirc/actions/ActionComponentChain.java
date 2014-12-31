/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.Precondition;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.actions.ActionComponent;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An action component chain supports chaining of multiple action components together.
 */
public class ActionComponentChain implements ActionComponent {

    /**
     * A list of components in this chain.
     */
    private final List<ActionComponent> components = new ArrayList<>();

    /**
     * Creates a new component chain from the specified text representation. Chains are separated
     * with full stops (.).
     *
     * @param source  The class that this chain needs to start with
     * @param chain   The textual representation of the chain
     * @param manager The action manager to use to look up components
     */
    public ActionComponentChain(final Class<?> source, final String chain,
            final ActionController manager) {
        Class<?> current = source;

        for (String componentName : chain.split("\\.")) {
            final ActionComponent component = manager.getComponent(componentName);

            if (component == null) {
                throw new IllegalArgumentException("Component " + componentName
                        + " not found");
            } else if (component.appliesTo().isAssignableFrom(current)) {
                components.add(component);
                current = component.getType();
            } else {
                throw new IllegalArgumentException("Component " + componentName
                        + " cannot be applied to " + current.getName());
            }
        }
    }

    @Override
    public Object get(final Object arg) {
        Object res = arg;

        for (ActionComponent component : components) {
            if (res == null) {
                return null;
            }

            res = component.get(res);
        }

        return res;
    }

    @Precondition("This component chain has one or more components")
    @Override
    public Class<?> appliesTo() {
        checkArgument(!components.isEmpty());

        return components.get(0).appliesTo();
    }

    @Precondition("This component chain has one or more components")
    @Override
    public Class<?> getType() {
        checkArgument(!components.isEmpty());

        return components.get(components.size() - 1).getType();
    }

    @Precondition("This component chain has one or more components")
    @Override
    public String getName() {
        checkArgument(!components.isEmpty());

        final StringBuilder name = new StringBuilder();

        for (ActionComponent component : components) {
            name.append("'s ");
            name.append(component.getName());
        }

        return name.substring(3);
    }

    @Override
    @Precondition("This component chain has one or more components")
    public String toString() {
        checkArgument(!components.isEmpty());

        final StringBuilder name = new StringBuilder();

        for (ActionComponent component : components) {
            name.append('.');
            name.append(component.toString());
        }

        return name.substring(1);
    }

    @Override
    public String name() {
        return toString();
    }

    /**
     * Determines if any components in this chain require a server to have an established connection
     * in order to function.
     *
     * @since 0.6.4
     * @return True iff at least one component requires a connection
     */
    public boolean requiresConnection() {
        boolean res = false;

        for (ActionComponent component : components) {
            try {
                final ComponentOptions options = component.getClass()
                        .getMethod("get", Object.class).getAnnotation(ComponentOptions.class);
                if (options != null) {
                    res |= options.requireConnected();
                }
            } catch (NoSuchMethodException ex) {
                // Do nothing
            }
        }

        return res;
    }

}
