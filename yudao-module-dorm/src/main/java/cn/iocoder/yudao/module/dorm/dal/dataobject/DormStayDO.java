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
@TableName("dorm_stay")
public class DormStayDO extends DormRecordDO {
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
