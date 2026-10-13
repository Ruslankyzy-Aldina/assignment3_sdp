package org.example.notification;

import org.example.sender.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {
    private static class RecordingSender implements MessageSender {
        boolean available = true;
        boolean deliveryResult = true;
        final List<String> recipients = new ArrayList<>();
        String title;
        String body;

        @Override
        public boolean sendMessage(String recipient, String title, String body) {
            recipients.add(recipient);
            this.title = title;
            this.body = body;
            return available && deliveryResult && !"invalid".equals(recipient);
        }

        @Override
        public boolean isAvailable() { return available; }

        @Override
        public String getChannelName() { return "Recording"; }
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(stream);
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void urgentAddsPrefixAndUsesPrimaryProvider() {
        RecordingSender primary = new RecordingSender();
        RecordingSender fallback = new RecordingSender();
        new UrgentNotification(primary, fallback).send("user", "Hello");
        assertEquals(List.of("user"), primary.recipients);
        assertEquals("[URGENT] Hello", primary.body);
        assertTrue(fallback.recipients.isEmpty());
    }

    @Test
    void urgentUsesFallbackWhenPrimaryIsUnavailable() {
        RecordingSender primary = new RecordingSender();
        primary.available = false;
        RecordingSender fallback = new RecordingSender();
        new UrgentNotification(primary, fallback).send("user", "Hello");
        assertTrue(primary.recipients.isEmpty());
        assertEquals(List.of("user"), fallback.recipients);
        assertEquals("[URGENT] Hello", fallback.body);
    }

    @Test
    void urgentReportsFailureWhenBothProvidersAreUnavailable() {
        RecordingSender primary = new RecordingSender();
        RecordingSender fallback = new RecordingSender();
        primary.available = false;
        fallback.available = false;
        String output = captureOutput(() -> new UrgentNotification(primary, fallback).send("user", "Hello"));
        assertTrue(output.contains("no available fallback"));
        assertTrue(primary.recipients.isEmpty());
        assertTrue(fallback.recipients.isEmpty());
    }

    @Test
    void urgentHandlesMissingFallback() {
        RecordingSender primary = new RecordingSender();
        primary.available = false;
        String output = captureOutput(() -> new UrgentNotification(primary).send("user", "Hello"));
        assertTrue(output.contains("no available fallback"));
        assertTrue(primary.recipients.isEmpty());
    }

    @Test
    void switchingProviderRedirectsSameNotificationObject() {
        RecordingSender first = new RecordingSender();
        RecordingSender second = new RecordingSender();
        Notification notification = new UrgentNotification(first);
        notification.send("first-user", "First");
        notification.setSender(second);
        notification.send("second-user", "Second");
        assertEquals(List.of("first-user"), first.recipients);
        assertEquals(List.of("second-user"), second.recipients);
        assertEquals("[URGENT] Second", second.body);
    }

    @Test
    void encryptedTransformsUtf8MessageBeforeSending() {
        RecordingSender sender = new RecordingSender();
        String message = "Hello, Мир!";
        new EncryptedNotification(sender).send("user", message);
        assertNotEquals(message, sender.body);
        assertEquals(message, new String(Base64.getDecoder().decode(sender.body), StandardCharsets.UTF_8));
        assertEquals(List.of("user"), sender.recipients);
    }

    @Test
    void batchVisitsAllRecipientsAndCountsMixedResults() {
        RecordingSender sender = new RecordingSender();
        List<String> recipients = List.of("first", "invalid", "last");
        ScheduledBatchNotification batch = new ScheduledBatchNotification(sender, recipients);
        String output = captureOutput(() -> batch.sendBatch("Hello"));
        assertEquals(recipients, sender.recipients);
        assertTrue(output.contains("Total: 3"));
        assertTrue(output.contains("Successful: 2"));
        assertTrue(output.contains("Failed: 1"));
    }

    @Test
    void emptyBatchMakesNoDeliveryAttempts() {
        RecordingSender sender = new RecordingSender();
        String output = captureOutput(() -> new ScheduledBatchNotification(sender, List.of()).sendBatch("Hello"));
        assertTrue(sender.recipients.isEmpty());
        assertTrue(output.contains("Total: 0"));
        assertTrue(output.contains("Successful: 0"));
        assertTrue(output.contains("Failed: 0"));
    }

    @Test
    void batchCountsSmsLengthFailures() {
        ScheduledBatchNotification batch = new ScheduledBatchNotification(new SmsMessageSender(), List.of("one", "two"));
        String output = captureOutput(() -> batch.sendBatch("a".repeat(161)));
        assertTrue(output.contains("Successful: 0"));
        assertTrue(output.contains("Failed: 2"));
    }

    @Test
    void marketingAddsPromotionAndPassesRecipient() {
        RecordingSender sender = new RecordingSender();
        new MarketingNotification(sender).send("customer", "20% off");
        assertEquals("Special offer", sender.title);
        assertEquals("[PROMO] 20% off", sender.body);
        assertEquals(List.of("customer"), sender.recipients);
    }

    @Test
    void marketingWorksWithEveryProvider() {
        List<MessageSender> senders = List.of(new TelegramMessageSender(), new EmailMessageSender(),
                new SmsMessageSender(), new KafkaEventSender(), new PushNotificationSender());
        for (MessageSender sender : senders) {
            String output = captureOutput(() -> new MarketingNotification(sender).send("customer", "Discount"));
            assertTrue(output.contains("Marketing notification delivery: true"), sender.getChannelName());
            assertTrue(output.contains("[PROMO] Discount"), sender.getChannelName());
        }
    }

    @Test
    void pushWorksWithExistingNotificationTypes() {
        MessageSender push = new PushNotificationSender();
        assertTrue(captureOutput(() -> new UrgentNotification(push).send("device", "Hello"))
                .contains("Urgent notification delivery: true"));
        assertTrue(captureOutput(() -> new EncryptedNotification(push).send("device", "Hello"))
                .contains("Encoded notification delivery: true"));
        assertTrue(captureOutput(() -> new ScheduledBatchNotification(push, List.of("device")).sendBatch("Hello"))
                .contains("Successful: 1"));
    }

    @Test
    void rejectedDeliveryIsReportedAsFailure() {
        RecordingSender sender = new RecordingSender();
        sender.deliveryResult = false;
        String output = captureOutput(() -> new MarketingNotification(sender).send("customer", "Discount"));
        assertTrue(output.contains("Marketing notification delivery: false"));
    }

    @Test
    void blankMessageIsRejectedBeforeAddingPrefixOrEncoding() {
        RecordingSender sender = new RecordingSender();
        List<Notification> notifications = List.of(new UrgentNotification(sender), new EncryptedNotification(sender),
                new MarketingNotification(sender), new ScheduledBatchNotification(sender, List.of("user")));
        for (Notification notification : notifications) {
            assertThrows(IllegalArgumentException.class, () -> notification.send("user", " "));
            assertThrows(IllegalArgumentException.class, () -> notification.send("user", null));
        }
        assertTrue(sender.recipients.isEmpty());
    }
}
