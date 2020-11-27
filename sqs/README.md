# PoC Application for Spring Cloud AWS Message (SQS)
Application with Spring Boot + Spring Cloud to test queue and consuming SQS messages

## Prerequisites
* Java11 virtual machine installed
* Maven from command line installed
* AmazonWS account and credentials to access it
* A not FIFO SQS resource created and reachable by the previous credentials.

## How to startup
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--sqsName={YOUR QUEUE URL}
```
**Note:** You required to have you environment configured with your AWS credentials.

## How to test
The following commands from your terminal will help you out test functionality. Check `ProducerController` for code.

```bash
curl -X POST 'localhost:8080/producer/now'
curl -X POST 'localhost:8080/producer/now' -d'{ "content": "test", "sentDate": "2020-11-20T20:48:00" }' -H'Content-Type:application/json'
curl -X POST 'localhost:8080/producer/deplayed'
curl -X POST 'localhost:8080/producer/delay/10' -d'{ "content": "delayed text", "sentDate": "2020-11-20T20:48:00" }' -H'Content-Type:application/json'
```

It will log something like the following since the root log level is INFO:
```text
2020-11-27 13:11:14.198  INFO 29927 --- [nio-8080-exec-2] com.app.rest.ProducerController          : Produced: Message{content='FxdSLUXyBE', sentDate=Fri Nov 27 13:11:13 ART 2020}
2020-11-27 13:11:14.228  INFO 29927 --- [enerContainer-2] com.app.sqs.Consumer                     : Consumed: Message{content='FxdSLUXyBE', sentDate=Fri Nov 27 13:11:13 ART 2020}
...
2020-11-27 13:11:29.189  INFO 29927 --- [nio-8080-exec-5] com.app.rest.ProducerController          : Delaying message consumption by 10 secs.
2020-11-27 13:11:29.587  INFO 29927 --- [nio-8080-exec-6] com.app.rest.ProducerController          : Produced: Message{content='I8K6bFtRmV', sentDate=Fri Nov 27 13:11:29 ART 2020}
2020-11-27 13:11:29.610  INFO 29927 --- [enerContainer-2] com.app.sqs.Consumer                     : Consumed: Message{content='I8K6bFtRmV', sentDate=Fri Nov 27 13:11:29 ART 2020}
...
```

More about Spring Boot Actuator [here](https://spring.io/projects/spring-cloud-aws) 