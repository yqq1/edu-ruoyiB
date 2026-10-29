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
@RequestMapping("/dorm/room")
@Validated
@Tag(name = "宿舍管理 - room")
public class DormRoomController {
    @Resource private DormResourceService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('dorm:room:create')")
    @Operation(summary = "创建房间")
    public CommonResult<Long> create(@Valid @RequestBody RoomSave req) {
        return success(service.saveRoom(req, true));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('dorm:room:update')")
    @Operation(summary = "修改房间")
    public CommonResult<Boolean> update(@Valid @RequestBody RoomSave req) {
        service.saveRoom(req, false);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('dorm:room:delete')")
    @Operation(summary = "删除房间")
    public CommonResult<Boolean> delete(@Valid VersionReq req) {
        service.deleteRoom(req);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:room:query')")
    @Operation(summary = "房间详情")
    public CommonResult<RoomResp> get(@RequestParam @Positive Long id) {
        return success(service.getRoom(id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('dorm:room:query')")
    @Operation(summary = "房间分页")
    public CommonResult<PageResult<RoomResp>> page(@Valid DormQuery req) {
        return success(service.roomPage(req));
    }

    @GetMapping("/beds")
    @PreAuthorize("@ss.hasPermission('dorm:room:query')")
    @Operation(summary = "房间床位")
    public CommonResult<List<BedResp>> beds(@RequestParam @Positive Long roomId) {
        return success(service.beds(roomId));
    }

    @PutMapping("/bed/update")
    @PreAuthorize("@ss.hasPermission('dorm:room:update')")
    @Operation(summary = "启停床位")
    public CommonResult<Boolean> bed_update(@Valid @RequestBody BedSave req) {
        service.saveBed(req);
        return success(true);
    }

    @GetMapping("/bed-options")
    @PreAuthorize("@ss.hasPermission('dorm:bed:query')")
    @Operation(summary = "空床选项")
    public CommonResult<PageResult<BedResp>> bedOptions(@Valid DormQuery req) {
        return success(service.bedOptions(req));
    }
}
