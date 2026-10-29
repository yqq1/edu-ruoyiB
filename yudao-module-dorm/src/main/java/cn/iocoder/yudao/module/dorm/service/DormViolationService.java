package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.ViolationResp;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.*;

public interface DormViolationService {
    Long save(ViolationSave req, boolean create);
    void link(DisciplineReq req);
    ViolationResp get(Long id);
    PageResult<ViolationResp> page(DormQuery req);
}
