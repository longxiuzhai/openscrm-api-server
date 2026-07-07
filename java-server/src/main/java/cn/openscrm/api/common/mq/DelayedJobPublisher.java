package cn.openscrm.api.common.mq;

import cn.openscrm.api.config.RabbitMqConfig;
import java.time.Duration;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DelayedJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public DelayedJobPublisher(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    public void publish(Object payload, Duration delay) {
        MessagePostProcessor ttlProcessor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delay.toMillis()));
            return message;
        };
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.OPEN_SCRM_EXCHANGE,
                RabbitMqConfig.DELAYED_JOB_WAIT_ROUTING_KEY,
                payload,
                ttlProcessor
        );
    }

    public boolean publishOnce(String dedupeKey, Object payload, Duration delay) {
        Duration ttl = delay.plus(Duration.ofMinutes(10));
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(dedupeKey, "1", ttl);
        if (!Boolean.TRUE.equals(locked)) {
            return false;
        }
        publish(payload, delay);
        return true;
    }
}
