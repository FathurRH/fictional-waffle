# fictional-waffle

Basic Kafka Message Forwarder: a small order system that shows how Spring Boot microservices talk to each other through Apache Kafka instead of calling each other over HTTP.

```
             POST /orders
                  │
          ┌───────▼───────┐   OrderCreated    ┌─────────────────┐
          │ order-service │ ───── orders ───▶ │ payment-service │
          │    :8081      │ ◀─── payments ─── │                 │
          └───────────────┘  PaymentProcessed └─────────────────┘
```

1. `order-service` saves the order as `PENDING_PAYMENT` and publishes `OrderCreated` to the `orders` topic.
2. `payment-service` consumes it, runs a fake charge (amounts over 1000 are declined) and publishes `PaymentProcessed` to the `payments` topic.
3. `order-service` consumes the result and marks the order `PAID` or `PAYMENT_FAILED`.

The two services share no code and no database. The only contract between them is the JSON on the topics.

## Run it

You need Java 21, Maven and Docker.

```bash
docker compose up -d                                  # Kafka on localhost:9092, Kafka UI on http://localhost:8090
mvn -pl payment-service spring-boot:run               # terminal 1
mvn -pl order-service spring-boot:run                 # terminal 2
```

Place an order, then check its status:

```bash
curl -s -X POST localhost:8081/orders -H 'Content-Type: application/json' \
     -d '{"customerId":"c-1","amount":250}'
curl -s localhost:8081/orders/<id-from-above>         # PAID

curl -s -X POST localhost:8081/orders -H 'Content-Type: application/json' \
     -d '{"customerId":"c-1","amount":5000}'          # ends up PAYMENT_FAILED
```

Open http://localhost:8090 to see the messages on each topic.

## Tests

```bash
mvn verify
```

The tests start an in-memory Kafka broker (`@EmbeddedKafka`), so they don't need Docker.

## Things to try

- Stop `payment-service`, place a few orders, start it again. The orders get paid anyway, because Kafka kept the messages and the consumer group remembers its offset.
- Start a second `payment-service` (`mvn -pl payment-service spring-boot:run` in another terminal). Both join the `payment-service` group and Kafka splits the 3 partitions between them. Watch the logs.

## Roadmap

- [x] Step 1: producer and consumer, request/response through two topics
- [ ] Step 2: consumer groups and partitions in depth
- [ ] Step 3: retries and a dead-letter topic
- [ ] Step 4: `inventory-service` and the saga pattern (compensating actions)
- [ ] Step 5: idempotent consumers and the transactional outbox
