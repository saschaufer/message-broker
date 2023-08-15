package de.saschaufer.message_broker.app.broker.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.saschaufer.message_broker.app.broker.config.ApplicationProperties;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories
@EnableR2dbcAuditing
public class DatabaseConfig extends AbstractR2dbcConfiguration {
    final ObjectMapper objectMapper;
    final ApplicationProperties applicationProperties;

    public DatabaseConfig(final ObjectMapper objectMapper, final ApplicationProperties applicationProperties) {
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(applicationProperties.database().host())
                        .port(applicationProperties.database().port())
                        .database(applicationProperties.database().database())
                        .schema(applicationProperties.database().schema())
                        .username(applicationProperties.database().username())
                        .password(applicationProperties.database().password())
                        .codecRegistrar(EnumCodec.builder().withEnum("status", Message.Status.class).build())
                        .build()
        );
    }

    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
                new MapToJsonConverter(objectMapper),
                new JsonToMapConverter(objectMapper),
                new StatusConverter()
        );
    }

    @Bean
    ReactiveTransactionManager transactionManager(final ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public ConnectionFactoryInitializer initializer(final ConnectionFactory connectionFactory) {

        final Resource sql = new ByteArrayResource("""
                create schema if not exists "%s";
                                
                drop table if exists "%s".%s;
                                
                drop type if exists status;
                create type status as enum ('%s', '%s', '%s', '%s');
                                
                create table if not exists "%s".%s (
                    id serial primary key,
                    correlation_id varchar(27) not null unique,
                    status status not null,
                    procedure varchar(50) not null,
                    previous_step varchar(50),
                    next_step varchar(50),
                    payload json not null default '{}'::json,
                    error varchar,
                    reception_time timestamp not null,
                    last_changed_time timestamp not null
                );
                """
                .formatted(
                        applicationProperties.database().schema(),
                        applicationProperties.database().schema(),
                        applicationProperties.database().table(),
                        Message.Status.waiting_for_processing,
                        Message.Status.in_process,
                        Message.Status.finished,
                        Message.Status.finished_with_error,
                        applicationProperties.database().schema(),
                        applicationProperties.database().table()
                )
                .getBytes()
        );

        final ResourceDatabasePopulator resource = new ResourceDatabasePopulator();
        resource.addScript(sql);

        final ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(resource);

        return initializer;
    }
}
