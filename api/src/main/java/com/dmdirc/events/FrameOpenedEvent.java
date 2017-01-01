package com.dmdirc.events;

import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

/**
 * Fired when a new window is opened.
 */
public class FrameOpenedEvent extends FrameEvent {

    private final Optional<WindowModel> parent;

    public FrameOpenedEvent(final WindowModel source, final WindowModel parent) {
        super(source);
        this.parent = Optional.of(parent);
    }

    public FrameOpenedEvent(final WindowModel source) {
        super(source);
        this.parent = Optional.empty();
    }

    public Optional<WindowModel> getParent() {
        return parent;
    }

}
