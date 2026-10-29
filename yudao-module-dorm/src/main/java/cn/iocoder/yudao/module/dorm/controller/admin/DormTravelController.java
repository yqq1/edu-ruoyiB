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
@RequestMapping("/dorm/travel")
@Validated
@Tag(name = "宿舍管理 - travel")
public class DormTravelController {
    @Resource private DormTravelService service;

    @GetMapping("/context")
    @PreAuthorize("@ss.hasPermission('dorm:travel:check-in')")
    @Operation(summary = "本人打卡上下文与服务器时间")
    public CommonResult<ContextResp> context() {
        return success(service.context());
    }

    @PostMapping("/depart")
    @PreAuthorize("@ss.hasPermission('dorm:travel:check-in')")
    @Operation(summary = "离校打卡（data为JSON，photo为图片）")
    public CommonResult<TripResp> depart(@Valid @RequestPart("data") PositionReq req, @RequestPart("photo") org.springframework.web.multipart.MultipartFile photo) {
        return success(service.depart(req, photo));
    }

    @PostMapping("/return")
    @PreAuthorize("@ss.hasPermission('dorm:travel:check-in')")
    @Operation(summary = "返校打卡")
    public CommonResult<TripResp> returnToSchool(@Valid @RequestBody ReturnReq req) {
        return success(service.returnToSchool(req));
    }

    @PostMapping("/review")
    @PreAuthorize("@ss.hasPermission('dorm:travel:review')")
    @Operation(summary = "审核离校记录")
    public CommonResult<Boolean> review(@Valid @RequestBody TravelReview req) {
        service.review(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:travel:query')")
    @Operation(summary = "离返校详情")
    public CommonResult<TripResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:travel:query')")
    @Operation(summary = "离返校分页")
    public CommonResult<PageResult<TripResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('dorm:travel:query')")
    @Operation(summary = "按出行记录的班级汇总")
    public CommonResult<List<ClassSummary>> summary(@Valid DormQuery req) {
        return success(service.summary(req));
    }
}
