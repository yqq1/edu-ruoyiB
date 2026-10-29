package cn.iocoder.yudao.module.dorm.api.reference;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.*;
import java.util.*;

public interface DormReferenceGateway {
    boolean isMock();
    Actor actor(Long accountId);
    Map<Long, String> classes(Set<Long> allowedClassIds);
    Map<Long, Student> students(Set<Long> studentIds);
    PageResult<Student> studentPage(Set<Long> allowedClassIds, String keyword, int pageNo, int pageSize);
    Map<Long, Set<Long>> counselorAccounts(Set<Long> classIds);
    boolean disciplineExists(Long studentId, Long disciplineId);
}
