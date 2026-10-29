package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormStayVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.StayResp;
import java.util.List;

public interface DormStayService {
    Long assign(AssignReq req);
    List<Long> autoAssign(AutoAssignReq req);
    void cancel(VersionReq req);
    void checkIn(CheckInReq req);
    void checkOut(CheckOutReq req);
    void refund(VersionReq req);
    StayResp get(Long id);
    PageResult<StayResp> page(DormQuery req);
    PageResult<StayResp> my(DormQuery req);
}
