package com.app.sqs.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.core.SqsMessageHeaders;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.messaging.Message;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsManager.class);

    private final SqsInstance sqsInstance;
    private final Thread consumerThread;
    private final Map<Pair<Object, Method>, ConsumeMethodHolder> hookedConsumers;

    public SqsManager(final SqsInstance sqsInstance, final boolean createConsumer) {
        this.sqsInstance = sqsInstance;
        this.hookedConsumers = new ConcurrentHashMap<>();
        if (createConsumer) {
            this.consumerThread = new Thread(this::consume);
        } else {
            this.consumerThread = null;
        }
    }

    @SuppressWarnings("java:S1452")
    public Message<?> receive() {
        return this.sqsInstance.poll();
    }

    private void consume() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final Message<?> taken = this.sqsInstance.take();
                boolean removed = this.hookedConsumers.values().stream()
                        .map(consumer -> this.handledConsume(consumer, taken))
                        .reduce(Boolean::logicalOr)
                        .orElse(false);
                if (!removed) {
                    this.send(taken);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean handledConsume(final ConsumeMethodHolder consumeMethodHolder, final Message<?> taken) {
        try {
            return consumeMethodHolder.invoke(taken);
        } catch (RuntimeException e) {
            LOGGER.error("There was an error executing consumer {}", consumeMethodHolder, e);
            return false;
        }
    }

    public void start() {
        if (this.consumerThread != null) {
            this.consumerThread.start();
        }
    }

    public void stop() {
        LOGGER.trace("Stopping!!!!");
        if (this.consumerThread != null) {
            this.consumerThread.interrupt();
        }
    }

    public void addListener(final Object bean, final Method method,
                            final SqsMessageDeletionPolicy deletionPolicy) {
        this.hookedConsumers.putIfAbsent(Pair.of(bean, method), new ConsumeMethodHolder(bean, method, deletionPolicy));
    }

    public void removeListener(final Object bean, final Method method) {
        this.hookedConsumers.remove(Pair.of(bean, method));
    }

    public void send(final Message<?> message) {
        Object delayValue = message.getHeaders().get(SqsMessageHeaders.SQS_DELAY_HEADER);
        if (delayValue instanceof Number) {
            this.sqsInstance.add(message, ((Number) delayValue).longValue());
        } else {
            this.sqsInstance.add(message);
        }
    }
}
