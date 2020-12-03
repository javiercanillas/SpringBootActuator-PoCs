package com.app.sqs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.app.sqs.simulation.LocalQueueMessagingTemplate;
import com.app.sqs.simulation.SqsListenerBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(value = "queue.aws.sqs.enabled", havingValue = "false", matchIfMissing = true)
@Configuration(value = "Mocked SQS spring configuration")
public class SimulatedAppConfiguration {

    @Bean
    public LocalQueueMessagingTemplate queueMessagingTemplate() {
        return new LocalQueueMessagingTemplate(AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(Regions.DEFAULT_REGION)
                .build());
    }

    @Bean
    public SqsListenerBeanPostProcessor sqsListenerBeanPostProcessor(final LocalQueueMessagingTemplate queueMessagingTemplate) {
        return new SqsListenerBeanPostProcessor(queueMessagingTemplate);
    }
}