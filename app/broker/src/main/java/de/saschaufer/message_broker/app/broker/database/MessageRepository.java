package de.saschaufer.message_broker.app.broker.database;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {
    Flux<Message> findTop10ByStatusOrderByReceptionTimeAsc(final String status);
}
