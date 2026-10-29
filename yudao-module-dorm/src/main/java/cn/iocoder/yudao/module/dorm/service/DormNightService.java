package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.*;

public interface DormNightService {
    PageResult<RosterItem> roster(DormQuery req);
    void save(NightBatch req);
    NightCheckResp get(Long id);
    PageResult<NightCheckResp> page(DormQuery req);
}
