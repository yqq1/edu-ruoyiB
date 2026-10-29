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
@RequestMapping("/dorm/stay")
@Validated
@Tag(name = "宿舍管理 - stay")
public class DormStayController {
    @Resource private DormStayService service;

    @PostMapping("/assign")
    @PreAuthorize("@ss.hasPermission('dorm:stay:assign')")
    @Operation(summary = "手动分配床位")
    public CommonResult<Long> assign(@Valid @RequestBody AssignReq req) {
        return success(service.assign(req));
    }

    @PostMapping("/auto-assign")
    @PreAuthorize("@ss.hasPermission('dorm:stay:assign')")
    @Operation(summary = "批量自动分配")
    public CommonResult<List<Long>> autoAssign(@Valid @RequestBody AutoAssignReq req) {
        return success(service.autoAssign(req));
    }

    @PostMapping("/cancel")
    @PreAuthorize("@ss.hasPermission('dorm:stay:cancel')")
    @Operation(summary = "取消待入住")
    public CommonResult<Boolean> cancel(@Valid @RequestBody VersionReq req) {
        service.cancel(req);
        return success(true);
    }

    @PostMapping("/check-in")
    @PreAuthorize("@ss.hasPermission('dorm:stay:check-in')")
    @Operation(summary = "办理入住")
    public CommonResult<Boolean> checkIn(@Valid @RequestBody CheckInReq req) {
        service.checkIn(req);
        return success(true);
    }

    @PostMapping("/check-out")
    @PreAuthorize("@ss.hasPermission('dorm:stay:check-out')")
    @Operation(summary = "办理退宿")
    public CommonResult<Boolean> checkOut(@Valid @RequestBody CheckOutReq req) {
        service.checkOut(req);
        return success(true);
    }

    @PostMapping("/confirm-refund")
    @PreAuthorize("@ss.hasPermission('dorm:stay:refund')")
    @Operation(summary = "确认线下退款")
    public CommonResult<Boolean> confirmRefund(@Valid @RequestBody VersionReq req) {
        service.refund(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:stay:query')")
    @Operation(summary = "住宿详情")
    public CommonResult<StayResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:stay:query')")
    @Operation(summary = "住宿分页")
    public CommonResult<PageResult<StayResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }

    @GetMapping("/my")
    @PreAuthorize("@ss.hasPermission('dorm:stay:my')")
    @Operation(summary = "本人住宿")
    public CommonResult<PageResult<StayResp>> my(@Valid DormQuery req) {
        return success(service.my(req));
    }
}
