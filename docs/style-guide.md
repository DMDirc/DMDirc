Style guide
================================================================================

This document aims to give a brief introduction to the code style we try to use
in DMDirc. Having a consistent style makes it easier for developers to read code
and reduces conflicts and excessive changes caused by individual style
preferences.

If you have any comments or suggestions about these guidelines, please raise
an issue or open a pull request.

## General

* Lines should not exceed 120 columns
* Java source code is indented using 4 space characters
* Trailing whitespace should be removed
* Braces should always be used even when optional (`if`/`while`/etc blocks)
* Braces should open on the same line as the block begins (`if (...) {`)
* `@Override` should be used whenever possible (including interface
  implementations)
* British English is preferred over American English

## Imports

* Do not import classes just for JavaDoc references (use fully-qualified names
  instead)
* Imports are split into non-static and static, separated by a blank line
* Imports are then sorted lexicographically (in alphabetical order), with no spaces
* Do not use wildcard (*) imports

## Constructors

* Constructors should be the first method defined in the class
* Explicit default constructors should be avoided unless required
* Explicit calls to `super()` in constructors should be avoided

## Methods

* Method annotations should each be on a separate line
* All modifiers should be in the order recommended by the JLS:
  `public`, `protected`, `private`, `abstract`, `default`, `static`, `final`,
  `transient`, `volatile`, `synchronized`, `native`, `strictfp`
* Method-level synchronisation should be avoided in favour of `synchronized`
  blocks on private objects

## Documentation

* All classes should be documented using javadoc
* Author names should not be included, and should be actively removed if present
* Methods should be documented unless their function is extremely obvious (e.g.
  getters and setters)
* Methods overriding a superclass/interface may omit documentation to inherit
  the parent's
* `{@inheritDoc}` should only be used when adding extra information to a parent's
  documentation