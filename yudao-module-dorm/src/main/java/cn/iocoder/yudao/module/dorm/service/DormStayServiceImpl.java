package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.StayResp;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormStayServiceImpl implements DormStayService {
    @Resource private DormStore store;
    @Resource private DormGuard guard;
    @Resource private DormAccess access;
    @Resource private DormReferences references;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long assign(AssignReq req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:stay:assign"); guard.lock();
        Student student = references.student(req.getStudentId()); scope.maintain(student);
        return assignOne(student, store.bed(req.getBedId()));
    }
    private Long assignOne(Student student, DormBedDO bed) {
        require(student.isEnrolled(), "学生不在籍，不能新分配床位");
        require(store.stays.selectCount(store.activeStays().eq("student_id", student.getId())) == 0, "学生已有有效床位分配");
        store.available(bed, student.getGender(), null);
        DormRoomDO room = store.room(bed.getRoomId());
        DormStayDO stay = new DormStayDO();
        stay.setStudentId(student.getId()); stay.setStudentNo(student.getStudentNo()); stay.setStudentName(student.getName());
        stay.setClassId(student.getClassId()); stay.setClassName(student.getClassName()); stay.setGender(student.getGender());
        stay.setBedId(bed.getId()); stay.setRoomId(room.getId()); stay.setBuildingId(room.getBuildingId());
        stay.setBedLabel(store.bedLabel(bed)); stay.setStatus("RESERVED");
        stay.setDepositAmount(BigDecimal.ZERO); stay.setDeductionAmount(BigDecimal.ZERO); stay.setRefundAmount(BigDecimal.ZERO);
        stay.setRefundStatus("NOT_REQUIRED");
        store.insert(store.stays, stay);
        return stay.getId();
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<Long> autoAssign(AutoAssignReq req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:stay:assign"); guard.lock();
        Set<Long> students = DormRules.ids(req.getStudentIds()), buildings = DormRules.ids(req.getBuildingIds());
        Map<Long, Student> data = references.students(students);
        data.values().forEach(scope::maintain);
        Map<Long, DormBuildingDO> buildingMap = store.buildings.selectList(DormStore.<DormBuildingDO>query()
                .in("id", buildings).eq("status", 0)).stream().collect(Collectors.toMap(DormBuildingDO::getId, b -> b));
        require(buildingMap.size() == buildings.size(), "所选楼栋不存在或已停用");
        Map<Long, DormRoomDO> rooms = store.rooms.selectList(DormStore.<DormRoomDO>query()
                .in("building_id", buildings).eq("status", 0)).stream().collect(Collectors.toMap(DormRoomDO::getId, r -> r));
        require(!rooms.isEmpty(), "所选楼栋没有可用房间");
        List<DormBedDO> beds = store.beds.selectList(DormStore.<DormBedDO>query().in("room_id", rooms.keySet()).eq("status", 0)
                .notInSql("id", "SELECT bed_id FROM dorm_stay WHERE tenant_id=" + DormStore.tenant()
                        + " AND deleted=0 AND status IN ('RESERVED','OCCUPIED')"));
        beds.sort(Comparator.comparing((DormBedDO b) -> rooms.get(b.getRoomId()).getBuildingId())
                .thenComparing(b -> rooms.get(b.getRoomId()).getRoomNo()).thenComparing(DormBedDO::getBedNo)
                .thenComparing(DormBedDO::getId));
        List<Long> result = new ArrayList<>();
        for (Long id : students.stream().sorted().collect(Collectors.toList())) {
            Student student = data.get(id);
            DormBedDO bed = beds.stream().filter(b -> {
                String gender = buildingMap.get(rooms.get(b.getRoomId()).getBuildingId()).getGender();
                return "UNLIMITED".equals(gender) || gender.equals(student.getGender());
            }).min(Comparator.comparing((DormBedDO b) -> "UNLIMITED".equals(
                    buildingMap.get(rooms.get(b.getRoomId()).getBuildingId()).getGender()))).orElse(null);
            require(bed != null, "符合性别限制的床位不足，整批未分配");
            result.add(assignOne(student, bed)); beds.remove(bed);
        }
        return result;
    }
    private DormStayDO maintain(VersionReq req, String permission) {
        store.valid(req); DormAccess.Scope scope = access.staff(permission); guard.lock();
        DormStayDO stay = store.stay(req.getId());
        scope.read(stay.getStudentId(), stay.getClassId());
        scope.maintain(references.student(stay.getStudentId()));
        DormRules.version(req.getRowVersion(), stay.getRowVersion());
        return stay;
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void cancel(VersionReq req) {
        DormStayDO stay = maintain(req, "dorm:stay:cancel");
        require("RESERVED".equals(stay.getStatus()), "仅待入住分配可以取消");
        store.noPending(stay.getId()); stay.setStatus("CANCELLED");
        store.update(store.stays, stay, req.getRowVersion());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void checkIn(CheckInReq req) {
        DormStayDO stay = maintain(req, "dorm:stay:check-in");
        require("RESERVED".equals(stay.getStatus()), "仅待入住分配可以办理入住");
        require(references.student(stay.getStudentId()).isEnrolled(), "学生不在籍，不能办理入住");
        DormRules.money(req.getDepositAmount());
        stay.setStatus("OCCUPIED"); stay.setCheckInTime(DormRules.now()); stay.setDepositAmount(req.getDepositAmount());
        store.update(store.stays, stay, req.getRowVersion());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void checkOut(CheckOutReq req) {
        DormStayDO stay = maintain(req, "dorm:stay:check-out");
        require("OCCUPIED".equals(stay.getStatus()), "仅已入住学生可以退宿");
        store.noPending(stay.getId()); DormRules.money(req.getDeductionAmount());
        require(req.getDeductionAmount().compareTo(stay.getDepositAmount()) <= 0, "扣款不能超过实收押金");
        require(req.getDeductionAmount().signum() == 0 || (req.getDeductionReason() != null
                && !req.getDeductionReason().trim().isEmpty()), "发生扣款必须说明原因");
        stay.setInspection(req.getInspection()); stay.setDeductionAmount(req.getDeductionAmount());
        stay.setDeductionReason(req.getDeductionReason()); stay.setRefundAmount(stay.getDepositAmount().subtract(req.getDeductionAmount()));
        stay.setRefundStatus(stay.getRefundAmount().signum() == 0 ? "NOT_REQUIRED" : "PENDING");
        stay.setCheckOutTime(DormRules.now()); stay.setStatus("CHECKED_OUT");
        store.update(store.stays, stay, req.getRowVersion());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void refund(VersionReq req) {
        DormStayDO stay = maintain(req, "dorm:stay:refund");
        require("CHECKED_OUT".equals(stay.getStatus()) && "PENDING".equals(stay.getRefundStatus()), "没有可确认的待退押金");
        stay.setRefundStatus("REFUNDED"); stay.setRefundTime(DormRules.now()); stay.setRefundAccountId(access.accountId());
        store.update(store.stays, stay, req.getRowVersion());
    }
    public StayResp get(Long id) {
        DormAccess.Scope scope = access.scope("dorm:stay:query"); DormStayDO stay = store.stay(id);
        scope.read(stay.getStudentId(), stay.getClassId());
        return BeanUtils.toBean(stay, StayResp.class);
    }
    public PageResult<StayResp> page(DormQuery req) {
        DormAccess.Scope scope = access.scope("dorm:stay:query");
        return scopedPage(req, scope);
    }
    public PageResult<StayResp> my(DormQuery req) {
        access.student("dorm:stay:my");
        return scopedPage(req, access.scope("dorm:stay:my"));
    }
    private PageResult<StayResp> scopedPage(DormQuery req, DormAccess.Scope scope) {
        QueryWrapper<DormStayDO> query = store.filters(DormStore.<DormStayDO>query(), req, null)
                .eq(req.getStayId() != null, "id", req.getStayId())
                .eq(req.getBuildingId() != null, "building_id", req.getBuildingId())
                .eq(req.getStatus() != null, "status", req.getStatus()).orderByDesc("id");
        scope.apply(query);
        return store.page(store.stays, query, req, StayResp.class);
    }
}
