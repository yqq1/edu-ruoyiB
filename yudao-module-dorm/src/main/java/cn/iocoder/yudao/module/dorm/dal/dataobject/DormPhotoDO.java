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
@TableName("dorm_photo")
public class DormPhotoDO extends DormRecordDO {
    private Long tripId;
    private String contentType;
    private Integer size;
    @TableField(select = false)
    @lombok.ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] content;
}
