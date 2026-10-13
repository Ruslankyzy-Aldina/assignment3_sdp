package org.example;

import org.example.sender.*;

public class Main {
    public static void main(String[] args) {
        MessageSender[] senders = {
                new TelegramMessageSender(),
                new EmailMessageSender(),
                new SmsMessageSender(),
                new KafkaEventSender()
        };

        for (MessageSender sender : senders) {
            boolean sent = sender.sendMessage("student", "Reminder", "Your assignment is due soon.");
            System.out.println(sender.getChannelName() + " delivery: " + sent);
            System.out.println();
        }
    }
}
