package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormResourceVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.VersionReq;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormResourceServiceImpl implements DormResourceService {
    @Resource private DormStore store;
    @Resource private DormGuard guard;
    @Resource private DormAccess access;
    @Resource private DormReferences references;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long saveBuilding(BuildingSave req, boolean create) {
        store.valid(req);
        access.administrator(create ? "dorm:building:create" : "dorm:building:update");
        guard.lock();
        require(create == (req.getId() == null), "创建与修改参数不匹配");
        DormBuildingDO row = create ? new DormBuildingDO() : store.building(req.getId());
        require(store.buildings.selectCount(DormStore.<DormBuildingDO>query().eq("name", req.getName())
                .ne(!create, "id", row.getId())) == 0, "楼栋名称重复");
        if (!create) {
            DormRules.version(req.getRowVersion(), row.getRowVersion());
            require(store.rooms.selectCount(DormStore.<DormRoomDO>query().eq("building_id", row.getId())
                    .gt("floor_number", req.getFloors())) == 0, "楼层调整会影响已有房间");
            require("UNLIMITED".equals(req.getGender()) || store.stays.selectCount(store.activeStays()
                    .eq("building_id", row.getId()).ne("gender", req.getGender())) == 0, "性别限制与现有分配不符");
        }
        row.setName(req.getName()); row.setFloors(req.getFloors());
        row.setGender(req.getGender()); row.setStatus(req.getStatus());
        if (create) store.insert(store.buildings, row);
        else store.update(store.buildings, row, req.getRowVersion());
        return row.getId();
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteBuilding(VersionReq req) {
        store.valid(req); access.administrator("dorm:building:delete"); guard.lock();
        DormBuildingDO row = store.building(req.getId());
        require(store.rooms.selectCount(DormStore.<DormRoomDO>query().eq("building_id", row.getId())) == 0
                && store.stays.selectCount(DormStore.<DormStayDO>query().eq("building_id", row.getId())) == 0,
                "楼栋已有房间或历史分配，只能停用");
        store.delete(store.buildings, row, req.getRowVersion());
    }

    public BuildingResp getBuilding(Long id) {
        access.staff("dorm:building:query");
        return BeanUtils.toBean(store.building(id), BuildingResp.class);
    }
    public PageResult<BuildingResp> buildingPage(DormQuery req) {
        store.valid(req); access.staff("dorm:building:query");
        return store.page(store.buildings, DormStore.<DormBuildingDO>query()
                .like(req.getKeyword() != null, "name", req.getKeyword()).orderByAsc("id"), req, BuildingResp.class);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long saveRoom(RoomSave req, boolean create) {
        store.valid(req); access.administrator(create ? "dorm:room:create" : "dorm:room:update"); guard.lock();
        require(create == (req.getId() == null), "创建与修改参数不匹配");
        DormBuildingDO building = store.building(req.getBuildingId());
        require(req.getFloorNumber() <= building.getFloors(), "楼层超出楼栋范围");
        DormRoomDO row = create ? new DormRoomDO() : store.room(req.getId());
        require(create || Objects.equals(row.getBuildingId(), req.getBuildingId()), "已有房间不能更换所属楼栋");
        require(store.rooms.selectCount(DormStore.<DormRoomDO>query().eq("building_id", req.getBuildingId())
                .eq("room_no", req.getRoomNo()).ne(!create, "id", row.getId())) == 0, "房间号重复");
        row.setBuildingId(req.getBuildingId()); row.setRoomNo(req.getRoomNo());
        row.setFloorNumber(req.getFloorNumber()); row.setStatus(req.getStatus());
        if (create) {
            require(req.getBedCount() != null, "创建房间必须指定床位数");
            store.insert(store.rooms, row);
        } else store.update(store.rooms, row, req.getRowVersion());
        if (req.getBedCount() != null) resizeBeds(row.getId(), req.getBedCount());
        return row.getId();
    }

    private void resizeBeds(Long roomId, int count) {
        Map<String, DormBedDO> existing = store.beds.selectList(DormStore.<DormBedDO>query().eq("room_id", roomId))
                .stream().collect(Collectors.toMap(DormBedDO::getBedNo, v -> v));
        for (int i = 1; i <= count; i++) {
            String no = String.format(java.util.Locale.ROOT, "%03d", i);
            DormBedDO bed = existing.remove(no);
            if (bed == null) {
                bed = new DormBedDO(); bed.setRoomId(roomId); bed.setBedNo(no); bed.setStatus(0);
                store.insert(store.beds, bed);
            } else if (!Integer.valueOf(0).equals(bed.getStatus())) {
                bed.setStatus(0); store.update(store.beds, bed, bed.getRowVersion());
            }
        }
        for (DormBedDO bed : existing.values()) {
            require(store.stays.selectCount(store.activeStays().eq("bed_id", bed.getId())) == 0, "减少床位会影响已有分配");
            if (!Integer.valueOf(1).equals(bed.getStatus())) {
                bed.setStatus(1); store.update(store.beds, bed, bed.getRowVersion());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void saveBed(BedSave req) {
        store.valid(req); access.administrator("dorm:room:update"); guard.lock();
        DormBedDO bed = store.bed(req.getId());
        require(req.getStatus() == 0 || store.stays.selectCount(store.activeStays().eq("bed_id", bed.getId())) == 0,
                "床位已被分配，不能停用");
        bed.setStatus(req.getStatus()); store.update(store.beds, bed, req.getRowVersion());
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteRoom(VersionReq req) {
        store.valid(req); access.administrator("dorm:room:delete"); guard.lock();
        DormRoomDO room = store.room(req.getId());
        require(store.stays.selectCount(DormStore.<DormStayDO>query().eq("room_id", room.getId())) == 0
                && store.hygieneChecks.selectCount(DormStore.<DormHygieneCheckDO>query().eq("room_id", room.getId())) == 0
                && store.nightChecks.selectCount(DormStore.<DormNightCheckDO>query().eq("room_id", room.getId())) == 0
                && store.violations.selectCount(DormStore.<DormViolationDO>query().eq("room_id", room.getId())) == 0,
                "房间已有历史记录，只能停用");
        String bedIds = "SELECT id FROM dorm_bed WHERE tenant_id=" + DormStore.tenant() + " AND room_id=" + room.getId();
        require(store.changeRequests.selectCount(DormStore.<DormChangeRequestDO>query()
                .and(q -> q.inSql("source_bed_id", bedIds).or().inSql("target_bed_id", bedIds))) == 0,
                "房间被调宿申请或调床历史引用，只能停用");
        for (DormBedDO bed : store.beds.selectList(DormStore.<DormBedDO>query().eq("room_id", room.getId())))
            store.delete(store.beds, bed, bed.getRowVersion());
        store.delete(store.rooms, room, req.getRowVersion());
    }

    public RoomResp getRoom(Long id) {
        access.staff("dorm:room:query");
        return roomViews(Collections.singletonList(BeanUtils.toBean(store.room(id), RoomResp.class))).get(0);
    }
    public PageResult<RoomResp> roomPage(DormQuery req) {
        store.valid(req); access.staff("dorm:room:query");
        PageResult<RoomResp> result = store.page(store.rooms, DormStore.<DormRoomDO>query()
                .eq(req.getBuildingId() != null, "building_id", req.getBuildingId())
                .like(req.getKeyword() != null, "room_no", req.getKeyword()).orderByAsc("building_id", "room_no", "id"),
                req, RoomResp.class);
        roomViews(result.getList());
        return result;
    }
    private List<RoomResp> roomViews(List<RoomResp> rows) {
        if (rows.isEmpty()) return rows;
        Set<Long> ids = rows.stream().map(RoomResp::getId).collect(Collectors.toSet());
        Set<Long> buildingIds = rows.stream().map(RoomResp::getBuildingId).collect(Collectors.toSet());
        Map<Long, DormBuildingDO> buildings = store.buildings.selectList(DormStore.<DormBuildingDO>query().in("id", buildingIds))
                .stream().collect(Collectors.toMap(DormBuildingDO::getId, b -> b));
        Map<Long, Long> beds = store.beds.selectList(DormStore.<DormBedDO>query().in("room_id", ids).eq("status", 0))
                .stream().collect(Collectors.groupingBy(DormBedDO::getRoomId, Collectors.counting()));
        List<DormStayDO> stays = store.stays.selectList(store.activeStays().in("room_id", ids));
        Map<Long, Long> allocated = stays.stream().collect(Collectors.groupingBy(DormStayDO::getRoomId, Collectors.counting()));
        Map<Long, Long> occupied = stays.stream().filter(s -> "OCCUPIED".equals(s.getStatus()))
                .collect(Collectors.groupingBy(DormStayDO::getRoomId, Collectors.counting()));
        rows.forEach(r -> {
            DormBuildingDO building = buildings.get(r.getBuildingId());
            require(building != null, "房间所属楼栋不存在");
            r.setBuildingName(building.getName()); r.setGender(building.getGender());
            r.setBedCount(beds.getOrDefault(r.getId(), 0L)); r.setAllocatedCount(allocated.getOrDefault(r.getId(), 0L));
            r.setOccupiedCount(occupied.getOrDefault(r.getId(), 0L));
        });
        return rows;
    }

    public PageResult<BedResp> bedOptions(DormQuery req) {
        store.valid(req);
        DormAccess.Scope scope = access.scope("dorm:bed:query");
        QueryWrapper<DormBedDO> query = DormStore.<DormBedDO>query().eq("status", 0)
                .eq(req.getRoomId() != null, "room_id", req.getRoomId())
                .notInSql("id", "SELECT bed_id FROM dorm_stay WHERE tenant_id=" + DormStore.tenant()
                        + " AND deleted=0 AND status IN ('RESERVED','OCCUPIED')");
        String rooms = "SELECT r.id FROM dorm_room r JOIN dorm_building b ON b.id=r.building_id"
                + " AND b.tenant_id=r.tenant_id WHERE r.tenant_id=" + DormStore.tenant()
                + " AND r.deleted=0 AND b.deleted=0 AND r.status=0 AND b.status=0";
        if (req.getBuildingId() != null) rooms += " AND b.id=" + req.getBuildingId();
        Student student = null;
        if ("STUDENT".equals(scope.type())) {
            require(req.getStudentId() == null || Objects.equals(req.getStudentId(), scope.studentId()), "只能查询本人可选床位");
            student = references.student(scope.studentId());
        } else if (req.getStudentId() != null) {
            student = references.student(req.getStudentId()); scope.maintain(student);
        }
        if (student != null) rooms += " AND b.gender IN ('UNLIMITED','" + student.getGender() + "')";
        query.inSql("room_id", rooms).orderByAsc("room_id", "bed_no", "id");
        PageResult<BedResp> result = store.page(store.beds, query, req, BedResp.class);
        bedViews(result.getList());
        return result;
    }

    public List<BedResp> beds(Long roomId) {
        access.staff("dorm:room:query"); store.room(roomId);
        return bedViews(BeanUtils.toBean(store.beds.selectList(DormStore.<DormBedDO>query().eq("room_id", roomId)
                .orderByAsc("bed_no", "id")), BedResp.class));
    }
    private List<BedResp> bedViews(List<BedResp> rows) {
        if (rows.isEmpty()) return rows;
        Set<Long> roomIds = rows.stream().map(BedResp::getRoomId).collect(Collectors.toSet());
        Map<Long, DormRoomDO> rooms = store.rooms.selectList(DormStore.<DormRoomDO>query().in("id", roomIds))
                .stream().collect(Collectors.toMap(DormRoomDO::getId, room -> room));
        Set<Long> buildingIds = rooms.values().stream().map(DormRoomDO::getBuildingId).collect(Collectors.toSet());
        require(!buildingIds.isEmpty(), "床位所属房间不存在");
        Map<Long, DormBuildingDO> buildings = store.buildings.selectList(DormStore.<DormBuildingDO>query().in("id", buildingIds))
                .stream().collect(Collectors.toMap(DormBuildingDO::getId, building -> building));
        for (BedResp bed : rows) {
            DormRoomDO room = rooms.get(bed.getRoomId());
            require(room != null && buildings.containsKey(room.getBuildingId()), "床位所属资源不存在");
            bed.setBuildingId(room.getBuildingId()); bed.setRoomNo(room.getRoomNo());
            bed.setBuildingName(buildings.get(room.getBuildingId()).getName());
            bed.setBedLabel(bed.getBuildingName() + " / " + bed.getRoomNo() + " / " + bed.getBedNo());
        }
        return rows;
    }
}
