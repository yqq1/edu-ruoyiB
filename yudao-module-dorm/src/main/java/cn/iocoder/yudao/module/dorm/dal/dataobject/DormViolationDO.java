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
@TableName("dorm_violation")
public class DormViolationDO extends DormRecordDO {
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
