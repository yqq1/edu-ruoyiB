package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import cn.iocoder.yudao.module.dorm.dal.mysql.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.*;

@Component
public class DormStore {
    @Resource DormBuildingMapper buildings;
    @Resource DormRoomMapper rooms;
    @Resource DormBedMapper beds;
    @Resource DormStayMapper stays;
    @Resource DormChangeRequestMapper changeRequests;
    @Resource DormNightCheckMapper nightChecks;
    @Resource DormTripMapper trips;
    @Resource DormHygieneCheckMapper hygieneChecks;
    @Resource DormViolationMapper violations;
    @Resource DormPhotoMapper photos;
    @Resource private Validator validator;
    public static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }
    public static <T> QueryWrapper<T> query() {
        return new QueryWrapper<T>().eq("tenant_id", tenant()).eq("deleted", false);
    }
    public void valid(Object req) {
        require(req != null, "请求不能为空");
        Set<javax.validation.ConstraintViolation<Object>> errors = validator.validate(req);
        require(errors.isEmpty(), errors.isEmpty() ? "参数无效" : errors.iterator().next().getMessage());
    }
    public <T extends DormRecordDO> T get(BaseMapper<T> mapper, Long id) {
        DormRules.id(id);
        T row = mapper.selectOne(DormStore.<T>query().eq("id", id));
        require(row != null, "记录不存在或不在可访问范围");
        return row;
    }
    public <T extends DormRecordDO> void insert(BaseMapper<T> mapper, T row) {
        row.setTenantId(tenant());
        row.setRowVersion(1L);
        require(mapper.insert(row) == 1, "记录写入失败");
    }
    public <T extends DormRecordDO> void update(BaseMapper<T> mapper, T row, Long version) {
        DormRules.version(version, row.getRowVersion());
        row.setRowVersion(version + 1);
        require(mapper.update(row, DormStore.<T>query().eq("id", row.getId()).eq("row_version", version)) == 1,
                DORM_CONFLICT, "数据版本已变化，请刷新后重试");
    }
    public <T extends DormRecordDO> void delete(BaseMapper<T> mapper, T row, Long version) {
        DormRules.version(version, row.getRowVersion());
        require(mapper.delete(DormStore.<T>query().eq("id", row.getId()).eq("row_version", version)) == 1,
                DORM_CONFLICT, "数据版本已变化，请刷新后重试");
    }
    public <T, R> PageResult<R> page(BaseMapper<T> mapper, QueryWrapper<T> query, PageParam req, Class<R> type) {
        DormRules.page(req);
        Page<T> page = mapper.selectPage(new Page<>(req.getPageNo(), req.getPageSize()), query);
        return new PageResult<>(BeanUtils.toBean(page.getRecords(), type), page.getTotal());
    }
    public <T> QueryWrapper<T> filters(QueryWrapper<T> query, DormQuery req, String dateColumn) {
        valid(req);
        DormRules.dates(req);
        query.eq(req.getStudentId() != null, "student_id", req.getStudentId())
                .eq(req.getClassId() != null, "class_id", req.getClassId())
                .eq(req.getRoomId() != null, "room_id", req.getRoomId());
        query.and(req.getKeyword() != null, q -> q.like("student_name", req.getKeyword()).or().like("student_no", req.getKeyword()));
        if (dateColumn != null) {
            query.ge(req.getStartDate() != null, dateColumn, req.getStartDate());
            query.lt(req.getEndDate() != null, dateColumn, req.getEndDate() == null ? null : req.getEndDate().plusDays(1));
        }
        return query;
    }
    public DormBuildingDO building(Long id) { return get(buildings, id); }
    public DormRoomDO room(Long id) { return get(rooms, id); }
    public DormBedDO bed(Long id) { return get(beds, id); }
    public DormStayDO stay(Long id) { return get(stays, id); }
    public QueryWrapper<DormStayDO> activeStays() {
        return DormStore.<DormStayDO>query().in("status", "RESERVED", "OCCUPIED");
    }
    public String bedLabel(DormBedDO bed) {
        DormRoomDO room = room(bed.getRoomId());
        return building(room.getBuildingId()).getName() + " / " + room.getRoomNo() + " / " + bed.getBedNo();
    }
    public void available(DormBedDO bed, String gender, Long excludingStay) {
        DormRoomDO room = room(bed.getRoomId());
        DormBuildingDO building = building(room.getBuildingId());
        require(Integer.valueOf(0).equals(bed.getStatus()) && Integer.valueOf(0).equals(room.getStatus())
                && Integer.valueOf(0).equals(building.getStatus()), "楼栋、房间或床位已停用");
        require("UNLIMITED".equals(building.getGender()) || building.getGender().equals(gender), "不符合楼栋性别限制");
        QueryWrapper<DormStayDO> query = activeStays().eq("bed_id", bed.getId());
        if (excludingStay != null) query.ne("id", excludingStay);
        require(stays.selectCount(query) == 0, DORM_CONFLICT, "目标床位已被分配");
    }
    public void noPending(Long stayId) {
        require(changeRequests.selectCount(DormStore.<DormChangeRequestDO>query().eq("stay_id", stayId)
                .eq("status", "PENDING")) == 0, "请先处理或取消待审调宿申请");
    }
}
