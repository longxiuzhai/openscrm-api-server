package cn.openscrm.api.common.id;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {

    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis() << 12);

    public Long nextId() {
        return sequence.incrementAndGet();
    }
}
