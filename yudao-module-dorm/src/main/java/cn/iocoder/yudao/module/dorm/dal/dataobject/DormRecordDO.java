package cn.iocoder.yudao.module.dorm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class DormRecordDO extends TenantBaseDO {
    private Long id;
    private Long rowVersion;
}
