package org.example.notification;

import org.example.sender.MessageSender;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptedNotification extends Notification {
    public EncryptedNotification(MessageSender sender) {
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        validateMessage(message);
        // Base64 simulates the transformation required by the assignment; it is not encryption.
        String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        boolean successful = sender.sendMessage(recipient, "Encoded notification", encodedMessage);
        System.out.println("Encoded notification delivery: " + successful);
    }
}
