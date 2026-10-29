package cn.iocoder.yudao.module.dorm.controller.admin;

import cn.iocoder.yudao.module.dorm.service.DormTravelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.Positive;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/dorm/photo")
@Validated
@Tag(name = "宿舍管理 - 私有照片")
public class DormPhotoController {
    @Resource private DormTravelService service;
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('dorm:photo:query')")
    @Operation(summary = "读取本人或授权学生的离校照片")
    public void get(@RequestParam @Positive Long id, HttpServletResponse response) throws IOException {
        DormTravelService.PhotoPayload photo = service.photo(id);
        response.setContentType(photo.getContentType());
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Disposition", "inline; filename=\"dorm-photo\"");
        response.setContentLength(photo.getContent().length);
        response.getOutputStream().write(photo.getContent());
    }
}
