package org.example.sender;

public class SmsMessageSender implements MessageSender {
    public static final int MAX_LENGTH = 160;

    private final boolean available;

    public SmsMessageSender() {
        this(true);
    }

    public SmsMessageSender(boolean available) {
        this.available = available;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            return false;
        }
        String message = title.isBlank() ? body : title + ": " + body;
        if (message.codePointCount(0, message.length()) > MAX_LENGTH) {
            return false;
        }
        System.out.println("[SMS] To: " + recipient + " | " + message);
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
