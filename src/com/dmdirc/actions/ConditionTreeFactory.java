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

/**
 * Provides methods to automatically generated condition tree for a specified number of arguments.
 *
 * @since 0.6
 */
public abstract class ConditionTreeFactory {

    /**
     * Retrieves a condition tree for the specified number of arguments.
     *
     * @param args The number of arguments in the {@link Action}
     *
     * @return A ConditionTree for the specified number of args
     */
    public abstract ConditionTree getConditionTree(final int args);

    /**
     * Retrieves the type this of factory.
     *
     * @return This factory's type
     */
    public abstract ConditionTreeFactoryType getType();

    /**
     * The possible types of ConditionTreeFactories.
     */
    public enum ConditionTreeFactoryType {

        /** Factories that produce disjunction (OR) trees. */
        DISJUNCTION,
        /** Factories that produce conjunction (AND) trees. */
        CONJUNCTION,
        /** Factories that produce custom trees. */
        CUSTOM,

    }

    /**
     * Retrieves a factory that will extrapolate the specified {@link ConditionTree} for different
     * number of arguments, if applicable.
     *
     * @param tree The {@link ConditionTree} that's in use
     * @param args The number of conditions currently in use
     *
     * @return A {@link ConditionTreeFactory} that will create relevant {@link ConditionTree}s
     */
    public static ConditionTreeFactory getFactory(final ConditionTree tree, final int args) {
        if (tree.equals(ConditionTree.createConjunction(args))) {
            return new ConjunctionFactory();
        } else if (tree.equals(ConditionTree.createDisjunction(args))) {
            return new DisjunctionFactory();
        } else {
            return new CustomFactory(tree);
        }
    }

    /**
     * Creates condition trees where the arguments are conjoined together.
     */
    public static class ConjunctionFactory extends ConditionTreeFactory {

        @Override
        public ConditionTree getConditionTree(final int args) {
            return ConditionTree.createConjunction(args);
        }

        @Override
        public ConditionTreeFactoryType getType() {
            return ConditionTreeFactoryType.CONJUNCTION;
        }

    }

    /**
     * Creates condition trees where the arguments are disjointed together.
     */
    public static class DisjunctionFactory extends ConditionTreeFactory {

        @Override
        public ConditionTree getConditionTree(final int args) {
            return ConditionTree.createDisjunction(args);
        }

        @Override
        public ConditionTreeFactoryType getType() {
            return ConditionTreeFactoryType.DISJUNCTION;
        }

    }

    /**
     * Creates condition trees with a custom structure.
     */
    public static class CustomFactory extends ConditionTreeFactory {

        /** The condition tree to use. */
        protected final ConditionTree tree;

        /**
         * Creates a new CustomFactory for the specified tree.
         *
         * @param tree The tree to use
         */
        public CustomFactory(final ConditionTree tree) {
            this.tree = tree;
        }

        @Override
        public ConditionTree getConditionTree(final int args) {
            return tree;
        }

        @Override
        public ConditionTreeFactoryType getType() {
            return ConditionTreeFactoryType.CUSTOM;
        }

    }

}
