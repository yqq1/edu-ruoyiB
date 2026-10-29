package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;

public final class DormResourceVO {
    private DormResourceVO() {}
    @Data
    @Schema(description = "楼栋保存参数")
    public static class BuildingSave {
        @Positive private Long id;
        @Positive private Long rowVersion;
        @NotBlank @Size(max = 100) private String name;
        @NotNull @Min(1) @Max(100) private Integer floors;
        @NotBlank @Pattern(regexp = "MALE|FEMALE|UNLIMITED") private String gender;
        @NotNull @Min(0) @Max(1) private Integer status;
    }
    @Data
    @Schema(description = "房间保存参数")
    public static class RoomSave {
        @Positive private Long id;
        @Positive private Long rowVersion;
        @NotNull @Positive private Long buildingId;
        @NotBlank @Size(max = 32) private String roomNo;
        @NotNull @Min(1) @Max(100) private Integer floorNumber;
        @NotNull @Min(0) @Max(1) private Integer status;
        @Min(1) @Max(100) private Integer bedCount;
    }
    @Data
    @Schema(description = "床位启停参数")
    public static class BedSave {
        @NotNull @Positive private Long id;
        @NotNull @Positive private Long rowVersion;
        @NotNull @Min(0) @Max(1) private Integer status;
    }
}
