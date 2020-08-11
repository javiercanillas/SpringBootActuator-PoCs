# PoC Application for Spring Actuator Logging capabilities
Application with Spring Boot + Spring Actuator to test changing log configuration dynamically.

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

## How to request log configuration
This will request the full logging configuration to the application
```bash
curl http://localhost:8080/actuator/loggers/
```
Example (unfortunatelly this is quite extensive, I didn't copy it all):
```json
{
  "levels": [
    "OFF",
    "ERROR",
    "WARN",
    "INFO",
    "DEBUG",
    "TRACE"
  ],
  "loggers": {
    "ROOT": {
      "configuredLevel": "INFO",
      "effectiveLevel": "INFO"
    },
    "com": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    },
    "com.app": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    },
    "com.app.Application": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    },
    "com.app.rest": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    },
    "com.app.rest.LoggingController": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    },
    ...
    "org.springframework.web.util.UrlPathHelper": {
      "configuredLevel": null,
      "effectiveLevel": "INFO"
    }
  },
  "groups": {
    "web": {
      "configuredLevel": null,
      "members": [
        "org.springframework.core.codec",
        "org.springframework.http",
        "org.springframework.web",
        "org.springframework.boot.actuate.endpoint.web",
        "org.springframework.boot.web.servlet.ServletContextInitializerBeans"
      ]
    },
    "sql": {
      "configuredLevel": null,
      "members": [
        "org.springframework.jdbc.core",
        "org.hibernate.SQL",
        "org.jooq.tools.LoggerListener"
      ]
    }
  }
}
```

If you want to see the ROOT log configuration:
```bash
curl http://localhost:8080/actuator/loggers/ROOT
```
Output:
```json
{
  "configuredLevel": "INFO",
  "effectiveLevel": "INFO"
}
```

If you want to see the log configuration of particular class:
```bash
curl http://localhost:8080/actuator/loggers/com.app.rest.LoggingController
```
Output:
```json
{
  "configuredLevel": null,
  "effectiveLevel": "INFO"
}
```

## How to change a particular level of a package or class
Lets suppose we want to change the log level of our class `com.app.rest.LoggingController`. The curl should be:
```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://localhost:8080/actuator/loggers/com.app.rest.LoggingController
```
The output should be:
```bash
HTTP/1.1 204
Date: Tue, 11 Aug 2020 13:28:52 GMT
```
If you want to check the configuration again:
```bash
curl http://localhost:8080/actuator/loggers/com.app.rest.LoggingController
```
Output:
```json
{
  "configuredLevel": null,
  "effectiveLevel": "TRACE"
}
```
Now, if we call the endpoint again, we will see a new entry on the ouput for the trace event:
```bash
2020-08-11 10:31:16.841 TRACE 5693 --- [nio-8080-exec-8] com.app.rest.LoggingController           : This is a TRACE level message
2020-08-11 10:31:16.841 DEBUG 5693 --- [nio-8080-exec-8] com.app.rest.LoggingController           : This is a DEBUG level message
2020-08-11 10:31:16.842  INFO 5693 --- [nio-8080-exec-8] com.app.rest.LoggingController           : This is an INFO level message
2020-08-11 10:31:16.842  WARN 5693 --- [nio-8080-exec-8] com.app.rest.LoggingController           : This is a WARN level message
2020-08-11 10:31:16.842 ERROR 5693 --- [nio-8080-exec-8] com.app.rest.LoggingController           : This is an ERROR level message
```

More about Spring Boot Actuator [here](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-enabling) 