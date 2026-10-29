package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.ChangeRequestResp;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormChangeServiceImpl implements DormChangeService {
    @Resource private DormStore store;
    @Resource private DormGuard guard;
    @Resource private DormAccess access;
    @Resource private DormReferences references;
    @Resource private DormNotifier notifier;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(ChangeCreateReq req) {
        store.valid(req); Student student = access.student("dorm:change-request:create"); guard.lock();
        require(student.getId().equals(access.student("dorm:change-request:create").getId()), "学生身份绑定已变化");
        DormStayDO stay = store.stay(req.getStayId());
        require(student.getId().equals(stay.getStudentId()) && "OCCUPIED".equals(stay.getStatus()), "只能申请本人已入住床位");
        DormRules.version(req.getStayVersion(), stay.getRowVersion()); store.noPending(stay.getId());
        DormChangeRequestDO row = request(req, stay, "STUDENT", "PENDING");
        store.insert(store.changeRequests, row);
        return row.getId();
    }
    private DormChangeRequestDO request(ChangeCreateReq req, DormStayDO stay, String source, String status) {
        require(!Objects.equals(stay.getBedId(), req.getTargetBedId()), "目标床位与当前床位相同");
        Student student = references.student(stay.getStudentId());
        require(student.isEnrolled(), "学生不在籍，不能调整床位");
        DormBedDO target = store.bed(req.getTargetBedId()); store.available(target, student.getGender(), stay.getId());
        DormChangeRequestDO row = new DormChangeRequestDO();
        row.setStayId(stay.getId()); row.setStudentId(stay.getStudentId()); row.setClassId(stay.getClassId());
        row.setStudentNo(stay.getStudentNo()); row.setStudentName(stay.getStudentName()); row.setClassName(stay.getClassName());
        row.setApplicantId(access.accountId()); row.setSourceBedId(stay.getBedId()); row.setSourceBedLabel(stay.getBedLabel());
        row.setTargetBedId(target.getId()); row.setTargetBedLabel(store.bedLabel(target)); row.setStayVersion(stay.getRowVersion());
        row.setReason(req.getReason()); row.setSource(source); row.setStatus(status);
        return row;
    }
    private DormChangeRequestDO pending(VersionReq req, String permission) {
        store.valid(req); access.permission(permission); guard.lock();
        DormChangeRequestDO row = store.get(store.changeRequests, req.getId());
        DormRules.version(req.getRowVersion(), row.getRowVersion());
        require("PENDING".equals(row.getStatus()), "申请已处理，不能重复操作");
        return row;
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void cancel(VersionReq req) {
        Student student = access.student("dorm:change-request:cancel");
        DormChangeRequestDO row = pending(req, "dorm:change-request:cancel");
        require(Objects.equals(row.getApplicantId(), access.accountId()) && Objects.equals(row.getStudentId(), student.getId()),
                "只能取消本人待审申请");
        row.setStatus("CANCELLED"); store.update(store.changeRequests, row, req.getRowVersion());
    }
    private DormStayDO reviewStay(DormChangeRequestDO row, String permission) {
        DormAccess.Scope scope = access.staff(permission);
        scope.read(row.getStudentId(), row.getClassId());
        scope.maintain(references.student(row.getStudentId()));
        return store.stay(row.getStayId());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void approve(ReviewReq req) {
        DormChangeRequestDO row = pending(req, "dorm:change-request:approve");
        DormStayDO stay = reviewStay(row, "dorm:change-request:approve");
        DormRules.version(row.getStayVersion(), stay.getRowVersion());
        require("OCCUPIED".equals(stay.getStatus()) && Objects.equals(stay.getBedId(), row.getSourceBedId()), "原住宿状态已变化");
        move(stay, row.getTargetBedId());
        finish(row, req, "APPROVED");
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reject(ReviewReq req) {
        require(req.getReason() != null && !req.getReason().trim().isEmpty(), "拒绝申请必须说明原因");
        DormChangeRequestDO row = pending(req, "dorm:change-request:approve");
        reviewStay(row, "dorm:change-request:approve");
        finish(row, req, "REJECTED");
    }
    private void finish(DormChangeRequestDO row, ReviewReq req, String status) {
        row.setStatus(status); row.setReviewerId(access.accountId()); row.setReviewedAt(DormRules.now());
        row.setReviewReason(req.getReason()); store.update(store.changeRequests, row, req.getRowVersion());
        notifier.student(row.getStudentId(), "调宿申请" + ("APPROVED".equals(status) ? "已通过：" : "已拒绝：") + row.getReason());
    }
    private void move(DormStayDO stay, Long targetId) {
        Student student = references.student(stay.getStudentId());
        require(student.isEnrolled(), "学生不在籍，不能调整床位");
        DormBedDO target = store.bed(targetId); store.available(target, student.getGender(), stay.getId());
        DormRoomDO room = store.room(target.getRoomId());
        stay.setBedId(target.getId()); stay.setRoomId(room.getId()); stay.setBuildingId(room.getBuildingId());
        stay.setBedLabel(store.bedLabel(target)); stay.setGender(student.getGender());
        store.update(store.stays, stay, stay.getRowVersion());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long direct(ChangeCreateReq req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:change-request:direct"); guard.lock();
        DormStayDO stay = store.stay(req.getStayId()); scope.read(stay.getStudentId(), stay.getClassId());
        scope.maintain(references.student(stay.getStudentId()));
        DormRules.version(req.getStayVersion(), stay.getRowVersion()); require(DormRules.active(stay.getStatus()), "住宿记录已结束");
        store.noPending(stay.getId());
        DormChangeRequestDO row = request(req, stay, "STAFF", "APPROVED");
        row.setReviewerId(access.accountId()); row.setReviewedAt(DormRules.now());
        store.insert(store.changeRequests, row); move(stay, req.getTargetBedId());
        notifier.student(stay.getStudentId(), "床位已调整至：" + stay.getBedLabel());
        return row.getId();
    }
    public ChangeRequestResp get(Long id) {
        DormAccess.Scope scope = access.scope("dorm:change-request:query");
        DormChangeRequestDO row = store.get(store.changeRequests, id); scope.read(row.getStudentId(), row.getClassId());
        return BeanUtils.toBean(row, ChangeRequestResp.class);
    }
    public PageResult<ChangeRequestResp> page(DormQuery req) {
        DormAccess.Scope scope = access.scope("dorm:change-request:query");
        QueryWrapper<DormChangeRequestDO> query = DormStore.query(); store.valid(req);
        query.eq(req.getStudentId() != null, "student_id", req.getStudentId()).eq(req.getClassId() != null, "class_id", req.getClassId())
                .eq(req.getStayId() != null, "stay_id", req.getStayId()).eq(req.getStatus() != null, "status", req.getStatus());
        query.and(req.getKeyword() != null, q -> q.like("student_name", req.getKeyword()).or().like("student_no", req.getKeyword()));
        scope.apply(query); query.orderByDesc("id");
        return store.page(store.changeRequests, query, req, ChangeRequestResp.class);
    }
}
