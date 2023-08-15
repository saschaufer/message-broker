package de.saschaufer.message_broker.app.broker.routing;

import de.saschaufer.message_broker.app.broker.database.Message;
import de.saschaufer.message_broker.app.broker.database.MessageRepository;
import de.saschaufer.message_broker.app.broker.plugins.PluginManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static de.saschaufer.message_broker.plugin.spi.Constants.Logging.CORRELATION_ID;
import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
@Component
public class Router {
    private final MessageRepository messageRepository;
    private final RabbitMq rabbitMq;
    private final Function<Message, Mono<Message>> getNextStep;
    private final Function<Message, Mono<Message>> runStep;
    private final Function<Throwable, Mono<Message>> saveWithError;
    private final List<Disposable> disposables = new ArrayList<>();

    public Router(final MessageRepository messageRepository, final PluginManager pluginManager, final RabbitMq rabbitMq) {
        this.messageRepository = messageRepository;
        this.rabbitMq = rabbitMq;

        getNextStep = Processor.getNextStep(pluginManager);
        runStep = Processor.runStep(pluginManager);
        saveWithError = Processor.saveWithError(messageRepository);
    }

    public void start() {

        log.atInfo().setMessage("Starting routes.").log();

        disposables.add(run(getConsumers()).subscribe());

        log.atInfo().setMessage("Routes started.").log();
    }

    @PreDestroy
    public void destroy() {

        log.atInfo().setMessage("Destroying routes.").log();

        for (final Disposable disposable : disposables) {
            disposable.dispose();
        }

        log.atInfo().setMessage("Routes destroyed.").log();
    }

    private Flux<Message> getConsumers() {

        // Get messages from database.
        final Flux<Message> database = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(1))
                .flatMap(i -> messageRepository.findTop10ByStatusOrderByReceptionTimeAsc(Message.Status.waiting_for_processing.name()));

        // Get messages from RabbitMQ.
        final Flux<Message> rabbitmq = rabbitMq.receive()
                .map(message -> new Message()
                        .correlationId(message.correlationId())
                        .procedure(message.procedure())
                        .payload(message.payload())
                        .receptionTime(message.receptionTime())
                );

        // Merge all consumers.
        return Flux.merge(database, rabbitmq)
                // Save new messages or update existing messages.
                .map(message -> message.status(Message.Status.in_process).lastChangedTime(LocalDateTime.now()))
                .flatMap(messageRepository::save)
                ;
    }

    private Flux<Message> run(final Flux<Message> consumers) {

        return Flux.merge(consumers)

                // Iterate steps
                .flatMap(getNextStep)

                .flatMap(_message -> Mono.just(_message)

                        .expand(__message -> __message.nextStep() == null ? Mono.empty() : Mono.just(__message)

                                // Run step
                                .flatMap(___message -> Mono.just(___message)
                                        .doOnNext(message -> log.atInfo().setMessage("Run next step.").addKeyValue("procedure", message.procedure()).addKeyValue("step", message.nextStep()).addKeyValue(CORRELATION_ID, message.correlationId()).log())
                                        .flatMap(runStep)
                                        .map(message -> message.previousStep(message.nextStep()).nextStep(null))
                                        .flatMap(getNextStep)
                                )
                                .doOnNext(message -> log.atInfo().setMessage("Save message.").addKeyValue(CORRELATION_ID, message.correlationId()).addKeyValue("payload", json(message.payload())::get).log())
                                .map(message -> message.status(Message.Status.finished).lastChangedTime(LocalDateTime.now()))
                                .flatMap(messageRepository::save)
                        )
                        .last()
                )
                .onErrorResume(saveWithError)
                .doOnNext(message -> log.atInfo().setMessage("Finished.").addKeyValue(CORRELATION_ID, message.correlationId()).log())
                ;
    }
}
