package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.ChangeRequestResp;

public interface DormChangeService {
    Long create(ChangeCreateReq req);
    void cancel(VersionReq req);
    void approve(ReviewReq req);
    void reject(ReviewReq req);
    Long direct(ChangeCreateReq req);
    ChangeRequestResp get(Long id);
    PageResult<ChangeRequestResp> page(DormQuery req);
}
