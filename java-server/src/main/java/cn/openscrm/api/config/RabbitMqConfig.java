package cn.openscrm.api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String OPEN_SCRM_EXCHANGE = "openscrm.exchange";
    public static final String OPEN_SCRM_DEAD_LETTER_EXCHANGE = "openscrm.dead-letter.exchange";
    public static final String DELAYED_JOB_WAIT_QUEUE = "openscrm.delayed-job.wait.queue";
    public static final String DELAYED_JOB_QUEUE = "openscrm.delayed-job.ready.queue";
    public static final String DELAYED_JOB_ROUTING_KEY = "openscrm.delayed-job";
    public static final String DELAYED_JOB_WAIT_ROUTING_KEY = "openscrm.delayed-job.wait";
    public static final String DELAYED_JOB_DEAD_LETTER_ROUTING_KEY = "openscrm.delayed-job.ready";

    @Bean
    public DirectExchange openScrmExchange() {
        return new DirectExchange(OPEN_SCRM_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange openScrmDeadLetterExchange() {
        return new DirectExchange(OPEN_SCRM_DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue delayedJobWaitQueue() {
        return QueueBuilder.durable(DELAYED_JOB_WAIT_QUEUE)
                .deadLetterExchange(OPEN_SCRM_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DELAYED_JOB_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue delayedJobQueue() {
        return QueueBuilder.durable(DELAYED_JOB_QUEUE).build();
    }

    @Bean
    public Binding delayedJobWaitBinding(DirectExchange openScrmExchange, Queue delayedJobWaitQueue) {
        return BindingBuilder.bind(delayedJobWaitQueue)
                .to(openScrmExchange)
                .with(DELAYED_JOB_WAIT_ROUTING_KEY);
    }

    @Bean
    public Binding delayedJobBinding(DirectExchange openScrmDeadLetterExchange, Queue delayedJobQueue) {
        return BindingBuilder.bind(delayedJobQueue)
                .to(openScrmDeadLetterExchange)
                .with(DELAYED_JOB_DEAD_LETTER_ROUTING_KEY);
    }
}
