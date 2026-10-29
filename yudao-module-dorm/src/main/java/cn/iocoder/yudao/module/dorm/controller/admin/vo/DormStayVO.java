package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class DormStayVO {
    private DormStayVO() {}
    @Data
    public static class VersionReq {
        @NotNull @Positive private Long id;
        @NotNull @Positive private Long rowVersion;
    }
    @Data
    public static class AssignReq {
        @NotNull @Positive private Long studentId;
        @NotNull @Positive private Long bedId;
    }
    @Data
    public static class AutoAssignReq {
        @NotEmpty @Size(max = 200) private List<@NotNull @Positive Long> studentIds;
        @NotEmpty @Size(max = 100) private List<@NotNull @Positive Long> buildingIds;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CheckInReq extends VersionReq {
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) private BigDecimal depositAmount;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CheckOutReq extends VersionReq {
        @NotBlank @Size(max = 1000) private String inspection;
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) private BigDecimal deductionAmount;
        @Size(max = 500) private String deductionReason;
    }
    @Data
    public static class ChangeCreateReq {
        @NotNull @Positive private Long stayId;
        @NotNull @Positive private Long stayVersion;
        @NotNull @Positive private Long targetBedId;
        @NotBlank @Size(max = 500) private String reason;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ReviewReq extends VersionReq {
        @Size(max = 500) private String reason;
    }
}
