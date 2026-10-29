package cn.iocoder.yudao.module.dorm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormVO.*;
import cn.iocoder.yudao.module.dorm.controller.admin.vo.DormCheckVO.HygieneSave;
import java.time.LocalDate;
import java.util.List;

public interface DormHygieneService {
    Long save(HygieneSave req, boolean create);
    HygieneCheckResp get(Long id);
    PageResult<HygieneCheckResp> page(DormQuery req);
    List<Ranking> ranking(String period, LocalDate date);
}
