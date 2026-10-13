package org.example.notification;

import org.example.sender.MessageSender;

import java.util.Objects;

public abstract class Notification {
    protected MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "Sender must not be null");
    }

    public void setSender(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "Sender must not be null");
    }

    protected void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
    }

    public abstract void send(String recipient, String message);
}
