package org.example.notification;

import org.example.sender.MessageSender;

public class MarketingNotification extends Notification {
    public MarketingNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        validateMessage(message);
        String promotionalMessage = "[PROMO] " + message;
        boolean successful = sender.sendMessage(recipient, "Special offer", promotionalMessage);
        System.out.println("Marketing notification delivery: " + successful);
    }
}
