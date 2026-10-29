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
@TableName("dorm_night_check")
public class DormNightCheckDO extends DormRecordDO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long classId;
    private String className;
    private Long stayId;
    private Long roomId;
    private String bedLabel;
    private LocalDate checkDate;
    private String status;
    private String reason;
}
