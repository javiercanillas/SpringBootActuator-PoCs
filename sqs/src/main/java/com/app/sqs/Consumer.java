package com.app.sqs;

import com.app.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

    @SqsListener("${sqsName}")
    public void receiveMessage(@Payload Message message, @Headers Map<String,Object> headers) {
        LOG.info("Consumed: {} with headers {}", message, headers);
    }
}
