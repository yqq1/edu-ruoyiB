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
@RequestMapping("/dorm/building")
@Validated
@Tag(name = "宿舍管理 - building")
public class DormBuildingController {
    @Resource private DormResourceService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('dorm:building:create')")
    @Operation(summary = "创建宿舍楼")
    public CommonResult<Long> create(@Valid @RequestBody BuildingSave req) {
        return success(service.saveBuilding(req, true));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('dorm:building:update')")
    @Operation(summary = "修改宿舍楼")
    public CommonResult<Boolean> update(@Valid @RequestBody BuildingSave req) {
        service.saveBuilding(req, false);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('dorm:building:delete')")
    @Operation(summary = "删除宿舍楼")
    public CommonResult<Boolean> delete(@Valid VersionReq req) {
        service.deleteBuilding(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:building:query')")
    @Operation(summary = "宿舍楼详情")
    public CommonResult<BuildingResp> get(@RequestParam @Positive Long id) {
        return success(service.getBuilding(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:building:query')")
    @Operation(summary = "宿舍楼分页")
    public CommonResult<PageResult<BuildingResp>> page(@Valid DormQuery req) {
        return success(service.buildingPage(req));
    }
}
