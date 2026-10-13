package org.example.sender;

public class KafkaEventSender implements MessageSender {
    private final boolean available;

    public KafkaEventSender() {
        this(true);
    }

    public KafkaEventSender(boolean available) {
        this.available = available;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            return false;
        }
        System.out.println("[Kafka] Published event to topic: notifications");
        System.out.println("Key: " + recipient);
        System.out.println("Title: " + title + "\nPayload: " + body);
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Kafka";
    }
}
