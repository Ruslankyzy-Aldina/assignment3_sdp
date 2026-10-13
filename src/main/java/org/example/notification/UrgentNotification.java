package org.example.notification;

import org.example.sender.MessageSender;

public class UrgentNotification extends Notification {
    private final MessageSender fallbackSender;

    public UrgentNotification(MessageSender sender) {
        this(sender, null);
    }

    public UrgentNotification(MessageSender sender, MessageSender fallbackSender) {
        super(sender);
        this.fallbackSender = fallbackSender;
    }

    @Override
    public void send(String recipient, String message) {
        validateMessage(message);
        MessageSender activeSender = sender;

        if (!activeSender.isAvailable()) {
            System.out.println(activeSender.getChannelName() + " is unavailable.");
            if (fallbackSender == null || !fallbackSender.isAvailable()) {
                System.out.println("Urgent notification failed: no available fallback.");
                return;
            }
            activeSender = fallbackSender;
            System.out.println("Using fallback: " + activeSender.getChannelName());
        }

        boolean successful = activeSender.sendMessage(recipient, "Urgent notification", "[URGENT] " + message);
        System.out.println("Urgent notification delivery: " + successful);
    }
}
