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
@RequestMapping("/dorm/change-request")
@Validated
@Tag(name = "宿舍管理 - change-request")
public class DormChangeController {
    @Resource private DormChangeService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:create')")
    @Operation(summary = "申请调宿")
    public CommonResult<Long> create(@Valid @RequestBody ChangeCreateReq req) {
        return success(service.create(req));
    }

    @PostMapping("/cancel")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:cancel')")
    @Operation(summary = "取消本人申请")
    public CommonResult<Boolean> cancel(@Valid @RequestBody VersionReq req) {
        service.cancel(req);
        return success(true);
    }

    @PostMapping("/approve")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:approve')")
    @Operation(summary = "通过调宿")
    public CommonResult<Boolean> approve(@Valid @RequestBody ReviewReq req) {
        service.approve(req);
        return success(true);
    }

    @PostMapping("/reject")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:approve')")
    @Operation(summary = "拒绝调宿")
    public CommonResult<Boolean> reject(@Valid @RequestBody ReviewReq req) {
        service.reject(req);
        return success(true);
    }

    @PostMapping("/direct-adjust")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:direct')")
    @Operation(summary = "工作人员调床")
    public CommonResult<Long> directAdjust(@Valid @RequestBody ChangeCreateReq req) {
        return success(service.direct(req));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:query')")
    @Operation(summary = "调宿申请详情")
    public CommonResult<ChangeRequestResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:change-request:query')")
    @Operation(summary = "调宿申请分页")
    public CommonResult<PageResult<ChangeRequestResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }
}
