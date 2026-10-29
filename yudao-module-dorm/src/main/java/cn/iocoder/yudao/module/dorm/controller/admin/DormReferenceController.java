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
@RequestMapping("/dorm/reference")
@Validated
@Tag(name = "宿舍管理 - reference")
public class DormReferenceController {
    @Resource private DormReferenceService service;

    @GetMapping("/options")
    @PreAuthorize("@ss.hasPermission('dorm:reference:query')")
    @Operation(summary = "宿舍公共选项")
    public CommonResult<ReferenceResp> options() {
        return success(service.options());
    }

    @GetMapping("/students")
    @PreAuthorize("@ss.hasPermission('dorm:reference:query')")
    @Operation(summary = "授权学生分页")
    public CommonResult<PageResult<DormReferenceService.StudentResp>> students(@Valid DormQuery req) {
        return success(service.students(req));
    }
}
