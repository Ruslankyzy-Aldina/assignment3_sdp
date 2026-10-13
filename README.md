# Multi-Channel Notification Gateway

A Java demonstration of the Bridge design pattern. Notification behavior and message delivery channels are modeled as separate dimensions, so either side can be extended without creating a class for every combination.

## Requirements

- Java 17 or later
- Maven 3.9 or later

## Run

Compile the application and run the demonstration:

```bash
mvn compile
java -cp target/classes org.example.Main
```

Run the unit tests:

```bash
mvn test
```

The demonstration can also be run from an IDE by starting `org.example.Main`.

## Demonstrated scenarios

`Main` demonstrates urgent delivery through Telegram, an encoded notification through Email, a batch through SMS, changing a notification's sender at runtime, fallback from unavailable Telegram to SMS, and a marketing notification through Push.

All providers are simulations. They print a representation of the delivery and return whether it succeeded. SMS rejects messages longer than 160 Unicode code points, including the title and separator when a title is present. `EncryptedNotification` uses Base64 to demonstrate message transformation; Base64 is not encryption and must not be used to protect sensitive information.

## Bridge design

- **Abstraction:** `Notification`, which holds a `MessageSender` reference and exposes the notification operation.
- **Refined abstractions:** `UrgentNotification`, `EncryptedNotification`, `ScheduledBatchNotification`, and `MarketingNotification`, each of which adds notification-specific behavior.
- **Implementor:** the `MessageSender` interface.
- **Concrete implementors:** `TelegramMessageSender`, `EmailMessageSender`, `SmsMessageSender`, `KafkaEventSender`, and `PushNotificationSender`.
- **Bridge relationship:** `Notification` has a `MessageSender` and delegates delivery through that interface. `setSender` can replace the provider while the notification object remains the same.

## Design questions

**What are the two independent dimensions?** Notification types (urgent, encoded, batch, and marketing) and delivery providers (Telegram, Email, SMS, Kafka, and Push).

**Where exactly is the Bridge?** The `sender` field in `Notification` points to the `MessageSender` interface. Each notification delegates its delivery through that interface, so it does not need to know which concrete provider performs it.

**Why is inheritance problematic here?** If each notification type were combined with each provider through subclasses, every new type or provider would multiply the number of combination classes. Those subclasses would also duplicate behavior and make changes harder to keep consistent.

**What happens when a new provider is added?** Implement `MessageSender` in a new class. Existing notification classes can use it as-is. `PushNotificationSender` is the example in this project.

**What happens when a new notification type is added?** Extend `Notification` and implement `send`. Existing providers remain usable. `MarketingNotification` is the example in this project.

**Why is this Bridge rather than Adapter?** These abstractions and providers are designed to work together through `MessageSender`; the interface separates two dimensions that can evolve independently. An Adapter instead translates an existing incompatible interface so that it can be used by a client.
