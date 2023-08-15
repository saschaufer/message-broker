package de.saschaufer.message_broker.app.agent_http.exception;


import de.saschaufer.message_broker.plugin.spi.Message;

public class RuntimeExceptionWithMessage extends RuntimeException {
    private final Message routedMessage;

    public RuntimeExceptionWithMessage(final String message, final Message routedMessage) {
        this(message, null, routedMessage);
    }

    public RuntimeExceptionWithMessage(final String message, final Throwable cause, final Message routedMessage) {
        // Suppress stacktrace since we know were a method is called by a meaningful message.
        // The stacktrace of the Exception from the mapper is not suppressed.
        super(message, cause, true, false);
        this.routedMessage = routedMessage;
    }

    public Message getRoutedMessage() {
        return routedMessage;
    }
}
