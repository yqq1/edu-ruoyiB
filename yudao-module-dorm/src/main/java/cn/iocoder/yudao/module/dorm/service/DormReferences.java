package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.module.dorm.api.reference.*;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.*;

@Component
public class DormReferences {
    @Resource private ObjectProvider<DormReferenceGateway> gateways;
    public DormReferenceGateway gateway() {
        DormReferenceGateway gateway = gateways.getIfAvailable();
        require(gateway != null, DORM_REFERENCE_UNAVAILABLE, "宿舍公共数据尚未接入，禁止使用模拟ID兜底");
        return gateway;
    }
    public Map<Long, Student> students(Collection<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        Set<Long> distinct = new LinkedHashSet<>(ids);
        Map<Long, Student> result = gateway().students(distinct);
        require(result != null, DORM_REFERENCE_UNAVAILABLE, "学生公共数据不可用");
        for (Long id : distinct) {
            Student s = result.get(id);
            require(s != null && id.equals(s.getId()) && s.getClassId() != null && s.getClassId() > 0
                    && s.getStudentNo() != null && s.getName() != null && s.getClassName() != null
                    && Arrays.asList("MALE", "FEMALE", "UNKNOWN").contains(s.getGender()),
                    DORM_REFERENCE_UNAVAILABLE, "学生公共信息不完整或不存在");
        }
        return result;
    }
    public Student student(Long id) {
        DormRules.id(id);
        return students(Collections.singleton(id)).get(id);
    }
}
