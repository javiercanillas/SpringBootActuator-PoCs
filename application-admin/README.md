# PoC Application for Spring Actuator Logging capabilities + Spring Admin
Application with Spring Boot + Spring Actuator to test changing log configuration dynamically.

## Requirements
In order to run this application, you should run first the admin application, since this will attempt to register itself on it.

## How to startup
```bash
mvn spring-boot:run
```

## How to test log
Run the following command in a terminal:
```bash
curl localhost:8080/log
```

It will log something like the following since the root log level is INFO:
```text
2020-08-11 10:12:43.398  INFO 5693 --- [nio-8080-exec-1] com.app.rest.LoggingController           : This is an INFO level message
2020-08-11 10:12:43.398  WARN 5693 --- [nio-8080-exec-1] com.app.rest.LoggingController           : This is a WARN level message
2020-08-11 10:12:43.398 ERROR 5693 --- [nio-8080-exec-1] com.app.rest.LoggingController           : This is an ERROR level message
```

## How to modify the logging configuration using Spring Admin
Since you have started the admin demo application within this repository, open a browser and navigate to: `http://localhost:8081/`.

You will see the demo application if everything went well. Now click on it and all instances running such application will show up. Select the instance and you will see the details of the instance.

On the left side, you will see a logger section, click on it.

On this page you will see all the logs, it's advise to check the option `configured` to only show what is explicity configured.

Now you can play around with the `ROOT` configuration. Change it to `ERROR` and then call the exaple service again:
```bash
curl localhost:8080/log
```

The expected output should be:
```text
2020-08-11 11:38:17.970 ERROR 7015 --- [nio-8080-exec-6] com.app.rest.LoggingController           : This is an ERROR level message
```

Moreover, you can add or remove entries on your application (only for the selected instance). For example add `com.app.rest.LoggingController` with level `TRACE` and reattempt the call:
```bash
curl localhost:8080/log
```

The expected output should be:
```text
2020-08-11 11:40:10.561 TRACE 7015 --- [nio-8080-exec-4] com.app.rest.LoggingController           : This is a TRACE level message
2020-08-11 11:40:10.561 DEBUG 7015 --- [nio-8080-exec-4] com.app.rest.LoggingController           : This is a DEBUG level message
2020-08-11 11:40:10.561  INFO 7015 --- [nio-8080-exec-4] com.app.rest.LoggingController           : This is an INFO level message
2020-08-11 11:40:10.561  WARN 7015 --- [nio-8080-exec-4] com.app.rest.LoggingController           : This is a WARN level message
2020-08-11 11:40:10.561 ERROR 7015 --- [nio-8080-exec-4] com.app.rest.LoggingController           : This is an ERROR level message
``` 

More about Spring Boot Actuator [here](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-enabling) 