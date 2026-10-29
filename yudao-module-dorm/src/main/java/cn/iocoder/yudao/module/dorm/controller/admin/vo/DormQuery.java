package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class DormQuery extends PageParam {
    @Positive private Long studentId;
    @Positive private Long classId;
    @Positive private Long roomId;
    @Positive private Long buildingId;
    @Positive private Long stayId;
    @Size(max = 100) private String keyword;
    @Size(max = 20) private String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate endDate;
}
