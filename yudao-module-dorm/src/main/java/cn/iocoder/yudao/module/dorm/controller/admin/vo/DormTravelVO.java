package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class DormTravelVO {
    private DormTravelVO() {}
    @Data
    public static class PositionReq {
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{16,64}") private String requestId;
        @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 2, fraction = 7) private BigDecimal latitude;
        @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 7) private BigDecimal longitude;
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("99999999.99")
        @Digits(integer = 8, fraction = 2) private BigDecimal accuracy;
        @NotNull private LocalDateTime capturedAt;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ReturnReq extends PositionReq {
        @NotNull @Positive private Long tripId;
    }
    @Data
    public static class TravelReview {
        @NotNull @Positive private Long id;
        @NotNull @Positive private Long rowVersion;
        @NotBlank @Pattern(regexp = "APPROVED|REJECTED") private String status;
        @NotBlank @Size(max = 500) private String reason;
    }
    @Data
    public static class ContextResp {
        private LocalDateTime serverTime;
        private DormVO.TripResp activeTrip;
    }
    @Data
    public static class ClassSummary {
        private Long classId;
        private String className;
        private long totalDepartures;
        private long returned;
        private long outside;
    }
}
