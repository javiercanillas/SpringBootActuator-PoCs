package com.app.sqs.simulation;

import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class SqsInstance {

    private final DelayQueue<DelayedItem> internalQueue;

    public SqsInstance() {
        this.internalQueue = new DelayQueue<>();
    }

    public boolean add(final Message<?> content) {
        return this.add(content, 0L);
    }

    public boolean add(final Message<?> content, final long delayInMillis) {
        return this.internalQueue.add(new DelayedItem(content, delayInMillis));
    }

    @SuppressWarnings("java:S1452")
    public Message<?> take() throws InterruptedException {
        final DelayedItem taken = this.internalQueue.take();
        return taken.getContent();
    }

    @SuppressWarnings("java:S1452")
    public Message<?> poll() {
        final DelayedItem taken = this.internalQueue.poll();
        return Optional.ofNullable(taken).map(DelayedItem::getContent).orElse(null);
    }

    private static class DelayedItem implements Delayed {

        private final Message<?> content;
        private final long consumeOnTimeInMillis;

        DelayedItem(final Message<?> content, final long delayInSeconds) {
            this.content = content;
            this.consumeOnTimeInMillis = System.currentTimeMillis() + (delayInSeconds > 0 ? delayInSeconds : 0);
        }
        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(this.consumeOnTimeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(final Delayed o) {
            return Long.valueOf(this.consumeOnTimeInMillis - ((DelayedItem) o).getConsumeOnTimeInMillis()).intValue();
        }

        public long getConsumeOnTimeInMillis() {
            return consumeOnTimeInMillis;
        }

        @SuppressWarnings("java:S1452")
        public Message<?> getContent() {
            return content;
        }
    }
}
