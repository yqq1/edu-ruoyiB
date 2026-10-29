package cn.iocoder.yudao.module.dorm.api.reference;

import lombok.Data;
import java.util.*;

public final class DormReferenceData {
    private DormReferenceData() {}
    @Data
    public static class Actor {
        private String type;
        private Long studentId;
        private Set<Long> classIds = new HashSet<>();
    }
    @Data
    public static class Student {
        private Long id;
        private String studentNo;
        private String name;
        private Long classId;
        private String className;
        private String gender;
        private boolean enrolled;
        private Long accountId;
    }
}
