package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.*;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormNightServiceImpl implements DormNightService {
    @Resource private DormStore store;
    @Resource private DormAccess access;
    @Resource private DormGuard guard;
    @Resource private DormReferences references;
    @Resource private DormNotifier notifier;

    public PageResult<RosterItem> roster(DormQuery req) {
        DormAccess.Scope scope = access.staff("dorm:night-check:query");
        QueryWrapper<DormStayDO> query = store.filters(DormStore.<DormStayDO>query(), req, null).eq("status", "OCCUPIED");
        scope.apply(query); query.orderByAsc("student_id");
        PageResult<StayResp> stays = store.page(store.stays, query, req, StayResp.class);
        if (stays.getList().isEmpty()) return PageResult.empty(stays.getTotal());
        LocalDate date = DormRules.now().toLocalDate();
        Set<Long> ids = stays.getList().stream().map(StayResp::getStudentId).collect(Collectors.toSet());
        Map<Long, NightCheckResp> checks = store.nightChecks.selectList(DormStore.<DormNightCheckDO>query()
                .eq("check_date", date).in("student_id", ids)).stream()
                .collect(Collectors.toMap(DormNightCheckDO::getStudentId, c -> BeanUtils.toBean(c, NightCheckResp.class)));
        List<RosterItem> rows = new ArrayList<>();
        for (StayResp stay : stays.getList()) {
            RosterItem row = new RosterItem(); row.setStay(stay); row.setCheckDate(date);
            row.setCheck(checks.get(stay.getStudentId())); rows.add(row);
        }
        return new PageResult<>(rows, stays.getTotal());
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void save(NightBatch req) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:night-check:update"); guard.lock();
        LocalDate checkDate = DormRules.now().toLocalDate();
        require(req.getRosterDate() == null || checkDate.equals(req.getRosterDate()), "查寝名单已跨日，请刷新后重新录入");
        Set<Long> stays = new HashSet<>(); Set<Long> records = new HashSet<>();
        for (NightItem item : req.getItems()) {
            store.valid(item);
            require(stays.add(item.getStayId()), "同批次住宿记录重复");
            require(item.getId() == null || records.add(item.getId()), "同批次查寝记录重复");
            saveOne(scope, item, checkDate);
        }
    }
    private void saveOne(DormAccess.Scope scope, NightItem req, LocalDate checkDate) {
        boolean create = req.getId() == null;
        DormStayDO stay = store.stay(req.getStayId()); scope.read(stay.getStudentId(), stay.getClassId());
        scope.maintain(references.student(stay.getStudentId()));
        require(!"ABSENT".equals(req.getStatus()) || (req.getReason() != null && !req.getReason().trim().isEmpty()),
                "不在寝必须填写原因");
        DormNightCheckDO row;
        if (create) {
            require(checkDate.equals(req.getCheckDate()), "查寝名单已跨日，请刷新后重新录入");
            require("OCCUPIED".equals(stay.getStatus()), "当日新查寝记录只能来自当前已入住学生");
            row = new DormNightCheckDO();
            row.setStudentId(stay.getStudentId()); row.setStudentNo(stay.getStudentNo()); row.setStudentName(stay.getStudentName());
            row.setClassId(stay.getClassId()); row.setClassName(stay.getClassName()); row.setStayId(stay.getId());
            row.setRoomId(stay.getRoomId()); row.setBedLabel(stay.getBedLabel()); row.setCheckDate(checkDate);
            require(store.nightChecks.selectCount(DormStore.<DormNightCheckDO>query().eq("student_id", row.getStudentId())
                    .eq("check_date", row.getCheckDate())) == 0, "该生当日查寝已存在，请刷新后更正");
        } else {
            row = store.get(store.nightChecks, req.getId()); scope.read(row.getStudentId(), row.getClassId());
            require(Objects.equals(row.getStayId(), req.getStayId()), "历史查寝不能更换学生或住宿记录");
            require(Objects.equals(row.getCheckDate(), req.getCheckDate()), "历史查寝日期不能修改，请刷新后重试");
            DormRules.version(req.getRowVersion(), row.getRowVersion());
        }
        boolean alert = "ABSENT".equals(req.getStatus()) && !"ABSENT".equals(row.getStatus());
        row.setStatus(req.getStatus()); row.setReason("ABSENT".equals(req.getStatus()) ? req.getReason() : "");
        if (create) store.insert(store.nightChecks, row); else store.update(store.nightChecks, row, req.getRowVersion());
        if (alert) notifier.counselors(Collections.singleton(row.getClassId()),
                row.getStudentName() + " " + row.getCheckDate() + " 查寝不在寝：" + row.getReason());
    }
    public NightCheckResp get(Long id) {
        DormAccess.Scope scope = access.scope("dorm:night-check:query");
        DormNightCheckDO row = store.get(store.nightChecks, id); scope.read(row.getStudentId(), row.getClassId());
        return BeanUtils.toBean(row, NightCheckResp.class);
    }
    public PageResult<NightCheckResp> page(DormQuery req) {
        DormAccess.Scope scope = access.scope("dorm:night-check:query");
        QueryWrapper<DormNightCheckDO> query = store.filters(DormStore.<DormNightCheckDO>query(), req, "check_date")
                .eq(req.getStatus() != null, "status", req.getStatus()).orderByDesc("check_date", "id");
        scope.apply(query);
        return store.page(store.nightChecks, query, req, NightCheckResp.class);
    }
}
