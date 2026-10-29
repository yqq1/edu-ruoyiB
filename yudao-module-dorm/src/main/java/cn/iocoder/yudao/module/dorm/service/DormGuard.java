package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.module.dorm.dal.mysql.DormOperationGuardMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.annotation.Resource;
import static cn.iocoder.yudao.module.dorm.support.DormException.require;

@Component
public class DormGuard {
    @Resource private DormOperationGuardMapper mapper;
    public void lock() {
        require(TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                && Integer.valueOf(TransactionDefinition.ISOLATION_READ_COMMITTED).equals(
                TransactionSynchronizationManager.getCurrentTransactionIsolationLevel()),
                "宿舍写事务必须使用READ_COMMITTED");
        Long tenant = DormStore.tenant();
        mapper.ensure(tenant);
        require(tenant.equals(mapper.lock(tenant)), "无法取得租户住宿事务锁");
    }
}
