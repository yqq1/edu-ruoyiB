package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormReferenceServiceImpl implements DormReferenceService {
    @Resource private DormStore store;
    @Resource private DormAccess access;
    @Resource private DormReferences references;
    public DormVO.ReferenceResp options() {
        DormAccess.Scope scope = access.scope("dorm:reference:query");
        Map<Long, String> classes = references.gateway().classes(scope.classes());
        require(classes != null, "班级公共数据不可用");
        DormVO.ReferenceResp result = new DormVO.ReferenceResp(); result.setMock(references.gateway().isMock());
        result.setActorType(scope.type()); result.setStudentId(scope.studentId());
        Map<Long, String> allowed = new LinkedHashMap<>();
        classes.forEach((id, name) -> { if (scope.isAdmin() || scope.classes().contains(id)) allowed.put(id, name); });
        result.setClasses(allowed); return result;
    }
    public PageResult<StudentResp> students(DormQuery req) {
        store.valid(req); DormRules.page(req); DormAccess.Scope scope = access.scope("dorm:reference:query");
        if ("STUDENT".equals(scope.type())) {
            Student s = references.student(scope.studentId());
            return new PageResult<>(req.getPageNo() == 1 ? Collections.singletonList(BeanUtils.toBean(s, StudentResp.class))
                    : Collections.emptyList(), 1L);
        }
        Set<Long> classes = scope.classes();
        if (req.getClassId() != null) {
            require(scope.isAdmin() || classes.contains(req.getClassId()), "班级不在本人范围");
            classes = Collections.singleton(req.getClassId());
        }
        PageResult<Student> page = references.gateway().studentPage(classes, req.getKeyword(), req.getPageNo(), req.getPageSize());
        require(page != null && page.getList() != null && page.getTotal() != null && page.getTotal() >= 0
                && page.getList().size() <= req.getPageSize(), "学生分页公共数据无效");
        page.getList().forEach(s -> scope.read(s.getId(), s.getClassId()));
        return new PageResult<>(BeanUtils.toBean(page.getList(), StudentResp.class), page.getTotal());
    }
}
