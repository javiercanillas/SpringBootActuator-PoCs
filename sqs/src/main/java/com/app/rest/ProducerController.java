package com.app.rest;

import com.app.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.SqsMessageHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.Random;


@RestController
@RequestMapping("/producer")
public class ProducerController {
    private static final Logger LOG = LoggerFactory.getLogger(ProducerController.class);
    private final Random random;
    private final QueueMessagingTemplate queueMessagingTemplate;
    private final String topicName;

    @Autowired
    public ProducerController(@Value("${sqsName}") String topicName, QueueMessagingTemplate queueMessagingTemplate) {
        this.queueMessagingTemplate = queueMessagingTemplate;
        this.topicName = topicName;
        this.random = new Random();
    }

    /**
     * Example: curl -X POST "localhost:8080/producer/now"
     * @return
     */
    @PostMapping(name = "produce a random message now", path = "now", produces = MediaType.APPLICATION_JSON_VALUE)
    public Message randomMessageNow() {
        Message msg = Message.of(generateRandomString(), new Date());
        queue(msg);
        return msg;
    }

    /**
     * Example: curl -X POST "localhost:8080/producer/now" -d'{ "content": "test", "sentDate": "2020-11-20T20:48:00" }' -H'Content-Type:application/json'
     * @param message
     * @return
     */
    @PostMapping(name = "produce the posted message now", path = "now", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Message messageNow(@RequestBody Message message) {
        queue(message);
        return message;
    }

    /**
     * Example: curl -X POST "localhost:8080/producer/deplayed"
     * @return
     */
    @PostMapping(name = "produce a random message with a random delay", path = "delayed",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Message randomMessageDelayed() {
        Message msg = Message.of("delayed " + generateRandomString(), new Date());
        queue(msg, 5 + this.random.nextInt(10));
        return msg;
    }

    /**
     * Example: curl -X POST "localhost:8080/producer/delay/10" -d'{ "content": "delayed text", "sentDate": "2020-11-20T20:48:00" }' -H'Content-Type:application/json'
     * @param message
     * @return
     */
    @PostMapping(name = "produce the posted message with the given delay", path = "delay/{delay}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Message messageDelayed(@RequestBody Message message, @PathVariable("delay") int delay) {
        queue(message, delay);
        return message;
    }

    private void queue(Message msg) {
        queue(msg, 0);
    }

    private void queue(Message msg, int delayInSeconds) {
        Map<String, Object> headers = null;
        if (delayInSeconds > 0) {
            LOG.info("Delaying message consumption by {} secs.", delayInSeconds);
            headers = Map.of(SqsMessageHeaders.SQS_DELAY_HEADER, delayInSeconds);
        }
        this.queueMessagingTemplate.convertAndSend(topicName, msg, headers);
        LOG.info("Produced: {}", msg);
    }

    private String generateRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}