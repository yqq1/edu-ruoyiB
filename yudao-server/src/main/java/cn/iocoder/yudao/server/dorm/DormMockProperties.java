package cn.iocoder.yudao.server.dorm;

import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;

@Data
@ConfigurationProperties(prefix = "dorm.mock")
public class DormMockProperties {
    private boolean enabled;
    private Map<Long, TenantData> tenants = new HashMap<>();
    @Data
    public static class TenantData {
        private Map<Long, String> classes = new LinkedHashMap<>();
        private Map<Long, Student> students = new LinkedHashMap<>();
        private Map<Long, Actor> actors = new LinkedHashMap<>();
        private Map<Long, Set<Long>> counselorAccounts = new HashMap<>();
        private Map<Long, Set<Long>> disciplinesByStudent = new HashMap<>();
    }
}
