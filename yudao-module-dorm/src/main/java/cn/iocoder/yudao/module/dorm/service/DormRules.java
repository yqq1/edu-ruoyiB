package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormQuery;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.*;

public final class DormRules {
    private DormRules() {}
    public static LocalDateTime now() {
        require(ZoneId.systemDefault().getRules().equals(ZoneId.of("Asia/Shanghai").getRules()),
                "宿舍运行时区须为Asia/Shanghai，请设置-Duser.timezone=Asia/Shanghai");
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
    public static void id(Long id) { require(id != null && id > 0, "标识无效"); }
    public static void version(Long expected, Long actual) {
        require(expected != null && expected.equals(actual), DORM_CONFLICT, "数据版本已变化，请刷新后重试");
    }
    public static void page(PageParam q) {
        require(q != null && q.getPageNo() != null && q.getPageNo() > 0 && q.getPageSize() != null
                && q.getPageSize() > 0 && q.getPageSize() <= 200, "分页参数无效，每页最多200条");
    }
    public static void dates(DormQuery q) {
        require(q.getStartDate() == null || q.getEndDate() == null || !q.getStartDate().isAfter(q.getEndDate()),
                "开始日期不能晚于结束日期");
    }
    public static void money(BigDecimal amount) {
        require(amount != null && amount.signum() >= 0 && amount.scale() <= 2
                && amount.compareTo(new BigDecimal("9999999999.99")) <= 0, "金额必须非负且最多两位小数");
    }
    public static boolean active(String status) {
        return "RESERVED".equals(status) || "OCCUPIED".equals(status);
    }
    public static Set<Long> ids(Collection<Long> ids) {
        require(ids != null && !ids.isEmpty() && ids.size() <= 1000, "标识列表不能为空且最多1000项");
        ids.forEach(DormRules::id);
        Set<Long> result = new LinkedHashSet<>(ids);
        require(result.size() == ids.size(), "标识列表不能重复");
        return result;
    }
}
