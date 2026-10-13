package org.example.sender;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageSenderTest {
    private List<MessageSender> availableSenders() {
        return List.of(new TelegramMessageSender(), new EmailMessageSender(),
                new SmsMessageSender(), new KafkaEventSender(), new PushNotificationSender());
    }

    @Test
    void allProvidersDeliverValidMessages() {
        for (MessageSender sender : availableSenders()) {
            assertTrue(sender.sendMessage("recipient", "Title", "Hello"), sender.getChannelName());
        }
    }

    @Test
    void unavailableProvidersRejectDelivery() {
        List<MessageSender> senders = List.of(new TelegramMessageSender(false),
                new EmailMessageSender(false), new SmsMessageSender(false),
                new KafkaEventSender(false), new PushNotificationSender(false));
        for (MessageSender sender : senders) {
            assertFalse(sender.isAvailable(), sender.getChannelName());
            assertFalse(sender.sendMessage("recipient", "Title", "Hello"), sender.getChannelName());
        }
    }

    @Test
    void providersRejectMissingRecipientsAndContent() {
        for (MessageSender sender : availableSenders()) {
            assertFalse(sender.sendMessage(null, "Title", "Hello"));
            assertFalse(sender.sendMessage(" ", "Title", "Hello"));
            assertFalse(sender.sendMessage("recipient", null, "Hello"));
            assertFalse(sender.sendMessage("recipient", "Title", null));
            assertFalse(sender.sendMessage("recipient", "Title", " "));
        }
    }

    @Test
    void smsAcceptsExactly160Characters() {
        assertTrue(new SmsMessageSender().sendMessage("recipient", "", "a".repeat(160)));
    }

    @Test
    void smsRejects161Characters() {
        assertFalse(new SmsMessageSender().sendMessage("recipient", "", "a".repeat(161)));
    }

    @Test
    void smsIncludesTitleAndSeparatorInLengthLimit() {
        MessageSender sms = new SmsMessageSender();
        assertTrue(sms.sendMessage("recipient", "Hi", "a".repeat(156)));
        assertFalse(sms.sendMessage("recipient", "Hi", "a".repeat(157)));
    }

    @Test
    void pushIdentifiesItsChannelAndDeliversToDeviceToken() {
        MessageSender push = new PushNotificationSender();
        assertEquals("Push", push.getChannelName());
        assertTrue(push.sendMessage("device-token", "Offer", "Discount"));
    }
}
