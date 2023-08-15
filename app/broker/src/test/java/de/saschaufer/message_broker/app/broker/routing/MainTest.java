package de.saschaufer.message_broker.app.broker.routing;

import de.saschaufer.message_broker.app.broker.database.Message;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
class MainTest {

    @Test
    void test() {

        Flux.range(1, 2)
                .map(i -> {
                    System.err.println("before " + i);
                    return i;
                })
                .flatMap(i -> Mono.just(i)
                        .expand(in -> {

                            System.err.println("run    " + i + "/" + in);

                            if (in > 5) {
                                return Mono.empty();
                            }

                            return Mono.just(in + 1);
                        })
                        .last()
                )
                .map(i -> {
                    System.err.println("after  " + i);
                    return i;
                })
                .subscribe()
        ;

/*
        final Flux<Message> flux = Flux.just(new Message().correlationId(Ksuid.generate()))
                .doOnNext(message -> System.err.println("start : " + message.previousStep()))
                .flatMap(message -> {

                    if (message.previousStep() == null) {

                    }

                    Mono<Message> mono = Mono.just(message);
                    for (int j = 0; j < 5; j++) {
                        mono = step(mono);
                    }
                    return mono;
                })
                .doOnNext(message -> System.err.println("stop  : " + message.previousStep()));

        flux.subscribe(System.err::println);
*/
        //StepVerifier s = StepVerifier.create(flux)
        //        .expectNextCount(4)
        //        .expectNext()
        //        .expectComplete()
        //        .verifyLater();

        //StepVerifier.create(flux)// withVirtualTime(() -> flux)
        //        .expectSubscription()
        //        .thenAwait(Duration.ofSeconds(10))
        //        .expectNextCount(10)
        //        .expectComplete()
        //        .verify();
        //;
    }

    String getNextStep(final Message message) {
        return null;
    }

    Mono<Message> step(final Mono<Message> mono) {

        return mono
                .doOnNext(message -> System.err.println("before: " + message.previousStep()))
                //.delayElement(Duration.ofMillis(500))
                .map(message -> {

                    if (message.previousStep() == null) {
                        return message.previousStep("step-1");
                    }

                    return switch (message.previousStep()) {
                        case "step-1" -> message.previousStep("step-2");
                        case "step-2" -> message.previousStep("step-3");
                        case "step-3" -> message.previousStep("step-4");
                        case "step-4" -> message.previousStep("step-5");
                        default -> throw new RuntimeException("Unknown step.");
                    };
                })
                .doOnNext(message -> System.err.println("after : " + message.previousStep()))
                //.repeatWhen(Repeat.onlyIf(o -> o.companionValue() < 5))
                //.repeatWhen(Repeat.onlyIf(o -> o.companionValue() < 5))//.doOnRepeat(c -> c.companionValue()))
                //.last();
                ;

    }
}
