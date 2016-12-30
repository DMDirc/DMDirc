package com.dmdirc.commandparser.commands;

import com.dmdirc.interfaces.CommandController;

/**
 * @deprecated Use {@link BaseCommand} directly.
 */
@Deprecated
public abstract class Command extends BaseCommand {

    public Command(CommandController controller) {
        super(controller);
    }

}
