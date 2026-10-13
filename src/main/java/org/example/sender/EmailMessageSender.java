package org.example.sender;

public class EmailMessageSender implements MessageSender {
    private final boolean available;

    public EmailMessageSender() {
        this(true);
    }

    public EmailMessageSender(boolean available) {
        this.available = available;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            return false;
        }
        System.out.println("[Email] To: " + recipient);
        System.out.println("Subject: " + title);
        System.out.println(body + "\n\nBest regards,\nNotification Gateway");
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Email";
    }
}
