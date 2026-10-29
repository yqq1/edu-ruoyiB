package cn.iocoder.yudao.server.dorm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.dorm.api.reference.*;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.*;
import cn.iocoder.yudao.server.dorm.DormMockProperties.TenantData;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

final class DormMockGateway implements DormReferenceGateway {
    private final DormMockProperties properties;
    DormMockGateway(DormMockProperties properties) { this.properties = properties; }
    private TenantData data() {
        TenantData data = properties.getTenants().get(TenantContextHolder.getRequiredTenantId());
        require(data != null, "当前租户未显式配置宿舍模拟数据");
        return data;
    }
    public boolean isMock() { return true; }
    public Actor actor(Long accountId) {
        Actor configured = data().getActors().get(accountId);
        require(configured != null, "当前账号未显式绑定宿舍测试身份");
        Actor actor = BeanUtils.toBean(configured, Actor.class);
        if ("STUDENT".equals(actor.getType())) {
            Student student = student(actor.getStudentId());
            require(Objects.equals(accountId, student.getAccountId()), "学生测试账号须与通知账号显式一致绑定");
            actor.setClassIds(Collections.singleton(student.getClassId()));
        } else {
            require("COUNSELOR".equals(actor.getType()) && actor.getClassIds() != null
                    && data().getClasses().keySet().containsAll(actor.getClassIds()), "辅导员测试班级绑定无效");
        }
        return actor;
    }
    private Student student(Long id) {
        Student configured = data().getStudents().get(id);
        require(configured != null, "模拟学生不存在，请显式配置业务ID");
        Student student = BeanUtils.toBean(configured, Student.class);
        student.setId(id);
        student.setClassName(data().getClasses().get(student.getClassId()));
        require(student.getClassName() != null, "模拟学生班级未配置");
        return student;
    }
    public Map<Long, String> classes(Set<Long> allowed) {
        Map<Long, String> result = new LinkedHashMap<>();
        data().getClasses().forEach((id, name) -> { if (allowed == null || allowed.contains(id)) result.put(id, name); });
        return result;
    }
    public Map<Long, Student> students(Set<Long> ids) {
        Map<Long, Student> result = new LinkedHashMap<>();
        ids.forEach(id -> result.put(id, student(id)));
        return result;
    }
    public PageResult<Student> studentPage(Set<Long> allowed, String keyword, int pageNo, int pageSize) {
        require(pageNo > 0 && pageSize > 0 && pageSize <= 200, "分页参数无效");
        List<Student> all = data().getStudents().keySet().stream().sorted().map(this::student)
                .filter(s -> allowed == null || allowed.contains(s.getClassId()))
                .filter(s -> keyword == null || keyword.isEmpty() || (s.getName() != null && s.getName().contains(keyword))
                        || (s.getStudentNo() != null && s.getStudentNo().contains(keyword))).collect(Collectors.toList());
        long from = Math.min((long) (pageNo - 1) * pageSize, all.size());
        int to = (int) Math.min(from + pageSize, all.size());
        return new PageResult<>(all.subList((int) from, to), (long) all.size());
    }
    public Map<Long, Set<Long>> counselorAccounts(Set<Long> classes) {
        Map<Long, Set<Long>> result = new LinkedHashMap<>();
        for (Long id : classes) {
            Set<Long> accounts = data().getCounselorAccounts().get(id);
            require(accounts != null && !accounts.isEmpty(), "每个班级必须显式配置现有辅导员通知账号");
            result.put(id, new LinkedHashSet<>(accounts));
        }
        return result;
    }
    public boolean disciplineExists(Long studentId, Long disciplineId) {
        return data().getDisciplinesByStudent().getOrDefault(studentId, Collections.emptySet()).contains(disciplineId);
    }
}
