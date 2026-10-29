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
@TableName("dorm_change_request")
public class DormChangeRequestDO extends DormRecordDO {
    private Long stayId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long classId;
    private String className;
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
