package cn.iocoder.yudao.module.dorm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dorm_trip")
public class DormTripDO extends DormRecordDO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long classId;
    private String className;
    private String departRequestId;
    private String departFingerprint;
    private String returnRequestId;
    private String returnFingerprint;
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
