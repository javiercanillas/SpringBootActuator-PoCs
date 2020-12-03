package com.app.sqs.simulation;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.GenericMessage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalQueueMessagingTemplate extends QueueMessagingTemplate implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalQueueMessagingTemplate.class);

    private final Map<String, SqsManager> instances;
    private static final String DEFAULT = "__DEFAULT__";

    public LocalQueueMessagingTemplate(final AmazonSQSAsync amazonSqs) {
        super(amazonSqs);
        this.instances = new ConcurrentHashMap<>();
        this.instances.put(DEFAULT, new SqsManager(new SqsInstance(), false));
    }

    @Override
    public void destroy() {
        final Collection<SqsManager> sqsManagerList = this.instances.values();
        this.instances.clear();
        sqsManagerList.forEach(SqsManager::stop);
    }

    public Boolean register(final Object bean, final Method method) {
        final SqsListener annotation = AnnotationUtils.getAnnotation(method, SqsListener.class);
        if (annotation == null) {
            return false;
        }

        final Class<?> clazz = bean.getClass();
        LOGGER.info("Registering {}.{} for queues: {} with deletationPolicy: {}", clazz.getSimpleName(), method,
                annotation.value(), annotation.deletionPolicy());
        Arrays.stream(annotation.value())
                .forEach(queueName -> {
                    SqsManager sqsManager = this.instances.get(queueName);
                    if (sqsManager == null) {
                        sqsManager = new SqsManager(new SqsInstance(), true);
                        sqsManager.start();
                        this.instances.put(queueName, sqsManager);
                    }
                    sqsManager.addListener(bean, method, annotation.deletionPolicy());
                });
        return true;
    }

    public Boolean unregister(final Object bean, final Method method) {
        final SqsListener annotation = AnnotationUtils.getAnnotation(method, SqsListener.class);
        if (annotation == null) {
            return false;
        }

        final Class<?> clazz = bean.getClass();
        LOGGER.info("Unregistering {}.{} for queues: {}", clazz.getSimpleName(), method,
                annotation.value());
        Arrays.stream(annotation.value())
                .forEach(queueName -> {
                    final SqsManager sqsManager = this.instances.get(queueName);
                    if (sqsManager != null) {
                        sqsManager.removeListener(bean, method);
                    }
                });
        return true;
    }

    @Override
    public Message<?> receive() {
        return this.instances.get(DEFAULT).receive();
    }

    @Override
    public Message<?> receive(final String destinationName) {
        final SqsManager sqsManager = this.instances.get(destinationName);
        if (sqsManager != null) {
            return sqsManager.receive();
        } else {
            throw new MessagingException("Couldn't find resource by name: " + destinationName);
        }
    }

    @Override
    public Message<?> receive(final QueueMessageChannel destination) {
        return destination.receive();
    }

    @Override
    public <T> T receiveAndConvert(final Class<T> targetClass) {
        return convert(targetClass, this.receive());
    }

    @Override
    public <T> T receiveAndConvert(final String destinationName, final Class<T> targetClass) {
        return convert(targetClass, this.receive(destinationName));
    }

    @Override
    public <T> T receiveAndConvert(final QueueMessageChannel destination, final Class<T> targetClass) {
        return convert(targetClass, destination.receive());
    }

    @Override
    public void convertAndSend(final Object payload) {
        this.send(new GenericMessage<>(payload));
    }

    @Override
    public <T> void convertAndSend(final String destinationName, final T payload) {
        this.convertAndSend(destinationName, payload, (Map<String, Object>) null);
    }

    @Override
    public <T> void convertAndSend(final String destinationName, final T payload, final Map<String, Object> headers) {
        GenericMessage<Object> genericMessage;
        if (headers != null) {
            genericMessage = new GenericMessage<>(payload, headers);
        } else {
            genericMessage = new GenericMessage<>(payload);
        }
        this.send(destinationName, genericMessage);
    }

    @Override
    public void convertAndSend(final QueueMessageChannel destination, final Object payload) {
        destination.send(new GenericMessage<>(payload));
    }

    @Override
    public void convertAndSend(final Object payload, final MessagePostProcessor postProcessor) {
        // To simplify, we will do nothing with the postProcessor
        this.convertAndSend(payload);
    }

    @Override
    public void convertAndSend(final QueueMessageChannel destination, final Object payload,
                               final Map<String, Object> headers) {
        destination.send(new GenericMessage<>(payload, headers));
    }

    @Override
    public <T> void convertAndSend(final String destinationName, final T payload,
                                   final MessagePostProcessor postProcessor) {
        // To simplify, we will do nothing with the postProcessor
        this.convertAndSend(destinationName, payload);
    }

    @Override
    public <T> void convertAndSend(final String destinationName, final T payload, final Map<String, Object> headers,
                                   final MessagePostProcessor postProcessor) {
        // To simplify, we will do nothing with the postProcessor
        this.convertAndSend(destinationName, payload, headers);
    }

    @Override
    public void convertAndSend(final QueueMessageChannel destination, final Object payload,
                               final MessagePostProcessor postProcessor) {
        // To simplify, we will do nothing with the postProcessor
        this.convertAndSend(destination, payload);
    }

    @Override
    public void convertAndSend(final QueueMessageChannel destination, final Object payload,
                               final Map<String, Object> headers,
                               final MessagePostProcessor postProcessor) {
        // To simplify, we will do nothing with the postProcessor
        this.convertAndSend(destination, payload, headers);
    }

    @Override
    public void send(final Message<?> message) {
        this.instances.get(DEFAULT).send(message);
    }

    @Override
    public void send(final String destinationName, final Message<?> message) {
        SqsManager sqsManager = this.instances.computeIfAbsent(destinationName, k -> {
            SqsManager newInstance = new SqsManager(new SqsInstance(), false);
            newInstance.start();
            return newInstance;
        });
        sqsManager.send(message);
    }

    @Override
    public void send(final QueueMessageChannel destination, final Message<?> message) {
        destination.send(message);
    }

    private <T> T convert(final Class<T> targetClass, final Message<?> receive) {
        if (receive != null) {
            final Object payload = receive.getPayload();
            if (targetClass.isAssignableFrom(payload.getClass())) {
                return (T) payload;
            } else {
                throw new MessagingException("Cannot convert " + payload.getClass() + " to " + targetClass.getClassLoader());
            }
        } else {
            return null;
        }
    }

}
