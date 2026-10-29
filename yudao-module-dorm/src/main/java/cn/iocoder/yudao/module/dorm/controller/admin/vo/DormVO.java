package cn.iocoder.yudao.module.dorm.controller.admin.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

public final class DormVO {
    private DormVO() {}
    @Data
    public static class RecordResp {
        private Long id;
        private Long rowVersion;
        private LocalDateTime createTime;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Building响应")
    public static class BuildingResp extends RecordResp {
        private String name;
        private Integer floors;
        private String gender;
        private Integer status;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Room响应")
    public static class RoomResp extends RecordResp {
        private String buildingName;
        private String gender;
        private Long buildingId;
        private String roomNo;
        private Integer floorNumber;
        private Integer status;
        private long bedCount;
        private long allocatedCount;
        private long occupiedCount;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Bed响应")
    public static class BedResp extends RecordResp {
        private Long buildingId;
        private String buildingName;
        private String roomNo;
        private String bedLabel;
        private Long roomId;
        private String bedNo;
        private Integer status;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Stay响应")
    public static class StayResp extends RecordResp {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long classId;
        private String className;
        private String gender;
        private Long bedId;
        private Long roomId;
        private Long buildingId;
        private String bedLabel;
        private String status;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private BigDecimal depositAmount;
        private BigDecimal deductionAmount;
        private BigDecimal refundAmount;
        private String inspection;
        private String deductionReason;
        private String refundStatus;
        private LocalDateTime refundTime;
        private Long refundAccountId;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "ChangeRequest响应")
    public static class ChangeRequestResp extends RecordResp {
        private String studentNo;
        private String studentName;
        private String className;
        private Long stayId;
        private Long studentId;
        private Long classId;
        private Long applicantId;
        private Long sourceBedId;
        private String sourceBedLabel;
        private Long targetBedId;
        private String targetBedLabel;
        private Long stayVersion;
        private String reason;
        private String source;
        private String status;
        private Long reviewerId;
        private LocalDateTime reviewedAt;
        private String reviewReason;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "NightCheck响应")
    public static class NightCheckResp extends RecordResp {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long classId;
        private String className;
        private Long stayId;
        private Long roomId;
        private String bedLabel;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate checkDate;
        private String status;
        private String reason;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Trip响应")
    public static class TripResp extends RecordResp {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long classId;
        private String className;
        private String departRequestId;
        private String returnRequestId;
        private LocalDateTime departTime;
        private BigDecimal departLatitude;
        private BigDecimal departLongitude;
        private BigDecimal departAccuracy;
        private LocalDateTime departCapturedAt;
        private LocalDateTime returnTime;
        private BigDecimal returnLatitude;
        private BigDecimal returnLongitude;
        private BigDecimal returnAccuracy;
        private LocalDateTime returnCapturedAt;
        private String reviewStatus;
        private Long reviewerId;
        private LocalDateTime reviewedAt;
        private String reviewReason;
        private Long photoId;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "HygieneCheck响应")
    public static class HygieneCheckResp extends RecordResp {
        private Long roomId;
        private String roomLabel;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate checkDate;
        private Integer score;
        private String remark;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Violation响应")
    public static class ViolationResp extends RecordResp {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long classId;
        private String className;
        private Long stayId;
        private Long roomId;
        private String bedLabel;
        private LocalDateTime occurredAt;
        private String type;
        private String description;
        private Long disciplineId;
    }
    @Data
    public static class ReferenceResp {
        private boolean mock;
        private String actorType;
        private Long studentId;
        private Map<Long, String> classes;
    }
    @Data
    public static class Ranking {
        private Long roomId;
        private String roomLabel;
        private BigDecimal averageScore;
        private long checkedDays;
        private int rank;
    }
    @Data
    public static class RosterItem {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate checkDate;
        private StayResp stay;
        private NightCheckResp check;
    }
}
