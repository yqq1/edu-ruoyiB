package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.HygieneSave;
import cn.iocoder.yudao.module.dorm.dal.dataobject.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import javax.annotation.Resource;
import java.math.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Service
public class DormHygieneServiceImpl implements DormHygieneService {
    @Resource private DormStore store;
    @Resource private DormAccess access;
    @Resource private DormGuard guard;
    @Resource private DormNotifier notifier;

    private DormRoomDO room(DormAccess.Scope scope, Long id) {
        DormRoomDO room = store.room(id);
        QueryWrapper<DormRoomDO> query = DormStore.<DormRoomDO>query().eq("id", id); scope.rooms(query, "id");
        require(store.rooms.selectCount(query) > 0, "房间不在本人可检查范围");
        return room;
    }
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long save(HygieneSave req, boolean create) {
        store.valid(req); DormAccess.Scope scope = access.staff("dorm:hygiene:update"); guard.lock();
        require(create == (req.getId() == null), "创建与修改参数不匹配");
        require(!req.getCheckDate().isAfter(DormRules.now().toLocalDate()), "不能录入未来卫生检查");
        DormRoomDO room = room(scope, req.getRoomId());
        DormHygieneCheckDO row = create ? new DormHygieneCheckDO() : store.get(store.hygieneChecks, req.getId());
        require(create || (Objects.equals(row.getRoomId(), req.getRoomId()) && Objects.equals(row.getCheckDate(), req.getCheckDate())),
                "已有检查不能更换房间或日期");
        require(store.hygieneChecks.selectCount(DormStore.<DormHygieneCheckDO>query().eq("room_id", room.getId())
                .eq("check_date", req.getCheckDate()).ne(!create, "id", row.getId())) == 0, "房间当日评分已存在");
        boolean alert = req.getScore() < 60 && (create || row.getScore() >= 60);
        row.setRoomId(room.getId()); row.setCheckDate(req.getCheckDate());
        if (create) row.setRoomLabel(store.building(room.getBuildingId()).getName() + " / " + room.getRoomNo());
        row.setScore(req.getScore()); row.setRemark(req.getRemark() == null ? "" : req.getRemark());
        if (create) store.insert(store.hygieneChecks, row); else store.update(store.hygieneChecks, row, req.getRowVersion());
        if (alert) {
            Set<Long> classes = store.stays.selectList(DormStore.<DormStayDO>query().eq("room_id", room.getId()).eq("status", "OCCUPIED"))
                    .stream().map(DormStayDO::getClassId).collect(Collectors.toSet());
            notifier.counselors(classes, row.getRoomLabel() + " " + row.getCheckDate() + " 卫生不合格：" + row.getScore() + "分");
        }
        return row.getId();
    }
    public HygieneCheckResp get(Long id) {
        DormAccess.Scope scope = access.staff("dorm:hygiene:query");
        DormHygieneCheckDO row = store.get(store.hygieneChecks, id); room(scope, row.getRoomId());
        return BeanUtils.toBean(row, HygieneCheckResp.class);
    }
    public PageResult<HygieneCheckResp> page(DormQuery req) {
        store.valid(req); DormRules.dates(req); DormAccess.Scope scope = access.staff("dorm:hygiene:query");
        QueryWrapper<DormHygieneCheckDO> query = DormStore.<DormHygieneCheckDO>query()
                .eq(req.getRoomId() != null, "room_id", req.getRoomId())
                .ge(req.getStartDate() != null, "check_date", req.getStartDate())
                .le(req.getEndDate() != null, "check_date", req.getEndDate()).orderByDesc("check_date", "id");
        scope.rooms(query, "room_id");
        return store.page(store.hygieneChecks, query, req, HygieneCheckResp.class);
    }
    public List<Ranking> ranking(String period, LocalDate date) {
        DormAccess.Scope scope = access.staff("dorm:hygiene:query");
        require(date != null && Arrays.asList("WEEK", "MONTH").contains(period), "排名周期只能为WEEK或MONTH");
        LocalDate start = "WEEK".equals(period) ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : date.withDayOfMonth(1);
        LocalDate end = "WEEK".equals(period) ? start.plusWeeks(1) : start.plusMonths(1);
        QueryWrapper<DormHygieneCheckDO> query = DormStore.<DormHygieneCheckDO>query().ge("check_date", start)
                .lt("check_date", end).orderByDesc("check_date", "id"); scope.rooms(query, "room_id");
        Map<Long, List<DormHygieneCheckDO>> grouped = store.hygieneChecks.selectList(query).stream()
                .collect(Collectors.groupingBy(DormHygieneCheckDO::getRoomId));
        List<Ranking> result = new ArrayList<>();
        grouped.forEach((id, rows) -> {
            Ranking rank = new Ranking(); rank.setRoomId(id); rank.setRoomLabel(rows.get(0).getRoomLabel()); rank.setCheckedDays(rows.size());
            int sum = rows.stream().mapToInt(DormHygieneCheckDO::getScore).sum();
            rank.setAverageScore(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP));
            result.add(rank);
        });
        result.sort(Comparator.comparing(Ranking::getAverageScore).reversed().thenComparing(Ranking::getRoomId));
        for (int i = 0; i < result.size(); i++) {
            Ranking row = result.get(i);
            row.setRank(i > 0 && row.getAverageScore().compareTo(result.get(i - 1).getAverageScore()) == 0
                    ? result.get(i - 1).getRank() : i + 1);
        }
        return result;
    }
}
