package cn.iocoder.yudao.module.dorm.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormResourceVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormTravelVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/dorm/night-check")
@Validated
@Tag(name = "宿舍管理 - night-check")
public class DormNightController {
    @Resource private DormNightService service;

    @GetMapping("/roster")
    @PreAuthorize("@ss.hasPermission('dorm:night-check:query')")
    @Operation(summary = "当日查寝名单")
    public CommonResult<PageResult<RosterItem>> roster(@Valid DormQuery req) {
        return success(service.roster(req));
    }

    @PostMapping("/batch-save")
    @PreAuthorize("@ss.hasPermission('dorm:night-check:update')")
    @Operation(summary = "保存查寝结果")
    public CommonResult<Boolean> batchSave(@Valid @RequestBody NightBatch req) {
        service.save(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:night-check:query')")
    @Operation(summary = "查寝详情")
    public CommonResult<NightCheckResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:night-check:query')")
    @Operation(summary = "查寝历史分页")
    public CommonResult<PageResult<NightCheckResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }
}
