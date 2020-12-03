package com.app.sqs.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiPredicate;

public class SqsListenerBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsListenerBeanPostProcessor.class);

    private final LocalQueueMessagingTemplate localQueueMessagingTemplate;

    public SqsListenerBeanPostProcessor(final LocalQueueMessagingTemplate localQueueMessagingTemplate) {
        this.localQueueMessagingTemplate = localQueueMessagingTemplate;
    }

    @Override
    public void postProcessBeforeDestruction(final Object bean, final String beanName) {
        this.process(bean, this.localQueueMessagingTemplate::unregister);
    }

    @Override
    public boolean requiresDestruction(final Object bean) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) {
        this.process(bean, this.localQueueMessagingTemplate::register);
        return bean;
    }

    private void process(final Object bean, final BiPredicate<Object, Method> applyFunction) {
        Object proxy = this.getTargetObject(bean);
        if (proxy == null) {
            throw new FatalBeanException("Couldn't obtain real proxy");
        }
        final Class<?> proxyClass = proxy.getClass();
        Arrays.stream(proxyClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(SqsListener.class))
                .forEach(method -> {
                    if (!Boolean.TRUE.equals(applyFunction.test(bean, method))) {
                        LOGGER.error("Couldn't apply method {} of bean {}", method, bean);
                    }
                });
    }

    private Object getTargetObject(final Object proxy) {
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            try {
                return ((Advised) proxy).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new FatalBeanException("Error getting target of JDK proxy", e);
            }
        }
        return proxy;
    }
}
