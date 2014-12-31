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

import com.dmdirc.FrameContainer;
import com.dmdirc.GlobalWindow;
import com.dmdirc.Precondition;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.actions.ActionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

/**
 * Represents the basic model of an action, and its triggering mechanism. Saving and loading are
 * handled by the Action class.
 */
public class ActionModel {

    // TODO: Sort out the mess of protected fields here.

    /** Provider of global windows for global actions. */
    private final Provider<GlobalWindow> globalWindowProvider;
    /** Factory to use to creator substitutors. */
    private final ActionSubstitutorFactory substitutorFactory;
    /** The group this action belongs to. */
    protected String group;
    /** The name of this action. */
    protected String name;
    /** The ActionTypes that trigger this action. */
    protected ActionType[] triggers;
    /** The commands to execute if this action is triggered. */
    protected String[] response;
    /** The change that should be made to the format string, if any. */
    protected String newFormat;
    /** The conditions for this action. */
    protected List<ActionCondition> conditions = new ArrayList<>();
    /** The condition tree used for evaluating conditions. */
    protected ConditionTree conditionTree;
    /** Whether this action has been modified or not. */
    protected boolean modified;
    /** Whether this action wishes the event to be stopped. */
    protected boolean stop;
    /** The concurrency group this action belongs to, if any. */
    protected String concurrencyGroup;
    /** The status of this action. */
    protected ActionStatus status = ActionStatus.ACTIVE;
    /** A description of any error that occurs while creating the action. */
    protected String error;
    /** The type of error that occurred, if any. */
    protected ActionErrorType errorType;

    /**
     * Creates a new instance of ActionModel with the specified properties.
     *
     * @param globalWindowProvider        Provider of global windows for global actions.
     * @param substitutorFactory          Factory to use to create action substitutors.
     * @param group                       The group the action belongs to
     * @param name                        The name of the action
     */
    public ActionModel(
            final Provider<GlobalWindow> globalWindowProvider,
            final ActionSubstitutorFactory substitutorFactory,
            final String group,
            final String name) {
        this.globalWindowProvider = globalWindowProvider;
        this.substitutorFactory = substitutorFactory;
        this.group = group;
        this.name = name;
    }

    /**
     * Creates a new instance of ActionModel with the specified properties.
     *  @param globalWindowProvider        Provider of global windows for global actions.
     * @param substitutorFactory          Factory to use to create action substitutors.
     * @param group                       The group the action belongs to
     * @param name                        The name of the action
     * @param triggers                    The triggers to use
     * @param response                    The response to use
     * @param conditions                  The conditions to use
     * @param conditionTree               The condition tree to use
     * @param newFormat                   The new formatter to use
     */
    public ActionModel(
            final Provider<GlobalWindow> globalWindowProvider,
            final ActionSubstitutorFactory substitutorFactory,
            final String group, final String name,
            final ActionType[] triggers, final String[] response,
            final List<ActionCondition> conditions,
            final ConditionTree conditionTree, final String newFormat) {
        this(globalWindowProvider, substitutorFactory, group, name);
        this.triggers = triggers.clone();
        this.response = response.clone();
        this.conditions = conditions;
        this.conditionTree = conditionTree;
        this.newFormat = newFormat;
        this.modified = true;
    }

    /**
     * Triggers this action.
     *
     * @param format        The format of the message that's going to be displayed.
     * @param arguments     The arguments from the action that caused this trigger.
     *
     * @return True if the execution of the event should be stopped, or false if the event may
     *         continue
     */
    @Precondition({
        "This action has at least one trigger",
        "This action's primary trigger is non-null"
    })
    public boolean trigger(final StringBuffer format, final Object... arguments) {
        assert triggers.length > 0;
        assert triggers[0] != null;

        if (status != ActionStatus.ACTIVE) {
            return false;
        }

        final ActionSubstitutor sub = substitutorFactory.getActionSubstitutor(triggers[0]);

        if (!test(sub, arguments)) {
            return false;
        }

        final FrameContainer container;
        if (arguments.length > 0 && arguments[0] instanceof FrameContainer
                && ((FrameContainer) arguments[0]).isWritable()) {
            container = (FrameContainer) arguments[0];
        } else {
            container = globalWindowProvider.get();
        }

        final CommandParser cp = container.getCommandParser();
        for (String command : response) {
            cp.parseCommand(container, sub.doSubstitution(command, arguments));
        }

        if (newFormat != null && format != null) {
            format.setLength(0);
            format.append(newFormat);
        }

        return stop;
    }

    /**
     * Tests to see if this action should be triggered or not.
     *
     * @param sub       The ActionsSubstitutor to use to substitute args
     * @param arguments The arguments for the action event
     *
     * @return True if the action should be executed, false otherwise
     */
    public boolean test(final ActionSubstitutor sub,
            final Object... arguments) {
        final boolean[] results = new boolean[conditions.size()];

        int i = 0;
        for (ActionCondition condition : conditions) {
            results[i++] = condition.test(sub, arguments);
        }

        return getRealConditionTree().evaluate(results);
    }

    /**
     * Retrieves a list of this action's conditions.
     *
     * @return A list of this action's conditions
     */
    public List<ActionCondition> getConditions() {
        return conditions;
    }

    /**
     * Sets this action's conditions.
     *
     * @param conditions A list of conditions to use
     */
    public void setConditions(final List<ActionCondition> conditions) {
        this.conditions = conditions;
        this.modified = true;
    }

    /**
     * Retrieves the status of this action.
     *
     * @since 0.6.5
     * @return This action's current status
     */
    public ActionStatus getStatus() {
        return status;
    }

    /**
     * Retrieves a plain text description of any error that occurred while loading this action.
     *
     * @since 0.6.5
     * @return This action's error message, or null if no error was encountered
     */
    public String getError() {
        return error;
    }

    /**
     * Retrieves the type of the error that occurred while loading this action.
     *
     * @since 0.6.5
     * @return This action's error's type, or null if no error was encountered
     */
    public ActionErrorType getErrorType() {
        return errorType;
    }

    /**
     * Retrieves this action's triggers.
     *
     * @return The triggers used by this action
     */
    public ActionType[] getTriggers() {
        return triggers == null ? null : triggers.clone();
    }

    /**
     * Sets this action's triggers.
     *
     * @param triggers The new triggers to use
     */
    public void setTriggers(final ActionType[] triggers) {
        this.triggers = triggers.clone();
        this.modified = true;
    }

    /**
     * Retrieves this action's new format setting.
     *
     * @return The format that this action will use, or null if no change
     */
    public String getNewFormat() {
        return newFormat;
    }

    /**
     * Sets this action's new format setting.
     *
     * @param newFormat The new 'new format' setting
     */
    public void setNewFormat(final String newFormat) {
        this.newFormat = newFormat;
        this.modified = true;
    }

    /**
     * Retrieves this action's response.
     *
     * @return The commands that will be executed if this action is triggered
     */
    public String[] getResponse() {
        return response == null ? null : response.clone();
    }

    /**
     * Sets this action's response.
     *
     * @param response The new response to use
     */
    public void setResponse(final String[] response) {
        this.response = response.clone();
        this.modified = true;
    }

    /**
     * Retrieves this action's group name.
     *
     * @return This action's group name
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the group of this action.
     *
     * @param newGroup The new group for this action
     */
    public void setGroup(final String newGroup) {
        this.group = newGroup;
        this.modified = true;
    }

    /**
     * Retrieves this action's name.
     *
     * @return This action's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this action.
     *
     * @param newName The new name for this action
     */
    public void setName(final String newName) {
        this.name = newName;
        this.modified = true;
    }

    /**
     * Retrieves the condition tree used for this action. Condition trees may be null, in which case
     * the arguments are conjoined together.
     *
     * @return This action's condition tree
     */
    public ConditionTree getConditionTree() {
        return conditionTree;
    }

    /**
     * Retrieves a concrete condition tree used for this action. If there is no condition tree
     * defined for this action, returns a conjunction tree for the arguments.
     *
     * @since 0.6
     * @return A {@link ConditionTree} object for this action
     */
    public ConditionTree getRealConditionTree() {
        return conditionTree == null ? ConditionTree.createConjunction(
                conditions.size()) : conditionTree;
    }

    /**
     * Sets the condition tree used for this action.
     *
     * @param conditionTree The new condition tree to be used
     */
    public void setConditionTree(final ConditionTree conditionTree) {
        this.conditionTree = conditionTree;
        this.modified = true;
    }

    /**
     * Retrieves the concurrency group of this action.
     *
     * @return This action's concurrency group
     *
     * @since 0.6.3
     */
    public String getConcurrencyGroup() {
        return concurrencyGroup;
    }

    /**
     * Sets the concurrency group of this action.
     *
     * @param concurrencyGroup This action's new concurrency group
     *
     * @since 0.6.3
     */
    public void setConcurrencyGroup(final String concurrencyGroup) {
        this.concurrencyGroup = (concurrencyGroup == null
                || concurrencyGroup.isEmpty()) ? null : concurrencyGroup;
    }

    /**
     * Determines whether or not this action will stop the event execution if it is triggered.
     *
     * @return The stopping preference of this action
     *
     * @see #setStopping(boolean)
     * @since 0.6.4
     */
    public boolean isStopping() {
        return stop;
    }

    /**
     * Sets the stopping preference of this action. If the stopping preference is {@code true} then
     * when this action is successfully triggered,it will request that execution of the event is
     * stopped. This will prevent the default behaviour of the callee being executed.
     *
     * @param stop The new stopping preference of this action
     *
     * @see #isStopping()
     * @since 0.6.4
     */
    public void setStopping(final boolean stop) {
        this.stop = stop;
    }

    /**
     * Determine if this model has been modified since it was constructed or its modified status was
     * reset.
     *
     * @return True if this model has been modified, false otherwise
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Resets the modified status of this model. After a call to resetModified(), this model will
     * report that it has not been modified, until one of the set* methods is used.
     */
    public void resetModified() {
        this.modified = false;
    }

    @Override
    public String toString() {
        return "[name=" + group + "/" + name + ", triggers="
                + Arrays.toString(triggers) + ", response="
                + Arrays.toString(response) + ", "
                + conditions + ", format='" + newFormat + "']";
    }

}
