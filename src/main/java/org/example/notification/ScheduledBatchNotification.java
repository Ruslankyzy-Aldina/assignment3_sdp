package org.example.notification;

import org.example.sender.MessageSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ScheduledBatchNotification extends Notification {
    private final List<String> recipients;

    public ScheduledBatchNotification(MessageSender sender, Collection<String> recipients) {
        super(sender);
        this.recipients = new ArrayList<>(Objects.requireNonNull(recipients, "Recipients must not be null"));
    }

    @Override
    public void send(String recipient, String message) {
        validateMessage(message);
        boolean successful = sender.sendMessage(recipient, "Batch notification", message);
        System.out.println("Batch notification delivery: " + successful);
    }

    public void sendBatch(String message) {
        validateMessage(message);
        int successful = 0;

        for (String recipient : recipients) {
            if (sender.sendMessage(recipient, "Batch notification", message)) {
                successful++;
            }
        }

        System.out.println("Total: " + recipients.size());
        System.out.println("Successful: " + successful);
        System.out.println("Failed: " + (recipients.size() - successful));
    }
}
