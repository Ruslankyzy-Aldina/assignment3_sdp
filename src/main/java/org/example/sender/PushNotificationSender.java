package org.example.sender;

public class PushNotificationSender implements MessageSender {
    private final boolean available;

    public PushNotificationSender() {
        this(true);
    }

    public PushNotificationSender(boolean available) {
        this.available = available;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            return false;
        }

        System.out.println("[Push] Device token: " + recipient);
        System.out.println("Notification title: " + title);
        System.out.println("Notification body: " + body);
        System.out.println("Displayed in the device notification tray.");
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Push";
    }
}
