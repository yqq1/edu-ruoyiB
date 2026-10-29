package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.ViolationResp;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.*;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormViolationServiceImpl implements DormViolationService {
    @Resource private DormStore store;
    @Resource private DormAccess access;
    @Resource private DormGuard guard;
    @Resource private DormReferences references;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long save(ViolationSave req, boolean create) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:violation:update"); guard.lock();
        require(create == (req.getId() == null), "创建与修改参数不匹配");
        DormStayDO stay = store.stay(req.getStayId()); scope.read(stay.getStudentId(), stay.getClassId());
        scope.maintain(references.student(stay.getStudentId()));
        require(stay.getCheckInTime() != null && !req.getOccurredAt().isBefore(stay.getCheckInTime())
                && (stay.getCheckOutTime() == null || !req.getOccurredAt().isAfter(stay.getCheckOutTime()))
                && !req.getOccurredAt().isAfter(DormRules.now()), "发生时间不在实际入住区间内");
        DormViolationDO row = create ? new DormViolationDO() : store.get(store.violations, req.getId());
        require(create || Objects.equals(row.getStayId(), stay.getId()), "已有违规不能更换学生或住宿记录");
        if (create) {
            row.setStudentId(stay.getStudentId()); row.setStudentNo(stay.getStudentNo()); row.setStudentName(stay.getStudentName());
            row.setClassId(stay.getClassId()); row.setClassName(stay.getClassName()); row.setStayId(stay.getId());
        }
        DormChangeRequestDO later = store.changeRequests.selectOne(DormStore.<DormChangeRequestDO>query()
                .eq("stay_id", stay.getId()).eq("status", "APPROVED").gt("reviewed_at", req.getOccurredAt())
                .orderByAsc("reviewed_at", "id").last("LIMIT 1"));
        row.setRoomId(later == null ? stay.getRoomId() : store.bed(later.getSourceBedId()).getRoomId());
        row.setBedLabel(later == null ? stay.getBedLabel() : later.getSourceBedLabel());
        row.setOccurredAt(req.getOccurredAt()); row.setType(req.getType()); row.setDescription(req.getDescription());
        if (req.getDisciplineId() != null) {
            checkDiscipline(row.getStudentId(), req.getDisciplineId()); row.setDisciplineId(req.getDisciplineId());
        }
        if (create) store.insert(store.violations, row); else store.update(store.violations, row, req.getRowVersion());
        return row.getId();
    }
    private void checkDiscipline(Long student, Long discipline) {
        require(references.gateway().disciplineExists(student, discipline), "处分不存在、不属于该生或真实关联尚未接入");
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void link(DisciplineReq req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:violation:update"); guard.lock();
        DormViolationDO row = store.get(store.violations, req.getId()); scope.read(row.getStudentId(), row.getClassId());
        scope.maintain(references.student(row.getStudentId())); checkDiscipline(row.getStudentId(), req.getDisciplineId());
        row.setDisciplineId(req.getDisciplineId()); store.update(store.violations, row, req.getRowVersion());
    }
    public ViolationResp get(Long id) {
        DormAccess.Scope scope = access.scope("dorm:violation:query"); DormViolationDO row = store.get(store.violations, id);
        scope.read(row.getStudentId(), row.getClassId());
        return BeanUtils.toBean(row, ViolationResp.class);
    }
    public PageResult<ViolationResp> page(DormQuery req) {
        DormAccess.Scope scope = access.scope("dorm:violation:query");
        QueryWrapper<DormViolationDO> query = store.filters(DormStore.<DormViolationDO>query(), req, "occurred_at")
                .eq(req.getStayId() != null, "stay_id", req.getStayId()).orderByDesc("occurred_at", "id"); scope.apply(query);
        return store.page(store.violations, query, req, ViolationResp.class);
    }
}
