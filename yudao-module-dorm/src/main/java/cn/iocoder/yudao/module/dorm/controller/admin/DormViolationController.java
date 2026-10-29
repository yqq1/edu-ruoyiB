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
@RequestMapping("/dorm/violation")
@Validated
@Tag(name = "宿舍管理 - violation")
public class DormViolationController {
    @Resource private DormViolationService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('dorm:violation:update')")
    @Operation(summary = "录入违规")
    public CommonResult<Long> create(@Valid @RequestBody ViolationSave req) {
        return success(service.save(req, true));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('dorm:violation:update')")
    @Operation(summary = "更正违规记录")
    public CommonResult<Boolean> update(@Valid @RequestBody ViolationSave req) {
        service.save(req, false);
        return success(true);
    }

    @PutMapping("/link-discipline")
    @PreAuthorize("@ss.hasPermission('dorm:violation:update')")
    @Operation(summary = "关联纪律处分")
    public CommonResult<Boolean> linkDiscipline(@Valid @RequestBody DisciplineReq req) {
        service.link(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:violation:query')")
    @Operation(summary = "违规详情")
    public CommonResult<ViolationResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:violation:query')")
    @Operation(summary = "违规分页")
    public CommonResult<PageResult<ViolationResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }
}
