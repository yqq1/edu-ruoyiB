package cn.iocoder.yudao.module.dorm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    ErrorCode DORM_INVALID = new ErrorCode(1_032_000_001, "住宿操作不满足约束");
    ErrorCode DORM_FORBIDDEN = new ErrorCode(1_032_000_002, "无权执行住宿操作");
    ErrorCode DORM_CONFLICT = new ErrorCode(1_032_000_003, "住宿数据或版本已变化");
    ErrorCode DORM_REFERENCE_UNAVAILABLE = new ErrorCode(1_032_000_004, "住宿公共数据不可用");
}
