package de.saschaufer.message_broker.app.broker.routing;

import de.saschaufer.message_broker.app.broker.database.Message;
import de.saschaufer.message_broker.app.broker.database.MessageRepository;
import de.saschaufer.message_broker.app.broker.plugins.PluginManager;
import de.saschaufer.message_broker.plugin.spi.procedure.Procedure;
import de.saschaufer.message_broker.plugin.spi.task.Task;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

import static de.saschaufer.message_broker.plugin.spi.Constants.Logging.CORRELATION_ID;
import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
public class Processor {
    public static Function<Message, Mono<Message>> getNextStep(final PluginManager pluginManager) {
        return message -> {

            final Procedure procedure = pluginManager.getProcedures().get(message.procedure());

            if (procedure == null) {
                return Mono.error(new RuntimeExceptionWithMessage("Unknown procedure.", message));
            }

            try {
                return Mono.just(message.nextStep(procedure.getNextStep(
                        message.correlationId(),
                        message.previousStep(),
                        message.nextStep(),
                        message.payload()
                )));
            } catch (final Exception e) {
                return Mono.error(new RuntimeExceptionWithMessage("Error finding next step.", e, message));
            }
        };
    }

    public static Function<Message, Mono<Message>> runStep(final PluginManager pluginManager) {
        return message -> {

            final Procedure procedure = pluginManager.getProcedures().get(message.procedure());

            // Map input
            final Map<String, Object> input;
            try {
                input = procedure.mapInput(
                        message.correlationId(),
                        message.nextStep(),
                        message.previousStep(),
                        message.payload()
                );
            } catch (final Exception e) {
                log.atError().setMessage("Error mapping input.").setCause(e).log();
                return Mono.error(new RuntimeExceptionWithMessage("Error mapping input.", e, message));
            }

            // Run task
            final String taskId = procedure.getTaskPluginInfo(message.nextStep()).id();
            final Task task = pluginManager.getTasks().get(taskId);

            final Map<String, Object> output;
            try {
                output = task.run(message.correlationId(), input);
            } catch (final Exception e) {
                log.atError().setMessage("Error running task.").addKeyValue("task", taskId).setCause(e).log();
                return Mono.error(new RuntimeExceptionWithMessage("Error running task.", e, message));
            }

            // Map output
            try {
                return Mono.just(message.payload(procedure.mapOutput(
                        message.correlationId(),
                        message.nextStep(),
                        message.previousStep(),
                        output
                )));
            } catch (final Exception e) {
                log.atError().setMessage("Error mapping output.").setCause(e).log();
                return Mono.error(new RuntimeExceptionWithMessage("Error mapping output.", e, message));
            }
        };
    }

    public static Function<Throwable, Mono<Message>> saveWithError(final MessageRepository messageRepository) {
        return throwable -> {

            log.atError().setMessage("Handling error.").setCause(throwable).log();

            if (throwable instanceof RuntimeExceptionWithMessage e) {

                final Message message = e.getRoutedMessage();

                log.atInfo().setMessage("Save message.").addKeyValue(CORRELATION_ID, message.correlationId()).addKeyValue("payload", json(message.payload())::get).log();

                return messageRepository.save(message
                        .error(stacktraceToString(e))
                        .status(Message.Status.finished_with_error)
                        .lastChangedTime(LocalDateTime.now())
                );
            }

            return Mono.error(new RuntimeException("Unexpected exception."));
        };
    }

    private static String stacktraceToString(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
