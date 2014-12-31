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

import com.dmdirc.interfaces.actions.ActionComparison;
import com.dmdirc.interfaces.actions.ActionComponent;

/**
 * An action condition represents one condition within an action.
 */
public class ActionCondition {

    /** The argument number that this action condition applies to. */
    private int arg;
    /** The component that this action condition applies to. */
    private ActionComponent component;
    /** The comparison that should be used for this condition. */
    private ActionComparison comparison;
    /** The target of the comparison for this condition. */
    private String target = "";
    /** The source target for this comparison. */
    private String starget = "";

    /**
     * Creates a new instance of ActionCondition that compares the output of a component to a
     * string.
     *
     * @param arg        The argument number to be tested
     * @param component  The component to be tested
     * @param comparison The comparison to be used
     * @param target     The target of the comparison
     */
    public ActionCondition(final int arg, final ActionComponent component,
            final ActionComparison comparison, final String target) {

        this.arg = arg;
        this.component = component;
        this.comparison = comparison;
        this.target = target;
    }

    /**
     * Creates a new instance of ActionCondition that compares two strings.
     *
     * @param starget    The first target for comparison.
     * @param comparison The comparison to be used
     * @param target     The second target for the comparison
     */
    public ActionCondition(final String starget, final ActionComparison comparison,
            final String target) {

        this.arg = -1;
        this.starget = starget;
        this.comparison = comparison;
        this.target = target;
    }

    /**
     * Tests to see if this condition holds.
     *
     * @param sub  The substitutor to use for this
     * @param args The event arguments to be tested
     *
     * @return True if the condition holds, false otherwise
     */
    public boolean test(final ActionSubstitutor sub, final Object... args) {
        final String thisTarget = sub.doSubstitution(getTarget(), args);

        if (arg == -1) {
            final String thisStarget = sub.doSubstitution(starget, args);
            return getComparison().test(thisStarget, thisTarget);
        } else {
            return getComparison().test(getComponent().get(args[getArg()]), thisTarget);
        }
    }

    /**
     * Returns the argument number this condition applies to.
     *
     * @return Argument number
     */
    public int getArg() {
        return arg;
    }

    /**
     * Returns the component this condition applies to.
     *
     * @return Component to apply condition to
     */
    public ActionComponent getComponent() {
        return component;
    }

    /**
     * Returns the comparison this condition applies to.
     *
     * @return Comparison to be used
     */
    public ActionComparison getComparison() {
        return comparison;
    }

    /**
     * Returns the target of the comparison for this condition.
     *
     * @return Target for comparison
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the argument number this condition applies to.
     *
     * @param arg Argument number
     */
    public void setArg(final int arg) {
        this.arg = arg;
    }

    /**
     * Sets the component this condition applies to.
     *
     * @param component Component to apply condition to
     */
    public void setComponent(final ActionComponent component) {
        this.component = component;
    }

    /**
     * Sets the comparison this condition applies to.
     *
     * @param comparison Comparison to be used
     */
    public void setComparison(final ActionComparison comparison) {
        this.comparison = comparison;
    }

    /**
     * Sets the target of the comparison for this condition.
     *
     * @param target Target for comparison
     */
    public void setTarget(final String target) {
        this.target = target;
    }

    /**
     * Retrieves the starget of this condition.
     *
     * @return This condition's starget, or null if none was set.
     */
    public String getStarget() {
        return starget;
    }

    /**
     * Sets the starget for this condition.
     *
     * @param starget The new starget for this condition.
     */
    public void setStarget(final String starget) {
        this.starget = starget;
    }

    @Override
    public String toString() {
        return "[ arg=" + arg + ", component=" + component + ", comparison="
                + comparison + ", target=" + target + ", starget=" + starget + " ]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ActionCondition)) {
            return false;
        }

        final ActionCondition o = (ActionCondition) obj;

        return arg == o.getArg() && component == o.getComponent()
                && comparison == o.getComparison() && target.equals(o.getTarget())
                && starget.equals(o.getStarget());
    }

    @Override
    public int hashCode() {
        return arg + 100 * (arg == -1 ? starget.hashCode() : component.hashCode())
                + 10000 * comparison.hashCode() + 100000 * target.hashCode();
    }

}
