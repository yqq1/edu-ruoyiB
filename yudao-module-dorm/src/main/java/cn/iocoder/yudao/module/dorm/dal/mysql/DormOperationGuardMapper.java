package cn.iocoder.yudao.module.dorm.dal.mysql;

import org.apache.ibatis.annotations.*;

@Mapper
public interface DormOperationGuardMapper {
    @Insert("INSERT INTO dorm_operation_guard (tenant_id) VALUES (#{tenantId}) ON DUPLICATE KEY UPDATE tenant_id=#{tenantId}")
    int ensure(@Param("tenantId") Long tenantId);
    @Select("SELECT tenant_id FROM dorm_operation_guard WHERE tenant_id=#{tenantId} FOR UPDATE")
    Long lock(@Param("tenantId") Long tenantId);
}
