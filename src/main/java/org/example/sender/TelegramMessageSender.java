package org.example.sender;

public class TelegramMessageSender implements MessageSender {
    private final boolean available;

    public TelegramMessageSender() {
        this(true);
    }

    public TelegramMessageSender(boolean available) {
        this.available = available;
    }

    @Override
    public boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            return false;
        }
        System.out.println("[Telegram] To: " + recipient);
        System.out.println("<b>" + escapeHtml(title) + "</b>\n" + escapeHtml(body));
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getChannelName() {
        return "Telegram";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
