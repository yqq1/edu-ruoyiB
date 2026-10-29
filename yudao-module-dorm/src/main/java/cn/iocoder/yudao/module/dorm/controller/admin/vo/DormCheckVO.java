package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.VersionReq;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class DormCheckVO {
    private DormCheckVO() {}
    @Data
    public static class NightItem {
        @Positive private Long id;
        @Positive private Long rowVersion;
        @NotNull @Positive private Long stayId;
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") private LocalDate checkDate;
        @NotBlank @Pattern(regexp = "PRESENT|ABSENT") private String status;
        @Size(max = 500) private String reason;
    }
    @Data
    public static class NightBatch {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") private LocalDate rosterDate;
        @NotEmpty @Size(max = 200) @Valid private List<NightItem> items;
    }
    @Data
    public static class HygieneSave {
        @Positive private Long id;
        @Positive private Long rowVersion;
        @NotNull @Positive private Long roomId;
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") private LocalDate checkDate;
        @NotNull @Min(0) @Max(100) private Integer score;
        @Size(max = 1000) private String remark;
    }
    @Data
    public static class ViolationSave {
        @Positive private Long id;
        @Positive private Long rowVersion;
        @NotNull @Positive private Long stayId;
        @NotNull private LocalDateTime occurredAt;
        @NotBlank @Size(max = 50) private String type;
        @NotBlank @Size(max = 2000) private String description;
        @Positive private Long disciplineId;
    }
    @Data
    public static class DisciplineReq {
        @NotNull @Positive private Long id;
        @NotNull @Positive private Long rowVersion;
        @NotNull @Positive private Long disciplineId;
    }
}
