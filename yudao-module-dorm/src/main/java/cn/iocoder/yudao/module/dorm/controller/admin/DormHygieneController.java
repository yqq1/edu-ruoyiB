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
@RequestMapping("/dorm/hygiene")
@Validated
@Tag(name = "宿舍管理 - hygiene")
public class DormHygieneController {
    @Resource private DormHygieneService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('dorm:hygiene:update')")
    @Operation(summary = "录入卫生评分")
    public CommonResult<Long> create(@Valid @RequestBody HygieneSave req) {
        return success(service.save(req, true));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('dorm:hygiene:update')")
    @Operation(summary = "更正卫生评分")
    public CommonResult<Boolean> update(@Valid @RequestBody HygieneSave req) {
        service.save(req, false);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:hygiene:query')")
    @Operation(summary = "卫生检查详情")
    public CommonResult<HygieneCheckResp> get(@RequestParam @Positive Long id) {
        return success(service.get(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:hygiene:query')")
    @Operation(summary = "卫生检查分页")
    public CommonResult<PageResult<HygieneCheckResp>> page(@Valid DormQuery req) {
        return success(service.page(req));
    }

    @GetMapping("/ranking")
    @PreAuthorize("@ss.hasPermission('dorm:hygiene:query')")
    @Operation(summary = "卫生周月排名")
    public CommonResult<List<Ranking>> ranking(@RequestParam @Pattern(regexp = "WEEK|MONTH") String period, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return success(service.ranking(period, date));
    }
}
