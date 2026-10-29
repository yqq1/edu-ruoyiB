package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.TripResp;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormTravelVO.*;
import cn.iocoder.yudao.module.dorm.api.reference.DormReferenceData.Student;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.DORM_CONFLICT;

@Service
public class DormTravelServiceImpl implements DormTravelService {
    @Resource private DormStore store;
    @Resource private DormAccess access;
    @Resource private DormGuard guard;
    @Resource private DormReferences references;

    public ContextResp context() {
        Student student = access.student("dorm:travel:check-in");
        DormTripDO active = store.trips.selectOne(DormStore.<DormTripDO>query().eq("student_id", student.getId()).isNull("return_time"));
        ContextResp result = new ContextResp(); result.setServerTime(DormRules.now());
        result.setActiveTrip(BeanUtils.toBean(active, TripResp.class)); return result;
    }
    private void position(PositionReq req, LocalDateTime now) {
        require(!req.getCapturedAt().isAfter(now) && !req.getCapturedAt().isBefore(now.minusSeconds(60)),
                "定位已过期或定位时间在未来");
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public TripResp depart(PositionReq req, MultipartFile file) {
        store.valid(req); Student student = access.student("dorm:travel:check-in");
        PhotoPayload photo = DormImage.read(file); String fingerprint = DormImage.fingerprint(req, photo.getContent());
        guard.lock();
        Student current = access.student("dorm:travel:check-in");
        require(student.getId().equals(current.getId()), "学生身份绑定已变化");
        student = current;
        DormTripDO previous = store.trips.selectOne(DormStore.<DormTripDO>query().eq("student_id", student.getId())
                .eq("depart_request_id", req.getRequestId()));
        if (previous != null) {
            require(fingerprint.equals(previous.getDepartFingerprint()), DORM_CONFLICT, "请求标识已被不同内容使用");
            return BeanUtils.toBean(previous, TripResp.class);
        }
        require(student.isEnrolled(), "学生不在籍，不能新增离校记录");
        require(store.trips.selectCount(DormStore.<DormTripDO>query().eq("student_id", student.getId())
                .isNull("return_time")) == 0, "已有未返校记录，请先办理返校");
        LocalDateTime now = DormRules.now(); position(req, now);
        DormTripDO trip = new DormTripDO();
        trip.setStudentId(student.getId()); trip.setStudentNo(student.getStudentNo()); trip.setStudentName(student.getName());
        trip.setClassId(student.getClassId()); trip.setClassName(student.getClassName());
        trip.setDepartRequestId(req.getRequestId()); trip.setDepartFingerprint(fingerprint);
        trip.setDepartTime(now); trip.setDepartLatitude(req.getLatitude()); trip.setDepartLongitude(req.getLongitude());
        trip.setDepartAccuracy(req.getAccuracy()); trip.setDepartCapturedAt(req.getCapturedAt()); trip.setReviewStatus("PENDING");
        store.insert(store.trips, trip);
        DormPhotoDO image = new DormPhotoDO(); image.setTripId(trip.getId()); image.setContentType(photo.getContentType());
        image.setSize(photo.getContent().length); image.setContent(photo.getContent()); store.insert(store.photos, image);
        trip.setPhotoId(image.getId()); store.update(store.trips, trip, trip.getRowVersion());
        return BeanUtils.toBean(trip, TripResp.class);
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public TripResp returnToSchool(ReturnReq req) {
        store.valid(req); Student student = access.student("dorm:travel:check-in"); guard.lock();
        require(student.getId().equals(access.student("dorm:travel:check-in").getId()), "学生身份绑定已变化");
        DormTripDO trip = store.get(store.trips, req.getTripId());
        require(Objects.equals(student.getId(), trip.getStudentId()), "只能为本人离校记录办理返校");
        String fingerprint = DormImage.fingerprint(req, null);
        if (trip.getReturnTime() != null) {
            require(Objects.equals(req.getRequestId(), trip.getReturnRequestId()) && fingerprint.equals(trip.getReturnFingerprint()),
                    DORM_CONFLICT, "该记录已经返校，不能覆盖原打卡时间");
            return BeanUtils.toBean(trip, TripResp.class);
        }
        require(store.trips.selectCount(DormStore.<DormTripDO>query().eq("student_id", student.getId())
                .eq("return_request_id", req.getRequestId()).ne("id", trip.getId())) == 0, "请求标识已被其他返校记录使用");
        LocalDateTime now = DormRules.now(); position(req, now);
        require(!now.isBefore(trip.getDepartTime()), "服务器时间异常，返校早于离校");
        trip.setReturnRequestId(req.getRequestId()); trip.setReturnFingerprint(fingerprint);
        trip.setReturnTime(now); trip.setReturnLatitude(req.getLatitude()); trip.setReturnLongitude(req.getLongitude());
        trip.setReturnAccuracy(req.getAccuracy()); trip.setReturnCapturedAt(req.getCapturedAt());
        store.update(store.trips, trip, trip.getRowVersion());
        return BeanUtils.toBean(trip, TripResp.class);
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void review(TravelReview req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:travel:review"); guard.lock();
        DormTripDO trip = store.get(store.trips, req.getId()); scope.read(trip.getStudentId(), trip.getClassId());
        scope.maintain(references.student(trip.getStudentId()));
        require("PENDING".equals(trip.getReviewStatus()), "离校记录已审核");
        trip.setReviewStatus(req.getStatus()); trip.setReviewReason(req.getReason());
        trip.setReviewerId(access.accountId()); trip.setReviewedAt(DormRules.now());
        store.update(store.trips, trip, req.getRowVersion());
    }
    public TripResp get(Long id) {
        DormAccess.Scope scope = access.scope("dorm:travel:query");
        DormTripDO trip = store.get(store.trips, id); scope.read(trip.getStudentId(), trip.getClassId());
        return BeanUtils.toBean(trip, TripResp.class);
    }
    private QueryWrapper<DormTripDO> query(DormQuery req, DormAccess.Scope scope) {
        QueryWrapper<DormTripDO> query = store.filters(DormStore.<DormTripDO>query(), req, "depart_time");
        require(req.getRoomId() == null, "出行记录不支持按房间筛选");
        if (req.getStatus() != null) {
            require(Arrays.asList("OUTSIDE", "RETURNED").contains(req.getStatus()), "出行状态无效");
            if ("OUTSIDE".equals(req.getStatus())) query.isNull("return_time"); else query.isNotNull("return_time");
        }
        scope.apply(query); return query;
    }
    public PageResult<TripResp> page(DormQuery req) {
        return store.page(store.trips, query(req, access.scope("dorm:travel:query")).orderByDesc("depart_time", "id"), req, TripResp.class);
    }
    public List<ClassSummary> summary(DormQuery req) {
        DormAccess.Scope scope = access.staff("dorm:travel:query");
        List<Map<String, Object>> groups = store.trips.selectMaps(query(req, scope)
                .select("class_id", "MAX(class_name) AS class_name", "COUNT(*) AS total_departures",
                        "SUM(CASE WHEN return_time IS NULL THEN 1 ELSE 0 END) AS outside_count",
                        "SUM(CASE WHEN return_time IS NOT NULL THEN 1 ELSE 0 END) AS returned_count")
                .groupBy("class_id").orderByAsc("class_id"));
        List<ClassSummary> result = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            ClassSummary item = new ClassSummary();
            item.setClassId(((Number) group.get("class_id")).longValue()); item.setClassName((String) group.get("class_name"));
            item.setTotalDepartures(((Number) group.get("total_departures")).longValue());
            item.setOutside(((Number) group.get("outside_count")).longValue());
            item.setReturned(((Number) group.get("returned_count")).longValue()); result.add(item);
        }
        return result;
    }
    public PhotoPayload photo(Long id) {
        DormAccess.Scope scope = access.scope("dorm:photo:query");
        DormPhotoDO metadata = store.get(store.photos, id); DormTripDO trip = store.get(store.trips, metadata.getTripId());
        scope.read(trip.getStudentId(), trip.getClassId());
        require(Objects.equals(trip.getPhotoId(), metadata.getId()), "照片不属于该离校记录");
        DormPhotoDO row = store.photos.selectOne(DormStore.<DormPhotoDO>query().select("id", "content").eq("id", id));
        require(row != null && row.getContent() != null, "照片内容不存在");
        PhotoPayload result = new PhotoPayload(); result.setContent(row.getContent()); result.setContentType(metadata.getContentType());
        return result;
    }
}
