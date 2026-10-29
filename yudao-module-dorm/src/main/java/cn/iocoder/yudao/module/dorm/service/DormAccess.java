package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.*;

@Component
public class DormAccess {
    @Resource private PermissionApi permissions;
    @Resource private DormReferences references;
    public Long accountId() {
        TenantContextHolder.getRequiredTenantId();
        Long id = SecurityFrameworkUtils.getLoginUserId();
        require(id != null, DORM_FORBIDDEN, "请先登录");
        return id;
    }
    public void permission(String value) {
        require(permissions.hasAnyPermissions(accountId(), value), DORM_FORBIDDEN, "没有该宿舍操作权限");
    }
    public boolean admin() {
        return permissions.hasAnyRoles(accountId(), "super_admin")
                || permissions.hasAnyPermissions(accountId(), "dorm:admin");
    }
    public Scope scope(String permission) {
        permission(permission);
        if (admin()) return new Scope(true, null);
        Actor actor = references.gateway().actor(accountId());
        require(actor != null && actor.getClassIds() != null
                && Arrays.asList("STUDENT", "COUNSELOR").contains(actor.getType()),
                DORM_FORBIDDEN, "账号未绑定学生或辅导员身份");
        actor.getClassIds().forEach(DormRules::id);
        if ("STUDENT".equals(actor.getType())) DormRules.id(actor.getStudentId());
        return new Scope(false, actor);
    }
    public Scope staff(String permission) {
        Scope scope = scope(permission);
        require(scope.admin || "COUNSELOR".equals(scope.actor.getType()), DORM_FORBIDDEN, "仅管理员或辅导员可操作");
        return scope;
    }
    public void administrator(String permission) {
        permission(permission);
        require(admin(), DORM_FORBIDDEN, "仅管理员可维护宿舍资源");
    }
    public Student student(String permission) {
        Scope scope = scope(permission);
        require(!scope.admin && "STUDENT".equals(scope.actor.getType()), DORM_FORBIDDEN, "仅学生本人可操作");
        return references.student(scope.actor.getStudentId());
    }
    public static final class Scope {
        private final boolean admin;
        private final Actor actor;
        private Scope(boolean admin, Actor actor) { this.admin = admin; this.actor = actor; }
        public boolean isAdmin() { return admin; }
        public String type() { return admin ? "ADMIN" : actor.getType(); }
        public Long studentId() { return admin ? null : actor.getStudentId(); }
        public Set<Long> classes() { return admin ? null : actor.getClassIds(); }
        public void read(Long studentId, Long classId) {
            require(admin || ("COUNSELOR".equals(type()) && classes().contains(classId))
                    || ("STUDENT".equals(type()) && Objects.equals(studentId(), studentId)),
                    DORM_FORBIDDEN, "记录不存在或不在可访问范围");
        }
        public void maintain(Student student) {
            require(admin || ("COUNSELOR".equals(type()) && classes().contains(student.getClassId())),
                    DORM_FORBIDDEN, "仅可管理本人所带班级学生");
        }
        public <T> void apply(QueryWrapper<T> query) {
            if (admin) return;
            if ("STUDENT".equals(type())) query.eq("student_id", studentId());
            else if (classes().isEmpty()) query.eq("id", -1L);
            else query.in("class_id", classes());
        }
        public <T> void rooms(QueryWrapper<T> query, String column) {
            if (admin) return;
            require("COUNSELOR".equals(type()), DORM_FORBIDDEN, "学生不可查看房间检查数据");
            String ids = classes().stream().map(String::valueOf).collect(Collectors.joining(","));
            if (ids.isEmpty()) { query.eq("id", -1L); return; }
            query.inSql(column, "SELECT room_id FROM dorm_stay WHERE tenant_id=" + DormStore.tenant()
                    + " AND deleted=0 AND status='OCCUPIED' AND class_id IN (" + ids + ")");
        }
    }
}
