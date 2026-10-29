package cn.iocoder.yudao.server.dorm;

import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceGateway;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Profile("local & dorm-mock & !prod & !production")
@ConditionalOnProperty(prefix = "dorm.mock", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DormMockProperties.class)
public class DormMockConfiguration {
    @Bean
    @ConditionalOnMissingBean(DormReferenceGateway.class)
    public DormReferenceGateway dormMockGateway(DormMockProperties properties) {
        return new DormMockGateway(properties);
    }
}
