package org.example;

import org.example.sender.*;
import org.example.notification.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        MessageSender telegram = new TelegramMessageSender();
        MessageSender email = new EmailMessageSender();
        MessageSender sms = new SmsMessageSender();

        System.out.println("1. Urgent notification via Telegram");
        Notification urgent = new UrgentNotification(telegram, sms);
        urgent.send("student", "Your assignment is due soon.");

        System.out.println("\n2. Encoded notification via Email");
        Notification encrypted = new EncryptedNotification(email);
        encrypted.send("student@example.com", "Your access code is 1234.");

        System.out.println("\n3. Batch notification via SMS");
        ScheduledBatchNotification batch = new ScheduledBatchNotification(
                sms, List.of("+77000000001", "+77000000002", "+77000000003"));
        batch.sendBatch("Class starts at 10:00.");

        System.out.println("\n4. Switch the same urgent notification to Email");
        urgent.setSender(email);
        urgent.send("student@example.com", "Your assignment is due soon.");

        System.out.println("\n5. Unavailable Telegram with SMS fallback");
        Notification withFallback = new UrgentNotification(new TelegramMessageSender(false), sms);
        withFallback.send("+77000000001", "Please check your schedule.");
    }
}
