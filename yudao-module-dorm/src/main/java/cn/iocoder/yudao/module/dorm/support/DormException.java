package cn.iocoder.yudao.module.dorm.support;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import static cn.iocoder.yudao.module.dorm.enums.ErrorCodeConstants.*;

public final class DormException {
    private DormException() {}
    public static void require(boolean condition, String message) {
        require(condition, DORM_INVALID, message);
    }
    public static void require(boolean condition, ErrorCode code, String message) {
        if (!condition) throw new ServiceException(code.getCode(), message);
    }
}
