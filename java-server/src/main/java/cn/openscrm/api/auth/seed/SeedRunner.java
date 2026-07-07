package cn.openscrm.api.auth.seed;

import cn.openscrm.api.config.OpenScrmProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedRunner implements ApplicationRunner {

    private final OpenScrmProperties properties;
    private final PermissionRoleSeedService seedService;

    public SeedRunner(OpenScrmProperties properties, PermissionRoleSeedService seedService) {
        this.properties = properties;
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (Boolean.TRUE.equals(properties.getSeed().getEnabled())) {
            seedService.seed();
        }
    }
}
