# message-broker

A message broker with Project Reactor and a plugin infrastructure.

> This is an unfinished version and still work in process.
> At the moment it is to be taken with a huge grain of salt.

# How to run it

You need to install a [Postgres](https://www.postgresql.org/) database and [RabbitMQ](https://www.rabbitmq.com/).
After that you configure the connections under

- app/agent-http/src/main/resources/application-default.yml # RabbitMq
- app/broker/src/main/resources/application-default.yml # Database
- app/broker/src/main/resources/application-default.yml # RabbitMq

Now you can build everything. In the root directory of the project run:

```
mvn clean verify
```

In the build process the procedure and task plugins are copied to .assets/plugins/...
The task plugins need a property file *example.yml*:

```yaml
example: "I am an example."
```

Create this property file in .assets/plugins/tasks

Now you can run the apps. The main classes are

- app/agent-http/src/main/java/de/saschaufer/message_broker/app/agent-http/Application.java
- app/broker/src/main/java/de/saschaufer/message_broker/app/broker/Application.java

To post a message visit the SwaggerUI under http://localhost:8002/swagger-ui

To view the results take a look at the logs or in the database. Logs are printed to SYSTEM_OUT.
